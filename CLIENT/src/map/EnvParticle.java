package map;

import org.newdawn.slick.Color;

import utils.RandomUtils;
import game.ClientSettings;
import graphics.ImageResource;
import graphics.Sprite;
import screens.ScreenHandler;

public class EnvParticle {
	
	private String Type;
	private float dX;
	private float dY;
	private float x;
	private float y;
	private int size;
	private int alphaGoal;
	private float alpha = 0.0f;
	
	private float rZ;
	private float drZ;
	private boolean ready = false;
	
	private Sprite particleGFX;

	private int cameraStartX;
	private int cameraStartY;

	
	private int red = 255;
	private int green = 255;
	private int blue = 255;

	
	private boolean dissappear = false;
	
	public EnvParticle(String type){
		
		Type = type;
		
		alpha = 0;
		
		particleGFX = ImageResource.getSprite("effects/"+Type);
		
		if(Type.equals("snow")){
			x = RandomUtils.getInt(0,ClientSettings.SCREEN_WIDTH);
			y = RandomUtils.getInt(-10,ClientSettings.SCREEN_HEIGHT);
			
			dX = 0;
			dY = RandomUtils.getFloat(1.5f,5.0f);
			size = RandomUtils.getInt(2,20);
			alpha = RandomUtils.getFloat(55.0f,220.0f);
			cameraStartX = ScreenHandler.myCamera.getX();
			cameraStartY = ScreenHandler.myCamera.getY();
			
			ready = true;
		}else{
			resetParticle();
		}
		
	}
	
	
	public void draw(){
		if(ready){
			x += dX;
			y += dY;
			
			
			int realX = (int) ((ScreenHandler.myCamera.getX() - cameraStartX) + x);
			int realY = (int) ((ScreenHandler.myCamera.getY() - cameraStartY) + y);
			
			
			//int realX = (int) x;
			//int realY = (int) y;
			
			
			if(Type.equals("spore")){
				rZ += drZ;
				
				particleGFX.getImage().setRotation(rZ);
				
				if(realX < -50 || realX > ClientSettings.SCREEN_WIDTH + 50 || realY < -50 || realY > ClientSettings.SCREEN_HEIGHT+50){
					resetParticle();
				}
			}else if(Type.equals("firefly")){
				// Check if firefly is within range of player
				float dfX = ClientSettings.SCREEN_WIDTH/2 - realX;
				float dfY = ClientSettings.SCREEN_HEIGHT/2 - realY;
				
				float changeD = 0.1f;
				
				float distance = (float) Math.sqrt(Math.pow(dfX, 2) + Math.pow(dfY, 2));
				if(distance < 100.0f){
					int evade = RandomUtils.getInt(0, 10);
					if(!dissappear){
						alphaGoal = 255;
					}
					
					green += 50;
					red += 50;
					blue += 50;
					
					if(evade == 0){
						evade = -1;
					}else{
						evade = 1;
					}
					
					if(realX < ClientSettings.SCREEN_WIDTH/2){
						dX+=changeD*evade;
					}else if(realX > ClientSettings.SCREEN_WIDTH/2){
						dX-=changeD*evade;
					}
					
					if(realY < ClientSettings.SCREEN_HEIGHT/2){
						dY+=changeD*evade;
					}else if(realY > ClientSettings.SCREEN_HEIGHT/2){
						dY-=changeD*evade;
					}
					
				}else{
					red = RandomUtils.getInt(50,255);
					green = 255;
					blue =  255;
					
					
					if(alphaGoal == 255 && !dissappear){
						alphaGoal = RandomUtils.getInt(55,200);
					}
					if(dX > 0.5f){
						dX -= changeD;
					}else if(dX < -0.5f){
						dX += changeD;
					}
					if(dY > 0.5f){
						dY -= changeD;
					}else if(dY < -0.5f){
						dY += changeD;
					}
					
				}
				
				if(realX < -50 || realX > ClientSettings.SCREEN_WIDTH + 50 || realY < -50 || realY > ClientSettings.SCREEN_HEIGHT+50){
					resetParticle();
				}
			}else if(Type.equals("snow")){
				
				// FIX THIS: SNOW APPEARS ON OTHER SIDE IF WALKING
				if(realX < -100){
					cameraStartX = ScreenHandler.myCamera.getX(); 
					x = ClientSettings.SCREEN_WIDTH+50;
				}else if(realX > ClientSettings.SCREEN_WIDTH + 100){
					cameraStartX = ScreenHandler.myCamera.getX();
					x = -50;
				}
				if(alpha <= 0.0f || realY > ClientSettings.SCREEN_HEIGHT+50){
					resetParticle();
				}
			}
			
			if(alpha < alphaGoal){
				alpha +=0.5f;
			}else if(alpha > alphaGoal){
				alpha -= 0.5f;
			}
			
	
			
			float sizeF = 30.0f * (size / 20.0f);
			
			particleGFX.draw(Math.round(realX), Math.round(realY),Math.round(sizeF),Math.round(sizeF), new Color(red,green,blue, (int) alpha));
			
			
			
		}
	}
	
	public void resetParticle(){
		cameraStartX = ScreenHandler.myCamera.getX();
		cameraStartY = ScreenHandler.myCamera.getY();
		
		if(Type.equals("spore")){
			int newX = RandomUtils.getInt(0,ClientSettings.SCREEN_WIDTH);
			int newY = RandomUtils.getInt(0,ClientSettings.SCREEN_HEIGHT);
			
			rZ = RandomUtils.getInt(0,360);
			particleGFX.getImage().setRotation(rZ);
			
			drZ = RandomUtils.getFloat(0.0f,2.0f);
			int dir = RandomUtils.getInt(0,1);
			if(dir < 1){
				drZ *= -1.0f;
			}
			
			x = newX;
			y = newY;
			
			dX = RandomUtils.getFloat(0.1f,2.0f);
			dir = RandomUtils.getInt(0,1);
			if(dir < 1){
				dX *= -1.0f;
			}
			
			dY = RandomUtils.getFloat(0.1f,2.0f);
			dir = RandomUtils.getInt(0,1);
			if(dir < 1){
				dY *= -1.0f;
			}
			
			size = RandomUtils.getInt(1,20);
			alphaGoal = RandomUtils.getInt(55,255);
			alpha = 0;
		}else if(Type.equals("firefly")){
			int newX = RandomUtils.getInt(0,ClientSettings.SCREEN_WIDTH);
			int newY = RandomUtils.getInt(0,ClientSettings.SCREEN_HEIGHT);
			
			drZ = RandomUtils.getFloat(0.0f,2.0f);
			int dir = RandomUtils.getInt(0,1);
			if(dir < 1){
				drZ *= -1.0f;
			}
			
			x = newX;
			y = newY;
			
			
			dX = RandomUtils.getFloat(0.1f,0.5f);
			dir = RandomUtils.getInt(0,1);
			if(dir < 1){
				dX *= -1.0f;
			}
			
			dY = RandomUtils.getFloat(0.1f,0.5f);
			dir = RandomUtils.getInt(0,1);
			if(dir < 1){
				dY *= -1.0f;
			}
			
			
			red = RandomUtils.getInt(50,255);
			green = 255;
			blue =  255;
			
			size = RandomUtils.getInt(1,10);
			alphaGoal = RandomUtils.getInt(55,200);
			alpha = 0;
		}else if(Type.equals("snow")){
			int newX = RandomUtils.getInt(0,ClientSettings.SCREEN_WIDTH);
			int newY = RandomUtils.getInt(-300,-10);
			
			
			x = newX;
			y = newY;
			
			
			dX = 0;
			dY = RandomUtils.getFloat(1.5f,5.0f);
			size = RandomUtils.getInt(2,20);
			alpha = RandomUtils.getFloat(55.0f,220.0f);
		}
		if(dissappear){
			alphaGoal = 0;
		}
		ready = true;
	}
	
	public void dissappear(){
		dissappear = true;
		alphaGoal = 0;
	}
	
	public float getAlpha() {
		return alpha;
	}


	public void setAlpha(float alpha) {
		this.alpha = alpha;
	}
}
