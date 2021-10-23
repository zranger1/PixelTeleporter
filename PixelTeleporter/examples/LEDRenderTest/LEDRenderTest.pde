// Improved LED Simulation Renderer Testbed
// 10/23/2021 ZRanger1
//
// Be sure to change PixelTeleporter's IP address in setup() to the
// one you're using.

import pixelTeleporter.library.*;

PShape lightMap;
PShape ledModel; 
PShape backgroundPlane;
PShape highlight;
PixelTeleporter pt;

float ledPadHeight = 0;
float lightMapSize = 0;
float backgroundSize = 0;
int xSize = 16;
int ySize = 16;

// camera sensor saturation level.
// control with 0,1,2,3,4 keys. 
float lit = 64;    

int pixelCount = xSize * ySize;
int[][] leds = new int[pixelCount][2];
int frameTimer = 0;

// works well at 16x16, should be OK with other dimensions as well,
// but you may have to play with pixel size for best results.
void setup() { 
  size(768,768, P3D);
  imageMode(CENTER);
  rectMode(CENTER);
  noStroke();
  blendMode(ADD);
  
  backgroundSize = width * 1.2;
  float ledSize = backgroundSize / (xSize * 1.9);  
  lightMapSize = ledSize * 6;

  lightMap = buildLightMap((int) lightMapSize,ledSize);
  highlight = buildHighlightMap(3 * (int) ledSize);
  ledPadHeight = ledSize / 5;
  ledModel = LEDPad(ledSize,ledSize,ledPadHeight);
  backgroundPlane = backgroundPlane(width * 1.2,height * 1.2,0);
  
  initLEDCoords(xSize,ySize);
  
  pt = new PixelTeleporter(this,"192.168.1.42");  
  pt.start();
}

void draw() {
  background(0);

  // set material and lighting properties that will apply to 
  // everything drawn in the frame
  shininess(1000);
  directionalLight(lit,lit,lit,0,0,-1);  
  lightSpecular(255,255,255);     

  // draw the background
  shape(backgroundPlane);
 
  for (int n = 0; n < pixelCount; n++) {
    pushMatrix();
    translate(leds[n][0],leds[n][1],ledPadHeight);
    
    int col = pt.pixelBuffer[n];  
    colorMode(HSB,255,255,255);
    float b = brightness(col);
    colorMode(RGB,255,255,255);
    
    // make sure there's always a tiny amount of
    // ambient white light around.
    if (b < 10) {
      col = color(10);
      b = 10;
    }
      
    // set up material properties and draw individual 
    // LEDs, then lay the light map on top.
    emissive(col);
    specular(b);
       
    tint(col);  
    shape(ledModel); 
    translate(0,0,4);
    shape(lightMap); 

    // draw (white) highlights proportional to brightness
    tint((int) b);
    emissive((int) b);
    shape(highlight);
    
    popMatrix();
  }
  
   if (millis() - frameTimer > 2000) {
     println(frameRate);
     frameTimer = millis();
   }
}

// Keyboard UI for camera saturation
void keyPressed() {
  switch (key) {
    case '0':
      lit = 0;
      break;
    case '1':
      lit = 32;
      break;
    case '2':
      lit = 64;
      break;
    case '3':
      lit = 128;
      break;
    case '4':
      lit = 256;
      break;  
   }        
}
