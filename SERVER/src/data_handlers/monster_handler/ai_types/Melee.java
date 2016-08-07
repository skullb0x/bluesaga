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

public class Melee extends BaseAI {
	
	private static WorldMap PathMap;
	private static AStarPathFinder pathfinder;
	private static Path lastPath;
		
	public Melee(Npc monster) {
		super(monster);
	}

	public void doAggroBehaviour(Vector<Npc> monsterMoved) {
		Creature target = me.getAggroTarget();
		int dX = target.getX() - me.getX();
		int dY = target.getY() - me.getY();

		boolean nearTarget = false;
		double distToTarget = Math.sqrt(Math.pow(dX, 2)+Math.pow(dY,2));
		boolean lostAggro = false;
		
		// CHECK IF TARGET IS CLOSE ENOUGH TO ATTACK
		if(Math.floor(distToTarget) <= me.getAttackRange()){
			nearTarget = true;
		
			float angleNeeded = MathUtils.angleBetween(-dX, -dY);

			if(angleNeeded < 0){
				angleNeeded = 360 + angleNeeded;
			}
			
			if(me.getGotoRotation() != angleNeeded){
				me.setGotoRotation(angleNeeded);
				monsterMoved.add(me);
			}
		}

		// If Guardian then check if target is on water, if target is, then drop aggro
		if(me.getOriginalAggroType() == 5){
			Tile targetTile = Server.WORLD_MAP.getTile(target.getX(), target.getY(), target.getZ());
			if(targetTile != null){
				if(targetTile.isWater()){
					lostAggro = true;
				}
			}
		}
		
		// IF FAR AWAY FROM PLAYER USE PATHFINDING
		if(!nearTarget && !lostAggro){
			int chaseRange = me.getAggroRange() * 2;
			
			// IF INSIDE CHASE RANGE - AGGRORANGE * 3
			if (distToTarget <= chaseRange && me.getZ() == target.getZ()) {
				
				// GET UPDATED X AND Y FROM TARGET
				int goalX = target.getX();
				int goalY = target.getY();
				int goalZ = target.getZ();

				int oldMonsterX = me.getX();
				int oldMonsterY = me.getY();
				int oldMonsterZ = me.getZ();

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
					lastPath = pathfinder.findPath(pathMover, me.getX() - PathMap.getPathMapStartX(), me.getY() - PathMap.getPathMapStartY(), goalX - PathMap.getPathMapStartX(), goalY - PathMap.getPathMapStartY());
				}catch(ArrayIndexOutOfBoundsException e){
					ServerMessage.printMessage("crash! - can't find path!",true);
					lastPath = null;
				}

				if (lastPath != null) {

					int stepX = lastPath.getX(1) + PathMap.getPathMapStartX();
					int stepY = lastPath.getY(1) + PathMap.getPathMapStartY();
					int stepZ = me.getZ();

					
					if(Server.WORLD_MAP.isPassableTileForMonster(me, stepX, stepY, stepZ)){

						int diagonalMove = 0;

						me.walkTo(stepX,stepY,stepZ);

						if (stepX < me.getX()) {
							diagonalMove++;
						} else if (stepX > me.getX()) {
							diagonalMove++;
						} 

						if (stepY < me.getY()) {
							diagonalMove++;
						} else if (stepY > me.getY()) {
							diagonalMove++;
						}

						boolean diagonal = false;
						if(diagonalMove > 1){
							diagonal = true;
						}

						me.startMoveTimer(diagonal);
					}
				}else{
					// Lose aggro
					lostAggro = true;
				}

				// OCCUPY TILES
				Server.WORLD_MAP.getTile(me.getX(),me.getY(), oldMonsterZ).setOccupant(CreatureType.Monster, me);
				
				// Check monster movement consequences
				if(oldMonsterX != me.getX() || oldMonsterY != me.getY()){
					MonsterHandler.checkMonsterMoveConsequences(me);
					monsterMoved.add(me);
				}

			} else {
				// Lose Aggro
				lostAggro = true;
			}
		}
		
		if(lostAggro){
			monsterMoved.add(me);
		}
	}
}
