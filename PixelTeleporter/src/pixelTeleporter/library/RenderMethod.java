package pixelTeleporter.library;

/**
 * Methods used to draw LED objects to the screen. Available methods are:
 * <p>
 * <li><strong>DEFAULT</strong> - selects DRAW3D if possible</li>
 * <li><strong>DRAW3D</strong>  - renders all objects in 3D space using OpenGL</li>
 * <li><strong>USER</strong> - PixelTeleporter supplies transformed 3D coordinates to
 * the sketch's draw() method at frame time. The user is responsible for all rendering.</li>
 */
public enum RenderMethod {
	DEFAULT,
	DRAW3D,
	USER
}
