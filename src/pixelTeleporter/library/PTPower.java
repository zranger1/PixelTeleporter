package pixelTeleporter.library;

import processing.core.*;



import java.util.LinkedList;

/**
 * PTPower class - handles power measurement for Pixelteleporter
 * @author ZRanger1
 * 
 * Power calculations are all in amps, because the main use case is power supply
 * and battery sizing, and current output (at the correct voltage for your LEDs) is
 * the main Thing You Need to Know.
 * 
 * Results
 *
 */
public class PTPower {
	PApplet pApp;
	PixelTeleporter pt;
	PowerEvaluator e;
	
	// statistics accumulator
	float nFrames;
	float current;
	float total;
	float maxPower;
		
	// current data per LED
	float idleCurrent;   
	float perElementMax;
	
	void setIdleConsumption(float n) { idleCurrent = n; }
	void setElementMaxPower(float n) { perElementMax = n; }
	void setPowerEvaluator(PowerEvaluator p) { e = p; }
	void setPowerModel(PowerModel p) {
		switch(p) {
		case RGB:
			setPowerEvaluator(new EvalRGB());
			break;
		case RGBW:
			setPowerEvaluator(new EvalRGBW());
			break;
		case CONSTANT_CURRENT:
			setPowerEvaluator(new EvalConstantCurrent());
			break;
		default:
			break;
		}
	}
		
	float getCurrent() { return current; } 
	float getMaximum() { return maxPower; }
	float getAverage() { return total / nFrames; }
	
	void reset() {
		nFrames = 0;
		current = 0;
		total = 0;
		maxPower = 0;				
	}
	
	void setLEDType() {
	   ;
	}
	
	/**
	 * Conservative profile for 5v WS282B-type LEDs
	 */
	void Profile_WS2812() {
		setIdleConsumption(0.001f);
		setElementMaxPower(0.020f);			
		setPowerModel(PowerModel.RGB); 
	}
	
	/**
	 * Profile for WS2812-ECO and similar 
	 * Probably more realistic for all WS2812s than the 
	 * conservative profile
	 */
	void Profile_WS2812ECO() {

		setIdleConsumption(0.001f);
		setElementMaxPower(0.0175f);				
		setPowerModel(PowerModel.RGB); 
	}	
	
	/**
	 * Profile for RGB (3-element) WS6812
	 */
	void Profile_WS6812() {
		setIdleConsumption(0.001f);
		setElementMaxPower(0.016f);				
		setPowerModel(PowerModel.RGB); 
	}		
	
	/**
	 * Profile for  5v RGBW and other 4-element SK6812
	 */
	void Profile_SK6812RGBW() {
		setIdleConsumption(0.001f);
		setElementMaxPower(0.016f);
		setPowerModel(PowerModel.RGBW); 		
	}
	
	void Profile_APA102() {
		setIdleConsumption(0.001f);
		setElementMaxPower(0.016f);
		setPowerModel(PowerModel.RGB); 
	}	
	
	// WS2815 - 12v 
	void Profile_WS2815() {
		setIdleConsumption(0.0027f);
		setElementMaxPower(0.014f);
		setPowerModel(PowerModel.CONSTANT_CURRENT); 		
	}	
	
	// GS8208 - 12v 
	void Profile_GS8208() {

		setIdleConsumption(0.0027f);
		setElementMaxPower(0.017f);
		setPowerModel(PowerModel.CONSTANT_CURRENT);					
	}		
				
	PTPower(PixelTeleporter pt) {
		this.pt = pt;
		pApp = pt.app;
		reset();
				
		// default to RGB WS2812b @ 5V
        Profile_WS2812();		
	}
		
	// Calculate power used by LEDs using current model and settings.
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
	
	class EvalRGB implements PowerEvaluator {
		public float evaluate(ScreenLED led) {					
			int col = led.getColor();
			
			// convert color to normalized per-element brightness
			float amps = (float) (((col >> 16) & 0xFF)) / 255.0f;
			amps += (float) (((col >> 8) & 0xFF)) / 255.0f;
			amps += (float) ((col & 0xFF)) / 255.0f;			
			
			return PApplet.max(idleCurrent,amps*perElementMax);
		}			
	}
	
	class EvalRGBW implements PowerEvaluator {
		
		public float evaluate(ScreenLED led) { 
			float w,briMax,briMin;

			// convert to normalized per-element brightness
			int col = led.getColor();			
			float r = (float) (((col >> 16) & 0xFF)) / 255.0f;
			float g = (float) (((col >> 8) & 0xFF)) / 255.0f;
			float b = (float) ((col & 0xFF)) / 255.0f;

			// whiteness is inversely related to the range between min and max, so...     
			// find brightest component
			briMax = Math.max(r, Math.max(g, b));

			// early out if LED is off
			if (briMax == 0) return idleCurrent;

			// find dimmest component
			briMin = Math.min(r,Math.min(g, b));

			// calculate and normalize the range 
			w = 1-((briMax - briMin) / briMax);
			w = w * briMax;

			// calculate and return overall power consumption
			return perElementMax *((r+g+b) -(2 *w));
		} 					
	}
	
	// this is... weird.
	class EvalConstantCurrent implements PowerEvaluator {
		public float evaluate(ScreenLED led) {					
			int col = led.getColor();
			
			// convert color to normalized per-element brightness
			float r = (float) (((col >> 16) & 0xFF)) / 255.0f;
			float g = (float) (((col >> 8) & 0xFF)) / 255.0f;
			float b = (float) ((col & 0xFF)) / 255.0f;			
			
			// constant current model -- same current is drawn regardless of color
			return PApplet.max(idleCurrent,3 * PApplet.max(r,PApplet.max(g,b))*perElementMax);
		}			
	}	
}
