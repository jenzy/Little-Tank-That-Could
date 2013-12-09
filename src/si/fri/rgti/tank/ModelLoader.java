package si.fri.rgti.tank;

import java.util.HashMap;

import si.fri.rgti.tank.Game.Level;
/**
 * @author Jani
 */
public class ModelLoader {
	public enum Model { Tank, AAA, PAC3, Tree };
	
	private static HashMap<Model, OBJModel> models = new HashMap<Model,OBJModel>();
	
	public static void loadModels(){
		System.out.println("\n> Loading T-90");
		models.put(Model.Tank, new OBJModel("models/T-90/T-90.obj", true));
		if(Game.getLevel() == Level.GRASSLAND){
			System.out.println("\n> Loading tree");
			models.put(Model.Tree, new OBJModel("models/Tree4/Tree4.obj", false));
		}
		System.out.println("\n> Loading AAA");
		models.put(Model.AAA, new OBJModel("models/AAA/AAA.obj", false));
		System.out.println("\n> Loading PAC3");
		models.put(Model.PAC3, new OBJModel("models/MSL/PAC3.obj", false));
	}
	
	public static OBJModel getModel(Model m){
		return models.get(m);
	}

}
