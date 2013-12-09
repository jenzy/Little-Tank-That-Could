package si.fri.rgti.tank;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;

import si.fri.rgti.tank.ModelLoader.Model;
import si.fri.rgti.tank.sound.Sound;

import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.vehicle.DefaultVehicleRaycaster;
import com.bulletphysics.dynamics.vehicle.RaycastVehicle;
import com.bulletphysics.dynamics.vehicle.VehicleRaycaster;
import com.bulletphysics.dynamics.vehicle.VehicleTuning;
import com.bulletphysics.dynamics.vehicle.WheelInfo;
import com.bulletphysics.linearmath.MatrixUtil;
import com.bulletphysics.linearmath.Transform;

/**
 * 
 * @author Jani
 *
 */
public class Vehicle extends APhysicsModel implements IControlableVehicle, ITarget{
	
	private static final boolean debug_Controls = false;
	
	/**
	 * JBullet's RaycastVehicle - Physics
	 */
	protected RaycastVehicle vehicle;

	/**
	 * Vehicle's maximum engine force - applied to wheels
	 */
	private final int maxEngineForce;
	/**
	 * Vehicle's maximum braking force - applied to wheels
	 */
	private final int maxBrakingForce;

	
	private final VehicleConstructionInfo constructionInfo;

	// Wheel indices
	public static final int WHEEL_FRONT_LEFT = 0;
	public static final int WHEEL_FRONT_RIGHT = 1;
	public static final int WHEEL_BACK_RIGHT = 2;
	public static final int WHEEL_BACK_LEFT = 3;
	
	//Sounds
	private static final int soundGO = Sound.TANKUP;
	private static final int soundIDLE = Sound.IDLE;
	
	
	public Vehicle(Vector3f position, Vector3f size, DynamicsWorld physicsWorld, VehicleConstructionInfo info, Model model) {
		super(position, size, physicsWorld, info, model);
		
		this.constructionInfo = info;
		this.maxEngineForce = info.maxEngineForce;
		this.maxBrakingForce = info.maxBrakingForce;
		
		// create vehicle
		{
			VehicleTuning tuning = new VehicleTuning();
			VehicleRaycaster vehicleRayCaster = new DefaultVehicleRaycaster(physicsWorld);
			vehicle = new RaycastVehicle(tuning, rigidBody, vehicleRayCaster);
			vehicle.setCoordinateSystem(0,1,2); // choose coordinate system
			physicsWorld.addVehicle(vehicle);

			boolean isFrontWheel = false; // Steerable front wheel, don't need 'em
			
			// Front left wheel
			Vector3f connectionPointCS = new Vector3f(halfSize.x -(0.3f*info.wheelWidth)/* + info.wheelWidth*/, info.connectionHeight, halfSize.z - info.wheelRadius);
			vehicle.addWheel(connectionPointCS, info.wheelDirectionCS, info.wheelAxleCS, info.suspensionRestLength ,info.wheelRadius, tuning, isFrontWheel);
			
			// Front right wheel
			connectionPointCS.set(-halfSize.x+(0.3f*info.wheelWidth) /*- info.wheelWidth*/, info.connectionHeight, halfSize.z - info.wheelRadius);
			vehicle.addWheel(connectionPointCS, info.wheelDirectionCS, info.wheelAxleCS, info.suspensionRestLength, info.wheelRadius, tuning, isFrontWheel);

			// Back right wheel
			connectionPointCS.set(-halfSize.x+(0.3f*info.wheelWidth) /*- info.wheelWidth*/, info.connectionHeight, -halfSize.z + info.wheelRadius);
			vehicle.addWheel(connectionPointCS, info.wheelDirectionCS, info.wheelAxleCS, info.suspensionRestLength, info.wheelRadius, tuning, isFrontWheel);

			// Back left wheel
			connectionPointCS.set(halfSize.x-(0.3f*info.wheelWidth) /*+ info.wheelWidth*/, info.connectionHeight, -halfSize.z + info.wheelRadius);
			vehicle.addWheel(connectionPointCS, info.wheelDirectionCS, info.wheelAxleCS, info.suspensionRestLength, info.wheelRadius, tuning, isFrontWheel);

			for (int i = 0; i < vehicle.getNumWheels(); i++) {
				WheelInfo wheel = vehicle.getWheelInfo(i);
				wheel.suspensionStiffness = info.suspensionStiffness;
				wheel.wheelsDampingRelaxation = info.wheelsDampingRelaxation;
				wheel.wheelsDampingCompression = info.wheelsDampingCompression;
				wheel.frictionSlip = info.frictionSlip;
				wheel.rollInfluence = info.rollInfluence;
			}
			
		}
	}
	
	@Override
	public void render() {
		super.render();
		
		// Draw the wheels	
		if(debug_BoundingBox){
			drawWheels();
		}
	}
	
	protected void drawWheels(){
		for (int i = 0; i < vehicle.getNumWheels(); i++) {
			GL11.glPushMatrix();
				vehicle.updateWheelTransform(i, true); // synchronize the wheels with the (interpolated) chassis worldtransform
				
				transform = vehicle.getWheelInfo(i).worldTransform;
				transform.getOpenGLMatrix(transformationMatrix);
	
				// Put the transformation matrix into a FloatBuffer.
				transformationBuffer.clear();
				transformationBuffer.put(transformationMatrix);
				transformationBuffer.flip();
			
				GL11.glMultMatrix(transformationBuffer);	// Apply the transformation
				
				GL11.glColor4f(0, 1, 0, 1);
				Utils.drawCylinder(constructionInfo.wheelRadius, constructionInfo.wheelWidth/2, Utils.AXIS_X, 30, 30);	// draw wheels (cylinders)
			
			GL11.glPopMatrix();
		}
	}

	@Override
	public Vector3f getPosition() {
		 return rigidBody.getCenterOfMassPosition(new Vector3f());
	}

	@Override
	public Vector3f getForwardVector() {
		return vehicle.getForwardVector(new Vector3f());
	}

	@Override
	public void goForward(boolean isFwdKeyDown) {
		if(isFwdKeyDown){
			vehicle.applyEngineForce(maxEngineForce, WHEEL_FRONT_LEFT);
			vehicle.applyEngineForce(maxEngineForce, WHEEL_BACK_LEFT);
			vehicle.applyEngineForce(maxEngineForce, WHEEL_FRONT_RIGHT);
			vehicle.applyEngineForce(maxEngineForce, WHEEL_BACK_RIGHT);
			if(debug_Controls) System.out.println("W down");
			
			Sound.stopSound(soundIDLE);
			Sound.playASound(soundGO, getPosition());
			
		} else {
			vehicle.applyEngineForce(0, WHEEL_FRONT_LEFT);
			vehicle.applyEngineForce(0, WHEEL_FRONT_RIGHT);
			vehicle.applyEngineForce(0, WHEEL_BACK_RIGHT);
			vehicle.applyEngineForce(0, WHEEL_BACK_LEFT);
			if(debug_Controls) System.out.println("W up");
			
			Sound.stopSound(soundGO);
			Sound.playASound(soundIDLE, getPosition());
		}
	}

	@Override
	public void goBackward(boolean isBackKeyDown) {
		if(isBackKeyDown){
			vehicle.setBrake(maxBrakingForce, WHEEL_BACK_LEFT);
			vehicle.setBrake(maxBrakingForce, WHEEL_BACK_RIGHT);
			if(debug_Controls) System.out.println("S down");
		} else {
			vehicle.setBrake(0, WHEEL_BACK_LEFT);
			vehicle.setBrake(0, WHEEL_BACK_RIGHT);
			if(debug_Controls) System.out.println("S up");
		}
	}

	@Override
	public void goRight(boolean isRightKeyDown, boolean isFwdKeyDown) {
		if(isRightKeyDown){
			vehicle.applyEngineForce(0, WHEEL_FRONT_RIGHT);
			vehicle.applyEngineForce(0, WHEEL_BACK_RIGHT);
			vehicle.setBrake(maxBrakingForce, WHEEL_BACK_RIGHT);
			vehicle.setBrake(maxBrakingForce, WHEEL_FRONT_RIGHT);
			if(isFwdKeyDown){
				vehicle.applyEngineForce(3*maxEngineForce, WHEEL_FRONT_LEFT);
				vehicle.applyEngineForce(3*maxEngineForce, WHEEL_BACK_LEFT);
			}
			if(debug_Controls) System.out.println("D down");
		} else {
			vehicle.setBrake(0, WHEEL_BACK_RIGHT);
			vehicle.setBrake(0, WHEEL_FRONT_RIGHT);
			if( isFwdKeyDown ) 
				goForward(true);
			if(debug_Controls) System.out.println("D up");
		}
	}

	@Override
	public void goLeft(boolean isLeftKeyDown, boolean isFwdKeyDown) {
		if(isLeftKeyDown){
			vehicle.applyEngineForce(0, WHEEL_FRONT_LEFT);
			vehicle.applyEngineForce(0, WHEEL_BACK_LEFT);
			vehicle.setBrake(maxBrakingForce, WHEEL_BACK_LEFT);
			vehicle.setBrake(maxBrakingForce, WHEEL_FRONT_LEFT);
			if(isFwdKeyDown){
				vehicle.applyEngineForce(3*maxEngineForce, WHEEL_FRONT_RIGHT);
				vehicle.applyEngineForce(3*maxEngineForce, WHEEL_BACK_RIGHT);
			}
			if(debug_Controls) System.out.println("D down");
		} else {
			vehicle.setBrake(0, WHEEL_BACK_LEFT);
			vehicle.setBrake(0, WHEEL_FRONT_LEFT);
			if( isFwdKeyDown ) 
				goForward(true);
			if(debug_Controls) System.out.println("D up");
		}
	}
	

	@Override
	public void reset() {
		rigidBody.getWorldTransform(transform);
		Matrix3f rotation = new Matrix3f();
		MatrixUtil.setEulerZYX(rotation, 0, 0, 0);
		Vector3f position = new Vector3f(transform.origin);
		position.y += 2f;
		transform = new Transform(new Matrix4f(rotation, position, 1f));
		rigidBody.setWorldTransform(transform);
	}

	
	public static class VehicleConstructionInfo extends ModelConstructionInfo{
		/**
		 * The stiffness constant for the suspension. 10.0 - Offroad buggy, 50.0 -
		 * Sports car, 200.0 - F1 Car
		 */
		private float suspensionStiffness = 30;
		/**
		 * The damping coefficient for when the suspension is compressed. Set to k *
		 * 2.0 * btSqrt(m_suspensionStiffness) so k is proportional to critical
		 * damping. k = 0.0 undamped & bouncy, k = 1.0 critical damping k = 0.1 to
		 * 0.3 are good values
		 */
		public float wheelsDampingCompression = (float) (0.5f * 2.0f * Math.sqrt(suspensionStiffness));
		/**
		 * The damping coefficient for when the suspension is expanding. See the
		 * comments for m_wheelsDampingCompression for how to set k.
		 * _wheelsDampingRelaxation should be slightly larger than
		 * m_wheelsDampingCompression, eg k = 0.2 to 0.5
		 */
		public float wheelsDampingRelaxation = (float) (0.8f * 2.0f * Math	.sqrt(suspensionStiffness));
		/**
		 * The coefficient of friction between the tire and the ground. Should be
		 * about 0.8 for realistic cars, but can increased for better handling. Set
		 * large (10000.0) for kart racers
		 */
		public float frictionSlip = 100f;
		/**
		 * Reduces the rolling torque applied from the wheels that cause the vehicle
		 * to roll over. This is a bit of a hack, but it's quite effective. 0.0 = no
		 * roll, 1.0 = physical behaviour. If m_frictionSlip is too high, you'll
		 * need to reduce this to stop the vehicle rolling over. You should also try
		 * lowering the vehicle's centre of mass
		 */
		public float rollInfluence = 0.001f;
		public float connectionHeight = 0.0f;
		/**
		 * Maximum length of suspension
		 */
		public float suspensionRestLength = 0.4f; // max length of suspension
		public Vector3f wheelDirectionCS = new Vector3f(0, -1, 0);
		public Vector3f wheelAxleCS = new Vector3f(-1, 0, 0);
		
		/**
		 * Vehicle "invisible" wheel properties - width of the wheel (ray)
		 */
		public float wheelWidth = 0.4f;
		/**
		 * Vehicle "invisible" wheel properties - radius of the wheel
		 */
		public float wheelRadius = 1f;
		
		/**
		 * Vehicle's maximum engine force - applied to wheels
		 */
		public int maxEngineForce = 150;
		/**
		 * Vehicle's maximum braking force - applied to wheels
		 */
		public int maxBrakingForce = 100;
		
		public float getSuspensionStiffness() {
			return suspensionStiffness;
		}
		public void setSuspensionStiffness(float suspensionStiffness) {
			this.suspensionStiffness = suspensionStiffness;
			this.wheelsDampingCompression = (float) (0.5f * 2.0f * Math.sqrt(suspensionStiffness));
			this.wheelsDampingRelaxation = (float) (0.8f * 2.0f * Math	.sqrt(suspensionStiffness));
		}

		
	}
}
