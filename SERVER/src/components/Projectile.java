package components;

import game.ServerSettings;

import java.util.Timer;
import map.Tile;
import network.Server;
import creature.Creature;
import data_handlers.ability_handler.Ability;

public class Projectile {

	
	private Ability Ability;
	
	private float OriginX;
	private float OriginY;
	private int OriginZ;
	
	private float moveX;
	private float moveY;
	
	private float goalX;
	private float goalY;
	private int goalZ;
	
	private boolean Active;
	
	private float dX = 0.0f;
	private float dY = 0.0f;
	
	private float distance = 0.0f;
	
	
	private int effectId;
	private int Delay;
	private float speed = 30.0f;
	
	public Projectile(Ability ability, int originX, int originY, int originZ, int newGoalX, int newGoalY, int newGoalZ){
		Ability = ability;
		
		Active = true;

		Delay = Ability.getDelay();

		OriginX = originX;
		OriginY = originY;
		setOriginZ(originZ);
		
		goalX = newGoalX;
		goalY = newGoalY;
		goalZ = newGoalZ;
		
		moveX = 0;
		moveY = 0;
		
		float diffX = (goalX - OriginX) * ServerSettings.TILE_SIZE;
		float diffY = (OriginY - goalY) * ServerSettings.TILE_SIZE;
		
		distance = (float) Math.sqrt(Math.pow(diffX,2) + Math.pow(diffY,2));

		// Calculate dX,dY per tick
		dX = (diffX/distance);
		dY = (diffY/distance);
		
		// Check if there is a non-passable tile in the way
		// If not reached, check if obstacle in the way
		// Exception: Goblin bombs
		if(ability.getAbilityId() != 49){
			float checkDxDy = (float) Math.sqrt(Math.pow(dX, 2) + Math.pow(dY, 2));
			
			for(float i = 0.0f; i <= distance+50; i += checkDxDy*speed){
				int checkX = (int) OriginX + Math.round((dX * i) / ServerSettings.TILE_SIZE);
				int checkY = (int) OriginY - Math.round((dY * i) / ServerSettings.TILE_SIZE);
				
				if(checkX != OriginX || checkY != OriginY){
					Tile checkTile = Server.WORLD_MAP.getTile(checkX, checkY, OriginZ);
					if(checkTile != null){
						
						if(checkX == goalX && checkY == goalY){
							break;
						}else if((!checkTile.isPassableType() && ability.getAbilityId() != 45) 
								|| checkTile.getOccupant() != null){
							goalX = checkTile.getX();
							goalY = checkTile.getY();
							break;
						}
					}
				}
			}
		}
		
		
		setEffectId(ability.getProjectileEffectId());
	}
	
	
	
	
	public void updatePos(){
		if(Active){
		
			// Check if reached goal
			if(getTileX() == goalX && getTileY() == goalY){
				Active = false;
			}else{
				moveX += dX*speed;
				moveY += dY*speed;
				
				// If not reached, check if obstacle in the way
				Tile checkTile = Server.WORLD_MAP.getTile(getTileX(), getTileY(), OriginZ);
				if(checkTile != null){
					boolean hitOccupant = false;
					
					if(checkTile.getOccupant() != null){
						hitOccupant = true;
						if(getCaster() != null){
							if(checkTile.getOccupant().getCreatureType().equals(getCaster().getCreatureType())
									&& checkTile.getOccupant().getDBId() == getCaster().getDBId()){
								// Ignore caster
								hitOccupant = false;
							}
						}
					}
					if(hitOccupant){
						Active = false;
					}
				}
			}
			
			if(!Active && Delay > 0){
				Delay -= 54;
				Active = true;
			}
			
		}
	}
	
	
	public int getTileX(){
		return (int) (OriginX + Math.round(moveX/ServerSettings.TILE_SIZE));
	}
	
	public int getTileY(){
		return (int) (OriginY - Math.round(moveY/ServerSettings.TILE_SIZE));
	}
	
	public boolean travelledTooFar(){
		float travelledDistance = (float) Math.sqrt(Math.pow(moveX,2) + Math.pow(moveY,2));
		if(travelledDistance >= distance){
			return true;
		}
		return false;
	}
	
	
	public boolean getActive(){
		return Active;
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
	
	public Ability getAbility(){
		return Ability;
	}
	
	public Creature getCaster(){
		return Ability.getCaster();
	}
	
	public int getGoalX(){
		return (int) goalX;
	}
	
	public int getGoalY(){
		return (int) goalY;
	}


	public int getGoalZ() {
		return goalZ;
	}


	public void setGoalZ(int goalZ) {
		this.goalZ = goalZ;
	}


	public int getOriginZ() {
		return OriginZ;
	}


	public void setOriginZ(int originZ) {
		OriginZ = originZ;
	}




	public int getEffectId() {
		return effectId;
	}




	public void setEffectId(int effectId) {
		this.effectId = effectId;
	}
	
	
	
}
