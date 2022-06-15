// 3D Point shader to emulate LED displays
// Part of Pixel Telelporter
// 2020-2022 ZRanger1

#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform float weight;
uniform float ambient;
uniform float time;

varying vec4 vertColor;
varying vec2 center;
varying vec2 outPos;

#define RING_SIZE 0.8    //0.375
#define CORE_SIZE 0.75     //0.35
#define ELEMENT_SIZE 0.995
#define MIN_BRIGHTNESS ambient
#define SPECULAR_BRI (MIN_BRIGHTNESS * 1.5)
#define OFF_THRESHOLD 0.005 

#define LINE_WIDTH 0.012


void main() {
  vec3 maxBri;
  float diameter = weight / 2.0;
  
  // calculate normalized distance from center
  float d1 = clamp((diameter - length(outPos))/diameter,0.,1.);  
  float dist = d1 * d1;
  
  // calculate normalized radius of specular highlight
  float d2 = diameter - (diameter * 0.9);
  d2 = clamp((d2-length(outPos - vec2(-d2, d2)))/d2,0.,1.);  
  
  vec3 ledBaseColor = vec3(MIN_BRIGHTNESS);
  
  // first, linear falloff based on distance from center
  vec4 color = vertColor; 
  color.a = dist;
  
  // now our plastic LED bulb, with fake specular highlighting
  color += vec4(ledBaseColor,step(CORE_SIZE,d1) * dist);
  color.a += MIN_BRIGHTNESS * smoothstep(LINE_WIDTH,0.0,abs(RING_SIZE-d1)); 
  color += (SPECULAR_BRI * smoothstep(0.,ELEMENT_SIZE,d2));  
  
  // brighten color slightly in center.  
  color += vertColor * 1.2 * smoothstep(0.,ELEMENT_SIZE,dist*dist);    
   
  gl_FragColor = color;
}
