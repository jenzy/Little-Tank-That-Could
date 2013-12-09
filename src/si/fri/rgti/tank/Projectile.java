package si.fri.rgti.tank;

import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;

import si.fri.rgti.tank.particles.Explosion;
import si.fri.rgti.tank.particles.Explosion.ExplosionType;
import si.fri.rgti.tank.sound.Sound;

import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.MotionState;
import com.bulletphysics.linearmath.Transform;

public class Projectile extends APhysicsModel implements IDestructable{
	private org.lwjgl.util.glu.Sphere renderBody;
	
	private float radius = 0.2f;
	

	public Projectile(Vector3f position, ModelConstructionInfo constructionInfo, DynamicsWorld physicsWorld) {
		super(position, constructionInfo, physicsWorld);
	}
	
	public Projectile(Vector3f position, ModelConstructionInfo constructionInfo, DynamicsWorld physicsWorld, Vector3f velocity, float radius){
		this(position, constructionInfo, physicsWorld);
		this.radius = radius;
		
		MotionState motionState = new DefaultMotionState(new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1), position, 1.0f)));
        CollisionShape collisionShape = new com.bulletphysics.collision.shapes.SphereShape(radius);
        renderBody = new org.lwjgl.util.glu.Sphere();
        
        Vector3f ballInertia = new Vector3f(0, 0, 0);
        collisionShape.calculateLocalInertia(constructionInfo.mass, ballInertia);
        RigidBodyConstructionInfo ballConstructionInfo = new RigidBodyConstructionInfo(10f, motionState, collisionShape, ballInertia);
        ballConstructionInfo.restitution = constructionInfo.ciRestitution;
        ballConstructionInfo.angularDamping = constructionInfo.ciAngluarDamping;
        ballConstructionInfo.linearDamping = constructionInfo.ciLinearDamping;
        this.rigidBody = new RigidBody(ballConstructionInfo);
        this.rigidBody.setUserPointer(this);
        
        rigidBody.setLinearVelocity(velocity);
        this.physicsWorld.addRigidBody(rigidBody);
	}
	
	public static Projectile createTankProjectile(Vector3f position, DynamicsWorld physicsWorld, Vector3f velocity){
		ModelConstructionInfo info = new ModelConstructionInfo();
		info.ciRestitution = 0.1f;
		info.ciAngluarDamping = 0.95f;
		info.ciLinearDamping = 0.2f;
		info.ciFriction = 1f;
		info.mass = 2.5f;
		float radius = 0.1f;
		
		velocity.normalize();
		velocity.scale(70f);
		
		Projectile p = new Projectile(position, info, physicsWorld, velocity, radius);
		
		Game.addObject(p);
		
		Explosion e = new Explosion(ExplosionType.GunFired, position, velocity, 100);
		Game.addObject(e);
		
		return p;
	}

	@Override
	public void render() {
		 glPushMatrix();
	        // Get the transformation matrix from JBullet.
	 		rigidBody.getMotionState().getWorldTransform(transform);
	 		transform.getOpenGLMatrix(transformationMatrix);

	 		// Put the transformation matrix into a FloatBuffer.
	 		transformationBuffer.clear();
	 		transformationBuffer.put(transformationMatrix);
	 		transformationBuffer.flip();
	 		GL11.glMultMatrix(transformationBuffer);
	        renderBody.draw(radius, 10, 10);
	     glPopMatrix();
	}

	@Override
	public void destroy(Vector3f impactLocation) {
		Sound.playASound(Sound.EXPLOSION, impactLocation);
		
		Explosion e = new Explosion(ExplosionType.Explosion, impactLocation, null, 200);
		Game.addObject(e);
		
		Game.removeFromDynamicsWorld(this.rigidBody);
		Game.removeObject(this);
	}

	@Override
	public boolean isGameGoal() {
		return false;
	}
	
	


}
