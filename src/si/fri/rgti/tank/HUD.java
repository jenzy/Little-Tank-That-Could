package si.fri.rgti.tank;

import java.nio.IntBuffer;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;

import si.fri.rgti.tank.Game.Difficulty;
import si.fri.rgti.tank.TankGun.AmmoInfo;

public class HUD {
	protected final int WIDTH;
	protected final int HEIGHT;
	private Tank tank;
	private Vector2f goalLocation;
	
	private final int fpsHeight = 16;
	private final int ammoHeight = 16;
	private final int timeHeight = 16;
	
	// radar textures
	private IntBuffer texture;
	
	protected BitmapText text;
	
	public HUD(int width, int height) {
		this.WIDTH = width;
		this.HEIGHT = height;
		this.text = new BitmapText();
	}


	public HUD(int width, int height, Tank tank, Vector2f goalLocation) {
		this(width, height);
		this.tank = tank;
		this.goalLocation = goalLocation;
		
		if(Game.getDifficulty() == Difficulty.EASY)
			texture = Texture.loadTextures2D(new String[]{"models/radar_background.png", "models/radar_arrow.png"}, false);
	}

	
	public void render(int fps, int timeLeftSeconds){
		startHUD();
		
		renderFPS(fps);
		renderAmmo(tank.getAmmoInfo());
		renderTime(timeLeftSeconds);
		if(Game.getDifficulty() == Difficulty.EASY)
			renderCompass();
		
		endHUD();
	}
	
	private void renderCompass(){
		Vector3f tankDirection = tank.getForwardVector();
		Vector3f tankPositon = tank.getPosition();
		Vector2f tankDir = new Vector2f(tankDirection.x, tankDirection.z);
		Vector2f goalDir = new Vector2f(goalLocation.x-tankPositon.x, goalLocation.y-tankPositon.z);
		
		float angle = (float) (Math.atan2(tankDir.x, tankDir.y)-Math.atan2(goalDir.x, goalDir.y));
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glColor4f(1, 1, 1, 1);
		
		GL11.glPushMatrix();
			// render radar background
			GL11.glTranslatef(75, 75, 0);
			GL11.glScalef(50, 50, 0);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.get(0));
			GL11.glBegin(GL11.GL_QUADS);
				GL11.glTexCoord2f(1, 0);	GL11.glVertex3f(1, -1, 0);
				GL11.glTexCoord2f(1, 1);	GL11.glVertex3f(1, 1, 0);
				GL11.glTexCoord2f(0, 1);	GL11.glVertex3f(-1, 1, 0);
				GL11.glTexCoord2f(0, 0);	GL11.glVertex3f(-1, -1, 0);
			GL11.glEnd();
			
			// render radar arrow
			GL11.glRotatef((float) -Math.toDegrees(angle), 0, 0, 1);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.get(1));
			GL11.glBegin(GL11.GL_QUADS);
				GL11.glTexCoord2f(1, 1);	GL11.glVertex3f(1, -1, 0);
				GL11.glTexCoord2f(1, 0);	GL11.glVertex3f(1, 1, 0);
				GL11.glTexCoord2f(0, 0);	GL11.glVertex3f(-1, 1, 0);
				GL11.glTexCoord2f(0, 1);	GL11.glVertex3f(-1, -1, 0);
			GL11.glEnd();
		GL11.glPopMatrix();
		
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
	}
	
	private void renderTime(int timeS){
		int min = timeS / 60;
		int sec = timeS % 60;
		if (sec < 0) {
			min = 0;
			sec = 0;
		}
		
		String s = String.format("%2d:%s%d", min, sec<10 ? "0" : "", sec);
		
		GL11.glPushMatrix();
			if(min == 0)
				GL11.glColor4f(1, 0, 0, 1);
			else
				GL11.glColor4f(0, 0.8f, 0, 1);
			GL11.glTranslatef(0, HEIGHT-2*timeHeight, 0);
			GL11.glScalef(2, 2, 1);
			text.renderString(s, timeHeight);
		GL11.glPopMatrix();
	}
	
	private void renderAmmo(AmmoInfo ammo){
		String s = String.format("Ammo: %3d + %d", ammo.ammo, ammo.oneInTheChamber ? 1 : 0);
		float w = text.textWidth(s, ammoHeight);
		
		GL11.glPushMatrix();
		GL11.glColor4f(1, 1, 1, 1);
		GL11.glTranslatef(WIDTH-w-5, 5, 0);
		text.renderString(s, ammoHeight);
		GL11.glPopMatrix();
	}
	
	private void renderFPS(int fps){
		String s = "" + fps;
		float w = text.textWidth(s, fpsHeight);
		
		float scale = 1.5f;
		GL11.glPushMatrix();
		GL11.glColor4f(0, 0.8f, 0, 1);
		GL11.glTranslatef(WIDTH-scale*w, HEIGHT-scale*fpsHeight-5, 0);
		GL11.glScalef(scale, scale, 1);
		text.renderString(s, fpsHeight);
		GL11.glPopMatrix();
	}
	
	protected void startHUD() {
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glPushMatrix();
		GL11.glLoadIdentity();
		GL11.glOrtho(0, WIDTH, 0, HEIGHT, -1, 1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glPushMatrix();
		GL11.glLoadIdentity();
		GL11.glDisable(GL11.GL_LIGHTING);
	}

	protected void endHUD() {
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glPopMatrix();
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glPopMatrix();
		GL11.glEnable(GL11.GL_LIGHTING);
	}

}
