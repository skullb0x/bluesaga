package projectile;

import java.sql.ResultSet;
import java.sql.SQLException;

import game.BlueSaga;
import game.ClientSettings;
import graphics.ImageResource;

import org.newdawn.slick.Animation;
import org.newdawn.slick.Color;
import org.newdawn.slick.Image;

import particlesystem.Emitter.Emitter;
import screens.ScreenHandler;

public class Projectile {
	private Animation animation;
	private Image graphics;

	private Animation effect;
	
	private String sfx;
	
	private boolean Animated;
	
	private float OriginX;
	private float OriginY;
	private float OriginZ;
	
	private float moveX;
	private float moveY;
	
	private float goalX;
	private float goalY;
	private float goalZ;
	
	private float dX;
	private float dY;
	
	private int Delay; // Delay before effect (ex: bomb)
	
	private Color HitColor;
	
	private boolean Active;
	
	private int effectId;
	
	private Emitter myEmitter;
	
	public Projectile(int ProjectileId, int originX, int originY, int originZ, int newGoalX, int newGoalY, int delay, float speed, int effectId){
		
		ResultSet projectileInfo = BlueSaga.gameDB.askDB("select * from projectile where Id = "+ProjectileId);
		
		try {
			if(projectileInfo.next()){
				
				this.effectId = effectId;
				
				Active = true;

				OriginX = originX;
				OriginY = originY;
				OriginZ = originZ;
				
				moveX = 0.0f;
				moveY = 0.0f;
				
				goalX = newGoalX;
				goalY = newGoalY;
				
				double diffX = goalX - OriginX;
				double diffY = goalY - OriginY;
				
				
				double angle = Math.atan(diffY / diffX);
				
				setDelay(delay);
				
				dX = (float) Math.cos(angle) * 1.2f;
				dY = (float) Math.sin(angle) * 1.2f; // to make them move faster * 1.2f
				angle *= (360 / (Math.PI*2));
				
				String color[] = projectileInfo.getString("HitColor").split(",");
				
				setHitColor(new Color(Integer.parseInt(color[0]),Integer.parseInt(color[1]),Integer.parseInt(color[2])));
				
				myEmitter = null;
				ResultSet emitterInfo = BlueSaga.gameDB.askDB("select Name from emitter where Id = "+projectileInfo.getInt("EmitterId"));
				if(emitterInfo.next()){
					myEmitter = ScreenHandler.myEmitterManager.SpawnEmitter(originX*ClientSettings.TILE_SIZE, originY*ClientSettings.TILE_SIZE, emitterInfo.getString("Name"));
				}
				emitterInfo.close();
				
				if(diffX < 0){
					angle -= 90;
				}else{
					angle += 90;
				}
				
				// Twister projectile should not be rotated
				if(ProjectileId == 15){
					angle = 0;
				}
				
				if(diffX < 0){
					dX *= -1;
					dY *= -1;
				}
				
				dX *= 8;
				dY *= 8;
				
				Animated = ImageResource.getSprite("projectiles/"+projectileInfo.getString("GfxName")).isAnimated(); 
				
				if(Animated){
					animation = new Animation();
					animation = ImageResource.getSprite("projectiles/"+projectileInfo.getString("GfxName")).getAnimation().copy();
					
					animation.getImage(0).rotate((float) angle);
					animation.getImage(1).rotate((float) angle);
				}else{
					graphics = ImageResource.getSprite("projectiles/"+projectileInfo.getString("GfxName")).getImage().copy();
					graphics.rotate((float) angle);
				}
				
				if(effectId > 0){
					effect = new Animation();
					effect = ImageResource.getSprite("projectiles/projectile_effect"+effectId).getAnimation().copy();
				
					effect.getImage(0).rotate((float) angle);
					effect.getImage(1).rotate((float) angle);
				}
				
				
				setSfx(projectileInfo.getString("Sfx"));
			}
			projectileInfo.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}	
		
		
	}
	
	public void draw(int x, int y){
		if(effectId > 0){
			effect.draw(x, y);
		}
		
		if(Animated){
			if(getDelay() > 0 && getDelay() < 1000){
				if(getDelay() % 200 < 100){
					animation.drawFlash(x, y, 50, 50);
				}else{
					animation.draw(x, y);
				}
			}else{
				animation.draw(x, y);
			}
		}else{
			if(getDelay() > 0 && getDelay() < 1000){
				if(getDelay() % 200 < 100){
					graphics.draw(x, y,new Color(255,255,100,200));
				}else{
					graphics.draw(x, y);
				}
			}else{
				graphics.draw(x, y);
			}
		}
		 
		
		if(Active){
			moveX += dX;
			moveY -= dY;
			
			if(myEmitter != null){
				myEmitter.SetPosition(Math.round(OriginX*ClientSettings.TILE_SIZE + moveX + 25), Math.round(-OriginY*ClientSettings.TILE_SIZE + moveY + 25));
			}
			
			// REACH GOAL
			if(OriginX + Math.round(moveX/ClientSettings.TILE_SIZE) == goalX && OriginY - Math.round(moveY/ClientSettings.TILE_SIZE) == goalY){
				Active = false;
			}
		}
		
	}
	
	public int getCurrentX(){
		float TileSize = 50.0f;
		return (int) OriginX + Math.round(moveX/TileSize);
	}
	
	public int getCurrentY(){
		float TileSize = 50.0f;
		return (int) OriginY + Math.round(moveY/TileSize);
	}
	
	public boolean getActive(){
		if(Active){
			return Active;
		}else{
			if(getDelay() > 0){
				Delay -= 18;
				return true;
			}else{
				Active = false;
			}
			return Active;
		}
	}
	
	public int getOriginX(){
		return Math.round(OriginX);
	}
	
	public int getOriginY(){
		return Math.round(OriginY);
	}
	
	public int getMoveX(){
		return Math.round(moveX);
	}
	
	public int getMoveY(){
		return Math.round(moveY);
	}
	
	public int getGoalX(){
		return (int) goalX;
	}
	
	public int getGoalY(){
		return (int) goalY;
	}

	public float getOriginZ() {
		return OriginZ;
	}

	public void setOriginZ(float originZ) {
		OriginZ = originZ;
	}

	public float getGoalZ() {
		return goalZ;
	}

	public void setGoalZ(float goalZ) {
		this.goalZ = goalZ;
	}

	public Color getHitColor() {
		return HitColor;
	}

	public void setHitColor(Color hitColor) {
		HitColor = hitColor;
	}

	public int getDelay() {
		return Delay;
	}

	public void setDelay(int delay) {
		Delay = delay;
	}

	public String getSfx() {
		return sfx;
	}

	public void setSfx(String sfx) {
		this.sfx = sfx;
	}
	

	
}
