package pixelTeleporter.library;
import java.net.URL;
import java.nio.file.Paths;
import java.util.LinkedList;
import processing.core.*;
import processing.opengl.PShader;

public class PTUtils {
	// NOTE - shader names must include extension.  It saves work for ME!
	public static PShader loadShader(PApplet pApp, String fragment, String vertex) {
	  String path = getLibPath(pApp);
	  System.out.println(path);
	  return pApp.loadShader(Paths.get(path, "shaders", fragment).toString(),
                       		  Paths.get(path, "shaders", vertex).toString());
	}	
	
    private static String getLibPath(PApplet pApp) {
        URL url = pApp.getClass().getResource(PixelTeleporter.class.getSimpleName() + ".class");
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
	

		
	
	

}
