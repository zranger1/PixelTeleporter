// Pixel Teleporter cylinder example
// Uses the standard Pixelblaze 2D matrix mapper if you want to use it as a
// curved 2D display.  If you just want to use it with 1D patterns set
// the <spiral> parameter to true when calling buildCylinder().
//
// 8/26/2020 JEM (ZRanger1)
import pixelTeleporter.library.*;
import java.util.*;

// global variables
PixelTeleporter pt;           // network data handler
LinkedList<ScreenLED> object; // list of LEDs in our object w/position and color info

PShader shade;               // shader for post processing effect

// create one ring (not THE One Ring, mind you... 
// (Build a ring on the xz plane, centered at the origin)
LinkedList<ScreenLED> buildRing(int nPixels,float yOffset,float radius,
                                float startAngle, float stopAngle,int startIndex) {
  LinkedList<ScreenLED> ring;
  float angle, increment;
    
  ring = new LinkedList<ScreenLED>();
         
  angle = startAngle;  
  increment = stopAngle / (float) nPixels;
    
  for (int i = 0; i < nPixels; i++) {
      ScreenLED led = pt.ScreenLEDFactory();
      
// pixel position     
      led.x = (radius * cos(angle));
      led.y = yOffset;
      led.z = (radius * sin(angle));
      angle += increment;
      
// calculate starting index of pixel color data
      led.setIndex(startIndex+i);
      ring.add(led);
  } 
  return ring;
}

LinkedList<ScreenLED> buildCylinder(int xDim,int yDim, float radius,float startAngle,boolean spiral) {
  LinkedList<ScreenLED> cylinder = new LinkedList<ScreenLED>();  
  int i;
  float yOffset = -(yDim * pt.getElementSize() / 2);
  
  for (i = 0; i < yDim; i++) {
    cylinder.addAll(buildRing(xDim,yOffset,radius,startAngle,TWO_PI,i * xDim));
    if (spiral) startAngle += TWO_PI / yDim;
    yOffset += pt.getElementSize();
  }
  
  return cylinder;
}

void setup() {
  size(1000,1000,P3D);     // Set up the stage 
    
  pt = new PixelTeleporter(this,"192.168.1.42");  
  pt.setElementSize(18);   // set display "led" size  
  
// Optional - load two pass blur shader  
  shade = loadShader("blur.glsl");  
  shade.set("blurSize",5);
  shade.set("sigma",5.0f);

// build 400 pixel cylinder, 20 rings of 30 pixels each, centered
// at the origin.
  object = buildCylinder(20,30,width / 10,0,false);
  pt.setObjectCenter(0,0,0);
  
// add slow rotation to enhance depth.  Spacebar toggles
// rotation on/off, 'r' resets to original orientation.
  pt.setRotation(0,0,0);
  pt.setRotationRate(0,PI / 5000, 0);
 
// start listening on network thread   
  pt.start();
}

void draw() {   
  background(30);

// draw cylinder
  pt.draw(object);    
  
// Optional - Apply blur shader
// If you need extra performance, comment out or remove
// calls to shade.set and filter().    
  shade.set("horizontalPass",0);
  filter(shade);
  shade.set("horizontalPass",1);  
  filter(shade);     
}
