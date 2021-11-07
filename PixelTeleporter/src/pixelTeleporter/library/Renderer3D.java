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
		
	void initialize() { ; }
	
	public void render(LinkedList <ScreenLED> obj) {
		pApp.pushMatrix();
		pt.mover.applyObjectTransform();

		for (ScreenLED led : obj) {
			led.draw3D();
		}   
		pApp.popMatrix();
	}
}