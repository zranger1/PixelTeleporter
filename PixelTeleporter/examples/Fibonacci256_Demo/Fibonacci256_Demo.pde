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
  pt.setElementSize(22,90);   // set display "led" size
  pt.setRenderMethod(RenderMethod.HD2D);
  pt.setRenderControl(RenderControl.LEDMODEL_SMD,0);  
  pt.setRenderControl(RenderControl.FALLOFF,5.5);
  pt.setRenderControl(RenderControl.BGALPHA,0);

  
// Optional - load and configure two pass blur shader  
//  shade = loadShader("blur.glsl");  
//  shade.set("blurSize",4);
//  shade.set("sigma",5.0f);

// read JSON pixel map from file
  object = 
     pt.importPixelblazeMap("data/fibonacci256.json",1);
    
  
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
  circle(0,0,width * 0.275);
  blendMode(ADD);
  
  pt.draw(object);

// Optional - Apply blur shader
// If you need extra performance, comment out or remove
// calls to shade.set and filter().    
//  shade.set("horizontalPass",0);
//  filter(shade);
//  shade.set("horizontalPass",1);  
//  filter(shade);   
}
