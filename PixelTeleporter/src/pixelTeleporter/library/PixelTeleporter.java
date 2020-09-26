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

// TODO: Auto-generated Javadoc
/**
    Manages communication with the PixelTeleporter server device
    and provides a framework for object rendering and basic user 
    camera control.
    <p>
	To use, create a PixelTeleporter object in your sketch and
	call its start() method to begin listening for pixel data
	on the network.
 */
public class PixelTeleporter implements PConstants {
	PApplet app;	
	PixelTeleporterThread thread;
	Mover mover;
	int ledSize = 15;        
	int pixelSize = 20;      
	boolean uiActive = false;
	boolean autoDataActive = false;
	private final ptEventListener ptEventListener = new ptEventListener();	

	//constants
	public final static String VERSION = "##library.prettyVersion##";	
	final int PIXEL_BUFFER_SIZE=8192;  //  room for 2048 pixels, plus a bit

	/**
	 * Creates and initializes a PixelTeleporter object  .
	 *
	 * @param pApp Pointer to currently running PApplet
	 * @param ipAddr IPv4 address of PixelTeleporter transmitter
	 * @param port Port number to listen on. 8081 is usual.
	 */
	public PixelTeleporter(PApplet pApp,String ipAddr,int port) {
		this.app = pApp;	
		mover = new Mover(this); 
		thread = new PixelTeleporterThread(this,ipAddr,port,PIXEL_BUFFER_SIZE);

		// register cleanup function
		app.registerMethod("dispose", this);

		// enable keyboard/mouse UI
		enableUI();

		// enable automatic per frame data request/read
		enableAutoData();				

		// configure default drawing settings
		app.colorMode(RGB, 255);     // RGB color, values from 0-255
		app.noStroke();              // no outlining
		app.rectMode(CENTER);        // Rectangles are positioned based on their center.
		app.ellipseMode(CENTER);     // Ellipses are positioned based on their center.  
		app.sphereDetail(8);         // reduce number of sphere vertices for performance		

		welcome();
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
	 *  Start data transport.
	 */
	public void start() {
		thread.start();
	}

	/**
	  Copies pixel colors from the network buffer of bytes into PixelTeleporter's
	  integer pixel buffer. 
	 * @return number of pixels copied
	 */
	public int readData() {
		return thread.readData(mover.pixelBuffer);
	}

	/**
 		Requests a frame of pixel data from the transport
 		<p>
 		Called by user from draw() when ready to render pixels.
	 */	
	public void requestData() {
		thread.requestData();
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
 		Called when the library shuts down.
 		<p>
 		Closes active devices and shuts down the transport listener
 		thread.
	 */		
	public void dispose() {
		disableUI();
		disableAutoData();
		thread.quit();
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
		LinkedList<ScreenLED> object = new LinkedList<ScreenLED>();
		JSONArray json = app.loadJSONArray(fileName);

		for (int i = 0; i < json.size(); i++) {
			float x,y,z;

			JSONArray mapEntry = json.getJSONArray(i);
			float [] coords = mapEntry.getFloatArray();  

			x = coords[0];
			y = coords[1];
			z = (coords.length == 3) ? z = coords[2] : 0;

			ScreenLED led = new ScreenLED(this,scale * x,scale * y,scale * z);
			led.setIndex(i);
			object.add(led);
		}

		return object;
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

	/**
	 * Draw an LED object using the 3D renderer and the current viewing
	 * transform.  The 3D renderer uses a translucent sphere with diameter
	 * dependent on brightness, to represent LEDs.
	 * @param obj list of ScreenLEDs representing an LED object or panel.
	 */
	public void render3D(LinkedList <ScreenLED> obj) {
		for (ScreenLED led : obj) {
			led.draw3D();
		}    			
	}

	/**
	 * Draw an LED object using the 2D renderer and the current viewing
	 * transform.  The 2D renderer draws LEDs as 2D circles.  It is 
	 * somewhat faster than the 3D render and is well suited to flat panels.
	 * @param obj list of ScreenLEDs representing an LED object or panel.
	 */	
	public void render2D(LinkedList <ScreenLED> obj) {
		for (ScreenLED led : obj) {
			led.draw2D();
		}    			
	}

	public void pre() {
		readData();
		applyViewingTransform();
	}

	public void post() {
		requestData();	
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
				case 'r': //reset rotation
					mover.setRotation(0,0,0);
					mover.mouseRotation.x = 0;
					mover.mouseRotation.y = 0;
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

			switch (e.getAction()) {
			case MouseEvent.WHEEL:
				mover.zoomCamera(30 * e.getCount());				  
				break;
			case MouseEvent.PRESS:
				mover.dragOriginX = x;
				mover.dragOriginY = y;

				break;
			case MouseEvent.DRAG:
				if (e.getButton() == LEFT) {
					//rotation based on distance from start of drag    
					mover.mouseRotation.x = (x - mover.dragOriginX)/(app.width / 2) * TWO_PI;
					mover.mouseRotation.y = (y - mover.dragOriginY)/(app.height / 2) * TWO_PI;
				}			    	
				break;				
			case MouseEvent.RELEASE:
				break;
			case MouseEvent.CLICK:
				break;
			case MouseEvent.MOVE:
				break;
			}				
		}
	}
}


