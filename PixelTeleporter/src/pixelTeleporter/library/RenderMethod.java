package pixelTeleporter.library;

/**
 * Methods used to draw LED objects to the screen. Available methods are:
 * <p>
 * <li><strong>DEFAULT</strong>  - renders ScreenObj lists in 2D and ScreenShape lists in 3D.  Fast and simple.</li>
 * <li><strong>DRAW3D</strong>   - renders all objects in 3D space using Processing graphics API calls</li>
 * <li><strong>REALISTIC2D</strong> - uses Processing API calls to render realistic video-quality LED objects.
 * Looks great, but performance will vary depending on your computer and GPU.</li> 
 * <li><strong>FILE</strong> - records incoming LED data to a JSON file for later playback. Useful for making
 * movies and debugging.</li>
 * <li><strong>SHADER3D</strong> - NOT YET IMPLEMENTED - does nothing at the moment. (Uses OpenGL and GLSL to render
 * highly detailed objects in 3D space. Performance may vary greatly depending on your GPU.) </li>
 */
public enum RenderMethod {
	DEFAULT,
	DRAW3D,
	REALISTIC2D,
	FILE,
	SHADER3D
}
