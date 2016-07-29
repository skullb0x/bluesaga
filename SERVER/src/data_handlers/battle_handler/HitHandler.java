package data_handlers.battle_handler;

import game.ServerSettings;
import network.Client;
import network.Server;

import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import utils.RandomUtils;
import utils.ServerGameInfo;
import creature.Creature;
import creature.Npc;
import creature.PlayerCharacter;
import creature.Creature.CreatureType;
import data_handlers.Handler;
import data_handlers.ability_handler.StatusEffect;
import data_handlers.ability_handler.StatusEffectHandler;
import data_handlers.monster_handler.MonsterHandler;

public class HitHandler extends Handler {

	/**
	 * Handles all events connected to a creature getting hit, including:
	 * - counters
	 * - remove statuseffects
	 * - add statuseffects
	 * - death check
	 * - if target monster, give xp
	 * - handles pvp
	 * - set monster aggro/change target
	 * @param TARGET
	 * @param ATTACKER
	 * @param damage
	 * @param damageType
	 * @param arena
	 * @return
	 */
	
	public static void creatureGetHit(Creature TARGET, Creature ATTACKER, int damage, String damageType, String criticalOrMiss, boolean arena, Vector<StatusEffect> statusEffects){
		// Check that target isn't dead
		if(!TARGET.isDead()){
		
			boolean targetDies = false;
			boolean immune = false;
			
			// Update old statuseffects when hit
			hitStatusEffectsCheck(TARGET, ATTACKER);
			
			// IF TARGET is Player
			if(TARGET.getCreatureType() == CreatureType.Player){
				PlayerCharacter playerTarget = (PlayerCharacter) TARGET;
	
				// Reset logout timer
				if(damage > 0 && !arena){
					if(playerTarget.getPkMarker() < 2){
						addOutGoingMessage(playerTarget.client,"restart_logout",""+BattleHandler.playerHitTime);
					}
				}
			}
			
			// If TARGET is a Monster
			if(TARGET.getCreatureType() == CreatureType.Monster){
				Npc targetMonster = (Npc) TARGET;
				
				// If guardian and not aggro, do not take damage
				if(targetMonster.getOriginalAggroType() == 5){
					if(!targetMonster.isAggro()){
						damage = 0;
					}
				}
				
				// If npc, do not take damage
				if(targetMonster.getOriginalAggroType() == 3){
					immune = true;
					damage = 0;
					damageType = "None";
				}
				
				// Set aggro if monster
				if(ATTACKER != null){
					// Add monster damage to split xp amongst attackers
					targetMonster.addAttackerDamage(ATTACKER, damage);
					
					if(ATTACKER.getCreatureType() == CreatureType.Monster && ATTACKER.getDBId() == targetMonster.getDBId()){
						// Can't set itself as target
					}else{
						if(damage == targetMonster.getMostHitDamage()){
							if(targetMonster.getOriginalAggroType() != 5 && targetMonster.getOriginalAggroType() != 3){
								targetMonster.setAggro(ATTACKER);
							}
						}else{
							// 5% CHANCE OF CHANGING TARGET
							int changeChance = RandomUtils.getInt(0,100);
							if(changeChance < 5){
								// CHANGE TARGET
								if(targetMonster.getOriginalAggroType() != 5){
									targetMonster.setAggro(ATTACKER);
								}
							}
						}
					}
				}
			}
			
				
			/*
			// Casters are immune to their own spells
			if(ATTACKER != null && TARGET != null){
				if(ATTACKER.getCreatureType().equals(TARGET.getCreatureType())
					&& ATTACKER.getDBId() == TARGET.getDBId()){
					immune = true;
					damageType = "None";
					damage = 0;
				}
			}
			*/
			
			// Check if PVP is ok
			boolean pvpOk = ServerSettings.PVP;
						
			// If TARGET is a Player
			if(TARGET.getCreatureType() == CreatureType.Player){
				PlayerCharacter playerTarget = (PlayerCharacter) TARGET;
			
				
				if(ATTACKER != null && !arena){
					if(ATTACKER.getCreatureType() == CreatureType.Player){
						PlayerCharacter playerAttacker = (PlayerCharacter) ATTACKER;
						
						if(!PvpHandler.canAttackPlayer(playerAttacker, playerTarget)){
							immune = true;
						}
					}
				}
				
				// Admins and training dummies are immune
				if(playerTarget.getAdminLevel() >= 3){
					immune = true;
				}
			}
			
			if(TARGET.getFamilyId() == 8){
				immune = true;
			}
			
			
			
			
			// Target takes damage, returns if it dies or not
			if(!immune){
				targetDies = TARGET.hitByAttack(damage);
			}
			
			if(targetDies){
				targetDies = true;
				targetDeath(TARGET,ATTACKER, arena);
			}else {
				boolean removeManaShield = false;
				
				if(!immune){
					// Remove Mana Shield if 0 in mana
					if(TARGET.hasManaShield() && TARGET.getMana() <= 0){
						TARGET.getStatusEffect(25).setActive(false);
						removeManaShield = true;
					}
				}
				
				// Add attack statuseffects
				if(statusEffects != null){
					for(StatusEffect statusFX: statusEffects){
						StatusEffect seFX = new StatusEffect(statusFX.getId());
						seFX.setCaster(ATTACKER);
						StatusEffectHandler.addStatusEffect(TARGET, seFX);
					}
				}
				
				// Add statuseffects from weapon
				if(ATTACKER != null){
					if(ATTACKER.getWeapon() != null){
						for(StatusEffect statusFX: ATTACKER.getWeapon().getStatusEffects()){
							int weaponSEchance = RandomUtils.getInt(0, 3);
							if(weaponSEchance == 0){
								StatusEffect weaponSE = new StatusEffect(statusFX.getId());
								weaponSE.setCaster(ATTACKER);
								StatusEffectHandler.addStatusEffect(TARGET, weaponSE);
							}
						}
					}
				}
				
				for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
					Client other = entry.getValue();
	
					if(other.Ready && Handler.isVisibleForPlayer(other.playerCharacter, TARGET.getX(), TARGET.getY(),TARGET.getZ())){
						// Send health status
						addOutGoingMessage(other, "changehealthstatus",TARGET.getSmallData()+";"+TARGET.getHealthStatus());
						
						if(removeManaShield){
							addOutGoingMessage(other, "statuseffect_remove", TARGET.getSmallData()+";"+25);
						}
						if(TARGET.getCreatureType() == CreatureType.Player && other.playerCharacter.getDBId() == TARGET.getDBId()){
							addOutGoingMessage(other,"update_bonusstats",TARGET.getBonusStatsAsString());
						}
					}
				}
			}
	
			// Send damage to clients
			if(!(damageType.equals("None") && damage == 0)){
				sendHitToClients(TARGET, ATTACKER, damage, damageType, criticalOrMiss, targetDies);
			
				// Handle monster/player death and pvp marker
				if(ATTACKER != null && !arena){
					if(ATTACKER.getCreatureType() == CreatureType.Player){
						PlayerCharacter playerAttacker = (PlayerCharacter) ATTACKER;
		
						if(targetDies){
							if(TARGET.getCreatureType() == CreatureType.Monster){
								// PVE, get XP
								BattleHandler.playerKillsMonster(playerAttacker.client,(Npc) TARGET);
							}else if(TARGET.getCreatureType() == CreatureType.Player){
								if(playerAttacker.getAdminLevel() < 3 && pvpOk){
									// PVP
									PvpHandler.setPlayerKillerMark(playerAttacker,2);
								}
							}
						}else {
							if(TARGET.getCreatureType() == CreatureType.Player){
								if(playerAttacker.getAdminLevel() < 3 && ATTACKER.getDBId() != TARGET.getDBId() && pvpOk){
									// PVP
									if(playerAttacker.getPkMarker() <= 1){
										PvpHandler.setPlayerKillerMark(playerAttacker,1);
									}
								}
							}
						}
					}
				}
			}
		}
	}

	public static void sendHitToClients(Creature TARGET, Creature ATTACKER, int damage, String damageType, String criticalOrMiss, boolean targetDies){
		if(damage < 0){
			// If heal, then show positive number
			damageType = "Healing";
		}
		
		for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
			Client s = entry.getValue();
	 
			if (s.Ready) {
				if(isVisibleForPlayer(s.playerCharacter,TARGET.getX(),TARGET.getY(),TARGET.getZ())){
					addOutGoingMessage(s,"creature_hit",TARGET.getSmallData()+";"+damage+","+damageType+","+criticalOrMiss);
				}
				
				// Send updated health to target player
				if(TARGET.getCreatureType() == CreatureType.Player && s.playerCharacter.getDBId() == TARGET.getDBId()){
					addOutGoingMessage(s, "update_health",""+TARGET.getHealth());
				}
				
				if(targetDies){
					if(isVisibleForPlayer(s.playerCharacter,TARGET.getX(),TARGET.getY(),TARGET.getZ())){
						addOutGoingMessage(s,"death",TARGET.getSmallData());
						
						// PLAYERS HAVING AGGRO ON TARGET LOSES AGGRO
						if(s.playerCharacter.getAggroTarget() != null){
							if(s.playerCharacter.getAggroTarget().getCreatureType().equals(TARGET.getCreatureType()) && s.playerCharacter.getAggroTarget().getDBId() == TARGET.getDBId()){
								s.playerCharacter.setAggro(null);
								addOutGoingMessage(s, "settarget", "None,0");
							}	
						}
							
						if(TARGET.getCreatureType() == CreatureType.Player){
							if(TARGET.getDBId() != s.playerCharacter.getDBId()){
								// SEND REMOVE PLAYER TO OTHERS
								addOutGoingMessage(s,"creature_remove",TARGET.getSmallData());
							}
						}
					}
				}
			}
		}
	}
	
	
	/**
	 * Update status of statuseffects when hit
	 * @param TARGET
	 * @param ATTACKER
	 */
	public static void hitStatusEffectsCheck(Creature TARGET, Creature ATTACKER){
		// If target is sleeping
		MonsterHandler.changeMonsterSleepState(TARGET, false);
		
		// If Invisible, remove Invisible
		if(TARGET.hasStatusEffect(13)){
			TARGET.removeStatusEffect(13);

			for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
				Client other = entry.getValue();

				if(other.Ready && isVisibleForPlayer(other.playerCharacter, TARGET.getX(), TARGET.getY(), TARGET.getZ())){
					addOutGoingMessage(other, "statuseffect_remove", TARGET.getSmallData()+";13");
					if(TARGET.getCreatureType() == CreatureType.Player && other.playerCharacter.getDBId() == TARGET.getDBId()){
						addOutGoingMessage(other,"update_bonusstats",other.playerCharacter.getBonusStatsAsString());
					}
				}
			}
		}

		// Counter damage
		if(ATTACKER != null){
			// Calculate distance between Target and Attacker
			int dX = TARGET.getX() - ATTACKER.getX();
			int dY = TARGET.getY() - ATTACKER.getY();
			
			double distToTarget = Math.sqrt(Math.pow(dX, 2)+Math.pow(dY,2));
			
			// Counter damage distance is 1 and has counter status effect
			if(Math.floor(distToTarget) == 1){
				if(TARGET.hasStatusEffect(33)){
					// Flame shield
					StatusEffect SE = new StatusEffect(1);
					SE.setAbility(ServerGameInfo.abilityDef.get(57));
					SE.setCaster(TARGET);
					StatusEffectHandler.addStatusEffect(ATTACKER,SE);
				}else if(TARGET.hasStatusEffect(34)){
					StatusEffect SE = new StatusEffect(6);
					SE.setAbility(ServerGameInfo.abilityDef.get(58));
					SE.setCaster(TARGET);
					StatusEffectHandler.addStatusEffect(ATTACKER,SE);
				}
			}	
		}
	}
	
	/**
	 * Handles events upon creature death such as:
	 * - kills monster
	 * - make occupied tile free
	 * - send data about death to clients
	 * - if monster, drop loot
	 * -  
	 * @param TARGET
	 * @param ATTACKER
	 * @param arena
	 */
	
	public static void targetDeath(Creature TARGET, Creature ATTACKER, boolean arena){
		
		// Remove aggro from attacker
		if(ATTACKER != null){
			if(ATTACKER.getAggroTarget() != null){
				if(ATTACKER.getAggroTarget().getCreatureType().equals(TARGET.getCreatureType()) 
						&& ATTACKER.getAggroTarget().getDBId() == TARGET.getDBId()){
					ATTACKER.setAggro(null);
				}
			}
		}

		// REMOVE TARGET FROM MAP
		for(int i = TARGET.getX() - 3; i < TARGET.getX() + 3; i++){
			for(int j = TARGET.getY() - 3; j < TARGET.getY() + 3; j++){
				if(Server.WORLD_MAP.getTile(i,j, TARGET.getZ()) != null){
					if(Server.WORLD_MAP.getTile(i,j, TARGET.getZ()).getOccupantType().equals(TARGET.getCreatureType()) && Server.WORLD_MAP.getTile(i,j, TARGET.getZ()).getOccupant().getDBId() == TARGET.getDBId()){
						Server.WORLD_MAP.getTile(i,j, TARGET.getZ()).setOccupant(CreatureType.None, null);
					}
				}
			}	
		}

		// REMOVE AGGRO TARGET FROM TARGET
		TARGET.setAggro(null);
		

		if(TARGET.getCreatureType() == CreatureType.Player){
			// IF TARGET IS PLAYER
			PlayerCharacter playerTarget = (PlayerCharacter) TARGET;

			boolean pkAttack = false;
			
			if(ATTACKER != null){
				if(ATTACKER.getCreatureType() == CreatureType.Player){
					pkAttack = true;
					PlayerCharacter playerAttacker = (PlayerCharacter) ATTACKER;
					PvpHandler.playerKillsPlayer(playerAttacker, playerTarget);
				}
			}
			
			// PLAYER LOSE XP AND LOOT
			BattleHandler.playerDeath(playerTarget.client,pkAttack);
			
		}else if(TARGET.getCreatureType() == CreatureType.Monster){
			// IF TARGET IS A MONSTER 
			Npc npcTarget = Server.WORLD_MAP.getMonster(TARGET.getDBId());

			// START RESPAWN
			npcTarget.startRespawnTimer();

			// DROP LOOT
			MonsterHandler.monsterDropLoot(npcTarget);
		}

		// MONSTERS HAVING AGGRO ON TARGET LOSES AGGRO
		for(Iterator<Npc> iter = Server.WORLD_MAP.getMonsters().values().iterator();iter.hasNext();){  
			Npc m = iter.next();
			if (m.isAggro()) {
				if (m.getAggroTarget().getCreatureType().equals(TARGET.getCreatureType()) && m.getAggroTarget().getDBId() ==  TARGET.getDBId()) {
					m.turnAggroOff();
				}
			}
		}
	}
}
