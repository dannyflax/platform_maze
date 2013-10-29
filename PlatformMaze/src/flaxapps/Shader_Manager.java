package flaxapps;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import javax.media.opengl.GL2;


public class Shader_Manager {
	public int init(String name,GL2 gl) throws IOException {
		int v = gl.glCreateShader(GL2.GL_VERTEX_SHADER);
		int f = gl.glCreateShader(GL2.GL_FRAGMENT_SHADER);
		System.out.println(name.concat(".vert"));
		String vsrc = readFromStream(JOGL2Nehe10World3D.class .getResourceAsStream(name.concat(".vert")));
		gl.glShaderSource(v, 1, new String[] { vsrc }, (int[]) null, 0);
		gl.glCompileShader(v);

		String fsrc = readFromStream(JOGL2Nehe10World3D.class.getResourceAsStream(name.concat(".frag")));
		gl.glShaderSource(f, 1, new String[] { fsrc }, (int[]) null, 0);
		gl.glCompileShader(f);

		int shaderprogram = gl.glCreateProgram();
		gl.glAttachShader(shaderprogram, v);
		gl.glAttachShader(shaderprogram, f);
		gl.glLinkProgram(shaderprogram);
		gl.glValidateProgram(shaderprogram);

		return shaderprogram;
		//gl.glUseProgram(shaderprogram);

		

	}
	

	public static String readFromStream(InputStream ins) throws IOException {
		if (ins == null) {
			throw new IOException("Could not read from stream.");
		}
		StringBuffer buffer = new StringBuffer();
		Scanner scanner = new Scanner(ins);
		try {
			while (scanner.hasNextLine()) {
				buffer.append(scanner.nextLine() + "\n");
			}
		} finally {
			scanner.close();
		}

		return buffer.toString();
	}
}

