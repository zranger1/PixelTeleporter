package pixelTeleporter.library;

import processing.core.*;

// models the structure and light output of the LED core
// the larger, global lightmap is handled separately
class LightModel {
	float x,y;
	float lightLevel;
	PApplet pApp;
	PGraphics pg;
	PGraphics lightMap;
	PGraphics led;
	PGraphics diode;
	LEDType type;

	// just the basics for setup.  
	LightModel(PApplet pApp,PGraphics pg, PGraphics lm, float scale, float lightLevel,LEDType type) {
		this.pApp = pApp;
		this.pg = pg;
		this.lightMap = lm;
		this.type = type;
		this.lightLevel = lightLevel;
		
		switch (type) {
		case BULB:
			diode = buildLightMap(scale,2);   					
			led = buildBulbModel(scale,lightLevel);      
			break;
		case SMD:     
			diode = buildLightMap(scale,(float) 1.5);   					
			led = buildSMDModel(scale,lightLevel);      
			break;     
		}
	}

	void draw(int col) {
		float bri;
		int hCol;
		
		bri = (float) ((float) ScreenLED.getBrightness(col) / 255.0);
		if (bri < 0.005) {
			col = pApp.color(8);
			hCol = pApp.color(0);
		}
		else {
			// calculate bright diode center color
			bri = PApplet.max((float) 0.02,bri);			
			hCol = ScreenLED.setBrightness(col,bri);
			
			// display diffuse light map
			pg.emissive(col);
			pg.tint(col);
			pg.image(lightMap,0,0); 			
		}
		
		pg.noTint();  
		pg.emissive(col);
		pg.image(led,0,0);		
		
		pg.emissive(hCol);
		pg.tint(hCol);
		pg.image(diode,0,0);		

	} 

	PGraphics buildBulbModel(float mapSize,float lit) {
		PGraphics pg;
		PShape base;
			
	    base = pApp.createShape(PConstants.ELLIPSE,0,0,mapSize,mapSize);
	    base.setFill(pApp.color((int) (lit*0.8)));
	    base.translate(0,0,-(mapSize / 6));
	    base.disableStyle();

		pg = pApp.createGraphics((int) mapSize,(int) mapSize,PConstants.P3D);
		pg.smooth(8);
	    
	    pg.beginDraw();
	    pg.translate(mapSize / 2, mapSize / 2,0);
	    pg.background(0,0,0,0);        
	    pg.noStroke();
	    pg.ellipseMode(PConstants.CENTER);

	    pg.fill(pApp.color(lit));
	    pg.sphereDetail(60);
	    
	    pg.shininess(60);
	    pg.emissive(pApp.color(lit));
	    pg.lightSpecular(255, 255, 255);   
	    pg.ambientLight(lit/2,lit/2,lit/2);
	    pg.directionalLight(lit, lit, lit, (float) 2.25, 2, -1);    
	    pg.specular(255,255,255);
	    
	    pg.shape(base);
	    pg.sphere((float) (mapSize * 0.3125));
	        
		pg.endDraw();
		return pg;    
	}  

	PGraphics buildSMDModel(float mapSize,float lit) {
		PGraphics pg;
	    float s,v;
	    float scaledStroke = (float) (0.015625 * mapSize);  // 1/64 of the map size		

	    lit = 64;
	    
		pg = pApp.createGraphics((int) mapSize,(int) mapSize,PConstants.P3D);

	    pg.beginDraw();    
	    pg.background(0,0,0,0);

	    pg.imageMode(PConstants.CENTER);
	    pg.ellipseMode(PConstants.CENTER);
	    pg.rectMode(PConstants.CENTER);    
	    pg.translate(mapSize / 2, mapSize / 2);  
	    
	    pg.shininess(150);
	    pg.emissive(pApp.color(lit));
	    pg.specular(100);		    
	    pg.ambientLight(lit,lit,lit);    
	    pg.lightSpecular(255, 255, 255);    
    	            
	    s = (float) (mapSize * 0.775);
	    
	    // draw square SMD frame
	    pg.noStroke();
	    pg.fill(pApp.color((int) (lit * 0.6)));    
	    pg.square(0,0,s);
	    
	    // 3D highlighting on top and left edges
	    pg.stroke(pApp.color(lit));
	    pg.strokeWeight(scaledStroke);
	    v = s / 2;
	    pg.line(-v,-v,v,-v);
	    pg.line(-v,v,-v,-v);
	   
	    s *= 0.875;

	    // elliptical area at center of SMD
	    pg.shininess(100);
	    pg.fill(pApp.color((int) ((int) lit * 0.75)));
	    pg.strokeWeight(scaledStroke);
	    pg.stroke(pApp.color(lit));
	    pg.ellipse(0,0,s,(float) (s * 0.925));
	    
	    pg.noFill();
	    pg.strokeWeight(scaledStroke);
	    pg.stroke(pApp.color((int) (lit * 0.25)));
	    v = (float) (s - scaledStroke * 0.9);    
	    pg.ellipse(0,0,v,(float) (v * 0.925));       
	    
	    // fake wiring!
	    s /= 2;   
	    s *= 0.8;      
	    float cDark = (float) (lit * 0.25);
	    float cLight = (float) (lit * 0.8);
	    pg.strokeWeight(scaledStroke);
	    pg.stroke(pApp.color(cDark));
	    pg.line(scaledStroke/(float) 2.25,(float) 0,scaledStroke/(float) 2.25,-s);
	    pg.stroke(pApp.color(cLight));
	    pg.line(0,0,0,-s);
	    
	    s *= 0.725;  
	    v = s + scaledStroke / 2;
	    pg.stroke(pApp.color(cDark));    
	    pg.line(-scaledStroke / 2,0,-v,s);    
	    pg.stroke(pApp.color(cLight));    
	    pg.line(0,0,-s,s);
	    
	    pg.stroke(pApp.color(cDark)); 
	    pg.line(scaledStroke / 2,0,v,s);
	    pg.stroke(pApp.color(cLight));    
	    pg.line(0,0,s,s);
	    
	    pg.noStroke();
	    pg.fill(pApp.color(lit));
	    pg.emissive(pApp.color(lit));
	    pg.circle(0,0,scaledStroke * 5);
	        
	    pg.endDraw();
		return pg; 
	} 

	// map of inverse power law-based light falloff around LED
	PGraphics buildLightMap(float mapSize,float falloff) {
		PGraphics pg;

		pg = pApp.createGraphics((int) mapSize,(int) mapSize,PConstants.P3D);
		pg.smooth(8);

		pg.beginDraw();
		pg.imageMode(PConstants.CENTER);
		pg.shininess(100);
		pg.emissive(pApp.color(255));
		pg.specular(pApp.color(255));    

		setFalloffModel(pg,0,0,mapSize,falloff);

		pg.endDraw();
		return pg;
	}

	// takes a PGraphics object on which beginDraw() has been called, and fills it
	// with a regional light map that falls off at the specified rate
	void setFalloffModel(PGraphics pg,int xst, int yst, float mapSize,float falloff) {
		int x,y;
		float cx,cy,dx,dy,dist,maxDist;
		float alpha;

		dx = mapSize / 2;
		maxDist = (float) dx; //Math.sqrt(dx * dx + dx * dx);
		cx = xst + dx;
		cy = yst + dx;

		for (y = 0; y < mapSize; y++) {
			for (x = 0; x < mapSize; x++) {

				dx = (float) (x+xst) - cx;
				dy = (float) (y+yst) - cy;
				dist = (float) Math.sqrt(dx * dx + dy * dy);
				dist = PApplet.max(0,1-(dist/maxDist));
				alpha = (float) (255 * Math.pow(dist,falloff));
				//dist = (float) (255 * Math.pow(dist,falloff));
				pg.set(x+xst,y+yst,pApp.color(255,alpha));
			}      
		}      
	}  	
}