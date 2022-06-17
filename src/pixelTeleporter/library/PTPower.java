package pixelTeleporter.library;

import processing.core.*;
import java.util.LinkedList;

public class PTPower {
	PApplet pApp;
	PixelTeleporter pt;
	PowerEvaluator e;
	
	// statistics accumulator
	float nFrames;
	float current;
	float total;
	float maxPower;
		
	float idleConsumption = 0.001f;  // WS2812 - 1ma 
	float elementMaxPower = 0.06f;   // WS2812 - 60ma
	
	void setIdleConsumption(float n) { idleConsumption = n; }
	void setElementMaxPower(float n) { elementMaxPower = n; }
	void setPowerEvaluator(PowerEvaluator p) { e = p; }
		
	float getCurrent() { return current; } 
	float getMaximum() { return maxPower; }
	float getAverage() { return total / nFrames; }
	
	void reset() {
		nFrames = 0;
		current = 0;
		total = 0;
		maxPower = 0;				
	}
		
	PTPower(PixelTeleporter pt) {
		this.pt = pt;
		pApp = pt.app;
		reset();
				
		// default to RGB WS2812b - 1ma at idle, 60ma per full value color component.
		// we assume brightness/power linearity here, probably isn't really the case.
		e = new PowerEvaluator() {
				public float evaluate(ScreenLED led) {					
					int col = led.getColor();
					
					// red
					int n = (col >> 16) & 0xFF; 
					float amps = ((float) n / 255.0f) * 0.06f;
					
					// green
					n = (col >> 8) & 0xFF;
					amps += ((float) n / 255.0f) * 0.06f;
					
					// blue
					n = col & 0xFF;
					amps += ((float) n / 255.0f) * 0.06f;
					
					return PApplet.max(idleConsumption,amps);
				}
		};
	}
		
	// do nothing
	void evaluate(LinkedList <ScreenLED> obj) {
		float amps = 0;

		int n = obj.size();
		for (int i = 0; i < n; i++) {
			amps += e.evaluate(obj.get(i));
		}
		current = amps;
		total += amps;
		nFrames += 1f;
		if (amps > maxPower) maxPower = amps;
	}
}
