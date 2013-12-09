package si.fri.rgti.tank;

/**
 * Manages ammo and reloading
 * @author Jani
 */
public class TankGun implements Runnable{
	private int ammo;
	private boolean oneInTheChamber;
	private int reloadTime;
	
	public TankGun(int ammo, int reloadTimeMS) {
		this.oneInTheChamber = true;
		this.ammo = ammo-1;
		this.reloadTime = reloadTimeMS;
	}
	
	public boolean fire(){
		synchronized (this) {
			if(oneInTheChamber){
				oneInTheChamber = false;
				if(ammo >= 1){
					Thread t = new Thread(this);
					t.start();
				}
				System.out.println("TankGun: fired, reloading");
				return true;
			}
			else if(ammo == 0){
				System.out.println("TankGun: no ammo");
				return false;
			}
			else{
				System.out.println("TankGun: empty chamber");
				return false;
			}
		}
	}

	@Override
	public void run() {
		try {
			Thread.sleep(reloadTime);
			synchronized (this) {
				oneInTheChamber = true;
				ammo--;
				System.out.println("TankGun: 1 in the chamber + " + ammo + " ammo");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public AmmoInfo getAmmoInfo(){
		return new AmmoInfo(ammo, oneInTheChamber);
	}
	
	class AmmoInfo{
		public int ammo;
		public boolean oneInTheChamber;
		public AmmoInfo(int ammo, boolean oneInTheChamber) {
			this.ammo = ammo;
			this.oneInTheChamber = oneInTheChamber;
		}
	}
}
