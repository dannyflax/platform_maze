varying vec3 N;
varying float NdotL;
uniform vec4 dude_color;
uniform sampler2D textureMap;
void main()
{

	
	vec4 color = dude_color;
	color.a = texture2D(textureMap, gl_TexCoord[0].st).a;
	
	gl_FragColor = color;
        
}