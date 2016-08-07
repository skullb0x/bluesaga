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

public class Ranged extends BaseAI {
	
	private static WorldMap PathMap;
	private static AStarPathFinder pathfinder;
	private static Path lastPath;
	
	public Ranged(Npc monster) {
		super(monster);
	}

	public void doAggroBehaviour(Vector<Npc> monsterMoved) {
		Creature target = me.getAggroTarget();
		int dX = target.getX() - me.getX();
		int dY = target.getY() - me.getY();

		double distToTarget = Math.sqrt(Math.pow(dX, 2)+Math.pow(dY,2));
		boolean lostAggro = false;
		
		// CHECK IF TARGET IS TOO CLOSE, THEN STEP BACK
		if(Math.floor(distToTarget) < me.getAttackRange()){
			// Find free tile that is outside range
			Spiral s = new Spiral(10,10);
			List<Point> l = s.spiral();

			Point foundTile = null;
			
			for(Point p: l){
				if(p.getX() != 0 || p.getY() != 0){
					int escapeX = (int) (me.getX()+p.getX());
					int escapeY = (int) (me.getY()+p.getY());

					distToTarget = Math.sqrt(Math.pow(escapeX - target.getX(), 2)+Math.pow(escapeY - target.getY(),2));
					if(distToTarget == me.getAttackRange()){
						
						if(Server.WORLD_MAP.getTile(escapeX,escapeY,me.getZ()) != null){
							if(Server.WORLD_MAP.getTile(escapeX,escapeY,me.getZ()).isPassableNonAggro()){
								foundTile = new Point(escapeX,escapeY);
								break;
							}
						}
					}
				}
			}
			
			if(foundTile != null){
				int monsterOldX = me.getX();
				int monsterOldY = me.getY();
				int monsterOldZ = me.getZ();
				
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
						monsterMoved.add(me);
					}
				}
				
				// OCCUPY TILES
				Server.WORLD_MAP.getTile(me.getX(),me.getY(), me.getZ()).setOccupant(CreatureType.Monster, me);
				
				// Check monster movement consequences
				if(goalX != me.getX() || goalY != me.getY()){
					MonsterHandler.checkMonsterMoveConsequences(me);
					monsterMoved.add(me);
				}
			}
		}else{
			// Target is too far away for attack, chase target if within chase range
			
			int chaseRange = me.getAggroRange() * 2;
			
			// IF INSIDE CHASE RANGE - AGGRORANGE * 3
			if (distToTarget < chaseRange && distToTarget > me.getAttackRange() && me.getZ() == target.getZ()) {
				
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
