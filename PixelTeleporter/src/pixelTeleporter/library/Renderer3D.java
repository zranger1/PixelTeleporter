package pixelTeleporter.library;

import java.util.LinkedList;
import processing.core.*;

/**
 * Draw an LED object using the 3D renderer and the current viewing
 * transform.  The 3D renderer uses a translucent sphere with diameter
 * dependent on brightness, to represent LEDs. 
 * @param obj list of ScreenLEDs representing an LED object or panel.
 */
class Renderer3D extends LEDRenderer {
	
	Renderer3D(PixelTeleporter p) {
		super(p);
	}
		
	void initialize() { 
		loadShader("pointfrag.glsl","pointvertex.glsl");
		pApp.strokeCap(PConstants.SQUARE);		
		pApp.hint(PConstants.ENABLE_STROKE_PERSPECTIVE);
		pApp.hint(PConstants.DISABLE_DEPTH_TEST);			
		pApp.hint(PConstants.ENABLE_DEPTH_SORT);				
		pApp.strokeWeight(100);
		this.shader.set("weight",(float) 100.0);	
		this.shader.set("ambient",(float)(ambient_light / 255.0));		
     }
	
	public void render(LinkedList <ScreenLED> obj) {
		pApp.blendMode(PConstants.ADD);

		pApp.pushMatrix();
		pt.mover.applyObjectTransform();
		this.shader.set("time",(float) (pApp.millis()/1000.0));
		pApp.shader(this.shader,PConstants.POINTS);		

		for (ScreenLED led : obj) {
			led.draw3D();
		}   
		pApp.resetShader();
		pApp.popMatrix();
	}
}