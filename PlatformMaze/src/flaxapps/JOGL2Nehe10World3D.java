package flaxapps;

import static java.awt.event.KeyEvent.VK_A;
import static java.awt.event.KeyEvent.VK_B;
import static java.awt.event.KeyEvent.VK_C;
import static java.awt.event.KeyEvent.VK_D;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_E;
import static java.awt.event.KeyEvent.VK_ESCAPE;
import static java.awt.event.KeyEvent.VK_F;
import static java.awt.event.KeyEvent.VK_F1;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_S;
import static java.awt.event.KeyEvent.VK_T;
import static java.awt.event.KeyEvent.VK_UP;
import static java.awt.event.KeyEvent.VK_V;
import static java.awt.event.KeyEvent.VK_W;
import static java.awt.event.KeyEvent.VK_X;
import static java.awt.event.KeyEvent.VK_Z;
import static javax.media.opengl.GL.GL_COLOR_BUFFER_BIT;
import static javax.media.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static javax.media.opengl.GL.GL_DEPTH_TEST;
import static javax.media.opengl.GL.GL_LEQUAL;
import static javax.media.opengl.GL.GL_NICEST;
import static javax.media.opengl.GL.GL_ONE;
import static javax.media.opengl.GL.GL_SRC_ALPHA;
import static javax.media.opengl.GL2ES1.GL_PERSPECTIVE_CORRECTION_HINT;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_AMBIENT;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_DIFFUSE;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHT1;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_POSITION;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_SMOOTH;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_SPECULAR;
import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_MODELVIEW;
import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_PROJECTION;

import java.awt.AWTException;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.color.ColorSpace;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLException;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;
import javax.swing.JFrame;

import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;

/**
 * NeHe Lesson #10: Loading And Moving Through A 3D World
 * 
 * 'b': toggle blending on/off 't': switch to the next texture filters (nearest,
 * linear, mipmap) Page-up/Page-down: player looks up/down, scene rotates in
 * negative x-axis up-arrow/down-arrow: player move in/out, posX and posZ become
 * smaller left-arrow/right-arrow: player turns left/right (scene rotates
 * right/left)
 */

public class JOGL2Nehe10World3D implements GLEventListener, KeyListener {

	private static String TITLE = "NeHe Lesson #10: Loading And Moving Through A 3D World";
	private static final int CANVAS_WIDTH = 640; // width of the drawable
	private static final int CANVAS_HEIGHT = 480; // height of the drawable
	private static final int FPS = 100; // animator's target frames per second
	final static JFrame frame = new JFrame();
	ModelControl mc;
	ModelControl mc2;

	ModelControl w2;
	ModelControl floor;
	ModelControl top;

	ArrayList<flaxapps.Vertex> collisionVerts = new ArrayList<flaxapps.Vertex>();

	Monster mydude;
	public boolean controlled = true;

	int frm = 0;

	boolean up = false;
	boolean left = false;
	boolean right = false;
	boolean down = false;

	private GLU glu; // for the GL Utility
	int t;
	// The world
	Point c_mpos;
	Point p_mpos;
	Sector sector;
	int cmap_id;

	public float lookUpMax = (float) -45.0;
	public float lookUpMin = (float) 45.0;

	private boolean blendingEnabled; // Blending ON/OFF

	float[] cOffset = { 0.0f, 0.0f, 0.0f, 1.0f };

	Shader_Manager sm = new Shader_Manager();

	int shader1;
	int shader2;

	// x and z position of the player, y is 0
	public float posX = 0;
	public float posZ = 0;
	public float posY = 7;
	boolean asdf = false;
	public float headingY = 0; // heading of player, about y-axis
	public float lookUpAngle = 0.0f;

	private flaxapps.Vertex pos = new flaxapps.Vertex(0, 0, 0);
	private float moveIncrement = 1.0f;
	// private float turnIncrement = 1.5f; // each turn in degree
	private float lookUpIncrement = 1.0f;

	// private float walkBias = 0;
	// private float walkBiasAngle = 0;

	private Texture[] textures = new Texture[3];
	private int currTextureFilter = 0; // Which Filter To Use
	private String textureFilename = "/images/wall.jpg";
	static GLCanvas canvas;

	// Texture image flips vertically. Shall use TextureCoords class to retrieve
	// the
	// top, bottom, left and right coordinates.

	// private float textureLeft;
	// private float textureRight;

	/** The entry main() method */
	public static void main(String[] args) {
		// Create the OpenGL rendering canvas
		canvas = new GLCanvas(); // heavy-weight GLCanvas

		canvas.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));
		JOGL2Nehe10World3D renderer = new JOGL2Nehe10World3D();
		canvas.addGLEventListener(renderer);

		// For Handling KeyEvents
		canvas.addKeyListener(renderer);
		canvas.setFocusable(true);

		canvas.requestFocus();

		// Create a animator that drives canvas' display() at the specified FPS.
		final FPSAnimator animator = new FPSAnimator(canvas, FPS, true);

		// Create the top-level container frame
		// Swing's JFrame or AWT's Frame
		frame.setUndecorated(true);
		frame.getContentPane().add(canvas);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				// Use a dedicate thread to run the stop() to ensure that the
				// animator stops before program exits.
				new Thread() {
					@Override
					public void run() {
						animator.stop(); // stop the animator loop
						System.exit(0);
					}
				}.start();
			}
		});
		frame.setTitle(TITLE);
		frame.pack();

		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

		frame.setVisible(true);
		animator.start(); // start the animation loop
	}

	// ------ Implement methods declared in GLEventListener ------

	/**
	 * Called back immediately after the OpenGL context is initialized. Can be
	 * used to perform one-time initialization. Run only once.
	 */

	public static BufferedImage componentToImage(Component component,
			Rectangle region) throws IOException {
		BufferedImage img = new BufferedImage(component.getWidth(),
				component.getHeight(), BufferedImage.TYPE_INT_ARGB_PRE);
		Graphics2D g = (Graphics2D) img.getGraphics();
		g.setColor(component.getForeground());
		g.setFont(component.getFont());
		component.paintAll(g);
		if (region == null) {
			region = new Rectangle(0, 0, img.getWidth(), img.getHeight());
		}
		return img.getSubimage(region.x, region.y, region.width, region.height);
	}

	@Override
	public void init(GLAutoDrawable drawable) {

		pos.x = 0.0f;
		pos.y = 0.0f;
		pos.z = 0.0f;

		GL2 gl = drawable.getGL().getGL2(); // get the OpenGL graphics context
		glu = new GLU(); // get GL Utilities
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f); // set background (clear) color
		gl.glClearDepth(1.0f); // set clear depth value to farthest
		gl.glEnable(GL_DEPTH_TEST); // enables depth testing
		gl.glDepthFunc(GL_LEQUAL); // the type of depth test to do
		gl.glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST); // best
																// perspective
																// correction
		gl.glShadeModel(GL_SMOOTH); // blends colors nicely, and smoothes out
									// lighting

		// Read the world
		try {
			shader1 = sm.init("walls", gl);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		try {
			shader2 = sm.init("spider", gl);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// Load the texture image
		try {
			// Use URL so that can read from JAR and disk file.
			BufferedImage image = ImageIO.read(this.getClass().getResource(
					textureFilename));

			// Create a OpenGL Texture object
			textures[0] = AWTTextureIO.newTexture(GLProfile.getDefault(),
					image, false);
			// Nearest filter is least compute-intensive
			// Use nearer filter if image is larger than the original texture
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER,
					GL.GL_NEAREST);
			// Use nearer filter if image is smaller than the original texture
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER,GL.GL_NEAREST);

			// For texture coordinates more than 1, set to wrap mode to
			// GL_REPEAT for
			// both S and T axes (default setting is GL_CLAMP)
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S,
					GL.GL_REPEAT);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T,
					GL.GL_REPEAT);

		} catch (GLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// glimage img = new glimage("/images/mud.png");

		// Blending control
		gl.glColor4f(1.0f, 1.0f, 1.0f, 0.5f); // Brightness with alpha
		// Blending function For translucency based On source alpha value
		gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE);

		AnimationHolder vase = new AnimationHolder(
				"/flaxapps/personmodels/person", 20);

		mydude = new Monster(vase, new flaxapps.Vertex(0.0f, 0.0f, -200.0f));
		mydude.stop();
		mydude.rangle = 0;
		mydude.speed = 3.0f;

		w2 = new ModelControl();
		mc2 = new ModelControl();
		floor = new ModelControl();
		top = new ModelControl();

		
		try {
			w2.loadModelData("walls2.obj");
		} catch (IOException ex) {
			Logger.getLogger(JOGL2Nehe10World3D.class.getName()).log(
					Level.SEVERE, null, ex);
		}
		try {
			floor.loadModelData("floor.obj");
		} catch (IOException ex) {
			Logger.getLogger(JOGL2Nehe10World3D.class.getName()).log(
					Level.SEVERE, null, ex);
		}
		try {
			top.loadModelData("top.obj");
		} catch (IOException ex) {
			Logger.getLogger(JOGL2Nehe10World3D.class.getName()).log(
					Level.SEVERE, null, ex);
		}
		
		this.setUp2DText(gl, "/images/wall.jpg");

		// Set up the lighting for Light-1
		// Ambient light does not come from a particular direction. Need some
		// ambient
		// light to light up the scene. Ambient's value in RGBA
		float[] lightAmbientValue = { 1.0f, 0.0f, 0.0f, 1.0f };
		// Diffuse light comes from a particular location. Diffuse's value in
		// RGBA
		float[] lightDiffuseValue = { 1.0f, 0.0f, 0.0f, 1.0f };
		// Diffuse light location xyz (in front of the screen).
		float lightDiffusePosition[] = { 1.0f, 1.0f, 0.0f, 1.0f };

		float[] lightSpecularValue = { 1.0f, 1.0f, 1.0f, 1.0f };

		gl.glLightfv(GL_LIGHT1, GL_SPECULAR, lightSpecularValue, 0);
		gl.glLightfv(GL_LIGHT1, GL_AMBIENT, lightAmbientValue, 0);
		gl.glLightfv(GL_LIGHT1, GL_DIFFUSE, lightDiffuseValue, 0);
		gl.glLightfv(GL_LIGHT1, GL_POSITION, lightDiffusePosition, 0);

		// gl.glEnable(GL_COLOR_MATERIAL);
		// gl.glColorMaterial(GL_FRONT, GL_AMBIENT_AND_DIFFUSE);

		// gl.glMaterialfv(GL_FRONT, GL_SPECULAR, specReflection);
		// gl.glMateriali(GL_FRONT, GL_SHININESS, 56);

		// gl.glEnable(GL_LIGHT1); // Enable Light-1
		// gl.glEnable(GL_LIGHTING); // But disable lighting
		// isLightOn = false;

		Robot r = null;
		try {
			r = new Robot();
		} catch (AWTException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		if (r != null) {
			r.mouseMove(frame.getWidth() / 2, frame.getHeight() / 2);
		}

		BufferedImage cursorImg = new BufferedImage(16, 16,
				BufferedImage.TYPE_INT_ARGB);

		// Create a new blank cursor.
		Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
				cursorImg, new Point(0, 0), "blank cursor");
		frame.setCursor(blankCursor);
		p_mpos = MouseInfo.getPointerInfo().getLocation();

	}

	/**
	 * Call-back handler for window re-size event. Also called when the drawable
	 * is first set to visible.
	 */
	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height) {
		GL2 gl = drawable.getGL().getGL2(); // get the OpenGL 2 graphics context

		if (height == 0)
			height = 1; // prevent divide by zero
		float aspect = (float) width / height;

		// Set the view port (display area) to cover the entire window
		gl.glViewport(0, 0, width, height);

		// Setup perspective projection, with aspect ratio matches viewport
		gl.glMatrixMode(GL_PROJECTION); // choose projection matrix
		gl.glLoadIdentity(); // reset projection matrix
		glu.gluPerspective(45.0, aspect, 0.1, 100.0); // fovy, aspect, zNear,
														// zFar

		// Enable the model-view transform
		gl.glMatrixMode(GL_MODELVIEW);
		gl.glLoadIdentity(); // reset
	}

	/**
	 * Called back by the animator to perform rendering.
	 */

	class AABB {
		flaxapps.Vertex c; // center point
		float[] r; // halfwidths

		public AABB(float[] ar, flaxapps.Vertex ac) {
			c = ac;
			r = ar;
		}

	};

	public boolean testAABBAABB(AABB a, AABB b) {
		if (Math.abs(a.c.x - b.c.x) > (a.r[0] + b.r[0]))
			return false;
		if (Math.abs(a.c.y - b.c.y) > (a.r[1] + b.r[1]))
			return false;
		if (Math.abs(a.c.z - b.c.z) > (a.r[2] + b.r[2]))
			return false;
		return true;
	}

	public void draw2doorRoom(flaxapps.Vertex l, double a, GL2 gl) {
		gl.glColor3d(1.0, 0.0, 0.0);
		collisionVerts.addAll(floor.drawModel(l, gl, 0));
		gl.glColor3d(0.0, 1.0, 0.0);
		
		collisionVerts.addAll(w2.drawModel(l, gl, (float) a));
		w2.restore();
		gl.glColor3d(0.0, 0.0, 1.0);
		top.drawModel(l, gl, 0);
		
		
	}

	public void captureScreen(String fileName) throws Exception {
		/*
		 * Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		 * Rectangle screenRectangle = new Rectangle(screenSize); Robot robot =
		 * new Robot(); Component component = canvas;
		 * 
		 * BufferedImage img = new BufferedImage(component.getWidth(),
		 * component.getHeight(), BufferedImage.TYPE_INT_ARGB_PRE); Graphics2D g
		 * = (Graphics2D) img.getGraphics();
		 * g.setColor(component.getForeground());
		 * g.setFont(component.getFont());
		 * 
		 * 
		 * ImageIO.write(img,"png",new File(fileName));
		 */
	}

	public boolean hitsWall(flaxapps.Vertex pos, float[] hls) {

		float[] whls = { 0.0f, 0.0f, 0.0f };
		AABB person = new AABB(hls, pos);
		for (int i = 0; i < collisionVerts.size(); i++) {

			AABB wall = new AABB(whls, collisionVerts.get(i));
			if (testAABBAABB(person, wall)) {
				return true;
			}

		}
		return false;
	}

	@Override
	public void display(GLAutoDrawable drawable) {

		float[] hls = { 2.0f, 2.0f, 2.0f };

		if (up) {
			posX -= (float) Math.sin(Math.toRadians(headingY)) * moveIncrement;
			posZ -= (float) Math.cos(Math.toRadians(headingY)) * moveIncrement;

			if (this.hitsWall(new flaxapps.Vertex(posX, posY + 10.0f, posZ),
					hls)) {
				posX += (float) Math.sin(Math.toRadians(headingY))
						* moveIncrement;
				posZ += (float) Math.cos(Math.toRadians(headingY))
						* moveIncrement;
			}

		}
		if (down) {
			// Player move out, posX and posZ become bigger
			posX += (float) Math.sin(Math.toRadians(headingY)) * moveIncrement;
			posZ += (float) Math.cos(Math.toRadians(headingY)) * moveIncrement;

			if (this.hitsWall(new flaxapps.Vertex(posX, posY + 10.0f, posZ),
					hls)) {
				posX -= (float) Math.sin(Math.toRadians(headingY))
						* moveIncrement;
				posZ -= (float) Math.cos(Math.toRadians(headingY))
						* moveIncrement;
			}
		}
		if (left) {
			// Player move out, posX and posZ become bigger
			posX -= (float) Math.sin(Math.toRadians(headingY + 90.0))
					* moveIncrement;
			posZ -= (float) Math.cos(Math.toRadians(headingY + 90.0))
					* moveIncrement;
			if (this.hitsWall(new flaxapps.Vertex(posX, posY + 10.0f, posZ),
					hls)) {
				posX += (float) Math.sin(Math.toRadians(headingY + 90.0))
						* moveIncrement;
				posZ += (float) Math.cos(Math.toRadians(headingY + 90.0))
						* moveIncrement;
			}
		}
		if (right) {
			// Player move out, posX and posZ become bigger
			posX -= (float) Math.sin(Math.toRadians(headingY - 90.0))
					* moveIncrement;
			posZ -= (float) Math.cos(Math.toRadians(headingY - 90.0))
					* moveIncrement;

			if (this.hitsWall(new flaxapps.Vertex(posX, posY + 10.0f, posZ),
					hls)) {
				posX += (float) Math.sin(Math.toRadians(headingY - 90.0))
						* moveIncrement;
				posZ += (float) Math.cos(Math.toRadians(headingY - 90.0))
						* moveIncrement;
			}
		}

		if (controlled) {
			c_mpos = MouseInfo.getPointerInfo().getLocation();
			int xdif = c_mpos.x - p_mpos.x;
			int ydif = c_mpos.y - p_mpos.y;
			// System.out.println(xdif);
			// System.out.println(frame.getLocation().x);
			lookUpAngle += (ydif / 5.0);
			
			if ((lookUpAngle <= lookUpMax || lookUpAngle >= lookUpMin)) {
				lookUpAngle -= (ydif / 5.0);
			}

			headingY -= (xdif / 5.0);

			Robot r = null;
			try {
				r = new Robot();
			} catch (AWTException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			if (r != null) {

				r.mouseMove(frame.getWidth() / 2, frame.getHeight() / 2);

			}

		}

		t = (t + 1);
		GL2 gl = drawable.getGL().getGL2(); // get the OpenGL 2 graphics context
		gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear color
																// and depth
																// buffers
		gl.glLoadIdentity(); // reset the model-view matrix

		gl.glEnable(GL_SMOOTH);

		// Blending control
		gl.glEnable(GL.GL_BLEND);
		
		
		// Rotate up and down to look up and down
		gl.glRotatef(lookUpAngle, 1.0f, 0, 0);

		// Player at headingY. Rotate the scene by -headingY instead (add 360 to
		// get a
		// positive angle)
		gl.glRotatef(360.0f - headingY, 0, 1.0f, 0);

		// Player is at (posX, 0, posZ). Translate the scene to (-posX, 0,
		// -posZ)
		// instead.
		// float[] po = {posX,-(posY + (-walkBias - 0.25f)),posZ};
		gl.glEnable(GL.GL_TEXTURE_2D);
		gl.glBindTexture(GL.GL_TEXTURE_2D, 11);
		
		gl.glTranslatef(-posX, -posY, -posZ);

		gl.glUseProgram(0);
		int txt1 = gl.glGetUniformLocation(shader2, "textureMap");
		gl.glUniform1f(txt1, 11);
		
		float x = (float) Math.sin(Math.toRadians(headingY));
		float z = (float) Math.cos(Math.toRadians(headingY));
		float y = (float) Math.sin(Math.toRadians(lookUpAngle));
		int ps = gl.glGetUniformLocation(shader2, "view_direction");
		gl.glUniform4f(ps, x, y, z, 1.0f);
		int vps = gl.glGetUniformLocation(shader2, "view_position");
		gl.glUniform4f(vps, posX, posY, posZ, 1.0f);
		int clor = gl.glGetUniformLocation(shader2, "dude_color");

		
		
		
		gl.glUniform4f(clor, 0.0f, 0.0f, 1.0f, 1.0f);
		
		// System.out.println( Math.toDegrees(a) );
		// System.out.println("R" + Math.toRadians(90));
		// mydude.act(gl,this);
		
		
		/*
		int fl = gl.glGetUniformLocation(shader2,"furLength");
		for(int i = 1; i<=20; i++){
			gl.glUniform1f(fl, (float) (i*.10));
			mc2.drawModel(new flaxapps.Vertex(0, 0, -5), gl, 0);
		}
		
		*/
		
		
		
		gl.glUseProgram(shader1);

		int txt = gl.glGetUniformLocation(shader1, "mytext");
		gl.glUniform1f(txt, 11);

		ps = gl.glGetUniformLocation(shader1, "view_direction");
		gl.glUniform4f(ps, x, y, z, 1.0f);
		vps = gl.glGetUniformLocation(shader1, "view_position");
		gl.glUniform4f(vps, posX, posY, posZ, 1.0f);

		gl.glDisable(GL.GL_BLEND);
		
		collisionVerts = new ArrayList<flaxapps.Vertex>();

		
		  for(int num = 0; num<2; num+=2){
			this.draw2doorRoom(new
			  flaxapps.Vertex(51.0f * num ,0.0f,0.0f), 0, gl);
			  this.draw2doorRoom(new flaxapps.Vertex(51.0f * num,0.0f,-51.0f),
			  -Math.PI, gl);
			  
			  this.draw2doorRoom(new flaxapps.Vertex((51.0f * (num + 1))
			  ,0.0f,-51.0f), Math.PI/2, gl); 
			  this.draw2doorRoom(new
			  flaxapps.Vertex((51.0f * (num + 1)),0.0f,0.0f), -Math.PI/2, gl);
		  
		  }
		 

		gl.glFlush();

		// gl.glEnable(GL_BLEND);
		// gl.glDisable(GL_DEPTH_TEST);
		gl.glBlendFuncSeparate(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA,
				GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_COLOR);

		// Select a texture based on filter
		// textures[currTextureFilter].bind(gl);
		/*
		 * // Process each triangle for (int i = 0; i < sector.triangles.length;
		 * i++) { gl.glBegin(GL_TRIANGLES); gl.glNormal3f(0.0f, 0.0f, 1.0f); //
		 * Normal pointing out of screen
		 * 
		 * // need to flip the image float textureHeight = textureTop -
		 * textureBottom; float u, v;
		 * 
		 * u = sector.triangles[i].vertices[0].u; v =
		 * sector.triangles[i].vertices[0].v * textureHeight - textureBottom;
		 * gl.glTexCoord2f(u, v);
		 * gl.glVertex3f(sector.triangles[i].vertices[0].x,
		 * sector.triangles[i].vertices[0].y,
		 * sector.triangles[i].vertices[0].z);
		 * 
		 * u = sector.triangles[i].vertices[1].u; v =
		 * sector.triangles[i].vertices[1].v * textureHeight - textureBottom;
		 * gl.glTexCoord2f(u, v);
		 * gl.glVertex3f(sector.triangles[i].vertices[1].x,
		 * sector.triangles[i].vertices[1].y,
		 * sector.triangles[i].vertices[1].z);
		 * 
		 * u = sector.triangles[i].vertices[2].u; v =
		 * sector.triangles[i].vertices[2].v * textureHeight - textureBottom;
		 * gl.glTexCoord2f(u, v);
		 * gl.glVertex3f(sector.triangles[i].vertices[2].x,
		 * sector.triangles[i].vertices[2].y,
		 * sector.triangles[i].vertices[2].z);
		 * 
		 * gl.glEnd(); }
		 */
	}

	/**
	 * Called back before the OpenGL context is destroyed. Release resource such
	 * as buffers.
	 */

	public im makeImg(String txt) {
		BufferedImage bufferedImage = null;
		int w = 0;
		int h = 0;
		try {
			bufferedImage = ImageIO.read(JOGL2Nehe10World3D.class
					.getResource(txt));
			w = bufferedImage.getWidth();
			h = bufferedImage.getHeight();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		WritableRaster raster = Raster.createInterleavedRaster(
				DataBuffer.TYPE_BYTE, w, h, 4, null);
		ComponentColorModel colorModel = new ComponentColorModel(
				ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[] { 8, 8,
						8, 8 }, true, false, ComponentColorModel.TRANSLUCENT,
				DataBuffer.TYPE_BYTE);
		BufferedImage dukeImg = new BufferedImage(colorModel, raster, false,
				null);

		Graphics2D g = dukeImg.createGraphics();
		g.drawImage(bufferedImage, null, null);
		DataBufferByte dukeBuf = (DataBufferByte) raster.getDataBuffer();
		byte[] dukeRGBA = dukeBuf.getData();
		ByteBuffer bb = ByteBuffer.wrap(dukeRGBA);
		bb.position(0);
		bb.mark();
		im i = new im();
		i.b = bb;
		i.he = h;
		i.wi = w;
		return i;
	}

	class im {
		public ByteBuffer b;
		public int wi;
		public int he;
	}

	public int setUp2DText(GL2 gl, String txt) {

		im mud = this.makeImg(txt);

		gl.glBindTexture(GL.GL_TEXTURE_2D, 11);
		gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);
		gl.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL.GL_RGBA, mud.wi, mud.he, 0,
				GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, mud.b);

		// Use nearer filter if image is larger than the original texture
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER,
				GL.GL_NEAREST);
		// Use nearer filter if image is smaller than the original texture
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER,
				GL.GL_NEAREST);

		// For texture coordinates more than 1, set to wrap mode to GL_REPEAT
		// for
		// both S and T axes (default setting is GL_CLAMP)
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_LINEAR);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_LINEAR);
		return 11;
	}

	public int setUpText(GL2 gl, String txt) {
		im b = this.makeImg(txt);
		im mud = this.makeImg("/images/mars2.jpg");
		ByteBuffer bb = b.b;
		int w = b.wi;
		int h = b.he;
		gl.glBindTexture(GL.GL_TEXTURE_CUBE_MAP, 13);
		gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);
		gl.glTexImage2D(GL2.GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, GL.GL_RGBA,
				mud.wi, mud.he, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, mud.b);
		// postive x
		gl.glTexImage2D(GL2.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, GL.GL_RGBA, w,
				h, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, bb);
		// negative x
		gl.glTexImage2D(GL2.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, GL.GL_RGBA, w,
				h, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, bb);
		// postive y
		gl.glTexImage2D(GL2.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, GL.GL_RGBA, w,
				h, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, bb);
		// negative y
		gl.glTexImage2D(GL2.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, GL.GL_RGBA,
				mud.wi, mud.he, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, mud.b);
		// positive z
		gl.glTexImage2D(GL2.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, GL.GL_RGBA, w,
				h, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, bb);

		// gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER,
		// GL.GL_LINEAR);
		// gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER,
		// GL.GL_LINEAR_MIPMAP_NEAREST);
		// gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S,
		// GL.GL_REPEAT);
		// gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T,
		// GL.GL_REPEAT);

		gl.glTexParameteri(GL2.GL_TEXTURE_CUBE_MAP, GL2.GL_TEXTURE_MAG_FILTER,
				GL2.GL_LINEAR);
		gl.glTexParameteri(GL2.GL_TEXTURE_CUBE_MAP, GL2.GL_TEXTURE_MIN_FILTER,
				GL2.GL_LINEAR);
		gl.glTexParameteri(GL2.GL_TEXTURE_CUBE_MAP, GL2.GL_TEXTURE_WRAP_S,
				GL2.GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL2.GL_TEXTURE_CUBE_MAP, GL2.GL_TEXTURE_WRAP_T,
				GL2.GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL2.GL_TEXTURE_CUBE_MAP, GL2.GL_TEXTURE_WRAP_R,
				GL2.GL_CLAMP_TO_EDGE);

		return 13;
	}

	public int setUpCubeMap(GL2 gl, int width, int height, ByteBuffer data_px,
			ByteBuffer data_nx, ByteBuffer data_py, ByteBuffer data_ny,
			ByteBuffer data_pz, ByteBuffer data_nz) {

		// Make texture
		gl.glBindTexture(GL2.GL_TEXTURE_CUBE_MAP, 13); // Make it a cubemap
		gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);
		gl.glTexImage2D(GL2.GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, GL.GL_RGBA,
				width, height, 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, data_px);
		// postive x
		gl.glTexImage2D(GL2.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, GL.GL_RGBA,
				width, height, 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, data_nx);
		// negative x
		gl.glTexImage2D(GL2.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, GL.GL_RGBA,
				width, height, 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, data_py);
		// postive y
		gl.glTexImage2D(GL2.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, GL.GL_RGBA,
				width, height, 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, data_ny);
		// negative y
		gl.glTexImage2D(GL2.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, GL.GL_RGBA,
				width, height, 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, data_pz);
		// positive z
		gl.glTexImage2D(GL2.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, GL.GL_RGBA,
				width, height, 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, data_nz);
		// negative z
		gl.glTexParameteri(GL2.GL_TEXTURE_CUBE_MAP, GL.GL_TEXTURE_MAG_FILTER,
				GL.GL_LINEAR);
		// Set far filtering mode
		gl.glTexParameteri(GL2.GL_TEXTURE_CUBE_MAP, GL.GL_TEXTURE_MIN_FILTER,
				GL.GL_LINEAR);
		// Set near filtering mode

		return 13;
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
	}

	// ----- Implement methods declared in KeyListener -----

	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {

		case VK_F1:
			if (controlled) {

				frame.setCursor(Cursor.getDefaultCursor());

				Robot r = null;
				try {
					r = new Robot();
				} catch (AWTException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				if (r != null) {

					r.mouseMove(frame.getWidth() / 2, frame.getHeight() / 2);

				}

				p_mpos = MouseInfo.getPointerInfo().getLocation();

				controlled = false;

			} else {
				BufferedImage cursorImg = new BufferedImage(16, 16,
						BufferedImage.TYPE_INT_ARGB);

				// Create a new blank cursor.
				Cursor blankCursor = Toolkit.getDefaultToolkit()
						.createCustomCursor(cursorImg, new Point(0, 0),
								"blank cursor");
				frame.setCursor(blankCursor);
				controlled = true;
			}

			break;
		case VK_ESCAPE:
			frame.dispose();
			System.exit(0);
			

			
			break;
		case VK_LEFT: // player turns left (scene rotates right)
			// headingY += turnIncrement;
			// posX +=0.01;

			break;
		case VK_RIGHT: // player turns right (scene rotates left)
			// headingY -= turnIncrement;
			// posX -= 0.01;
			// mydude.stop();
			mydude.stage = Monster.STAGE_PASSIVE;
			break;
		case VK_UP:
			mydude.start();
			// Player move in, posX and posZ become smaller

			// walkBiasAngle = (walkBiasAngle >= 359.0f) ? 0.0f : walkBiasAngle
			// + 10.0f;
			// What is this walkbias? It's a word I invented :-) It's basically
			// an
			// offset that occurs when a person walks around (head bobbing up
			// and
			// down like a buoy. It simply adjusts the camera's Y position with
			// a
			// sine wave. I had to put this in, as simply moving forwards and
			// backwards didn't look to great.

			// Causes the player to bounce in sine-wave pattern rather than
			// straight-line
			// walkBias = (float)Math.sin(Math.toRadians(walkBiasAngle)) /
			// 20.0f;

			// posZ += 0.01;
			break;
		case VK_DOWN:

			mydude.position.z -= mydude.speed;

			try {
				this.captureScreen("AMAZING.png");
			} catch (Exception ex) {
				Logger.getLogger(JOGL2Nehe10World3D.class.getName()).log(
						Level.SEVERE, null, ex);
			}
			break;
		case KeyEvent.VK_PAGE_UP:
			// player looks up, scene rotates in negative x-axis
			if (lookUpAngle >= lookUpMax)
				lookUpAngle -= lookUpIncrement;
			break;
		case KeyEvent.VK_PAGE_DOWN:
			// player looks down, scene rotates in positive x-axis
			if (lookUpAngle <= lookUpMin)
				lookUpAngle += lookUpIncrement;
			break;
		case VK_T: // switch texture filter nearer -> linear -> mipmap
			currTextureFilter = (currTextureFilter + 1) % textures.length;
			break;

		case VK_W: // toggle blending mode

			up = true;

			break;

		case VK_S:

			down = true;
			break;
		case VK_D:
			right = true;
			break;
		case VK_A:
			left = true;
			break;
		case VK_B: // toggle blending mode
			blendingEnabled = !blendingEnabled;
			break;
		case VK_F:
			cOffset[0] += 0.1;
			break;

		case VK_E:
			cOffset[1] += 0.1;
			break;
		case VK_C:
			pos.z -= 0.1;
			break;
		case VK_V:
			pos.z += 0.1;
			break;
		case VK_X:
			pos.y -= 0.1;
			break;
		case VK_Z:
			pos.y += 0.1;
			break;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		switch (e.getKeyCode()) {
		case VK_W: // toggle blending mode

			up = false;

			break;

		case VK_S:
			down = false;
			break;
		case VK_D:
			right = false;
			break;
		case VK_A:
			left = false;
			break;
		}

	}

	@Override
	public void keyTyped(KeyEvent e) {
		switch (e.getKeyChar()) {

		}
	}

	class glimage {
		public byte[] dataInBytes;
		public ByteBuffer data;
		public int width;
		public int height;

		public glimage(String src) {
			BufferedImage bufferedImage = null;
			int w = 0;
			int h = 0;
			try {
				bufferedImage = ImageIO.read(JOGL2Nehe10World3D.class
						.getResource(src));
				w = bufferedImage.getWidth();
				h = bufferedImage.getHeight();
			} catch (IOException e) {
				e.printStackTrace();
			}
			WritableRaster raster = Raster.createInterleavedRaster(
					DataBuffer.TYPE_BYTE, w, h, 4, null);
			ComponentColorModel colorModel = new ComponentColorModel(
					ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[] { 8,
							8, 8, 8 }, true, false,
					ComponentColorModel.TRANSLUCENT, DataBuffer.TYPE_BYTE);
			BufferedImage dukeImg = new BufferedImage(colorModel, raster,
					false, null);

			Graphics2D g = dukeImg.createGraphics();
			g.drawImage(bufferedImage, null, null);
			DataBufferByte dukeBuf = (DataBufferByte) raster.getDataBuffer();
			byte[] dukeRGBA = dukeBuf.getData();
			ByteBuffer bb = ByteBuffer.wrap(dukeRGBA);
			bb.position(0);
			bb.mark();
			data = bb;

		}

	}

	// A sector comprises many triangles (inner class)
	class Sector {
		Triangle[] triangles;

		// Constructor
		public Sector(int numTriangles) {
			triangles = new Triangle[numTriangles];
			for (int i = 0; i < numTriangles; i++) {
				triangles[i] = new Triangle();
			}
		}
	}

	// A triangle has 3 vertices (inner class)
	class Triangle {
		Vertex[] vertices = new Vertex[3];

		public Triangle() {
			vertices[0] = new Vertex();
			vertices[1] = new Vertex();
			vertices[2] = new Vertex();
		}
	}

	// A vertex has xyz (location) and uv (for texture) (inner class)
	class Vertex {
		float x, y, z; // 3D x,y,z location
		float u, v; // 2D texture coordinates

		public String toString() {
			return "(" + x + "," + y + "," + z + ")" + "(" + u + "," + v + ")";
		}
	}
}
