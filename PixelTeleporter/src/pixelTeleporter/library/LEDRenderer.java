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
	
	// reasonable defaults for all control values
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
	
	// set control values for the high def renderer
	public void setControl(RenderControl ctl, float value) {
		switch(ctl) {
		case RESET:
            resetControls();
			break;
		case WEIGHT:
			weight = PApplet.constrain(value,0,2000);
			break;
		case AMBIENT_LIGHT:
			ambient_light = PApplet.constrain(value,0,255);
			break;
		case FALLOFF:
			falloff = PApplet.constrain(value,0,10);
			break;
		case BGCOLOR:
			bgColor = (int) value;
			break;
		case BGALPHA:
			bgAlpha = (int) PApplet.constrain(value,0,255);
			break;
		case LEDMODEL_BULB:			
			model = LEDType.BULB;		
			break;
		case LEDMODEL_SMD:	
			model = LEDType.SMD;
			break;
		case INDIRECT_INTENSITY:
			indirectIntensity = PApplet.constrain(value,0,1);
			break;
		case OVEREXPOSURE:
			overexposure = PApplet.constrain(value,0,1000);
			break;
		case GAMMA:
			gamma = PApplet.constrain(value,0,2);
			break;
		}
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