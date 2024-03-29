/*
 The Pixel Teleporter Library for Processing 3

 Makes it easier to prototype arrangements of LEDs by
 encapsulating everything needed to read and display pixels
 from a hardware LED controller. 

 Copyright 2020 JEM (ZRanger1)

 Permission is hereby granted, free of charge, to any person obtaining a copy of this
 software and associated documentation files (the "Software"), to deal in the Software
 without restriction, including without limitation the rights to use, copy, modify, merge,
 publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons
 to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or
 substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.

 Version  Date         Author Comment
 v0.0.1   08/20/2020   JEM(ZRanger1)    Created
 v0.0.2   09/08/2020   JEM(ZRanger1)    bug fixes, import/export map
 v1.0.0   09/15/2020   JEM(ZRanger1)    convert to Processing library
 */
package pixelTeleporter.library;

import java.util.*;
import processing.core.*;
import processing.data.JSONArray;
import processing.event.KeyEvent;
import processing.event.MouseEvent;


/**
    Manages communication with the PixelTeleporter server device
    and provides a framework for object rendering and basic user 
    camera control.
    <p>
	To use, create a PixelTeleporter object in your sketch and
	call its start() method to begin listening for pixel data
	on the network.
 */

/*
 TODO LIST
 JSON save/restore renderer!! (include frame rate/frame count in saved data so we can recover timing on playback.  Or
 maybe timestamp of each frame so we can make the deltas the same.)

 RenderType.USER renderer support??
 
 Recieve from multiple transport types - PT Classic, PT Broadcast, Artnet, etc..
 */

public class PixelTeleporter implements PConstants {
	PApplet app;	
	PixelTeleporterThread thread;
	Mover mover;
	PTBackground bg;
	int ledSize = 15;        
	int pixelSize = 20;    
	public int[] pixelBuffer;
	boolean uiActive = false;
	boolean autoDataActive = false;
	boolean isController = false;
	boolean isRunning = true;
	boolean showPixelInfo = false;
	boolean showAxes = true;
	TooltipHandler toolTip;
	
	LEDRenderer renderer = null;
	private final ptEventListener ptEventListener = new ptEventListener();

	// global pt object reference counter 
	static int refCount = 0; 

	//constants
	public final static String VERSION = "##library.prettyVersion##";	
	final int MAX_PIXELS = 4096;
	final int PIXEL_BUFFER_SIZE=(256+(MAX_PIXELS * 3)); 
	final int MOUSE_MIN_MOVEMENT = 10; // dead zone for mouse UI rotate/translate

	/**
	 * Creates and initializes a PixelTeleporter object.
	 *
	 * @param pApp Pointer to currently running PApplet
	 * @param ipAddr IPv4 address of PixelTeleporter server device
	 * @param serverPort Command receiver port on server device. Default: 8081  
	 * @param clientPort Port number to listen on. Default: 8082
	 */
	public PixelTeleporter(PApplet pApp,String ipAddr,int serverPort,int clientPort) {
		this.app = pApp;	

		mover = new Mover(this); 
		thread = new PixelTeleporterThread(this,ipAddr,clientPort,serverPort,PIXEL_BUFFER_SIZE);
		pixelBuffer = thread.getPixelBuffer();
		bg = new PTBackground(app);
		toolTip = new TooltipHandler();
		setRenderMethod(RenderMethod.DEFAULT);
		refCount++;

		// register cleanup function
		app.registerMethod("dispose", this);

		// enable automatic per frame data request/read
		enableAutoData();				

		// if we're the first PixelTeleporter object, enable keyboard/mouse UI,
		// configure default drawing settings and print welcome message
		if (refCount == 1) {
			isController = true;
			enableUI();

			app.colorMode(RGB, 255);     // RGB color, values from 0-255
			app.textMode(MODEL);         // draw text as textures
			app.noStroke();              // no outlining
			app.rectMode(CENTER);        // Rectangles are positioned based on their center.
			app.ellipseMode(CENTER);     // Ellipses are positioned based on their center.  
			app.imageMode(CENTER);       // Images at center too.
			app.sphereDetail(8);         // reduce number of sphere vertices for performance

			welcome();
		}		
	}

	/**
	 * Creates and initializes a PixelTeleporter object.  Alternate constructor
	 * to avoid breaking old scripts and firmware, this sets both client
	 * and server ports to the specified value.  
	 * !!!! It will definitely not work with any server/bridge software version after 1.0.0 
	 * Please update/reflash your devices and use the new version which requires two
	 * ports.
	 *
	 * @param pApp Pointer to currently running PApplet
	 * @param ipAddr IPv4 address of PixelTeleporter server device
	 * @param clientPort Port number to listen on. Default: 8081
	 */
	public PixelTeleporter(PApplet pApp,String ipAddr,int clientPort) {
		this(pApp,ipAddr,clientPort,clientPort);
	}

	/**
	 * Creates and initializes a PixelTeleporter object using default server/client
	 * UDP ports (8081,8082)
	 *
	 * @param pApp Pointer to currently running PApplet
	 * @param ipAddr IPv4 address of PixelTeleporter server device
	 */
	public PixelTeleporter(PApplet pApp,String ipAddr) {
		this(pApp,ipAddr,8081,8082);
	}

	/**
	 * Welcome.
	 */
	private void welcome() {
		System.out.println("\n##library.name## ##library.prettyVersion##");
	}  	

	/**
	 * Enables mouse/keyboard handlers.  These are
	 * enabled by default.  Call disableUI() in Setup() if you
	 * want to use your own handlers.
	 */
	public void enableUI() {
		if (uiActive) return;
		uiActive = true;
		app.registerMethod("mouseEvent", ptEventListener);
		app.registerMethod("keyEvent", ptEventListener);
	}

	/**
	 * Disables mouse/keyboard handlers.
	 *
	 */		
	public void disableUI() {
		if (!uiActive) return;
		uiActive = false;
		app.unregisterMethod("mouseEvent", ptEventListener);
		app.unregisterMethod("keyEvent", ptEventListener);	  
	}

	/**
	 * Returns state of PixelTeleporter's mouse/keyboard handlers
	 *
	 * @return true if enabled, false if disabled
	 */
	public boolean uiEnabled() {
		return uiActive;
	}	

	/**
	 * Enables automatic handling of per frame pixel request/read
	 * operations.  This is enabled by default. Call disableAutoData()
	 * in your sketch's Setup() function if you want to do it yourself.
	 *
	 */
	public void enableAutoData() {
		if (autoDataActive) return;

		autoDataActive = true;
		app.registerMethod("pre", this);
		app.registerMethod("post", this);
	}

	/**
	 * Disables automatic handling of per frame pixel request/read
	 * operations. 
	 *
	 */	
	public void disableAutoData() {
		if (!autoDataActive) return;

		autoDataActive = false;
		app.unregisterMethod("pre", this);
		app.unregisterMethod("post", this);
	}

	/**
	 * Returns state of PixelTeleporter's automatic data handler
	 *
	 * @return true if enabled, false if disabled
	 */
	public boolean autoDataEnabled() {
		return autoDataActive;
	}

	/**
	 * Enable display of per pixel tooltips on mouse hover
	 */
	public void enablePixelInfo() {
		showPixelInfo = true;
	}	

	/**
	 * Disable display of per pixel tooltips on mouse hover
	 */
	public void disablePixelInfo() {
		showPixelInfo = false;
	}	

	/**
	 * Returns state of PixelTeleporter's pixel index order labeling
	 *
	 * @return true if enabled, false if disabled
	 */
	public boolean pixelInfoEnabled() {
		return showPixelInfo;
	}

	/**
	 *  Start data transport and initialize camera UI if enabled
	 */
	public void start() {
		if (uiActive) mover.initializeCamera();
		thread.start();
	}

	/**
	  To be called in draw() prior to rendering. Asks the transport to copy any 
	  pixel data it has recieved from the network to the internal ARGB pixel
	  buffer, which we can use more easily for rendering. 
	 * @return number of pixels copied
	 */
	public int readData() {
		return thread.readData();
	}

	/**
 		Tells the transport to request a frame of pixel data from the
 		server. Called from draw() when ready to render pixels, although
 		pixels will not be available for drawing until after the transport has
 		received them from the network, and reformatted them into ARGB data
 		via the readData() method.
	 */	
	public void requestData() {
		thread.requestData();
	}  

	/**
	 * Gets the color of a pixel
	 * @param index - index of pixel to be retrieved
	 * @return - ARGB color of pixel at specified index
	 * @note NO PARAMETER VALIDATION HERE. It's supposed to be fast! So be careful
	 * not to request pixels outside the valid range (0-4095) unless you just happen to like
	 * being shut down by exceptions.
	 */
	public int getPixel(int index) {
		return pixelBuffer[index];
	}	

	/**
	 * Apply current viewing transform to all displayable objects.
	 * Should be called in draw() before rendering your displayable
	 * objects.  See examples for details.
	 */
	public void applyViewingTransform() {
		mover.applyViewingTransform();
	}

	//various setter/getter functions

	/** Sets size of displayed led "pixels"
	 * <p>
	 *  Total size includes space around the lighted portion.
	 *  @param n total size of pixel element in world units
	 *  @param pct (0-100) sets the percentage of the total that will be "lighted."
	 */
	public void setElementSize(int n,int pct) {
		pixelSize = n;
		ledSize = pct * pixelSize / 100;
	}

	/**
	 * Sets the size of displayed pixels with the illuminated
	 * portion defaulting to 40% of the total size.
	 *
	 * @param n total size of pixel element in world units
	 */
	public void setElementSize(int n) { setElementSize(n,40); }	

	/**
	 * Gets the current pixel size.
	 *
	 * @return size of LED "element" in world coords
	 */
	public int getElementSize() { return pixelSize; }

	/**
	 * Set the center of rotation to specified x,y,z position.
	 *
	 * @param x 
	 * @param y 
	 * @param z 
	 */
	public void setObjectCenter(float x, float y, float z) { mover.setObjectCenter(x,y,z); }

	/**
	 * Set current rotation of displayable object to specified x,y,z angle (radians).
	 *
	 * @param x 
	 * @param y 
	 * @param z 
	 */	
	public void setRotation(float x, float y, float z) { mover.setRotation(x,y,z); }


	/**
	 * Set rotation rate of displayable object to x,y,z radians/millisecond.
	 *
	 * @param x 
	 * @param y 
	 * @param z 
	 */		
	public void setRotationRate(float x, float y, float z) { mover.setRotationRate(x,y,z); } 

	/**
	 * Loads an image file for to use as a background.  Supported formats are: 
	 * (.gif, .jpg, .tga, and .png)
	 *  
	 * If the file is not available or an error occurs, no background will be created and
	 * a non-fatal error message will be printed to the console.
	 * 
	 * @param imgPath An absolute path to a local file, or the URL of an image on the Web.
	 */		
	public void setBackgroundImage(String imgPath) {
		bg.load(imgPath);
	} 	

	/**
 		Called when the library shuts down.
 		<p>
 		Closes active devices and shuts down the transport listener
 		thread.
	 */		
	public void dispose() {
		disableUI();
		disableAutoData();
		thread.quit();
		refCount--;
	}  

	/**
	 * 	Creates ScreenLED object with x,y,z coords initialized to 0.
	 * 
	 * @return new ScreenLED object
	 */		
	public ScreenLED ScreenLEDFactory() {
		return new ScreenLED(this);
	}

	/**
	 	Creates ScreenLED object using x and y coords
	 	<p>
	 	(the z coordinate will be initialized to 0.)
		@param x x coordinate in 3D worldspace
	 	@param y y coordinate in 3D worldspace
	 	@return new ScreenLED object
	 */		
	public ScreenLED ScreenLEDFactory(float x, float y) {
		return new ScreenLED(this,x,y);
	}	

	/**
	 * 	 	Creates ScreenShape object using supplied shape
	 * 	 	<p>
	 * @param s PShape to attach to ScreenShape object
	 * @return new ScreenLED object
	 */	
	public ScreenShape ScreenShapeFactory(PShape s) {
		return new ScreenShape(this,s,255);
	}

	/**
	 * 	 	Creates ScreenShape object using supplied shape
	 * 	 	<p>
	 * @param s PShape to attach to ScreenShape object
	 * @param opacity optional opacity for this object (int 0-255, default == 255)
	 * @return new ScreenLED object
	 */	
	public ScreenShape ScreenShapeFactory(PShape s, int opacity) {
		return new ScreenShape(this,s,opacity);
	}		

	/**
	 * 	 	Creates ScreenLED object using x,y,z coords
	 * 	 	<p>.
	 *
	 * @param x x coordinate in 3D worldspace
	 * @param y y coordinate in 3D worldspace
	 * @param z z coordinate in 3D worldspace
	 * @return new ScreenLED object
	 */		
	public ScreenLED ScreenLEDFactory(float x, float y, float z) {
		return new ScreenLED(this,x,y,z);
	}

	/**
	 * Read a Pixelblaze compatible pixel map into a list of ScreenLED objects.
	 * @param fileName Name of file to write 
	 * @param scale Coordinate multiplier for scaling output 
	 * @return Linked list of ScreenLEDs corresponding to the pixel map if successful,
	 * null if unable to read the specified file.
	 */
	public LinkedList<ScreenLED> importPixelblazeMap(String fileName,float scale) {
		LinkedList<ScreenLED> obj = new LinkedList<ScreenLED>();
		JSONArray json = app.loadJSONArray(fileName);

		// read the map
		for (int i = 0; i < json.size(); i++) {
			float x,y,z;

			JSONArray mapEntry = json.getJSONArray(i);
			float [] coords = mapEntry.getFloatArray();  

			x = coords[0];
			y = coords[1];
			z = (coords.length == 3) ? z = coords[2] : 0;

			ScreenLED led = new ScreenLED(this,scale * x,scale * y,scale * z);
			led.setIndex(i);
			obj.add(led);
		}
		
		// adjust to center the object at (0,0,0) in world space
		PVector center = findObjectCenter(obj);
		for (ScreenLED p : obj) {
			p.x -= center.x;
			p.y -= center.y;
			p.z -= center.z;			
		}
		return obj;
	}

	/**
	 * comparator for sorting.  Used by exportPixeblazeMap()
	 */	
	class compareLEDIndex implements Comparator<ScreenLED> {

		@Override
		public int compare(ScreenLED p1, ScreenLED p2) {
			return (p1.index < p2.index) ? -1 : 1;
		}
	}

	/**
	 * Convert a list of ScreenLEDs to a Pixelblaze compatible JSON pixel map and
	 * write it to the specified file.
	 * @param obj Linked list of ScreenLEDs representing a displayable object
	 * @param fileName Name of file to write 
	 * @param scale Coordinate multiplier for scaling final output 
	 * @param is3D true for 3D (xyz), false for 2D (xy) 
	 * @return true if successful, false otherwise
	 */
	public boolean exportPixelblazeMap(LinkedList<ScreenLED> obj,String fileName,float scale, boolean is3D) {
		JSONArray json,mapEntry;

		//sort object by pixel index for export. 
		@SuppressWarnings("unchecked")
		LinkedList<ScreenLED> sortedCopy = (LinkedList<ScreenLED>) obj.clone();
		Collections.sort(sortedCopy,new compareLEDIndex());

		json = new JSONArray();
		for (ScreenLED led : sortedCopy) {
			mapEntry = new JSONArray();
			mapEntry.append(scale * led.x);
			mapEntry.append(scale * led.y);
			if (is3D) mapEntry.append(scale * led.z);    

			json.append(mapEntry);
		}  
		return app.saveJSONArray(json,fileName);  
	}

	/**
	 * Find geometric center of object represented by ScreenLED list.
	 *
	 * @param obj Linked list of ScreenLEDs representing a displayable object
	 * @return PVector with x,y,z set to object center
	 */
	public PVector findObjectCenter( LinkedList<ScreenLED> obj) {
		PVector c = new PVector(0,0,0);
		PVector mins = new PVector(0,0,0);
		PVector maxes = new PVector(0,0,0);

		for (ScreenLED led : obj) {
			if (led.x < mins.x) mins.x = led.x; if (led.x > maxes.x) maxes.x = led.x;
			if (led.y < mins.y) mins.y = led.y; if (led.y > maxes.y) maxes.y = led.y;
			if (led.z < mins.z) mins.z = led.z; if (led.z > maxes.z) maxes.z = led.z;    
		}

		c.x = (maxes.x - mins.x) / 2;
		c.y = (maxes.y - mins.y) / 2;
		c.z = (maxes.z - mins.z) / 2;

		return c;
	}   

	public void setRenderControl(RenderControl ctl, float value) {
		renderer.setControl(ctl,value);
	}

	/**
	 * Sets the method used to draw LED objects to the screen. Available methods are:
	 * <p>
	 * 
	 * <li><strong>RenderMethod.DEFAULT</strong>  - renders ScreenObj lists in 2D and ScreenShape lists in 3D.  Fast and simple.</li>
	 * <li><strong>RenderMethod.DRAW3D</strong>   - renders all objects in 3D space using Processing graphics API calls</li>
	 * <li><strong>RenderMethod.REALISTIC2D</strong> - uses Processing API calls to render realistic video-quality LED objects.
	 * Looks great, but performance will vary depending on your computer and GPU.</li> 
	 * <li><strong>RenderMethod.FILE</strong> - records incoming LED data to a JSON file for later playback. Useful for making
	 * movies and debugging.</li>
	 * <li><strong>RenderMethod.SHADER3D</strong> - NOT YET IMPLEMENTED - Does nothing at the moment. (Uses OpenGL and GLSL to
	 *  render highly detailed objects in 3D space. Performance may vary greatly depending on your GPU.) </li>
	 */
	public void setRenderMethod(RenderMethod m) {
		LEDRenderer r = new HDRenderFirstPass(this,m);
		if (renderer != null) {
			r.copyControlsFrom(renderer);
	
		}
		renderer = r;		
	}

	/**
	 * Draw an LED object using the selected renderer and the current viewing
	 * transform.<p>
	 */ 	
	public void draw(LinkedList <ScreenLED> obj) {
		renderer.render(obj);
		if (showAxes) renderer.drawAxes();
	}

	/**
	 * @deprecated 
	 * Use new draw() method instead.
	 * @see #draw
	 * @see #setRenderMethod
	 */
	@Deprecated
	public void render2D(LinkedList <ScreenLED> obj) {
		renderer.render(obj);
	}

	/**
	 * @deprecated
	 * Use new draw() method instead. 
	 * @see #draw
	 * @see #setRenderMethod
	 */
	@Deprecated
	public void render3D(LinkedList <ScreenLED> obj) {
		app.pushMatrix();
		mover.applyObjectTransform();

		for (ScreenLED led : obj) {
			led.draw3D();
		}   
		app.popMatrix();
	}	

	/**
	 * @deprecated
	 * Use new draw() method instead 
	 * @see #draw
	 * @see #setRenderMethod
	 */
	@Deprecated
	public void renderShape(LinkedList <ScreenShape> obj) {
		app.pushMatrix();
		mover.applyObjectTransform();

		for (ScreenShape led : obj) {
			led.draw3D();
		}   
		app.popMatrix();
	}
	
	public void pre() {
		bg.showImage(); 
		readData();
		if (uiActive && isController) applyViewingTransform();
	}

	public void post() {
		if (isRunning) requestData();	
	}

	/**
	 * The listener interface for receiving ptEvent events.
	 * The class that is interested in processing a ptEvent
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addptEventListener<code> method. When
	 * the ptEvent event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see ptEventEvent
	 */
	protected class ptEventListener {

		/**
		 * Keyboard handler
		 *
		 * @param e keyboard event
		 */
		public void keyEvent(final KeyEvent e) {
			if (e.getAction() == KeyEvent.RELEASE) {
				switch (e.getKey()) {        
				case ' ': // stop automatic rotation
					mover.autoMove = !mover.autoMove;
					break;
				case 'r': //reset rotation & translation for foreground and back
					mover.setRotation(0,0,0);
					mover.mouseRotation.set(0,0,0);
					mover.mouseTranslation.set(0,0,0);
					mover.initializeCamera();
					break;
				case 'R':
					bg.resetBackground();
					break;
				case TAB:
					// pause/unpause data, hold current frame if paused
					isRunning = !isRunning;
					
					// enable per-pixel tooltips only when paused.
					if (isRunning) {
						disablePixelInfo();						
					}
					else {
						enablePixelInfo();
					}
					break;
				case 'X':  // toggle display of axes
				case 'x':	
					showAxes = !showAxes;
					break;

				default:
					break;
				} 			
			}
		}

		/**
		 * Mouse event handler.
		 *
		 * @param e mouse event
		 */
		public void mouseEvent(final MouseEvent e) {
			float x = e.getX();
			float y = e.getY();
			float distX,distY,offsetX,offsetY;

			switch (e.getAction()) {
			case MouseEvent.WHEEL:
				if (e.isShiftDown())  {
					// if shift, zoom in/out on the background
					float s = bg.getScale();
					s = s - (0.05f * (float) (e.getCount()));
					PApplet.constrain(s,0.01f,1.5f);
					bg.setScale(s);
					bg.needScale = true;
					bg.needClip = true;
				}
				else  {
					mover.zoomCamera(30 * e.getCount());
				}

				break;
			case MouseEvent.PRESS:
				mover.dragOriginX = x;
				mover.dragOriginY = y;
				break;

			case MouseEvent.RELEASE:
				break;

			case MouseEvent.DRAG:
				distX = x - mover.dragOriginX;
				distY = y - mover.dragOriginY;
				mover.dragOriginX = x; 
				mover.dragOriginY = y;	

				if (e.isShiftDown())  {
					// if shift drag w/either key, move the background
					bg.moveRect(distX,distY);	
				}
				else if (e.getButton() == LEFT) {
					// small dead zone so you won't always accidentally rotate when
					// you click the screen.
					if ((PApplet.abs(distX) < MOUSE_MIN_MOVEMENT) && 
							(PApplet.abs(distY) < MOUSE_MIN_MOVEMENT)) {
						return;						
					}					

					// rotation based on distance from start of drag 
					distX = distX / app.width * TWO_PI;
					distY = distY / app.height * TWO_PI;

					// holding down alt rotates around the z axis only
					if (e.isAltDown()) {
						mover.mouseRotation.z += 
								(PApplet.abs(distX) > PApplet.abs(distY)) ?  distX : distY; 						 						
					}
					// otherwise rotate around x and y
					else {
						mover.mouseRotation.x += distX;
						mover.mouseRotation.y += distY;
					}
				}
				else if (e.getButton() == RIGHT) {
					// scale translation so we move a little faster as the camera moves away.
					float m = (mover.eye.z <= mover.DEFAULT_CAMERA_DISTANCE) ? 1 : mover.eye.z / mover.DEFAULT_CAMERA_DISTANCE;
					mover.mouseTranslation.x += m * distX; 
					mover.mouseTranslation.y += m * distY; 									
				}
				break;				
			case MouseEvent.CLICK:
				break;
			case MouseEvent.MOVE:
				if (pixelInfoEnabled()) {
					; // TODO - display per-pixel tooltip
				}
				break;
			}				
		}
	}
}


