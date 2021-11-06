/**
 * 
 */
package pixelTeleporter.library;

import java.util.LinkedList;
import processing.core.*;

/**
 * Realistic 2D ScreenLED renderer. Works best w/square aspect ratio windows
 *
 */
// TODO -- add parameter control interface
public class RendererR2D implements LEDRenderer {
	PixelTeleporter pt;
	PApplet pApp;
	float mapWidth;     // dimensions of offscreen surface
	float mapHeight;
	float mapCenterX;
	float mapCenterY;
	float lightMapSize;
	float ledSize;

	float exposure;     // use lighting to model camera sensor saturation
	float falloff;      // how fast light from the LED attenuates

	PGraphics pg;  // offscreen drawing surface
	PGraphics lightMap;  // texture model of light falloff
	PGraphics highlight; // texture model of specular highlights on LED

	public RendererR2D(PixelTeleporter p,float xSize,float ySize) {
		pt = p;
		pApp = pt.app;

		// create and configure offscreen surface for drawing
		mapWidth = (float) (xSize * 1.1); mapHeight = (float) (ySize * 1.1);
		mapCenterX = mapWidth / 2;  mapCenterY = mapHeight / 2;
		exposure = 8;
		falloff = (float) 3.25;

		pg = pApp.createGraphics((int) mapWidth,(int) mapHeight,PConstants.P3D);
		pg.imageMode(PConstants.CENTER);
		pg.rectMode(PConstants.CENTER);
		pg.beginDraw();
		pg.blendMode(PConstants.ADD);
		pg.shininess(1000);
		pg.specular(pg.color(255));
		pg.endDraw();

		// create light map and highlight textures
		ledSize = pt.pixelSize;
		lightMapSize = PApplet.max(mapWidth,mapHeight) / 6;  
		lightMap = buildLightMap((int) lightMapSize,falloff,(int) (0.55 * ledSize));
		highlight = buildLightMap((int) ledSize / 2,3,0);;		
	}

	public void render(LinkedList <ScreenLED> obj) {
		pApp.pushMatrix();
		pg.beginDraw();
		pg.translate(mapCenterX,mapCenterY,0);

		pg.lightSpecular(exposure,exposure,exposure);
		pg.directionalLight(exposure,exposure,exposure,0,0,-1);
		pg.background(8); // really needs to be (0,0);

		pt.mover.applyObjectTransform();
		for (ScreenLED led : obj) {
			int col = pt.pixelBuffer[led.index];
			int b = ScreenLED.getBrightness(col);

			// make sure there's always a tiny amount of
			// ambient white light around.
			if (b < 10) {
				col = pApp.color(10);
				b = 10;
			}		

			// set up material properties and draw individual 
			// LEDs, then lay the light map on top.
			pg.emissive(col);    
			pg.image(lightMap,led.x,led.y);      

			// draw (white) highlights proportional to brightness
			pg.emissive(b);
			pg.image(highlight,led.x-4,led.y-4);  		    			
		}   	
		pg.endDraw();
		pApp.image(pg.get(),0,0);
		pApp.popMatrix();
	}

	PGraphics buildLightMap(int mapSize,float falloff,int coreSize) {
		PGraphics pg;
		int x,y;
		float dx,dy,center,dist,maxDist;
		float alpha;

		center = mapSize / 2;
		maxDist = (float) Math.sqrt(center * center + center * center); 

		pg = pApp.createGraphics(mapSize,mapSize,PConstants.P3D);
		pg.smooth(8);

		pg.beginDraw();
		pg.noStroke();
		pg.noFill();

		for (y = 0; y < mapSize; y++) {
			for (x = 0; x < mapSize; x++) {

				dx = (float) x - center;
				dy = (float) y - center;
				dist = (float) (1-(Math.sqrt(dx * dx + dy * dy) / maxDist));      
				alpha = (float) (255 * Math.pow(dist,falloff));
				dist = (float) (255 * Math.pow(dist, falloff));
				pg.set(x,y,pApp.color(dist,dist,dist,alpha));
			}		  
		}

		if (coreSize > 0) {
			float c = (float) (255 * Math.pow(coreSize,falloff) /2);
			pg.stroke(pApp.color(c));
			pg.fill(255);
			pg.circle(center,center,coreSize);    
		}
		pg.endDraw();
		return pg;
	}

	/**
	 * Higher values admit more light to the model "camera", increasing
	 * overall brightness and possibly oversaturating and blowing out
	 * brightly lit areas.<p>
	 * Some overexposure is common in LED videos. It gives a glowing halo effect 
	 * around the lit LEDs.  Use with care though - a little goes a long way.
	 * The exposure setting here has enough range to make completely washed 
	 * out displays.<p> 
	 * The default value for exposure is 8. A setting of 0 turns the effect off, and
	 * settings between 20 and 32, depending on your LED pattern, produce a good
	 * "overexposed" video look. 
	 * @param x
	 */
	public void setExposure(int x) {
		exposure = PApplet.constrain(x,0,255);
	}

	// set control values for the high def renderer
	public void setControl(RenderControl ctl, float value) {
		switch(ctl) {
		case RESET:
			// TODO - (mostly) not yet implemented
			setExposure(8);
			falloff = (float) 3.25;
			break;
		case EXPOSURE:
			setExposure((int) value);
			break;
		case FALLOFF:
			// TODO - (mostly) not yet implemented
			falloff = value;
			break;
		}
	}	



}
