package minigames;

import utils.RandomUtils;

public class AggressiveGame {
	private int aggressiveX = 0;
	private int playerHitX = 0;
	
	private boolean CheckedResult = false;
	
	private float damageBonus = 0.4f; // 1.0f == 100%
	
	public AggressiveGame(){
		
	}
	
	public void start(){
		setAggressiveX(RandomUtils.getInt(15,50));
		setCheckedResult(false);
	}

	public void stop(int newPlayerHitX){
		playerHitX = newPlayerHitX;
	}
	
	public int getAggressiveX() {
		return aggressiveX;
	}

	public void setAggressiveX(int newAggressiveX) {
		aggressiveX = newAggressiveX;
	}

	public float getResult(){
		if(!isCheckedResult()){
			setCheckedResult(true);
			float calc_bonus = 0.0f;
			if(Math.abs(aggressiveX - playerHitX) < 15){
				calc_bonus = damageBonus - damageBonus * Math.abs(aggressiveX - playerHitX)/50.0f;
			}
			if(calc_bonus < 0.0f){
				calc_bonus = 0.0f;
			}
			
			return calc_bonus;
		}
		return 0;
	}

	public boolean isCheckedResult() {
		return CheckedResult;
	}

	public void setCheckedResult(boolean checkedResult) {
		CheckedResult = checkedResult;
	}
}
