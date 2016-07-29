package data_handlers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import utils.ServerGameInfo;
import creature.Creature;
import creature.Creature.CreatureType;
import data_handlers.ability_handler.Ability;
import data_handlers.ability_handler.AbilityHandler;
import data_handlers.battle_handler.HitHandler;
import map.Trap;
import network.Client;
import network.Server;

public class TrapHandler extends Handler {
	
	
	private static HashMap<Integer,Trap> Traps;
	
	public static void init(){
		Traps = new HashMap<Integer,Trap>();
	}
	
	
	public static void update(){
		Vector<Trap> updatedTraps = new Vector<Trap>();
		
		// TRIGGER TRAPS THAT ARE ACTIVE
		for(Iterator<Trap> iter = Traps.values().iterator();iter.hasNext();){  
			Trap T = iter.next();
			
			if(T.isActive()){
				if(T.isReady()){
					T.trigger();
					updatedTraps.add(T);
				}
				
				// CHECK IF SOMEONE IS STANDING ON THE SPIKES
				if(T.isOn() && T.getDamage() > 0){
					trapDamage(T);
				}
			}
		}
		
		for(Iterator<Trap> iter = updatedTraps.iterator();iter.hasNext();){  
			Trap T = iter.next();
			
			String TrapName = T.getName();
			if(T.isOn()){
				TrapName += "_on";
			}else{
				TrapName += "_off";
			}
			
			Server.WORLD_MAP.getTile(T.getX(), T.getY(), T.getZ()).setObjectId(TrapName);
			
			
			// SEND INFO ABOUT TRIGGERED TRAPS TO CLIENTS
			for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
				Client s = entry.getValue();
				if(s.Ready){
					if(isVisibleForPlayer(s.playerCharacter,T.getX(),T.getY(),T.getZ())){
						addOutGoingMessage(s,"trap_trigger",T.getX()+","+T.getY()+","+T.getZ()+","+TrapName);
					}
				}
			}
		}
		
	}
	
	public static void addTrap(Trap newTrap){
		Traps.put(newTrap.getId(), newTrap);
	}
	
	public static Trap getTrap(int trapId){
		return Traps.get(trapId);
	}
	
	public static void triggerTrap(int TrapId, int triggerX, int triggerY, int triggerZ){
		Trap T = Traps.get(TrapId);
		
		if(T != null){
			T.trigger();
			if(T.getDamage() > 0){
				trapDamage(T);
			}
			if(T.getAbilityId() > 0){
				Ability ABILITY = new Ability(ServerGameInfo.abilityDef.get(T.getAbilityId()));
				
				ABILITY.setCaster(null);
				
				if(!ABILITY.isInstant()){
					// Add projectile
					AbilityHandler.addProjectile(ABILITY, T.getX(), T.getY(), T.getZ(), triggerX, triggerY, triggerZ);
				}else{
					// Instant effect
					AbilityHandler.abilityEffect(ABILITY, triggerX, triggerY, triggerZ);
				}
				
			}
		}
	}
	
	public static void trapDamage(Trap T){
		Creature TARGET = Server.WORLD_MAP.getTile(T.getX(), T.getY(), T.getZ()).getOccupant();
		if(TARGET != null){
			
			// GIVE OCCUPANT DAMAGE
			//float percentDamage = (float) TARGET.getStat("MAX_HEALTH") * ((float) T.getDamage() / 100.0f);
			float percentDamage = T.getDamage();
			
			HitHandler.creatureGetHit(TARGET, null, Math.round(percentDamage), T.getDamageType(),"false",false,null);
			
			if(TARGET.isDead()){
				Server.WORLD_MAP.getTile(TARGET.getX(), TARGET.getY(), TARGET.getZ()).setOccupant(CreatureType.None, null);
			}	
		}
	}
}