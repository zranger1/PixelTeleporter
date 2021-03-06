# pbxTeleporter for ESP8266

## Setup - The Short Version
To build the ESP8266 version of pbxTeleporter, you'll need the latest **Arduino IDE**, which can be downloaded from here:

[Arduino IDE](https://www.arduino.cc/en/Main/Software)

Once you've installed the Arduino IDE, use the "Boards Manager..." menu item to 
install the ESP8266 board support package.  Then, use the IDE's "Tools" menu, configure
board options as follows:
- Board: "NodeMCU 1.0 (ESP-12E Module) - this is a reasonable default. If you have a 
different board and it's available in the options, by all means, use it.
- CPU Frequency: 160Mhz - probably the most important option.  Pixel Teleporter may not work
correctly if you leave this set to the default 80Mhz.
- IwIP variant: "v2 Higher Bandwidth"
- Upload Speed: "115200"

Once board options are configured, in the pbxTeleporter sketch, set the correct SSID and 
password for your network. When this is done:
- Connect Pixelblaze's Data line to GPIO13 (D7) on the ESP.  Power both devices appropriately, making
sure that they share a common ground.
- Compile and upload the sketch with the serial monitor open. Take note of the ESP's IP address.
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
pbxTeleporter emulates a single 8 channel Pixelblaze output expander board.  To use it, go to the 
Pixelblaze settings page and set the LED type to "Pixelblaze Output Expander".  A channel assignment list will
display at the bottom of the page.  Scroll down to it and set up your LEDs.

Set LED type to  WS2812, in RGB order. (APA102 in RGB order also works).  pbxTeleporter does not impose a limit on the
number of LEDs per channel, although the Pixelblaze Web UI may. If you run into the per-channel limit and need to add
more LEDs, just add another channel. At this time, the ESP8266 version of pbxTeleporter supports
a maximum of 2048 LEDs. 

Once you've set your per-channel LED counts, be sure to press the Start Index "Auto" button to properly set the starting index
for each channel.

When you're successfully set up and connected, pbxTeleporter window will display a "Connected" message,
along with the count of pixels it is currently receiving from the Pixelblaze.

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


  
