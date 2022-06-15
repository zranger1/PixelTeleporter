package pixelTeleporter.library;
import processing.core.*;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

/**
 * The listener interface for receiving ptEvent events.
 * The class that is interested in processing a ptEvent
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addptEventListener<code> method. When
 * the ptEvent event occurs, that object's appropriate
 * method is invoked.
 *
 * @see ptEventEvent
 */
public class PTEventListener {
	PixelTeleporter pt;
	PApplet pApp;
	
	final int MOUSE_MIN_MOVEMENT = 10; // dead zone for mouse UI rotate/translate
	
	PTEventListener(PixelTeleporter parent) {
		pt = parent;
		pApp = pt.app;
	}

	/**
	 * Keyboard handler
	 *
	 * @param e keyboard event
	 */
	public void keyEvent(final KeyEvent e) {
		if (e.getAction() == KeyEvent.RELEASE) {
			switch (e.getKey()) {        
			case ' ': // stop automatic rotation
				//pt.ptCam.autoMove = !pt.ptCam.autoMove;
				break;
			case 'r': //reset rotation & translation for foreground and back

				break;
			case 'R':
				pt.bg.resetBackground();
				break;
			case PConstants.TAB:
				// pause/unpause data, hold current frame if paused
				pt.isRunning = !pt.isRunning;

				// enable per-pixel tooltips only when paused.
				if (pt.isRunning) {
					pt.disablePixelInfo();						
				}
				else {
					pt.enablePixelInfo();
				}
				break;
			case 'X':  // toggle display of axes
			case 'x':	
				pt.showAxes = !pt.showAxes;
				break;

			default:
				break;
			} 			
		}
	}

	/**
	 * Mouse event handler.
	 *
	 * @param e mouse event
	 */
	public void mouseEvent(final MouseEvent e) {
		float x = e.getX();
		float y = e.getY();
		float distX,distY;
/*
		switch (e.getAction()) {
		case MouseEvent.WHEEL:
			if (e.isShiftDown())  {
				// if shift, zoom in/out on the background
				float s = pt.bg.getScale();
				s = s - (0.05f * (float) (e.getCount()));
				PApplet.constrain(s,0.01f,1.5f);
				pt.bg.setScale(s);
				pt.bg.needScale = true;
				pt.bg.needClip = true;
			}
			else  {
				// zoom into screen
				ptCam.eye.z += 30 * e.getCount();
			}

			break;
		case MouseEvent.PRESS:
			ptCam.dragOriginX = x;
			ptCam.dragOriginY = y;
			break;

		case MouseEvent.RELEASE:
			break;

		case MouseEvent.DRAG:
			distX = x - ptCam.dragOriginX;
			distY = y - ptCam.dragOriginY;
			
			if (e.isShiftDown())  {
				// if shift drag w/either key, move the background
				pt.bg.moveRect(distX,distY);	
			}
			else if (e.getButton() == PConstants.LEFT) {
				// rotation based on distance from start of drag 
				distX = distX / pApp.width * PConstants.TWO_PI;
				distY = distY / pApp.height * PConstants.TWO_PI;

				// holding down alt rotates around the y axis only
				if (e.isAltDown()) {
					ptCam.mouseRotation.z += 
							(PApplet.abs(distX) > PApplet.abs(distY)) ?  distX : distY; 						 						
				}
				// otherwise rotate 
				else {
					ptCam.mouseRotation.y += distX;
					ptCam.mouseRotation.x += distY;
				}
			}
			else if (e.getButton() == PConstants.RIGHT) {
				// scale translation so we move a little faster as the camera moves away.
				float m = (ptCam.eye.z <= ptCam.DEFAULT_CAMERA_DISTANCE) ? 1 : ptCam.eye.z / ptCam.DEFAULT_CAMERA_DISTANCE;
				ptCam.eye.x += m * distX; 
				ptCam.eye.y += m * distY; 									
			}
			break;				
		case MouseEvent.CLICK:
			break;
		case MouseEvent.MOVE:
			if (pt.pixelInfoEnabled()) {
				; // TODO - display per-pixel tooltip
			}
			break;
		}	
	*/
	}
}