package pixelTeleporter.library;

import java.util.LinkedList;

// generic rendering interface.  Takes a list of ScreenLED objects
// to be rendered.
public interface LEDRenderer {
	void setControl(RenderControl ctl, float value);
	void render(LinkedList <ScreenLED> obj);
}