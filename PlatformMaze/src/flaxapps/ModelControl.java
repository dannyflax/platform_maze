package flaxapps;

import static javax.media.opengl.GL.GL_TRIANGLE_FAN;
import static javax.media.opengl.GL2.GL_QUAD_STRIP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.media.opengl.GL2;

//The shader control class.
//loads and starts/stops shaders.


class Vertex{
	float x, y, z;
	public Vertex(float a,float b,float c){
		x=a;
		y=b;
		z=c;
	}
}

class Face{
	public ArrayList<Vertex> vertices;
	public ArrayList<Vertex> normals;
	public ArrayList<Vertex> texts;
	public int size;
	public Face(ArrayList<Vertex> vs, ArrayList<Vertex> ns, ArrayList<Vertex> tx){
		vertices = vs;
		normals = ns;
		texts = tx;
		size = vs.size();
		
	}
}



public class ModelControl
{
	//Class I designed to process .obj files into OpenGL commands
	
	//Verticies, indicies (faces), normals, and texture coordinates are all stored upon loading to be processed immediately during drawing
	ArrayList<Vertex> verts = new ArrayList<Vertex>();
	ArrayList<Vertex> norms = new ArrayList<Vertex>();
	ArrayList<Vertex> texts = new ArrayList<Vertex>();
	ArrayList<Vertex> backupverts = new ArrayList<Vertex>();
	public ArrayList<Face> faces = new ArrayList<Face>();
	
	public int vcount;
	public int icount;
	
	public void rotateZ(double angle){
		//Nifty little function that rotates all of the vertex coordinates
		//Old coordinates are stored as well to make this function easily reversible
		backupverts = verts;
		for(int i = 0; i<verts.size(); i++){
			Vertex v = verts.get(i);
			Vertex vf = new Vertex(0,0,0);
			vf.x = (float) (v.x*Math.cos(angle) + v.y*-Math.sin(angle));
			vf.y = (float) (v.x*Math.sin(angle) + v.y*Math.cos(angle));
			
			verts.set(i, vf);
		}
	}
	
	public void restore(){ //Function to restore verticies after rotation
		verts = backupverts;
	}
	
	public ArrayList<Vertex> drawModel(Vertex pos, GL2 gl, float rangle){
		//Drawing code, uses the model data to send GL commands to the given OpenGL object
		
		ArrayList<Vertex> ret = new ArrayList<Vertex>();
		
	      for(int i = 0; i<faces.size(); i++){
	 		 Face f = faces.get(i);
	 		 if(f.size == 3 || f.size== 4){
		 		 if(f.size == 3){
		 			 gl.glBegin(GL_TRIANGLE_FAN);
		 			 //System.out.println("triangles");
		 		 }
		 		 if(f.size == 4){
		 			gl.glBegin(GL_QUAD_STRIP);
		 			//System.out.println("quads");
		 		 }
		 		//System.out.println("Face: ");
		 		 for(int j = 0; j<f.size; j++){
		 			 
		 			 
		 			if(f.texts.size()!=0){
			 			 flaxapps.Vertex t = f.texts.get(j);
			 			// gl.glTexCoord3f(1.0f, 1.0f, 1.0f);
			 			gl.glTexCoord2f(t.x, 1.0f-t.y);
			 			}
		 			
		 			if(f.normals.size() != 0)
		 			{
		 			 flaxapps.Vertex n = f.normals.get(j);
		 			
		 			Vertex vf = new Vertex(0,0,0);
					vf.x = (float) (n.x*Math.cos(rangle) + n.z*Math.sin(rangle));
					vf.z = (float) (n.x*-Math.sin(rangle) + n.z*Math.cos(rangle));
					vf.y = n.y;
		 			n = vf;
                                        gl.glNormal3f(n.x, n.y, n.z); 
		 			}
		 			
		 			 flaxapps.Vertex v = f.vertices.get(j);
		 			
		 			Vertex vf = new Vertex(0,0,0);
					vf.x = (float) (v.x*Math.cos(rangle) + v.z*Math.sin(rangle));
					vf.z = (float) (v.x*-Math.sin(rangle) + v.z*Math.cos(rangle));
					vf.y = v.y;
		 			v = vf;
		 			 
		 			
		 			
		 			
		 			gl.glVertex3f(pos.x + v.x, pos.y + v.y, pos.z + v.z);
		 			ret.add(new Vertex(pos.x + v.x, pos.y + v.y, pos.z + v.z));
		 			 
		 		 }
		 		 
		 		 gl.glEnd();
		 		 
	 		 }
	 	 }
	      
	     return ret;
	      
	}
	
 public void printFaces(){
	 for(int i = 0; i<faces.size(); i++){
		 Face f = faces.get(i);
		 System.out.println("Face " + i + ": Size = " + f.size + " Vertices: \n");
		 for(int j = 0; j<f.size; j++){
			 Vertex v = f.vertices.get(j);
			 System.out.println("X: " + v.x + " Y: " + v.y + " Z: " + v.z + "\n");
		 }
		 
		 
	 }
	 
 }
	
 public void loadModelData(String name) throws IOException
 {
	 
	 ArrayList<String> lines = new ArrayList<String>();
     
        
         
         
	 
            InputStream in;
            in = JOGL2Nehe10World3D.class.getResource(name).openStream();
                 BufferedReader br = new BufferedReader(new InputStreamReader(in));
                 String strLine;
                 //Read File Line By Line
                 while ((strLine = br.readLine()) != null)   {
                         // Print the content on the console
                         lines.add(strLine);
                 }
             
    
	 
	 for(int i = 0; i<lines.size(); i++){
		 
		 
		 String l = lines.get(i);
		 
		// System.out.println(l);
		 if(l.startsWith("v ")){
			 String[] k =  l.split(" ");
			 if(k.length == 4)
			 {
				 verts.add(new Vertex(Float.parseFloat(k[1]),Float.parseFloat(k[2]),Float.parseFloat(k[3])));
				 vcount+=1;
			 }
		 }
		 else if(l.startsWith("f")){
			// System.out.println(l);
			 
			 String[] k =  l.split(" ");
			 ArrayList<Vertex> faceVerts = new ArrayList<Vertex>();
			ArrayList<Vertex> faceVerts2 = new ArrayList<Vertex>();
			 ArrayList<Vertex> faceNorms = new ArrayList<Vertex>();
				ArrayList<Vertex> faceNorms2 = new ArrayList<Vertex>();
				ArrayList<Vertex> faceTexts = new ArrayList<Vertex>();
				ArrayList<Vertex> faceTexts2 = new ArrayList<Vertex>();
			 
			 if(k.length == 4){
				
				 icount+=3;
				 
				 faceVerts.add(verts.get(Integer.parseInt(k[1].split("//")[0].split("/")[0]) - 1));
				 faceVerts.add(verts.get(Integer.parseInt(k[2].split("//")[0].split("/")[0]) - 1));
				 faceVerts.add(verts.get(Integer.parseInt(k[3].split("//")[0].split("/")[0]) - 1));
				 
				 if(k[1].split("//").length==2){
				
					 faceNorms.add(norms.get(Integer.parseInt(k[1].split("//")[1]) - 1));
					 faceNorms.add(norms.get(Integer.parseInt(k[2].split("//")[1]) - 1));
					 faceNorms.add(norms.get(Integer.parseInt(k[3].split("//")[1]) - 1));
					
				 }
				 else if(k[1].split("/").length == 3){
					 
					 faceTexts.add(texts.get(Integer.parseInt(k[1].split("/")[1]) - 1));
					 faceTexts.add(texts.get(Integer.parseInt(k[2].split("/")[1]) - 1));
					 faceTexts.add(texts.get(Integer.parseInt(k[3].split("/")[1]) - 1));
					 
					 faceNorms.add(norms.get(Integer.parseInt(k[1].split("/")[2]) - 1));
					 faceNorms.add(norms.get(Integer.parseInt(k[2].split("/")[2]) - 1));
					 faceNorms.add(norms.get(Integer.parseInt(k[3].split("/")[2]) - 1));
				 }
				 
				 
				 
			 }
			 if(k.length == 5){
				 icount+=4;
				 faceVerts.add(verts.get(Integer.parseInt(k[1].split("//")[0].split("/")[0]) - 1));
				 faceVerts.add(verts.get(Integer.parseInt(k[2].split("//")[0].split("/")[0]) - 1));
				 faceVerts.add(verts.get(Integer.parseInt(k[3].split("//")[0].split("/")[0]) - 1));
				 
				 faceVerts2.add(verts.get(Integer.parseInt(k[1].split("//")[0].split("/")[0]) - 1));
				 faceVerts2.add(verts.get(Integer.parseInt(k[3].split("//")[0].split("/")[0]) - 1));
				 faceVerts2.add(verts.get(Integer.parseInt(k[4].split("//")[0].split("/")[0]) - 1));
				
				 if(k[1].split("//").length==2){
					 faceNorms.add(norms.get(Integer.parseInt(k[1].split("//")[1].split("/")[0]) - 1));
					 faceNorms.add(norms.get(Integer.parseInt(k[2].split("//")[1].split("/")[0]) - 1));
					 faceNorms.add(norms.get(Integer.parseInt(k[3].split("//")[1].split("/")[0]) - 1));
					 
					 faceNorms2.add(norms.get(Integer.parseInt(k[1].split("//")[1].split("/")[0]) - 1));
					 faceNorms2.add(norms.get(Integer.parseInt(k[3].split("//")[1].split("/")[0]) - 1));
					 faceNorms2.add(norms.get(Integer.parseInt(k[4].split("//")[1].split("/")[0]) - 1));
				 }
				 else if(k[1].split("/").length == 3){
					 
					 faceNorms.add(norms.get(Integer.parseInt(k[1].split("/")[2]) - 1));
					 faceNorms.add(norms.get(Integer.parseInt(k[2].split("/")[2]) - 1));
					 faceNorms.add(norms.get(Integer.parseInt(k[3].split("/")[2]) - 1));
					 faceNorms2.add(norms.get(Integer.parseInt(k[1].split("/")[2]) - 1));
					 faceNorms2.add(norms.get(Integer.parseInt(k[3].split("/")[2]) - 1));
					 faceNorms2.add(norms.get(Integer.parseInt(k[4].split("/")[2]) - 1));
					 
					 faceTexts.add(texts.get(Integer.parseInt(k[1].split("/")[2]) - 1));
					 faceTexts.add(texts.get(Integer.parseInt(k[2].split("/")[2]) - 1));
					 faceTexts.add(texts.get(Integer.parseInt(k[3].split("/")[2]) - 1));
					 faceTexts2.add(texts.get(Integer.parseInt(k[1].split("/")[2]) - 1));
					 faceTexts2.add(texts.get(Integer.parseInt(k[3].split("/")[2]) - 1));
					 faceTexts2.add(texts.get(Integer.parseInt(k[4].split("/")[2]) - 1));
				 }
				 
				 
				 faces.add(new Face(faceVerts2,faceNorms2,faceTexts2));
			}
			
			 faces.add(new Face(faceVerts,faceNorms,faceTexts));
			 
		 }
		 else if(l.startsWith("vn")){
			//System.out.println("asdfsa");
			 String[] k =  l.split(" ");
			 if(k.length == 4)
				 norms.add(new Vertex(Float.parseFloat(k[1]),Float.parseFloat(k[2]),Float.parseFloat(k[3])));
	
			 
			 
		 }
		 
		 else if(l.startsWith("vt")){
				// System.out.println("asdfsa");
				 String[] k =  l.split(" ");
				 if(k.length == 3)
					 texts.add(new Vertex(Float.parseFloat(k[1]),Float.parseFloat(k[2]),0.0f));
		
				 
				 
			 }
		 
		 
	 }
	 
	 
	 
	 
 }

}


/*
 * 
 *   gl.glColor3f(0.0f, 1.0f, 0.0f);
      
      for(int i = 0; i<mc.faces.size(); i++){
 		 Face f = mc.faces.get(i);
 		 if(f.size == 3 || f.size== 4){
	 		 if(f.size == 3){
	 			 gl.glBegin(GL_TRIANGLES);
	 			 //System.out.println("triangles");
	 		 }
	 		 if(f.size == 4){
	 			gl.glBegin(GL_QUADS);
	 			//System.out.println("quads");
	 		 }
	 		 for(int j = 0; j<f.size; j++){
	 			 Vertex v = f.vertices.get(j);
	 			 //System.out.println(v.z);
	 			System.out.println("X: " + v.x + " Y: " + v.y + " Z: " + v.z + "\n");
	 			 gl.glVertex3f(v.x * 100.0f, v.y * 100.0f, v.z * 100.0f);
	 		 }
	 		 
	 		 gl.glEnd();
	 		 
 		 }
 	 }
      
 * 
 * 
 */
