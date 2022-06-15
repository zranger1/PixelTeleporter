package pixelTeleporter.library;

import processing.core.*;

class TooltipHandler {
	int hoverTimer = 0;
	int ttDelay = 700;  
	boolean on = false;
	int x,y;
	
	TooltipHandler() {
		hoverTimer = 0;
		on = false;
		x = y = 0;
	}
	
	// if we've been sitting in the same place for a while...
	void hoverCheck(int t, int x1,int y1) {
		
		// if tooltip is already onscreen, do nothing
		if (on) return; 

		// if the mouse has moved a significant distance, restart the hover timer
		if ((PApplet.abs(x - x1) > 3 ) || (PApplet.abs(y - y1) > 3)) {
		    hoverTimer = t;			
		}
		
		// save the latest mouse coords
		this.x = x1;
		this.y = y1;
		
		// see if we've been sitting long enough to bring up the tooltip.
		on = ((t - hoverTimer > ttDelay));
	}
	
}
