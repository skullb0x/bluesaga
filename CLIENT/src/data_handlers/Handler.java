package data_handlers;

import creature.Creature;
import game.ClientSettings;

public class Handler {
	
	public Handler(){
	}
	
	public static boolean isVisibleForPlayer(Creature c, int x, int y, int z){
		boolean visible = false;
	
		if(c.getZ() == z && Math.abs(c.getX() - x) < ClientSettings.TILE_HALF_W+2 && Math.abs(c.getY() - y) < ClientSettings.TILE_HALF_H+3){
			visible = true;
		}
		
		return visible;
	}	
}
