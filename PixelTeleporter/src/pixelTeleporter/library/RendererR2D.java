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
	PixelTeleporter parent;
	PApplet pApp;
	PGraphics pgLightMap;
	PGraphics pgLED;
	
	public RendererR2D(PixelTeleporter p) {
		parent = p;
		pApp = parent.app;
		pgLightMap = buildLightMap(pApp.max(pApp.width,pApp.height) / 5);
		pgLED = buildLEDModel(parent.pixelSize);		
	}
	
	
	public void render(LinkedList <ScreenLED> obj) {
		pApp.pushMatrix();

	    pApp.blendMode(pApp.ADD);
	    pApp.shininess(100);
	    pApp.specular(255);
	    pApp.lightSpecular(77,77,77);
	    pApp.directionalLight(255,255,255,0,0,-1);	    
	
		//parent.mover.applyObjectTransform();
		for (ScreenLED led : obj) {
			pApp.pushMatrix();
			int col = led.renderAssist();
				
			
			pApp.emissive(led.getBrightness());
		    pApp.blendMode(pApp.BLEND);
		    pApp.image(pgLED,0,0); 
		    pApp.blendMode(pApp.ADD);
		    pApp.tint(col);
		    pApp.image(pgLightMap,0,0);			
			pApp.popMatrix();
		}   
		pApp.popMatrix();		
	}
	
	PGraphics buildLightMap(int mapSize) {
		  PGraphics pg;
		  int x,y;
		  float dx,dy,center,dist,maxDist;
		  float LEDSize = (float) 0.12; // size of the lit area of the LED
		  float LEDCore = (float) (LEDSize * 0.5);
		  
		  center = mapSize / 2;
		  maxDist = (float) Math.sqrt(2 * center * center);
		  
		  pg = pApp.createGraphics(mapSize,mapSize);
		  
		  pg.beginDraw();

		  pg.noFill();
		  pg.rectMode(pApp.CENTER);
		  pg.blendMode(pApp.ADD);

		  
		  for (y = 0; y < mapSize; y++) {
		    for (x = 0; x < mapSize; x++) {

		      dx = (float) x - center;
		      dy = (float) y - center;
		      dist = (float) (Math.sqrt(dx * dx + dy * dy) / maxDist);
		      if (dist <= LEDCore) {
		        dist = 255;
		      }
		      else if (dist <= LEDSize) {
		        dist = 255 * (1-dist);
		      } else {
		        dist = (float) (255 * Math.pow(1-dist , 2.75));
		      }
		      pg.set(x,y,pApp.color(dist,dist,dist,dist));
		    }
		  }

		  dist = (float) Math.floor(mapSize * LEDSize);
		/*  
		  pg.strokeWeight(2);
		  pg.stroke(16,16,16);
		  pg.square(center,center - 1,dist + 1);   
		  
		  pg.strokeWeight(1);  
		  pg.stroke(52,52,52,128);
		  pg.square(center,center,dist);
		*/  
		  
		  pg.endDraw();
		  return pg; 
		}	
	
	// builds a sprite for the default "simple" 2D LED model
	PGraphics buildLEDModel(int sz) {
		  PGraphics pg;
		  float center;
		  float LEDCore = (float) (sz * 0.9);  
		  center = sz / 2;
		  
		  pg = pApp.createGraphics(sz,sz);
		  
		  pg.beginDraw();
		  pg.shininess(100);

		  pg.rectMode(pApp.CENTER);
		  pg.blendMode(pApp.ADD);

		  pg.strokeWeight(2);
		  pg.fill(20);
		  pg.stroke(20);
		  pg.square(center,center - 1,sz-1);   

		  pg.noFill();
		  pg.strokeWeight(1);  
		  pg.stroke(32,32,32);
		//  pg.square(center,center,sz-2);  
		  
		  pg.circle(center,center,LEDCore);
		  
		  pg.endDraw();
		  return pg; 
		}	

}
