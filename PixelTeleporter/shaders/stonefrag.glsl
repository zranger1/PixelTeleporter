//#version 420

// original https://www.shadertoy.com/view/4tBSW3

uniform float time;
uniform float weight;

varying vec4 vertColor;
varying vec2 center;
varying vec2 outPos;

out vec4 glFragColor;

// License Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License.
//Created by 834144373  2015/11/5

float noise(vec3 p) //from Las of the "Mercury"
{
  vec3 i = floor(p);
  vec4 a = dot(i, vec3(1., 57., 21.)) + vec4(0., 57., 21., 78.);
  vec3 f = cos((p-i)*acos(-1.))*(-.5)+.5;
  a = mix(sin(cos(a)*a),sin(cos(1.+a)*(1.+a)), f.x);
  a.xy = mix(a.xz, a.yw, f.y);
  return mix(a.x, a.y, f.z);
}

//..........................................................
vec3 roty(vec3 p,float angle){
  float s = sin(angle),c = cos(angle);
    mat3 rot = mat3(
      c, 0.,-s,
        0.,1., 0.,
        s, 0., c
    );
    return p*rot; 
}
//............................................................

///////////////////////////////////
//raymaching step I for normal obj
///////////////////////////////////
float obj(vec3 pos){
    pos -= vec3(0.,0.13,0.);
    float  n = noise(pos);
    // float res =  length(max(abs(pos)-vec3(0.8,0.4,0.4)-n,0.0))-0.1;
  
    float res = length(pos)-(1.- 0.3*n);
    return res;
}

//raymarching step I
//find object
float disobj(vec3 pointpos,vec3 dir){
    float dd = 1.;
    float d = 0.;
    for(int i = 0;i<30;++i){
      vec3 sphere = pointpos + dd*dir;
          d = obj(sphere);
        if(d<0.02)break;
      dd += d;
    }
    return dd;
}

//////raymarching step II for detal obj
/////////////////////////////////////////////////////////////
///////here is form guil https://www.shadertoy.com/view/MtX3Ws
vec2 csqr( vec2 a )  { return vec2( a.x*a.x - a.y*a.y, 2.*a.x*a.y  ); }
float objdetal(in vec3 p) {
      float res = 0.;
    vec3 c = p;
      for (int i = 0; i < 10; ++i) {
        p =1.7*abs(p)/dot(p,p) -0.8;
        p.yz= csqr(p.yz);
        p=p.zxy;
        res += exp(-20. * abs(dot(p,c)));        
  }
  return res/2.;
}
////////////////////////////////////////////////////
//raymarching step II 
vec4 objdensity(vec3 pointpos,vec3 dir,float finaldis){
  vec4 color;
    float den = 0.;
    vec3 sphere = pointpos + finaldis*dir;
    float dd = 0.;
        for(int j = 0;j<45;++j){
            vec4 col;
            col.a = objdetal(sphere);
      
            float c = col.a/200.;
            col.rgb = vertColor.rgb;
            col.rgb *= c; //col.a;
            col.rgb *= float(j)/20.;
            dd = 0.01*exp(-2.*col.a);
            sphere += dd*dir;

            color += col*0.8;
        }
    return color*4.5;
}
/////////////////////////////////////////
/////////////////////////////////////////
void main(void)
{
  vec2 uv = (outPos / weight)*2.;  // scale range to (-1, 1)
  
  
    ///////////////////
    vec3 dir = normalize(vec3(uv,2.));
      dir = roty(dir,time);
    ///////////////////
    vec3 campos = vec3(0.,0.,-2.8);
      campos = roty(campos,time);
    //raymarching step I
    float finaldis = disobj(campos,dir);
    vec4 col = vec4(0.0);
    if(finaldis < 40.){
        col = objdensity(campos,dir,finaldis);
    }
   
    glFragColor = vec4(col);
}
