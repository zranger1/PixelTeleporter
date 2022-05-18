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
	int xColor,yColor,zColor; // axis legend colors
	
	
	LEDRenderer(PixelTeleporter pt) {
		this.pt = pt;
		pApp = pt.app;
		axisOrigin = new PVector();
		xColor = pApp.color(255,0,0);
		yColor = pApp.color(0,255,0);
		zColor = pApp.color(0,0,255);
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
		shader = null;
	}
	
	void copyControlsFrom(LEDRenderer r) {
		worldXSize = r.worldXSize;
		worldYSize = r.worldYSize;
		worldZSize = r.worldZSize;
		
		axisOrigin = r.axisOrigin;
		
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
	
	// TODO - add billboard text textures as axis labels, and make this thing
	// a PShape.
	public void drawAxes() {
		  float sw = pApp.getGraphics().strokeWeight;

		  // origin marker
		  float d = 20;
		  pApp.stroke(128);
		  pApp.strokeWeight(2);
		  pApp.line(-d,0,0,d,0,0);
		  pApp.line(0,-d,0,0,d,0);
		  pApp.line(0,0,-d,0,0,d);	  
		  
		  // colored axes
		  d = 200;

		  pApp.translate(axisOrigin.x,axisOrigin.y,axisOrigin.z);
		  
		  pApp.stroke(xColor);		  
		  pApp.line(0,0,0,d,0,0);
		  
		  pApp.stroke(yColor);	  
		  pApp.line(0,0,0,0,d,0);
		  
		  pApp.stroke(zColor);		  
		  pApp.line(0,0,0,0,0,d);
		  
		  
		  // calculate placement of axis label billboards
		  float x1 = pApp.screenX(d,0,0);
		  float y1 = pApp.screenY(d,0,0);
		  float z1 = pApp.screenZ(d,0,0);
		  
		  float x2 = pApp.screenX(0,d,0);
		  float y2 = pApp.screenY(0,d,0);
		  float z2 = pApp.screenZ(0,d,0);
		  
		  float x3 = pApp.screenX(0,0,d);
		  float y3 = pApp.screenY(0,0,d);
		  float z3 = pApp.screenZ(0,0,d);		  		  
		  
		  // label axes
		  pApp.resetMatrix();
		  pApp.textMode(PConstants.MODEL);
		  
		  pApp.textSize(44);
		  pApp.textAlign(PConstants.CENTER,PConstants.CENTER);
		  pApp.fill(xColor);
		  pApp.text("X",x1,y1);
		  pApp.fill(yColor);
		  pApp.text("Y",x2,y2);
		  pApp.fill(zColor);
		  pApp.text("Z",x3,y3);	
		  
		  pApp.strokeWeight(sw);
		}
	
	

	// do nothing
	void render(LinkedList <ScreenLED> obj) {
	   ;	
	}
}