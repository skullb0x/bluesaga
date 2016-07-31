package data_handlers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import creature.Npc;
import creature.Creature.CreatureType;
import data_handlers.ability_handler.StatusEffect;
import map.Tile;
import map.TileData;
import network.Client;
import network.Server;
import game.ServerSettings;


public class MapHandler extends Handler {

	public static int dayNightTime = 2; // 2 = day, 1 = night 

	public static int worldTimeItr = 0;
	public static int worldDayDuration = 4*3600; 
	public static int worldNightTime = 3*3600; // hours before it becomes night time

	public static void init() {
	}

	public static void handleData(Client client, String message){

		if(message.startsWith("<screen>")){
			if(client.playerCharacter != null){
				sendScreenData(client);

				// SEND NEW PLAYER TO NEARBY PLAYERS
				String playerData = client.playerCharacter.getSmallData();

				for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
					Client other = entry.getValue();

					if(other.Ready){
						if(other.playerCharacter.getDBId() != client.playerCharacter.getDBId()){
							if(isVisibleForPlayer(other.playerCharacter,client.playerCharacter.getX(),client.playerCharacter.getY(),client.playerCharacter.getZ())){
								addOutGoingMessage(other,"new_creature",playerData);
							}
						}
					}
				}
			}
		}else if(message.startsWith("<cinfo>")){
			String cTypeId[] = message.substring(7).split(",");
			CreatureType cType = CreatureType.valueOf(cTypeId[0]);
			int cDbId = Integer.parseInt(cTypeId[1]);

			sendCreatureInfo(client, cType, cDbId);
		}
	}

	public static void sendCreatureInfo(Client client, CreatureType cType, int cDbId){
		String cData = "";
		String cSE = "";

		if(cType == CreatureType.Player){
			for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
				Client s = entry.getValue();
				if(s.playerCharacter != null){
					if(s.playerCharacter.getDBId() == cDbId){
						cData = s.playerCharacter.getFullData();

						// get creature status effects
						int nrStatusEffects = s.playerCharacter.getStatusEffects().size();
						if(nrStatusEffects > 0){
							cSE = s.playerCharacter.getSmallData()+"/";
							for(Iterator<StatusEffect> iter = s.playerCharacter.getStatusEffects().values().iterator();iter.hasNext();){  
								StatusEffect se = iter.next();
								cSE += se.getId()+","+se.getGraphicsNr()+";";
							}
						}

						break;
					}
				}
			}
		}else{
			Npc monster = Server.WORLD_MAP.getMonster(cDbId);
			if(monster != null){
				cData = monster.getFullData();

				// get creature status effects
				int nrStatusEffects = monster.getStatusEffects().size();
				if(nrStatusEffects > 0){
					cSE = monster.getSmallData()+"/";
					for(Iterator<StatusEffect> iter = monster.getStatusEffects().values().iterator();iter.hasNext();){  
						StatusEffect se = iter.next();
						cSE += se.getId()+","+se.getGraphicsNr()+";";
					}
				}
			}
		}

		if(!cData.equals("")){
			addOutGoingMessage(client,"cinfo",cData);

			if(!cSE.equals("")){
				addOutGoingMessage(client,"creature_seffects",cSE);
			}

		}
	}
	
	
	public static void sendScreenData(Client client){
		if(client.playerCharacter != null){

			client.playerCharacter.setAggro(null);

			if(worldTimeItr > worldNightTime){
				if(client.playerCharacter.getZ() == 0 || client.playerCharacter.getZ() >= 10){
					// Send night time
					addOutGoingMessage(client,"night","now");
				}else{
					addOutGoingMessage(client,"night","stopnow");
				}
			}


			// Send "talk" tutorial
			TutorialHandler.updateTutorials(1, client);
		
			int z = client.playerCharacter.getZ();

			TileData tileData = new TileData();

			addOutGoingMessage(client,"canwalk",client.playerCharacter.getX()+","+client.playerCharacter.getY()+","+client.playerCharacter.getZ()+","+client.playerCharacter.getStat("SPEED"));

			// Gather tile data
			StringBuilder screenData = new StringBuilder(1000);

			for(int j = client.playerCharacter.getY() - ServerSettings.TILE_HALF_H - 1; j < client.playerCharacter.getY() + ServerSettings.TILE_HALF_H+2; j++){
				for(int i = client.playerCharacter.getX() - ServerSettings.TILE_HALF_W - 1; i < client.playerCharacter.getX() + ServerSettings.TILE_HALF_W+2; i++){

					getTileInfo(client, i, j, z, tileData, screenData);
				}
			}

			// Send tile data
			addOutGoingMessage(client, "screen", screenData.toString());

			// Send info about soul if found
			if(tileData.foundSoul){
				addOutGoingMessage(client,"soul",tileData.soulX+","+tileData.soulY+","+tileData.soulZ);
			}

			// Send info about monster lock if found
			if(tileData.foundMonsterLockedDoor){
				addOutGoingMessage(client,"lock_door",tileData.monsterLockedX+","+tileData.monsterLockedY+","+tileData.monsterLockedZ);
			}
		}
	}

	public void playerChangeZ(Client client, int oldZ, int newZ){
		
	}
	
	
	/**
	 * Gets all data to send for a tile
	 * @param client
	 * @param tileX
	 * @param tileY
	 * @param tileZ
	 * @param tileData - additional info about the whole screen of tiles
	 * @return
	 */
	public static void getTileInfo(Client client, int tileX, int tileY, int tileZ, TileData tileData, StringBuilder buf){

		Tile TILE = Server.WORLD_MAP.getTile(tileX,tileY,tileZ);

		// Data to send
		String tileType = "none";
		String tileName = "none";
		int passable = 0;
		int lootInfo = 0;
		String objectInfo = "0";
		String statusEffects = "0";
		String occupantInfo = "0";
		
		// Check if tile exists
		if(TILE != null){
			tileType = TILE.getType();
			tileName = TILE.getName();

			// Occupant info
			if(TILE.getOccupant() != null){
				occupantInfo = TILE.getOccupant().getSmallData();
			}else{
				occupantInfo = "0";
			}
			
			// Object info
			if(!TILE.getObjectId().equals("None")){
				String objectId = TILE.getObjectId();
				if(objectId.contains("chest")){
					// CHECK IF CHEST IS OPENED FOR PLAYER
					ResultSet chestInfo = Server.userDB.askDB("select ContainerId from character_container where ContainerId = "+TILE.getContainerId()+" and CharacterId = "+client.playerCharacter.getDBId());
					try {
						if(chestInfo.next()){
							objectId += "_open";
						}
						chestInfo.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				objectInfo = objectId;
			}else{
				objectInfo = "0";
			}
			
			// Loot info
			if(TILE.getLoot().size() > 0){
				lootInfo = 1; 
			}else{
				lootInfo = 0;
			}
			
			// Passable info
			if(TILE.isPassableType() || TILE.getDoorId() > 0){
				passable = 1;
			}
			
			// Status effects info
			if(TILE.getStatusEffects().size() > 0){
				statusEffects = "";
				for(StatusEffect se: TILE.getStatusEffects()){
					statusEffects += se.getId()+"/"+se.getGraphicsNr()+"-";
				}
				statusEffects = statusEffects.substring(0,statusEffects.length()-1);
			}
			
			
			// Check for soul
			if(client.playerCharacter.getDBId() == TILE.getSoulCharacterId()){
				tileData.foundSoul = true;
				tileData.soulX = TILE.getX();
				tileData.soulY = TILE.getY();
				tileData.soulZ = TILE.getZ();
			}

			// Check for monster lock
			if(TILE.isMonsterLocked()){
				tileData.foundMonsterLockedDoor = true;
				tileData.monsterLockedX = TILE.getX();
				tileData.monsterLockedY = TILE.getY();
				tileData.monsterLockedZ = TILE.getZ();
			}
		}
		
		buf.append(tileX).append(',')
		   .append(tileY).append(',')
		   .append(tileZ).append(',')
		   .append(tileType).append(',')
		   .append(tileName).append(',')
		   .append(passable).append(',')
		   .append(lootInfo).append(',')
		   .append(objectInfo).append(',')
		   .append(statusEffects).append(':')
		   .append(occupantInfo).append(';');
	}

	public static void checkIfDoorOpens(int MonsterDBId){
		// CHECK IF OPENS DOOR
		ResultSet doorCheck = Server.mapDB.askDB("select Id from door where CreatureIds = '"+MonsterDBId+"'");
		try {
			if(doorCheck.next()){
				ResultSet tileInfo = Server.mapDB.askDB("select X,Y,Z from area_tile where DoorId = "+doorCheck.getInt("Id"));
				if(tileInfo.next()){
					Tile DoorTile = Server.WORLD_MAP.getTile(tileInfo.getInt("X"), tileInfo.getInt("Y"), tileInfo.getInt("Z"));
					if(DoorTile != null){
						DoorTile.setMonsterLocked(false);
					}

					// SEND TO ALL PLAYERS IN AREA THAT DOOR IS OPENED
					for (Entry<Integer, Client> entry : Server.clients.entrySet()) {
						Client s = entry.getValue();
						if(s.Ready){
							if(isVisibleForPlayer(s.playerCharacter,DoorTile.getX(),DoorTile.getY(),DoorTile.getZ())){
								addOutGoingMessage(s,"unlock_door",DoorTile.getX()+","+DoorTile.getY()+","+DoorTile.getZ());
							}
						}
					}
				}
				tileInfo.close();
			}
			doorCheck.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void checkIfDoorCloses(int MonsterDBId){
		// CHECK IF CLOSES DOOR
		ResultSet doorCheck = Server.mapDB.askDB("select Id from door where CreatureIds = '"+MonsterDBId+"'");
		try {
			if(doorCheck.next()){
				ResultSet tileInfo = Server.mapDB.askDB("select X,Y,Z from area_tile where DoorId = "+doorCheck.getInt("Id"));
				if(tileInfo.next()){
					Tile DoorTile = Server.WORLD_MAP.getTile(tileInfo.getInt("X"), tileInfo.getInt("Y"), tileInfo.getInt("Z"));
					if(DoorTile != null){
						DoorTile.setMonsterLocked(true);
					}

					// SEND TO ALL PLAYERS IN AREA THAT DOOR IS OPENED
					for (Entry<Integer, Client> entry : Server.clients.entrySet()) {
						Client s = entry.getValue();
						if(s.Ready){
							if(isVisibleForPlayer(s.playerCharacter,DoorTile.getX(),DoorTile.getY(),DoorTile.getZ())){
								addOutGoingMessage(s,"lock_door",DoorTile.getX()+","+DoorTile.getY()+","+DoorTile.getZ());
							}
						}
					}
				}
				tileInfo.close();
			}
			doorCheck.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void updateNightTime(){
		worldTimeItr++;

		boolean dayNightChange = false;
		
		if(worldTimeItr == worldDayDuration){
			// NIGHT ENDS, MORNING COMES
			
			dayNightTime = 2;

			dayNightChange = true;
			
			
			worldTimeItr = 0;
		}else if(worldTimeItr == worldNightTime){
			// NIGHT TIME
			
			dayNightTime = 1;

			dayNightChange = true;
		}
		
		if(dayNightChange){
			// Go through monsters, remove those who don't like the time of the day
			for(Npc m: Server.WORLD_MAP.getMonsters().values()){
				if(m.getExistDayOrNight() != 0){
					if(m.getExistDayOrNight() != dayNightTime){
						// MONSTER DISSAPPEAR
						m.kill();
						Server.WORLD_MAP.getTile(m.getX(), m.getY(), m.getZ()).setOccupant(CreatureType.None, null);
						
						for (Entry<Integer, Client> entry : Server.clients.entrySet()) {
							Client s = entry.getValue();
							if(s.Ready){
								if(isVisibleForPlayer(s.playerCharacter, m.getSpawnX(), m.getSpawnY(), m.getSpawnZ())){
									addOutGoingMessage(s,"monstergone",m.getSmallData());
								}
							}
						}
					}
				}
			}
	
			// Send time change to all players
			for (Entry<Integer, Client> entry : Server.clients.entrySet()) {
				Client s = entry.getValue();
				if(s.Ready){
					if(s.playerCharacter.getZ() == 0 || s.playerCharacter.getZ() > 9){
						if(dayNightTime == 2){
							addOutGoingMessage(s,"night","stop");
						}else{
							addOutGoingMessage(s,"night","start");
						}
					}
				}
			}
		}
		
	}
}