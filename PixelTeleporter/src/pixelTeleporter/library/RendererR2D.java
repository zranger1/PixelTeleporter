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
public class RendererR2D extends LEDRenderer {

	float mapWidth;     // dimensions of offscreen surface
	float mapHeight;
	float mapCenterX;
	float mapCenterY;
	float lightMapSize;
	float ledSize;
	float coreSize;

	PGraphics pg;        // offscreen drawing surface
	PGraphics lightMap;  // texture model of light falloff
	LightModel ledModel; // model of physical LED

	public RendererR2D(PixelTeleporter p) {
		super(p);
	}
	
	void initialize() {
		// create and configure offscreen surface for drawing
		ledSize = pt.ledSize;
		coreSize = (float) 0.55 * ledSize;
		float mapMargin = ledSize * 3;
		mapWidth = worldXSize + mapMargin;
		mapHeight = worldYSize + mapMargin;
		mapCenterX = mapWidth / 2;  mapCenterY = mapHeight / 2;

		pg = pApp.createGraphics((int) mapWidth,(int) mapHeight,PConstants.P3D);
		pg.imageMode(PConstants.CENTER);
		pg.rectMode(PConstants.CENTER);
		pg.beginDraw();
		pg.blendMode(PConstants.ADD);
		pg.shininess(1000);
		pg.specular(pg.color(255));
        pg.ambient(255,255,255);  		
		pg.noStroke();
		pg.endDraw();

		// create light map and highlight textures
		lightMapSize = pt.pixelSize * 4;  
		lightMap = buildLightMap(lightMapSize,falloff,model);
		ledModel = new LightModel(pApp,pg,lightMap,ledSize,ambient_light,model);
	}
	
	public void render(LinkedList <ScreenLED> obj) {
		pApp.pushMatrix();
		pg.beginDraw();
		pg.blendMode(PConstants.ADD);				
		pg.translate(mapCenterX,mapCenterY,0);

		pg.background(bgColor, bgAlpha);
        pg.ambientLight(ambient_light,ambient_light,ambient_light);
		pg.lightSpecular(ambient_light,ambient_light,ambient_light);
		pg.ambient(255,255,255);		
		
		pt.mover.applyObjectTransform();
		for (ScreenLED led : obj) {
			pg.pushMatrix();			
			pg.translate(led.x,led.y);
			
			int col = pt.pixelBuffer[led.index];
			ledModel.draw(col);
			
			pg.popMatrix();
 						    			
		}   	
		pg.endDraw();
		pApp.image(pg.get(),0,0);
		pApp.popMatrix();
	}	
	
	// map of inverse power law-based light falloff around LED
	PGraphics buildLightMap(float mapSize,float falloff,LEDType style) {
		PGraphics pg;

		pg = pApp.createGraphics((int) mapSize,(int) mapSize,PConstants.P3D);
		pg.smooth(8);

		pg.beginDraw();
		pg.imageMode(PConstants.CENTER);		
		pg.shininess(100);
		pg.specular(pg.color(255));		
		
		setFalloffModel(pg,0,0,mapSize,falloff);

		pg.endDraw();
		return pg;
	}
	
	// takes a PGraphics object on which beginDraw() has been called, and fills it
	// with a regional light map that falls off at the specified rate
	void setFalloffModel(PGraphics pg,int xst, int yst, float mapSize,float falloff) {
		int x,y;
		float cx,cy,dx,dy,dist,maxDist;
		float alpha,brightness;

		maxDist = (mapSize / 2);
		cx = xst + maxDist;
		cy = yst + maxDist;
		brightness = 255 * indirectIntensity;
 			
		for (y = 0; y < mapSize; y++) {
			for (x = 0; x < mapSize; x++) {
				dx = (float) (x+xst) - cx;
				dy = (float) (y+yst) - cy;
				dist = (float) Math.sqrt(dx * dx + dy * dy);
				dist = PApplet.max(0,1-(dist/maxDist));
				alpha = (float) (255 * Math.pow(dist,falloff));
				//dist = (float) (255 * Math.pow(dist, falloff));
				pg.set(x+xst,y+yst,pApp.color(brightness,alpha));
			}		  
		}			
	}
	
	/**
	 * Controls the amount of light admitted to the model "camera", increasing
	 * overall brightness and possibly oversaturating and blowing out
	 * brightly lit areas.<p>
	 * Some overexposure is common in LED videos. It gives a glowing halo effect 
	 * around the lit LEDs.  Use with care though - a little goes a long way.
	 * The exposure setting here has enough range to make completely washed 
	 * out displays.<p> 
	 * This value is essentially a luminance multiplier. It controls the base 
	 * ambient light level plus a bloom effect.  The default value is 1.
	 * A setting of 0 turns the effect completely off, and settings above 1 
	 * produce increasingly "overexposed" images.
	 * @param x
	 */
	public void setExposure(float x) {
		ambient_light = PApplet.constrain(x,0,255);
	}
	
	public void setFalloff(float v) {	
		lightMap.beginDraw();	
		setFalloffModel(lightMap,0,0,lightMap.width,v);
		lightMap.endDraw();
	}

	// set control values for the high def renderer
	public void setControl(RenderControl ctl, float value) {
		super.setControl(ctl,value);
		
		switch(ctl) {
		case FALLOFF:
			setFalloff(value);
			break;
		case LEDMODEL_BULB:
		case LEDMODEL_SMD:
			// rebuild LED core appearance model
			ledModel = new LightModel(pApp,pg,lightMap,ledSize,ambient_light,model);
			break;			
		default:
			break;
		}
	}	



}
