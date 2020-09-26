package pixelTeleporter.library;

import processing.core.PConstants;
import processing.core.PVector;

/**
 * Handles object and camera translation/rotation.
 * <p>
 * Mostly for internal use by the PixelTeleporter class.
*/  
class Mover implements PConstants{
	PixelTeleporter parent;
	int[] pixelBuffer;       // buffer for incoming network data

	//camera position  
	PVector origin;
	PVector eye;

	//object position/rotation  
	PVector objectCenter;
	PVector currentRotation;
	PVector rotationRate;
	boolean autoMove; 

	//rotation controlled by mouse drag  
	PVector mouseRotation;

	int timer;
	float dragOriginX;
	float dragOriginY;
	float mouseZoom;

	public Mover(PixelTeleporter parent) {
		this.parent = parent;

		pixelBuffer = new int[parent.PIXEL_BUFFER_SIZE];    

		//camera    
		origin = new PVector(parent.app.width / 2, parent.app.height / 2, -200);
		eye = origin.copy();
		eye.z = 1000;

		//vectors for object movement/rotation    
		objectCenter = new PVector(0,0,0);
		currentRotation = new PVector(0,0,0);
		rotationRate = new PVector(0,0,0);
		mouseRotation = new PVector(0,0,0);
		mouseZoom = 0;
		autoMove = true;

		initializeCamera();

		timer = parent.app.millis();
	}  

	void addAndNormalize(PVector p1,PVector p2) {
		p1.x = (p1.x + p2.x) % TWO_PI;
		p1.y = (p1.y + p2.y) % TWO_PI;
		p1.z = (p1.z + p2.z) % TWO_PI;  
	}

	void setObjectCenter(float x, float y, float z) { objectCenter.set(x,y,z); }
	
	void setRotation(float x, float y, float z) { currentRotation.set(x,y,z); }
	
	void setRotationRate(float x, float y, float z) { rotationRate.set(x,y,z); } 

	public void initializeCamera() {   
		parent.app.camera(eye.x,eye.y,eye.z,       // looking from...
				origin.x,origin.y,objectCenter.z,  // looking at...
				0, 1, 0);                          // upX, upY, upZ    

		mouseZoom = 0;     
	}

	public void moveCamera() {     
		parent.app.camera(eye.x,eye.y,eye.z,      // looking from...
				origin.x,origin.y,objectCenter.z, // looking at...
				0, 1, 0);                         // upX, upY, upZ     
	}

	public void zoomCamera(float amount) {
		eye.add(0,0,amount);
	}    

	void applyMouseRotation() {
		parent.app.rotateX(-mouseRotation.y);
		parent.app.rotateY(mouseRotation.x);
	}

	void applyViewingTransform() { 
		moveCamera();    
		parent.app.translate(parent.app.width / 2,parent.app.height /2, 0);

		applyMouseRotation();

		if (autoMove) {
			PVector deltaRotation = PVector.mult(rotationRate,(float) parent.app.millis()-timer);
			addAndNormalize(currentRotation,deltaRotation);
		}

		timer = parent.app.millis();

		parent.app.rotateX(currentRotation.x);
		parent.app.rotateY(currentRotation.y);
		parent.app.rotateZ(currentRotation.z);
		parent.app.translate(-objectCenter.x,-objectCenter.y,-objectCenter.z);     
	} 
}