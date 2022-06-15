// Pixel Teleporter Dual Matrix example
// Uses the Pixelblaze's default 2D mapping function
//
// 11/05/2020 JEM (ZRanger1)
//
// Demonstrates the use of two Pixelblazes, each sending rendering to its
// own 16x32 matrix.  
//
import pixelTeleporter.library.*;
import java.util.*;

// constants
final int numRows=16; 
final int numCols=32; 

// global variables

// A PixelTeleporter Object for each Pixelblaze
PixelTeleporter pt1;
PixelTeleporter pt2;

// An LED list for each pixelblaze
LinkedList<ScreenLED> panel1;
LinkedList<ScreenLED> panel2;

// Build 2D LED Matrix centered on the origin
LinkedList<ScreenLED> buildMatrix(PixelTeleporter pt,int dimX,int dimY) {
  int pixelSize = pt.getElementSize();
  int row,col,pixel;
  float xOffs, yOffs;
  ScreenLED led;
  LinkedList<ScreenLED> panel;
    
  panel = new LinkedList<ScreenLED>();
  
  xOffs = -((dimX-1) * pixelSize) / 2;
  yOffs = -((dimY-1) * pixelSize) / 2;
  pixel = 0;
  
  for (row = 0; row < dimY; row++) {
    for (col = 0; col < dimX; col++) {
       led = pt.ScreenLEDFactory(xOffs+(col * pixelSize), yOffs+(row * pixelSize));
       led.setIndex(pixel++);
       panel.add(led);
    }  
  }        
  return panel;
}

// These variables track the first pixel for each Pixelblaze so we can
// anchor a text label to it for convenience
ScreenLED label1;   
ScreenLED label2;
int xOffset = (26 * numCols) / 2;

void setup() {
  size(1000,500,P3D);     // Set up the stage 
  
// To use multiple Pixelblazes, each Pixelblaze needs its own PixelTeleporter object
// They must differ by IP address or port, and you must make sure that the server
// is sending to the correct place by setting the address in the firmware, command 
// line options or menu options, depending on which server you're using. 
//
  pt1 = new PixelTeleporter(this,"127.0.0.1",8083,8084);  
  pt1.setElementSize(22,95); 
  pt1.setObjectCenter(xOffset,0,0);  // move this object to the left edge of the window
  
  pt2 = new PixelTeleporter(this,"192.168.1.23");  // uses default ports 8081,8082.
  pt2.setElementSize(22,95);   
  pt2.setObjectCenter(-xOffset, 0, 0); // move this object to the right edge of the window
  
// create LED panels for each Pixelblaze,  saving the first pixel location to use as
// a label location.
  panel1 = buildMatrix(pt1,numCols,numRows);
  label1 = panel1.getFirst();
  
  panel2 = buildMatrix(pt2,numCols,numRows);
  label2 = panel2.getFirst();  
   
// start listening to both Pixelblazes   
  pt1.start();
  pt2.start();
}

void draw() {  
  background(30);
  
// set text size and color and draw labels for each panel
  textAlign(LEFT);
  textSize(40);        
  fill(130,255,255);
  text("Pixelblaze1",label1.x-xOffset,label1.y - 50);
  text("Pixelblaze2",label2.x+xOffset,label2.y - 50);

// render LED output for each panel
  pt1.draw(panel1);
  pt2.draw(panel2);
}
