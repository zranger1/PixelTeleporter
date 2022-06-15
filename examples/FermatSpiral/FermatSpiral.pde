// Pixel Teleporter Fermat Spiral (Fibonacci256) example
//
// 10/21/2020 JEM (ZRanger1)
//
// Creates a fermat spiral of 256 pixels.  Use an angle of 137.5 to create a "normal"
// fibonacci pattern, other angles create other interesting spirals and patterns.
import pixelTeleporter.library.*;
import java.util.*;

// global variables
PixelTeleporter pt;
LinkedList<ScreenLED> panel;    // list of LEDs in our spiral w/position and color info
PShader blur;

LinkedList<ScreenLED> buildFermatSpiral(int pixelCount, float angle) {  
  float x,y,r,theta;
  float pixSize;
  float originX = 0;
  float originY = 0;
  ScreenLED led;
  LinkedList<ScreenLED> panel;

// set pixel spacing to something that looks reasonable
  pixSize = 1.4 * pt.getElementSize();
     
  panel = new LinkedList<ScreenLED>();
    
// create spiral, pixel 0 at center.      
  for(int i = 0; i < pixelCount; i++) {
    r = pixSize * sqrt(i);
    theta = i * angle;

    y = originY + (-1 * r * sin(theta));
    x = originX + (r * cos(theta));
      
    led = pt.ScreenLEDFactory(x,y);
    led.setIndex(i);
    panel.add(led);              
  }
  return panel;
}

void setup() {
  size(1000,1000,P3D);     // Set up the stage 
  
  pt = new PixelTeleporter(this,"127.0.0.1");
  pt.setElementSize(18);

// Optional - load single pass blur shader
  blur = loadShader("blur.glsl");   
  
  panel = buildFermatSpiral(256,radians(137.5));
  
// add very slow rotation to enhance trippyness.  Spacebar toggles
// rotation on/off, mouse wheel zooms, 'r' resets to original orientation.
  pt.setRotation(0,0,0);
  pt.setRotationRate(0, 0,PI / 8000);  
   
  pt.start();
}

void draw() {  
  background(15);

// draw LED matrix
  pt.draw(panel);

// Optional - Apply blur shader. 
  filter(blur);          
}
