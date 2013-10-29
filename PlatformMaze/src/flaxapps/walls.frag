varying vec2 vTexCoord;
uniform sampler2D mytext;
varying vec3 N;
varying float NdotL;
void main()
{
	gl_FragColor = NdotL * texture2D(mytext,vTexCoord);

}