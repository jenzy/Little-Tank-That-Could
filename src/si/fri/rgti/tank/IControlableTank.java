package si.fri.rgti.tank;

public interface IControlableTank extends IControlableVehicle {
	public void turnTurretLeft();
	public void turnTurretRight();
	public void gunUp();
	public void gunDown();
	
	public void fire(boolean isKeyDown);
}
