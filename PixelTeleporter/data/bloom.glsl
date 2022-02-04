// Bloom and gamma (de)-adjustment filter designed for use
// with Pixel Teleporter.  Since Pixel Teleporter has
// a built in indirect light scattering model, we don't need
// the blur filter usually associated with bloom effects.

#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

#define PROCESSING_TEXTURE_SHADER

uniform sampler2D texture;
uniform vec2 texOffset;

varying vec4 vertColor;
varying vec4 vertTexCoord;

uniform float overdrive;
uniform float gamma;

void main() {
    float luminance;
    
    // get current pixel from frame buffer
    vec4 c = texture2D(texture, vertTexCoord.st); // * vertColor;   

    // un-gamma correct
    c.rgb = pow(c.rgb,vec3(gamma));
    
	vec3 luminanceVector = vec3(0.2125, 0.7154, 0.0721);
    luminance = dot(luminanceVector, c.xyz);
    
    // overdrive:0.75 is about normal for CG bloom...
    c.rgb = c.rgb + (c.rgb * (overdrive * luminance)); 
     
    // set the current pixel color
    gl_FragColor = c;
}