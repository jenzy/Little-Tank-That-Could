package si.fri.rgti.tank;

import javax.vecmath.Vector3f;

import si.fri.rgti.tank.ModelLoader.Model;

import com.bulletphysics.dynamics.DynamicsWorld;

/**
 * A model that doesn't move (except up and down ~ gravity)
 * This solution kinda sucks, but jBullet isn't a complete port of Bullet, so it
 * doesn't have the .setLinearFactor method to restrict movement, so I resorted
 * to assigning static objects a massive mass so that they can't be moved.
 * @author Jani
 */
public abstract class APhysicsModelStatic extends APhysicsModel {

	public APhysicsModelStatic(Vector3f position, Vector3f size,
			DynamicsWorld physicsWorld, ModelConstructionInfo info,
			String modelFile) {
		super(position, size, physicsWorld, info, modelFile);
		
		rigidBody.setMassProps(constructionInfo.mass, new Vector3f(0,0,0));
		rigidBody.setAngularFactor(0f);
	}

	public APhysicsModelStatic(Vector3f position, Vector3f size, DynamicsWorld physicsWorld, ModelConstructionInfo info, Model m) {
		super(position, size, physicsWorld, info, m);
		
		rigidBody.setMassProps(constructionInfo.mass, new Vector3f(0,0,0));
		rigidBody.setAngularFactor(0f);
	}

}
