// Pixel Teleporter 2D LED Panel example
// Uses the Pixelblaze's default 2D mapping function
//
// 8/26/2020 JEM (ZRanger1)
//
// Sets up a matrix with the maximum supported number of pixels:  32x64=2048
// when you change the matrix size, be sure to adjust the processing stage
// size in setup() as well.  
import pixelTeleporter.library.*;
import java.util.*;

// constants
final int numRows=16; 
final int numCols=16; 
final float pixelSize = 28;

// global variables
PixelTeleporter pt;
LinkedList<ScreenLED> panel;    // list of LEDs in our matrix w/position and color info
int frameTime;

// build 2D LED Matrix centered on the origin
LinkedList<ScreenLED> buildMatrix(int dimX,int dimY) {
  int row,col,pixel;
  float xOffs, yOffs;
  float pixelSpacing = pixelSize * 0.5;
  ScreenLED led;
  LinkedList<ScreenLED> panel;
    
  panel = new LinkedList<ScreenLED>();
  
  xOffs = -((dimX-1) * pixelSpacing) / 2;
  yOffs = -((dimY-1) * pixelSpacing) / 2;
  pixel = 0;
  
  for (row = 0; row < dimY; row++) {
    for (col = 0; col < dimX; col++) {
       led = pt.ScreenLEDFactory(xOffs+(col * pixelSpacing), yOffs+(row * pixelSpacing));
       led.setIndex(pixel++);
       panel.add(led);
    }  
  }        
  return panel;
}

void setup() {
  size(800,800,P3D);     // Set up the stage 
  
  pt = new PixelTeleporter(this,"192.168.1.42");
  pt.setModel(LEDType.STAR);
  pt.setWeight(80);
  pt.setFalloff(2);
  pt.setAmbientLight(10);   

  panel = buildMatrix(numCols,numRows);
  pt.registerObject(panel);
   
  pt.start();
  frameTime = millis();
}


void draw() {  
  background(30);

// draw LED matrix
  pt.draw(panel); 
  
  if ((millis() - frameTime) > 2000) {
    frameTime = millis();
    println("Current, Average, Max: ",pt.getCurrentPower(),pt.getAveragePower(),pt.getMaxPower());
  }
}
