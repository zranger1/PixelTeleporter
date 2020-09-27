# PixelTeleporter
- **Display:** View the output from your hardware LED controller as virtual 3D pixels on a computer monitor.  
- **Prototype:** Build a virtual version of your next LED project so you can have software written and debugged before you build
the physical display.
- **Evaluate:** Test, tune and update your pattern software at your bench, away from the hardware installation. 

This version of PixelTeleporter works exclusively with the [Pixelblaze controller](https://www.bhencke.com/pixelblaze).

A generic APA-102 compatible version that will work with most other LED controllers is currently in the works.

## Ok, but what *exactly* is PixelTeleporter?
PixelTeleporter is a software toolkit - an Arduino IDE sketch file for ESP8266, and a library for
the Processing 3 environment.  It lets you hook a microcontroller (eventually other server devices as well)
to the output of your LED controller in place of an LED strip/panel/whatever.  The server then forwards the
LED pixel data from the hardware controller over WiFi as UDP datagrams to the computer running Processing and displaying your LED output.

The Processing library make it simple to write sketches that receive the data, then arrange and render the pixels on your
computer.  With this toolset and the included the examples, you can quickly prototype almost any physical 
arrangement of LEDs.

## Version 1.0.0 (9/24/2020) What's New
**PixelTeleporter is now a Processing library!**
See the new installation and usage instructions below.  Being a library makes many things easier going forward. 
Your sketch code will require a lot less boilerplate, the examples are easily accessible through the Processing UI,
and it will be way, way easier to add and improve features without breaking anybody's scripts.

This time though, exisiting scripts will need minor changes to work with the library version -- see the examples and the Javadoc for details. 
The "old" version of PixelTeleporter is still available on the "archive" branch of the [git repository](https://github.com/zranger1/PixelTeleporter).

## Version 0.0.2 (9/7/2020) What's New
Added Pixelblaze pixel map import export. This let you build a displayable object directly from a
Pixelblaze compatible json map. See the new **MapIO** example and Class Guide for more information.

Also... minor bug fixes in viewport management and ongoing cosmetic touchup of examples.

## Requirements
An **ESP8266 microcontroller**.  I used a NodeMCU board -- one of these:
  https://www.amazon.com/HiLetgo-Internet-Development-Wireless-Micropython/dp/B081CSJV2V/
  
The latest **Arduino IDE**, which can be downloaded from here:
https://www.arduino.cc/en/Main/Software

Once you've installed the Arduino IDE, use the "Boards Manager..." menu item to 
install the ESP8266 board support package.  

**Processing 3**, available at:
https://www.processing.org/download/
Processing is a very easy to use Java based graphics and prototyping environment.  If you 
haven't used it before, you're in for a treat.  

Basic familiarity with the hardware and software involved is helpful, as is some coding
skill.  If you're new to this, no worries.  The ESP8266 setup is pretty by-the-numbers, and
there's really nothing better than the instant feedback of the Pixelblaze/Processing combo to
improve your coding and graphics skills.

You'll also need a ***computer*** with reasonably modern OpenGL compatible graphics hardware, running
any OS that Processing 3 supports.  Faster is better.  

## Installing the Library into Processing
To install, you will need to [download the library](https://github.com/zranger1/PixelTeleporter/releases/tag/v1.0.0) and
manually copy it to the ```libraries``` folder of your Processing sketchbook.
To find the Processing sketchbook on your computer, open the Preferences window from the Processing application (PDE) and look 
for the "Sketchbook location" item at the top.

By default the following locations are used for your sketchbook folder: 
  * For Mac users, the sketchbook folder is located in `~/Documents/Processing` 
  * For Windows users, the sketchbook folder is located in `My Documents/Processing`

Download the latest release - [PixelTeleporter.zip](https://github.com/zranger1/PixelTeleporter/releases/tag/v1.0.0) from this repository.

Unzip and copy the PixelTeleporter folder into the `libraries` folder in the Processing sketchbook.
If `libraries` does not exist, (this is unlikely, but possible) you will need to create it.

The folder structure should look like this:

```
Processing
  libraries
    PixelTeleporter
      examples
      library
        PixelTeleporter.jar
      reference
      src
```
             
After installing PixelTeleporter, restart Processing.

## Hardware Setup ESP8266 - The Short Version
- Set the SSID and password for your network in servers/ESP8266/pbxTeleporter.ino
- Set the serial upload rate to 115000 baud
- Compile and upload the sketch with the serial monitor open.  Take note of the ESP's IP address.
- Connect Pixelblaze's Data line to GPIO13 (D7) on the ESP.  Power both devices appropriately.
- The Pixelblaze version of PixelTeleporter emulates an 8 channel expansion board, so set LED type
on the Pixelblaze to "Pixelblaze Output Expander".  Set your output channels up as necessary for 
WS2812 w/RGB order. Don't forget to set the start index for each channel you're using. For now,
PixelTeleporter acts as a single expansion board supporting a maximum of 2048 pixels.
- To run one of the processing examples, replace the IP address in the processing sketch with your
ESP board's IP, set up your Pixelblaze appropriately, and go!  

The examples will give you a good start towards building your own virtual LED objects.

## Hardware Setup for ESP8266 in More Detail
### Preparing your ESP8266
Connect your ESP8266 board to your computer with a USB cable (or other serial connector). Use the Arduino
IDE to open ESP8266 sketch pbxTeleporter.ino in the repository's servers/ESP8266/pbxTeleporter directory.
Near line 35 replace the _SSID and _PASS constants with your own network SSID and password, then save the sketch.  

Open the IDE's Serial Monitor (Tools/Serial Monitor), then compile and upload the sketch to your device.  If 
everything is working, you will see the message "Connected!", followed by your device's IP address.  Make note
of this address.  You'll need it for the Processing sketches.

### Wiring the Controller
**Warning!** Before performing this step, be sure your board is 5V compatible. If it isn't, you may need
to place a voltage divider circuit or a level shifter between your board and the LED controller. Or just
use a different board. There are endless variants available. 

Disconnect both your Pixelblaze and your ESP8266 from all power.  Connect the Pixelblaze's data line to 
the ESP's GPIO13 pin. That's arduino pin #13, or D7 on a NodeMCU board. Pin numbering may be different 
on different boards, so check yours carefully. 

If you wish to do so, and know your power supply can handle it, you can power the Pixelblaze from the 
ESP8266 by running power and ground lines from the ESP's 3.3v/GND pins to the corresponding power/ground
lines on the Pixelblaze.  I use this setup, powering the whole thing with a phone charger and USB cable
plugged into the ESP's USB port.

### Pixelblaze Configuration
PBExpansionTeleport.ino emulates a single 8 channel Pixelblaze output expander board.  To use it, go to the 
Pixelblaze settings page and set the LED type to "Pixelblaze Output Expander".  A channel assignment list will
display at the bottom of the page.  Scroll down to it and set up your LEDs.  Type should be WS2812, in RGB order.  
(APA102 in RGB order also works).  Note that each channel supports 256 LEDs.  If you want more, just add another 
channel. PixelTeleporter supports a maximum of 2048 LEDs -- 8 channels of 256.  Be sure to press the Start Index "Auto" 
button to set the starting index for each channel if you use more than one.

Configure your mapping function if needed, and set your pattern.

## Cautionary Notes
- Pin labels on ESP8266 boards may vary, so check your board specs and triple check
those pin labels when you're wiring things together.

- Although PixelTeleporter doesn't transmit a huge amount of data by modern
networking standards, it does create a lot of UDP packet traffic.  For most networks, this isn't a 
problem, but if you're in an environment where you think it might be, give other users
a heads-up, run at off hours, or move to a different net segment.  Note that PixelTeleporter only
sends data on request.  If you're not running a program that asks for pixels, it creates no network
traffic.

# Programming with PixelTeleporter:  An Introduction 
PixelTeleporter is designed to get you up and displaying pixels with as little code overhead
as possible. See the examples for complete implementations and handy tools. To use PixelTeleporter in
a sketch first, include the library.  (Typically, you'll need to include java.util as well.)

```
import pixelTeleporter.library.*;
import java.util.*;
```

Next, you'll need to create a PixelTeleporter object and set up a list of pixels with which to build
your LED object.  Like this:

```
    PixelTeleporter pt;
    LinkedList<ScreenLED> panel;  
```

In your sketch's setup() function, initialize your objects (using your own PixelTeleporter
transmitter's IP address, of course).

``` 
    void setup() {
    size(1000,1000,P3D);     // Set up the Processing window. 
                 
// initialize PixelTeleporter object    
      pt = new PixelTeleporter("192.168.1.42",8081);  
      pt.setElementSize(20);  // set display size of virtual LEDs
      
      pt.start();             // start the network thread. Request and listen for frames.
``` 
Now, you can build your own LED object. This is actually the fun, interesting part.  It amounts to writing
code to determine where each LED will be in 3D space, calling `pt.ScreenLEDFactory()` to create the LED, and
adding the LED to the LinkedList you created earlier.  See the examples for details on exactly how to do this.

```       
// build an LED object 
     panel = buildAnInterestingLEDObject(); // left as an exercise for reader!
    }
```

Now, you're done with setup. PixelTeleporter allows your draw function to be very simple. 

```
    void draw() {
    background(30);  // fill background with dark grey

    // render your LED object
    pt.render3D(panel);
   }  
```

That's it!  All you need for a basic PixelTeleporter sketch.  See the examples for
complete, working sketches, and the [Javadocs](https://zranger1.github.io/PixelTeleporter/pixelTeleporter/library/package-summary.html) in
the [git repository](https://github.com/zranger1/PixelTeleporter) (or in reference subdir of your installation directory)
for more technical information.

## Examples
After installation, examples can be accessed through the File/Examples menu item in the main Processing window.
Click on "PixelTeleporter" in the "Java Examples" dialog to see a list of available examples.  To load example,
double click on it.  To run it, replace the IP address in setup() with your own, check that your LED controller
is configured correctly and press Processing's "Run" button.

The following controls are available in all examples:
- mouse wheel:  zoom in and out
- mouse left drag:  rotate object.
- space bar:  Stop/start automatic rotation, if any
- 'r' key: Reset rotation.

The included examples are:

### Matrix
All 2048 pixels in a 32x64 matrix.  Use with the default Pixelblaze mapper.
(Be sure that any line that references zigzag wiring is commented out.) 

### Ring
Simulated LED ring.  Adjustable radius and start and stop angles.

### Walled Cube
600 pixel (10x10) walled cube.  Use with the walled cube mapping function from
this repository's MappingFunctions folder.

### Volumetric Cube
1000 pixel (10x10x10) volumetric cube. Use with either this
repository's mapping function or the default Pixelblaze mapper.
(If you use the Pixelblaze's mapper, be sure that any line that
references zigzag wiring is commented out.)

### Cylinder
Wraps a 2D panel around a cylinder (Or you can think of it as a
stack of rings.)  Use the Pixelblaze matrix mapper.   Alternately,
you can configure the cylinder as a spiral and just run it 
1D.  See the example code for details.

### Sphere
Maps LEDs evenly over the surface of a sphere.  Use the sphere mapper
in the repository to use the sphere as a 2D surface. 

### Fireworks2020
Emulation of JeffV's Fireworks2020 setup from the Pixelblaze forums. See the video
of his actual setup at  https://youtu.be/zgF3DJoTAWI.  The pattern is available under
the topic "Fireworks for the 4th" in the Pixelblaze forums.

### MapIO
Reads a Pixelblaze JSON pixel map (in this case, the walled cube) from a file and
uses it to build a displayable object.  You can get these maps by running your
Pixelblaze mapping function in a Javascript interpreter and capturing the output,
or as I did, you can use your browser's debug console to capture the map directly
from the Pixelblaze web UI by temporarily adding "console.debug(map) to the mapping function
right before it returns the map. 

# Building the PixelTeleporter Library 
The Processing library portion of PixelTeleporter is built using the Eclipse IDE for Java, version 2019-6.  To build it, clone the
[repository](https://github.com/zranger1/PixelTeleporter) ,and open it as an Eclipse project.  Check that
 the build and library settings in Eclipse work on your machine,
build the project and go! 


  
