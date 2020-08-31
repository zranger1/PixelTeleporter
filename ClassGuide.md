# PixelTeleporter Processing Class Guide

To include PixelTeleporter in a processing sketch, copy the file PixelTeleporter.pde to your
sketch directory from the /PixelTeleporter directory of the repository.

## Basics
PixelTeleporter is designed to get you up and displaying pixels with as little code overhead
as possible. See the examples for complete implementations and handy tools. To use PixelTeleporter in
a sketch, you need to create a PixelTeleporter object and a list of pixels to display.  Like this:

```
    PixelTeleporter pt;
    LinkedList<ScreenLED> panel;  
```

In your sketch's setup() function, initialize your objects (using your own PixelTeleporter
transmitter's IP address, of course).

``` 
    void setup() {
      size(1000,1000,P3D);     // Set up the stage 
      
// More or less required rendering settings...      
      colorMode(RGB, 255);   
      noStroke();              // do not outline
      rectMode(CENTER);        // Rectangles are positioned based on their center.
      ellipseMode(CENTER);     // Ellipses are positioned based on their center.
      sphereDetail(8);         // Reduce sphere polygon count to improve performance   
           
// initialize PixelTeleporter object    
      pt = new PixelTeleporter("192.168.1.42",8081);  
      pt.setElementSize(20);  // set display size of virtual LEDs
      pt.start();             // start the network thread. Request and listen for frames.
 
// initialize your LED object 
      panel = buildAnInterestingLEDObject(); // left as an exercise for reader!
    }
```

In your draw() function, you can call.

```
    void draw() {
      pt.readData();  // get the most recent frame of pixel data
    
    ...    
  
      pt.applyViewingTransform();  // apply current camera zoom and rotation to scene

    /// your rendering code.  for example...
      for (ScreenLED led : panel) {
        led.draw3D();
      } 
      
    ... 
    
      pt.requestData(); // request a new frame of pixel data from the network.
   }  
```

That's all you need for a basic PixelTeleporter sketch.  Again, see the examples for
complete, working sketches.

## Class Reference
### Class PixelTeleporter
#### PixelTeleporter(String ipAddr,int port)
Constructor.  Takes IPv4 address and port number. Port 8081 is usual.
#### public void start()
Start network thread, prepare to request and recieve data
#### public int readData()
Read the latest available frame of network data
#### public void requestData()
Request a frame of pixel data from the network  
#### public void applyViewingTransform()
Apply the current worldspace transform to the scene
#### void setElementSize(int n, int pct)
void setElementSize(int n)

Set size of LED element in world coordinates.  pct gives the percentage (0-100) of
the total size that can be "illuminated".
#### void setObjectCenter(float x, float y, float z)
Set the rotational center of the object in the current scene
#### void setRotation(float x, float y, float z)
Set static rotation of current scene (in radians)
#### void setRotationRate(float x, float y, float z)
Set rotation rate in radians per millisecond.  The user can 
stop/start this "automatic" rotation by pressing the space bar.
### Class ScreenLED
#### ScreenLED(float x, float y, float z)
ScreenLED(float x,float y)

Create a new ScreenLED object with the specified location (in world coordinates);
#### public void setIndex(int n)
Set the pixel index (into the current network data array) for a ScreenLED object
#### public int getIndex()
Retrieve the object's pixel index
#### public void draw2D()
Render LEDs as translucent circles.  Faster than draw3D, but not as cool.
#### public void draw3D()
Render LEDs as translucent spheres, with diameter proportional to on
brightness.
