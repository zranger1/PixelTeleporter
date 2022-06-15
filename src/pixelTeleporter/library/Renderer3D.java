package pixelTeleporter.library;

import java.util.LinkedList;
import processing.core.*;

/**
 * Draw an LED object using the 3D renderer and the current viewing
 * transform.  The 3D renderer uses a translucent sphere with diameter
 * dependent on brightness, to represent LEDs. 
 * @param obj list of ScreenLEDs representing an LED object or panel.
 */
class Renderer3D extends Renderer {
	
	Renderer3D(PixelTeleporter p) {
		super(p);
	}
		
	void initialize() { 
		super.initialize();
		pApp.strokeCap(PConstants.SQUARE);		
		pApp.hint(PConstants.ENABLE_STROKE_PERSPECTIVE);
		pApp.hint(PConstants.DISABLE_DEPTH_TEST);			
		pApp.hint(PConstants.ENABLE_DEPTH_SORT);				
		pApp.strokeWeight(weight);
		this.shader.set("weight",weight);	
		this.shader.set("ambient",(float)(ambient_light / 255.0));	
		this.shader.set("falloff",falloff);
     }
	
	void config_shader() {
		pApp.strokeCap(PConstants.SQUARE);		
		pApp.hint(PConstants.ENABLE_STROKE_PERSPECTIVE);
		pApp.hint(PConstants.DISABLE_DEPTH_TEST);			
		pApp.hint(PConstants.ENABLE_DEPTH_SORT);				
		pApp.strokeWeight(weight);
		this.shader.set("weight",weight);	
		this.shader.set("ambient",(float)(ambient_light / 255.0));	
		this.shader.set("falloff",falloff);	
		this.shader.set("time",(float) (pApp.millis()/1000.0));				
	}
	
	public void render(LinkedList <ScreenLED> obj) {
		config_shader();
		pApp.shader(this.shader,PConstants.POINTS);
		pApp.blendMode(PConstants.ADD);		

		int n = obj.size();
		for (int i = 0; i < n; i++) {
			obj.get(i).draw();
		}
	}
}