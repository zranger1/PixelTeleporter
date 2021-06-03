package pixelTeleporter.library;

import java.util.*;
import processing.core.*;

/**
    Manages communication with the PixelTeleporter server device
    and provides a framework for object rendering and basic user 
    camera control.
    <p>
	To use, create a PixelTeleporter object in your sketch and
	call its start() method to begin listening for pixel data
	on the network.
 */
public class BackgroundImage implements PConstants {
	PApplet app;
	PImage bgImageOriginal;   // full background image as read from file
	PImage bgImageZoomed;     // portion of background image displayed on screen
	PImage bgImageFinal;      // clipped, zoomed image ready to display
	float x;              // coordinates (in full image) of the portion
	float y;              // we're selecting to display on the screen 
	float scale;          // allows user to zoom in and out on image
	boolean needClip;
	boolean needScale;
	
	public BackgroundImage(PApplet pApp) {
		this.app = pApp;
		this.bgImageOriginal = null;  // original background image
		this.bgImageZoomed = null;      // image at user zoom factor
		this.bgImageFinal = null;
		this.x = 0;
		this.y = 0;
		this.scale = 1.0f;
		this.needClip = true;
		this.needScale = true;
	}
	
	public void resetBackground() {
		this.x = 0;
		this.y = 0;
		this.scale = 1.0f;
		this.needClip = true;
		this.needScale = true;		
	}
	
/**
 * Load an image file and scale it to the full current window size	
 * @param imgPath
 */
	public void load(String imgPath) {
		resetBackground();
		this.bgImageOriginal = app.loadImage(imgPath);
	}
	
	public void clipBackgroundToWindow() {
		if (this.bgImageZoomed == null) return;
		
		if (this.needClip) {
			this.x = app.constrain(this.x, -bgImageZoomed.width / 2, bgImageZoomed.width);
			this.y = app.constrain(this.y, -bgImageZoomed.height / 2, bgImageZoomed.height);		

			// clip to our display area
			this.bgImageFinal = this.bgImageZoomed.get((int) this.x,(int) this.y,app.width, app.height);			
			app.background(bgImageFinal);	
		
			this.needClip = false;
		}
	}
	
	public void buildBackgroundImage() {
		PImage tmp;
		if (bgImageOriginal == null) return;
		
		// make a copy of the current image at our new scale, preserving aspect ratio
		if (this.needScale) {
			this.bgImageZoomed = bgImageOriginal.copy();
			this.bgImageZoomed.resize((int) ((float) this.bgImageOriginal.width * this.scale),(int) 0);	
			this.needScale = false;			
		}
		
		clipBackgroundToWindow();
	}
		
	public void showImage() {
		buildBackgroundImage();
		if (this.bgImageFinal != null) {
			app.background(bgImageFinal);
		}		
	}
	
	public void moveRect(float xOffs,float yOffs) {
		if (bgImageOriginal == null) return;
		this.x -= xOffs;
		this.y -= yOffs;
		this.needClip = true;
	}
	
	public float getScale() { return this.scale; }
	public void setScale(float n) { this.scale = n;}	
}
