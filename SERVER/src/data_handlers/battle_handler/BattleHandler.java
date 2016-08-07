package data_handlers.battle_handler;

import game.ServerSettings;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import character_info.CharacterInfo;
import map.Tile;
import network.Client;
import network.Server;
import utils.MathUtils;
import utils.XPTables;
import creature.Creature;
import creature.Npc;
import creature.PlayerCharacter;
import creature.Creature.CreatureType;
import data_handlers.ClassHandler;
import data_handlers.DataHandlers;
import data_handlers.Handler;
import data_handlers.Message;
import data_handlers.QuestHandler;
import data_handlers.TutorialHandler;
import data_handlers.item_handler.ItemHandler;
import data_handlers.monster_handler.MonsterHandler;

public class BattleHandler extends Handler {

	public static int playerHitTime = 20; 


	public static void init() {
		//updatePkTime();
		
		DataHandlers.register("rest", m -> handleRest(m));
		DataHandlers.register("player_respawn", m -> handlePlayerRespawn(m));
		DataHandlers.register("settarget", m -> handleSetTarget(m));
	}
	
	public static void handleRest(Message m) {
		if (m.client.playerCharacter == null) return;
		Client client = m.client;
	
		if("start".equals(m.message)){
			MonsterHandler.changeMonsterSleepState(client.playerCharacter, true);
		}else{
			MonsterHandler.changeMonsterSleepState(client.playerCharacter, false);
		}
	}

	public static void handlePlayerRespawn(Message m) {
		if (m.client.playerCharacter == null) return;
		Client client = m.client;
		respawnPlayer(client.playerCharacter);

		client.Ready = true;

		int playerX = client.playerCharacter.getX();
		int playerY = client.playerCharacter.getY();
		int playerZ = client.playerCharacter.getZ();

		client.playerCharacter.getShip().setShow(false);

		client.playerCharacter.revive();
		addOutGoingMessage(client,"revive","");
		
		addOutGoingMessage(client,"update_bonusstats",client.playerCharacter.getBonusStatsAsString());

		Tile respawnTile = Server.WORLD_MAP.getTile(playerX, playerY, playerZ);

		if(respawnTile != null){
			respawnTile.setOccupant(CreatureType.Player, client.playerCharacter);
		}
		
		addOutGoingMessage(client, "respawn", playerX+","+playerY+","+playerZ);
	
		// Send death tutorial
		if(client.playerCharacter.getTutorialNr() == 7){
			TutorialHandler.updateTutorials(7,client);
		}
	}

	public static void handleSetTarget(Message m) {
		if (m.client.playerCharacter == null) return;
		Client client = m.client;
		String targetInfo[] = m.message.split(";");
		int targetX = Integer.parseInt(targetInfo[0]);
		int targetY = Integer.parseInt(targetInfo[1]);

		CreatureType targetType = CreatureType.None;
		boolean safeZone = false;

		Creature Target = null;
		
		boolean clickedObject = false;

		// Check for target or object on clicked tile
		if(Server.WORLD_MAP.getTile(targetX, targetY, client.playerCharacter.getZ()) != null){
			// If object then not look for nearby targets
			if(!Server.WORLD_MAP.getTile(targetX, targetY, client.playerCharacter.getZ()).getObjectId().equals("None")){
				clickedObject = true;
			}
			Target = Server.WORLD_MAP.getTile(targetX, targetY, client.playerCharacter.getZ()).getOccupant();
		}

		// If no target and no object
		if(Target == null && !clickedObject){
			for(int nearbyX = targetX-1; nearbyX <= targetX+1; nearbyX++){
				for(int nearbyY = targetY-1; nearbyY <= targetY+1; nearbyY++){
					if(Server.WORLD_MAP.getTile(nearbyX, nearbyY, client.playerCharacter.getZ()) != null){
						Target = Server.WORLD_MAP.getTile(nearbyX, nearbyY, client.playerCharacter.getZ()).getOccupant();

						if(Target != null){
							if(Target.getCreatureType() == CreatureType.Monster){
								Npc targetMonster = (Npc) Target;
								// Only target monsters with aggro type 2
								if(targetMonster.getOriginalAggroType() == 2){
									targetX = nearbyX;
									targetY = nearbyY;
									break;
								}
							}
						}
					}
				}
				if(Target != null){
					break;
				}
			}
		}


		if(Target != null){
			if(Target.getCreatureType() == CreatureType.Player && Target.getDBId() == client.playerCharacter.getDBId()){
				client.playerCharacter.setAggro(null);
				addOutGoingMessage(client, "settarget", "None,0");
			}else{
				targetType = Target.getCreatureType();

				// PVP - CHECK IF IN A SAFE ZONE OR CAN ATTACK TARGET
				if(targetType == CreatureType.Player){
					PlayerCharacter targetPlayer = (PlayerCharacter) Target;
						
					if(!PvpHandler.canAttackPlayer(client.playerCharacter,targetPlayer)){
						safeZone = true;
					}
				}


				// If Target is a NPC
				if(targetType == CreatureType.Monster){
					Npc npcTarget = (Npc) Target;

					if(npcTarget.getAggroType() == 3){
						// Get Quest dialog
						safeZone = true;
						QuestHandler.getNpcDialog(client, targetX,targetY,client.playerCharacter.getZ());
						client.playerCharacter.setAggro(null);
						addOutGoingMessage(client, "settarget", "None,0");
					}else if(!safeZone){
						// Update TARGET Tutorial
						TutorialHandler.updateTutorials(4, client);
					}
				}

				// IF NOT SAFEZONE AND TARGET LEGIT, PROCEED WITH ATTACK
				if(!safeZone){
					client.playerCharacter.setAggro(Target);
					addOutGoingMessage(client, "settarget", targetType+","+Target.getDBId());
				}else{
					client.playerCharacter.setAggro(null);
					addOutGoingMessage(client, "settarget", "None,0");
				}

			}
		}else{
			client.playerCharacter.setAggro(null);
			addOutGoingMessage(client, "settarget", "None,0");
		}
	}

	

	public static void attack(Creature ATTACKER, Creature TARGET){

		// Check if attack is ok
		boolean attackOk = BattleHandler.checkAttackOk(ATTACKER.getX(),ATTACKER.getY(),ATTACKER.getZ(),TARGET.getX(), TARGET.getY(),TARGET.getZ());
		
		// Check if target is still alive and not paralyzed
		if(attackOk && !TARGET.isDead() && ATTACKER.getStat("ATTACKSPEED") > 0){


			int dX = TARGET.getX() - ATTACKER.getX();
			int dY = TARGET.getY() - ATTACKER.getY();

			double distToTarget = Math.sqrt(Math.pow(dX, 2)+Math.pow(dY,2));

			float angleNeeded = MathUtils.angleBetween(-dX, -dY);

			if(angleNeeded < 0){
				angleNeeded = 360 + angleNeeded;
			}

			// Check if target is close enough
			boolean nearTarget = checkTargetNear(ATTACKER, TARGET, angleNeeded, distToTarget);

			if (nearTarget) {

				// Generate attack info
				String attackDir = "none";

				attackDir = dX+":"+dY+":"+ATTACKER.getAttackSpeed();

				boolean useProjectile = false;

				if(ATTACKER.getWeapon() != null){
					if(ATTACKER.getWeapon().getProjectileId() > 0){
						useProjectile = true;
						attackDir += ";proj";	
					}
				}

				// Get damage, criticalOrMiss, attackType
				String damageInfo = DamageCalculator.calculateAttack(ATTACKER,TARGET);

				String attack_info[] = damageInfo.split(";");

				String criticalOrMiss = attack_info[0];
				int damage = Integer.parseInt(attack_info[1]);
				String hitType = ATTACKER.getAttackType();

				// Check if target is standning in arena, if target is, don't count as PK kill
				boolean arena = false;
				if(Server.WORLD_MAP.getTile(TARGET.getX(), TARGET.getY(), TARGET.getZ()) != null){
					if(Server.WORLD_MAP.getTile(TARGET.getX(), TARGET.getY(), TARGET.getZ()).getType().equals("arena")){
						arena = true;
					}
				}

				// Perform attack
				ATTACKER.startAttackTimer();

				// If attacker is a monster
				if(ATTACKER.getCreatureType() == CreatureType.Monster){
					// Reset move timer for monsters
					ATTACKER.restartMoveTimer();
					
					// If monster attacking monster, then check if player is nearby
					if(TARGET != null && TARGET.getCreatureType() == CreatureType.Monster){
						for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
							Client s = entry.getValue();

							if(s.Ready){
								if(s.playerCharacter.getZ() == ATTACKER.getZ()){
									double distToMob = Math.sqrt(Math.pow(ATTACKER.getX() - s.playerCharacter.getX(),2) + Math.pow(ATTACKER.getY() - s.playerCharacter.getY(),2));
									
									int AGGRO_RANGE = 8;
									if (distToMob <= AGGRO_RANGE) {
										ATTACKER.setAggro(s.playerCharacter);
									}
								}
							}
						}
					}
				}else{
					PlayerCharacter playerAttacker = (PlayerCharacter) ATTACKER;
					if(playerAttacker.getPkMarker() < 2){
						addOutGoingMessage(playerAttacker.client,"restart_logout",""+playerHitTime);
					}
				}

				MonsterHandler.alertNearMonsters(ATTACKER, TARGET.getX(), TARGET.getY(), TARGET.getZ(), false);
				
				// Check if TARGET dies of attack
				HitHandler.creatureGetHit(TARGET,ATTACKER,damage,hitType,criticalOrMiss,arena,null);
			
				// SEND INFO ABOUT ATTACK TO ALL PLAYERS IN SAME AREA
				String attackInfo = attackDir;
				
				if(ATTACKER.getCreatureType() == CreatureType.Player){
					PlayerCharacter playerAttacker = (PlayerCharacter) ATTACKER;

					// Gain Class XP
					if(damage > 0){
						int classId = 0;
						if(ATTACKER.getWeapon() != null){
							classId = ATTACKER.getWeapon().getClassId();
						}
						if(classId > 0){
							boolean training = false;
							if(TARGET.getFamilyId() == 8){
								training = true;
							}
							ClassHandler.gainBaseXP(playerAttacker.client, classId, training);
						}
					}
				}

				for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
					Client s = entry.getValue();

					if (s.Ready) {

						if(isVisibleForPlayer(s.playerCharacter,TARGET.getX(),TARGET.getY(),TARGET.getZ())){

							// SEND ATTACK INFO
							addOutGoingMessage(s,"attack",ATTACKER.getSmallData()+"/"+TARGET.getSmallData()+"/"+attackInfo);

							if(useProjectile){
								addOutGoingMessage(s,"projectile",ATTACKER.getWeapon().getProjectileId()+","+ATTACKER.getX()+","+ATTACKER.getY()+","+ATTACKER.getZ()+","+TARGET.getX()+","+TARGET.getY()+",0,1,0");
							}							
						}
					}
				}	
			}
		}else{
			// Drop aggro
			if(ATTACKER.getCreatureType() == CreatureType.Monster && (!attackOk || ATTACKER.isDead())){
				ATTACKER.setAggro(null);
			}
		}
	}

	public static void updateRangedCooldowns(){
		for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
			Client s = entry.getValue();
			if (s.Ready) {
				if(s.playerCharacter.getAttackRange() > 1){
					 s.playerCharacter.cooldownRangedAttack();
				}
			}
		}
	}
	
	public static void update() {
		for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
			Client s = entry.getValue();
			if (s.Ready) {
				if (s.playerCharacter.checkAttackTimer()) {
					if (s.playerCharacter.isAggro()) {
						if(s.playerCharacter.getAggroTarget() != null){
							boolean attackOk = true;
							
							if(s.playerCharacter.getAggroTarget().getCreatureType() == CreatureType.Player){
								PlayerCharacter playerTarget = (PlayerCharacter) s.playerCharacter.getAggroTarget();
								attackOk = PvpHandler.canAttackPlayer(s.playerCharacter, playerTarget);
							}
							
							boolean rangedAttackReady = true;
							
							// Ranged attacks needs cooldown after movement
							if(s.playerCharacter.getAttackRange() > 1 && !s.playerCharacter.hasStatusEffect(9)){
								 if(!s.playerCharacter.isRangedAttackReady()){
									 rangedAttackReady = false;
								 }
							}
							
							if(rangedAttackReady && attackOk){
								attack(s.playerCharacter,s.playerCharacter.getAggroTarget());
							}else if(!attackOk){
								s.playerCharacter.setAggro(null);
								addOutGoingMessage(s, "settarget", "None,0");
							}
						}	
					}
				}
			}
		}

		for(Iterator<Npc> iter = Server.WORLD_MAP.getMonsters().values().iterator();iter.hasNext();){  
			Npc monster = iter.next();
			if (monster.checkAttackTimer()) {
				monster.setUsedStashItem(false);

				if (monster.isAggro()) {
					attack(monster,monster.getAggroTarget());
				}

			}
		}
	}


	/****************************************
	 * * WIN BATTLE! * * *
	 ****************************************/

	public static void playerKillsMonster(Client client, Npc TARGET){

		if(client.playerCharacter.getParty() != null && client.playerCharacter.getParty().getNrMembers() > 1){
			// Client in Party

			float totalXPf = TARGET.getGiveXP();

			
			// Party XP boost
			int nrMembers = client.playerCharacter.getParty().getNrMembers();

			// XP boost cap
			if(nrMembers > 5){
				nrMembers = 5;
			}

			float xpBoost = 1.0f + (nrMembers*0.3f);
			totalXPf *= xpBoost;

			int totalLevel = 0;

			// Loop through members of party
			// Get sum of all levels in the party
			for(Client member: client.playerCharacter.getParty().getPlayers()){
				if(member.playerCharacter != null){
					totalLevel += member.playerCharacter.getLevel();
				}
			}

			// Give XP to party members
			for(Client member: client.playerCharacter.getParty().getPlayers()){
				if(member.Ready){
					// Party member must be within visible range
					if(isVisibleForPlayer(member.playerCharacter, TARGET.getX(),TARGET.getY(),TARGET.getZ())){

						// Calculate XP given depending on level of player;
						float givenXPf = totalXPf * ((float) member.playerCharacter.getLevel() / (float) totalLevel);

						addXP(member,Math.round(givenXPf));

						// Update kills and quests
						QuestHandler.updateKills(member, TARGET);
					}
				}
			}

		}else{
			// No Party, regular XP share based on damage

			// Calculate XP share
			HashMap<Creature, Integer> attackersXP = TARGET.getAttackersXP();
			
			// Loop through attackers and give XP
			for(Creature attacker: attackersXP.keySet()){
				if(attacker != null){
					// Check if killer is in the area to get XP share
					if(attacker.getCreatureType() == CreatureType.Player){
						PlayerCharacter playerAttacker = (PlayerCharacter) attacker;

						if(isVisibleForPlayer(playerAttacker, TARGET.getX(),TARGET.getY(),TARGET.getZ())){
							int gainedXP = attackersXP.get(playerAttacker);

							if(playerAttacker.client.Ready){
								addXP(playerAttacker.client,gainedXP);

								// Update kills and quests
								QuestHandler.updateKills(playerAttacker.client, TARGET);
							}
						}
					}
				}
			}
		}
	}

	public static void addXP(Client client, int gainedXP){
		if(client.playerCharacter != null){
			if (client.playerCharacter.addXP(gainedXP)) {
				String levelUpData = client.playerCharacter.levelUp();

				addOutGoingMessage(client, "level_up", levelUpData);
				
				System.out.println("SEND LEVEL UP INFO!");
				// Send level up info to website
				// DO NOT CODE BELOW
				String characterInfo = ServerSettings.SERVER_ID+","
									+client.playerCharacter.blueSagaId+","
									+client.UserId+","
									+client.playerCharacter.getDBId()+","
									+client.playerCharacter.getName()+","
									+client.playerCharacter.getCreatureId()+","
									+client.playerCharacter.getLevel()+","
									+client.playerCharacter.getHeadId()+","
									+client.playerCharacter.getWeaponId()+","
									+client.playerCharacter.getOffHandId()+","
									+client.playerCharacter.getAmuletId()+","
									+client.playerCharacter.getArtifactId()+","
									+client.playerCharacter.getBounty();
				
				CharacterInfo.SendCharacterInfoToWebsite(characterInfo);
				// DO NOT CHANGE THE CODE ABOVE
				
				
				String playerData = client.playerCharacter.getSmallData();

				for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
					Client other = entry.getValue();

					if(other.Ready && isVisibleForPlayer(other.playerCharacter,client.playerCharacter.getX(),client.playerCharacter.getY(),client.playerCharacter.getZ())){
						addOutGoingMessage(other,"other_level_up",playerData);
					}
				}
			}else{
				if(client.playerCharacter.getLevel() < ServerSettings.LEVEL_CAP){
					addOutGoingMessage(client, "gain_xp",gainedXP+"");
				}
			}

			client.playerCharacter.saveInfo();
		}
	}


	/**
	 * Handles player death
	 * @param client
	 */
	public static void playerDeath(Client client, boolean pkAttack) {
		Creature TARGET = client.playerCharacter;
		
		if(!Server.WORLD_MAP.getTile(TARGET.getX(), TARGET.getY(), TARGET.getZ()).getType().equals("arena")){
			
			// If not a pvp kill
			if(!pkAttack){
				// LOSE XP IN PERCENT AND MAYBE LEVEL
				int totalXP = 0;
				if(TARGET.getLevel() > 1){
					totalXP = XPTables.totalLevelXP.get(TARGET.getLevel());
				}
	
				totalXP += TARGET.getXP();
	
				int lostXP = (int) Math.ceil(totalXP * 0.02f);
	
				if(client.playerCharacter.loseXP(lostXP, client)){
					// Player loses a level
				}
	
				int retrieveXP = Math.round(lostXP * 0.5f);
			
				// REMOVE OLD SOUL
				ResultSet soulInfo = Server.userDB.askDB("select X,Y,Z from character_soul where CharacterId = "+client.playerCharacter.getDBId());
				try {
					if(soulInfo.next()){
						int soulX = soulInfo.getInt("X");
						int soulY = soulInfo.getInt("Y");
						int soulZ = soulInfo.getInt("Z");

						Server.WORLD_MAP.getTile(soulX, soulY, soulZ).setSoulCharacterId(0);

						Server.userDB.updateDB("delete from character_soul where CharacterId = "+client.playerCharacter.getDBId());

						if(client.Ready){
							addOutGoingMessage(client,"removesoul",soulX+","+soulY+","+soulZ);
						}
					}
					soulInfo.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}

				// CREATE NEW SOUL ON GROUND
				Server.userDB.updateDB("insert into character_soul (CharacterId, X, Y, Z, XP) values ("+client.playerCharacter.getDBId()+","+client.playerCharacter.getX()+","+client.playerCharacter.getY()+","+client.playerCharacter.getZ()+","+retrieveXP+")");
				Server.WORLD_MAP.getTile(client.playerCharacter.getX(), client.playerCharacter.getY(), client.playerCharacter.getZ()).setSoulCharacterId(client.playerCharacter.getDBId());

				if(client.Ready){
					addOutGoingMessage(client,"soul",client.playerCharacter.getX()+","+client.playerCharacter.getY()+","+client.playerCharacter.getZ());
				}
			}
			
			// REMOVE PK STATUS
			if(client.playerCharacter.getPkMarker() > 0){
				PvpHandler.setPlayerKillerMark(client.playerCharacter,0);
			}

			// CHECK IF PLAYER CAN WEAR EQUIP, OTHERWISE DROP IT ON GROUND
			ItemHandler.loseLootUponDeath(client);
		}

		client.playerCharacter.saveInfo();

		// IF CLIENT CRASHED, RESPAWN CLIENT
		if(!client.Ready){
			client.playerCharacter.revive();
			respawnPlayer(client.playerCharacter);
		}
	}

	
	public static boolean checkTargetNear(Creature ATTACKER, Creature TARGET, float angleNeeded, double distToTarget){
		// CALCULATE DISTANCE BETWEEN ATTACKER AND TARGET
		boolean nearTarget = false;

		// CHECK IF TARGET IS CLOSE ENOUGH TO ATTACK
		if(Math.floor(distToTarget) <= ATTACKER.getAttackRange()){
			nearTarget = true;
		}

		// Change angle of attacker to face target
		ATTACKER.setGotoRotation(angleNeeded);


		// MONSTER SPECIFIC CHECKS
		if(ATTACKER.getCreatureType() == CreatureType.Monster){
			if(nearTarget){
				if(ATTACKER.getAttackRange() == 1){
					// If melee then check if angle is ok
					if(Math.abs(angleNeeded - ATTACKER.getRotation()) > 10){
						nearTarget = false;
					}
				}
			}
			// MONSTER USE ABILITY
			if(MonsterHandler.monsterUseRandomAbility(ATTACKER, TARGET)){
				nearTarget = false;
			}

		}

		return nearTarget;
	}

	public static boolean checkAttackOk(int attackerX, int attackerY, int attackerZ, int targetX, int targetY, int targetZ){
		if(Server.WORLD_MAP.getTile(attackerX, attackerY, attackerZ) == null){
			return false;
		}

		boolean attackOk = true;

		if(attackerZ != targetZ){
			attackOk = false;
		}

		// CHECK ARENA ATTACKS
		if(Server.WORLD_MAP.getTile(attackerX, attackerY, attackerZ).getType().equals("arena") 
				&& !Server.WORLD_MAP.getTile(targetX, targetY, targetZ).getType().equals("arena")){
			attackOk = false;
		}

		if(!Server.WORLD_MAP.getTile(attackerX, attackerY, attackerZ).getType().equals("arena") 
				&& Server.WORLD_MAP.getTile(targetX, targetY, targetZ).getType().equals("arena")){
			attackOk = false;
		}

		// CHECK WATER ATTACKS
		boolean attackerWater = false;
		if(Server.WORLD_MAP.getTile(attackerX, attackerY, attackerZ).isWater()){
			attackerWater = true;
		}

		boolean targetWater = false;
		if(Server.WORLD_MAP.getTile(targetX, targetY, targetZ).isWater()){
			targetWater = true;
		}

		if(attackerWater && !targetWater){
			attackOk = false;
		}

		if(!attackerWater && targetWater){
			attackOk = false;
		}

		// NO ATTACKS INDOORS
		if(Server.WORLD_MAP.getTile(attackerX, attackerY, attackerZ).getType().equals("indoors")){
			attackOk = false;
		}
		return attackOk;
	}

	public static String respawnPlayer(PlayerCharacter playerCharacter){

		int checkpointId = 0;
		int playerX = 0;
		int playerY = 0;
		int playerZ = 0;

		String playerpos = "";
		
		Server.WORLD_MAP.removePlayerFromZ(playerCharacter, playerCharacter.getZ());
		
		ResultSet rs = Server.userDB.askDB("select CheckpointId from user_character where Id = "+playerCharacter.getDBId());

		try {
			while(rs.next()){
				checkpointId = rs.getInt("CheckpointId");
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}


		rs = Server.mapDB.askDB("select X,Y, Z from checkpoint where Id = "+checkpointId);

		try {
			while(rs.next()){
				playerX = rs.getInt("X");
				playerY = rs.getInt("Y");
				playerZ = rs.getInt("Z");
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		playerCharacter.walkTo(playerX, playerY, playerZ);
		
		Server.WORLD_MAP.addPlayerToZ(playerCharacter, playerCharacter.getZ());
		
		Server.userDB.updateDB("update user_character set X = "+playerX+", Y = "+playerY+", Z = "+playerZ+", AreaEffectId = 0 where Id = "+playerCharacter.getDBId());
		
		playerpos = playerX+";"+playerY+";"+playerZ;

		return playerpos;
	}
}