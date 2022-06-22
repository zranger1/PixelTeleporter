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
RingBuffer ring;
float avg,cur,max;
color curColor = color(255,0,128);
color avgColor = color(0,255,128);
float plotY = 100;
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
  pt.setWeight(110);
  pt.setFalloff(3);
  pt.setAmbientLight(10);   

  panel = buildMatrix(numCols,numRows);
  pt.registerObject(panel);
  
  ring = new RingBuffer(600);
   
  pt.start();
}


void draw() {  
  background(0);

// draw LED matrix
  pt.draw(panel); 
  
  avg = pt.getAveragePower();
  cur = pt.getCurrentPower();
  max = pt.getMaxPower();
  ring.add(cur,avg);
  plot();

}

void labelPlotAxis() {
  float x,y,amps;

  x = 0;
  y = height - plotY;
  amps = 0;
  
  strokeWeight(1); 
  textSize(28);
  fill(curColor);
  text(String.format("Amps:    %.2f",cur),10, 26);
  fill(avgColor);
  text(String.format("Average: %.2f",avg),10, 52);
  fill(255);
  text(String.format("Max: %.2f",max),200, 26);
  textSize(12);
  
  stroke(255);
  for (int i = 0; i <= 5; i++) { 
    text(amps,x,y - 10);
    line(0,y,width,y);
    y -= 200;
    amps += 2;
  }   

}


void plot() {
    float x,yc,x1,yc1,ya,ya1;
    pt.beginHUD();
    resetShader();
    labelPlotAxis();
    strokeWeight(4);
    float[] val = new float[2];
    float inc = (float) width/599.0;
    
    x = 0;
    ring.getAt(0,val);
    yc = height - (plotY+val[0] * 100);
    ya = height - (plotY+val[1] * 100);
    
    for (int i = 0; i < ring.size();i++) {
      ring.getAt(i,val);
      x1 = (float) (i * inc);
      yc1 = height - (plotY+val[0]*100);
      ya1 = height - (plotY+val[1] * 100);  
      stroke(curColor);      
      line(x,yc,x1,yc1);  // current
      stroke(avgColor);
      line(x,ya,x1,ya1);  //average 
      x = x1;yc = yc1; ya = ya1; 
      
    }
    pt.endHUD();  
}
