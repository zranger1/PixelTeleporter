package pixelTeleporter.library;

import processing.core.PShape;

/**
Allows the use of an arbitrary 2D or 3D shape to represent an LED 
<p>
x,y,z are positional coordinates in world space, 
and index is the LED's index in the incoming RGB pixel data stream. 
<p>
To create a ScreenShape for your sketch, use the PixelTeleporter class 
method ScreenShapeFactory().
*/
public class ScreenShape extends ScreenLED {
    PShape ledShape;
    int opacity;
    
    public ScreenShape(PixelTeleporter parent,PShape s,int opacity) {
      super(parent);
      ledShape = s;
      this.opacity = opacity;
    }

	public ScreenShape(PixelTeleporter parent,PShape s) {
	  this(parent,s,255);
	}
	
	/**
	Set opacity of current shape.  It will be updated the next
	time the shape is drawn.
	* @param o opacity for this object (0-255, default == 255)
	*/	
	public void setOpacity(int o) {
		this.opacity = o;
	}
	
	public void draw2D() {
		int r,g,b,i;   
		
		parent.app.pushMatrix();
		parent.app.translate(x,y);
		
		i = index;
		r = parent.mover.pixelBuffer[i++];
		g = parent.mover.pixelBuffer[i++];
		b = parent.mover.pixelBuffer[i];
		
		ledShape.setFill(parent.app.color(r,g,b,opacity));
		parent.app.shape(ledShape);
			
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
		
		ledShape.setFill(parent.app.color(r,g,b,opacity));
		parent.app.shape(ledShape);
		
		parent.app.popMatrix();
	}	
}
