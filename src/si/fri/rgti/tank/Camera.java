package si.fri.rgti.tank;

import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

/**
 * 
 * @author Jani
 */
public class Camera {
	public enum CameraMode { FOLLOW_TOPDOWN, STATIC_TOPDOWN, FOLLOW_3RD_PERSON };
	private CameraMode mode;	// current mode of the camera
	private Vector3f lastEye;	// for skybox
	
	/* for easy camera switching */
	private CameraMode[] switchBetweenTheseModes;
	private int currentMode = 0;
	
	/* perspective settings */
	private float fov = 45;		// field of view
	private float aspectRatio = 4f/3f;
	private float nearPlanePerspective = 1f;	// near plane
	private float farPlanePerspective = 30f;	// far plane
	
	/* orthogonal settings */
	//unused!
	private float left = -10;
	private float right = 10;
	private float bottom = -10;
	private float top = 10;
	private float nearPlaneOrthogonal = 1f;	// near plane
	private float farPlaneOrthogonal = 30f;	// far plane
	
	/* top-down static camera settings*/
	private Vector3f eyeStatic;		// coordinates of the camera
	private Vector3f centerStatic;	// where the camera is looking
	private Vector3f upStatic;		// up normal
	
	/* top-down folow camera settings */
	private ITarget targetTD;
	private float cameraHeightTD;
	
	/* 3rd person folow camera settings */
	private ITarget target3RD;
	private float cameraHeight3RD;
	private float cameraBehind3RD;
	private float cameraFocusForward3RD;
	
	public Camera(){	}
	
	public void setPerspectiveSettings(float fov, float aspectRatio, float near, float far) {
		this.fov = fov;		this.aspectRatio = aspectRatio;		this.nearPlanePerspective = near;		this.farPlanePerspective = far;
	}
	
	public void setOrthogonalSettings(float left, float right, float bottom, float top, float near, float far) {
		this.left = left;					this.right = right;		
		this.bottom = bottom;				this.top = top;		
		this.nearPlaneOrthogonal = near;	this.farPlaneOrthogonal = far;
	}
	
	public void setTopDownStaticCameraSettings(Vector3f eye, Vector3f center, Vector3f up){
		this.mode = CameraMode.STATIC_TOPDOWN;
		this.eyeStatic = eye;		this.centerStatic = center;		this.upStatic = up;
	}
	
	public void setTopDownFollowCameraSettings(ITarget target, float height){
		this.targetTD = target;		this.cameraHeightTD = height;
	}
	
	public void set3rdPersonFollowCameraSettings(ITarget target, float height, float behind, float focusForward){
		this.target3RD = target;		this.cameraHeight3RD = height;		this.cameraBehind3RD = behind; this.cameraFocusForward3RD = focusForward;
	}
	
	public void setSwitching(CameraMode[] switchBetweenTheseModes){
		this.switchBetweenTheseModes = switchBetweenTheseModes;
	}
	
	public void switchCamera(){
		if(switchBetweenTheseModes == null) return;
		CameraMode switchTo = switchBetweenTheseModes[++currentMode % switchBetweenTheseModes.length];
		switchToMode(switchTo);
	}

	public void switchToMode(CameraMode mode){
		this.mode = mode;
		switch(this.mode){
		case FOLLOW_TOPDOWN:
			setPerspective();
			break;
		case STATIC_TOPDOWN:
			setPerspective();
			break;
		case FOLLOW_3RD_PERSON:
			setPerspective();
			break;
		default:
			break;
		}
		
	}

	private void setPerspective(){
	    GL11.glMatrixMode(GL11.GL_PROJECTION);
	    GL11.glLoadIdentity();
	    GLU.gluPerspective(fov, aspectRatio, nearPlanePerspective, farPlanePerspective);
	}
	
	@SuppressWarnings("unused")
	private void setOrthogonal(){
		GL11.glMatrixMode(GL11.GL_PROJECTION);
	    GL11.glLoadIdentity();
		GL11.glOrtho(left,right,bottom,top,nearPlaneOrthogonal,farPlaneOrthogonal);
	}
	
	public void update(){
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
	    GL11.glLoadIdentity();
	    
	    Vector3f p, fwd, eye;
		switch(mode){
		case STATIC_TOPDOWN:
			GLU.gluLookAt(eyeStatic.x, eyeStatic.y, eyeStatic.z, centerStatic.x, centerStatic.y, centerStatic.z, upStatic.x, upStatic.y, upStatic.z);
			break;
		case FOLLOW_TOPDOWN:
			p = targetTD.getPosition();
			eye = new Vector3f(p);
			eye.y += cameraHeightTD;
			lastEye = eye;
			fwd = targetTD.getForwardVector();
			fwd.y=0;
			GLU.gluLookAt(eye.x, eye.y, eye.z, p.x, p.y, p.z, fwd.x, fwd.y, fwd.z);
			break;
		case FOLLOW_3RD_PERSON:
			p = target3RD.getPosition();			// vehicle position
			fwd = target3RD.getForwardVector();		// vehicle forward vector
			fwd.y=0;								// only interested in x and z
			fwd.normalize();
			eye = new Vector3f(fwd);
			eye.scaleAdd(-cameraBehind3RD, p);		// eye = position - cameraBehind3RD * fwd -> eye is now behind the target position
			eye.y += cameraHeight3RD;
			lastEye = eye;
			Vector3f focus = new Vector3f(fwd);
			focus.scaleAdd(cameraFocusForward3RD, p);
			GLU.gluLookAt(eye.x, eye.y, eye.z, focus.x, focus.y, focus.z, 0f, 1f, 0f);
			break;
		default:
			break;
		}
		GL11.glLight(GL11.GL_LIGHT0, GL11.GL_POSITION, BaseWindow.allocFloats(new float[] { 1f, 1f, 0f, 0f}));
	}
	
	public Vector3f getEyeLocation(){
		return lastEye;
	}
}
