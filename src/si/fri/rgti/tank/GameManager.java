package si.fri.rgti.tank;

import java.util.Random;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import si.fri.rgti.tank.CountdownTimer.ICountdownOver;
import si.fri.rgti.tank.Game.Level;
import si.fri.rgti.tank.Menu.MenuMode;

import com.bulletphysics.dynamics.DynamicsWorld;

/**
 * Manages game's win/lose state and initializes goals
 * @author Jani
 */
public class GameManager implements ICountdownOver {
	private DynamicsWorld dynamicsWorld;
	private Vector3f tankStartLocation = new Vector3f(0, 17, 0);
	private Vector3f clearStartingArea = new Vector3f(180, 0, 180);
	private Vector2f goalLocation;
	private int gameGoals = 0;
	private int range = 250;
	private int time = 5 * 60 * 1000; //ms
	private CountdownTimer timer;
	Random r = new Random();

	public GameManager(DynamicsWorld dynamicsWorld) {
		this.dynamicsWorld = dynamicsWorld;
	}
	
	public void spawnGoals(){
		
		if(Game.getLevel() == Level.DESERT){
			clearStartingArea = new Vector3f(180, 0, 180);
			range = 250;
		}
		else if(Game.getLevel() == Level.GRASSLAND){
			clearStartingArea = new Vector3f(250, 0, 250);
			range = 500;
		}
		
		float y = 60;
		float xGeneralLocation;
		float zGeneralLocation;
		while(true){
			xGeneralLocation = (r.nextInt()%range);
			if( xGeneralLocation < tankStartLocation.x-clearStartingArea.x 
					|| xGeneralLocation > tankStartLocation.x + clearStartingArea.x)
				break;
		}
		while(true){
			zGeneralLocation = (r.nextInt()%range);
			if( zGeneralLocation < tankStartLocation.z-clearStartingArea.z 
					|| zGeneralLocation > tankStartLocation.z + clearStartingArea.z)
				break;
		}
		System.out.println("Target location: (" + xGeneralLocation + ", " + zGeneralLocation + ")\n");
		goalLocation = new Vector2f(xGeneralLocation, zGeneralLocation);
		
		Vector3f pacLocation= new Vector3f(xGeneralLocation, y, zGeneralLocation-20);
		PAC3 pac = PAC3.createPAC3(pacLocation, dynamicsWorld);
		Game.addObject(pac);
		gameGoals++;
		
		Vector3f aaa1Location = new Vector3f(xGeneralLocation+30, y, zGeneralLocation+10);
		AAArtilery aaa1 = AAArtilery.createAAA(aaa1Location, dynamicsWorld);
		Game.addObject(aaa1);
		gameGoals++;
		
		Vector3f aaa2Location = new Vector3f(xGeneralLocation+0, y, zGeneralLocation+10);
		AAArtilery aaa2 = AAArtilery.createAAA(aaa2Location, dynamicsWorld);
		Game.addObject(aaa2);
		gameGoals++;
		
		Vector3f aaa3Location = new Vector3f(xGeneralLocation-30, y, zGeneralLocation+10);
		AAArtilery aaa3 = AAArtilery.createAAA(aaa3Location, dynamicsWorld);
		Game.addObject(aaa3);
		gameGoals++;
		
		this.timer = new CountdownTimer(time, this);
	}
	
	public void startTimer(){
		timer.start();
	}
	
	public int getTimeLeft(){
		return (int)timer.getRemainingTimeS();
	}
	
	public void goalDestroyed(){
		gameGoals--;
		if(gameGoals == 0){
			onGameWin();
		}
	}

	@Override
	public void onCountdownOver() {
		onGameOver();
	}
	
	private void onGameWin(){
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println("\nCongrats, you won!");
				Game.isRunning = false;
				Game.setEndGameScreen(MenuMode.WIN);
			}
		});
		t.start();
	}
	
	private void onGameOver(){
		System.out.println("\nYou lost!");
		Game.isRunning = false;
		Game.setEndGameScreen(MenuMode.LOSE);
	}

	public Vector2f getGoalLocation() {
		return goalLocation;
	}
	
	
}
