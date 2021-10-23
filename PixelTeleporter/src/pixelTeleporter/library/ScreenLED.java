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
	
	
	public int getBrightness() {
		return (int) parent.app.brightness(parent.pixelBuffer[index]);
	}
	
	/**
	 * Translates ScreenLED to center and returns an int containing
	 * the pixel's current RGB color.  Calling program is responsible
	 * for pushMatrix/popMatrix;
	 * @return
	 */
	public int renderAssist() {	   
		parent.app.translate(x,y,z);
		return  parent.pixelBuffer[index];
	}
	
}


