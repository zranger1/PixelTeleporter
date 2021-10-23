PShape buildLightMap(int mapSize,float LEDSize) {
  PGraphics pg;
  int x,y;
  float dx,dy,center,dist,maxDist;
  float alpha;
  float LEDCore = LEDSize * 0.4;
  println("Building light map...");
  
  center = mapSize / 2;
  maxDist = sqrt(center * center);
  
  pg = createGraphics(mapSize,mapSize);
  
  pg.beginDraw();
  pg.noStroke();
  pg.noFill();
 
  for (y = 0; y < mapSize; y++) {
    for (x = 0; x < mapSize; x++) {

      dx = (float) x - center;
      dy = (float) y - center;
      dist = sqrt(dx * dx + dy * dy);
      if (dist <= LEDCore) {
        dist = alpha = 255;
      }
      else {
        dist = 1-(dist / maxDist);  // normalized distance
        alpha = 150; //  200 * pow(dist,2);
        dist = 255 * pow(dist, 3);
      }
      pg.set(x,y,color(dist,dist,dist,alpha));
    }
  
  }
  pg.endDraw();
  
  PImage tex = pg.get();
  PShape sh = texturedPlane(tex,mapSize,mapSize, 0);  
  sh.disableStyle();
  
  println("Light map ready.");
  return sh; 
}

PShape buildHighlightMap(int mapSize) {
  PGraphics pg;
  int x,y;
  float dx,dy,center,dist,maxDist;
  float alpha;
  println("Building highlight map...");
  
  center = mapSize / 2;
  maxDist = sqrt(center * center);
  
  pg = createGraphics(mapSize,mapSize);
  
  pg.beginDraw();
  pg.noStroke();
  pg.noFill();
 
  for (y = 0; y < mapSize; y++) {
    for (x = 0; x < mapSize; x++) {

      dx = (float) x - center;
      dy = (float) y - center;
      dist = sqrt(dx * dx + dy * dy);
      dist = 1-(dist / maxDist);  // normalized distance
      alpha = 255 * pow(dist,2);
      if (alpha < 10) {
        alpha = 0;
        dist = 0; 
      } else {
        dist = 255 * pow(dist, 1.75);
      }
      pg.set(x,y,color(dist,dist,dist,alpha));
    }  
  }
  pg.endDraw();
  
  PImage tex = pg.get();
  PShape sh = texturedPlane(tex,mapSize,mapSize, 0);  
  sh.disableStyle();
  sh.translate(-4,-4,0);
  
  println("Highlight map ready.");
  return sh; 
}


// set the coordinates of the individual LEDs in our data 
// transfer "texture"
void initLEDCoords(float xSize,float ySize) {
  int tx = -width / 2;
  int ty = -height / 2;
  int n = 0;
  for (float y = 0; y < ySize; y++) {
    for (float x = 0; x < xSize; x++) {
      float x1 = (x + 1) / (xSize + 1);
      float y1 = (y + 1) / (xSize + 1);
      leds[n][0] = tx + (int) (x1 * width);
      leds[n][1] = ty + (int) (y1 * width);
      n++;
    }
  }
}

// textured x/y background rect of specified size, centered at origin facing viewer
PShape backgroundPlane(float x, float y, float z) {
  PImage tex = loadImage("plasticbg.png");
  return texturedPlane(tex,x,y,z);
}

// textured x/y background rect of specified size, centered at origin facing viewer
PShape texturedPlane(PImage tex,float x, float y, float z) {
  textureMode(NORMAL); 
  x /= 2; y /=2; z /=2;  
  PShape sh = createShape();
  sh.beginShape(QUADS); 
  sh.noStroke();
  sh.shininess(100);
  sh.texture(tex);   
  
  sh.vertex(-x, -y,  z, 0, 0);
  sh.vertex( x, -y,  z, 1, 0);
  sh.vertex( x,  y,  z, 1, 1);
  sh.vertex(-x,  y,  z, 0, 1);
  
  sh.endShape(CLOSE);
  return sh;
}


// Our model LED -- a thin rectangular box, but with no bottom face
PShape LEDPad(float xs, float ys, float zs) {
  textureMode(NORMAL);
  PImage tex = loadImage("texLED.jpg");    
  xs /= 2; ys /=2; zs /=2;  
  PShape sh = createShape();
  sh.beginShape(QUADS);
//  sh.setStroke(true);
  sh.shininess(1000);
  sh.specular(255);
  sh.texture(tex);
  
  // +Z "front" face
  sh.vertex(-xs, -ys,  zs, 0, 0);
  sh.vertex( xs, -ys,  zs, 1, 0);
  sh.vertex( xs,  ys,  zs, 1, 1);
  sh.vertex(-xs,  ys,  zs, 0, 1);  
    
  // +Y "bottom" face
  sh.vertex(-xs,  ys,  zs, 0, 0);
  sh.vertex( xs,  ys,  zs, 1, 0);
  sh.vertex( xs,  ys, -zs, 1, 1);
  sh.vertex(-xs,  ys, -zs, 0, 1);  

  // -Y "top" face
  sh.vertex(-xs, -ys, -zs, 0, 0);
  sh.vertex( xs, -ys, -zs, 1, 0);
  sh.vertex( xs, -ys,  zs, 1, 1);
  sh.vertex(-xs, -ys,  zs, 0, 1);

  // +X "right" face
  sh.vertex( xs, -ys,  zs, 0, 0);
  sh.vertex( xs, -ys, -zs, 1, 0);
  sh.vertex( xs,  ys, -zs, 1, 1);
  sh.vertex( xs,  ys,  zs, 0, 1);

  // -X "left" face
  sh.vertex(-xs, -ys, -zs, 0, 0);
  sh.vertex(-xs, -ys,  zs, 1, 0);
  sh.vertex(-xs,  ys,  zs, 1, 1);
  sh.vertex(-xs,  ys, -zs, 0, 1);
  
  sh.endShape(CLOSE);
  sh.disableStyle();
  return sh;
}

// Generic textured box of specified size, centered at the origin,
// in case we need it later...
PShape TexturedBox(PImage tex,float xs, float ys, float zs) {   
  xs /= 2; ys /=2; zs /=2;
  textureMode(NORMAL);
  PShape sh = createShape();
  sh.beginShape(QUADS);
  sh.texture(tex);
 
  // +Z "front" face
  sh.vertex(-xs, -ys,  zs, 0, 0);
  sh.vertex( xs, -ys,  zs, 1, 0);
  sh.vertex( xs,  ys,  zs, 1, 1);
  sh.vertex(-xs,  ys,  zs, 0, 1);

  // -Z "back" face
  sh.vertex( xs, -ys, -zs, 0, 0);
  sh.vertex(-xs, -ys, -zs, 1, 0);
  sh.vertex(-xs,  ys, -zs, 1, 1);
  sh.vertex( xs,  ys, -zs, 0, 1);

  // +Y "bottom" face
  sh.vertex(-xs,  ys,  zs, 0, 0);
  sh.vertex( xs,  ys,  zs, 1, 0);
  sh.vertex( xs,  ys, -zs, 1, 1);
  sh.vertex(-xs,  ys, -zs, 0, 1);

  // -Y "top" face
  sh.vertex(-xs, -ys, -zs, 0, 0);
  sh.vertex( xs, -ys, -zs, 1, 0);
  sh.vertex( xs, -ys,  zs, 1, 1);
  sh.vertex(-xs, -ys,  zs, 0, 1);

  // +X "right" face
  sh.vertex( xs, -ys,  zs, 0, 0);
  sh.vertex( xs, -ys, -zs, 1, 0);
  sh.vertex( xs,  ys, -zs, 1, 1);
  sh.vertex( xs,  ys,  zs, 0, 1);

  // -X "left" face
  sh.vertex(-xs, -ys, -zs, 0, 0);
  sh.vertex(-xs, -ys,  zs, 1, 0);
  sh.vertex(-xs,  ys,  zs, 1, 1);
  sh.vertex(-xs,  ys, -zs, 0, 1);

  sh.endShape(CLOSE);
  return sh;
}
