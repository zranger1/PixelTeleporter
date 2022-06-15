package pixelTeleporter.library;

import java.net.URL;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import processing.core.*;
import processing.data.JSONArray;
import processing.opengl.PShader;

public class PTFileUtils {
	PixelTeleporter pt;
	PApplet pApp;
	
	PTFileUtils(PixelTeleporter p) {
	  pt = p;
	  pApp = pt.app;
	}
		
	// NOTE - shader names must include extension.  It saves work for lazy me!
	public PShader loadShader(String fragment, String vertex) {
	  String path = getLibPath();
	  return pApp.loadShader(Paths.get(path, "shaders", fragment).toString(),
                       		  Paths.get(path, "shaders", vertex).toString());
	}	
	
    private String getLibPath() {
        URL url = this.getClass().getResource(PixelTeleporter.class.getSimpleName() + ".class");
        if (url != null) {
            // Convert URL to string, taking care of spaces represented by the "%20"
            // string.
            String path = url.toString().replace("%20", " ");

            if (!path.contains(".jar"))
                return pApp.sketchPath();

            int n0 = path.indexOf('/');

            int n1 = -1;

            // read jar file name
            String fullJarPath = PixelTeleporter.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .getPath();

            if (PApplet.platform == PConstants.WINDOWS) {
                // remove leading slash in windows path
                fullJarPath = fullJarPath.substring(1);
            }

            String jar = Paths.get(fullJarPath).getFileName().toString();

            n1 = path.indexOf(jar);
            if (PApplet.platform == PConstants.WINDOWS) {
                // remove leading slash in windows path
                n0++;
            }

            if ((-1 < n0) && (-1 < n1)) {
                return path.substring(n0, n1);
            } else {
                return pApp.sketchPath();
            }
        }
        return pApp.sketchPath();
    }	
    
	/**
	 * Read a Pixelblaze compatible pixel map into a list of ScreenLED objects.
	 * @param fileName Name of file to write 
	 * @param scale Coordinate multiplier for scaling output 
	 * @return Linked list of ScreenLEDs corresponding to the pixel map if successful,
	 * null if unable to read the specified file.
	 */
	public LinkedList<ScreenLED> importPixelblazeMap(String fileName,float scale) {
		LinkedList<ScreenLED> obj = new LinkedList<ScreenLED>();
		JSONArray json = pApp.loadJSONArray(fileName);

		// read the map
		for (int i = 0; i < json.size(); i++) {
			float x,y,z;

			JSONArray mapEntry = json.getJSONArray(i);
			float [] coords = mapEntry.getFloatArray();  

			x = coords[0];
			y = coords[1];
			z = (coords.length == 3) ? z = coords[2] : 0;

			ScreenLED led = new ScreenLED(pt,scale * x,scale * y,scale * z);
			led.setIndex(i);
			obj.add(led);
		}

/*		
		// adjust to center the object at (0,0,0) in world space
		PVector center = pt.mover.findObjectCenter(obj);
		for (ScreenLED p : obj) {
			p.x -= center.x;
			p.y -= center.y;
			p.z -= center.z;			
		}
*/		
		return obj;
	}

	/**
	 * comparator for sorting.  Used by exportPixeblazeMap()
	 */	
	class compareLEDIndex implements Comparator<ScreenLED> {

		@Override
		public int compare(ScreenLED p1, ScreenLED p2) {
			return (p1.index < p2.index) ? -1 : 1;
		}
	}

	/**
	 * Convert a list of ScreenLEDs to a Pixelblaze compatible JSON pixel map and
	 * write it to the specified file.
	 * @param obj Linked list of ScreenLEDs representing a displayable object
	 * @param fileName Name of file to write 
	 * @param scale Coordinate multiplier for scaling final output 
	 * @param is3D true for 3D (xyz), false for 2D (xy) 
	 * @return true if successful, false otherwise
	 */
	public boolean exportPixelblazeMap(LinkedList<ScreenLED> obj,String fileName,float scale, boolean is3D) {
		JSONArray json,mapEntry;

		//sort object by pixel index for export. 
		@SuppressWarnings("unchecked")
		LinkedList<ScreenLED> sortedCopy = (LinkedList<ScreenLED>) obj.clone();
		Collections.sort(sortedCopy,new compareLEDIndex());

		json = new JSONArray();
		for (ScreenLED led : sortedCopy) {
			mapEntry = new JSONArray();
			mapEntry.append(scale * led.x);
			mapEntry.append(scale * led.y);
			if (is3D) mapEntry.append(scale * led.z);    

			json.append(mapEntry);
		}  
		return pApp.saveJSONArray(json,fileName);  
	}    	

}
