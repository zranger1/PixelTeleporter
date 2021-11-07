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
		index = n;    
	}

	public int getIndex() {
		return index;
	}
	
	public void draw() {
		int pix;   
		
		parent.app.pushMatrix();

		parent.app.translate(x,y);
		pix = parent.pixelBuffer[index];
		parent.app.fill(pix);
		parent.app.circle(0,0,parent.ledSize);   
			
		parent.app.popMatrix();    
	} 

	public void draw3D() {
		int pix;
		float size,mag;
		
		parent.app.pushMatrix();    
		parent.app.translate(x,y,z);
		
		// get pixel and set slightly transparent alpha
		pix = parent.pixelBuffer[index];

		//draw roughly larger sphere for roughly brighter pixel    
		mag = parent.app.brightness(pix);
		mag /= 255.0;
		size = parent.ledSize + (parent.ledSize * mag);

		parent.app.fill(pix,176);
		parent.app.sphere(size);
		
		parent.app.popMatrix();
	}
		
	/**
	 * Calculate very fast approximate brightness.  Not really
	 * accurate, but very LED-ish, and Processing's native
	 * brightness() method seems to work this way too. 
	 * @return brightness (0-255) value of object's current color 
	 */
	public int getBrightness() {
	  return getBrightness(parent.pixelBuffer[index]);
	}	
	
	/**
	 * Calculate very fast approximate brightness of specified color.
	 * Not really accurate, but very LED-ish, and Processing's native
	 * brightness() method seems to work this way too. 
	 * @return brightness (0-255) value of object's current color 
	 */	
	public static int getBrightness(int col) {
		  int r = (col >> 16) & 0xFF;
		  int g = (col >> 8) & 0xFF;
		  int b = col & 0xFF;
	      if (b > g) g = b;
	      return (r > g) ? r : g;				
	}
	
	/**
	 * Returns the current color of the object in packed
	 * ARGB (int) format.
	 */
	public int getColor() {	   
		return  parent.pixelBuffer[index];
	}

	/**
	 * Stores the object's current (x,y,z) coordinates in the supplied PVector
	 */
	public void getPosition(PVector v) {
		v.set(x,y,z);
	}	
	
	/**
	 * Translates the coordinate frame so that
	 * the origin is at the object's current location.
	 * Caller is responsible for pushMatrix()/popMatrix().
	 */
	public void translateToOrigin() {
		parent.app.translate(x,y,z);		
	}
}


