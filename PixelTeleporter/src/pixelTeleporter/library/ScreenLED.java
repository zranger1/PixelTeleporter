package pixelTeleporter.library;

import processing.core.*;

/**
 Represents individual "LEDs". 
 <p>
 x,y,z are positional coordinates in world space, 
 and index is the LED's index in the incoming RGB pixel data stream. 
 <p>
 To create a ScreenLED for your sketch, use the PixelTeleporter class 
 method ScreenLEDFactory().
*/
public class ScreenLED {
	PixelTeleporter parent;
	public float x;
	public float y;
	public float z;
	int index;

	public ScreenLED(PixelTeleporter parent) {
		this.parent = parent;
		index = 0;
	}

	public ScreenLED(PixelTeleporter parent,float x,float y) {
		this.parent = parent;
		this.x = x;
		this.y = y;
		this.z = 0;
	}

	public ScreenLED(PixelTeleporter parent,float x, float y, float z) {
		this.parent = parent;
		this.x = x;
		this.y = y;
		this.z = z;
		index = 0;
	}

	public void setIndex(int n) {
		if (n >= 2048) {
			System.out.println("EXTREMELY DIRE WARNING: Pixel index out of bounds in ScreenLED:setIndex. (max is 2047)");
			System.out.println("I'd throw an exception, but frankly it's a pain in the butt for everyone, and I trust you to read!"); 
			return;
		} 
		index = 3 * n;    
	}

	public int getIndex() {
		return index / 3;
	}
	
	public void draw2D() {
		int r,g,b,i;   
		
		parent.app.pushMatrix();

		parent.app.translate(x,y);
		i = index;
		r = parent.mover.pixelBuffer[i++];
		g = parent.mover.pixelBuffer[i++];
		b = parent.mover.pixelBuffer[i];
		parent.app.fill(r,g,b);
		parent.app.circle(0,0,parent.ledSize);   
			
		parent.app.popMatrix();    
	} 

	public void draw3D() {
		int r,g,b,i;
		float size,mag;
		
		parent.app.pushMatrix();    
		parent.app.translate(x,y,z);

		i = index;
		r = parent.mover.pixelBuffer[i++];
		g = parent.mover.pixelBuffer[i++];
		b = parent.mover.pixelBuffer[i];

		//draw roughly larger sphere for roughly brighter pixel    
		mag = (r > g) ? ((r > b) ? r : b) : ((g > b) ? g : b);
		mag /= 255.0;
		size = parent.ledSize + (parent.ledSize * mag);

		parent.app.fill(r,g,b,176);
		parent.app.sphere(size);
		
		parent.app.popMatrix();
	}
}

