package si.fri.rgti.tank;

import javax.vecmath.Vector3f;

public interface IDestructable {
	public void destroy(Vector3f impactLocation);
	public boolean isGameGoal();
}
