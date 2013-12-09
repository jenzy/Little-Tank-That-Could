package si.fri.rgti.tank.particles;

import org.lwjgl.util.Renderable;
import org.lwjgl.util.vector.Vector3f;

import si.fri.rgti.tank.Game;

/**
 * @author Jani
 */
public class Explosion implements Renderable, Runnable{
	public enum ExplosionType { Explosion, GunFired }
	
	private ExplosionType type;
	private ParticleEmitter particleEmitter;
	private int explosionTime;
	private int explosionCleanupTime = 10000;
	
	public Explosion(ExplosionType type, javax.vecmath.Vector3f origin, javax.vecmath.Vector3f initialVelocity, int explosionTimeMS){
		this.explosionTime = explosionTimeMS;
		this.type = type;
		
		Vector3f o = new Vector3f(origin.x, origin.y, origin.z);
		
		switch(type){
		
		case GunFired:
			Vector3f v = new Vector3f(initialVelocity.x, initialVelocity.y, initialVelocity.z);
			v.normalise();
			v.scale(10f);
			particleEmitter = new ParticleEmitterBuilder()
			    .setEnable3D(true)
			    .setInitialVelocity(v)
			    .setGravity(new Vector3f(0, -0.00001f, 0))
			    .setSpawningRate(100)
			    .setParticleLifeTime(15)
			    .setPointSize(1f)
			    .setVelocityModifier(15f)
			    .setLocation(o)
			    .createParticleEmitter();
			break;
		case Explosion:
			particleEmitter = new ParticleEmitterBuilder()
			    .setEnable3D(true)
			    .setInitialVelocity(new Vector3f(0, 0, 0))
			    .setGravity(new Vector3f(0, -0.00001f, 0))
			    .setSpawningRate(1000)
			    .setParticleLifeTime(100)
			    .setPointSize(2f)
			    .setVelocityModifier(200f)
			    .setLocation(o)
			    .createParticleEmitter();
			break;
		}
		
		Thread t = new Thread(this);
		t.start();
	}

	@Override
	public void render() {
		particleEmitter.update();
		particleEmitter.draw();
	}

	@Override
	public void run() {
		try {
			Thread.sleep(explosionTime);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		particleEmitter.setSpawningRate(0);
		if(type != ExplosionType.GunFired){
			try {
				Thread.sleep(explosionCleanupTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		Game.removeObject(this);
	}

}
