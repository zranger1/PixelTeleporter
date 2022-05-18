## 3D Refactor

#### Graphics
- Everything is 3D! No more 2D renderer, always use P3D for drawing.

- Refactor all renderer parameters and include setShader(), selectShader() or something like that for
choosing LED model appearance.   All the light scattering, blending and diffusion stuff needs to stay.

- Need to be able to render into a PGraphic object so the user can apply the PixelTeleporter visual output as
a texture.
- (maybe) Retained mode geometry to speed rendering.  Not sure how much this will actually
 help in real world cases.
- (maybe) add up to 8 "real" lights by averaging LED output at corners. 

#### Power analysis (TODO)
- basically a secondary render pass over the received frame data)
- calculate power use in arbitrary power units
- convert to amps/watts/whatever using a table of LED-type appropriate conversions
- API returns a couple of numbers -  instantatneous power usage, peak power usage, average over entire run.
- example needs to present this graphically.

#### Things to document
- Pause/Resume functionality
- New, improved camera UI (TODO)

#### New networking features: 
- Over-the-air protocol converted to multicast so several devices can receive at once.  
- (We can do this with ExpanderVerse too -- it needs to be able to send Output Expander Protocol over the air.)
- Receive from Expanderverse TODO - (use Output Expander protocol format).
- This will enable the eventual Pixel Teleporter send/receive devices that will all run Output Expander protocol so we don't lose information from RGBW or high bit depth LEDs.
 (which we would if we went with e1.31 or Artnet or other stuff like that)

- Receive from e1.31 (TODO - just for compatibility's sake)

#### API Changes
- SetRotationRate() calls should specify rate in seconds per complete rotation.