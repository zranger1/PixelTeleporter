## 3D Refactor



#### Graphics
- Everything is 3D! No more 2D renderer, always use P3D for drawing.

- Refactor all renderer parameters and include setShader(), selectShader() or something like that for
choosing LED model appearance.   All the light scattering, blending and diffusion stuff needs to stay.

- Need to be able to render into a PGraphic object so the user can apply the PixelTeleporter visual output as
a texture. (This returned object can be the thing with associated "real" lights.)
- (maybe) Retained mode geometry to speed rendering.  Not sure how much this will actually
 help in real world cases.
- (maybe) add up to 8 "real" lights by averaging LED output at corners. 

- change from first frame special case to pt.addObject(obj) in setup.  This simplifies the draw code, and helps move
towards retained mode.   We can also potentially associate an object transform matrix with each object, separate from
the global transform.   And maybe a per-object renderer/shader too!


#### Power analysis (TODO)
- basically a secondary render pass over the received frame data)
- calculate power use in arbitrary power units
- convert to amps/watts/whatever using a table of LED-type appropriate conversions
- API returns a couple of numbers -  instantatneous power usage, peak power usage, average over entire run.
- example needs to present this graphically.

#### Things to document
- Pause/Resume functionality
- New, improved camera UI (TODO - need to steal some stuff from the Unreal Engine editor)

#### New networking features: 
- Over-the-air protocol converted to multicast so several devices can receive at once.  
- (We can do this with ExpanderVerse too -- it needs to be able to send Output Expander Protocol over the air.)
- Receive from Expanderverse TODO - (use Output Expander protocol format).
- This will enable the eventual Pixel Teleporter send/receive devices that will all run Output Expander protocol so we don't lose information from RGBW or high bit depth LEDs.
 (which we would if we went with e1.31 or Artnet or other stuff like that)

- Receive from e1.31 (TODO - just for compatibility's sake)
- receive preview frames for up to 1000 pixels from Pixelblaze, so no gear needed!

#### API Changes
- SetRotationRate() calls should specify rate in seconds per complete rotation.
- LEDObject - wraps the old list of pixels in an object with its own transform and rendering stuff.
- pt.addObject() - add LEDObject to display list

#### UI Changes
- Tab - freeze/unfreeze
- X - display axis markers

#### Known Issues
- Need shader switching API - should allow user specified shaders w/paths (maybe not this version)
- Consolidate element sizing mechanisms.   Maybe we need "element size" and "element spacing" instead of "weight". Gotta think about this b/c weight 
could act as a proxy for how far the light from a single LED can travel.  (but so can element size)
- 2D-ish SMD model
- strip model.  Maybe ribbon with curves?
- add shaders for panel/diffuser objects (even, edge lit, center lit, diffusion %/ focus)

# Things to Do
shader selection API -- maybe setRenderThingie(MODEL,CUSTOM), then setCustomShader("fragment","vertex");