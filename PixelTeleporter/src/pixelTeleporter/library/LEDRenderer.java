package pixelTeleporter.library;
import java.util.LinkedList;
import processing.core.*;
import processing.opengl.PShader;

// generic rendering interface.  Takes a list of ScreenLED objects
// to be rendered.
public class LEDRenderer {
	PixelTeleporter pt;
	PApplet pApp;
	PShader shader;
	PVector axisOrigin;
	AxisLegend axis;

	// world bounding cube. Not used by all renderers, but will be filled
	// in on the first frame for those that need it.
	float worldXSize = 0;
	float worldYSize = 0;
	float worldZSize = 0;

	// Renderer controls.  The base class stores the superset of controls
	// used by the various renderers, so control settings can persist if
	// the user switches rendering methods midstream.
	float weight;        // dimension of billboard used by shader
	float ambient_light;      // light level(base ambient + bloom level) for model camera 
	float falloff;       // how fast indirect light from the LED attenuates across the scene
	float indirectIntensity;  // initial brightness of indirect light (before falloff)
	float gamma;         // power factor for correcting LED colors
	LEDType model;      // LED light map appearance model. 
	int xColor,yColor,zColor; // axis legend colors


	LEDRenderer(PixelTeleporter pt) {
		this.pt = pt;
		pApp = pt.app;
		axisOrigin = new PVector();
		axis = new AxisLegend(this);
		xColor = pApp.color(255,0,0);
		yColor = pApp.color(0,255,0);
		zColor = pApp.color(0,0,255);
		resetControls();
	}

	void initialize() { 
		;
	}

	/**
	 * set all renderer controls to their default values
	 */	
	void resetControls() {
		weight = 100;
		ambient_light = 8;
		indirectIntensity = (float) 0.75;		
		falloff = (float) 2;
		gamma = 1;
		setModel(LEDType.BULB);
	}

	void copyControlsFrom(LEDRenderer r) {
		worldXSize = r.worldXSize;
		worldYSize = r.worldYSize;
		worldZSize = r.worldZSize;

		axisOrigin = r.axisOrigin;

		weight = r.weight;
		ambient_light = r.ambient_light; 
		indirectIntensity = r.indirectIntensity;		
		falloff  = r.falloff;
		gamma   = r.gamma;
		model   = r.model;	
		shader  = r.shader;
	}
	
	void registerObject(LinkedList <ScreenLED> obj) {
		pt.mover.findObjectCenter(obj);
		calculateWorldSize(obj);
		setAxisOrigin();
	}

	// Rendering control accessors
	//

	/**
	 * Sets size of billboard on which LED model is rendered.
	 */	
	public void setWeight(float value) {
		weight = PApplet.constrain(value,0,2000);
	}

	/**
	 * (0 - 255) the amount of ambient light the "camera" receives
	 */	
	public void setAmbientLight(float value) {
		ambient_light = PApplet.constrain(value,0,255);
	}	

	/**
	 * (0 - 10) how far light from LEDs travels in the scene
	 */	
	public void setFalloff(float value) {
		falloff = PApplet.constrain(value,0,10);
	}	

	/**
	 * (0.0 - 1.0) light intensity from sides of emitter
	 */	
	public void setIndirectIntensity(float value) {
		indirectIntensity = PApplet.constrain(value,0,1);
	}	

	/**
	 * (0.0 - 2) adjust displayed gamma to better match LED colors
	 */    
	public void setGammaCorrection(float value) {
		gamma = PApplet.constrain(value,0,2);
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
		model = value;
		String frag = new String();

		switch(model) {
		case BULB:
			frag = new String("bulb.glsl");
			break;
		case SMD:
			frag = new String("smd.glsl");    		
			break;
		case STONE:
			frag = new String("stone.glsl");    		
			break;
		case STAR:
			frag = new String("star.glsl");    		
			break;

		}

		this.shader = pt.ptf.loadShader(frag,"pointvertex.glsl");    	
	}

	/**
	 * Choose type of LED to render.  Available types are:
	 * 	<li><strong>LEDType.BULB</strong> - capsule shaped LED</li>
	 *  <li><strong>LEDType.SMD</strong>  - square SMD LED</li>
	 *  <li><strong>LEDType.STONE</strong> - small chunk of transparent sea glass</li>
	 *  <li><strong>LEDType.STAR</strong> - a... star. Bright.  With rays.</li>
	 *  To specify a custom shader, use the SetModel(String fragment, String vertex) variant
	 *  of this method.
	 */   
	public void setModel(String fragment,String vertex) {
		model = LEDType.CUSTOM;
		// TODO - load model given path, starting with sketch /data dir
		// if vertex renderer string is empty, use default.
	}

	/**
	 * Choose type of LED to render.  Available types are:
	 * 	<li><strong>LEDType.BULB</strong> - capsule shaped LED</li>
	 *  <li><strong>LEDType.SMD</strong> - square SMD LED</li>
	 *  <li><strong>LEDType.STONE</strong> - small chunk of transparent sea glass</li>
	 *  <li><strong>LEDType.STAR</strong> - a... star. Bright.  With rays.</li>
	 *  To specify a custom shader, use the SetModel(String fragment) or 
	 *  SetModel(String fragment, String vertex) variants of this method.
	 */   
	public void setModel(String fragment) {
		setModel(fragment,"pointvertex.glsl");
	}    
	
	/**
	 * Calculates the extent of world coordinates occupied by the specified
	 * object.
	 * @param obj
	 */
	public void calculateWorldSize(LinkedList <ScreenLED> obj) {
		float xmin,ymin,zmin;
		float xmax,ymax,zmax;
				
		// hopefully, nobody's using coordinates in the 99 million+ range
		xmin = ymin = zmin = 1E8f;
		xmax = ymax = zmax = -1E8f;
				
		// find the range of each coordinate
		for (ScreenLED led : obj) {
			if (led.x < xmin) xmin = led.x;
			if (led.y < ymin) ymin = led.y;
			if (led.z < zmin) zmin = led.z;	
			
			if (led.x > xmax) xmax = led.x;
			if (led.y > ymax) ymax = led.y;
			if (led.z > zmax) zmax = led.z;
		}    
		
		// If there was anything in the display list, calculate the range
		// for each coordinate, and thus the world coord size of the displayed
		// object.
		if (obj.size() > 0) {
		  worldXSize = PApplet.abs(xmax-xmin);
		  worldYSize = PApplet.abs(ymax-ymin);
		  worldZSize = PApplet.abs(zmax-zmin);		  
		}				
	}
	
	void getWorldSize(PVector s) {
		s.set(worldXSize,worldYSize,worldZSize);			
	}
	
	/**
	 * CAUTION: World size and center position must be accurate before calling this
	 */
	public void setAxisOrigin() {		  
		  axisOrigin.x = pt.mover.objectCenter.x + worldXSize / 2;
		  axisOrigin.y = pt.mover.objectCenter.y + worldYSize / 2;
		  axisOrigin.z = pt.mover.objectCenter.z + worldZSize / 2;	
		  axis.initialize();
	}

	public void drawAxes() {
		axis.draw();
	}

	// do nothing
	void render(LinkedList <ScreenLED> obj) {
		;	
	}
}