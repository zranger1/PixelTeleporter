# PixelTeleporter
- **View** pixel output from your hardware LED controller as virtual 3D pixels on a computer monitor.  
- **Prototype** a virtual version of your next LED project so you can build, preview and debug the software before you build
the physical display.
- **Evaluate** performance, test and tune your LED patterns away from the hardware installation. 

This version of PixelTeleporter works exclusively with Pixelblaze ( https://www.bhencke.com/pixelblaze ).
A generic APA-102 compatible version that will work with most other LED controllers is currently in the works.

## Ok, but what *exactly* is PixelTeleporter?
I wrote this because I wanted to write software for a volumetric LED cube, but I didn't want to 
buy or build one just yet.  To generalize, I wanted a way to build virtual prototypes of LED
objects and test them with a real controller.

PixelTeleporter is a software toolkit - an Arduino IDE sketch file for ESP8266, and a set of classes for
the Processing 3 environment.  It lets you hook an ESP8266 microcontroller to the output of your LED
controller in place of an LED strip/panel/whatever.  The microcontroller forwards the LED pixel data over
WiFi as UDP datagrams to the computer running Processing.

The Processing classes make it simple to write scripts that receive the data, then arrange and render the pixels on your
computer.  With this toolset and the included the examples, you can quickly prototype almost any physical 
arrangement of LEDs.

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


## Setup - The Short Version
- Set the SSID and password for your network int PBExpansionTeleport.ino
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

## Setup in More Detail
### Preparing your ESP8266
Connect your ESP8266 board to your computer with a USB cable (or other serial connector). Use the Arduino
IDE to open ESP8266 sketch PBExpansionTeleporter.ino in the repository's ESP/PBExpansionTeleporter directory.
Near line 35 replace the _SSID and _PASS constants with your own network SSID and password, then save the sketch.  

Open the IDE's Serial Monitor (Tools/Serial Monitor), then compile and upload the sketch to your device.  If 
everything is working, you will see the message "Connected!", followed by your device's IP address.  Make note
of this address.  You'll need it for the Processing sketches.

### Wiring the Controller
**Warning!** Before performing this step, be sure your board is 5V compatible. If it isn't, you may need
to place a voltage divider circuit or a level shifter between your board and the LED controller. Or just
use a different board. There are endless variants available. (TODO - instructions for 5v -> 3.3v voltage divider.
Also ask Wizard what is voltage range of Pixelblaze's data out.  From schematic, looks 5v to me, but I don't have
a scope, so can't test.)

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

## Examples
Processing scripts for each example are in their own directory in the repository. To
view an example, just load it in Processing 3. To run it, replace the IP address in 
setup() with your own, check that your LED controller is configured correctly
and press Processing's "Run" button.

The following controls are available in all examples:
- mouse wheel - zoom in and out
- space bar - stop/start automatic rotation, if any
- mouse left drag - rotate object.

The included examples are:

### Matrix
All 2048 pixels in a 32x64 matrix.  Use with the default Pixelblaze mapper.
(Be sure that any line that references zigzag wiring is commented out.) 

### Ring
Simulated LED ring.  Adjustable radius and start angle.

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
  
