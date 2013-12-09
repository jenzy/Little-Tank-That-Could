package si.fri.rgti.tank;

/**
 * 
 * @author Jani
 *
 */
public interface IControlableVehicle {
	public void goForward(boolean isFwdKeyDown);
	public void goBackward(boolean isBackKeyDown);
	public void goRight(boolean isRightKeyDown, boolean isFwdKeyDown);
	public void goLeft(boolean isLeftKeyDown, boolean isFwdKeyDown);
	public void reset();
}
