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
	LightModel(PApplet pApp,PGraphics pg, PGraphics lm, float scale,LEDType type) {
		this.pApp = pApp;
		this.pg = pg;
		this.lightMap = lm;
		this.type = type;
		lightLevel = 64;

		switch (type) {
		case BULB:
			diode = buildLightMap(scale/2,(float) 2);      
			led = buildBulbModel(scale,lightLevel);      
			break;
		case SMD:
			diode = buildLightMap(scale,2);      
			led = buildSMDModel(scale,lightLevel);      
			break;     
		}
	}

	void draw(int col) {
		float bri;
		int hCol;
		pg.pushMatrix();    

		bri = (float) ((float) ScreenLED.getBrightness(col) / 255.0);
		if (bri < 0.005) {
			col = pApp.color(10);
			hCol = pApp.color(0);
		}
		else {
			bri = pApp.max((float) 0.4,bri);
			float h = pApp.hue(col);
			float s = pApp.saturation(col);
			pApp.colorMode(PConstants.HSB,255);
			hCol = pApp.color(h,s,255 * bri);
			pApp.colorMode(PConstants.RGB,255);
			
			pg.emissive(col);
			pg.tint(col);
			pg.image(lightMap,0,0); 			
		}

		pg.noTint();  
		pg.emissive(col);
		pg.image(led,0,0);



		pg.emissive(col);
		pg.tint(hCol);
		pg.image(diode,0,0,diode.width * 2,diode.height * 2);

		pg.popMatrix();    
	} 

	PGraphics buildBulbModel(float mapSize,float lit) {
		PGraphics pg;

		pg = pApp.createGraphics((int) mapSize,(int) mapSize,PConstants.P3D);
		pg.smooth(8);

		pg.beginDraw();
		pg.noStroke();
		float diam = mapSize * (float) 0.9;
		pg.fill(pApp.color(lit));
		pg.sphereDetail(60);

		pg.background(0,0,0,0);
		pg.translate(mapSize / 2, mapSize / 2,-200);

		pg.shininess(35);

		pg.ambient(0);
		pg.lightSpecular(255, 255, 255);
		pg.directionalLight(lit, lit, lit, (float)2.25, 2, -1);  
		pg.specular(100);
		pg.emissive(lit);
		pg.pushMatrix();
		pg.translate(0,0,-88);
		pg.circle(0,0,diam);
		pg.popMatrix();
		pg.sphere(diam/2);

		pg.endDraw();
		return pg;    
	}  

	PGraphics buildSMDModel(float mapSize,float lit) {
		PGraphics pg;

		pg = pApp.createGraphics((int) mapSize,(int) mapSize,PConstants.P3D);

		pg.beginDraw();    
		pg.background(0,0,0,0);
		pg.imageMode(PConstants.CENTER);
		pg.ellipseMode(PConstants.CENTER);
		pg.rectMode(PConstants.CENTER);    
		pg.translate(mapSize / 2, mapSize / 2);    

		float s = mapSize / 2;
		pg.noStroke();
		pg.fill(pApp.color(lit * (float) 0.6));    
		pg.square(0,0,s);

		pg.stroke(pApp.color(lit));
		pg.strokeWeight(4);
		float v = s / 2;
		pg.line(-v,-v,v,-v);
		pg.line(-v,v,-v,-v);

		s *= 0.875;
		pg.shininess(100);
		pg.fill(pApp.color(lit * (float) 0.75));
		pg.strokeWeight(3);
		pg.stroke(pApp.color(lit));
		pg.ellipse(0,0,s,s * (float) 0.9);

		pg.noFill();
		pg.strokeWeight(3);
		pg.stroke(pApp.color(lit * (float) 0.25));
		pg.ellipse(0,0,(s-4),(s-4) * (float)0.9);       


		s /= 2;   
		s *= 0.8;      
		int cDark = pApp.color(lit * (float) 0.25);
		int cLight = pApp.color(lit * (float) 0.8);
		pg.strokeWeight(5);
		pg.stroke(cDark);
		pg.line(1,0,1,-s);
		pg.stroke(cLight);
		pg.line(0,0,0,-s);

		s *= 0.717;   
		pg.stroke(cDark);    
		pg.line(2,0,-s+2,s);    
		pg.stroke(cLight);    
		pg.line(0,0,-s,s);

		pg.stroke(cDark); 
		pg.line(2,0,s+2,s);
		pg.stroke(cLight);    
		pg.line(0,0,s,s);

		pg.noStroke();
		pg.fill(pApp.color(lit));
		pg.square(0,0,20);

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
		maxDist = (float) Math.sqrt(dx * dx + dx * dx);
		cx = xst + dx;
		cy = yst + dx;

		for (y = 0; y < mapSize; y++) {
			for (x = 0; x < mapSize; x++) {

				dx = (float) (x+xst) - cx;
				dy = (float) (y+yst) - cy;
				dist = (float) Math.sqrt(dx * dx + dy * dy);
				dist = (float) Math.max(0,1-(dist/maxDist));
				alpha = (float) (255 * Math.pow(dist,falloff));
				dist = (float) (255 * Math.pow(dist,falloff));
				pg.set(x+xst,y+yst,pApp.color(dist,dist,dist,alpha));
			}      
		}      
	}  	
}