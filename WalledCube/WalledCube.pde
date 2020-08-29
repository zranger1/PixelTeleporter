// Pixel Teleporter walled cube demo
// Requires the walled cube mapping function from this repository's
// MappingFunctions directory.
//
// 8/26/2020 JEM (ZRanger1)

// global variables
PixelTeleporter pt;           // network data handler
LinkedList<ScreenLED> object; // list of LEDs in our object w/position and color info

PShader shade;               // shader for post processing effect


// build volumetric cube centered at origin
LinkedList<ScreenLED> buildWalledCube(int dimX,int dimY,int dimZ) {
  LinkedList<ScreenLED> cube = new LinkedList<ScreenLED>(); 
  ScreenLED led;
  int row,col,index;
  float x,y,z;
  float xOffs,yOffs,zOffs;
  float pixelSpacing = 2 * pixelSize;
  
  zOffs = -((float) dimX * pixelSpacing / 2.0);  
  yOffs = -((float) dimY * pixelSpacing / 2.0);  
  xOffs = -((float) dimZ * pixelSpacing / 2.0);        
  
  pt.setObjectCenter(0,0,0);    
  index = 0;
    
// top
  for (row = 0; row < dimX; row++) {
    for (col = 0; col < dimZ; col++) {
      x = xOffs + (row * pixelSpacing);
      y = yOffs + (dimY * pixelSpacing);
      z = zOffs + (col * pixelSpacing);
      led = new ScreenLED(x,y,z);
      led.setIndex(index++);
      cube.add(led);
    }
  }
  
// front
  for (row = 0; row < dimY; row++) {
    for (col = 0; col < dimX; col++) {
      x = xOffs + (col * pixelSpacing);
      y = yOffs + (row * pixelSpacing);
      z = zOffs + (dimZ * pixelSpacing);
      led = new ScreenLED(x,y,z);    
      led.setIndex(index++);
      cube.add(led);
      
    }
  }

//right side
  for (row = 0; row < dimZ; row++) {
    for (col = 0; col < dimY; col++) {
      x = xOffs + (dimX * pixelSpacing);
      y = yOffs + (col * pixelSpacing);
      z = zOffs + (row * pixelSpacing);
      led = new ScreenLED(x,y,z);      
      led.setIndex(index++);
      cube.add(led);      
    }
  }

// back
  for (row = 0; row < dimY; row++) {
    for (col = 0; col < dimX; col++) {
      x = xOffs + (col * pixelSpacing);
      y = yOffs + (row * pixelSpacing);
      z = zOffs + (-1 * pixelSpacing);
      led = new ScreenLED(x,y,z);      
      led.setIndex(index++);
      cube.add(led);
      
    }
  }

// left side
  for (row = 0; row < dimZ; row++) {
    for (col = 0; col < dimY; col++) {
      x = xOffs + (-1 * pixelSpacing);
      y = yOffs + (col * pixelSpacing);
      z = zOffs + (row * pixelSpacing);
      led = new ScreenLED(x,y,z);      
      led.setIndex(index++);
      cube.add(led);
      
    }
  }

// bottom
  for (row = 0; row < dimX; row++) {
    for (col = 0; col < dimZ; col++) {
      x = xOffs + (row * pixelSpacing);
      y = yOffs + (-1 * pixelSpacing);
      z = zOffs + (col * pixelSpacing);
      led = new ScreenLED(x,y,z);      
      led.setIndex(index++);
      cube.add(led);
    }
  }
  return cube;
}  

void setup() {
  size(1000,1000,P3D);     // Set up the stage 
  colorMode(RGB, 255);   
  noStroke();
  rectMode(CENTER);        // Rectangles are positioned based on their center.
  ellipseMode(CENTER);     // Ellipses are positioned based on their center.  
  sphereDetail(10);         // reduce down number of sphere vertices
  
  pt = new PixelTeleporter("192.168.1.42",8081);  
  pt.setElementSize(20);   // set display "led" size  
  shade = loadShader("blur.glsl");  
  shade.set("blurSize",5);
  shade.set("sigma",5.0f);

  object = buildWalledCube(10,10,10); 
  
// add slow rotation to enhance depth.  Spacebar toggles
// rotation on/off.
  pt.setRotation(0,0,0);
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
