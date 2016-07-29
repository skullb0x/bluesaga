package data_handlers.monster_handler.ai_types;

import java.util.Vector;

import map.Tile;
import map.WorldMap;
import network.Server;

import org.newdawn.slick.util.pathfinding.AStarPathFinder;
import org.newdawn.slick.util.pathfinding.Path;

import utils.MathUtils;
import utils.ServerMessage;
import creature.Creature;
import creature.Npc;
import creature.PathMover;
import creature.Creature.CreatureType;
import data_handlers.monster_handler.MonsterHandler;

public class Melee {
	
	private static WorldMap PathMap;
	private static AStarPathFinder pathfinder;
	private static Path lastPath;
		
	
	public static void doAggroBehaviour(Npc aggroMonster, Creature target, Vector<Npc> monsterMoved, Vector<Npc> monsterLostAggro){
		int dX = target.getX() - aggroMonster.getX();
		int dY = target.getY() - aggroMonster.getY();

		boolean nearTarget = false;
		double distToTarget = Math.sqrt(Math.pow(dX, 2)+Math.pow(dY,2));
		boolean lostAggro = false;
		
		// CHECK IF TARGET IS CLOSE ENOUGH TO ATTACK
		if(Math.floor(distToTarget) <= aggroMonster.getAttackRange()){
			nearTarget = true;
		
			float angleNeeded = MathUtils.angleBetween(-dX, -dY);

			if(angleNeeded < 0){
				angleNeeded = 360 + angleNeeded;
			}
			
			if(aggroMonster.getGotoRotation() != angleNeeded){
				aggroMonster.setGotoRotation(angleNeeded);
				monsterMoved.add(aggroMonster);
			}					
		}

		// If Guardian then check if target is on water, if target is, then drop aggro
		if(aggroMonster.getOriginalAggroType() == 5){
			Tile targetTile = Server.WORLD_MAP.getTile(target.getX(), target.getY(), target.getZ());
			if(targetTile != null){
				if(targetTile.isWater()){
					lostAggro = true;
				}
			}
		}
		
		// IF FAR AWAY FROM PLAYER USE PATHFINDING
		if(!nearTarget && !lostAggro){
			int chaseRange = aggroMonster.getAggroRange() * 2;
			
			// IF INSIDE CHASE RANGE - AGGRORANGE * 3
			if (distToTarget <= chaseRange && aggroMonster.getZ() == target.getZ()) {
				
				// GET UPDATED X AND Y FROM TARGET
				int goalX = target.getX();
				int goalY = target.getY();
				int goalZ = target.getZ();

				int oldMonsterX = aggroMonster.getX();
				int oldMonsterY = aggroMonster.getY();
				int oldMonsterZ = aggroMonster.getZ();

				// FREE TILES
				Server.WORLD_MAP.getTile(oldMonsterX, oldMonsterY, oldMonsterZ).setOccupant(CreatureType.None, null);

				// MAKE A PATHFINDING MAP
				PathMap = new WorldMap();
				PathMap.createPathMap(oldMonsterX, oldMonsterY, oldMonsterZ, goalX, goalY, goalZ);

				pathfinder = new AStarPathFinder(PathMap, PathMap.getPathMapSize(), MonsterHandler.diagonalWalk);

				PathMover pathMover = new PathMover(CreatureType.Monster);
				pathMover.setTarget(goalX, goalY, goalZ);

				lastPath = new Path();
				
				try {
					lastPath = pathfinder.findPath(pathMover, aggroMonster.getX() - PathMap.getPathMapStartX(), aggroMonster.getY() - PathMap.getPathMapStartY(), goalX - PathMap.getPathMapStartX(), goalY - PathMap.getPathMapStartY());
				}catch(ArrayIndexOutOfBoundsException e){
					ServerMessage.printMessage("crash! - can't find path!",true);
					lastPath = null;
				}

				if (lastPath != null) {

					int stepX = lastPath.getX(1) + PathMap.getPathMapStartX();
					int stepY = lastPath.getY(1) + PathMap.getPathMapStartY();
					int stepZ = aggroMonster.getZ();

					
					if(Server.WORLD_MAP.isPassableTileForMonster(aggroMonster, stepX, stepY, stepZ)){

						int diagonalMove = 0;

						aggroMonster.walkTo(stepX,stepY,stepZ);

						if (stepX < aggroMonster.getX()) {
							diagonalMove++;
						} else if (stepX > aggroMonster.getX()) {
							diagonalMove++;
						} 

						if (stepY < aggroMonster.getY()) {
							diagonalMove++;
						} else if (stepY > aggroMonster.getY()) {
							diagonalMove++;
						}

						boolean diagonal = false;
						if(diagonalMove > 1){
							diagonal = true;
						}

						aggroMonster.startMoveTimer(diagonal);
					}
				}else{
					// Lose aggro
					lostAggro = true;
				}

				// OCCUPY TILES
				Server.WORLD_MAP.getTile(aggroMonster.getX(),aggroMonster.getY(), oldMonsterZ).setOccupant(CreatureType.Monster, aggroMonster);
				
				// Check monster movement consequences
				if(oldMonsterX != aggroMonster.getX() || oldMonsterY != aggroMonster.getY()){
					MonsterHandler.checkMonsterMoveConsequences(aggroMonster);
					monsterMoved.add(aggroMonster);
				}

			} else {
				// Lose Aggro
				lostAggro = true;
			}
		}
		
		if(lostAggro){
			monsterMoved.add(aggroMonster);
		}
	}
}
