package data_handlers.monster_handler.ai_types;

import java.awt.Point;
import java.util.List;
import java.util.Vector;

import map.WorldMap;
import network.Server;

import org.newdawn.slick.util.pathfinding.AStarPathFinder;
import org.newdawn.slick.util.pathfinding.Path;

import utils.ServerMessage;
import utils.Spiral;
import creature.Creature;
import creature.Npc;
import creature.PathMover;
import creature.Creature.CreatureType;
import data_handlers.monster_handler.MonsterHandler;

public class Ranged {
	
	private static WorldMap PathMap;
	private static AStarPathFinder pathfinder;
	private static Path lastPath;
	
	public static void doAggroBehaviour(Npc aggroMonster, Creature target, Vector<Npc> monsterMoved, Vector<Npc> monsterLostAggro){
		int dX = target.getX() - aggroMonster.getX();
		int dY = target.getY() - aggroMonster.getY();

		double distToTarget = Math.sqrt(Math.pow(dX, 2)+Math.pow(dY,2));
		boolean lostAggro = false;
		
		// CHECK IF TARGET IS TOO CLOSE, THEN STEP BACK
		if(Math.floor(distToTarget) < aggroMonster.getAttackRange()){
			// Find free tile that is outside range
			Spiral s = new Spiral(10,10);
			List<Point> l = s.spiral();

			Point foundTile = null;
			
			for(Point p: l){
				if(p.getX() != 0 || p.getY() != 0){
					int escapeX = (int) (aggroMonster.getX()+p.getX());
					int escapeY = (int) (aggroMonster.getY()+p.getY());

					distToTarget = Math.sqrt(Math.pow(escapeX - target.getX(), 2)+Math.pow(escapeY - target.getY(),2));
					if(distToTarget == aggroMonster.getAttackRange()){
						
						if(Server.WORLD_MAP.getTile(escapeX,escapeY,aggroMonster.getZ()) != null){
							if(Server.WORLD_MAP.getTile(escapeX,escapeY,aggroMonster.getZ()).isPassableNonAggro()){
								foundTile = new Point(escapeX,escapeY);
								break;
							}
						}
					}
				}
			}
			
			if(foundTile != null){
				int monsterOldX = aggroMonster.getX();
				int monsterOldY = aggroMonster.getY();
				int monsterOldZ = aggroMonster.getZ();
				
				int goalX = (int) foundTile.getX();
				int goalY = (int) foundTile.getY();
				int goalZ = monsterOldZ;
				
				// MAKE A PATHFINDING MAP
				PathMap = new WorldMap();
				PathMap.createPathMap(monsterOldX, monsterOldY, monsterOldZ, goalX, goalY, goalZ);

				pathfinder = new AStarPathFinder(PathMap, PathMap.getPathMapSize(), MonsterHandler.diagonalWalk);

				PathMover pathMover = new PathMover(CreatureType.Monster);
				pathMover.setTarget(goalX, goalY, goalZ);

				lastPath = new Path();
				
				// FREE TILES
				Server.WORLD_MAP.getTile(monsterOldX, monsterOldY, monsterOldZ).setOccupant(CreatureType.None, null);
				
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
						monsterMoved.add(aggroMonster);
					}
				}
				
				// OCCUPY TILES
				Server.WORLD_MAP.getTile(aggroMonster.getX(),aggroMonster.getY(), aggroMonster.getZ()).setOccupant(CreatureType.Monster, aggroMonster);
				
				// Check monster movement consequences
				if(goalX != aggroMonster.getX() || goalY != aggroMonster.getY()){
					MonsterHandler.checkMonsterMoveConsequences(aggroMonster);
					monsterMoved.add(aggroMonster);
				}
			}
		}else{
			// Target is too far away for attack, chase target if within chase range
			
			int chaseRange = aggroMonster.getAggroRange() * 2;
			
			// IF INSIDE CHASE RANGE - AGGRORANGE * 3
			if (distToTarget < chaseRange && distToTarget > aggroMonster.getAttackRange() && aggroMonster.getZ() == target.getZ()) {
				
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
