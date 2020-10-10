## pbxTeleporter for Raspberry Pi

## Serial Configuration
You will need to configure your Pi's serial ports so that a full UART is available via a /dev/tty device. 
The exact setup depends on which model of Raspberry Pi you're using.  A detailed explanation and 
instructions for how to do this are here:

[Raspberry Pi UART Configuration](https://www.raspberrypi.org/documentation/configuration/uart.md)

## Running pbxTeleporter
Once you've got your serial output set up, copy the [Pi/pbxTeleporter](./pbxTeleporter) directory to your home directory or
other convenient place.  Open a terminal window in the directory and type 

```make```

to build pbxTeleporter. Once the build is complete, you can start it by typing:

```./pbxTeleporter /dev/ttyAMA0```    (or whatever your serial device's name is)

Once pbxTeleporter is started, it will run 'till you press Ctrl-C,  close the terminal window, or
shut down your computer.  

### Wiring your Raspberry Pi
**Warning! DO NOT KILL YOUR PI!**  You **MUST** use a voltage divider or level shifter to convert your LED controller's
5v output voltage to the 3.3v safe for your Pi.  

A voltage divider works fine for this purpose and is easy to build -
it requires just two resistors and three short pieces of wire - for details, instructions and a resistor value
calculator, see these links: 

[Voltage Divider Tutorial](https://learn.sparkfun.com/tutorials/voltage-dividers/all)

[Voltage Divider Calculator](https://ohmslawcalculator.com/voltage-divider-calculator)

Disconnect both your Pixelblaze and your Raspberry Pi from all power.  Connect the Pixelblaze's data line to 
the Pi's RDX0 pin.  

You can power your Pixelblaze from the Pi by running power and ground lines from the Pi's 5v/GND pins to the
corresponding power/ground lines on the Pixelblaze.

### Pixelblaze Configuration
pbxTeleporter emulates a single 8 channel Pixelblaze output expander board.  To use it, go to the 
Pixelblaze settings page and set the LED type to "Pixelblaze Output Expander".  A channel assignment list will
display at the bottom of the page.  Scroll down to it and set up your LEDs.

Set LED type to  WS2812, in RGB order. (APA102 in RGB order also works).  Note that each channel supports 256 LEDs.  If you want more, just add another 
channel. PixelTeleporter supports a maximum of 2048 LEDs -- 8 channels of 256.  Be sure to press the Start Index "Auto" 
button to set the starting index for each channel if you use more than one.

Configure your mapping function if needed, and set your pattern.

## Known Issues
### Segmentation Fault using FTDI USB->Serial converter
The Pi and Linux pbxTeleporter servers are theoretically identical -- you should be able to hook an FTDI board to one of your Pi's USB ports and read
from your Pixelblaze over /dev/ttyUSB0.  On my hardware though, it'll initialize successfully, run for a short time,
then generate a Segmentation Fault and terminate.  I hope to track this down shortly, but for now, use the Pi's built in serial
hardware (/dev/tty/AMA0) instead.  


  
