package gamelogic.player;

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.Color;
import java.awt.Graphics;
import java.net.MalformedURLException;
import java.net.URL;
import java.awt.Font;
import gameengine.PhysicsObject;
import gameengine.graphics.MyGraphics;
import gameengine.hitbox.RectHitbox;
import gamelogic.Main;
import gamelogic.level.Level;
import gamelogic.tiles.Tile;
import gameengine.maths.Vector2D;
   import java.awt.event.*;
   import javax.sound.sampled.*;
   import java.io.File;
import java.io.IOException;

public class Player extends PhysicsObject{
	public float walkSpeed = 400;
	public float jumpPower = 1350;

	private boolean isJumping = false;
	private long time;
	private Clip leftMoveClip; // Add this field to your Player class
	public Player(float x, float y, Level level) {
	
		super(x, y, level.getLevelData().getTileSize(), level.getLevelData().getTileSize(), level);
		int offset =(int)(level.getLevelData().getTileSize()*0.1); //hitbox is offset by 10% of the player size.
		this.hitbox = new RectHitbox(this, offset,offset, width -offset, height - offset);
		time = System.currentTimeMillis();
	}

	@Override
	public void update(float tslf) {
		super.update(tslf);
		movementVector.x = 0;
		if(PlayerInput.isLeftKeyDown()) {
			movementVector.x = -walkSpeed;
			// try {
			// 	if (leftMoveClip == null || !leftMoveClip.isRunning()) {
			// 		File soundFile = new File("workspace/audio/zapsplat_multimedia_game_sound_error_incorrect_negative_hard_tone_103171.wav");
			// 		AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
			// 		leftMoveClip = AudioSystem.getClip();
			// 		leftMoveClip.open(audioIn);
			// 		leftMoveClip.start();
			// 		leftMoveClip.loop(Clip.LOOP_CONTINUOUSLY);
			// 	}
			// } catch (Exception e) {
			// 	e.printStackTrace();
			// }
		} else {
			// Stop the sound if the left key is not pressed
			// if (leftMoveClip != null && leftMoveClip.isRunning()) {
			// 	leftMoveClip.stop();
			// 	leftMoveClip.close();
			// 	leftMoveClip = null;
			// }
		}
		if(PlayerInput.isRightKeyDown()) {
			movementVector.x = +walkSpeed;
		}
		if(PlayerInput.isJumpKeyDown() && !isJumping) {
			movementVector.y = -jumpPower;
			isJumping = true;
		}
		
		
		isJumping = true;
		if(collisionMatrix[BOT] != null) isJumping = false;
	}

	@Override
	public void draw(Graphics g) {
		g.setColor(Color.YELLOW);
		MyGraphics.fillRectWithOutline(g, (int)getX(), (int)getY(), width, height);

		//g.setFont(new Font ("Comic Sans MS", Font.PLAIN, 50));
		//g.drawString((System.currentTimeMillis() - time)/1000 +"", (int)getX(), (int)getY());
		if (System.currentTimeMillis() - time >1000000000 ) {
			time = System.currentTimeMillis();
		}
		
		if(Main.DEBUGGING) {
			for (int i = 0; i < closestMatrix.length; i++) {
				Tile t = closestMatrix[i];
				if(t != null) {
					g.setColor(Color.RED);
					g.drawRect((int)t.getX(), (int)t.getY(), t.getSize(), t.getSize());
				}
			}
		}
		
		hitbox.draw(g);
	}

	public Vector2D getPosition(){
		return position;
	}
}
