/////////////////////////////////////////////////////////// 
////////////////////////////////////////////////////////////
// Pixel Teleporter
// Makes it easier to prototype arrangements of LEDs by
// encapsulating everything needed to read and display pixels
// from a hardware LED controller. 
//
// Version  Date         Author Comment
// v0.0.1   08/20/2020   JEM(ZRanger1)    Created
// v0.0.2   09/08/2020   JEM(ZRanger2)    bug fixes, import/export map
//
//////////////////////////////////////////////////////////
import java.lang.*; 
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.Arrays;

// constants
final int PIXEL_BUFFER_SIZE=8192;  // room for 2048 pixels, plus a bit
final int UDP_NONE=0;              // states for UDP listener
final int UDP_REQUESTED=0x01;
final int UDP_RECEIVED=0x03;

// global variables
int ledSize = 15;        // world coordinate total size of led element
int pixelSize = 20;      // portion of led that lights up.
Mover g_mover = null;    // currently active mover object.  There can be only one.

// empty array to return when no packet has been received.
final static byte [] NO_PACKET = {};

// command to fetch a frame from the microcontroller.  TODO -
// need to implement on the esp8266 side when we actually have more than one
// command.  For the moment, sending any datagram to the proper port acts
// as a data frame request.
final static byte CMD_REQUEST_FRAME = (byte) 0xF0;

////////////////////////////////////////////////////////
// Class PixelTeleporter
// Top level class.  Create one of these in your sketch then
// call its start() method to listen on the network.
//
// Contains a PixelTeleporterThread object, which manages communication,
// and a Mover object, which handles rendering and camera control.
////////////////////////////////////////////////////////
public class PixelTeleporter {
  PixelTeleporterThread thread;
  Mover mover;
 
  public PixelTeleporter(String ipAddr,int port) {
    mover = new Mover();    
    thread = new PixelTeleporterThread(ipAddr,port,PIXEL_BUFFER_SIZE);
  }

// start network listener
  public void start() {
    thread.start();
  }
  
// read most recent frame of data returned from the network  
  public int readData() {
    return thread.readData(mover.pixelBuffer);
  }

// request a frame of pixel data from the network  
  public void requestData() {
    thread.requestData();
  }  
  
  public void applyViewingTransform() {
    mover.applyViewingTransform();
  }

// various setter/getter functions  
  void setElementSize(int n, int pct) { mover.setElementSize(n,pct); }
  void setElementSize(int n) { mover.setElementSize(n,40); }
  void setObjectCenter(float x, float y, float z) { mover.setObjectCenter(x,y,z); }
  void setRotation(float x, float y, float z) { mover.setRotation(x,y,z); }
  void setRotationRate(float x, float y, float z) { mover.setRotationRate(x,y,z); }    
  
}

//////////////////////////////////////////////////////////
// Class PixelTeleporterThread 
// Handles requesting a frame of pixel data and copying the data into
// a user supplied buffer in a processing-friendly color format.
// Runs in a separate thread so we can be non-blocking.
//
// Once started, loops waiting for datagrams, yielding
// every time .receive() times out.
/////////////////////////////////////////////////////////
public class PixelTeleporterThread extends Thread {   
  DatagramSocket ds; 
  int port;
  byte[] buffer;
  byte[] sendbuf;
  DatagramPacket datagramIn;
  DatagramPacket datagramOut;

  boolean running;   
  int status;
  
  PixelTeleporterThread(String ipAddr,int port, int bufsize) {
    this.port = port;  
    buffer = new byte[bufsize];
    sendbuf = new byte[128];  
    sendbuf[0] = CMD_REQUEST_FRAME;  
    datagramIn = new DatagramPacket(buffer, buffer.length); 

    InetSocketAddress sourceAddress = new InetSocketAddress(ipAddr,8081);    
    datagramOut = new DatagramPacket(sendbuf,4,sourceAddress);
    
    running = false;
    status = UDP_NONE;

    try {
      ds = new DatagramSocket(port);
      ds.setSoTimeout(60);
    } catch (SocketException e) {
      e.printStackTrace();
    }
  }

// If data isn't already waiting to be read, ask for an update.  
  public void requestData() {     
    if (available()) return;
 
    try {
      ds.send(datagramOut);
      status = UDP_REQUESTED; 
    } 
    catch (IOException e) {
     println(e.getMessage());
     e.printStackTrace();
    }
  }

  public boolean available() {
    return (status == UDP_RECEIVED);    
  }
  
// Copies pixel colors from the network buffer of bytes into a user
// supplied buffer of integers, which work better with processing. (For some
// reason, Java thinks that bytes should be signed, so if I just pass the byte
// array back, the user has to deal with the weird, expensive conversion.  Might
// as well do it here and make things easy on the user.)
// Returns number of pixels copied.
  int readData(int [] buffer) {
    byte [] data;
    int i;
    
    if (available()) {
      data = datagramIn.getData();
      i = 0;
      while (i < datagramIn.getLength()) {
         buffer[i] = Byte.toUnsignedInt(data[i++]);  //r
         buffer[i] = Byte.toUnsignedInt(data[i++]);  //g
         buffer[i] = Byte.toUnsignedInt(data[i++]);  //b             
      }
      status = UDP_NONE;   
      return datagramIn.getLength() / 3;
    }
    else {
      return 0;
    }
  }      

  void start () {
    System.out.println("PixelTeleporter thread starting");  
    running = true;
    super.start();
  }

  void run() {
    while (running) {
      waitForDatagram();
      yield();
    } 
  }

  void waitForDatagram() {    
    if (status == UDP_REQUESTED) {  
      try {
        ds.receive(datagramIn);
      } 
      catch (IOException e) { // catch those pesky timeouts
        status = UDP_NONE;  
        return;
      }     
      status = UDP_RECEIVED;
    }  
  }

  void quit() {
    System.out.println("PixelTeleporter thread stopping"); 
    running = false;  
    interrupt();
  }
}


//////////////////////////////////////
// Class ScreenLED
// Represents individual "LEDs".  x,y,z position are coordinates in model space
// and index is the pixel's index in the incoming RGB data stream. 
//////////////////////////////////////
public class ScreenLED {
  float x,y,z;
  int index;
  
  public ScreenLED() {
    index = 0;
  }
  
  public ScreenLED(float x,float y) {
    this.x = x;
    this.y = y;
    this.z = 0;
  }
  
  public ScreenLED(float x, float y, float z) {
    this.x = x;
    this.y = y;
    this.z = z;
    index = 0;
  }
  
  public void setIndex(int n) {
    if (n >= 2048) {
      println("EXTREMELY DIRE WARNING: Pixel index ",n," out of bounds in ScreenLED:setIndex. (max is 2047)");
      println("I'd throw an exception, but frankly it's a pain in the butt for everyone, and I trust you to read!"); 
      return;
    } 
    index = 3 * n;    
  }
  
  public int getIndex() {
    return index / 3;
  }
  
  public void draw2D() {
    int r,g,b,i;   
    pushMatrix();
    
    translate(x,y);
    i = index;
    r = g_mover.pixelBuffer[i++]; g = g_mover.pixelBuffer[i++]; b = g_mover.pixelBuffer[i];
    fill(r,g,b);
    circle(0,0,ledSize);   
    popMatrix();    
  } 
  
  public void draw3D() {
    int r,g,b,i;
    float size,mag;
    pushMatrix();    
    translate(x,y,z);
    
    i = index;
    r = g_mover.pixelBuffer[i++]; g = g_mover.pixelBuffer[i++]; b = g_mover.pixelBuffer[i];
    
// draw roughly larger sphere for roughly brighter pixel    
    mag = ((r > g) ? ((r > b) ? r : b) : ((g > b) ? g : b)) / 255.0;
    size = ledSize + (ledSize * mag);
    
    fill(r,g,b,176);
    sphere(size);
    
    popMatrix();
  }
}

//////////////////////////////////////
// Class Mover
// Handles object rotation and user camera control
//////////////////////////////////////
class Mover {
  int[] pixelBuffer;       // buffer for incoming network data
  
// camera position  
  PVector origin;
  PVector eye;
  
// object position/rotation  
  PVector objectCenter;
  PVector currentRotation;
  PVector rotationRate;
  boolean autoMove; 
  
// rotation controlled by mouse drag  
  PVector mouseRotation;

  int timer;
  int dragOriginX,dragOriginY;
  float mouseZoom;
  
  public Mover() {
    g_mover = this;
    
    pixelBuffer = new int[PIXEL_BUFFER_SIZE];    
    
//  camera    
    origin = new PVector(width / 2, height / 2, -200);
    eye = origin.copy();
    eye.z = 1000;
    
// vectors for object movement/rotation    
    objectCenter = new PVector(0,0,0);
    currentRotation = new PVector(0,0,0);
    rotationRate = new PVector(0,0,0);
    mouseRotation = new PVector(0,0,0);
    mouseZoom = 0;
    autoMove = true;
    
    initializeCamera();
    
    timer = millis();
  }  

  void rotateAndNormalize(PVector p1,PVector p2) {
    p1.x = (p1.x + p2.x) % TWO_PI;
    p1.y = (p1.y + p2.y) % TWO_PI;
    p1.z = (p1.z + p2.z) % TWO_PI;  
  }
  
// set size of displayed led "pixels"  Total size <n> includes
// space around the lighted portion.  The <pct> (0-100) argument sets
// the percentage of the total space that will be "lighted."
  public void setElementSize(int n,int pct) {
    pixelSize = n;
    ledSize = pct * pixelSize / 100;
  }
  
  void setObjectCenter(float x, float y, float z) { objectCenter.set(x,y,z); }
  void setRotation(float x, float y, float z) { currentRotation.set(x,y,z); }
  void setRotationRate(float x, float y, float z) { rotationRate.set(x,y,z); } 
  
  public void initializeCamera() {   
    camera(eye.x,eye.y,eye.z,                 // looking from...
           origin.x,origin.y,objectCenter.z,  // looking at...
           0.0, 1.0, 0.0);                    // upX, upY, upZ    
    
    mouseZoom = 0.0;     
  }
  
  public void moveCamera() {     
  camera(eye.x,eye.y,eye.z,                // looking from...
         origin.x,origin.y,objectCenter.z, // looking at...
         0.0, 1.0, 0.0);                   // upX, upY, upZ     
  }
  
  public void zoomCamera(float amount) {
    eye.add(0,0,amount);
  }    
  
  void applyMouseRotation() {
    rotateX(-mouseRotation.y);
    rotateY(mouseRotation.x);
  }
  
  void applyViewingTransform() { 
  moveCamera();    
  translate(width / 2,height /2, 0);
  
  applyMouseRotation();

  if (autoMove) {
    PVector deltaRotation = PVector.mult(rotationRate,(float) millis()-timer);
    rotateAndNormalize(currentRotation,deltaRotation);
  }

  timer = millis();

  rotateX(currentRotation.x);
  rotateY(currentRotation.y);
  rotateZ(currentRotation.z);
  translate(-objectCenter.x,-objectCenter.y,-objectCenter.z);     
  } 
}

//////////////////////////////////////
// Utility Methods - mostly for working with "objects"
// or lists of ScreenLEDs.  
// WARNING:  This is #1 on the list for refactoring when
// PixelTeleporter becomes a real Java library.  Shouldn't
// change much in sketch code, but keep an eye out.
//////////////////////////////////////
 
LinkedList<ScreenLED> importPixelblazeMap(String fileName,float scale) {
  LinkedList<ScreenLED> object = new LinkedList<ScreenLED>();
  JSONArray json = loadJSONArray(fileName);
  print(json.size());
  
  for (int i = 0; i < json.size(); i++) {
   float x,y,z;
    
    JSONArray mapEntry = json.getJSONArray(i);
    float [] coords = mapEntry.getFloatArray();  
    
    x = coords[0];
    y = coords[1];
    z = (coords.length == 3) ? z = coords[2] : 0;
    
    ScreenLED led = new ScreenLED(scale * x,scale * y,scale * z);
    led.setIndex(i);
    object.add(led);
  }
  
  return object;
}

// comparator for sorting.  Used by exportPixeblazeMap()
class compareLEDIndex implements Comparator<ScreenLED> {
   public int compare(ScreenLED p1, ScreenLED p2) {
     return (p1.index < p2.index) ? -1 : 1;
   }
}

// Write ScreenLED list coordinates to file as a JSON pixel map
boolean exportPixelblazeMap(LinkedList<ScreenLED> object,String fileName,float scale, boolean is3D) {
  JSONArray json,mapEntry;
  
// sort object by pixel index for export. 
  LinkedList<ScreenLED> sortedCopy = (LinkedList<ScreenLED>) object.clone();
  Collections.sort(sortedCopy,new compareLEDIndex());
  print(sortedCopy.size());

  json = new JSONArray();
  for (ScreenLED led : sortedCopy) {
    mapEntry = new JSONArray();
    mapEntry.append(scale * led.x);
    mapEntry.append(scale * led.y);
    if (is3D) mapEntry.append(scale * led.z);    
   
    json.append(mapEntry);
  }  
  return saveJSONArray(json,fileName);  
}

// Find geometric center of object represented by ScreenLED list
PVector findObjectCenter( LinkedList<ScreenLED> object) {
  PVector c = new PVector(0,0,0);
  PVector mins = new PVector(0,0,0);
  PVector maxes = new PVector(0,0,0);
  
  for (ScreenLED led : object) {
    if (led.x < mins.x) mins.x = led.x; if (led.x > maxes.x) maxes.x = led.x;
    if (led.y < mins.y) mins.y = led.y; if (led.y > maxes.y) maxes.y = led.y;
    if (led.z < mins.z) mins.z = led.z; if (led.z > maxes.z) maxes.z = led.z;    
  }
  
  c.x = (maxes.x - mins.x) / 2;
  c.y = (maxes.y - mins.y) / 2;
  c.z = (maxes.z - mins.z) / 2;
  
  return c;
}   

//////////////////////////////////// 
// UI Event (mouse and keyboard) event handlers.  Requires an initialized
// mover object, which you will have once you've created a PixelTeleporter
// object
//////////////////////////////////////
void keyReleased() {
  switch (key) {        
    case ' ': // stop automatic rotation
      g_mover.autoMove = !g_mover.autoMove;
      break;
    case 'r': //reset rotation
      g_mover.setRotation(0,0,0);
      g_mover.mouseRotation.x = 0;
      g_mover.mouseRotation.y = 0;
      break;
    default:
      break;
  }  
}

void mouseWheel(MouseEvent event) {
  g_mover.zoomCamera(30 * event.getCount());
}

void mousePressed(MouseEvent event) {
  g_mover.dragOriginX = mouseX;
  g_mover.dragOriginY = mouseY;
}

void mouseDragged(MouseEvent event) {
  if (event.getButton() == LEFT) {
    
// rotation based on distance from start of drag    
     g_mover.mouseRotation.x = (mouseX - g_mover.dragOriginX)/float(width / 2) * TWO_PI;
     g_mover.mouseRotation.y = (mouseY - g_mover.dragOriginY)/float(height / 2) * TWO_PI;        
  }
}
