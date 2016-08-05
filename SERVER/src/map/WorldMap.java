package map;

import instances.DungeonGenerator;
import instances.PirateIslandGenerator;
import network.Client;
import network.Server;

import java.awt.Point;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import org.newdawn.slick.util.pathfinding.Mover;
import org.newdawn.slick.util.pathfinding.PathFindingContext;
import org.newdawn.slick.util.pathfinding.TileBasedMap;

import utils.ServerGameInfo;
import utils.ServerMessage;
import utils.Spiral;
import creature.Creature;
import creature.Npc;
import creature.PathMover;
import creature.PlayerCharacter;
import creature.Creature.CreatureType;
import data_handlers.Handler;
import data_handlers.MapHandler;
import data_handlers.TrapHandler;
import data_handlers.item_handler.ContainerHandler;
import data_handlers.item_handler.Item;
import data_handlers.monster_handler.MonsterHandler;

public class WorldMap implements TileBasedMap {

	private HashMap<String,Tile> MapTiles;
	
	public HashMap<Integer,Vector<Npc>> monstersByZ;
	public HashMap<Integer,Vector<PlayerCharacter>> playersByZ;
	public Vector<Integer> zLevels;
	
	private boolean[][] visited;

	private int pathMapSize;
	private int pathZ;
	private int pathMapStartX;
	private int pathMapStartY;

	private int entranceX;
	private int entranceY;

	private String MapType;

	private int NrPlayers;

	private ConcurrentHashMap<Integer,Npc> Monsters;

	private int newMonsterId = 0;

	public WorldMap() {

	}

	public int getPathMapSize(){
		return pathMapSize;
	}

	public void loadMap() {
		Monsters = new ConcurrentHashMap<Integer, Npc>();
		Monsters.clear();

		MapTiles = new HashMap<String, Tile>();

		NrPlayers = 0;

		int minX = 100000;
		int minY = 100000;
		int maxX = 0;
		int maxY = 0;

		monstersByZ = new HashMap<Integer,Vector<Npc>>();
		playersByZ = new HashMap<Integer,Vector<PlayerCharacter>>();
		zLevels = new Vector<Integer>();
		pathMapSize = 0;

		// LOAD MAP FROM DB
		ServerMessage.printMessage("Loading map...",false);

		// LOAD MAP FROM DB
		ResultSet tileInfo = Server.mapDB.askDB("select * from area_tile order by Y asc, X asc");

		try {
			while(tileInfo.next()){
				Tile newTile =  new Tile(tileInfo.getInt("X"),tileInfo.getInt("Y"),tileInfo.getInt("Z"));
				newTile.setZ(tileInfo.getInt("Z"));
				newTile.setType(tileInfo.getString("Type"), tileInfo.getString("Name"), tileInfo.getInt("Passable"));
				newTile.setObjectId(tileInfo.getString("ObjectId"));
				newTile.setDoorId(tileInfo.getInt("DoorId"));
				newTile.setAreaEffectId(tileInfo.getInt("AreaEffectId"));

				if(newTile.getObjectId().contains("moveable")){
					ContainerHandler.MOVEABLES.put(newTile.getX()+","+newTile.getY()+","+newTile.getZ(), new Moveable(newTile.getObjectId(),newTile.getX(),newTile.getY(),newTile.getZ()));
				}

				MapTiles.put(tileInfo.getInt("X")+","+tileInfo.getInt("Y")+","+tileInfo.getInt("Z"),newTile);

				if(tileInfo.getInt("X") > maxX){
					maxX = tileInfo.getInt("X");
				}

				if(tileInfo.getInt("Y") > maxY){
					maxY = tileInfo.getInt("Y");
				}

				if(tileInfo.getInt("X") < minX){
					minX = tileInfo.getInt("X");
				}

				if(tileInfo.getInt("Y") < minY){
					minY = tileInfo.getInt("Y");
				}

				if(newTile.getDoorId() > 0){
					// LOAD DOORINFO
					ResultSet doorInfo = Server.mapDB.askDB("select CreatureIds from door where Id = "+newTile.getDoorId()+" and CreatureIds != 'None'");
					while(doorInfo.next()){
						newTile.setMonsterLocked(true);
					}
					doorInfo.close();
				}
				
				int tileZ = tileInfo.getInt("Z");
				
				if(!monstersByZ.containsKey(tileZ)){
					monstersByZ.put(tileZ, new Vector<Npc>());
				}
				if(!playersByZ.containsKey(tileZ)){
					playersByZ.put(tileZ, new Vector<PlayerCharacter>());
				}
				if(!zLevels.contains(tileZ)){
					zLevels.add(tileZ);
				}
			}
			tileInfo.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		
		ServerMessage.printMessage("Load traps...",false);

		// LOAD TRAPS AND TRIGGERS
		ResultSet areatrapInfo = Server.mapDB.askDB("select * from area_trap");

		try {
			while(areatrapInfo.next()){
				ResultSet trapInfo = Server.mapDB.askDB("select * from trap where Id = "+areatrapInfo.getInt("TrapId"));
				if(trapInfo.next()){
					Trap newTrap = new Trap();
					newTrap.load(trapInfo);
					newTrap.setId(areatrapInfo.getInt("Id"));
					newTrap.setX(areatrapInfo.getInt("X"));
					newTrap.setY(areatrapInfo.getInt("Y"));
					newTrap.setZ(areatrapInfo.getInt("Z"));

					TrapHandler.addTrap(newTrap);

					MapTiles.get(areatrapInfo.getInt("X")+","+areatrapInfo.getInt("Y")+","+areatrapInfo.getInt("Z")).setTrapId(newTrap.getId());

				}
				trapInfo.close();
			}
			areatrapInfo.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		ServerMessage.printMessage("Load triggers...",false);

		ResultSet triggerInfo = Server.mapDB.askDB("select * from trigger");

		try {
			while(triggerInfo.next()){
				Trigger newTrigger = new Trigger();
				newTrigger.load(triggerInfo);

				MapTiles.get(triggerInfo.getInt("X")+","+triggerInfo.getInt("Y")+","+triggerInfo.getInt("Z")).setTrigger(newTrigger);
			}
			triggerInfo.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		ServerMessage.printMessage("Load containers...",false);

		// LOAD CONTAINERS
		ResultSet containerInfo = Server.mapDB.askDB("select Id, Type, X, Y, Z from area_container order by Y asc, X asc");

		try {
			while(containerInfo.next()){
				if(MapTiles.get(containerInfo.getInt("X")+","+containerInfo.getInt("Y")+","+containerInfo.getInt("Z")) != null){
					MapTiles.get(containerInfo.getInt("X")+","+containerInfo.getInt("Y")+","+containerInfo.getInt("Z")).setObjectId(containerInfo.getString("Type"));
					MapTiles.get(containerInfo.getInt("X")+","+containerInfo.getInt("Y")+","+containerInfo.getInt("Z")).setContainerId(containerInfo.getInt("Id"));
				}
			}
			containerInfo.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		ServerMessage.printMessage("Load souls...",false);

		// LOAD SOULS
		ResultSet soulInfo = Server.userDB.askDB("select Id, X,Y,Z,CharacterId from character_soul");
		try {
			while(soulInfo.next()){
				if(MapTiles.get(soulInfo.getInt("X")+","+soulInfo.getInt("Y")+","+soulInfo.getInt("Z")) != null){
					MapTiles.get(soulInfo.getInt("X")+","+soulInfo.getInt("Y")+","+soulInfo.getInt("Z")).setSoulCharacterId(soulInfo.getInt("CharacterId"));
				}else{
					Server.userDB.updateDB("delete from character_soul where Id = "+soulInfo.getInt("Id"));
				}
			}
			soulInfo.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		if(maxX > maxY){
			pathMapSize = maxX;
		}else{
			pathMapSize = maxY;
		}

		ServerMessage.printMessage("Load monsters...",false);

		// LOAD NPCs FROM DATABASE
		ResultSet creatureInfo;
		
		creatureInfo = Server.mapDB.askDB("select * from area_creature where AggroType <> 5");
		
		
		Npc tempNpc;
		
		try {
			while (creatureInfo.next()) {
				tempNpc = new Npc(ServerGameInfo.creatureDef.get(creatureInfo.getInt("CreatureId")), creatureInfo.getInt("SpawnX"), creatureInfo.getInt("SpawnY"), creatureInfo.getInt("SpawnZ"));
				tempNpc.setDBId(creatureInfo.getInt("Id"));

				tempNpc.setAggroType(creatureInfo.getInt("AggroType"));
				tempNpc.setOriginalAggroType(creatureInfo.getInt("AggroType"));

				if(!creatureInfo.getString("Name").equals("")){
					tempNpc.setName(creatureInfo.getString("Name"));
				}

				tempNpc.setCreatureType(CreatureType.Monster);
				
				// Sets eventual equipment
				String equipmentInfo = creatureInfo.getString("Equipment");
				if(!equipmentInfo.equals("None")){
					String equipment[] = equipmentInfo.split(",");
					
					for(String itemId_str: equipment){
						try{
							int itemId = Integer.parseInt(itemId_str);
							if(ServerGameInfo.itemDef.containsKey(itemId)){
								tempNpc.equipItem(new Item(ServerGameInfo.itemDef.get(itemId)));
							}
						}catch(NumberFormatException e){
						}
					}
				}
				
				// CHANCE OF MAKING MONSTER SPECIAL
				if(tempNpc.getLevel() > 9 && tempNpc.getOriginalAggroType() < 3){
					tempNpc.turnSpecial(0);
				}
				
				// SET DAY NIGHT SPAWN TIME
				tempNpc.setExistDayOrNight(creatureInfo.getInt("SpawnCriteria"));

				if(tempNpc.getExistDayOrNight() == 1){
					tempNpc.die();
					tempNpc.setRespawnTimerReady();
				}

				if(MapTiles.get(creatureInfo.getInt("SpawnX") + "," + creatureInfo.getInt("SpawnY")+","+creatureInfo.getInt("SpawnZ")) == null){
					ServerMessage.printMessage("MONSTER ON NULL TILE: "+creatureInfo.getInt("Id"),false);
				}else{
					Monsters.put(creatureInfo.getInt("Id"),tempNpc);
					if(!tempNpc.isDead()){
						MapTiles.get(creatureInfo.getInt("SpawnX") + "," + creatureInfo.getInt("SpawnY")+","+creatureInfo.getInt("SpawnZ")).setOccupant(CreatureType.Monster, tempNpc);
					}
				}

				if(creatureInfo.getInt("Id") > newMonsterId){
					newMonsterId = creatureInfo.getInt("Id");
				}
				
				// Add monster to z-index maps
				addMonsterToZ(tempNpc,tempNpc.getZ());
			}
			creatureInfo.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		ServerMessage.printMessage("Generating random archipelago...", false);
		
		// Pirate island
		zLevels.add(-200);
		playersByZ.put(-200,new Vector<PlayerCharacter>());
		monstersByZ.put(-200, new Vector<Npc>());
		
		PirateIslandGenerator.generate(MapTiles);
	
		ServerMessage.printMessage("Generating random dungeons...", false);
		
		// Generate instances
		DungeonGenerator dGenerator = new DungeonGenerator();
		dGenerator.generate(MapTiles,10);
		
		dGenerator.generate(MapTiles,20);
		
		dGenerator.generate(MapTiles,30);
		
		ServerMessage.printMessage("Loading world map done.",false);
	}

	public void addMonsterToZ(Npc npc, int z){
		if(monstersByZ.containsKey(z)){
			monstersByZ.get(z).add(npc);
		}
	}
	
	public void addPlayerToZ(PlayerCharacter player, int z){
		if(playersByZ.containsKey(z)){
			playersByZ.get(z).add(player);
		}
	}
	
	public void removePlayerFromZ(PlayerCharacter player, int z){
		if(playersByZ.containsKey(z)){
			playersByZ.get(z).remove(player);
		}
	}
	
	public void removeMonsterFromZ(Npc monster, int z){
		if(monstersByZ.containsKey(z)){
			monstersByZ.get(z).remove(monster);
		}
	}
	
	
	/*
	 * 
	 * MONSTER MOVEMENT
	 *
	 */


	public Vector<Npc> moveNonAggroMonsters() {
		Vector<Npc> movedMonsters = new Vector<Npc>();

		for(int z: zLevels){
			if(playersByZ.get(z).size() > 0){
				for(Iterator<Npc> iter = monstersByZ.get(z).iterator();iter.hasNext();){ 
					Npc m = iter.next();
					if (!m.isAggro() && !m.isDead() && m.isReadyToMove()) {
						
						// Respawn guardian if not at spawn point
						if(m.getOriginalAggroType() == 5){
							if(m.getX() != m.getSpawnX() || m.getY() != m.getSpawnY()){
								// Send dissappearence to players
								// MONSTER DISSAPPEAR
								Server.WORLD_MAP.getTile(m.getX(), m.getY(), m.getZ()).setOccupant(CreatureType.None, null);
								
								for (Entry<Integer, Client> entry : Server.clients.entrySet()) {
									Client s = entry.getValue();
									if(s.Ready){
										if(Handler.isVisibleForPlayer(s.playerCharacter, m.getX(), m.getY(), m.getZ())){
											Handler.addOutGoingMessage(s,"monstergone",m.getSmallData());
										}
									}
								}
								
								m.respawn();
								Server.WORLD_MAP.getTile(m.getX(), m.getY(), m.getZ()).setOccupant(m.getCreatureType(), m);
								
								// MONSTER APPEAR
								for (Entry<Integer, Client> entry : Server.clients.entrySet()) {
									Client s = entry.getValue();
									if(s.Ready){
										if(Handler.isVisibleForPlayer(s.playerCharacter, m.getSpawnX(), m.getSpawnY(), m.getSpawnZ())){
											Handler.addOutGoingMessage(s,"respawnmonster",m.getSmallData());
										}
									}
								}
							}
						}else if(MonsterHandler.movingMonsterTypes.contains(m.getAggroType())){
							if(m.getHealth() < m.getStat("MAX_HEALTH") && !m.isResting()){
								MonsterHandler.changeMonsterSleepState(m, true);
							}else if(!m.isResting()){
								// MOVE RANDOM DIRECTION

								boolean monsterMoved = false;

								int randomDir = ThreadLocalRandom.current().nextInt(8);

								monsterMoved = true;

								if (randomDir > 3) {
									int gotoX = m.getX();
									int gotoY = m.getY();

									if(m.getGotoRotation() == 0.0f){
										gotoY--;
									}else if(m.getGotoRotation() == 90.0f){
										gotoX++;
									}else if(m.getGotoRotation() == 180.0f){
										gotoY++;
									}else if(m.getGotoRotation() == 270.0f){
										gotoX--;
									}

									if(isPassableTileForMonster(m, gotoX, gotoY, m.getZ())){
										// Remove monster from tile
										MapTiles.get(m.getX()+","+m.getY()+","+m.getZ()).setOccupant(CreatureType.None, null);

										m.walkTo(gotoX, gotoY, m.getZ());

										// Occupy tile with monster
										MapTiles.get(m.getX()+","+m.getY()+","+m.getZ()).setOccupant(CreatureType.Monster, m);
									}
								} else if (randomDir == 0) {
									m.setGotoRotation(0.0f);
								} else if (randomDir == 1) {
									m.setGotoRotation(90.0f);
								} else if (randomDir == 2) {
									m.setGotoRotation(180.0f);
								}else if(randomDir == 3){
									m.setGotoRotation(270.0f);
								}

								m.startMoveTimer(false);

								if(monsterMoved){
									// CHECK IF TILE HAS TRIGGER
									if(MapTiles.get(m.getX()+","+m.getY()+","+m.getZ()).getTrigger() != null){
										Trigger T = MapTiles.get(m.getX()+","+m.getY()+","+m.getZ()).getTrigger();

										T.setTriggered(true);

										// IF TRIGGERS TRAP, TRAP EFFECT
										if(T.getTrapId() > 0){
											TrapHandler.triggerTrap(T.getTrapId(),m.getX(),m.getY(),m.getZ());
										}
									}

									movedMonsters.add(m);
								}
							}
						}
					}else if(!m.isDead()){
						MapTiles.get(m.getX()+","+m.getY()+","+m.getZ()).setOccupant(CreatureType.Monster, m);
					}
				}
			}
		}
		
		return movedMonsters;
	}

	
	/*
	 * 
	 * 	PATH MAP
	 * 
	 * 
	 */

	public void createPathMap(int startX, int startY, int startZ, int goalX, int goalY, int goalZ){
		setPathMapStartX(startX-10);
		setPathMapStartY(startY-10);
		setPathZ(startZ);

		pathMapSize = 20;

		visited = new boolean[pathMapSize][pathMapSize];

		int pathMapX = 0;
		int pathMapY = 0;

		MapTiles = new HashMap<String, Tile>();

		for(int i = startX - 10; i < startX + 10; i++){
			for(int j = startY - 10; j < startY + 10; j++){
				pathMapX = i - (startX - 10);
				pathMapY = j - (startY - 10);

				MapTiles.put(pathMapX+","+pathMapY+","+getPathZ(),Server.WORLD_MAP.getTile(i, j, getPathZ()));
			}
		}
	}

	public String checkRespawns(){

		String respawnInfo = "";

		for(Iterator<Npc> iter = getMonsters().values().iterator();iter.hasNext();){ 
			Npc m = iter.next();

			if(m.isDead() && !m.isSpawned()){
				boolean okToRespawn = true;

				if(m.getExistDayOrNight() > 0 && m.getExistDayOrNight() != MapHandler.dayNightTime){
					okToRespawn = false;
					m.setRespawnTimerReady();
				}else{
					if(m.getCreatureId() != 1){
						// CHECK IF VISIBLE BY PLAYER
						for (Entry<Integer, Client> entry : Server.clients.entrySet()) {
							Client client = entry.getValue();

							boolean visibleByPlayer = false;

							if(client.Ready){
								if(client.playerCharacter.getZ() == m.getSpawnZ() && Math.abs(client.playerCharacter.getX() - m.getSpawnX()) < 11 && Math.abs(client.playerCharacter.getY() - m.getSpawnY()) < 6){
									visibleByPlayer = true;
								}
							}

							if(visibleByPlayer){
								okToRespawn = false;
								break;
							}
						}
					}
				}

				if(okToRespawn){
					if(m.checkRespawn()){

						/*
						// Chance of spawning more monsters if not npc
						if(m.getOriginalAggroType() != 3 && !m.isBoss() && m.getCreatureId() != 1){
							//int nrSpawns = RandomUtils.getInt(0, 2);
							int nrSpawns = 0;
							for(int i = 0; i < nrSpawns; i++){
								Npc newSpawn = new Npc(ServerGameInfo.creatureDef.get(m.getCreatureId()), m.getSpawnX(),m.getSpawnY(),m.getSpawnZ());
								newSpawn.setExistDayOrNight(m.getExistDayOrNight());
								newSpawn.setAggroType(m.getOriginalAggroType());
								Server.WORLD_MAP.addMonsterSpawn(newSpawn, m.getSpawnX(),m.getSpawnY(),m.getSpawnZ());
							}
						}
						*/
						
						MapTiles.get(m.getX()+","+m.getY()+","+m.getZ()).setOccupant(CreatureType.Monster, m);
						respawnInfo += m.getDBId()+",";
					}
				}
			}

			if(m.isDead() && m.isSpawned()){
				iter.remove();
			}

		}
		if(respawnInfo.equals("")){
			respawnInfo = "none";
		}

		return respawnInfo;
	}





	public String getAggroInfo(){
		String aggroInfo = "";

		for(Iterator<Npc> iter = getMonsters().values().iterator();iter.hasNext();){ 
			Npc m = iter.next();

			int aggroStatus = 0;

			if(!m.isDead() && m.isAggro()){
				aggroStatus = 1;
			}

			aggroInfo += aggroStatus+","+m.getDBId();

			if(aggroStatus == 1){
				aggroInfo += ","+m.getStat("MAX_HEALTH")+","+m.getHealth()+";";
			}else{
				aggroInfo += ";";
			}
		}

		return aggroInfo;
	}


	/*
	 * 
	 * 
	 * 	PROJECTILES
	 * 
	 * 
	 */


	public String checkProjectileObstacles(int startX, int startY, int startZ, int goalX, int goalY, int goalZ){
		String goalTile = goalX+","+goalY;

		float startXf = startX;
		float startYf = startY;

		float goalXf = goalX;
		float goalYf = goalY;

		float dX = (goalXf - startXf) * 50.0f;
		float dY = (goalYf - startYf) * 50.0f; 

		float gotoItr = 0.0f;


		if(Math.abs(dX) > Math.abs(dY)){
			gotoItr = Math.abs(dX); 
			dY = dY / Math.abs(dX);
			dX = dX / Math.abs(dX);
		}else{
			gotoItr = Math.abs(dY); 
			dX = dX / Math.abs(dY);
			dY = dY / Math.abs(dY);
		}

		startXf *= 50;
		startYf *= 50;

		for(float itr = 0.0f; itr < gotoItr; itr++){
			startXf += dX;
			startYf += dY;

			if(!MapTiles.get(Math.round(startXf/50) + "," + Math.round(startYf/50) + "," + startZ).isPassableType()){
				startXf -= dX;
				startYf -= dY;

				goalTile = Math.round(startXf/50)+","+ Math.round(startYf/50);
				break;
			}
		}
		return goalTile;
	}

	
	public void addMonster(Npc m){
		newMonsterId++;

		m.setDBId(newMonsterId);
		m.setCreatureType(CreatureType.Monster);
		m.setReadyToMove(true);		
		m.loadEquipment();
		getMonsters().put(newMonsterId, m);
		monstersByZ.get(m.getZ()).add(m);
	}
	
	public synchronized void addMonsterSpawn(Npc m, int posX, int posY, int posZ){
		newMonsterId++;

		m.setDBId(newMonsterId);
		m.setSpawned(true);
		m.setJustSpawned(true);
		m.generateLoot();
		m.setCreatureType(CreatureType.Monster);
		m.setReadyToMove(true);
		
		m.loadEquipment();

		for (Entry<Integer, Client> entry : Server.clients.entrySet()) {
			Client s = entry.getValue();

			if(s.Ready && Handler.isVisibleForPlayer(s.playerCharacter, posX, posY, posZ)){
				Handler.addOutGoingMessage(s,"respawnmonster",m.getSmallData());
			}
		}
		
		monstersByZ.get(m.getZ()).add(m);
		
		getMonsters().put(newMonsterId, m);
	}

	
	
	

	/*
	 * 
	 * GETTER / SETTER
	 */



	public HashMap<String,Tile> getMap() {
		return MapTiles;
	}

	public ConcurrentHashMap<Integer,Npc> getMonsters() {
		return Monsters;
	}



	public Tile getTile(int x, int y, int z) {
		return MapTiles.get(x+","+y+","+z);
	}

	public Npc getMonster(int monsterId) {
		return getMonsters().get(monsterId);
	}

	public int getEntranceX() {
		return entranceX;
	}

	public int getEntranceY() {
		return entranceY;
	}

	public String getType() {
		return MapType;
	}

	public int getNrPlayers() {
		return NrPlayers;
	}


	public int getNrMonsters() {
		return Monsters.size();
	}

	public String getMonstersPosAsString() {
		String mobinfo = "";


		for(Iterator<Npc> iter = getMonsters().values().iterator();iter.hasNext();){ 
			Npc m = iter.next();

			// SEND dbId, newX, newY
			mobinfo += m.getDBId()+","+m.getX()+","+m.getY()+","+m.getStat("SPEED")+";";
		}
		return mobinfo;
	}


	public HashMap<String,Tile> getMapTiles(){
		return MapTiles;
	}


	public boolean isPassableTile(int X, int Y, int Z) {
		if(MapTiles.get(X+","+Y+","+Z) == null){
			return false;
		}
		return MapTiles.get(X+","+Y+","+Z).isPassable();
	}

	public boolean isPassableTileForMonster(Creature c, int X, int Y, int Z) {
		Tile gotoTile = MapTiles.get(X+","+Y+","+Z);

		if(gotoTile == null){
			return false;
		}

		if(gotoTile.getTrapId() > 0){
			return false;
		}

		if(gotoTile.getDoorId() > 0 ||gotoTile.getGeneratedDoor() != null){
			return false;
		}

		/*
		if(gotoTile.getStatusEffects().size() > 0){
			if(c.isAggro()){
				for(StatusEffect se: gotoTile.getStatusEffects()){
					if(!c.hasStatusEffect(se.getId())){
						return false;
					}
				}
			}else{
				return false;
			}
		}
		 */

		if(!c.getFamily().equals("Ocean Monsters") && gotoTile.isWater()){
			if(gotoTile.getObjectId().contains("bridge")){
				return true;
			}
			return false;
		}else if(c.getFamily().equals("Ocean Monsters") && !gotoTile.isWater()){
			return false;
		}

		return gotoTile.isPassableNonAggro();
	}


	public boolean isPassableTileForPlayer(PlayerCharacter player, int X, int Y) {
		boolean passable = true;

		if (passable) {
			passable = MapTiles.get(X+","+Y).isPassableForPlayer(player);
		}
		return passable;
	}

	
	public Point findClosestFreeTile(int startX, int startY, int startZ){
		Spiral s = new Spiral(8,8);
		List<Point> l = s.spiral();

		Point foundTile = null;
		
		for(Point p: l){
			if(p.getX() != 0 || p.getY() != 0){
				int targetX = (int) (startX+p.getX());
				int targetY = (int) (startY+p.getY());

				if(getTile(targetX,targetY,startZ) != null){
					if(getTile(targetX,targetY,startZ).isPassableNonAggro()){
						foundTile = new Point(targetX,targetY);
						return foundTile;
					}
				}
			}
		}
		return foundTile;
	}
	

	/****************************************
	 * * PATHFINDING * * *
	 ****************************************/

	/**
	 * Clear the array marking which tiles have been visted by the path finder.
	 */
	public void clearVisited() {
		for (int x = 0; x < getWidthInTiles(); x++) {
			for (int y = 0; y < getHeightInTiles(); y++) {
				visited[x][y] = false;
			}
		}
	}

	/**
	 * @see TileBasedMap#visited(int, int)
	 */
	public boolean visited(int x, int y) {
		return visited[x][y];
	}

	/**
	 * @see TileBasedMap#blocked(Mover, int, int)
	 */
	@Override
	public boolean blocked(PathFindingContext pathMover, int x, int y) {

		PathMover mover = (PathMover) pathMover.getMover();

		if(MapTiles.get(x+","+y+","+getPathZ()) != null){
			if (MapTiles.get(x+","+y+","+getPathZ()).isPassableNonAggro() ||
					x == mover.getTargetX() - getPathMapStartX() && y == mover.getTargetY() - getPathMapStartY()) {
				return false;
			}
		}else{
			return false;
		}
		return true;

		//}
	}

	/**
	 * @see TileBasedMap#getCost(Mover, int, int, int, int)
	 */
	@Override
	public float getCost(PathFindingContext arg0, int arg1, int arg2) {
		return 1;
	}

	/**
	 * @see TileBasedMap#getHeightInTiles()
	 */
	@Override
	public int getHeightInTiles() {
		return pathMapSize;
	}

	/**
	 * @see TileBasedMap#getWidthInTiles()
	 */
	@Override
	public int getWidthInTiles() {
		return pathMapSize;
	}

	/**
	 * @see TileBasedMap#pathFinderVisited(int, int)
	 */
	@Override
	public void pathFinderVisited(int x, int y) {
		visited[x][y] = true;
	}

	public int getPathZ() {
		return pathZ;
	}

	public void setPathZ(int pathZ) {
		this.pathZ = pathZ;
	}

	public int getPathMapStartX() {
		return pathMapStartX;
	}

	public void setPathMapStartX(int pathMapStartX) {
		this.pathMapStartX = pathMapStartX;
	}

	public int getPathMapStartY() {
		return pathMapStartY;
	}

	public void setPathMapStartY(int pathMapStartY) {
		this.pathMapStartY = pathMapStartY;
	}
}