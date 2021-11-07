/**
 * 
 */
package pixelTeleporter.library;

/**
 * Set configurable parameters for individual renderers.  Not all
 * renderers support all controls. <p>
 * RESET - set all renderer controls to their default values
 * EXPOSURE - (0 - 255) the amount of light the "camera" receives
 * FALLOFF - (0 - 10) how far light from LEDs travels in the scene
 * 
 */
public enum RenderControl {
	RESET,
	EXPOSURE,
	FALLOFF,
	BGCOLOR,
	BGALPHA
}
