// use this walled cube mapper with PixelTeleporter instead of 
// the default pixelblaze mapper.  This one allows you to specify
// all three dimensions and is wired in a very simple linear 
// fashion.
function (pixelCount) {
  var map = [];
  dimX = 10;
  dimY = 10;
  dimZ = 10;
  
  index = 0;
  
// top
  for (row = 0; row < dimX; row++) {
    for (col = 0; col < dimZ; col++) {
      map.push([row,dimY,col]);
    }
  }
  
// front
  for (row = 0; row < dimY; row++) {
    for (col = 0; col < dimX; col++) {
      map.push([col,row,dimZ]);      
    }
  }

//right side
  for (row = 0; row < dimZ; row++) {
    for (col = 0; col < dimY; col++) {
      map.push([dimX,col,row]);          
    }
  }

// back
  for (row = 0; row < dimY; row++) {
    for (col = 0; col < dimX; col++) {
      map.push([col,row,-1]);          
    }
  }

// left side
  for (row = 0; row < dimZ; row++) {
    for (col = 0; col < dimY; col++) {
      map.push([-1,col,row]);      
    }
  }

// bottom
  for (row = 0; row < dimX; row++) {
    for (col = 0; col < dimZ; col++) {
      map.push([row,-1,col]);      
    }
  }
  return map;
}