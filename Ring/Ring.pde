// Pixel Teleporter ring demo
//
// 8/26/2020 JEM (ZRanger1)
// global variables
PixelTeleporter pt;        // pixel data receiver
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
    radius = min(centerX,centerY) - (2 * ledSize);      // max displayable radius
    radius = min(radius,max(50,(float) nPixels * 4.25)); // try to pick a nice looking size
  }
  angle = startAngle;  
  increment = stopAngle / (float) nPixels;
    
  for (int i = 0; i < nPixels; i++) {
      ScreenLED led = new ScreenLED((radius * cos(angle)),
                                    (radius * sin(angle)));
      led.setIndex(i);                                    
      angle += increment;      
      ring.add(led);
  } 
  return ring;
}

void setup() {
  size(1000,1000,P3D);     // Set up the stage 
  colorMode(RGB, 255);   
  noStroke();
  rectMode(CENTER);        // Rectangles are positioned based on their center.
  ellipseMode(CENTER);     // Ellipses are positioned based on their center.  
   
  pt = new PixelTeleporter("192.168.1.42",8081);  
  pt.setElementSize(20);  
  pt.setObjectCenter(0,0,0);
  blur = loadShader("blur.glsl");

// build ring of 64 pixels, starting at 0 radians, with automatic radius  
  ring = buildRing(64,0,0,TWO_PI);  

  pt.start();
}

void draw() { 
    pt.readData();
    background(30);
    
    pt.applyViewingTransform();
    
    for (ScreenLED led : ring) {
      led.draw3D();
    } 
    
    filter(blur);        
    pt.requestData();    
}
