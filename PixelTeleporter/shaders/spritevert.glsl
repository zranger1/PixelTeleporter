uniform mat4 projection;
uniform mat4 modelview;

uniform float weight;

attribute vec4 position;
attribute vec4 color;
attribute vec2 offset;

varying vec4 vertColor;
varying vec2 texCoord;

void main() {
  vec4 pos = modelview * position;
  vec4 clip = projection * pos;

  gl_Position = clip + projection * vec4(offset, 0, 0);

  texCoord = 0.5 + vec2(offset / weight);
  texCoord.y = 1.-texCoord.y;

  vertColor = color;
}