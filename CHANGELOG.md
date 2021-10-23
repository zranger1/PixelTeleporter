# PixelTeleporter Change Log

## Version 1.1.4 (7/03/2021) What's New
Basic support for 3D printed and other geometric solids. Added ScreenShape and ScreenShapeFactory APIs so you can model objects with LEDs mounted behind translucent diffuser panels.  For the moment, supports just one LED per face.  See example code and javadocs for more details!

## Version 1.1.3 (4/03/2021) What's New
Minor update - increased the maximum supported number of LEDs to 4096 per PixelTeleporter object, and 
updated all the servers (except the ESP8266 version) to match.   The ESP8266 version still supports up
to 2048 pixels.

## Version 1.1.2 (2/21/2021) What's New
Many Quality-of-life and UI improvements:
- You can now translate (pan) objects by right-dragging with the mouse.
- Object rotation with the mouse is generally smoother and more responsive.
- Holding down the alt key allows you to rotate your object around the Z axis.
- added setBackgroundImage() method to Pixelteleporter object. Supported image formats are: 
 (.gif, .jpg, .tga, and .png).  (If you use setBackgroundImage, you can remove calls to background() from your sketches.)
- Hold down the shift key while dragging to pan your background image
- Hold down the shift key with the mouse wheel to zoom your background image
- Connection status feedback!  When not receiving data from the pixelblaze, Pixelteleporter will now pulse between
 slowly between black and grey.  Display will resume when the connection is reestablished
- More connection status feedback! pbxTeleporter for Windows now displays connection status and LED count in its main window.

Also includes various small fixes and improvements to servers. 
The beta wiring label feature has been removed for now.

## Version 1.1.1 (10/21/2020) What's New
Support for multiple Pixelblazes. (See new DualMatrix example)
Added Fermat Spiral (Fibonacci256) example.
BETA: You can now use the "l" key while your sketch is running to toggle the display of wiring labels -- the index of each pixel. 
 
## Version 1.1.0 (10/08/2020) What's New
**Support for Raspberry Pi and FTDI USB-to-Serial boards on Linux & Windows**

You can now hook your Pixelblaze directly to your Raspberry Pi's serial input, or to your
Linux or Windows 10 computer through an FTDI USB-to-Serial converter.

The new server software, pbxTeleporter, uses very little CPU,so you can hook up your Pixelblaze and run your PixelTeleporter scripts
on the same machine!  Both wired and wireless networks are supported, and the servers support sending data to multiple clients. 

To use the new features, you will need to reinstall the Processing library. (You should also update your ESP8266 firmware, just to stay current.)

A a small change to the PixelTeleporter object creation API was necessary. When you create a PixelTeleporter object, you must now specify two port numbers:
- **serverPort** (default 8081) - the port number that the server software uses to listen for commands
- **clientPort** ( default 8082) - the port that the Processing script receives data on.

To simplify things, you can now create a PixelTeleporter object without specifying ports. The defaults ports will be used.  The examples are all set up this way now.

Existing scripts will not break. If you use the old constructor to specify only one port number, it is used for both client and server.
But you will not be able to have script and Pixelblaze on the same machine without adjusting your scripts. 

## Version 1.0.0 (9/24/2020) 
**PixelTeleporter is now a Processing library!**
See the new installation and usage instructions below.  Being a library makes many things easier going forward. 
Your sketch code will require a lot less boilerplate, the examples are easily accessible through the Processing UI,
and it will be way, way easier to add and improve features without breaking anybody's scripts.

This time though, exisiting scripts will need minor changes to work with the library version -- see the examples
and the [Javadoc](https://zranger1.github.io/PixelTeleporter/pixelTeleporter/library/package-summary.html) for details. 
The "old" version of PixelTeleporter is still available on the "archive" branch of the [git repository](https://github.com/zranger1/PixelTeleporter).

## Version 0.0.2 (9/7/2020) 
Added Pixelblaze pixel map import export. This let you build a displayable object directly from a
Pixelblaze compatible json map. See the new **MapIO** example for more information.

Also... minor bug fixes in viewport management and ongoing cosmetic touchup of examples.
