package si.fri.rgti.tank;

import javax.vecmath.Vector3f;

import com.bulletphysics.dynamics.DynamicsWorld;

public class Box extends APhysicsModel {
	protected boolean wireframe = false;

	public Box(Vector3f position, Vector3f size, DynamicsWorld physicsWorld,	ModelConstructionInfo info) {
		super(position, size, physicsWorld, info);
	}

}
