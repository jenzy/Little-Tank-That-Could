package si.fri.rgti.tank;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.imageio.ImageIO;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Point3i;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Renderable;

import com.bulletphysics.collision.shapes.BvhTriangleMeshShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.OptimizedBvh;
import com.bulletphysics.collision.shapes.TriangleIndexVertexArray;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.MotionState;
import com.bulletphysics.linearmath.Transform;

/**
 * Loads and renders a terrain (heightmap)
 * @author Jani
 */
public class Terrain implements Renderable{
	private Point3f[] groundVertices;
	private Vector3f[] groundNormals;
	private Point3i[] groundTriangles;
	private int pDisplayList;
	private IntBuffer texture;
	private String mapfile;
	
	private int numXVertices;
	private int numZVertices;

	private Terrain(String map) {	this.mapfile = map; }
	
	public static Terrain createTerrain(String map, DynamicsWorld physicsWorld){
		System.out.println("\nLoading terrain " + map);
		
		Terrain t = new Terrain(map);
		t.loadHeightmap(map + "_heightmap.bmp", 3, 5);
		t.loadTexture(map + "_texture.jpg");
		//t.loadTexture("maps/checker.jpg");
		t.initPhysics(physicsWorld);
		t.calculateNormals();
		t.initList();
		
		System.out.println("Map loaded\n");
		return t;
	}

	/**
	 * Loads a heightmap from a .bmp file
	 * @param file			bmp file
	 * @param triangleSize	size of triangles (scales X and Z coordinates)
	 */
	private void loadHeightmap(String file, float triangleSize, float divHeight) {
		System.out.println("Loading heightmap");
		try {
			BufferedImage heightmapImage = ImageIO.read(new File(file));
			
			numZVertices = heightmapImage.getHeight();
			numXVertices = heightmapImage.getWidth();
			groundVertices = new Point3f[numXVertices*numZVertices];
			System.out.println("Map size: " + numXVertices + "x" + numZVertices + " vertices");
			
			Color colour;
			int index = 0;
			for (int x = 0; x < numXVertices; x++) {
				for (int z = 0; z < numZVertices; z++) {
					colour = new Color(heightmapImage.getRGB(x, z));
					Point3f tmp = new Point3f(
							(float) (x - numXVertices*0.5) * triangleSize,
							(float) (colour.getRed() / divHeight),
							(float) (z - numZVertices*0.5) * triangleSize
						);
					groundVertices[index++] = tmp;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	private void loadTexture(String file){
		System.out.println("Loading texture " + file);
		texture = Texture.loadTextures2D(new String[]{file}, true);
	}
	
	/**
	 * Initializes the terrains' rigidbody and collision mesh
	 * @param physicsWorld	the world for the terrain to live in
	 */
	private void initPhysics(DynamicsWorld physicsWorld){
		int totalTriangles = 2 * (numXVertices - 1) * (numZVertices - 1);
		groundTriangles = new Point3i[totalTriangles];
		System.out.println("Total triangles: " + totalTriangles);
		
		System.out.println("Initializing triangles & collision mesh");
		ByteBuffer gVertices = ByteBuffer.allocateDirect(groundVertices.length * 3 * (Float.SIZE/8)).order(ByteOrder.nativeOrder());
		ByteBuffer gIndices = ByteBuffer.allocateDirect(totalTriangles * 3 * (Float.SIZE/8)).order(ByteOrder.nativeOrder());
		
		// put vertices in a byte buffer
		gVertices.clear();
		for(int i=0; i<groundVertices.length; i++){
			Point3f tmp = groundVertices[i];
			gVertices.putFloat((i*3 + 0) * 4, tmp.x);
			gVertices.putFloat((i*3 + 1) * 4, tmp.y);
			gVertices.putFloat((i*3 + 2) * 4, tmp.z);
		}
		
		// Create triangles for the collision mesh (and rendering, but now i'm using triangle strips) 
		gIndices.clear();
		int index = 0;
		for (int i = 0; i < numXVertices - 1; i++) {
			for (int j = 0; j < numZVertices - 1; j++) {
				Point3i p = new Point3i(j * numXVertices + i, j * numXVertices + i + 1, (j + 1) * numXVertices + i + 1);
				gIndices.putInt(p.x);
				gIndices.putInt(p.y);
				gIndices.putInt(p.z);
				groundTriangles[index++] = p;

				p = new Point3i(j*numXVertices + i, (j+1)*numXVertices + i + 1, (j + 1) * numXVertices + i);
				gIndices.putInt(p.x);
				gIndices.putInt(p.y);
				gIndices.putInt(p.z);
				groundTriangles[index++] = p;
			}
		}
		gIndices.flip();
		
		int vertStride = 3 * (Float.SIZE/8);
		int indexStride = 3 * (Float.SIZE/8);
		TriangleIndexVertexArray indexVertexArrays = new TriangleIndexVertexArray(
				totalTriangles, gIndices, indexStride, groundVertices.length, gVertices, vertStride);

		boolean useQuantizedAabbCompression = true;
		BvhTriangleMeshShape trimeshShape;
		File file = new File(mapfile + "_serialized.bin");
		if(file.exists()){
			// load from disk
			trimeshShape = new BvhTriangleMeshShape(indexVertexArrays, useQuantizedAabbCompression, false);

			OptimizedBvh bvh = null;
			try {
				ObjectInputStream in = new ObjectInputStream(new GZIPInputStream(new FileInputStream(file)));
				bvh = (OptimizedBvh)in.readObject();
				in.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}

			trimeshShape.setOptimizedBvh(bvh);
			trimeshShape.recalcLocalAabb();
		} else {
			// Serialize to disk
			trimeshShape = new BvhTriangleMeshShape(indexVertexArrays, useQuantizedAabbCompression);

			try {
				ObjectOutputStream out = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(file)));
				out.writeObject(trimeshShape.getOptimizedBvh());
				out.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		//BvhTriangleMeshShape trimeshShape = new BvhTriangleMeshShape(indexVertexArrays, useQuantizedAabbCompression);
		
		CollisionShape groundShape = trimeshShape;
       
		MotionState groundMotionState = new DefaultMotionState(new Transform(new Matrix4f(
                new Quat4f(0, 0, 0, 1),
                new Vector3f(0, 0, 0), 1.0f)));
        RigidBodyConstructionInfo groundBodyConstructionInfo = new RigidBodyConstructionInfo(0, groundMotionState, groundShape, new Vector3f(0, 0, 0));
        groundBodyConstructionInfo.restitution = 0.25f;
        RigidBody groundRigidBody = new RigidBody(groundBodyConstructionInfo);
        groundRigidBody.setUserPointer(this);
        physicsWorld.addRigidBody(groundRigidBody);
	}
	
	/**
	 * Calculates a normal for each vertex - normalized sum of 3 adjoining triangles' normals
	 * No more flat shading :)
	 */
	private void calculateNormals(){
		System.out.println("Calculating normals");
		groundNormals = new Vector3f[groundVertices.length];
		for (int x = 0; x < numXVertices; x++) {
			for (int z = 0; z < numZVertices; z++) {
				Vector3f n = new Vector3f(0, 0, 0);
				
				Point3f thisVertex = groundVertices[ x*numZVertices+z ];
				if(x!=0 && z!=0){
					Point3f p1 = groundVertices[ (x-1)*numZVertices+z ];
					Point3f p2 = groundVertices[ (x)*numZVertices+z-1 ];
					Vector3f newNormal = Utils.calcNormal(thisVertex, p2, p1);
					n.add(newNormal);
				}
				if(x!=numXVertices-1 && z!=numZVertices-1){
					Point3f p1 = groundVertices[ (x+1)*numZVertices+z ];
					Point3f p2 = groundVertices[ (x)*numZVertices+z+1 ];
					Vector3f newNormal = Utils.calcNormal(thisVertex, p2, p1);
					n.add(newNormal);
				}
				if(x!=0 && z!=numZVertices-1){
					Point3f p1 = groundVertices[ (x-1)*numZVertices+z ];
					Point3f p2 = groundVertices[ (x)*numZVertices+z+1 ];
					Vector3f newNormal = Utils.calcNormal(thisVertex, p1, p2);
					n.add(newNormal);
				}
				if(x!=numXVertices-1 && z!=0){
					Point3f p1 = groundVertices[ (x+1)*numZVertices+z ];
					Point3f p2 = groundVertices[ (x)*numZVertices+z-1 ];
					Vector3f newNormal = Utils.calcNormal(thisVertex, p1, p2);
					n.add(newNormal);
				}
				n.normalize();
				groundNormals[ x*numZVertices+z ] = n;
			}
		}
	}
	
	/**
	 * Initializes a display list for über fast rendering.
	 * Uses triangle strips.
	 * (code in comments uses actual triangles, not strips)
	 */
	public void initList(){
		System.out.println("Initializing display list");
		pDisplayList = GL11.glGenLists(1);
		
		GL11.glNewList(pDisplayList, GL11.GL_COMPILE);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			
			GL11.glMaterial(GL11.GL_FRONT, GL11.GL_DIFFUSE, BaseWindow.allocFloats(new float[] {0.5f, 0.7f, 0.5f, 1f}));
	        GL11.glMaterial(GL11.GL_FRONT, GL11.GL_AMBIENT, BaseWindow.allocFloats(new float[] {0.1f, 0.1f, 0.1f, 1f}));
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.get(0));
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
			
//			GL11.glBegin(GL11.GL_TRIANGLES);
//			for (Point3i triangle : groundTriangles) {
//				Point3f p1 = groundVertices.get(triangle.x);
//				Vector3f n1 = groundNormals.get(triangle.x);
//				Point3f p2 = groundVertices.get(triangle.y);
//				Vector3f n2 = groundNormals.get(triangle.y);
//				Point3f p3 = groundVertices.get(triangle.z);
//				Vector3f n3 = groundNormals.get(triangle.z);
//				
//				Vector3f n;
//				n = Utils.calcNormal(p1, p2, p3);
//				GL11.glNormal3f(n.x, n.y, n.z);
//	
//				GL11.glNormal3f(n1.x, n1.y, n1.z);
//				GL11.glTexCoord2f(0, 0);
//				GL11.glVertex3f(p1.x, p1.y, p1.z);
//				
//				GL11.glNormal3f(n2.x, n2.y, n2.z);
//				GL11.glTexCoord2f(0, 1);
//				GL11.glVertex3f(p2.x, p2.y, p2.z);
//
//				GL11.glNormal3f(n3.x, n3.y, n3.z);
//				GL11.glTexCoord2f(1, 1);
//				GL11.glVertex3f(p3.x, p3.y, p3.z);
//	
//			}
//			GL11.glEnd();
			
			for (int z = 0; z < numZVertices - 1; z++) {
	            // Render a triangle strip for each 'strip'.
	            GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
	            for (int x = numXVertices-1; x >= 0; x--) {
	            	
	            	// Take a vertex from the current strip
	            	Point3f current = groundVertices[ z*numXVertices + x ];	
	            	Vector3f currentNormal = groundNormals[ z*numXVertices + x ];
	            	GL11.glTexCoord2f(z, x);
	            	GL11.glNormal3f(currentNormal.x, currentNormal.y, currentNormal.z);
	            	GL11.glVertex3f(current.x, current.y, current.z);
	            	
	            	// Take a vertex from the next strip
	            	Point3f next = groundVertices[ (z+1)*numXVertices + x ];	
	            	Vector3f nextNormal = groundNormals[ (z+1)*numXVertices + x ];
	            	GL11.glTexCoord2f(z+1, x);
	            	GL11.glNormal3f(nextNormal.x, nextNormal.y, nextNormal.z);
	            	GL11.glVertex3f(next.x, next.y, next.z);
	            }
	            GL11.glEnd();
			}
			GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEndList();
	}
	
	@Override
	public void render(){
		GL11.glCallList(pDisplayList);
	}

}
