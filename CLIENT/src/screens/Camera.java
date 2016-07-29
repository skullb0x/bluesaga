package screens;

import org.newdawn.slick.Input;

import utils.RandomUtils;

public class Camera {
	
	private float x;
	private float y;
	
	private float shakeX = 0.0f;
	private float shakeY = 0.0f;
	private int shakeItr = 0;
	
	private float myCameraMoveSpeed;
	private float myScreenDimentionsX;
	private float myScreenDimentionsY;
	public Camera(int aScreenDimentionsX, int aScreenDimentionsY) {
		myScreenDimentionsX = aScreenDimentionsX;
		myScreenDimentionsY = aScreenDimentionsY;
		x = 0;
		y = 0;
		myCameraMoveSpeed = 400.0f;
	}
	
	public void setScreenDimentionsY(int newValue){
		myScreenDimentionsY = newValue;
	}
	
	public void Update(float aElapsedTime) {
		/*
		if(myMouseEffectIsActive){
			HandleMouseEffect();
		}
		*/
		if(shakeItr > 0){
			shakeItr++;
			if(shakeItr % 5 == 0){
				shakeX = RandomUtils.getInt(-10,10);
				shakeY = RandomUtils.getInt(-10,10);
			}
			if(shakeItr > 30){
				shakeItr = 0;
				shakeX = 0;
				shakeY = 0;
			}
		}
		//CheckBoundaries();
	}
	
	public void HandleInput(org.newdawn.slick.Input aInput, float aElapsedTime) {
		
		float mousePosX = aInput.getAbsoluteMouseX();
		float mousePosY = aInput.getAbsoluteMouseY();
		if(aInput.isKeyDown(Input.KEY_U)) {
			y += myCameraMoveSpeed * aElapsedTime;
		}
		if(aInput.isKeyDown(Input.KEY_J)) {
			y -= myCameraMoveSpeed * aElapsedTime;
		}
		if(aInput.isKeyDown(Input.KEY_H)) {
			x += myCameraMoveSpeed * aElapsedTime;
		}
		if(aInput.isKeyDown(Input.KEY_K)) {
			x -= myCameraMoveSpeed * aElapsedTime;
		}
		
	}
	
	public void SetCenterPosition(float aX, float aY){
		x = aX + (myScreenDimentionsX/2.0f);
		
		y = aY + (myScreenDimentionsY/2.0f);
	}
	
	public int getX(){
		return (int) x + (int) shakeX;
	}
	
	public int getY(){
		return (int) y + (int) shakeY;
	}
	
	public float getfX(){
		return x + shakeX;
	}
	
	public float getfY(){
		return y + shakeY;
	}
	
	public void shake(){
		if(shakeItr == 0){
			shakeX = 0.0f;
			shakeY = 0.0f;
			shakeItr = 1;
		}
	}
	
	/*
	private void CheckBoundaries(){
		float temp = -(BlueSaga.WORLD_MAP.getMapSize()*Settings.TILE_SIZE);
		
		if(x > 0) {
			x = 0;
		}
		if(x < temp+myScreenDimentionsX) {
			x = temp+myScreenDimentionsX;
		}
		
		if(y < 0 + myScreenDimentionsY - Settings.TILE_SIZE) {
			y = 0 + myScreenDimentionsY - Settings.TILE_SIZE;
		}
		if(y > -temp - Settings.TILE_SIZE) {
			y = -temp - Settings.TILE_SIZE;
		}
	}
	
	private void HandleMouseEffect(){
		x += myMouseOffsetFromCenterX/myMouseEffectImpact;
		y += myMouseOffsetFromCenterY/myMouseEffectImpact;
	}
	*/
}
