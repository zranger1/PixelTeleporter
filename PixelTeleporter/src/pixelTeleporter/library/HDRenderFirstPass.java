/**
 * 
 */
package pixelTeleporter.library;
import processing.core.*;
import java.util.LinkedList;

/**
 * FOR INTERNAL USE:
 * 
 * Renderer for the first frame drawn with either the 2D or 3D
 * high def renderers.  Measures the world dimensions of the 
 * LEDs drawn so the HD renderers can scale properly, then
 * hands over control to the "real" renderer specified by 
 * the user.
 *
 */
public class HDRenderFirstPass extends LEDRenderer {
	RenderMethod realRenderer;
	
	HDRenderFirstPass(PixelTeleporter p,RenderMethod rr) {
		super (p);
		realRenderer = rr;
		worldXSize = worldYSize = worldZSize = 0;
	}
	
	void initialize() { ; }
	
	void switchToRealRenderer() {
		// TODO - create new renderer of appropriate type,
		// copy inheritable control settings and
		// set the new renderer as active for the next frame.
		switch(realRenderer) {
		case DEFAULT: // select normal 2D or 3D based on world depth
			if (worldZSize == 0) {
				pt.renderer = new Renderer2D(pt);				
			}
			else {
				pt.renderer = new Renderer3D(pt);				
			}
			break;
		case DRAW2D:
			pt.renderer = new Renderer2D(pt);
			break;
		case DRAW3D:
			pt.renderer = new Renderer3D(pt);
			break;
		case HD2D: // 2D HD renderer
            pt.renderer = new RendererR2D(pt);
			break;
		case HD3D: // 3D HD renderer
			System.out.println("3D HD renderer is not yet implemented.");
			System.out.println("default 3D renderer (DRAW3D) selected.");
			pt.renderer = new Renderer3D(pt);			
			break;			
		default:
			System.out.println("Unsupported renderer requested from HDRenderFirstPass");
			System.out.println("DRAW3D renderer selected.");						
			pt.renderer = new Renderer3D(pt);
			break;
		}	
		pt.renderer.copyControlsFrom(this);
		pt.renderer.initialize();
	}
	
	public void render(LinkedList <ScreenLED> obj) {
		float xmin,ymin,zmin;
		float xmax,ymax,zmax;
				
		// hopefully, nobody's using coordinates in the 9 million+ range
		xmin = ymin = zmin = 9999999;
		xmax = ymax = zmax = -9999999;
				
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
		  
		  axisOrigin.x = xmin;
		  axisOrigin.y = ymin;
		  axisOrigin.z = zmin;
		}
		else {
			System.out.println("HDRenderFirstPass invoked on empty display list.");
			System.out.println("This is... not good... you'll need to fix it.");
			realRenderer = RenderMethod.DRAW2D;
		}
		
		// first pass done.  Switch renderers next time this is 
        switchToRealRenderer();
	}	

}
