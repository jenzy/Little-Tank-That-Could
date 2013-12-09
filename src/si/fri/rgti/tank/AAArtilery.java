package si.fri.rgti.tank;

import javax.vecmath.Vector3f;

import si.fri.rgti.tank.ModelLoader.Model;

import com.bulletphysics.dynamics.DynamicsWorld;

public class AAArtilery extends APhysicsModelStatic implements IDestructable {

	private AAArtilery(Vector3f position, Vector3f size, DynamicsWorld physicsWorld, ModelConstructionInfo info, String modelFile) {
		super(position, size, physicsWorld, info, modelFile);
	}
	private AAArtilery(Vector3f position, Vector3f size, DynamicsWorld physicsWorld, ModelConstructionInfo info, Model model) {
		super(position, size, physicsWorld, info, model);
	}
	
	public static AAArtilery createAAA(Vector3f position, DynamicsWorld physicsWorld){
		ModelConstructionInfo cInfo = new ModelConstructionInfo();
        cInfo.boundingBoxWireframe = true;
        cInfo.mass = 999999;
        AAArtilery aaa = new AAArtilery(position, new Vector3f(3.5f, 5, 3.5f), physicsWorld, cInfo, Model.AAA);
        return aaa;
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
