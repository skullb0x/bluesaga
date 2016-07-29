package minigames;

import graphics.BlueSagaColors;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

public class BoostAttackGame {
	private int boostItr = 0;
	private int boostMax = 50;
	private boolean Active = false;
	
	public BoostAttackGame(){
		
	}
	
	public void start(){
		boostItr = 0;
		setActive(true);
	}
	
	
	public void stop(){
		boostItr = 0;
		setActive(false);
	}
	
	public void update(){
		if(isActive()){
			if(boostItr < boostMax){
				boostItr += 6;
				if(boostItr > boostMax){
					boostItr = boostMax;
				}
			}
		}
	}
	
	public void draw(Graphics g, int x, int y){
		if(isActive()){
			g.setColor(new Color(255,255,255));
			g.drawRect(x, y, 51, 5);
			
			g.setColor(BlueSagaColors.RED);
			
			g.fillRect(x+1, y+1, boostItr, 4);
		}
	}
	
	public int getBoost(){
		return boostItr;
	}
	
	public boolean isMax(){
		if(boostItr >= boostMax-1){
			return true;
		}
		return false;
	}
	
	public void resetBoost(){
		boostItr = 0;
	}
	
	public void setActive(boolean newStatus){
		Active = newStatus;
	}
	
	public boolean isActive(){
		return Active;
	}
}
