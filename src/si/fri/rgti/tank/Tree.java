package si.fri.rgti.tank;

import java.util.Random;

import javax.vecmath.Vector3f;

import si.fri.rgti.tank.ModelLoader.Model;

import com.bulletphysics.dynamics.DynamicsWorld;

/**
 * @author Jani
 */
public class Tree extends APhysicsModelStatic implements IDestructable {
	
	private Tree(Vector3f position, Vector3f size, DynamicsWorld physicsWorld,	ModelConstructionInfo info, Model model) {
		super(position, size, physicsWorld, info, model);
	}

	public static Tree createTree(Vector3f position, DynamicsWorld physicsWorld){
		ModelConstructionInfo cInfo = new ModelConstructionInfo();
        cInfo.boundingBoxWireframe = true;
        cInfo.mass = 999999;
        Tree tree = new Tree(position, new Vector3f(1.5f, 17, 1.5f), physicsWorld, cInfo, Model.Tree);
        return tree;
	}

	@Override
	public void destroy(Vector3f impactLocation) {
		Game.removeFromDynamicsWorld(this.rigidBody);
		Game.removeObject(this);
		
		if(isGameGoal()){
			Game.getGamemanager().goalDestroyed();
		}
	}
	
	public static void createRandomTrees(DynamicsWorld physicsWorld){
		Random r = new Random();
		int numTrees = 10;
		int range = 300;
		float y = 100;
		for(int i=0; i<numTrees; i++){
			float x = (r.nextInt()%range);
			float z = (r.nextInt()%range);
			Tree t = createTree(new Vector3f(x,y,z), physicsWorld);
			Game.addObject(t);
		}
	}

	@Override
	public boolean isGameGoal() {
		return false;
	}
	
}
