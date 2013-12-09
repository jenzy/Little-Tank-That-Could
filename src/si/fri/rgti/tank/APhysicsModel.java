package si.fri.rgti.tank;

import java.nio.FloatBuffer;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Renderable;

import si.fri.rgti.tank.ModelLoader.Model;
import si.fri.rgti.tank.Utils.QuadDrawer;

import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.MotionState;
import com.bulletphysics.linearmath.Transform;

/**
 * Class for all other object. Contains everything for JBullet physics and can draw a bounding box.
 * @author Jani
 */
public abstract class APhysicsModel implements Renderable{
	protected static final boolean debug_BoundingBox = false;
	
	/**
	 * A world for the model to live in
	 */
	protected DynamicsWorld physicsWorld;
	/**
	 * JBullet's Rigid body - physics
	 */
	protected RigidBody rigidBody;
	/**
	 * Render model - Looks
	 */
	protected OBJModel model3D;

	/**
	 * Half-extents of the bounding box
	 */
	protected Vector3f halfSize;
	/**
	 * The construction info that the constructor needs
	 */
	protected final ModelConstructionInfo constructionInfo;

	// DEBUG stuff - for drawing the collision box
	/**
	 * Vertices of the bounding box
	 */
	private Point3f[] boundingBoxVertices;
	/**
	 * pointer for the bounding box DisplayList
	 */
	protected int pListBoundingBox;			// pointer for the bounding box DisplayList
	/**
	 * draw the bounding box with wireframe
	 */
	protected boolean wireframe = true; 		// draw the bounding box with wireframe

	// preallocated stuff for retrieving transformation matrix
	protected float[] transformationMatrix = new float[16];
	protected Transform transform = new Transform();
	protected FloatBuffer transformationBuffer = BufferUtils.createFloatBuffer(16);

	public APhysicsModel(Vector3f position, ModelConstructionInfo constructionInfo, DynamicsWorld physicsWorld){
		this.constructionInfo = constructionInfo;
		this.physicsWorld = physicsWorld;
	}
	
	public APhysicsModel(Vector3f position, Vector3f size, DynamicsWorld physicsWorld, ModelConstructionInfo info) {
		this(position, info, physicsWorld);
		this.halfSize = new Vector3f(size.x / 2f, size.y / 2f, size.z / 2f);

		MotionState motionState = new DefaultMotionState(new Transform(	new Matrix4f(new Quat4f(0, 0, 0, 1), position, 1.0f)));
		CollisionShape collisionShape = new BoxShape(halfSize);

		Vector3f inertia = new Vector3f(0, 0, 0);
		collisionShape.calculateLocalInertia(constructionInfo.mass, inertia);

		RigidBodyConstructionInfo constructionInfo = new RigidBodyConstructionInfo(
				info.mass, motionState, collisionShape, inertia);
		constructionInfo.restitution = info.ciRestitution;
		constructionInfo.angularDamping = info.ciAngluarDamping;
		constructionInfo.friction = info.ciFriction;
		constructionInfo.mass = info.mass;
		rigidBody = new RigidBody(constructionInfo);
		rigidBody.setActivationState(CollisionObject.DISABLE_DEACTIVATION); // never deactivate
		rigidBody.setUserPointer(this);
		physicsWorld.addRigidBody(rigidBody);
		
		initBoundingBox();
	}
	
	public APhysicsModel(Vector3f position, Vector3f size, DynamicsWorld physicsWorld, ModelConstructionInfo info, String modelFile){
		this(position, size, physicsWorld, info);
		
		model3D = new OBJModel(modelFile, info.makeModelGroups);
	}
	
	public APhysicsModel(Vector3f position, Vector3f size, DynamicsWorld physicsWorld, ModelConstructionInfo info, Model m) {
		this(position, size, physicsWorld, info);

		model3D = ModelLoader.getModel(m);
	}
	
	/**
	 * initializes vertices for the bounding box and initializes
	 * the DisplayList that draws it.
	 */
	protected void initBoundingBox() {
		boundingBoxVertices = new Point3f[] {
				new Point3f(-halfSize.x, -halfSize.y, -halfSize.z),
				new Point3f(halfSize.x, -halfSize.y, -halfSize.z),
				new Point3f(halfSize.x, -halfSize.y, halfSize.z),
				new Point3f(-halfSize.x, -halfSize.y, halfSize.z),
				new Point3f(-halfSize.x, halfSize.y, -halfSize.z),
				new Point3f(halfSize.x, halfSize.y, -halfSize.z),
				new Point3f(halfSize.x, halfSize.y, halfSize.z),
				new Point3f(-halfSize.x, halfSize.y, halfSize.z), };

		QuadDrawer qd = new QuadDrawer(boundingBoxVertices);
		pListBoundingBox = GL11.glGenLists(1);
		GL11.glNewList(pListBoundingBox, GL11.GL_COMPILE);
			if (constructionInfo.boundingBoxWireframe)
				GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE); // wireframe
			GL11.glBegin(GL11.GL_QUADS);
	
				// BOTTOM FACE
				GL11.glColor4f(0, 0, 1, 1);
				qd.drawQuad(new int[] { 0, 1, 2, 3 });
				if (constructionInfo.boundingBoxWireframe)
					qd.drawQuad(new int[] { 3, 2, 1, 0 }); // other side
		
				// TOP FACE
				GL11.glColor4f(0, 0, 1, 1);
				qd.drawQuad(new int[] { 7, 6, 5, 4 });
				if (constructionInfo.boundingBoxWireframe)
					qd.drawQuad(new int[] { 4, 5, 6, 7 }); // other side
		
				// BACK FACE
				GL11.glColor4f(1, 0, 0, 1);
				qd.drawQuad(new int[] { 4, 5, 1, 0 });
				if (constructionInfo.boundingBoxWireframe)
					qd.drawQuad(new int[] { 0, 1, 5, 4 }); // other side
		
				// FRONT FACE
				GL11.glColor4f(1, 0, 0, 1);
				qd.drawQuad(new int[] { 3, 2, 6, 7 });
				if (constructionInfo.boundingBoxWireframe)
					qd.drawQuad(new int[] { 7, 6, 2, 3 }); // other side
		
				// LEFT FACE
				GL11.glColor4f(0, 1, 0, 1);
				qd.drawQuad(new int[] { 5, 6, 2, 1 });
				if (constructionInfo.boundingBoxWireframe)
					qd.drawQuad(new int[] { 1, 2, 6, 5 }); // other side
		
				// RIGHT FACE
				GL11.glColor4f(0, 1, 0, 1);
				qd.drawQuad(new int[] { 3, 7, 4, 0 });
				if (constructionInfo.boundingBoxWireframe)
					qd.drawQuad(new int[] { 0, 4, 7, 3 }); // other side
			GL11.glEnd();
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL); // no wireframe
		GL11.glEndList();
	}

	@Override
	public void render(){
		GL11.glPushMatrix();
			// Get the transformation matrix from JBullet.
			MotionState motionState = rigidBody.getMotionState();
			motionState.getWorldTransform(transform);
			transform.getOpenGLMatrix(transformationMatrix);
	
			// Put the transformation matrix into a FloatBuffer.
			transformationBuffer.clear();
			transformationBuffer.put(transformationMatrix);
			transformationBuffer.flip();
			
			//Apply the transformation
			GL11.glMultMatrix(transformationBuffer);
			
			//Draw the bounding box
			if(debug_BoundingBox){
				GL11.glCallList(pListBoundingBox);
			}
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			if(model3D != null)
				model3D.render();
			GL11.glDisable(GL11.GL_TEXTURE_2D);
		
		GL11.glPopMatrix();
	}

	public static class ModelConstructionInfo{
		// RigidBody construction info
		public float ciRestitution = 0.5f;
		public float ciAngluarDamping = 0.95f;
		public float ciLinearDamping = 0.05f;
		public float ciFriction = 0.5f;
		
		public boolean makeModelGroups = false;
		
		/**
		 * Mass of the model, should be in kilograms
		 */
		public float mass = 100.0f;
		
		public boolean boundingBoxWireframe = true;
	}
}
