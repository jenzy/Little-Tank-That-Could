package si.fri.rgti.tank;

import java.nio.IntBuffer;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import si.fri.rgti.tank.Game.Difficulty;
import si.fri.rgti.tank.Game.Level;

public class Menu extends HUD {
	public enum MenuMode {OPTIONS, WIN, LOSE, NONE, OUT_OF_MAP};
	
	private boolean inMenu;

	public Menu(int width, int height) {
		super(width, height);
	}
	
	public void showMenu(MenuMode mode){
		if(mode == MenuMode.NONE) return;
		
		inMenu = true;
		startHUD();
		
		menuLoop(mode);
		
		endHUD();
	}
	
	private void menuLoop(MenuMode mode){
		while(inMenu && !Display.isCloseRequested()){
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
			
			switch(mode){
			case OPTIONS:
				renderOptions();
				break;
			case LOSE:
				renderGameOverScreen();
				break;
			case WIN:
				renderWinScreen();
				break;
			case NONE:
				break;
			case OUT_OF_MAP:
				renderOutOfMapScreen();
				break;
			}
			
			processInput(mode);
			
			Display.update();
			Display.sync(60);
		}
	}
	
	private void renderOptions(){
		String title = Game.TITLE;
		float w = text.textWidth(title, 16);
		
		float scale = 4;
		GL11.glPushMatrix();
			GL11.glColor4f(0.9f, 1, 0.9f, 1);
			GL11.glTranslatef((WIDTH/2)-(scale*w/2), (HEIGHT-200)-(scale*16/2), 0);
			GL11.glScalef(scale, scale, 1);
			text.renderString(title, 16);
		GL11.glPopMatrix();
		
		IntBuffer texture = Texture.loadTextures2D(new String[]{"models/T-90/front.png"}, false);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.get(0));
		
		GL11.glPushMatrix();
			GL11.glColor4f(1, 1, 1, 1);
			GL11.glTranslatef(WIDTH/2, 300, 0);
			GL11.glScalef(100, 100, 0);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.get(0));
			GL11.glBegin(GL11.GL_QUADS);
				GL11.glTexCoord2f(1, 1);	GL11.glVertex3f(1, -1, 0);
				GL11.glTexCoord2f(1, 0);	GL11.glVertex3f(1, 1, 0);
				GL11.glTexCoord2f(0, 0);	GL11.glVertex3f(-1, 1, 0);
				GL11.glTexCoord2f(0, 1);	GL11.glVertex3f(-1, -1, 0);
			GL11.glEnd();
		GL11.glPopMatrix();
		
		
		String[] options = new String[]{"1 - Grassland - Easy", "2 - Grassland - Normal", "3 - Desert - Easy", "4 - Desert - Normal", "ESC - Quit"};
		int currHeight = 200;
		int fontHeight = 16;
		
		GL11.glColor4f(1, 1, 1, 1);
		for (String s : options) {
			w = text.textWidth(s, fontHeight);
			
			GL11.glPushMatrix();
			GL11.glTranslatef((WIDTH/2)-(w/2), currHeight, 0);
			text.renderString(s, fontHeight);
			GL11.glPopMatrix();
			
			currHeight -= fontHeight;
		}
		
	}
	
	private void processInput(MenuMode mode){
		if (Display.isCloseRequested() || Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
			BaseWindow.isRunning = false;
			inMenu = false;
		}
		else if(mode == MenuMode.OPTIONS){
			if (Keyboard.isKeyDown(Keyboard.KEY_1)) {
				Game.setDifficulty(Difficulty.EASY);
				Game.setLevel(Level.GRASSLAND);
				inMenu = false;
				showLoadingScreen();
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_2)) {
				Game.setDifficulty(Difficulty.NORMAL);
				Game.setLevel(Level.GRASSLAND);
				inMenu = false;
				showLoadingScreen();
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_3)) {
				Game.setDifficulty(Difficulty.EASY);
				Game.setLevel(Level.DESERT);
				inMenu = false;
				showLoadingScreen();
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_4)) {
				Game.setDifficulty(Difficulty.NORMAL);
				Game.setLevel(Level.DESERT);
				inMenu = false;
				showLoadingScreen();
			}
		}
	}
	
	private void showLoadingScreen(){
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
		
		IntBuffer texture = Texture.loadTextures2D(new String[]{"models/T-90/T-90.png"}, false);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.get(0));
		
		GL11.glPushMatrix();
			GL11.glColor4f(1, 1, 1, 1);
			GL11.glTranslatef(WIDTH/2+150, HEIGHT-500, 0);
			GL11.glScalef(500, 500, 0);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.get(0));
			GL11.glBegin(GL11.GL_QUADS);
				GL11.glTexCoord2f(1, 1);	GL11.glVertex3f(1, -1, 0);
				GL11.glTexCoord2f(1, 0);	GL11.glVertex3f(1, 1, 0);
				GL11.glTexCoord2f(0, 0);	GL11.glVertex3f(-1, 1, 0);
				GL11.glTexCoord2f(0, 1);	GL11.glVertex3f(-1, -1, 0);
			GL11.glEnd();
		GL11.glPopMatrix();
		
		
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		
		
		String s = String.format("Loading...");
		float w = text.textWidth(s, 16);
		
		GL11.glPushMatrix();
			GL11.glColor4f(1, 1, 1, 1);
			GL11.glTranslatef((WIDTH/2)-(2*w/2), 100, 0);
			GL11.glScalef(2, 2, 1);
			text.renderString(s, 16);
		GL11.glPopMatrix();
		
		Display.update();
	}
	
	private void renderGameOverScreen(){
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		
		String s = String.format("GAME OVER!");
		float w = text.textWidth(s, 16);
		
		float scale = 4;
		GL11.glPushMatrix();
			GL11.glColor4f(1, 0, 0, 1);
			GL11.glTranslatef((WIDTH/2)-(scale*w/2), (HEIGHT/2)-(scale*16/2), 0);
			GL11.glScalef(scale, scale, 1);
			text.renderString(s, 16);
		GL11.glPopMatrix();
		
		s = String.format("You were too late, the enemy has defeated us!");
		w = text.textWidth(s, 16);
		
		scale = 2;
		GL11.glPushMatrix();
			GL11.glColor4f(1, 0, 0, 1);
			GL11.glTranslatef((WIDTH/2)-(scale*w/2), (HEIGHT/2)-4*(scale*16/2), 0);
			GL11.glScalef(scale, scale, 1);
			text.renderString(s, 16);
		GL11.glPopMatrix();
		
		GL11.glEnable(GL11.GL_DEPTH_TEST);
	}
	
	private void renderWinScreen(){
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		
		String s = String.format("CONGRATULATIONS!");
		float w = text.textWidth(s, 16);
		
		float scale = 4;
		GL11.glPushMatrix();
			GL11.glColor4f(1, 0, 0, 1);
			GL11.glTranslatef((WIDTH/2)-(scale*w/2), (HEIGHT/2)-(scale*16/2), 0);
			GL11.glScalef(scale, scale, 1);
			text.renderString(s, 16);
		GL11.glPopMatrix();
		
		s = String.format("You have succesfully foiled the enemy's plans!");
		w = text.textWidth(s, 16);
		
		scale = 2;
		GL11.glPushMatrix();
			GL11.glColor4f(1, 0, 0, 1);
			GL11.glTranslatef((WIDTH/2)-(scale*w/2), (HEIGHT/2)-4*(scale*16/2), 0);
			GL11.glScalef(scale, scale, 1);
			text.renderString(s, 16);
		GL11.glPopMatrix();
		
		GL11.glEnable(GL11.GL_DEPTH_TEST);
	}
	
	private void renderOutOfMapScreen(){
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		
		String s = String.format("GAME OVER!");
		float w = text.textWidth(s, 16);
		
		float scale = 4;
		GL11.glPushMatrix();
			GL11.glColor4f(1, 0, 0, 1);
			GL11.glTranslatef((WIDTH/2)-(scale*w/2), (HEIGHT/2)-(scale*16/2), 0);
			GL11.glScalef(scale, scale, 1);
			text.renderString(s, 16);
		GL11.glPopMatrix();
		
		s = String.format("You fell out of the map - a true explorer!");
		w = text.textWidth(s, 16);
		
		scale = 2;
		GL11.glPushMatrix();
			GL11.glColor4f(1, 0, 0, 1);
			GL11.glTranslatef((WIDTH/2)-(scale*w/2), (HEIGHT/2)-4*(scale*16/2), 0);
			GL11.glScalef(scale, scale, 1);
			text.renderString(s, 16);
		GL11.glPopMatrix();
		
		s = String.format("But... we were defeated.");
		w = text.textWidth(s, 16);
		
		scale = 2;
		GL11.glPushMatrix();
			GL11.glColor4f(1, 0, 0, 1);
			GL11.glTranslatef((WIDTH/2)-(scale*w/2), (HEIGHT/2)-6*(scale*16/2), 0);
			GL11.glScalef(scale, scale, 1);
			text.renderString(s, 16);
		GL11.glPopMatrix();
		
		GL11.glEnable(GL11.GL_DEPTH_TEST);
	}

}
