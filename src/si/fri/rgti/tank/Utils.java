package si.fri.rgti.tank;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Cylinder;
import org.lwjgl.util.glu.GLU;

/**
 * 
 * @author Jani
 * 
 */
public class Utils {
	public static final int AXIS_X = 0;
	public static final int AXIS_Y = 1;
	public static final int AXIS_Z = 2;

	/**
	 * Draws a cylinder
	 * 
	 * @param radius
	 * @param halfHeight
	 * @param upAxis
	 *            0=X, 1=Y, 2=Z
	 * @param slices
	 * @param stacks
	 */
	public static void drawCylinder(float radius, float halfHeight, int upAxis,
			int slices, int stacks) {
		GL11.glPushMatrix();
		switch (upAxis) {
		case 0:
			GL11.glRotatef(-90f, 0.0f, 1.0f, 0.0f);
			GL11.glTranslatef(0.0f, 0.0f, -halfHeight);
			break;
		case 1:
			GL11.glRotatef(-90.0f, 1.0f, 0.0f, 0.0f);
			GL11.glTranslatef(0.0f, 0.0f, -halfHeight);
			break;
		case 2:
			GL11.glTranslatef(0.0f, 0.0f, -halfHeight);
			break;
		}

		// The gluCylinder subroutine draws a cylinder that is oriented along
		// the z axis.
		// The base of the cylinder is placed at z = 0; the top of the cylinder
		// is placed at z=height.
		// Like a sphere, the cylinder is subdivided around the z axis into
		// slices and along the z axis into stacks.

		Cylinder c = new Cylinder();
		c.setDrawStyle(GLU.GLU_SILHOUETTE);
		c.draw(radius, radius, halfHeight * 2, slices, stacks);

		GL11.glPopMatrix();
	}

	public static Vector3f calcNormal(Point3f points2, Point3f points3, Point3f points4) {
		float[] vec1 = substract(points2, points3);
		float[] vec2 = substract(points2, points4);
		Vector3f n = new Vector3f ( vec1[1] * vec2[2] - vec1[2] * vec2[1],
				vec1[2] * vec2[0] - vec1[0] * vec2[2],
				vec1[0] * vec2[1] - vec1[1] * vec2[0] );
		n.normalize();
		return n;
	}
	private static float[] substract(Point3f points2, Point3f points3) {
		return new float[] { points3.x - points2.x, points3.y - points2.y,
				points3.z - points2.z };
	}

	/**
	 * Class for drawing quads
	 * 
	 * @author Jani
	 */
	static class QuadDrawer {
		/**
		 * Array of vertices from where the quad vertices are taken
		 */
		private Point3f[] vertices;

		/**
		 * Constructor
		 * 
		 * @param vertices
		 *            Array of vertices from where the quad vertices are taken
		 */
		public QuadDrawer(Point3f[] vertices) {
			this.vertices = vertices;
		}

		/**
		 * Draw a quad using vertices in the order given by indices
		 * 
		 * @param indices
		 *            order of the vertices used to draw a quad
		 * @param bothSides
		 *            draw both sides of the polygon?
		 */
		public void drawQuad(int[] indices, boolean bothSides) {
			Vector3f normal = calcNormal(vertices[indices[0]], vertices[indices[1]], vertices[indices[2]]);
			GL11.glNormal3f(normal.x, normal.y, normal.z);
			for (int i : indices)
				GL11.glVertex3f(vertices[i].x, vertices[i].y, vertices[i].z);
		}

		/**
		 * Draws a quad, same as {@link #drawQuad(int[], boolean)} with false as
		 * the socond parameter
		 * 
		 * @param indices
		 */
		public void drawQuad(int[] indices) {
			drawQuad(indices, false);
		}
	}

	/**
	 * Transformations, copied from my homework
	 * @author Jani
	 */
	static class Transformations {
		public static Matrix4f rotateX(float alpha) {
			float cos = (float) Math.cos(alpha);
			float sin = (float) Math.sin(alpha);
			return new Matrix4f(new float[] { 1, 0, 0, 0, 0, cos, -1 * sin, 0,
					0, sin, cos, 0, 0, 0, 0, 1 });
		}

		public static Matrix4f rotateY(float alpha) {
			float cos = (float) Math.cos(alpha);
			float sin = (float) Math.sin(alpha);
			return new Matrix4f(new float[] { cos, 0, sin, 0, 0, 1, 0, 0,
					-1 * sin, 0, cos, 0, 0, 0, 0, 1 });
		}

		public static Matrix4f rotateZ(float alpha) {
			float cos = (float) Math.cos(alpha);
			float sin = (float) Math.sin(alpha);
			return new Matrix4f(new float[] { cos, -1 * sin, 0, 0, sin, cos, 0,
					0, 0, 0, 1, 0, 0, 0, 0, 1 });
		}

		public static Matrix4f translate(float dx, float dy, float dz) {
			return new Matrix4f(new float[] { 1, 0, 0, dx, 0, 1, 0, dy, 0, 0,
					1, dz, 0, 0, 0, 1 });
		}

		public static Matrix4f scale(float sx, float sy, float sz) {
			return new Matrix4f(new float[] { sx, 0, 0, 0, 0, sy, 0, 0, 0, 0,
					sz, 0, 0, 0, 0, 1 });
		}

		public static Matrix4f perspective(float d) {
			return new Matrix4f(new float[] { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1,
					0, 0, 0, 1 / d, 0 });
		} // primerna vrednost je d=4

	}

}
