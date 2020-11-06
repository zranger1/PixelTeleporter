/* 
// Sphere mapping function for Pixelblaze
// Maps pixels on the surface of the sphere to latitude/longitude,
// which work neatly as x and y coordinates.
// To use, paste the javascript below into the Pixel Mapper in the
// Pixelblaze's web UI.

function (pixelCount) {
 var phi = (Math.sqrt(5)+1)/2 - 1; 
 var ga = phi*2*Math.PI;           
 var lat,lon;
 var map = [];
     
  for (var i = 1; i <= pixelCount; ++i) {
    lon = ga*i;    
    lon /= 2*Math.PI;
    lon -= Math.floor(lon);
    lon *= 2*Math.PI
 
    lat = Math.asin(-1 + 2*i/pixelCount);
    map.push([lon,lat]);
  } 

  return map;
}    

*/
