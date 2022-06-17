// Pixel Teleporter volumetric cube example
// Works with either the walled cube mapping function in the examples/MappingFunctions 
// directory or the default Pixelblaze volumetric cube mapper.
//
// 6/14/2022 JEM (ZRanger1)
import pixelTeleporter.library.*;
import java.util.*;

// global variables
float pixelSpacing = 50;
PixelTeleporter pt;           
LinkedList<ScreenLED> object; // list of LEDs in our object w/position and color info

// build volumetric cube centered at origin
LinkedList<ScreenLED> buildVolumetricCube(int xDim,int yDim,int zDim) {
  LinkedList<ScreenLED> cube = new LinkedList<ScreenLED>(); 
  int x,y,z,index;
  float xOffs,yOffs,zOffs;
  
  index = 0;

  zOffs = -((float) zDim * pixelSpacing / 2.0);  
  for (z = 0; z < zDim; z++) {
    yOffs = -((float) yDim * pixelSpacing / 2.0);
    for (y = 0; y < yDim; y++) {
      xOffs = -((float) xDim * pixelSpacing / 2.0);      
      for (x = 0; x < xDim; x++) {
        ScreenLED led = pt.ScreenLEDFactory(x+xOffs,y+yOffs,z+zOffs);
        led.setIndex(index++);
        xOffs += pixelSpacing;        
        cube.add(led);
      }  
      yOffs += pixelSpacing;
    }
    zOffs += pixelSpacing;    
  } 
  return cube;
}

void setup() {
  size(1000,1000,P3D);     // Set up the stage 
  
  pt = new PixelTeleporter(this,"192.168.1.42");  
  pt.setModel(LEDType.BULB);
  pt.setWeight(80);
  pt.setFalloff(2.8);
  pt.setAmbientLight(10);   
  
// create 10x10x10 (1000 pixel) volumetric cube
  object = buildVolumetricCube(10,10,10); 
  pt.registerObject(object);
  

// start listening on network thread   
  pt.start();
}

void draw() {   
  background(0);

// draw volumetric cube
  pt.draw(object);
}
