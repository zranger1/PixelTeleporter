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
	float overexposure;  // model CCD oversaturation
	float gamma;         // power factor for correcting LED colors
	int bgColor;         // RGB color of the "PCB" background behind the LEDs
	int bgAlpha;         // transparency of background
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
		axis.initialize();
	}
	
	/**
	 * set all renderer controls to their default values
	 */	
	void resetControls() {
		weight = 100;
		ambient_light = 8;
        indirectIntensity = (float) 0.75;		
		falloff = (float) 2;
		overexposure = 0;
		gamma = 1;
		bgColor = 8;
		bgAlpha = 255;
		model = LEDType.BULB;
		shader = null;
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
		overexposure = r.overexposure;
		gamma   = r.gamma;
		bgColor = r.bgColor;
		bgAlpha = r.bgAlpha;
        model   = r.model;	
        shader  = r.shader;
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
	 * RGB color of surface behind emitter
	 */	
	public void setBackgroundColor(int value) {
		bgColor = value;
	}	
	
	/**
	 * (0 - 255) opacity of surface behind emitter
	 */	
	public void setBackgroundAlpha(int value) {
		bgAlpha = PApplet.constrain(value,0,255);
	}	
	
	/**
	 * (0.0 - 1.0) light intensity from sides of emitter
	 */	
	public void setIndirectIntensity(float value) {
		indirectIntensity = PApplet.constrain(value,0,1);
	}	
	
	/**
	 * (0.0 - 1000) simulates CCD camera bloom
	 */	
    public void setOverexposure(float value) {
    	overexposure = PApplet.constrain(value,0,1000);
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
    public void setModel(String frag,String vert) {
       model = LEDType.CUSTOM;
    }
	
	void getWorldSize(PVector s) {
		s.set(worldXSize,worldYSize,worldZSize);
	}
	
	public void drawAxes() {
		axis.draw();
	}
	
	// do nothing
	void render(LinkedList <ScreenLED> obj) {
	   ;	
	}
}