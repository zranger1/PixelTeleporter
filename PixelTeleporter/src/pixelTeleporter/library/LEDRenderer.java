package pixelTeleporter.library;
import java.net.URL;
import java.nio.file.Paths;
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

	
	LEDRenderer(PixelTeleporter pt) {
		this.pt = pt;
		pApp = pt.app;
		axisOrigin = new PVector();
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
	
	// NOTE - shader names must include extension.  It saves work for ME!
	public void loadShader(String fragment, String vertex) {
	  String path = getLibPath();
	  System.out.println(path);
	  shader = pApp.loadShader(Paths.get(path, "shaders", fragment).toString(),
                       		  Paths.get(path, "shaders", vertex).toString());
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
		  pApp.pushMatrix();
		  pApp.translate(axisOrigin.x,axisOrigin.y,axisOrigin.z);
		  
		  pApp.stroke(pApp.color(255,0,0));
		  pApp.line(0,0,0,d,0,0);
		  pApp.stroke(pApp.color(0,255,0));
		  pApp.line(0,0,0,0,d,0);
		  pApp.stroke(pApp.color(32,32,255));
		  pApp.line(0,0,0,0,0,d);
		  pApp.strokeWeight(sw);
		  
		  pApp.popMatrix();
		}
	
    private String getLibPath() {
        URL url = this.getClass().getResource(PixelTeleporter.class.getSimpleName() + ".class");
        if (url != null) {
            // Convert URL to string, taking care of spaces represented by the "%20"
            // string.
            String path = url.toString().replace("%20", " ");

            if (!path.contains(".jar"))
                return pApp.sketchPath();

            int n0 = path.indexOf('/');

            int n1 = -1;

            // read jar file name
            String fullJarPath = PixelTeleporter.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .getPath();

            if (PApplet.platform == PConstants.WINDOWS) {
                // remove leading slash in windows path
                fullJarPath = fullJarPath.substring(1);
            }

            String jar = Paths.get(fullJarPath).getFileName().toString();

            n1 = path.indexOf(jar);
            if (PApplet.platform == PConstants.WINDOWS) {
                // remove leading slash in windows path
                n0++;
            }

            if ((-1 < n0) && (-1 < n1)) {
                return path.substring(n0, n1);
            } else {
                return pApp.sketchPath();
            }
        }
        return pApp.sketchPath();
    }		
	

	
	// do nothing
	void render(LinkedList <ScreenLED> obj) {
	   ;	
	}
}