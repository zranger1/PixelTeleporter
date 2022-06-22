/*
class Plot {
  float xSize,ySize,xCenter,yCenter;
  int start,end;
  float xMin,xMax,xRange;
  float yMin,yMax,yRange;
  float xScale,yScale;
  float xOffset, yOffset;
  float startMarkerX;
  boolean tagsEnabled;
  Table data;
  PImage mark,startMark;     

  static final float legendFontSize = 32;
  static final float auxFontSize = 24;
  static final float axisFontSize = 18;
  static final float legendBlockWidth = 450;
  static final float tagBlockWidth = 600;
  static final float nXAxisTicks = 5;
  
  int markerTop;
  int markerBottom; 
  int markerHeight;
  int redrawWidth = 10;
  
  int gridStart;
  int gridDelta;
   
  final color graphColor = color(129,137,212,255);
  final color gridColor = color(250,182,44,255);
  final color lineColor = color(255,0,0,255);
  final color pointColor = color(212,67,38,255);
  final color bgColor = color(0,12,60,255);
  final color startColor = color(0,212,12,255);  
  float lastLevel;  
  int lastIndex;
  int lastX,lastY;  
  int bri = 255;
  
  String tagStr = "";  
  
  Plot(float xs,float ys,int s,int e, Table d) {
    this.setup(xs,ys,s,e,d);
  }
  
  Plot() {
  }
  
  void setup(float xs,float ys,int s,int e, Table d) {
    start = s; end = e; data = d;
    xCenter = width / 2;
    yCenter = height / 2;
    xSize = xs; 
    ySize = ys;
    xOffset = 100;
    yOffset = 250;
    lastLevel = s-1;
    tagsEnabled = true;
   
    getDataRange();
    
    markerTop = (int) floor(legendFontSize * 3.5);
    markerBottom = (int) (height - (legendFontSize * 1.25)); 
    markerHeight = markerBottom - markerTop;
    markerBottom = markerBottom - 1;
    
    gridStart = (int) floor(getScreenY(150));
    gridDelta = gridStart - (int)floor(getScreenY(200));    
  }
  
  void getDataRange() {
    xMin = (float) data.getInt(start,"year");
    xMax = (float) data.getInt(end,"year");
    xRange = abs(xMax - xMin);   
    xScale = (xSize-(2 * xOffset)) / xRange;      
    
    yMin = 9999; yMax = -9999;
    for (int i = start; i <= end;i++) {
      float v = data.getFloat(i,1); 
      if (v > yMax) yMax = v;
      if (v < yMin) yMin = v;
    }  
    yRange = yMax - yMin;
    yScale =  (0.68 * ySize) / yRange; 
    yOffset = yMin+(yRange / 2);

    // set a virtual x coordinate for each data entry
    if (data.getColumnCount() != 4) data.addColumn("x");
    for (int i = start; i <= end;i++) {
      data.setFloat(i,3, xScale * (float) (data.getFloat(i,0) - xMin)); 
    }          
  }
  
  float getScreenY(float lev) {
    lev = yScale * (yOffset - lev);
    return floor(max(lev + ySize / 2,-10));      
  }       
    
  void drawXAxis() {
    float n,i,x;
    textAlign(CENTER,BOTTOM);
    for (i = 0; i < nXAxisTicks; i++) {
      n = xMin + xRange * (i / (nXAxisTicks-1));  // target year
      x = xScale * (n - xMin) + xOffset;  // x axis position
      line(x,markerBottom,x,height-auxFontSize);
      
      if (n < 0) n = n - xMax;  // convert from BC to BP
      text(String.format("%.0f",n),x,height); 
    }      
  }
   
  void drawStartMarker() {
    
    if (drawStartBar) {
      if (currentCycleStart < this.start) return;
         
      startMarkerX = xOffset+data.getFloat(currentCycleStart,3);    
      startMark = get((int) startMarkerX-6,markerTop,12,markerHeight);
      stroke(startColor);
      strokeWeight(1);
      line(startMarkerX,markerTop,startMarkerX,markerBottom);
    }
  }
  
  void clearStartMarker() {
    if (startMarkChanged && !drawStartBar) {       
      try {
        println("Clearing");
        image(startMark,(int)startMarkerX-6,markerTop);      
      } catch (NullPointerException e) {;}
    }
}    
    
  void drawPlot() {
      float x1,y1,x2,y2;
      x1 = y1 = 0; // make java happy
                  
      fill(bgColor);
      noStroke();
      rect(0,0,width,height);
      textAlign(LEFT,BOTTOM);
      textSize(axisFontSize);     
           
      // ppm mile markers
      stroke(gridColor);
      fill(gridColor);      
      strokeWeight(1);
      
      float y = gridStart;
      line(0,y,xSize,y);  
      text("150 ppm",10,y);      

      y -= gridDelta;
      line(0,y,xSize,y);  
      text("200 ppm",10,y);
      
      y -= gridDelta;
      line(0,y,xSize,y);  
      text("250 ppm",10,y);
      
      y -= gridDelta;
      line(0,y,xSize,y); 
      text("300 ppm",10,y);     
      
      y -= gridDelta;
      line(0,y,xSize,y); 
      text("350 ppm",10,y);    
      
      y -= gridDelta;
      line(0,y,xSize,y);  
      text("400 ppm",10,y);  
      
      drawXAxis();
    
      strokeWeight(3);
      stroke(graphColor); 
    
      for (int row = start; row <= end; row++) {    
         float x = xOffset+data.getFloat(row,3);    
         float level = getScreenY(data.getFloat(row,1));
         if (row == start) {
           x1 = x; y1 = level;
         }
         x2 = x; y2 = level;
         line(x1,y1,x2,y2);
         x1 = x2; y1 = y2;
      }
 
     // initialization done here because we know it happens after we've read and preprocessed
     // the data set.
     lastIndex = this.start;
     lastX = (int) xOffset; // + (xScale * (float) (data.getInt(this.start,0) - xMin));   
     lastY = 0; //getScreenY(data.getFloat(this.start,1));   
     mark = get((int) xOffset,markerTop,12,markerHeight-2);     
  }
  
  void drawLegend() {
   String levelStr,dateStr;
   
   dateStr = client.getYearString();
   levelStr = String.format("%s,  %d ppm",dateStr,(int) client.getPPM());   
   
   fill(bgColor); 
   textSize(legendFontSize);
   textAlign(CENTER,TOP);   
   float y = legendFontSize / 4;
   noStroke();
   
   // clear the text areas for the legend and the speed indicator
   rect(xCenter-(legendBlockWidth /2),y,legendBlockWidth, legendFontSize+3);  
 
   // display legend text
   fill(255);
   text(levelStr,xCenter,0);   
  }    
  
  // draw informational tag on screen
  void drawTag(String tag) {
    
    if (tagsEnabled && tag.length() > 0) {
      tagStr = tag; 
      bri = 255;    
      
    }
    else {
      bri = max(0,bri-14);  
    }
    
    if (bri > 0) {     
      float y = legendFontSize * 1.5;  
      
      fill(bgColor);
      noStroke();
      textSize(auxFontSize);
      rect(xCenter-(tagBlockWidth / 2),y*1.1, tagBlockWidth, legendFontSize);
            
      fill(color(250,182,44,bri));
      text(tagStr,xCenter, y);       
    }
  }  
  
  void redrawPlot(int ndx,float x) {
      float x1,y1,x2,y2;
      float xSize = 14;
      x1 = y1 = 0; // make java happy
                                  
      // ppm mile markers
      stroke(gridColor);
      fill(gridColor);      
      strokeWeight(1);   
      
      x -= 7;
      x2 = x + xSize;
            
      float y = gridStart; 
      line(x,y,x2,y);  
      
      y -= gridDelta;
      line(x,y,x2,y);        
      
      y -= gridDelta;      
      line(x,y,x2,y);  
      
      y -= gridDelta;          
      line(x,y,x2,y); 
     
      y -= gridDelta;          
      line(x,y,x2,y); 
      
      y -= gridDelta;  
      line(x,y,x2,y);  
     
      strokeWeight(3);
      stroke(graphColor); 
      
      // wider redraw needed for more closely spaced data
      redrawWidth = ((index > 1024) && (index <= graphBreakEntry))? 50 : 10;
           
      int end = min(ndx + redrawWidth,this.end);
      ndx = max(ndx - redrawWidth, this.start);      
      
      x1 = xOffset+data.getFloat(ndx,3);  
      y1 = getScreenY(data.getFloat(ndx,1));    
      
      for (int row = ndx+1; row <= end; row++) {   
         x2 = xOffset + data.getFloat(row,3);         
         y2 = getScreenY(data.getFloat(row,1));
         line(x1,y1,x2,y2);
         x1 = x2; y1 = y2;
      }   
  }  
     
  void drawMarker(float x,float y) {
      strokeWeight(1);
      stroke(lineColor);
      mark = get((int) x-6,markerTop,12,markerHeight);
      line(x,markerTop,x,markerBottom);
      noFill();
      circle(x,y,10);
  }
  
  void clearMarker(float x,float y) {
      image(mark,(int)x-6,markerTop);
  }  
  
  void draw(int index) {
      float level;
      
      // if not in free mode, get precise level value from data set.
      // (the data stream from the server uses a moving average value.)      
      if (!client.freeMode()) {
         level = data.getFloat(index,1);
      }
      else {
         level = client.getPPM();
      }
       
      if (level == lastLevel) return;
      
      lastLevel = level;     
      int x = (int) (xOffset + data.getFloat(index,3)); 
      int y = (int) constrain(getScreenY(level),markerTop-6,markerBottom-6);
     
      clearMarker(lastX,lastY);
      clearStartMarker();
      drawStartMarker();
      drawMarker(x,y);
      
      lastIndex = index;
      lastX = x; lastY = y;
     
      drawLegend();
      drawTag(client.getTag());
  }
}

*/
