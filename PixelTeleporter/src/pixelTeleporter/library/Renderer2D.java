package pixelTeleporter.library;

import java.util.LinkedList;
import processing.core.*;

/**
 * Draw an LED object using the default renderer and the current viewing
 * transform.<p>
 * How the object is drawn by the default renderer depends on the type of object.
 * A list of ScreenLED objects will be rendered as 2D circles. A list of 
 * ScreenShapes will be drawn as shapes in 3D space.
 *
 * @param obj Linked list of ScreenLEDs or ScreenLED derived objects representing an
 * arrangement of LEDs.
 */	
class Renderer2D extends LEDRenderer {
	
	Renderer2D(PixelTeleporter p) {
		super(p);
	}
		
	void initialize() { ; }
	
	public void render(LinkedList <ScreenLED> obj) {
		pApp.pushMatrix();
		pt.mover.applyObjectTransform();

		for (ScreenLED led : obj) {
			led.draw();
		}   
		pApp.popMatrix();
	}
}
