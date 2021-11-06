// Pixel Teleporter volumetric cube example
// Works with either the walled cube mapping function in the examples/MappingFunctions 
// directory or the default Pixelblaze volumetric cube mapper.
//
// 8/26/2020 JEM (ZRanger1)
import pixelTeleporter.library.*;
import java.util.*;

// global variables
PixelTeleporter pt;           
LinkedList<ScreenLED> object; // list of LEDs in our object w/position and color info

PShader shade;               // shader for post processing effect

// build volumetric cube centered at origin
LinkedList<ScreenLED> buildVolumetricCube(int xDim,int yDim,int zDim) {
  LinkedList<ScreenLED> cube = new LinkedList<ScreenLED>(); 
  int x,y,z,index;
  float xOffs,yOffs,zOffs;
  float pixelSpacing = 2 * pt.getElementSize();
  
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
  pt.setElementSize(18);   // set display "led" size  
  
// Optional - load and configure two pass blur shader      
  shade = loadShader("blur.glsl");  
  shade.set("blurSize",4);
  shade.set("sigma",5.0f);

// create 10x10x10 (1000 pixel) volumetric cube
  object = buildVolumetricCube(10,10,10); 
  
// add slow rotation to enhance depth.  Spacebar toggles
// rotation on/off, mouse wheel zooms, 'r' resets to original orientation.
  pt.setRotation(radians(-20),0,0);
  pt.setRotationRate(0,PI / 5000, 0);
 
// start listening on network thread   
  pt.start();
}

void draw() {   
  background(30);

// draw volumetric cube
  pt.draw(object);
   
// Optional - Apply blur shader
// If you need extra performance, comment out or remove
// calls to shade.set and filter().  
  shade.set("horizontalPass",0);
  filter(shade);
  shade.set("horizontalPass",1);  
  filter(shade); 
}
