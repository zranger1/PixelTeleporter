/*
  Pixel Teleporter Fireworks 2020 example
  "Star" layout designed to work with Jeff Vyduna's Fireworks2020 pattern.  See the video
  of his actual setup at  https://youtu.be/zgF3DJoTAWI.  The pattern is available under
  the topic "Fireworks for the 4th" in the Pixelblaze forums. 
*/
import pixelTeleporter.library.*;
import java.util.*;

//  Constants
final int pixelCount = 480;
final int numArms = 8;

// global variables
PixelTeleporter pt;
LinkedList<ScreenLED> panel;    // list of LEDs in our matrix w/position and color info
int [] armOrder = new int[] {3,0,6,5,2,1,7,4};
PShader shade;

// build fireworks arrangement from circular arcs
public LinkedList<ScreenLED> buildFireworksDisplay(int pixelCount, int numArms,float radius) {
  LinkedList<ScreenLED> object = new LinkedList<ScreenLED>();
  ScreenLED led;
  
  int pixelsPerArm = pixelCount / numArms;
  float startAngle = 0;
  float angle;  
  float increment = radians(45) / pixelsPerArm;
  int armStartIndex = 0;
  
  if (radius == 0) {
     radius = pixelsPerArm * pt.getElementSize();
  } 
  
// we use the first pixel of each arm as our rotational center.  
  float x = radius * cos(startAngle);   
  float y = radius * sin(startAngle);
  
  float armRotation = 0;
  
  for (int i = 0; i < numArms; i++) {
    LinkedList<ScreenLED> arm = new LinkedList<ScreenLED>();
    armStartIndex = armOrder[i] * pixelsPerArm;
    angle = startAngle;
    for (int j = 0; j < pixelsPerArm; j++) {
      pushMatrix();
      rotateZ(armRotation);
      translate(radius * cos(angle), radius * sin(angle),0);
      translate(-x,-y,0);
      
      led = pt.ScreenLEDFactory(modelX(0,0,0),modelY(0,0,0),0);
      led.setIndex(armStartIndex + j);
      
      arm.add(led);
      popMatrix();
      angle += increment;
    }
    armRotation += radians(45);
    object.addAll(arm);   
  }  
  return object;  
}

void setup() {
  size(1000,1000,P3D);     // Set up the stage 
  
  pt = new PixelTeleporter(this,"192.168.1.42");
  pt.setElementSize(20);

// Optional = load and configure two pass blur shader
  shade = loadShader("blur.glsl");   
  shade.set("blurSize",4);
  shade.set("sigma",9.0f);  
  
  panel = buildFireworksDisplay(480,8,0.0);
   
  pt.start();
}

void draw() {  
  background(0);

// draw fireworks display
  pt.draw(panel);
     
// Optional - Apply blur shader
// If you need extra performance, comment out or remove
// calls to shade.set and filter().  
  shade.set("horizontalPass",0);
  filter(shade);
  shade.set("horizontalPass",1);  
  filter(shade);        
}
