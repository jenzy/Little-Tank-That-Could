package si.fri.rgti.tank;

/**
 * A timer that counts down. Not sure how accurate
 * @author Jani
 */
public class CountdownTimer implements Runnable{
	public static boolean forceStop = false;
	
	public interface ICountdownOver{
		public void onCountdownOver();
	}
	
	private boolean isDone;
	
	private long startTimeMS;
	private long endTimeMS;
	private long countdownDurationMS;
	private ICountdownOver endCountdown;

	
	public CountdownTimer(long countdownDurationMS, ICountdownOver endCountdown) {
		this.countdownDurationMS = countdownDurationMS;
		this.endCountdown = endCountdown;
	}

	public void start(){
		startTimeMS = System.nanoTime() / 1000000;
		endTimeMS = startTimeMS + countdownDurationMS;
		new Thread(this).start();
	}
	
	public float getRemainingTimeS(){
		long currentTimeMS = System.nanoTime()/1000000;
		return (endTimeMS - currentTimeMS) / 1000f;
	}
	
	public boolean isDone() {
		return isDone;
	}

	@Override
	public void run() {
		try {
			long endTimeNano = (endTimeMS-500) * 1000000;
			while(System.nanoTime() < endTimeNano){
				if(!Game.isRunning) return;
				Thread.sleep(1000);
			}
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		isDone = true;
		endCountdown.onCountdownOver();
	}
}
