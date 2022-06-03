package pixelTeleporter.library;

import processing.core.*;
import processing.opengl.PShader;
import processing.opengl.PGraphics3D;

public class AxisLegend {
	LEDRenderer parent;
	PApplet pApp;
	PGraphics xt,yt,zt;
	PShape axis;
	PVector xTag,yTag,zTag;
	PShader ps;
	float weight = 80;
	float axisLength = 250;	

	public AxisLegend(LEDRenderer p) {
		this.parent = p;
		pApp = parent.pApp;
	}

	public void initialize() {
		PShape sh;
		float edgeX,edgeY,edgeZ;
		xTag = new PVector();
		yTag = new PVector();
		zTag = new PVector();
		xt = createTextBillboard("X",pApp.color(255,0,0,255));  
		yt = createTextBillboard("Y",pApp.color(0,255,0,255));
		zt = createTextBillboard("Z",pApp.color(0,0,255,255));  

		ps = parent.pt.ptf.loadShader("spritefrag.glsl","spritevert.glsl");

		axis = pApp.createShape(PConstants.GROUP);
		
		sh = pApp.createShape();
		sh.beginShape(PConstants.LINES);
		sh.strokeWeight(2);
		sh.stroke(130);  
		// origin indicator
		// x
		sh.vertex(-30,0,0);
		sh.vertex(30,0,0);
		// y
		sh.vertex(0,-30,0);
		sh.vertex(0,30,0);
		// z
		sh.vertex(0,0,-30);
		sh.vertex(0,0,30);
		sh.endShape();
		
		axis.addChild(sh);

		// axis markers at edge of object model
		edgeX = parent.worldXSize / 2;
		edgeY = parent.worldYSize / 2;
		edgeZ = parent.worldZSize / 2;
		
		sh = pApp.createShape();
		sh.beginShape(PConstants.LINES);
		sh.strokeWeight(2);	
		sh.translate(edgeX,edgeY,edgeZ);
		// x
		float l = -axisLength;
		sh.stroke(255,0,0);
		sh.vertex(0,0,0);
		sh.vertex(l,0,0);
		// y
		sh.stroke(0,255,0);
		sh.vertex(0,0,0);		
		sh.vertex(0,l,0);
		// z	
		sh.stroke(0,0,255);
		sh.vertex(0,0,0);
		sh.vertex(0,0,l);  

		sh.endShape();
		
		axis.addChild(sh);
		
		l -= 10;
		xTag.set(edgeX + l,edgeY,edgeZ);
		yTag.set(edgeX,edgeY + l,edgeZ);
		zTag.set(edgeX,edgeY,edgeZ + l);

	} 	


	PGraphics createTextBillboard(String s,int c) {
		PGraphics b = pApp.createGraphics(100,100,PConstants.P3D);
		b.beginDraw();
		b.fill(c);
		b.background(pApp.color(0,0,0,0));
		b.textSize(weight);
		b.textAlign(PConstants.CENTER,PConstants.CENTER);
		b.text(s,50,50);
		b.endDraw();  

		return b;
	}	

	public void draw() {
		// if we have a 3D universe, draw center marker and axes 
		//if (parent.worldZSize == 0) return;
		
		pApp.shape(axis);
		pApp.stroke(255);
		
		// set up our billboard sprite shader
		// temorarily disable optimized stroke so we can switch shader uniforms
	    // during the frame while we draw the axis labels		
		
		pApp.hint(PConstants.DISABLE_OPTIMIZED_STROKE);		
		pApp.shader(ps,PConstants.POINTS);	
		ps.set("weight",weight);  				
		ps.set("sprite",xt);
//		pApp.stroke(255,0,0);
		pApp.point(xTag.x,xTag.y,xTag.z);
		
		ps.set("sprite",yt);
//		pApp.stroke(0,255,0);
		pApp.point(yTag.x,yTag.y,yTag.z);		

        ps.set("sprite",zt);
//		pApp.stroke(0,0,255);
		pApp.point(zTag.x,zTag.y,zTag.z);
		
		pApp.hint(PConstants.ENABLE_OPTIMIZED_STROKE);				
	}

}
