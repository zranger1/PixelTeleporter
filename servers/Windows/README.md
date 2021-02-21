## pbxTeleporter for Windows

## Wiring Setup
Be sure your FTDI adapter is configured for 5v logic -- this is usually set by a switch on the board. 

Disconnect both your Pixelblaze and your FTDI adapter from all power.  Connect the Pixelblaze's DAT line to 
the FTDI adapter's s RDX pin, and power your Pixelblaze by connecting the FTDI's VCC and GND lines to the
corresponding VIN/GND lines on the Pixelblaze.

Once the wires are connected, plug the FTDI adapter in to a USB port on your computer and watch the LEDs on the Pixelblaze
to make sure it powers up properly.  

## Running pbxTeleporter
Once you've got your Pixelblaze connected and your serial device set up, copy [Windows/pbxTeleporter.exe](./pbxTeleporter.exe) to a
convenient directory.  Start the program by clicking on pbxTeleporter.exe in Explorer.

pbxTeleporter for Windows is a Win32 GUI app -- it will present you with a normal Windows-ish menu of settings, for which it has
determined reasonable defaults. The only option you will probably need to check is the Serial Port setting, available
through the Settings menu.  Be sure that the selected serial port corresponds to your FTDI device.   

pbxTeleporter uses very little CPU and can safely run in the background while you're 
doing other things.  You can minimize it at any point.  To stop pbxTeleporter, close its window
or click the File/Exit menu item.

## Pixelblaze Configuration
pbxTeleporter emulates a single 8 channel Pixelblaze output expander board.  To use it, go to the 
Pixelblaze settings page and set the LED type to "Pixelblaze Output Expander".  A channel assignment list will
display at the bottom of the page.  Scroll down to it and set up your LEDs.

Set LED type to  WS2812, in RGB order. (APA102 in RGB order also works).  Note that each channel supports 256 LEDs.  If you want more, just add another 
channel. PixelTeleporter supports a maximum of 2048 LEDs -- 8 channels of 256.  Be sure to press the Start Index "Auto" 
button to set the starting index for each channel if you use more than one.

Once you're successfully set up and connected, pbxTeleporter window will display a "Connected" message,
along with the count of pixels it is currently receiving from the Pixelblaze.

Configure your mapping function if needed, and set your pattern.

## Notes 
If possible, plug the FTDI adapter directly to a USB port on the computer and **not**
through a hub.  If you must use a hub, first verify that pbxTeleporter works when directly 
connected to your computer, then test with the hub. USB hubs vary in design and capability and some
simply may not work.  

USB extension cables of a meter or so should pose no problems, but longer passive cables may cause power issues
and/or signal loss.  



  
