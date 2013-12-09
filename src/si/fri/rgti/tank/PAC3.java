package si.fri.rgti.tank;

import javax.vecmath.Vector3f;

import si.fri.rgti.tank.ModelLoader.Model;

import com.bulletphysics.dynamics.DynamicsWorld;

public class PAC3 extends APhysicsModel implements IDestructable {

	private PAC3(Vector3f position, Vector3f size, DynamicsWorld physicsWorld, ModelConstructionInfo info, Model model) {
		super(position, size, physicsWorld, info, model);
	}

	public static PAC3 createPAC3(Vector3f position, DynamicsWorld physicsWorld){
		ModelConstructionInfo cInfo = new ModelConstructionInfo();
        cInfo.boundingBoxWireframe = true;
        cInfo.mass = 999999;
        cInfo.ciAngluarDamping = 0.7f;
        PAC3 pac = new PAC3(position, new Vector3f(22f, 6f, 6f), physicsWorld, cInfo, Model.PAC3);
        return pac;
	}

	@Override
	public void destroy(Vector3f impactLocation) {
		Game.removeFromDynamicsWorld(this.rigidBody);
		Game.removeObject(this);
		
		if(isGameGoal()){
			Game.getGamemanager().goalDestroyed();
		}
	}
	
	@Override
	public boolean isGameGoal() {
		return true;
	}
}
