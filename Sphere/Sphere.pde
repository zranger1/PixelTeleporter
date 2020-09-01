// Pixel Teleporter sphere demo
// Fibonacci spiral sphere mapping algorithm from:
// https://bduvenhage.me/geometry/2019/07/31/generating-equidistant-vectors.html
//
// Requires the sphere mapping function from this repository's
// MappingFunctions directory.
//
// 8/26/2020 JEM (ZRanger1)

// global variables
PixelTeleporter pt;           // network data handler
LinkedList<ScreenLED> object; // list of LEDs in our object w/position and color info

PShader shade;               // shader for post processing effect

// build sphere centered at the origin  
// Draws a fibonacci spiral to distribute points at equal distances on the 
// surface of the sphere.  Treating this as a 2D object in a pixelblaze pattern
// requires the sphere map generator. (spheremap.js in this directory) 
public LinkedList<ScreenLED> buildSphere(int pixelCount,float radius) {
  LinkedList<ScreenLED> object;
  
  float phi = (sqrt(5)+1)/2 - 1; // golden ratio
  float ga = phi*2*PI;           // golden angle
  float lat,lon;
  float x,y,z;
  
  object = new LinkedList<ScreenLED>();
       
  if (radius == 0) {
    radius = height / 3;
  }
  pt.setObjectCenter(0,0,radius / 2);  
   
  for (int i = 1; i <= pixelCount; ++i) {
    lon = ga*i;    

    lon /= 2*PI; lon -= floor(lon); lon *= 2*PI;
    if (lon > PI)  lon -= 2*PI;
 
    lat = asin(-1 + 2*i/(float)pixelCount);
      
    pushMatrix();

    rotateY(lon);
    rotateZ(-lat);
      
    x = modelX(radius,0,0);
    y = modelY(radius,0,0);
    z = modelZ(radius,0,0);
      
    popMatrix(); 
      
    ScreenLED led = new ScreenLED();
    led.index = i * 3;      
    led.x = x; led.y = y; led.z = z;
    object.add(led);
  }  
  return object;
}  

void setup() {
  size(1000,1000,P3D);      // Set up the stage 
  colorMode(RGB, 255);   
  noStroke();
  rectMode(CENTER);         // Rectangles are positioned based on their center.
  ellipseMode(CENTER);      // Ellipses are positioned based on their center.  
  sphereDetail(10);         // reduce number of sphere vertices to improve performance
  
  pt = new PixelTeleporter("192.168.1.42",8081);  
  pt.setElementSize(18);   // set display "led" size  
  shade = loadShader("blur.glsl");  
  shade.set("blurSize",5);
  shade.set("sigma",6.0f);

// create sphere with 400 pixels and an automatically calculated radius
  object = buildSphere(400,0);
  
// add slow rotation to enhance depth.  Spacebar toggles
// rotation on/off.
  pt.setRotation(0,0,0);
  pt.setRotationRate(PI / 5000, 0,PI / 7000);
 
// start listening on network thread   
  pt.start();
}

void draw() {   
  pt.readData();
  background(30);
  
  pt.applyViewingTransform();
  
  for (ScreenLED led : object) {
    led.draw3D();
  }    

// if you need the extra performance, comment out the shader calls
  shade.set("horizontalPass",0);
  filter(shade);
  shade.set("horizontalPass",1);  
  filter(shade); 
  
  pt.requestData();    
}
