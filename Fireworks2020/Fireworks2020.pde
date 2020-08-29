/*
  "Star" layout designed to work with Jeff Vyduna's Fireworks2020 pattern.  See the video
  of his actual setup at  https://youtu.be/zgF3DJoTAWI.  The pattern is available under
  the topic "Fireworks for the 4th" in the Pixelblaze forums.
  
*/

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
     radius = pixelsPerArm * pixelSize;
  } 
  
// we use the first pixel of each arm as our rotational center.  
  float x = radius * cos(startAngle);   
  float y = radius * sin(startAngle);
  
  float armRotation = 0;
  
  for (int i = 0; i < numArms; i++) {
    LinkedList<ScreenLED> arm = new LinkedList<ScreenLED>();
    armStartIndex = armOrder[i] * pixelsPerArm;
    println(armStartIndex);
    angle = startAngle;
    for (int j = 0; j < pixelsPerArm; j++) {
      pushMatrix();
      rotateZ(armRotation);
      translate(radius * cos(angle), radius * sin(angle),0);
      translate(-x,-y,0);
      
      led = new ScreenLED(modelX(0,0,0),modelY(0,0,0),0);
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
  colorMode(RGB, 255);   
  noStroke();
  rectMode(CENTER);        // Rectangles are positioned based on their center.
  ellipseMode(CENTER);     // Ellipses are positioned based on their center.
  sphereDetail(8);         // reduce polygons in sphere for improved performance
  
  pt = new PixelTeleporter("192.168.1.42",8081);
  pt.setElementSize(20);

  shade = loadShader("blur.glsl");   
  shade.set("blurSize",4);
  shade.set("sigma",9.0f);  
  
  panel = buildFireworksDisplay(480,8,0.0);
   
  pt.start();
}

void draw() {  
  pt.readData();
  background(0);
  
  pt.applyViewingTransform();
    
  for (ScreenLED led : panel) {
    led.draw3D();
  }    
  
// if you need extra performance, comment out the shader calls  
  shade.set("horizontalPass",0);
  filter(shade);
  shade.set("horizontalPass",1);  
  filter(shade);     
  pt.requestData();    
}
