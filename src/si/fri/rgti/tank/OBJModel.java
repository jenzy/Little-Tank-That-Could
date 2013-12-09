package si.fri.rgti.tank;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import si.fri.rgti.glmodel.GLMaterial;
import si.fri.rgti.glmodel.GLModel;
import si.fri.rgti.glmodel.GL_Triangle;

/**
 * 
 * @author Jani
 *
 */
public class OBJModel extends GLModel {
	private Map<String, Integer> displayLists = new HashMap<String, Integer>();
	
	public OBJModel(String filename, boolean makeSeparateGroups) {
		super(filename);
		
		//if(makeSeparateGroups)
			makeGroupsIntoLists();
	}
	
	/**
	 * Creates a new Display List for each group in this model.
	 * Stores pointers in the displayLists Map.
	 */
	public void makeGroupsIntoLists(){
		for (int gid=0; gid < mesh.numGroups(); gid++) {
			String groupName = mesh.getGroupName(gid);
			
			int pDisplayList = GL11.glGenLists(1);
			
			GL11.glNewList(pDisplayList, GL11.GL_COMPILE);
			
				// draw the triangles in this group
				GLMaterial[] materials = mesh.materials;   // loaded from the .mtl file
				GL_Triangle[] triangles = mesh.getGroupFaces(gid);  // each group may have a material
				GLMaterial mtl = null;
				GL_Triangle t;
				int currMtl = -1;
				
				// draw all triangles in object
				for (int i=0; i < triangles.length; ) {
					t = triangles[i];
					
					// activate new material and texture
					currMtl = t.getMaterialID();
					mtl = (materials != null && materials.length>0 && currMtl >= 0)? materials[currMtl] : defaultMtl;
					mtl.apply();
					GL11.glBindTexture(GL11.GL_TEXTURE_2D, mtl.textureHandle);
					
					// draw triangles until material changes
					GL11.glBegin(GL11.GL_TRIANGLES);
					for ( ; i < triangles.length && (t=triangles[i])!=null && currMtl == t.getMaterialID(); i++) {
						GL11.glTexCoord2f(t.uvw1.x, t.uvw1.y);
						GL11.glNormal3f(t.norm1.x, t.norm1.y, t.norm1.z);
						GL11.glVertex3f( (float)t.p1.pos.x, (float)t.p1.pos.y, (float)t.p1.pos.z);
						
						GL11.glTexCoord2f(t.uvw2.x, t.uvw2.y);
						GL11.glNormal3f(t.norm2.x, t.norm2.y, t.norm2.z);
						GL11.glVertex3f( (float)t.p2.pos.x, (float)t.p2.pos.y, (float)t.p2.pos.z);
						
						GL11.glTexCoord2f(t.uvw3.x, t.uvw3.y);
						GL11.glNormal3f(t.norm3.x, t.norm3.y, t.norm3.z);
						GL11.glVertex3f( (float)t.p3.pos.x, (float)t.p3.pos.y, (float)t.p3.pos.z);
					}
					GL11.glEnd();
				}
				
			GL11.glEndList();
			
			displayLists.put(groupName, pDisplayList);	// add list pointer to the map
		}
	}

	
	
	@Override
	public void renderGroup(String groupName) {
		Integer pList = displayLists.get(groupName);
		if(pList == null){
			System.err.println("Group " + groupName + "does not exist.");
			return;
		}
		
		GL11.glCallList(pList);
	}

}
