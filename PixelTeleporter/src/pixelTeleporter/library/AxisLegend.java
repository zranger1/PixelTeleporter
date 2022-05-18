package pixelTeleporter.library;

import processing.core.*;
import processing.opengl.PGraphics3D;

/*
public class AxisLegend {
	PApplet pApp;
	PGraphics sprite,xt,yt,zt; 
	PShape axis;
	PShader ps;
	float weight = 100;
	float axisLength = 250;	
	
	public AxisLegend(PApplet p) {
		pApp = p;
		shade = loadShader("spritefrag.glsl","spritevert.glsl");		
		
	}
	
	public void initalize() { 	  
      xt = createTextBillboard("X",color(255,0,0,255));  
	  yt = createTextBillboard("Y",color(0,255,0,255));
	  zt = createTextBillboard("Z",color(0,0,255,255));  

		  particles = createShape(PShape.GROUP);
		  shade = loadShader("spritefrag.glsl","spritevert.glsl");


		  axis = createShape();
		  axis.beginShape(LINES);
		  axis.strokeWeight(2);
		  axis.stroke(130);  
		  // origin indicator
		  // x
		  axis.vertex(-30,0,0);
		  axis.vertex(30,0,0);
		  // y
		  axis.vertex(0,-30,0);
		  axis.vertex(0,30,0);
		  // z
		  axis.vertex(0,0,-30);
		  axis.vertex(0,0,30);
		  
		  // axis markers at edge of cloud
		  // x
		  float l = -edge + axisLength;
		  axis.stroke(255,0,0);
		  axis.vertex(-edge,-edge,-edge);
		  axis.vertex(l,-edge,-edge);
		  // y
		  axis.stroke(0,255,0);
		  axis.vertex(-edge,-edge,-edge);
		  axis.vertex(-edge,l,-edge);
		  // z
		  axis.stroke(0,0,255);
		  axis.vertex(-edge,-edge,-edge);
		  axis.vertex(-edge,-edge,l);  

		  axis.endShape();
		  
		  particles.addChild(axis);

		  // Writing to the depth buffer is disabled to avoid rendering
		  // artifacts due to the fact that the particles are semi-transparent
		  // but not z-sorted.
		  hint(ENABLE_STROKE_PERSPECTIVE);
		  hint(DISABLE_DEPTH_MASK);
		  blendMode(ADD);
		  shade.set("weight",weight);  
		} 	
	
	
	PGraphics createTextBillboard(String s,int c) {
		  PGraphics b = pApp.createGraphics(100,100,PConstants.P3D);
		  b.beginDraw();
		  b.fill(c);
		  b.background(color(0,0,0,0));
		  b.textSize(40);
		  b.textAlign(PConstants.CENTER,PConstants.CENTER);
		  b.text(s,50,50);
		  b.endDraw();  
		  
		  return b;
		}	
	
	public void draw() {
		
	}

}
*/