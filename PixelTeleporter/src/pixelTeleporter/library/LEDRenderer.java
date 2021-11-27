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
	float exposure;      // light level for model camera 
	float falloff;       // how fast light from the LED attenuates across the scene
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
		exposure = 8;
		falloff = (float) 2;
		bgColor = 8;
		bgAlpha = 255;
		model = LEDType.BULB;
	}
	
	void copyControlsFrom(LEDRenderer r) {
		worldXSize = r.worldXSize;
		worldYSize = r.worldYSize;
		worldZSize = r.worldZSize;
		
		exposure = r.exposure; 
		falloff  = r.falloff;
		bgColor  = r.bgColor;
		bgAlpha  = r.bgAlpha;
        model =    r.model;		
	}
	
	// set control values for the high def renderer
	public void setControl(RenderControl ctl, float value) {
		switch(ctl) {
		case RESET:
            resetControls();
			break;
		case EXPOSURE:
			exposure = (int) value;
			break;
		case FALLOFF:
			falloff = value;
			break;
		case BGCOLOR:
			bgColor = (int) value;
			break;
		case BGALPHA:
			bgAlpha = (int) value;
			break;
		case LEDMODEL_BULB:			
			model = LEDType.BULB;		
			break;
		case LEDMODEL_SMD:	
			model = LEDType.SMD;
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