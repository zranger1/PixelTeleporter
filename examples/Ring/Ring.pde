// Pixel Teleporter ring example 
// Build a ring or circular arc
//
// 8/26/2020 JEM (ZRanger1)
import pixelTeleporter.library.*;
import java.util.*;

// global variables
PixelTeleporter pt;        
PShader blur; 
LinkedList<ScreenLED> ring;    // list of LEDs in our ring w/position and color info

// build ring centered at origin of the drawing area.
LinkedList<ScreenLED> buildRing(int nPixels,float radius,float startAngle,float stopAngle) {
  LinkedList<ScreenLED> ring;
  float angle, increment;
  float centerX = width / 2.0;
  float centerY = height / 2.0;
    
  ring = new LinkedList<ScreenLED>();
         
// if supplied radius is zero, calculate a radius that gives decent pixel spacing
  if (radius == 0) {
    radius = min(centerX,centerY) - (2 * pt.getElementSize());      // max displayable radius
    radius = min(radius,max(50,(float) nPixels * 4.25)); // try to pick a nice looking size
  }
  angle = startAngle;  
  increment = stopAngle / (float) nPixels;
    
  for (int i = 0; i < nPixels; i++) {
      ScreenLED led = pt.ScreenLEDFactory(radius * cos(angle), radius * sin(angle));
      led.setIndex(i);                                    
      angle += increment;      
      ring.add(led);
  } 
  return ring;
}

void setup() {
  size(1000,1000,P3D);     // Set up the stage 
   
  pt = new PixelTeleporter(this,"192.168.1.42");  
  pt.setElementSize(20);  
//  pt.setObjectCenter(0,0,0);
  
// Optional - load single pass blur shader.  
  blur = loadShader("blur.glsl");

// build ring of 64 pixels, starting at 0 radians, with automatic radius  
  ring = buildRing(64,0,0,TWO_PI);  

  pt.start();
}

void draw() { 
    background(30);

// draw LED ring    
    pt.draw(ring);
    
// Optional - Apply blur shader
    filter(blur);           
}
