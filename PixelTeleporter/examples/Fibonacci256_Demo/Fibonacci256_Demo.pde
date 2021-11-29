// Pixel Teleporter volumetric MapIO example
// Build a displayable object from an exported Pixelblaze pixel map.
//
// 8/26/2020 JEM (ZRanger1)
import pixelTeleporter.library.*;
import java.util.*;

// global variables
PixelTeleporter pt;           // network data handler
LinkedList<ScreenLED> object; // list of LEDs in our object w/position and color info

PShader shade;               // shader for post processing effect

void setup() {
  size(1000,1000,P3D);     // Set up the stage 
  
  pt = new PixelTeleporter(this,"192.168.1.42");  
  pt.setElementSize(40,100);   // set display "led" size
  pt.setRenderMethod(RenderMethod.HD2D);
  pt.setRenderControl(RenderControl.LEDMODEL_SMD,0);  
  pt.setRenderControl(RenderControl.FALLOFF,2);
  pt.setRenderControl(RenderControl.BGALPHA,0);

// read JSON pixel map from file
  object = 
     pt.importPixelblazeMap("data/fibonacci256.json",3);
    
  
// add slow rotation to enhance depth.  Spacebar toggles
// rotation on/off, mouse wheel zooms, 'r' resets to original orientation.
//  pt.setRotation(radians(-20),0,0);
//  pt.setRotationRate(0,0, -PI/4000);
 
// start listening on network thread   
  pt.start();
}

PVector sz = new PVector();
void draw() {
 
  background(color(48,40,40));
  noStroke();
  blendMode(REPLACE);
  fill(0,255);
  circle(0,0,width * 0.825);
  blendMode(ADD);
  
  pt.draw(object);
}
