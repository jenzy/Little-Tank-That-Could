package si.fri.rgti.tank;

import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;

import java.nio.FloatBuffer;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.MotionState;
import com.bulletphysics.linearmath.Transform;

public class Sphere {
	private final DynamicsWorld physicsWorld;
	private final RigidBody physicsBody;
	private final org.lwjgl.util.glu.Sphere renderBody;
	
	// preallocated stuff for retrieving transformation matrix
	private float[] transformationMatrix = new float[16];
	private Transform transform = new Transform();
	private FloatBuffer transformationBuffer = BufferUtils.createFloatBuffer(16);
    
    private float radius = 0.25f;
    
	public Sphere(Vector3f position, DynamicsWorld physicsWorld) {
		this.physicsWorld = physicsWorld;
		
		MotionState motionState = new DefaultMotionState(new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1), position, 1.0f)));
        CollisionShape shape = new com.bulletphysics.collision.shapes.SphereShape(radius);
        //this.physicsBody = new RigidBody(1f, motionState, shape);
        //this.physicsWorld.addRigidBody(physicsBody);
        this.renderBody = new org.lwjgl.util.glu.Sphere();
        renderBody.setDrawStyle(GLU.GLU_SILHOUETTE);
        
        
        Vector3f ballInertia = new Vector3f(0, 0, 0);
        shape.calculateLocalInertia(2.5f, ballInertia);
        RigidBodyConstructionInfo ballConstructionInfo = new RigidBodyConstructionInfo(10f, motionState, shape, ballInertia);
        ballConstructionInfo.restitution = 0.5f;
        ballConstructionInfo.angularDamping = 0.95f;
        physicsBody = new RigidBody(ballConstructionInfo);
        this.physicsWorld.addRigidBody(physicsBody);
	}

	void render() {
        glPushMatrix();
        
        // Get the transformation matrix from JBullet.
 		MotionState motionState = physicsBody.getMotionState();
 		motionState.getWorldTransform(transform);
 		transform.getOpenGLMatrix(transformationMatrix);

 		// Put the transformation matrix into a FloatBuffer.
 		transformationBuffer.clear();
 		transformationBuffer.put(transformationMatrix);
 		transformationBuffer.flip();
 		GL11.glMultMatrix(transformationBuffer);
        renderBody.draw(radius, 15, 15);
        glPopMatrix();
    }

}
