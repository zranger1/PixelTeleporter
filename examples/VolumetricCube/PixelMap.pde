/* 
// Volumetric cube pixel map generator for Pixelblaze.
// Functionally identical to the default volumetric cube
// mapper, except that you can specify all three dimensions
// if you like. Either will work for actual cubic cubes.
//
// To use, paste the javascript below into the Pixel Mapper in the
// Pixelblaze's web UI.

function (pixelCount) {
  xDim = 10;
  yDim = 10;
  zDim = 10;
  var map = []
  
  for (z = 0; z < zDim; z++) {
    for (y = 0; y < yDim; y++) {
      for (x = 0; x < xDim; x++) {
        map.push([x,y,z]);
      }  
    }
  }

  return map
} 

*/
