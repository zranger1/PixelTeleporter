// Pixel Teleporter solid pentagonal icositetrahedron example 
// Illustrates the use of the ScreenShape class to build solid objects
// where the LED is mounted behind a diffuser and illuminates a whole
// face.
//
// Works with the mapping function "pentagonal icositetrahedron.js" in 
// the examples/MappingFunctions directory/
//
// 7/02/2021 JEM (ZRanger1)
import pixelTeleporter.library.*;
import java.util.*;

// global variables
PixelTeleporter pt;        
LinkedList<ScreenLED> obj;    // list of LEDs in our object

// the 38 vertices of "unit" pentagonal icositetrahedron centered at origin
float[][] pointArray = {
  {0.3721032, 0.2379147, 0.9281914 },
  {-0.05980191, 0.4375933, 0.9281914 },
  {-0.4222941, 0.1293516, 0.9281914  },
  {-0.2946239, -0.3290302, 0.9281914 },
  {0.3721032, -0.4375933, 0.9281914  },
  {0.8541992, -0.3290302, 0.467639   },
  {0.9465147, 0.1293516, 0.3794488   },
  {0.6844044, 0.4375933, 0.6298461   },
  {0.5146097, 0.8048595, 0.3794488   },
  {-0.1310552, 0.9589804, 0.5046475  },
  {-0.7116927, 0.6372719, 0.3794488  },
  {-0.77672, 0.2379147, 0.6298461    },
  {-1.017768, -0.2834724, 0.2743713  },
  {-0.5418979, -0.6051809, 0.6298461 },
  {-0.2797876, -0.9134226, 0.3794488 },
  {0.1874721, -0.8959751, 0.467639   },
  {0.4644187, -0.9134226, 0.08110353 },
  {0.8269109, -0.6051809, 0.08110353 },
  {0.8963238, -0.3785687, -0.3315008 },
  {1.017768, 0.2834724, -0.2743713   },
  {0.6342135, 0.8048595, -0.08110353 },
  {0.2797876, 0.9134226, -0.3794488  },
  {-0.1372811, 1.004538, -0.1692937  },
  {-0.5146097, 0.8048595, -0.3794488 },
  {-0.8269109, 0.6051809, -0.08110353},
  {-0.9886393, 0.1788901, -0.2172418 },
  {-0.7538173, -0.6642055, -0.2172418},
  {-0.3950058, -0.9455136, -0.08110353},
  {0.1310552, -0.9589804, -0.5046475 },
  {0.5920889, -0.4966178, -0.6777941 },
  {0.4222941, -0.1293516, -0.9281914 },
  {0.5840225, 0.2969392, -0.7920531  },
  {0.2524994, 0.6372719, -0.7659843  },
  {-0.3721032, 0.4375933, -0.9281914 },
  {-0.8120746, 0.02078851, -0.6298461},
  {-0.6844044, -0.4375933, -0.6298461},
  {-0.2673357, -0.5287088, -0.8400012},
  {-0.04496558, -0.1467991, -1.016382}
  };
  
// vertex indices used to construct the 24 faces  
int[][] faceArray =  {
  {4,0,1,2,3},
  {0,4,5,6,7},
  {1,0,7,8,9},
  {2,1,9,10,11},
  {3,2,11,12,13},
  {4,3,13,14,15},
  {5,4,15,16,17},
  {6,5,17,18,19},
  {7,6,19,20,8},
  {9,8,20,21,22},
  {10,9,22,23,24},
  {11,10,24,25,12},
  {13,12,26,27,14},
  {15,14,27,28,16},
  {17,16,28,29,18},
  {19,18,29,30,31},
  {20,19,31,32,21},
  {22,21,32,33,23},
  {24,23,33,34,25},
  {12,25,34,35,26},
  {27,26,35,36,28},
  {29,28,36,37,30},
  {31,30,37,33,32},
  {34,33,37,36,35}  
};

// Construct the faces, link each face to an LED and assemble them
// into our polyhedron.
LinkedList<ScreenLED> buildIcositetrahedron(float scale) {
  int face,i;
  PShape s;
  
  obj = new LinkedList<ScreenLED>();
  
  for (face = 0; face < 24;face++) {
    s = createShape();
    s.beginShape();
    for (i = 0; i < 5; i++) {      
      s.stroke(0);
      s.vertex(scale * pointArray[faceArray[face][i]][0],
             scale * pointArray[faceArray[face][i]][1],
             scale * pointArray[faceArray[face][i]][2]);

    }
    s.endShape();
    
    // ScreenShapeFactory takes an optional opacity argument if you'd like
    // model your object as partly transparent.  255 is fully opaque, 0 fully
    // transparent.
    ScreenLED led = pt.ScreenShapeFactory(s,176);
    led.setIndex(face);
    obj.add(led);
   
  }
  return obj;
}

void setup() {
  // configure display and select 3D renderer
  size(1000,1000,P3D);     
   
  // create PixelTeleporter object. Use your server's IP address here 
  pt = new PixelTeleporter(this,"127.0.0.1");  
 
  // build solid
  obj = buildIcositetrahedron(400);
  
  // add slow rotation to enhance depth.  Spacebar toggles
  // rotation on/off, mouse wheel zooms, 'r' resets to original orientation.
  pt.setRotation(0,0,0);
  pt.setRotationRate(0,PI / 5000, 0);  
  
  // initialize PixelTeleporter and start listener thread
  pt.start();
}

void draw() { 
  background(30);

  // draw our object, coloring faces with the most recently received
  // pixel data.
  pt.draw(obj);       
}
