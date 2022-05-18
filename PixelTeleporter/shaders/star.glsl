
// original https://www.shadertoy.com/view/Xs33R2


uniform float weight;
uniform float ambient;
uniform float time;

varying vec4 vertColor;
varying vec2 center;
varying vec2 outPos;


out vec4 glFragColor;

/*
"Magic particles" by Emmanuel Keller aka Tambako - December 2015
License Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License.
Contact: tamby@tambako.ch
*/

// ToDo:
// * Min/Max hue
// * Min/max saturation
// * Hue time factor
// * Main Particle color
// * Sparkling particles
// * Gravity
// * Motion blur

#define twopi 6.28319

// Particle intensity constants
const float part_int_div = 40000.;                            // Divisor of the particle intensity. Tweak this value to make the particles more or less bright
const float part_int_factor_min = 0.1;                        // Minimum initial intensity of a particle
const float part_int_factor_max = 3.2;                        // Maximum initial intensity of a particle
const float part_spark_min_int = 0.25;                        // Minimum sparkling intensity (factor of initial intensity) of a particle
const float part_spark_max_int = 0.88;                        // Minimum sparkling intensity (factor of initial intensity) of a particle
const float part_spark_min_freq = 2.5;                        // Minimum sparkling frequence in Hz of a particle
const float part_spark_max_freq = 6.0;                        // Maximum sparkling frequence in Hz of a particle
const float part_spark_time_freq_fact = 0.35;                 // Sparkling frequency factor at the end of the life of the particle
const float mp_int = 12.;                                     // Initial intensity of the main particle
const float dist_factor = 0.75;                               // Distance factor applied before calculating the intensity
const float ppow = 2.3;                                      // Exponent of the intensity in function of the distance

// Particle star constants
const vec2 part_starhv_dfac = vec2(9., 0.32);                 // x-y transformation vector of the distance to get the horizontal and vertical rays
const float part_starhv_ifac = 0.225;                         // Intensity factor of the horizontal and vertical rats
const vec2 part_stardiag_dfac = vec2(13., 0.61);              // x-y transformation vector of the distance to get the diagonal rays
const float part_stardiag_ifac = 0.19;                        // Intensity factor of the diagonal rays
const float part_starmin = 0.1;                               // overall intensity of all rays.

// Main function to draw particles, outputs the rgb color.
float dist;
vec3 drawParticles(vec2 uv, float timedelta) {  
    vec3 pcol = vec3(0.);

    vec2 ppos = vec2(0.);
    dist = distance(uv, ppos);
	
    // Draws the eight-branched star
    // Horizontal and vertical branches
    vec2 uvppos = uv - ppos;
    float distv = distance(uvppos*part_starhv_dfac + ppos, ppos);
    float disth = distance(uvppos*part_starhv_dfac.yx + ppos, ppos);
	
    // Diagonal branches
    vec2 uvpposd = 0.7071*vec2(dot(uvppos, vec2(1., 1.)), dot(uvppos, vec2(1., -1.)));
    float distd1 = distance(uvpposd*part_stardiag_dfac + ppos, ppos);
    float distd2 = distance(uvpposd*part_stardiag_dfac.yx + ppos, ppos);
	
    // Calculate star intensity
    float pint1 = 1./(dist*dist_factor + 0.015) + part_starhv_ifac/(disth*dist_factor + part_starmin) + 
	              part_starhv_ifac/(distv*dist_factor + part_starmin) + part_stardiag_ifac/(distd1*dist_factor + part_starmin) + 
				  part_stardiag_ifac/(distd2*dist_factor + part_starmin);
        
    if (part_int_factor_max*pint1>6.) {
      float pint = part_int_factor_max*(pow(pint1, ppow)/part_int_div)*mp_int;
      pcol += vertColor.xyz*pint;
    }
    return pcol;
}

void main(void)
{
    vec2 uv = (outPos / weight);  // scale range to (-1, 1)
    
    vec3 pcolor = drawParticles(uv,time);
       
    // We're 
    glFragColor = vec4(pcolor, clamp(1.0-dist,0.,1.));
}
