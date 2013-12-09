package si.fri.rgti.tank;

import java.util.LinkedList;

import javax.vecmath.Vector3f;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Renderable;

import si.fri.rgti.tank.Camera.CameraMode;
import si.fri.rgti.tank.Menu.MenuMode;
import si.fri.rgti.tank.sound.Sound;

import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.broadphase.Dispatcher;
import com.bulletphysics.collision.dispatch.CollisionConfiguration;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.narrowphase.ManifoldPoint;
import com.bulletphysics.collision.narrowphase.PersistentManifold;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;

/**
 * @author Jani
 */
public class Game extends BaseWindow {
	public enum Difficulty {EASY, NORMAL};
	public enum Level {GRASSLAND, DESERT};
	
	private static GameManager gameManager;
	private static Difficulty difficulty = Difficulty.EASY;
	private static Level level = Level.DESERT;
	private static HUD hud;
	
	private static DynamicsWorld dynamicsWorld;
    private static Tank tank;
    private static Terrain terrain;
    private static Skybox skybox;
    
    private static LinkedList<Renderable> objects = new LinkedList<Renderable>();
    private static Object mutexObjects = new Object();
    
    private static Camera camera  = new Camera();
    
    @Override
	protected void setupPhysicsAndModels() {
    	// Init bullet's dynamics world
		BroadphaseInterface broadphase = new DbvtBroadphase();
        CollisionConfiguration collisionConfiguration = new DefaultCollisionConfiguration();
        CollisionDispatcher dispatcher = new CollisionDispatcher(collisionConfiguration);
        ConstraintSolver solver = new SequentialImpulseConstraintSolver();
        dynamicsWorld = new DiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfiguration);
        dynamicsWorld.setGravity(new Vector3f(0, -10 /* m/s^2 */, 0));
        
        // Load models ant init physics objects
        ModelLoader.loadModels();
        
        switch(level){
        case GRASSLAND:
        	terrain = Terrain.createTerrain("maps/map1", dynamicsWorld);
        	skybox = new Skybox("maps/skybox1", camera);
        	Tree.createRandomTrees(dynamicsWorld);
        	tank = Tank.createTankT90(new Vector3f(0, 15, 0), dynamicsWorld);
        	break;
        case DESERT:
        	terrain = Terrain.createTerrain("maps/map2", dynamicsWorld);
        	skybox = new Skybox("maps/skybox2", camera);
        	tank = Tank.createTankT90(new Vector3f(0, 30, 0), dynamicsWorld);
        	break;
        }
        
        gameManager = new GameManager(dynamicsWorld);
        gameManager.spawnGoals();
        
        Sound.init();
    }
    
	@Override
	protected void setupView() {
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
	    GL11.glLoadIdentity();
	    
		// OpenGL stuff
	    GL11.glEnable(GL11.GL_DEPTH_TEST);  // enable depth buffer (off by default)
	    GL11.glEnable(GL11.GL_CULL_FACE); // enable culling of back sides of polygons
	    GL11.glViewport(0, 0, m_width, m_height); // mapping from normalized to window coordinates
	    
	    // lights
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_LIGHT0);
		GL11.glLight(GL11.GL_LIGHT0, GL11.GL_POSITION, allocFloats(new float[] { -1f, 0.25f, 1f, 0f}));
		GL11.glLight(GL11.GL_LIGHT0, GL11.GL_AMBIENT, allocFloats(new float[] { 0.2f, 0.2f, 0.2f, 0.0f}));
		GL11.glLight(GL11.GL_LIGHT0, GL11.GL_DIFFUSE , allocFloats(new float[] { 1.0f, 1.0f, 1.0f, 0.0f}));
		//GL11.glLightf(GL11.GL_LIGHT0, GL11.GL_LINEAR_ATTENUATION , 1f);
		
		 // fog
	    GL11.glFog(GL11.GL_FOG_COLOR,allocFloats(new float[] { 0.8f,0.8f,0.8f,0.0f }));
	    GL11.glFogi(GL11.GL_FOG_MODE,GL11.GL_EXP2);
	    GL11.glFogf(GL11.GL_FOG_DENSITY,0.005f);
	    GL11.glHint (GL11.GL_FOG_HINT, GL11.GL_NICEST);
	    GL11.glEnable(GL11.GL_FOG);

	    // textures
	    //GL11.glEnable(GL11.GL_TEXTURE_2D); // enable 2D textures ~> nope, will enable when needed
	    // select modulate to mix texture with color for shading; GL_REPLACE, GL_MODULATE ...
	    GL11.glTexEnvf( GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE );
	    
	    // Camera setup
	    camera.setPerspectiveSettings(45, m_width / (float)m_height, 5f, 200f);
	    camera.setTopDownFollowCameraSettings(tank, 90f);
	    camera.set3rdPersonFollowCameraSettings(tank, 5f, 15f, 15f);
	    camera.setSwitching(new CameraMode[] {CameraMode.FOLLOW_3RD_PERSON, CameraMode.FOLLOW_TOPDOWN});
	    camera.switchToMode(CameraMode.FOLLOW_3RD_PERSON);
	    
	    hud = new HUD(m_width, m_height, tank, gameManager.getGoalLocation());
	}
	
	

	@Override
	protected void setupGame() {
		gameManager.startTimer();
	}

	@Override
	protected void updateFrame(int deltaTimeMiliseconds) {
		float deltaTimeSeconds = deltaTimeMiliseconds / 1000.0f;
		
		dynamicsWorld.stepSimulation( deltaTimeSeconds );
		checkCollisions();
		Sound.update(camera.getEyeLocation());
		
		// Check if player fell out of map
		if(tank.getPosition().y < -1f){
			System.out.println("You fell out of the map, idiot");
			Game.isRunning = false;
			Game.setEndGameScreen(MenuMode.OUT_OF_MAP);
		}
		
	}

	/**
	 * checks for collisions with projectiles
	 */
	private void checkCollisions(){
		Dispatcher disp = dynamicsWorld.getDispatcher();
		int numManifolds = disp.getNumManifolds();
		for(int i=0; i<numManifolds; i++){
			PersistentManifold contactManifold =  disp.getManifoldByIndexInternal(i);
			if(contactManifold == null) break;
			CollisionObject A = (CollisionObject) contactManifold.getBody0();
			CollisionObject B = (CollisionObject) contactManifold.getBody1();
			Object a = A.getUserPointer();
			Object b = B.getUserPointer();
			
			Projectile p = null;
			IDestructable target = null;
			
			if(a instanceof Projectile){
				p = (Projectile) a;
				if(b instanceof IDestructable)
					target = (IDestructable) b;
			}
			else if(b instanceof Projectile){
				p = (Projectile) b;
				if(a instanceof IDestructable)
					target = (IDestructable) a;
			}
			
			
			if( p != null ){
				int numContacts = contactManifold.getNumContacts();
				for (int j=0; j<numContacts; j++){
					ManifoldPoint pt = contactManifold.getContactPoint(j);
					if(pt.getDistance() < 0.0f){
						System.out.println("collision");
						Vector3f pos = pt.getPositionWorldOnA(new Vector3f());
						p.destroy(pos);
						
						if(target != null){
							target.destroy(null);
						}
					}
				}
			}
		}
	}
	
	@Override
	protected void renderFrame() {
		skybox.render();
		camera.update();
		terrain.render();
		tank.render();
		
		synchronized (mutexObjects) {
			for(Renderable object : objects)
					object.render();
		}
		
		hud.render(m_fps, gameManager.getTimeLeft());
	}

	@Override
	protected void processInput() {
		super.processInput();
		
		while (Keyboard.next()) {
			switch(Keyboard.getEventKey()){
			
			// Vehicle controls
			case Keyboard.KEY_W:
				tank.goForward(Keyboard.getEventKeyState());
				break;
			case Keyboard.KEY_S:
				tank.goBackward(Keyboard.getEventKeyState());
				break;
			case Keyboard.KEY_D:
				tank.goRight(Keyboard.getEventKeyState(), Keyboard.isKeyDown(Keyboard.KEY_W));
				break;
			case Keyboard.KEY_A:
				tank.goLeft(Keyboard.getEventKeyState(), Keyboard.isKeyDown(Keyboard.KEY_W));
				break;
				
			case Keyboard.KEY_R:
				if(!Keyboard.getEventKeyState()) 	// key up
					tank.reset();
				break;
				
			case Keyboard.KEY_SPACE:
				tank.fire(Keyboard.getEventKeyState());
				break;
				
			// Camera switch
			case Keyboard.KEY_C:
				if(!Keyboard.getEventKeyState()) // key up
					camera.switchCamera();
			}
			
		}
		
		// Tank controls
		if(Keyboard.isKeyDown(Keyboard.KEY_LEFT)){
			tank.turnTurretLeft();
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_RIGHT)){
			tank.turnTurretRight();
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_UP)){
			tank.gunUp();
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_DOWN)){
			tank.gunDown();
		}
		
	}
	
	
	public static void addObject(Renderable model){
		objects.add(model);
	}
	
	public static void removeObject(Renderable object){
		synchronized (mutexObjects) {
			objects.remove(object);
		}
	}
	
	public static void removeFromDynamicsWorld(RigidBody body){
		dynamicsWorld.removeRigidBody(body);
	}
	
	public static GameManager getGamemanager(){
		return gameManager;
	}

	public static void setDifficulty(Difficulty difficulty) {
		Game.difficulty = difficulty;
	}

	public static Difficulty getDifficulty() {
		return difficulty;
	}

	public static Level getLevel() {
		return level;
	}

	public static void setLevel(Level level) {
		Game.level = level;
	}

}


