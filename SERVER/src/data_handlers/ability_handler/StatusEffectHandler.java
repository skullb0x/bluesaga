package data_handlers.ability_handler;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import creature.Creature;
import creature.Npc;
import creature.PlayerCharacter;
import creature.Creature.CreatureType;
import map.Tile;
import network.Client;
import network.Server;
import data_handlers.Handler;
import data_handlers.battle_handler.HitHandler;

public class StatusEffectHandler extends Handler {
	
	private static int statusEffectItr = 0;
	
	/**
	 * Add statuseffect to creature
	 * @param target
	 * @param se
	 */
	public static void addStatusEffect(Creature target, StatusEffect se){

		target.addStatusEffect(se);
		String statusEffectsInfo = se.getId()+","+se.getGraphicsNr()+","+se.getDuration()+","+se.getName()+","+se.getColor().getRed()+","+se.getColor().getGreen()+","+se.getColor().getBlue()+","+se.getAnimationId()+","+se.getSfx()+";";

		PlayerCharacter targetPlayer = null;
		if(target.getCreatureType() == CreatureType.Player){
			targetPlayer = (PlayerCharacter) target;
		}

		
		for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
			Client s = entry.getValue();
			if(s.Ready){
				if(targetPlayer != null){
					if(s.playerCharacter.getDBId() == targetPlayer.getDBId()){
						addOutGoingMessage(s,"update_bonusstats",s.playerCharacter.getBonusStatsAsString());
					}
				}
				if(isVisibleForPlayer(s.playerCharacter , target.getX(), target.getY(), target.getZ())){
					addOutGoingMessage(s,"statuseffect_add",target.getSmallData()+"/"+statusEffectsInfo);
				}
			}
		}
	}

	private static int calculateSEdamage(StatusEffect se, boolean playerTarget){

		int SEskillLvl = 1;
		boolean pvpAttack = false;

		if(se.getCaster() != null){
			if(se.getCaster().getCreatureType() == CreatureType.Player){
				PlayerCharacter playerCaster = (PlayerCharacter) se.getCaster();
				if(playerTarget){
					pvpAttack = true;
				}
				if(se.getAbility() != null){
					SEskillLvl = playerCaster.getClassById(se.getAbility().getClassId()).level;
				}
			}else{
				SEskillLvl = se.getCaster().getLevel();
			}
		}
		
		float SEdamage = se.getRepeatDamage() + (SEskillLvl / 2.0f);

		if(pvpAttack){
			SEdamage /= 2;
		}

		return Math.round(SEdamage);
	}
	
	
	/**
	 * Update players and npcs status effects
	 */
	public static void updateStatusEffects(){
		
		statusEffectItr++;
		if(statusEffectItr >= 1000){
			statusEffectItr = 0;
		}
		
		// Update Players status effects
		for (Map.Entry<Integer,Client> entry : Server.clients.entrySet()) {
			Client s = entry.getValue();

			if(s.Ready){
				if(s.playerCharacter != null){
					if(!s.playerCharacter.isDead()){
						for(Iterator<StatusEffect> iter2 = s.playerCharacter.getStatusEffects().values().iterator();iter2.hasNext();){  
							StatusEffect SE = iter2.next();

							if(SE.isActive()){
								if(SE.getRepeatDamage() > 0 && statusEffectItr % 2 == 0){
									
									int damage = calculateSEdamage(SE,true);

									int tileX = s.playerCharacter.getX();
									int tileY = s.playerCharacter.getY();
									int tileZ = s.playerCharacter.getZ();

									boolean arena = false;

									if(Server.WORLD_MAP.getTile(tileX, tileY, tileZ).getType().equals("arena")){
										arena = true;
									}

									HitHandler.creatureGetHit(s.playerCharacter, SE.getCaster(), damage, SE.getRepeatDamageType(),"false",arena,null);
								}

							}else{
								// REMOVE STATUS EFFECT
								iter2.remove();
								s.playerCharacter.updateBonusStats();

								for (Map.Entry<Integer, Client> entry3 : Server.clients.entrySet()) {
									Client other = entry3.getValue();
									if(other.Ready && isVisibleForPlayer(other.playerCharacter, s.playerCharacter.getX(), s.playerCharacter.getY(),s.playerCharacter.getZ())){
										addOutGoingMessage(other, "statuseffect_remove", s.playerCharacter.getSmallData()+";"+SE.getId());
										if(other.playerCharacter.getDBId() == s.playerCharacter.getDBId()){
											addOutGoingMessage(other,"update_bonusstats",other.playerCharacter.getBonusStatsAsString());
										}
									}
								}
							}
						}
					}
				}
			}
		}
	
		// Update Npcs status effects
		for(Iterator<Npc> iter2 = Server.WORLD_MAP.getMonsters().values().iterator();iter2.hasNext();){  
			Npc m = iter2.next();

		
			for(Iterator<StatusEffect> iter3 = m.getStatusEffects().values().iterator();iter3.hasNext();){  
				StatusEffect SE = iter3.next();

				if(SE.isActive() && !m.isDead()){
					if(SE.getRepeatDamage() > 0){
						int damage = calculateSEdamage(SE, false);

						HitHandler.creatureGetHit(m, SE.getCaster(), damage, SE.getRepeatDamageType(),"false", false, null);
						
						if(m.isDead()){
							int tileX = m.getX();
							int tileY = m.getY();
							int tileZ = m.getZ();

							Server.WORLD_MAP.getTile(tileX, tileY, tileZ).setOccupant(CreatureType.None, null);

							m.startRespawnTimer();
						}
					}
				}else{
					iter3.remove();
					m.updateBonusStats();

					for (Entry<Integer, Client> entry : Server.clients.entrySet()) {
						Client other = entry.getValue();
						if(other.Ready && isVisibleForPlayer(other.playerCharacter,m.getX(),m.getY(),m.getZ())){
							addOutGoingMessage(other, "statuseffect_remove", m.getSmallData()+";"+SE.getId());
						}
					}
				}
			}
		}
	}

	/**
	 * Update status effects on all tiles
	 */
	public static void updateTileStatusEffects(){
		
		for(Tile t: Server.WORLD_MAP.getMapTiles().values()){
			String tileInfo = t.updateStatusEffect(); 

			if(!tileInfo.equals("")){
				for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
					Client s = entry.getValue();

					if(s.Ready && isVisibleForPlayer(s.playerCharacter,t.getX(),t.getY(),t.getZ())){
						addOutGoingMessage(s,"update_tiles",t.getX()+","+t.getY()+","+t.getZ()+";"+tileInfo);
					}
				}
			}
		}
	}
}
