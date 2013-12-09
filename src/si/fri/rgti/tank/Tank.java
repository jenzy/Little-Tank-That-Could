package si.fri.rgti.tank;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;

import si.fri.rgti.tank.ModelLoader.Model;
import si.fri.rgti.tank.TankGun.AmmoInfo;
import si.fri.rgti.tank.Utils.Transformations;
import si.fri.rgti.tank.sound.Sound;

import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.linearmath.MatrixUtil;

/**
 * 
 * @author Jani
 *
 */
public class Tank extends Vehicle implements IControlableTank{
	private enum TankParts {BODY, TURRET, GUN}
	private final TankConstructionInfo info;

	private float turretAngle = 0;
	private float gunAngle = 0;
	private TankGun gun;
	
	public Tank(Vector3f position, Vector3f size, DynamicsWorld physicsWorld, TankConstructionInfo info, Model model) {
		super(position, size, physicsWorld, info, model);
		this.info = info;
		gun = new TankGun(info.ammo, info.reloadTime);
	}
	
	/**
	 * Creates a T-90 Tank with all the properties specific to T-90
	 * @param position		position in the world
	 * @param physicsWorld	physicsWorld for the tank to live in
	 * @return
	 */
	public static Tank createTankT90(Vector3f position, DynamicsWorld physicsWorld){
		Vector3f size = new Vector3f(3.5f, 1.5f, 7f);
		
		TankConstructionInfo constructionInfo = new TankConstructionInfo();
		constructionInfo.setSuspensionStiffness(50f);
		constructionInfo.frictionSlip = 100f;
		constructionInfo.rollInfluence = 0.001f;
		constructionInfo.connectionHeight = 0.5f;
		constructionInfo.suspensionRestLength = 0.5f; // max length of suspension
		constructionInfo.wheelDirectionCS = new Vector3f(0, -1, 0);
		constructionInfo.wheelAxleCS = new Vector3f(-1, 0, 0);
		constructionInfo.wheelWidth = 0.6f;
		constructionInfo.wheelRadius = 1.5f;
		
		constructionInfo.maxEngineForce = 1500; //150
		constructionInfo.maxBrakingForce = 250;//100
		constructionInfo.mass = 1000f;
		
		constructionInfo.turretAngleIncrement = 0.3f;
		constructionInfo.gunAngleIncrement = 0.3f;
		constructionInfo.gunAngleDownClamp = -7f;
		constructionInfo.gunAngleUpClamp = 30f;
		constructionInfo.gunPivotOffsetY = 0.5f;
		constructionInfo.gunPivotOffsetZ = 1.5f;
		
		constructionInfo.ammo = 15;
		constructionInfo.reloadTime = 5000;
		
		constructionInfo.makeModelGroups = true;
		
		Tank t = new Tank(position, size, physicsWorld, constructionInfo, Model.Tank);
		
		return t;
	}

	
	@Override
	public void render() {
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glPushMatrix();
			// Get the transformation matrix from JBullet.
			rigidBody.getMotionState().getWorldTransform(transform);
			transform.getOpenGLMatrix(transformationMatrix);
	
			// Put the transformation matrix into a FloatBuffer.
			transformationBuffer.clear();
			transformationBuffer.put(transformationMatrix);
			transformationBuffer.flip();
			
			//Apply the transformation
			GL11.glMultMatrix(transformationBuffer);
			
			//Draw the bounding box
			if(debug_BoundingBox)
				GL11.glCallList(pListBoundingBox);
			
			//model3D.render();
			renderPart(TankParts.BODY);
			
			GL11.glRotatef(turretAngle, 0f, 1f, 0f);
			renderPart(TankParts.TURRET);
			
			GL11.glTranslatef(0, info.gunPivotOffsetY, info.gunPivotOffsetZ);
			GL11.glRotatef(-gunAngle, 1f, 0f, 0f);
			GL11.glTranslatef(0, -info.gunPivotOffsetY, -info.gunPivotOffsetZ);
			renderPart(TankParts.GUN);
			
		GL11.glPopMatrix();
		
		// Draw the wheels	
		if(debug_BoundingBox)
			super.drawWheels();

		GL11.glDisable(GL11.GL_TEXTURE_2D);
	}
	
	private void renderPart(TankParts part){
		switch(part){
		case BODY:
			model3D.renderGroup("MainBody");
			model3D.renderGroup("MainBody2");
			break;
		case GUN:
			model3D.renderGroup("MainGun");
			break;
		case TURRET:
			model3D.renderGroup("MachineGun");
			model3D.renderGroup("Turret");
			break;
		}
	}

	
	
	
	@Override
	public Vector3f getForwardVector() {
		Vector3f fwd = vehicle.getForwardVector(new Vector3f());
		if(fwd.z>0)
			Transformations.rotateX((float) Math.toRadians(-gunAngle)).transform(fwd);
		else
			Transformations.rotateX((float) Math.toRadians(gunAngle)).transform(fwd);
		Transformations.rotateY((float) Math.toRadians(turretAngle)).transform(fwd);
		//fwd.scale(-1);
		return fwd;
		//return super.getForwardVector();
	}

	@Override
	public void turnTurretLeft() {
		turretAngle = (turretAngle + info.turretAngleIncrement) % 360;
	}

	@Override
	public void turnTurretRight() {
		turretAngle = (turretAngle - info.turretAngleIncrement) % 360;
	}

	@Override
	public void gunUp() {
		float newAngle = gunAngle + info.gunAngleIncrement;
		if( newAngle > info.gunAngleUpClamp )
			gunAngle = info.gunAngleUpClamp;
		else
			gunAngle = newAngle;
	}

	@Override
	public void gunDown() {
		float newAngle = gunAngle - info.gunAngleIncrement;
		if( newAngle < info.gunAngleDownClamp )
			gunAngle = info.gunAngleDownClamp;
		else
			gunAngle = newAngle;
	}

	static class TankConstructionInfo extends VehicleConstructionInfo{
		public float turretAngleIncrement = 0.5f;
		public float gunAngleIncrement = 0.5f;
		public float gunAngleDownClamp = -4.5f;
		public float gunAngleUpClamp = 45f;
		public float gunPivotOffsetY = 0.5f;
		public float gunPivotOffsetZ = 1.5f;
		public int ammo = 15;
		public int reloadTime = 5000; //ms
		
	}

	@Override
	public void fire(boolean isKeyDown) {
		if(!isKeyDown){ //key up
			
			if(gun.fire()){
				rigidBody.getWorldTransform(transform);
				Vector3f posOrigin = rigidBody.getCenterOfMassPosition(new Vector3f());
				
				Matrix3f mTankRotation = transform.basis;
				Matrix3f mGunRotation = new Matrix3f();
				Matrix3f mTurretRotation = new Matrix3f();
				MatrixUtil.setEulerZYX(mGunRotation, (float) Math.toRadians(-gunAngle), 0, 0);
				MatrixUtil.setEulerZYX(mTurretRotation, 0, (float) Math.toRadians(turretAngle), 0);
				
				Vector3f vStartingPoint = new Vector3f(0, 0, 5);
				mGunRotation.transform(vStartingPoint);
				
				Vector3f velocity = new Vector3f(vStartingPoint);
				
				vStartingPoint.add(new Vector3f(0, info.gunPivotOffsetY, info.gunPivotOffsetZ));
				
				mTurretRotation.transform(vStartingPoint);
				mTurretRotation.transform(velocity);
				//m.mul(m2);
				
				mTankRotation.transform(vStartingPoint);
				mTankRotation.transform(velocity);
				//m.transform(v);
				vStartingPoint.add(posOrigin);
				
				/*
				//Debugging
				org.lwjgl.util.glu.Sphere renderBody = new org.lwjgl.util.glu.Sphere();
				GL11.glPushMatrix();
				GL11.glTranslatef(vStartingPoint.x, vStartingPoint.y, vStartingPoint.z);
				renderBody.draw(0.1f, 15, 15);
				GL11.glPopMatrix();
				GL11.glPushMatrix();
				GL11.glTranslatef(vStartingPoint.x + velocity.x, vStartingPoint.y + velocity.y, vStartingPoint.z + velocity.z);
				renderBody.draw(0.1f, 15, 15);
				GL11.glPopMatrix();*/
				/*
				
				Vector3f posOrigin = rigidBody.getCenterOfMassPosition(new Vector3f());
				Vector3f fwd = vehicle.getForwardVector(new Vector3f());
				
				Vector3f vStartingPoint = new Vector3f(0, 0, 5);
				//v.scale(5);
				
				Transformations.rotateX((float) Math.toRadians(-gunAngle)).transform(vStartingPoint);	// up/down rotation
				
				Vector3f velocity = new Vector3f(vStartingPoint);
				
				vStartingPoint.add(new Vector3f(0, info.gunPivotOffsetY, info.gunPivotOffsetZ));			// 
				
				float totalAngle = (float) (Math.atan2(fwd.x, fwd.z) + Math.toRadians(turretAngle));
				Matrix4f M = Transformations.rotateY(totalAngle);
				M.transform(vStartingPoint);				// rotate the starting point around Y
				M.transform(velocity);		// rotate the velocity around Y
				
				vStartingPoint.add(posOrigin);	*/		
				
				Sound.playASound(Sound.FIRE, vStartingPoint);
				Projectile.createTankProjectile(vStartingPoint, physicsWorld, velocity);
			}

		}
	}
	
	public AmmoInfo getAmmoInfo(){
		return gun.getAmmoInfo();
	}

}
