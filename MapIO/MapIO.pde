// Pixel Teleporter volumetric cube demo
// Works with either the walled cube mapping function from this repository's
// MappingFunctions directory or the default Pixelblaze volumetric cube mapper.
//
// 8/26/2020 JEM (ZRanger1)

// global variables
PixelTeleporter pt;           // network data handler
LinkedList<ScreenLED> object; // list of LEDs in our object w/position and color info

PShader shade;               // shader for post processing effect

void setup() {
  size(1000,1000,P3D);     // Set up the stage 
  colorMode(RGB, 255);   
  noStroke();
  rectMode(CENTER);        // Rectangles are positioned based on their center.
  ellipseMode(CENTER);     // Ellipses are positioned based on their center.  
  sphereDetail(8);         // reduce down number of sphere vertices
  
  pt = new PixelTeleporter("192.168.1.42",8081);  
  pt.setElementSize(18);   // set display "led" size  
  shade = loadShader("blur.glsl");  
  shade.set("blurSize",4);
  shade.set("sigma",5.0f);

// read map JSON from file in data directory
  object = 
  importPixelblazeMap("data/cubemap.json",pixelSize);

// find the object's rotational center and set it in the viewer
  PVector center = findObjectCenter(object);
  pt.setObjectCenter(center.x,center.y,center.z);  
  
// To export an object to a Pixelblaze compatible map file, call exportPixelblazeMap()
// with the object list, a file name, a scaling factor, and a flag indicating whether
// or not the object is 3D (true if 3D, false if 2D)
// for example:
//  exportPixelblazeMap(object,"data/output.json",1.0 / pixelSize,true); 
  
// add slow rotation to enhance depth.  Spacebar toggles
// rotation on/off, 'r' resets rotation.
  pt.setRotation(radians(-20),0,0);
  pt.setRotationRate(0,PI / 5000, 0);
 
// start listening on network thread   
  pt.start();
}
void draw() {   
  pt.readData();
  background(30);
  
  pt.mover.applyViewingTransform();
  
  for (ScreenLED led : object) {
    led.draw3D();
  }    
  
  shade.set("horizontalPass",0);
  filter(shade);
  shade.set("horizontalPass",1);  
  filter(shade); 
  
  pt.requestData();    
}
