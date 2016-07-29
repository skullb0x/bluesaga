package components;

import game.BlueSaga;

import java.awt.Point;
import java.util.List;
import java.util.Vector;

import creature.Creature.CreatureType;
import map.ScreenObject;
import screens.ScreenHandler;
import utils.MySpiral;

public class TargetingSystem {

	private static Vector<String> closestTargets = new Vector<String>();
	
	/**
	 * Loop in a spiral around player, find all monsters that are not guardians
	 */
	public static void findClosestTarget(boolean targetPlayer){
		MySpiral s = new MySpiral(8,8);
		List<Point> l = s.spiral();

		boolean foundTarget = false;

		for(Point p: l){
			if(p.getX() != 0 || p.getY() != 0){
				int targetX = (int) (BlueSaga.playerCharacter.getX()+p.getX());
				int targetY = (int) (BlueSaga.playerCharacter.getY()+p.getY());

				for(ScreenObject c: ScreenHandler.SCREEN_OBJECTS_DRAW){
					
					if(c.getType().equals("Creature")){
						if((targetPlayer && c.getCreature().getCreatureType() == CreatureType.Player) || (!targetPlayer && c.getCreature().getCreatureType() == CreatureType.Monster && c.getCreature().getCreatureId() != 86)){
							if(c.getX() == targetX && c.getY() == targetY){
								if(!closestTargets.contains(c.getCreature().getCreatureType().toString()+c.getCreature().getDBId())){
									foundTarget = true;
									closestTargets.add(c.getCreature().getCreatureType().toString()+c.getCreature().getDBId());
									BlueSaga.client.sendMessage("settarget",targetX+";"+targetY);
									break;
								}
							}
						}
					}
				}
				if(foundTarget){
					break;
				}
			}
		}
		
		if(!foundTarget && closestTargets.size() > 0){
			closestTargets.clear();
			findClosestTarget(targetPlayer);
		}
	}
}
