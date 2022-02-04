package pixelTeleporter.library;
import java.util.LinkedList;
import processing.core.*;

// generic rendering interface.  Takes a list of ScreenLED objects
// to be rendered.
public class LEDRenderer {
	PixelTeleporter pt;
	PApplet pApp;	
	
	// world bounding cube. Not used by all renderers, but will be filled
	// in on the first frame for those that need it.
	float worldXSize = 0;
	float worldYSize = 0;
	float worldZSize = 0;
	
	// Renderer controls.  The base class stores the superset of controls
	// used by the various renderers, so control settings can persist if
	// the user switches rendering methods midstream.
	float ambient_light;      // light level(base ambient + bloom level) for model camera 
	float falloff;       // how fast indirect light from the LED attenuates across the scene
	float indirectIntensity;  // initial brightness of indirect light (before falloff)
	float overexposure;  // model CCD oversaturation
	float gamma;         // power factor for correcting LED colors
	int bgColor;         // RGB color of the "PCB" background behind the LEDs
	int bgAlpha;         // transparency of background
	LEDType model;      // LED light map appearance model. 

	
	LEDRenderer(PixelTeleporter pt) {
		this.pt = pt;
		pApp = pt.app;
		resetControls();
	}
	
	void initialize() { ; }
	
	// reasonable defaults for all control values
	void resetControls() {
		ambient_light = 8;
        indirectIntensity = (float) 0.75;		
		falloff = (float) 2;
		overexposure = 0;
		gamma = 1;
		bgColor = 8;
		bgAlpha = 255;
		model = LEDType.BULB;
	}
	
	void copyControlsFrom(LEDRenderer r) {
		worldXSize = r.worldXSize;
		worldYSize = r.worldYSize;
		worldZSize = r.worldZSize;
		
		ambient_light = r.ambient_light; 
        indirectIntensity = r.indirectIntensity;		
		falloff  = r.falloff;
		overexposure = r.overexposure;
		gamma    = r.gamma;
		bgColor  = r.bgColor;
		bgAlpha  = r.bgAlpha;
        model    = r.model;	
	}
	
	// set control values for the high def renderer
	public void setControl(RenderControl ctl, float value) {
		switch(ctl) {
		case RESET:
            resetControls();
			break;
		case AMBIENT_LIGHT:
			ambient_light = pApp.constrain(value,0,255);
			break;
		case FALLOFF:
			falloff = pApp.constrain(value,0,10);
			break;
		case BGCOLOR:
			bgColor = (int) value;
			break;
		case BGALPHA:
			bgAlpha = (int) pApp.constrain(value,0,255);
			break;
		case LEDMODEL_BULB:			
			model = LEDType.BULB;		
			break;
		case LEDMODEL_SMD:	
			model = LEDType.SMD;
			break;
		case INDIRECT_INTENSITY:
			indirectIntensity = pApp.constrain(value,0,1);
			break;
		case OVEREXPOSURE:
			overexposure = pApp.constrain(value,0,1000);
			break;
		case GAMMA:
			gamma = pApp.constrain(value,0,2);
			break;
		}
	}
	
	void getWorldSize(PVector s) {
		s.set(worldXSize,worldYSize,worldZSize);
	}
	
	// do nothing
	void render(LinkedList <ScreenLED> obj) {
	   ;	
	}
}