package pixelTeleporter.library;

import java.util.LinkedList;

import processing.core.*;
import processing.opengl.PGraphics3D;

/**
 * Handles object and camera translation/rotation.
 * <p>
 * Mostly for internal use by the PixelTeleporter class.
*/  
class Mover implements PConstants{
	PixelTeleporter parent;

	//camera position  
	float DEFAULT_CAMERA_DISTANCE = 1000;
	PVector origin;
	PVector eye;

	//object position/rotation  
	PVector objectCenter;
	PVector currentRotation;
	PVector rotationRate;
	boolean autoMove; 

	//rotation & translation controlled by mouse drag  
	PVector mouseRotation;
	PVector mouseTranslation;

	int timer;
	float dragOriginX;
	float dragOriginY;
	float mouseZoom;

	public Mover(PixelTeleporter parent) {
		this.parent = parent;

		//camera    
		origin = new PVector(parent.app.width / 2, parent.app.height / 2, DEFAULT_CAMERA_DISTANCE);
		eye = origin.copy();

		//vectors for object movement/rotation    
		objectCenter = new PVector(0,0,0);
		currentRotation = new PVector(0,0,0);
		rotationRate = new PVector(0,0,0);
		mouseRotation = new PVector(0,0,0);
		mouseTranslation = new PVector(0,0,0);
		autoMove = true;

		timer = parent.app.millis();
	}  

	void addAndNormalizeAngle(PVector p1,PVector p2) {
		p1.x = (p1.x + p2.x) % TWO_PI;
		p1.y = (p1.y + p2.y) % TWO_PI;
		p1.z = (p1.z + p2.z) % TWO_PI;  
	}
	
	/**
	 * Find geometric center of object represented by ScreenLED list.
	 *
	 * @param obj Linked list of ScreenLEDs representing a displayable object
	 * @return PVector with x,y,z set to object center
	 */
	public void findObjectCenter(LinkedList<ScreenLED> obj) {
		PVector c = new PVector(0,0,0);
		PVector mins = new PVector(1E7f,1E7f,1E7f);
		PVector maxes = new PVector(-1E7f,-1E7f,-1E7f);

		for (ScreenLED led : obj) {
			if (led.x < mins.x) mins.x = led.x; if (led.x > maxes.x) maxes.x = led.x;
			if (led.y < mins.y) mins.y = led.y; if (led.y > maxes.y) maxes.y = led.y;
			if (led.z < mins.z) mins.z = led.z; if (led.z > maxes.z) maxes.z = led.z;    
		}

		objectCenter.x = mins.x + (maxes.x - mins.x) / 2;
		objectCenter.y = mins.y + (maxes.y - mins.y) / 2;
		objectCenter.z = mins.z + (maxes.z - mins.z) / 2;
	}   	

	void setObjectCenter(float x, float y, float z) { objectCenter.set(x,y,z); }
	
	void setRotation(float x, float y, float z) { currentRotation.set(x,y,z); }
	
	void setRotationRate(float x, float y, float z) { rotationRate.set(x,y,z); } 

	public void initializeCamera() { 
		//parent.app.camera(eye.x,eye.y,eye.z,      // looking from...
		//		objectCenter.x,objectCenter.y,objectCenter.z, // looking at...
		//		0, 1, 0);                         // upX, upY, upZ    
	
      moveCamera();   
    
	}

	public void moveCamera() {     
	    parent.app.beginCamera();	
		parent.app.camera(eye.x,eye.y,eye.z,      // looking from...
				objectCenter.x,objectCenter.y,objectCenter.z, // looking at...
				0, 1, 0);                         // upX, upY, upZ
		parent.app.rotateX(PConstants.PI / 2);
		parent.app.endCamera();  		

	}

	public void zoomCamera(float amount) {
		eye.add(0,0,amount);
	}    

	void applyMouseRotation() {
		parent.app.translate(mouseTranslation.x,mouseTranslation.y,0);
		
		parent.app.rotateX(-mouseRotation.y+currentRotation.x);
		parent.app.rotateY(mouseRotation.x+currentRotation.y);
		parent.app.rotateZ(mouseRotation.z+currentRotation.z);
	}
	
	void applyObjectTransform() {
		
	   parent.app.translate(-objectCenter.x,-objectCenter.y,-objectCenter.z); 
	   
		if (autoMove) {
			PVector deltaRotation = PVector.mult(rotationRate,(float) parent.app.millis()-timer);
			addAndNormalizeAngle(currentRotation,deltaRotation);
		}
		
		timer = parent.app.millis();		
	}

	void applyViewingTransform() { 
		moveCamera();    
		//parent.app.translate(parent.app.width / 2,parent.app.height /2, 0);

		applyMouseRotation();
	} 
}