package minigames;

import game.BlueSaga;
import graphics.BlueSagaColors;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;

import sound.Sfx;

public class AggressiveGame {

	private int attackXitr = 0;
	private int attackX = 0;

	private int activeDelayItr = 0;
	
	private boolean Active = false;
	private boolean HitSpace = false;
	
	public AggressiveGame(){
		
	}
	
	public void start(int newAttackX){
		attackXitr = 0;
		activeDelayItr = 0;
		setAttackX(newAttackX);
		setActive(true);
		setHitSpace(false);
	}

	public int getAttackX() {
		return attackX;
	}

	public void setAttackX(int attackX) {
		this.attackX = attackX;
	}

	public boolean isActive() {
		return Active;
	}

	public void setActive(boolean active) {
		Active = active;
	}
	
	public void draw(Graphics g, int x, int y){
		if(isActive()){
			g.setColor(new Color(255,255,255));
			g.drawRect(x, y, 50, 5);
			
			if(isHitSpace()){
				if(Math.abs(attackXitr - attackX) < 4){
					g.setColor(BlueSagaColors.GREEN);
				}else if(Math.abs(attackXitr - attackX) < 15){
					g.setColor(BlueSagaColors.YELLOW);
				}else {
					g.setColor(BlueSagaColors.RED);
				}
			}else if(attackXitr == 50){
				g.setColor(BlueSagaColors.RED);
			}
			
			g.fillRect(x+1, y+1, attackXitr-1, 4);
			
			g.setColor(new Color(255,0,0));
			g.drawLine(x+attackX, y, x+attackX, y+5);
			
			boolean done = false;
			
			if(!isHitSpace()){
				if(attackXitr < 50){
					attackXitr +=2;
				}else{
					done = true;
				}
			}else{
				done = true;
			}
			
			if(done){
				activeDelayItr++;
				if(activeDelayItr == 10){
					setActive(false);
				}
			}
		}
	}
	
	public void keyLogic(Input INPUT){
		if(!isHitSpace() && INPUT.isKeyPressed(Input.KEY_SPACE)){
			setHitSpace(true);
			
			if(Math.abs(attackXitr - attackX) < 4){
				Sfx.play("battle/minigame_success");
			}else{
				Sfx.play("battle/minigame_done");
			}
			BlueSaga.client.sendMessage("<att_game>", ""+attackXitr);
		}
	}

	public boolean isHitSpace() {
		return HitSpace;
	}

	public void setHitSpace(boolean hitSpace) {
		HitSpace = hitSpace;
	}
		
}
