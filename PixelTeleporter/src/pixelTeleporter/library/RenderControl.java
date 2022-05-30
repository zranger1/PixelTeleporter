/**
 * 
 */
package pixelTeleporter.library;

/**
 * Set configurable parameters for individual renderers.  Not all
 * renderers support all controls. <p>
 * RESET - set all renderer controls to their default values
 * AMBIENT_LIGHT - (0 - 255) the amount of ambient light the "camera" receives
 * BGCOLOR - RGB color of surface behind emitter
 * BGALPHA - (0 - 255) opacity of surface behind emitter
 * LEDMODEL_BULB - draw emitter as a capsule shaped, bulb-type LED
 * LEDMODEL_SMD - draw emitter as a square, surface mounted LED 
 * FALLOFF - (0 - 10) how far light from LEDs travels in the scene
 * INDIRECT_INTENSITY - (0.0 - 1.0) light level from sides of emitter
 * OVEREXPOSURE - (0.0 - 1000) simulates CCD camera bloom
 * GAMMA - (0.0 - 2) adjust displayed gamma to better match LED colors
 */
public enum RenderControl {
	RESET,
	WEIGHT,
	AMBIENT_LIGHT,
	FALLOFF,
	BGCOLOR,
	BGALPHA,
    LEDMODEL_BULB,
    LEDMODEL_SMD,
    INDIRECT_INTENSITY,
    OVEREXPOSURE,
    GAMMA
}
