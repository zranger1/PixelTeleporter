package pixelTeleporter.library;

interface PowerEvaluator {
	// returns the power, in amps, used by an LED at the
	// input brightness level.
	public float evaluate(ScreenLED led);
}
