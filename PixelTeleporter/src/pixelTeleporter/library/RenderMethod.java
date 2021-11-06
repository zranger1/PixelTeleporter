package pixelTeleporter.library;

/**
 * Methods used to draw LED objects to the screen. Available methods are:
 * <p>
 * <li><strong>DEFAULT</strong> - chooses DRAW2D or DRAW3D depending on the z-axis size of the current object.</li>
 * <li><strong>DRAW2D</strong>  - renders ScreenObj lists in 2D and ScreenShape lists in 3D.  Fast and simple.</li>
 * <li><strong>DRAW3D</strong>  - renders all objects in 3D space using Processing graphics API calls</li>
 * <li><strong>HD2D</strong> - renders realistic video-quality 2D LED objects. Looks great, but performance
 *  will vary depending on your computer and GPU.  Will probably not look good with 3D objects.</li>
 * <li><strong>HD3D</strong> - NOT YET IMPLEMENTED. Uses OpenGL to render highly detailed 3D objects with
 * reasonably correct lighting. Performance will vary greatly depending on your GPU.  A mid-level gaming
 * GPU is pretty much a minimum requirement. </li>  
 * <li><strong>USER</strong> - NOT YET IMPLEMENTED. PixelTeleporter hands transformed 3D coordinates to the user sketch. The
 * user is responsible for all drawing.</li>
 */
public enum RenderMethod {
	DEFAULT,
	DRAW2D,
	DRAW3D,
	HD2D,
	HD3D,
	USER
}
