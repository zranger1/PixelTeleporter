/**
 * 
 */
package pixelTeleporter.library;

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
public class HDRenderFirstPass implements LEDRenderer {
	class _RControl {
		RenderControl cmd;
		float value;
		
		_RControl(RenderControl c, float v) {
			cmd = c;
			value = v;
		}
	}
	
	
	PixelTeleporter pt;
	int realRenderer;
	float xSize,ySize,zSize;
	LinkedList<_RControl> commands;
	
	HDRenderFirstPass(PixelTeleporter p,int rr) {
		pt = p;
		realRenderer = rr;
		xSize = ySize = zSize = 0;
		commands = new LinkedList<_RControl>();		
	}
	
	void switchToRealRenderer() {
		// TODO - create new renderer of appropriate type and
		// set it as the active renderer in the PT object.
		switch(realRenderer) {
		case 2: // 2D HD renderer
			// create a new HD 2D renderer
			System.out.println("World Size: "+xSize+" x "+ySize);
			pt.renderer = new RendererR2D(pt,xSize,ySize);
			break;
		case 3: // 3D HD renderer
			System.out.println("3D HDR renderer is not yet implemented.");
			System.out.println("default 3D renderer (DRAW3D) selected.");			
			pt.setRenderMethod(RenderMethod.DRAW3D);				
			break;			
		default:
			System.out.println("Invalid renderer requested from HDRenderFirstPass");
			System.out.println("Only 2 (2D HDR) and 3 (3D HDR) are supported.");
			System.out.println("default renderer selected.");						
			pt.setRenderMethod(RenderMethod.DEFAULT);
			break;
		}		
		
		// set any renderer parameters the user gave us at startup time.
		for (_RControl r : commands) {
			pt.renderer.setControl(r.cmd,r.value);
		}		
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
		  xSize = pt.app.abs(xmax-xmin);
		  ySize = pt.app.abs(ymax-ymin);
		  zSize = pt.app.abs(zmax-zmin);
		}
		else {
			System.out.println("HDRenderFirstPass invoked on empty display list.");
			System.out.println("This is... not good... you'll need to fix it.");
			realRenderer = -1;
		}
		
		// first pass done.  Switch renderers next time this is 
        switchToRealRenderer();
	}
	
	// keep track of control commands the user sends at setup time so we can
	// pass them on to the actual renderer when we start it.
	public void setControl(RenderControl ctl, float value) {		
		_RControl c = new _RControl(ctl,value);
		commands.add(c);		
	}
	

}
