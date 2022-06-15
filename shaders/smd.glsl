// 3D Point shader to emulate LED displays
// Part of Pixel Telelporter
// 2020-2022 ZRanger1

uniform float weight;
uniform float ambient;
uniform float falloff;
uniform float time;

varying vec4 vertColor;
varying vec2 center;
varying vec2 outPos;

#define SCALE 1.
#define S2 (1-SCALE)
#define RECT_SIZE (0.25 * SCALE)
#define CORE_SIZE (0.23 * SCALE)
#define ELEMENT_SIZE (0.1 * SCALE)


// simple 2D box SDF with rounded corners
// b = width/height of box
// r = radius of corners
float sdRoundBox(vec2 p,float b,float r ) {
    vec2 q = abs(p)-b+r;
    return min(max(q.x,q.y),0.0) + length(max(q,0.0)) - r;
}

void main() {
  float radius = weight / 2.0;
  vec2 uv = (outPos / weight)*2.;  // scale range to (-1, 1)  
  
  // calculate normalized distance from center
  float d = clamp((radius - length(outPos))/radius,0.,1.);    
  float d2 = pow(d,falloff); 
  
  // calculate light falloff based on distance from center
  vec4 color = vec4(vertColor.xyz,d2);  
  
  // draw the slightly rounded outer box
  float dist = max(0.0,-sdRoundBox(uv,RECT_SIZE,0.04));
  if (dist > 0.0) {
    color.xyz += ambient;
	color.a = max(0.9,color.a);
  } 
  
  // and the circular lens 
  dist = step(1.-CORE_SIZE,d);
  color.xyz += ((d2*vertColor.xyz + ambient) * 0.5) * dist;
 

  // and brighten color in center area enough to wash away the outer square at
  // high brightnesses
  dist =  smoothstep(0.,1.-ELEMENT_SIZE,d*d*d*d);
  color.xyz += vertColor.xyz * 1.2 * dist; 
  color.a += dist;

   
  gl_FragColor = color;
}
