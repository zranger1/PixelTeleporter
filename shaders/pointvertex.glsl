// billboarding 3D point shader for Processing
// 2022 ZRanger1

uniform mat4 projectionMatrix;
uniform mat4 modelviewMatrix;
 
uniform vec4 viewport;
uniform int perspective; 
 
attribute vec4 position;
attribute vec4 color;
attribute vec2 offset;

varying vec4 vertColor;
varying vec2 center;
varying vec2 outPos;

void main() {
  vec4 pos =  modelviewMatrix * position;
  vec4 clip = projectionMatrix * pos;

  gl_Position = clip + vec4(offset,0.,0.);
  
  vertColor = color;
  center = clip.xy;
  outPos = offset;  
}

