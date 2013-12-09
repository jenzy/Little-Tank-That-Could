package si.fri.rgti.tank;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.PixelFormat;

import si.fri.rgti.tank.Menu.MenuMode;

public class BaseWindow {
	protected static boolean isRunning = false;
	public static final String TITLE = "Little Tank That Could";
	
	protected Menu menu;
	protected static MenuMode endGameScreen = MenuMode.NONE;
	
	//Settings
	protected int m_width = 1024;
	protected int m_height = 768;
	private int m_frameLimit = 60;
	
	private boolean m_FSAA = true;
	//private String m_windowTitle = this.getClass().getName();
	
	//Timer stuff
	private long timeOfLastFrame;
	private long timeOfLastFpsUpdate;
	//private int fps = 0;
	protected int m_fps = 0;
	
	
	public BaseWindow() {}

	/**
	 * Initializes display and enters main loop
	 */
	protected void execute() {
		try {
			initDisplay();
		} catch (LWJGLException e) {
			System.err.println("Can't open display.");
			System.exit(0);
		}
		BaseWindow.isRunning = true;

		menu = new Menu(m_width, m_height);
		menu.showMenu(MenuMode.OPTIONS);
		mainLoop();
		menu.showMenu(endGameScreen);
		
		Display.destroy();
	}

	/**
	 * Main loop: renders and processes input events
	 */
	protected void mainLoop() {
		if(!BaseWindow.isRunning) return;
		setupPhysicsAndModels(); 	// setup all the physics
		setupView(); 	// setup camera and lights
		setupGame();	// setup game goals and stuff ~ last thing before game starts

		timeOfLastFrame = getTime();
		timeOfLastFpsUpdate = timeOfLastFrame;
		while (BaseWindow.isRunning) {
			long timeOfCurrentFrame = getTime();	//tic
			int deltaTime = (int)(timeOfCurrentFrame-timeOfLastFrame);
			timeOfLastFrame = timeOfCurrentFrame;
			//updateFPS(timeOfCurrentFrame);
			
			resetView();  // reset view
			updateFrame(deltaTime); // Update game logic/physics
			renderFrame();  // let subsystem paint
			processInput(); // process input events

			Display.update(); // update window contents and process input messages
			if (m_frameLimit > 0) //set max fps
				Display.sync(m_frameLimit);
			
			// update fps
			if(timeOfCurrentFrame - timeOfLastFpsUpdate > 100){
				timeOfLastFpsUpdate = timeOfCurrentFrame;
				if (deltaTime > 0)
					m_fps = (int)(1000 / deltaTime);
			}
			
		}
		
	}


	/**
	 * Initial setup of projection of the scene onto screen, lights, etc.
	 */
	protected void setupView() {}

	/**
	 * Resets the view of current frame
	 */
	protected void resetView() {
		 GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT); // clear color and depth buffer
	}
	
	/**
	 * Update the logic for current frame
	 * @param deltaTimeMiliseconds 
	 */
	protected void updateFrame(int deltaTimeMiliseconds) {}

	/**
	 * Renders current frame
	 */
	protected void renderFrame() {}

	/**
	 * Initialize all the physics stuff, models too
	 */
	protected void setupPhysicsAndModels() {	}
	
	/**
	 * Setup game goals and stuff ~ last thing before game starts
	 */
	protected void setupGame() {	}

	/**
	 * Processes Keyboard and Mouse input and spawns actions
	 */
	protected void processInput() {
		if (Display.isCloseRequested()
				|| Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
			BaseWindow.isRunning = false;
		}
	}

	/**
	 * Finds best width x height display mode and sets it
	 * 
	 * @throws LWJGLException
	 */
	protected void initDisplay() throws LWJGLException {
		DisplayMode bestMode = null;
		DisplayMode[] dm = Display.getAvailableDisplayModes();
		for (int nI = 0; nI < dm.length; nI++) {
			DisplayMode mode = dm[nI];
			if (mode.getWidth() == m_width && mode.getHeight() == m_height
					&& mode.getFrequency() <= 85) {
				if (bestMode == null
						|| (mode.getBitsPerPixel() >= bestMode
								.getBitsPerPixel() && mode.getFrequency() > bestMode
								.getFrequency()))
					bestMode = mode;
			}
		}

		Display.setDisplayMode(bestMode);
		Display.setTitle(TITLE);

		if(m_FSAA) 	Display.create(new PixelFormat(8, 8, 8, 4)); // FSAA
		else 		Display.create(); // No FSAA
		
		//Display.setVSyncEnabled(true);
		
		System.out.println("GL_VERSION: " + GL11.glGetString(GL11.GL_VERSION));
		System.out.println("GL_VENDOR: " + GL11.glGetString(GL11.GL_VENDOR));
		System.out.println("GL_RENDERER: " + GL11.glGetString(GL11.GL_RENDERER));
		System.out.println();
	}

	/**
	 * Get the time in miliseconds
	 * @return current time in miliseconds
	 */
	public static long getTime(){
		return System.nanoTime() / 1000000;
	}
	
	/**
	 * Updates the fps counter and if 1s has passed since 
	 * the last update of the fps display it updates the fps
	 * display in the title
	 * @param timeOfCurrentFrame current time in miliseconds
	 */
//	private void updateFPS(long timeOfCurrentFrame) {
//		if(timeOfCurrentFrame - timeOfLastFpsUpdate > 500){
//			Display.setTitle(m_windowTitle + "  FPS: " + fps*2);
//			fps=0;
//			timeOfLastFpsUpdate = timeOfCurrentFrame;
//		}
//		fps++;
//	}
	

	/**
	 * Utils for creating native buffers
	 * 
	 * @throws LWJGLException
	 */
	public static ByteBuffer allocBytes(int howmany) {
		return ByteBuffer.allocateDirect(howmany)
				.order(ByteOrder.nativeOrder());
	}

	public static IntBuffer allocInts(int howmany) {
		return ByteBuffer.allocateDirect(howmany)
				.order(ByteOrder.nativeOrder()).asIntBuffer();
	}

	public static FloatBuffer allocFloats(int howmany) {
		return ByteBuffer.allocateDirect(howmany)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
	}

	public static ByteBuffer allocBytes(byte[] bytearray) {
		ByteBuffer bb = ByteBuffer.allocateDirect(bytearray.length * 1).order(
				ByteOrder.nativeOrder());
		bb.put(bytearray).flip();
		return bb;
	}

	public static IntBuffer allocInts(int[] intarray) {
		IntBuffer ib = ByteBuffer.allocateDirect(intarray.length * 4)
				.order(ByteOrder.nativeOrder()).asIntBuffer();
		ib.put(intarray).flip();
		return ib;
	}

	public static FloatBuffer allocFloats(float[] floatarray) {
		FloatBuffer fb = ByteBuffer.allocateDirect(floatarray.length * 4)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		fb.put(floatarray).flip();
		return fb;
	}

	public static void setEndGameScreen(MenuMode endGameScreen) {
		BaseWindow.endGameScreen = endGameScreen;
	}
	
	
}