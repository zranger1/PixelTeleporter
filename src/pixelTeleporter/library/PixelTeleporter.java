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
 JSON save/restore renderer to help make animated GIFs (include frame rate/frame count in saved data so we can recover timing on playback.  Or
 maybe timestamp of each frame so we can make the deltas the same.)
 Finish RenderType.USER renderer support
 Recieve from multiple transport types - PT Classic, PT Broadcast, Artnet, etc..
 */
public class PixelTeleporter implements PConstants {
	PApplet app;	
	PixelTeleporterThread thread;
	PTCamera ptCam;
	PTPower ptPower;
	PTBackground bg;
	PTFileUtils ptf;
	int ledSize = 15;        
	int pixelSize = 20;    
	public int[] pixelBuffer;
	boolean uiActive = false;
	boolean autoDataActive = false;
	boolean isController = false;
	boolean isRunning = true;
	boolean showPixelInfo = false;
	boolean showAxes = true;
	boolean powerAnalysis = true;
	TooltipHandler toolTip;

	Renderer renderer = null;

	// global pt object reference counter 
	static int refCount = 0; 

	//constants
	public final static String VERSION = "##library.prettyVersion##";	
	public final static float DEFAULT_CAMERA_DISTANCE = 600;	
	final int MAX_PIXELS = 4096;
	final int PIXEL_BUFFER_SIZE=(256+(MAX_PIXELS * 3)); 

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

		ptCam = new PTCamera(this,DEFAULT_CAMERA_DISTANCE);
		ptPower = new PTPower(this);
		thread = new PixelTeleporterThread(this,ipAddr,clientPort,serverPort,PIXEL_BUFFER_SIZE);
		pixelBuffer = thread.getPixelBuffer();
		bg = new PTBackground(app);
		ptf = new PTFileUtils(this);
		toolTip = new TooltipHandler();
		renderer = new Renderer3D(this);
		renderer.initialize();

		refCount++;

		// register cleanup function
		app.registerMethod("dispose", this);

		// enable automatic per frame data request/read
		enableAutoData();				

		// if we're the first PixelTeleporter object, enable keyboard/mouse UI,
		// configure default drawing settings and print welcome message
		if (refCount == 1) {
			isController = true;
			//enableUI();

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
		ptCam.setActive(uiActive);
	}

	/**
	 * Disables mouse/keyboard handlers.
	 *
	 */		
	public void disableUI() {
		if (!uiActive) return;
		uiActive = false;
		ptCam.setActive(uiActive);  
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
	
	// Power Analysis methods
	// TODO - add javadocs to these!
	public void enablePowerAnalysis() { powerAnalysis = true; }
	public void disablePowerAnalysis() { powerAnalysis = false; }
		
	public void setIdleConsumption(float n) { ptPower.setIdleConsumption(n); }
	public void setElementMaxPower(float n) { ptPower.setElementMaxPower(n); }
	public void setPowerEvaluator(PowerEvaluator p) { ptPower.setPowerEvaluator(p); }
		
	public float getCurrentPower() { return ptPower.getCurrent(); } 
	public float getMaxPower() { return ptPower.getMaximum(); }
	public float getAveragePower() { return ptPower.getAverage(); }	
	
	public void resetPowerStats() { ptPower.reset(); }	

	/**
	 *  Start data transport and initialize camera UI if enabled
	 */
	public void start() {
		//if (uiActive) ptCam.initializeCamera();
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
	 * Register an LED object for display. Must be called before drawing.
	 * @param obj
	 */
	public void registerObject(LinkedList <ScreenLED> obj) {
		renderer.registerObject(obj);
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
		return ptf.importPixelblazeMap(fileName,scale);
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
		return ptf.exportPixelblazeMap(obj,fileName,scale,is3D);
	}

	/**
	 * Sets size of billboard on which LED model is rendered.
	 */	
	public void setWeight(float value) {
		renderer.setWeight(value);
	}

	/**
	 * (0 - 255) the amount of ambient light the "camera" receives
	 */	
	public void setAmbientLight(float value) {
		renderer.setAmbientLight(value);
	}	

	/**
	 * (0 - 10) how far light from LEDs travels in the scene
	 */	
	public void setFalloff(float value) {
		renderer.setFalloff(value);
	}	

	/**
	 * (0.0 - 1.0) light intensity from sides of emitter
	 */	
	public void setIndirectIntensity(float value) {
		renderer.setIndirectIntensity(value);
	}	

	/**
	 * (0.0 - 2) adjust displayed gamma to better match LED colors
	 */    
	public void setGammaCorrection(float value) {
		renderer.setGammaCorrection(value);
	}

	/**
	 * Choose type of LED to render.  Available types are:
	 * 	<li><strong>LEDType.BULB</strong> - capsule shaped LED</li>
	 *  <li><strong>LEDType.SMD</strong> - square SMD LED</li>
	 *  <li><strong>LEDType.STONE</strong> - small chunk of transparent sea glass</li>
	 *  <li><strong>LEDType.STAR</strong> - a... star. Bright.  With rays.</li>
	 *  To specify a custom shader, use the SetModel(String fragment, String vertex) variant
	 *  of this method.
	 */
	public void setModel(LEDType value) {
		renderer.setModel(value);
	}

	/**
	 * Choose type of LED to render.  Available types are:
	 * 	<li><strong>LEDType.BULB</strong> - capsule shaped LED</li>
	 *  <li><strong>LEDType.SMD</strong> - square SMD LED</li>
	 *  <li><strong>LEDType.STONE</strong> - small chunk of transparent sea glass</li>
	 *  <li><strong>LEDType.STAR</strong> - a... star. Bright.  With rays.</li>
	 *  To specify a custom shader, use the SetModel(String fragment, String vertex) variant
	 *  of this method.
	 */   
	public void setModel(String fragment,String vertex) {
		renderer.setModel(fragment,vertex);
	}    

	/**
	 * Draw an LED object using the selected renderer and the current viewing
	 * transform.<p>
	 */ 	
	public void draw(LinkedList <ScreenLED> obj) {
		app.pushStyle();
		app.pushMatrix();	
		renderer.render(obj);
		if (showAxes) renderer.drawAxes();
		app.popMatrix();		
		app.resetShader();
		app.popStyle();
		
		if (powerAnalysis == true) ptPower.evaluate(obj);
	}

	public void pre() {
		bg.showImage(); 
		readData();
		//if (uiActive && isController) ptCam.applyViewingTransform();					
	}

	public void post() {
		if (isRunning) requestData();	
	}
}


