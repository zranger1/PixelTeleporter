// Pixel Teleporter walled cube example
// On Pixelblaze, can make use of the walled cube mapping function
// in the PixelMap tab.  Use this mapper instead of the Pixelblaze
// default walled cube mapper.
//
// 8/26/2020 JEM (ZRanger1)
import pixelTeleporter.library.*;
import java.util.*;

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
  float pixelSpacing = 2 * pt.getElementSize();
  
  zOffs = -((float) dimX * pixelSpacing / 2.0);  
  yOffs = -((float) dimY * pixelSpacing / 2.0);  
  xOffs = -((float) dimZ * pixelSpacing / 2.0);        
  
  index = 0;
    
// top
  for (row = 0; row < dimX; row++) {
    for (col = 0; col < dimZ; col++) {
      x = xOffs + (row * pixelSpacing);
      y = yOffs + (dimY * pixelSpacing);
      z = zOffs + (col * pixelSpacing);
      led = pt.ScreenLEDFactory(x,y,z);
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
      led = pt.ScreenLEDFactory(x,y,z);    
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
      led = pt.ScreenLEDFactory(x,y,z);      
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
      led = pt.ScreenLEDFactory(x,y,z);      
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
      led = pt.ScreenLEDFactory(x,y,z);      
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
      led = pt.ScreenLEDFactory(x,y,z);      
      led.setIndex(index++);
      cube.add(led);
    }
  }
  return cube;
}  

void setup() {
  size(1000,1000,P3D);     // Set up the stage 
  
  pt = new PixelTeleporter(this,"192.168.1.42",8081);  
  pt.setElementSize(20);   // set display "led" size  
  
 // Optional - load and configure two pass blur shader    
  shade = loadShader("blur.glsl");  
  shade.set("blurSize",5);
  shade.set("sigma",5.0f);

// build 10x10x10 (600 pixel) walled cube
  object = buildWalledCube(10,10,10); 
  
// add slow rotation to enhance depth.  Spacebar toggles
// rotation on/off, mouse wheel zooms, 'r' resets to original orientation.
  pt.setRotation(0,0,0);
  pt.setRotationRate(0,PI / 5000, 0);
 
// start listening on network thread   
  pt.start();
}

void draw() {   
  background(30);
  
// draw our walled cube  
  pt.render3D(object);

// Optional - Apply blur shader
// If you need extra performance, comment out or remove
// calls to shade.set and filter().    
  shade.set("horizontalPass",0);
  filter(shade);
  shade.set("horizontalPass",1);  
  filter(shade); 
}
