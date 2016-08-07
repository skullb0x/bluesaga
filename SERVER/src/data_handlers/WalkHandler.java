package data_handlers;

import game.ServerSettings;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import creature.Creature.CreatureType;
import utils.ServerGameInfo;
import data_handlers.ability_handler.StatusEffect;
import data_handlers.ability_handler.StatusEffectHandler;
import data_handlers.battle_handler.BattleHandler;
import data_handlers.item_handler.ContainerHandler;
import data_handlers.item_handler.Item;
import data_handlers.monster_handler.MonsterHandler;
import map.AreaEffect;
import map.Door;
import map.Moveable;
import map.Tile;
import map.TileData;
import map.Trigger;
import network.Client;
import network.Server;
import map.Trap;

public class WalkHandler extends Handler {

	public static void init() {
		DataHandlers.register("canwalk", m -> handleCanWalk(m));
		DataHandlers.register("changedir", m -> handleChangeDirection(m));
	}
	
	public static void handleCanWalk(Message m) {
		Client client = m.client;
		if (client.playerCharacter == null) return;

		String[] coords = m.message.split(":");

		int changeX = Integer.parseInt(coords[0]);
		int changeY = Integer.parseInt(coords[1]);


		int gotoX = client.playerCharacter.getX() + changeX;
		int gotoY = client.playerCharacter.getY() + changeY;
		int gotoZ = client.playerCharacter.getZ();

		if(client.playerCharacter.hasStatusEffect(27)){
			gotoX = client.playerCharacter.getX() - changeX;
			gotoY = client.playerCharacter.getY() - changeY;
		}

		if(canWalk(client,gotoX,gotoY, gotoZ)){
			TutorialHandler.updateTutorials(0,client);
			
			// Reset ranged attack cooldown, longer depending on diagonal move or not
			boolean diagonalMove = false;
			if(changeX != 0 && changeY != 0){
				diagonalMove = true;
			}
			client.playerCharacter.resetRangedAttackCooldown(diagonalMove);

			movePlayer(client, gotoX, gotoY, gotoZ);
		}else{
			addOutGoingMessage(client,"nowalk",client.playerCharacter.getX()+","+client.playerCharacter.getY()+","+client.playerCharacter.getZ());
		}
	}

	public static void handleChangeDirection(Message m) {
		Client client = m.client;
		if (client.playerCharacter == null) return;
		Float newAngle = Float.parseFloat(m.message);

		client.playerCharacter.setGotoRotation(newAngle);

		// Send new direction to all players
		for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
			Client s = entry.getValue();

			if(s.Ready && isVisibleForPlayer(s.playerCharacter,client.playerCharacter.getX(),client.playerCharacter.getY(),client.playerCharacter.getZ())){
				addOutGoingMessage(s,"change_dir",client.playerCharacter.getSmallData()+";"+newAngle);
			}
		}
	}

	public static boolean canWalk(Client client, int gotoX, int gotoY, int gotoZ){
		boolean okWalk = false;


		if(client.playerCharacter.isReadyToMove() && !client.playerCharacter.isDead()){
			Tile gotoTile = Server.WORLD_MAP.getTile(gotoX,gotoY, gotoZ);
			if(gotoTile != null){
				if(gotoZ != client.playerCharacter.getZ()){
					okWalk = false;
				}else{
					
					if(gotoTile.isPassableForPlayer(client.playerCharacter)){
						okWalk = true;
					}
					
					// Npc bug fix: check that npc is not located on tile
					if(gotoTile.getOccupant() != null){
						if(gotoTile.getOccupant().getCreatureType() == CreatureType.Monster){
							okWalk = false;
						}
					}
					
					// CHECK IF PLAYER IS PK, 
					// THEN FORBID PLAYER TO WALK INTO ARENAS
					if(client.playerCharacter.getPkMarker() > 0){
						if(Server.WORLD_MAP.getTile(gotoX, gotoY, gotoZ) != null){
							if(Server.WORLD_MAP.getTile(gotoX, gotoY, gotoZ).getType().equals("arena")){
								okWalk = false;
							}
							// Check if area effect is safe
							int areaEffectId = Server.WORLD_MAP.getTile(gotoX, gotoY, gotoZ).getAreaEffectId();
							if(areaEffectId > 0){
								if(ServerGameInfo.areaEffectsDef.get(areaEffectId).getGuardedLevel() == 0){
									okWalk = false;
								}
							}
							if(!okWalk){
								if(client.playerCharacter.getPkMarker() > 0){
									addOutGoingMessage(client, "message", "#messages.walking.no_enter_safe_zones");
								}
							}
						}
					}


					// CHECK IF TILE HAS BLOCK STATUSEFFECT
					if(gotoTile.getStatusEffect(41) != null){
						okWalk = false;
					}


					// CHECK IF DOOR AND IF IT IS LOCKED OR REQUIRES PREMIUM
					if(gotoTile.getDoorId() > 0){
						ResultSet doorInfo = Server.mapDB.askDB("select Locked, GotoX, GotoY, GotoZ, CreatureIds, Premium from door where Id = "+gotoTile.getDoorId());
						try {
							if(doorInfo.next()){
								int keyId = doorInfo.getInt("Locked");
								int premiumDoor = doorInfo.getInt("Premium");

								if(keyId > 0){
									okWalk = false;

									if(premiumDoor > 0){
										// IF PREMIUM KEY IS NEEDED, BOUGHT IN THE SHOP
										// CHECK IF USER HAS TICKET
										ResultSet checkTickets = Server.userDB.askDB("select TicketMorwyn, TicketNorth from user_settings where UserId = "+client.UserId);

										if(checkTickets.next()){
											if(keyId == 180 && checkTickets.getInt("TicketMorwyn") == 1){
												okWalk = true;
												addOutGoingMessage(client,"unlockdoor","Boat Ticket");
											}else if(keyId == 215 && checkTickets.getInt("TicketNorth") == 1){
												okWalk = true;
												addOutGoingMessage(client,"unlockdoor","Boat Ticket");
											}else{
												addOutGoingMessage(client,"message","#messages.walking.need_boat_ticket");
											}
										}
										checkTickets.close();

									}else{
										// CHECK IF PLAYER HAS KEY
										ResultSet checkKeys = Server.userDB.askDB("select KeyId from character_key where KeyId = "+keyId+" and CharacterId = "+client.playerCharacter.getDBId());

										if(checkKeys.next()){
											okWalk = true;

											// CHECK IF KEY IS DESTROYABLE
											Item keyItem = new Item(ServerGameInfo.itemDef.get(keyId));

											addOutGoingMessage(client,"unlockdoor",keyItem.getName());

											if(keyItem.getSubType().equals("Destroyable")){
												Server.userDB.updateDB("delete from character_key where KeyId = "+keyId+" and CharacterId = "+client.playerCharacter.getDBId());
												addOutGoingMessage(client,"message","#messages.walking.you_lost"+" "+keyItem.getName());
											}
										}else{
											addOutGoingMessage(client,"message","#messages.walking.need_key");
										}
										checkKeys.close();
									}
								}else if(!doorInfo.getString("CreatureIds").equals("None")){
									okWalk = false;
									if(!gotoTile.isMonsterLocked()){
										okWalk = true;
									}
								}else {
									okWalk = true;
								}

								// CHECK IF PLAYER IS PK, 
								// THEN FORBID PLAYER TO WALK INTO SAFE ZONES
								if(client.playerCharacter.getPkMarker() > 0){
									if(Server.WORLD_MAP.getTile(doorInfo.getInt("GotoX"), doorInfo.getInt("GotoY"), doorInfo.getInt("GotoZ")) != null){
										if(Server.WORLD_MAP.getTile(doorInfo.getInt("GotoX"), doorInfo.getInt("GotoY"), doorInfo.getInt("GotoZ")).getType().equals("indoors")){
											okWalk = false;
										}
										// Check if area effect is safe
										int areaEffectId = Server.WORLD_MAP.getTile(doorInfo.getInt("GotoX"), doorInfo.getInt("GotoY"), doorInfo.getInt("GotoZ")).getAreaEffectId();
										if(areaEffectId > 0){
											if(ServerGameInfo.areaEffectsDef.get(areaEffectId).getGuardedLevel() == 0){
												okWalk = false;
											}
										}
										
										if(!okWalk){
											if(client.playerCharacter.getPkMarker() > 0){
												addOutGoingMessage(client, "message", "#messages.walking.no_enter_safe_zones");
											}
										}
									}
								}
							}
							doorInfo.close();
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}


					// CHECK IF MOVEABLE OBJECT ON TILE
					if(gotoTile.getObjectId().contains("moveable")){
						okWalk = false;

						// CHECK IF NEXT TILE IN WALKING DIRECTION IS PASSABLE
						int playerX = client.playerCharacter.getX();
						int playerY = client.playerCharacter.getY();

						int dirX = gotoX - playerX;
						int dirY = gotoY - playerY;

						int nextX = gotoX + dirX;
						int nextY = gotoY + dirY;

						Tile nextTile = null;
						nextTile = Server.WORLD_MAP.getTile(nextX, nextY, client.playerCharacter.getZ());
						if(nextTile != null){
							if(nextTile.isPassableForPlayer(client.playerCharacter) && !nextTile.isWater()){
								if(!nextTile.getObjectId().contains("moveable")){
									okWalk = true;

									// MOVE OBJECT!
									String movedObjectName = gotoTile.getObjectId();

									Moveable movedObject = ContainerHandler.MOVEABLES.get(gotoX+","+gotoY+","+gotoZ);
									if(movedObject != null){
										movedObject.setX(nextX);
										movedObject.setY(nextY);
										ContainerHandler.MOVEABLES.remove(gotoX+","+gotoY+","+gotoZ);
										ContainerHandler.MOVEABLES.put(nextX+","+nextY+","+gotoZ,movedObject);
										movedObject.resetRespawnTimer();
									}
									gotoTile.setObjectId("None");
									nextTile.setObjectId(movedObjectName);



									// SEND OBJECT MOVE TO CLIENTS IN AREA
									for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
										Client s = entry.getValue();

										if(s.Ready){
											if(isVisibleForPlayer(s.playerCharacter, gotoTile.getX(), gotoTile.getY(), gotoTile.getZ())){
												addOutGoingMessage(s,"object_move",movedObjectName+";"+gotoX+","+gotoY+","+gotoZ+";"+nextX+","+nextY+","+gotoTile.getZ());
											}
										}
									}

								}
							}
						}

					}
				}
			}
		}



		return okWalk;
	}



	public static void movePlayer(Client client, int PlayerX, int PlayerY, int PlayerZ){


		Tile oldTile = Server.WORLD_MAP.getTile(client.playerCharacter.getX(),client.playerCharacter.getY(),client.playerCharacter.getZ()); 

		// FREE OLD TILE PLAYER WAS STANDING ON
		if(oldTile != null){
			oldTile.setOccupant(CreatureType.None,null);
		}

		int dirX = 0;
		int dirY = 0;

		StringBuilder rowData = new StringBuilder(2500);

		int doorId = Server.WORLD_MAP.getTile(PlayerX,PlayerY,PlayerZ).getDoorId();


		if(doorId > 0 || Server.WORLD_MAP.getTile(PlayerX,PlayerY,PlayerZ).getGeneratedDoor() != null){
			// GET DOOR DATA

			if(Server.WORLD_MAP.getTile(PlayerX,PlayerY,PlayerZ).getGeneratedDoor() != null){
				Door generatedDoor = Server.WORLD_MAP.getTile(PlayerX,PlayerY,PlayerZ).getGeneratedDoor();
				PlayerX = generatedDoor.getGotoX();
				PlayerY = generatedDoor.getGotoY();
				PlayerZ = generatedDoor.getGotoZ();

				doorId = 666; // YEAAAAH!!
				
				if(Server.WORLD_MAP.getTile(PlayerX,PlayerY,PlayerZ) != null){
					addOutGoingMessage(client,"fade","yes");
				}
			}else{
				ResultSet doorData = Server.mapDB.askDB("select GotoX,GotoY,GotoZ from door where Id = "+doorId);

				try {
					if(doorData.next()){
						if(doorData.getInt("GotoX") == 0 && doorData.getInt("GotoX") == 0 && doorData.getInt("GotoX") == 0){
							// IF DOOR HAS NO GOTO POSITION, THEN JUST LET PLAYER PASS THROUGH
							dirX = PlayerX - client.playerCharacter.getX();
							dirY = PlayerY - client.playerCharacter.getY();
							doorId = 0;
						}else{
							PlayerX = doorData.getInt("GotoX");
							PlayerY = doorData.getInt("GotoY");
							PlayerZ = doorData.getInt("GotoZ");

							if(Server.WORLD_MAP.getTile(PlayerX,PlayerY,PlayerZ) != null){
								addOutGoingMessage(client,"fade","yes");
							}
						}
					}
					doorData.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
			}
		}else{
			dirX = PlayerX - client.playerCharacter.getX();
			dirY = PlayerY - client.playerCharacter.getY();
		}

		TileData tileData = new TileData();

		if(doorId > 0){
			
			// Remove player from old z index
			Server.WORLD_MAP.removePlayerFromZ(client.playerCharacter,client.playerCharacter.getZ());
			
			// WALKED ON DOOR, SEND WHOLE SCREEN
			client.playerCharacter.walkTo(PlayerX, PlayerY, PlayerZ);

			// Add player to new z index
			Server.WORLD_MAP.addPlayerToZ(client.playerCharacter,client.playerCharacter.getZ());
			
			// Send "close death" tutorial
			TutorialHandler.updateTutorials(8, client);

			
			// SET NEW POSITION OF PLAYER
			MapHandler.sendScreenData(client);
		}else{

			// SEND ROW OF TILES

			int tileX = 0;
			int tileY = 0;
			int tileZ = client.playerCharacter.getZ();


			for(int i = 0; i < Math.abs(dirX); i++){
				if(dirX < 0){
					// GO LEFT
					tileX = client.playerCharacter.getX() - ServerSettings.TILE_HALF_W - i;

					for(tileY = client.playerCharacter.getY() - ServerSettings.TILE_HALF_H-1; tileY <= client.playerCharacter.getY() + ServerSettings.TILE_HALF_H+1; tileY++){
						MapHandler.getTileInfo(client,tileX, tileY, tileZ, tileData, rowData);
					}
					//client.playerCharacter.setGotoRotation(270);

				}else if(dirX > 0){
					// GO RIGHT
					tileX = client.playerCharacter.getX() + ServerSettings.TILE_HALF_W + i;

					for(tileY = client.playerCharacter.getY() - ServerSettings.TILE_HALF_H-1; tileY <= client.playerCharacter.getY() + ServerSettings.TILE_HALF_H+1; tileY++){
						MapHandler.getTileInfo(client,tileX, tileY, tileZ, tileData, rowData);
					}
					//client.playerCharacter.setGotoRotation(90);
				}
			}

			for(int i = 0; i < Math.abs(dirY); i++){
				if(dirY > 0){
					// GO DOWN
					tileY = client.playerCharacter.getY() + ServerSettings.TILE_HALF_H + i;

					for(tileX = client.playerCharacter.getX() - ServerSettings.TILE_HALF_W-1; tileX <= client.playerCharacter.getX() + ServerSettings.TILE_HALF_W +1; tileX++){
						MapHandler.getTileInfo(client,tileX, tileY, tileZ, tileData, rowData);
					}
					//client.playerCharacter.setGotoRotation(180);

				}else if(dirY < 0){
					// GO UP
					tileY = client.playerCharacter.getY() - ServerSettings.TILE_HALF_H - i;

					for(tileX = client.playerCharacter.getX() - ServerSettings.TILE_HALF_W-1; tileX <= client.playerCharacter.getX() + ServerSettings.TILE_HALF_W +1; tileX++){
						MapHandler.getTileInfo(client,tileX, tileY, tileZ, tileData, rowData);
					}
					//client.playerCharacter.setGotoRotation(0);
				}
			}

			addOutGoingMessage(client,"tilerow", rowData.toString());

			// SET NEW POSITION OF PLAYER
			client.playerCharacter.walkTo(PlayerX, PlayerY, PlayerZ);
		}


		
		Server.userDB.updateDB("update user_character set X = "+PlayerX+", Y = "+PlayerY+", Z = "+PlayerZ+" where Id ="+client.playerCharacter.getDBId());
		

		if(Server.WORLD_MAP.getTile(PlayerX,PlayerY,PlayerZ) != null){
			Server.WORLD_MAP.getTile(PlayerX,PlayerY,PlayerZ).setOccupant(CreatureType.Player,client.playerCharacter);
		
			// BOAT OR NOT CHECK
			if(Server.WORLD_MAP.getTile(client.playerCharacter.getX(), client.playerCharacter.getY(), client.playerCharacter.getZ()).isWater()){
				// TURN PLAYER INTO BOAT!
				if(client.playerCharacter.getShip() != null){
					if(client.playerCharacter.getShip().getShipId() > 0){
						if(!client.playerCharacter.getShip().isShow()){
							client.playerCharacter.getShip().setShow(true);
							
							// SEND BOAT INFO TO OTHER PLAYERS
							for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
								Client other = entry.getValue();

								if(other.Ready){
									if(isVisibleForPlayer(other.playerCharacter,client.playerCharacter.getX(),client.playerCharacter.getY(), client.playerCharacter.getZ())){
										addOutGoingMessage(other, "goboat", client.playerCharacter.getSmallData()+";"+client.playerCharacter.getShip().getShipId()+",1");
									}
								}
							}
						}
					}
				}
			}else{
				if(client.playerCharacter.getShip() != null){
					if(client.playerCharacter.getShip().isShow()){
						// PLAYER GOES OFF BOAT!
						client.playerCharacter.getShip().setShow(false);
						// SEND BOAT INFO TO OTHER PLAYERS

						for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
							Client other = entry.getValue();

							if(other.Ready){
								if(isVisibleForPlayer(other.playerCharacter,client.playerCharacter.getX(),client.playerCharacter.getY(), client.playerCharacter.getZ())){
									addOutGoingMessage(other, "goboat", client.playerCharacter.getSmallData()+";"+client.playerCharacter.getShip().getShipId()+",0");
								}
							}
						}	
					}
				}
			}
			
		}

		

		Tile playerTile = Server.WORLD_MAP.getTile(client.playerCharacter.getX(),client.playerCharacter.getY(),client.playerCharacter.getZ());

		if(tileData.foundSoul){
			addOutGoingMessage(client,"soul",tileData.soulX+","+tileData.soulY+","+tileData.soulZ);
		}

		if(tileData.foundMonsterLockedDoor){
			addOutGoingMessage(client,"lock_door",tileData.monsterLockedX+","+tileData.monsterLockedY+","+tileData.monsterLockedZ);
		}

		if(playerTile != null){
			// CHECK IF AREA EFFECT
			if(playerTile.getAreaEffectId() > 0){
				// SAVE MAP EFFECT IN USER TABLE
				sendAreaEffect(client, playerTile.getAreaEffectId());
	
				// CHECK QUESTS RELATED TO FIND AREA
				QuestHandler.updateFindAreaQuests(client, playerTile.getAreaEffectId());
			}
			
			// CHECK IF PLAYER WALKS ON SOUL
			if(playerTile.getSoulCharacterId() == client.playerCharacter.getDBId()){
				ResultSet soulInfo = Server.userDB.askDB("select Id, XP from character_soul where CharacterId = "+client.playerCharacter.getDBId());

				try {
					if(soulInfo.next()){
						int xp = soulInfo.getInt("XP");
						BattleHandler.addXP(client,xp);
					
						Server.userDB.updateDB("delete from character_soul where CharacterId  = "+client.playerCharacter.getDBId());

						addOutGoingMessage(client, "remove_soul", playerTile.getX()+","+playerTile.getY()+","+playerTile.getZ());
					}
					soulInfo.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				playerTile.setSoulCharacterId(0);
			}
			
			// CHECK IF TILE HAS STATUS EFFECT
			for(StatusEffect statusFX: playerTile.getStatusEffects()){
				StatusEffect seFX = new StatusEffect(statusFX.getId());
				seFX.setCaster(statusFX.getCaster());
				seFX.setAbility(statusFX.getAbility());
				StatusEffectHandler.addStatusEffect(client.playerCharacter, seFX);
			}
		
			// CHECK IF TILE HAS TRAP
			if(playerTile.getTrapId() > 0){
				Trap T = TrapHandler.getTrap(playerTile.getTrapId()); 

				if(T.isOn() && T.getDamage() > 0){
					TrapHandler.trapDamage(T);
				}
			}

			// CHECK IF TILE HAS TRIGGER
			if(playerTile.getTrigger() != null){
				Trigger T = playerTile.getTrigger();

				T.setTriggered(true);

				// IF TRIGGERS TRAP, TRAP EFFECT
				if(T.getTrapId() > 0){
					TrapHandler.triggerTrap(T.getTrapId(),playerTile.getX(),playerTile.getY(),playerTile.getZ());
				}
			}
		}

		// CHECK IF PLAYERS IS IN MONSTER AGGRO RANGE
		if(client.playerCharacter.getAdminLevel() < 3){
			MonsterHandler.alertNearMonsters(client.playerCharacter, client.playerCharacter.getX(), client.playerCharacter.getY(), client.playerCharacter.getZ(),false);
		}

		// SEND INFO ABOUT MOVE TO ALL CLIENTS IN SAME AREA
		String moveInfo = "Player,"+client.playerCharacter.getDBId()+","+client.playerCharacter.getCreatureId()+","+client.playerCharacter.getOldX()+","+client.playerCharacter.getOldY()+","+client.playerCharacter.getOldZ()+","+client.playerCharacter.getRotation()+"/"+client.playerCharacter.getX()+","+client.playerCharacter.getY()+","+client.playerCharacter.getZ()+","+client.playerCharacter.getStat("SPEED")+","+client.playerCharacter.getGotoRotation()+";";

		for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
			Client s = entry.getValue();

			if(s.Ready){
				if(isVisibleForPlayer(s.playerCharacter,PlayerX,PlayerY,PlayerZ) || isVisibleForPlayer(s.playerCharacter,client.playerCharacter.getOldX(),client.playerCharacter.getOldY(),client.playerCharacter.getOldZ())  ){
					if(s.playerCharacter.getDBId() != client.playerCharacter.getDBId()){
						addOutGoingMessage(s,"creaturepos",moveInfo);
					}
				}
			}
		}
	}




	public static void sendAreaEffect(Client client, int areaEffectId){
		Server.userDB.updateDB("update user_character set AreaEffectId = "+areaEffectId+" where Id = "+client.playerCharacter.getDBId());
		client.playerCharacter.setAreaEffectId(areaEffectId);

		if(areaEffectId > 0){
			AreaEffect ae = ServerGameInfo.areaEffectsDef.get(areaEffectId);
	
			String song = ae.getSong(); 
			String ambient = ae.getAmbient();
			String particles = ae.getParticles(); 
	
			if(MapHandler.dayNightTime == 1){
				if(client.playerCharacter.getZ() == 0){
					song = "night";
					ambient = "night";
					particles = "firefly";
				}
			}
			
			String effectData = ae.getId()+","+ae.getAreaName()+","+ae.getTint()+","+ae.getTintColor()+","+ae.getFog()+","+ae.getFogColor()+","+song+","+ambient+","+particles+","+ae.getGuardedLevel();
	
			if(areaEffectId == 2){
				TutorialHandler.updateTutorials(3, client);
			}
			addOutGoingMessage(client,"area_effect",effectData);
		}
	}
}
