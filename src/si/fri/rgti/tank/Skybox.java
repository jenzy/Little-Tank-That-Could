package si.fri.rgti.tank;

import java.nio.IntBuffer;

import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.util.Renderable;

/**
 * @author Jani
 */
public class Skybox implements Renderable{
	/**
	 * Textures for each of the cube's faces
	 */
	private IntBuffer textures;
	/**
	 * For getting the location of where to draw the skybox
	 */
	private Camera camera;
	/**
	 * display list, über fast rendering
	 */
	private int displayList;
	
	/**
	 * Creates and renders a skybox
	 * @param skyboxFolder	path to folder where face textures are.
	 * 						(front.jpg, right.jpg, back.jpg, left.jpg, up.jpg, down.jpg)
	 * @param camera		Camera, so the skybox can get the location of where to render
	 */
	public Skybox(String skyboxFolder, Camera camera) {
		System.out.println("Loading skybox");
		this.camera = camera;
        
        textures = Texture.loadTextures2D(new String[]{
        		skyboxFolder + "/front.jpg", skyboxFolder + "/right.jpg", skyboxFolder + "/back.jpg", 
        		skyboxFolder + "/left.jpg", skyboxFolder + "/up.jpg"/*, skyboxFolder + "/down.jpg"*/}, true);
        
        initDisplayList();
        System.out.println("Skybox loaded\n");
    }
	
	private void initDisplayList(){
		float offset = 8f;
		displayList = GL11.glGenLists(1);
		GL11.glNewList(displayList, GL11.GL_COMPILE);
		
			// Render the front quad
		    GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures.get(0));
		    clampToEdge();
		    GL11.glBegin(GL11.GL_QUADS);
			    GL11.glTexCoord2f(1, 0); GL11.glVertex3f( -offset,  offset, offset );
			    GL11.glTexCoord2f(0, 0); GL11.glVertex3f(  offset,  offset, offset );
			    GL11.glTexCoord2f(0, 1); GL11.glVertex3f(  offset, -offset, offset );
			    GL11.glTexCoord2f(1, 1); GL11.glVertex3f( -offset, -offset, offset );	
		    GL11.glEnd();
		
		    // Render the left quad
		    GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures.get(1));
		    clampToEdge();
		    GL11.glBegin(GL11.GL_QUADS);
			    GL11.glTexCoord2f(1, 0); GL11.glVertex3f(  offset,  offset,  offset );
			    GL11.glTexCoord2f(0, 0); GL11.glVertex3f(  offset,  offset, -offset );
			    GL11.glTexCoord2f(0, 1); GL11.glVertex3f(  offset, -offset, -offset );
	            GL11.glTexCoord2f(1, 1); GL11.glVertex3f(  offset, -offset,  offset );
		    GL11.glEnd();
		    
		    // Render the back quad
		    GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures.get(2));
		    clampToEdge();
		    GL11.glBegin(GL11.GL_QUADS);
			    GL11.glTexCoord2f(1, 0); GL11.glVertex3f(  offset,  offset,  -offset );
			    GL11.glTexCoord2f(0, 0); GL11.glVertex3f( -offset,  offset,  -offset );
			    GL11.glTexCoord2f(0, 1); GL11.glVertex3f( -offset, -offset,  -offset );
			    GL11.glTexCoord2f(1, 1); GL11.glVertex3f(  offset, -offset,  -offset );
		    GL11.glEnd();
		    
		    // Render the right quad
		    GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures.get(3));
		    clampToEdge();
		    GL11.glBegin(GL11.GL_QUADS);
			    GL11.glTexCoord2f(1, 0); GL11.glVertex3f( -offset,  offset, -offset );
			    GL11.glTexCoord2f(0, 0); GL11.glVertex3f( -offset,  offset,  offset );
			    GL11.glTexCoord2f(0, 1); GL11.glVertex3f( -offset, -offset,  offset );
			    GL11.glTexCoord2f(1, 1); GL11.glVertex3f( -offset, -offset, -offset );
		    GL11.glEnd();
		    
		    // Render the top quad
		    GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures.get(4));
		    clampToEdge();
		    GL11.glBegin(GL11.GL_QUADS);
			    GL11.glTexCoord2f(1, 0); GL11.glVertex3f(  -offset,  offset, -offset );
			    GL11.glTexCoord2f(0, 0); GL11.glVertex3f(  offset,  offset, -offset );
			    GL11.glTexCoord2f(0, 1); GL11.glVertex3f(  offset,  offset,  offset );
			    GL11.glTexCoord2f(1, 1); GL11.glVertex3f( -offset,  offset,  offset );
		    GL11.glEnd();
		    
		    // Render the bottom quad
		    // Why bother?
	//	    GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures.get(5));
	//	    clampToEdge();
	//	    GL11.glBegin(GL11.GL_QUADS);
	//	            GL11.glTexCoord2f(1, 1); GL11.glVertex3f( -offset, -offset, -offset );
	//	            GL11.glTexCoord2f(1, 0); GL11.glVertex3f( -offset, -offset,  offset );
	//	            GL11.glTexCoord2f(0, 0); GL11.glVertex3f(  offset, -offset,  offset );
	//	            GL11.glTexCoord2f(0, 1); GL11.glVertex3f(  offset, -offset, -offset );
	//	    GL11.glEnd();
		
		GL11.glEndList();
	}
        
	@Override
	public void render(){
		Vector3f player = camera.getEyeLocation();
		if(player == null) return;
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_LIGHTING);
		
        GL11.glPushMatrix();
        
        GL11.glTranslatef(player.x, player.y, player.z);
        GL11.glColor4f(1,1,1,1); // Just in case we set all vertices to white.
        
        // do the actual rendering
        GL11.glCallList(displayList);

	    // Restore enable bits and matrix
	    GL11.glEnable(GL11.GL_LIGHTING);
	    GL11.glEnable(GL11.GL_DEPTH_TEST);
	    GL11.glDisable(GL11.GL_TEXTURE_2D);
	    GL11.glPopMatrix();
	}
	
	/**
	 * clamp textures, so edges get don't create a line in between
	 */
    private void clampToEdge() {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
    }

}
