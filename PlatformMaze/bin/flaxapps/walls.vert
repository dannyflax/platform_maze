varying vec2 vTexCoord;
varying vec3 N;
varying float NdotL;
uniform vec4 view_direction;
uniform vec4 view_position;
void main()
{


		vTexCoord = gl_MultiTexCoord0.st;
		vec3 normal, lightDir;
	
		vec3 ve = vec3(gl_Vertex); 
	
		vec3 vp = vec3(view_position);
		     
        float max_brightness = 0.4;
        float light_radius = 100.0;

		N = normalize(gl_Normal);
		lightDir = normalize(vec3(view_direction));
		
		
		//NdotL is the shade of the pixel, a value between 0 and 1 (0 darkest, 1 lightest)
		//NdotL is calculated using two factors, the dot product between of the light vector 
		//the normal to the surface and the distance^10. 
		//Dot product allows for multidimensional surfaces, while the distance function
		//gives the light the fading effect, and gives it a nice round look at the corners
		 
		float distance = sqrt(pow(vp.x-ve.x,2)+pow(vp.y-ve.y,2)+pow(vp.z-ve.z,2));
		NdotL = pow(distance/light_radius,10);
        NdotL = 1/NdotL; 
       
        NdotL = clamp(0.0005*NdotL * clamp(dot(N, lightDir), 0.5,1.0),0.01,max_brightness);
		




	
	gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
}