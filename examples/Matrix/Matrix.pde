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

// global variables
PixelTeleporter pt;
LinkedList<ScreenLED> panel;    // list of LEDs in our matrix w/position and color info
PShader bloom;


// build 2D LED Matrix centered on the origin
LinkedList<ScreenLED> buildMatrix(int dimX,int dimY) {
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

void setup() {
  size(800,800,P3D);     // Set up the stage 
  
  pt = new PixelTeleporter(this,"192.168.1.42");
  pt.setElementSize(40,90);
  
  // All the available rendering controls!
  pt.setRenderMethod(RenderMethod.HD2D);
  //pt.setRenderControl(RenderControl.LEDMODEL_BULB,0);  
  pt.setRenderControl(RenderControl.LEDMODEL_SMD,0);    
  pt.setRenderControl(RenderControl.FALLOFF,2);
  pt.setRenderControl(RenderControl.INDIRECT_INTENSITY,0.35); 
  pt.setRenderControl(RenderControl.AMBIENT_LIGHT,16);
  pt.setRenderControl(RenderControl.OVEREXPOSURE,0.75);
  pt.setRenderControl(RenderControl.GAMMA,1.0);
  //pt.setRenderControl(RenderControl.BGALPHA,0);  
  

// Optional - load single pass blur shader
  bloom = loadShader("bloom.glsl");
  bloom.set("overdrive",1.0);
  bloom.set("gamma",1.0);  
  
  panel = buildMatrix(numCols,numRows);
   
  pt.start();
}

void draw() {  
  background(30);

// draw LED matrix
  pt.draw(panel);
  
// Testing!  Apply overexposure shader. 

  filter(bloom);          
}
