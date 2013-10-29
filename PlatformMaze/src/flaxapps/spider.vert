varying vec3 N;
varying float NdotL;
uniform vec4 view_direction;
uniform vec4 view_position;
uniform float furLength;


void main()
{

	gl_TexCoord[0] = gl_MultiTexCoord0;
	vec3 normal, lightDir;
	
	
	N = normalize(gl_Normal);
	lightDir = vec3(view_direction);
 

    NdotL = dot(N,lightDir);
    vec4 ps = gl_Vertex * 10;
        
	vec3 p = ps.xyz + (gl_Normal * furLength);
	
	gl_Position = gl_ModelViewProjectionMatrix * vec4(p,1);
}