package minigames;

public class BoostAttackGame {
	private int boostItr = 0;
	private int boostMax = 50;
	
	public BoostAttackGame(){
		
	}
	
	public void start(){
		boostItr = 0;
	}
	
	public void update(){
		if(boostItr < boostMax){
			boostItr += 6;
			if(boostItr > boostMax){
				boostItr = boostMax;
			}
		}
	}
	
	public int getBoost(){
		return boostItr;
	}
	
	public boolean isMax(){
		if(boostItr == boostMax){
			return true;
		}
		return false;
	}
	
	public void resetBoost(){
		boostItr = 0;
	}
}
