# PixelTeleporter
- **Display:** View the output from your hardware LED controller as virtual 3D pixels on a computer monitor.  
- **Prototype:** Build a virtual version of your next LED project so you can have software written and debugged before you build
the physical display.
- **Evaluate:** Test, tune and update your pattern software at your bench, away from the hardware installation. 

This version of PixelTeleporter works exclusively with the [Pixelblaze controller](https://www.bhencke.com/pixelblaze).

## Ok, but what *exactly* is PixelTeleporter?
PixelTeleporter is a software toolkit - a set of server programs for various platforms, and a library for the Processing 3 environment. It lets you
connect the output of your LED controller to a server device in place of an LED strip/panel/whatever.

The server then forwards the pixel data across your LAN to the computer running Processing and displays your LED output.

The Processing library make it simple to write sketches to receive the data and draw the pixels in 3D on your computer.
With this toolset and the included  examples, you can quickly prototype almost any physical arrangement of LEDs.

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

## Requirements
A **server device**. These are the currently supported server platforms:
- **[Raspberry Pi](./servers/Pi)** For Raspberry Pi, running Raspbian or other Linux
- **[Linux](./servers/Linux)**, For most common Linux distros -- tested on Debian 10 and Ubuntu 18.04.
- **[Windows](./servers/Windows)** For Windows 7 - 10.
- **[ESP8266 microcontroller](./servers/ESP8266)** - I used a NodeMCU 1.0 board for development, but there are many suitable boards.

**An FTDI USB->Serial adapter** - you'll need one of these for the Linux and Windows servers.  (I highly recommend adding one to your toolbox in any case 
if you don't own one already. Inexpensive and useful for any number of programming and debugging tasks.)

**[Processing 3](https://www.processing.org/download/)** - Processing is a very easy to use Java based graphics and prototyping environment.  If you 
haven't used it before, you're in for a treat.  

Basic familiarity with the hardware and a little coding skill are helpful. But if you're new to this, no worries.
Setup is pretty by-the-numbers, and there's really nothing better than the instant feedback of the Pixelblaze/Processing combo
to help you quickly improve your coding and graphics skills.

You'll also need a computer with reasonably modern OpenGL compatible graphics hardware, running any OS that Processing 3 supports.  Faster is better.  

## Installing the Library into Processing
To install, you will need to [download PixelTeleporter.zip](https://github.com/zranger1/PixelTeleporter/releases/tag/1.1.0) and
manually copy it to the ```libraries``` folder of your Processing sketchbook.

To find the Processing sketchbook on your computer, open the Preferences window from the Processing application (PDE) and look 
for the "Sketchbook location" item at the top.

By default the following locations are used for your sketchbook folder: 
  * For Mac users, the sketchbook folder is located in `~/Documents/Processing` 
  * For Windows users, the sketchbook folder is located in `My Documents/Processing`

Download the latest release - [PixelTeleporter.zip](https://github.com/zranger1/PixelTeleporter/releases/tag/1.1.0) from this repository.

Unzip and copy the PixelTeleporter folder into the `libraries` folder in the Processing sketchbook.
If `libraries` does not exist, (this is unlikely, but possible) you will need to create it.

The folder structure should look like when you're done:

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

## Connecting your Pixelblaze
See the README.md file in the servers directory for information on how to set up a device as
a PixelTeleporter server (pbxTeleporter) and connect it to your Pixelblaze.  

Source code and project files for the various server versions lives in its own repository at:

https://github.com/zranger1/pbxTeleporterServers

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
All 2048 pixels in a 32x64 matrix.  Use with the default Pixelblaze matrix mapper.
(Be sure to set width in the mapping function and comment out any line that
references zigzag wiring.) 

### Ring
Simulated LED ring.  Adjustable radius and start and stop angles.  The Pixelblaze
ring mapper is optional.

### Walled Cube
600 pixel (10x10) walled cube.  Use with the walled cube mapping function from
the example's PixelMap tab or from the repository's
[MappingFunctions](https://github.com/zranger1/PixelTeleporter/tree/master/PixelTeleporter/examples/MappingFunctions) .

### Volumetric Cube
1000 pixel (10x10x10) volumetric cube. Use with either this
mapping function in the example's PixelMap tab or with the default Pixelblaze mapper.
(If you use the Pixelblaze's mapper, be sure that any line that
references zigzag wiring is commented out.)

### Cylinder
Wraps a 2D panel around a cylinder (Or you can think of it as a
stack of rings.)  Use the Pixelblaze matrix mapper.   Alternately,
you can configure the cylinder as a spiral and just run it 
1D.  See the example code for details.

### Sphere
Maps LEDs evenly over the surface of a sphere. To use the sphere as a 2D surface, use the sphere mapper
in the example's PixelMap tab or the repository's
[MappingFunctions](https://github.com/zranger1/PixelTeleporter/tree/master/PixelTeleporter/examples/MappingFunctions) .

### Fireworks2020
Emulation of JeffV's Fireworks2020 setup from the Pixelblaze forums. See the video
of his actual setup at  https://youtu.be/zgF3DJoTAWI.  The pattern is available under
the topic "Fireworks for the 4th" in the Pixelblaze forums.

### MapIO
Reads a Pixelblaze JSON pixel map (in this case, the walled cube) from a file and
uses it to build a displayable object.  You can get these maps by running your
Pixelblaze mapping function in a Javascript interpreter and capturing the output,
or as I did, you can use your browser's debug console to capture the map directly
from the Pixelblaze web UI by temporarily adding `console.debug(map)` to the mapping function
right before it returns the map. 

# Building the PixelTeleporter Library 
The Processing library portion of PixelTeleporter is built using the Eclipse IDE for Java, version 2019-6.  To build it, clone the
[repository](https://github.com/zranger1/PixelTeleporter) ,and import it as an Eclipse project.  Check that
 the build and library settings in Eclipse work on your machine,
build the project and go! 


  
