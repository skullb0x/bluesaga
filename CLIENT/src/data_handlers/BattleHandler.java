package data_handlers;

import game.BlueSaga;
import graphics.BlueSagaColors;
import gui.Gui;
import screens.ScreenHandler;
import screens.ScreenHandler.ScreenType;

import org.newdawn.slick.Color;

import sound.Sfx;
import utils.MathUtils;
import creature.Creature;
import creature.PlayerCharacter;
import creature.Creature.CreatureType;


public class BattleHandler extends Handler {
	
	public BattleHandler(){
		super();
	}
	
	public void load(){
	}
	
	public static void handleData(String serverData){

		if(serverData.startsWith("<otherrest>")){
			String creatureRestInfo[] = serverData.substring(11).split(";");
			Creature c = MapHandler.addCreatureToScreen(creatureRestInfo[0]);
			c.setResting(Boolean.parseBoolean(creatureRestInfo[1]));
		}
		
		if(serverData.startsWith("<attack>")){
			showAttack(serverData.substring(8));
		}else if(serverData.startsWith("<death>")){
			showDeath(serverData.substring(7));
		}
		
		if(serverData.startsWith("<set_xp>")){
			int XP = Integer.parseInt(serverData.substring(8));
			BlueSaga.playerCharacter.setXP(XP);
		}
		
		if(serverData.startsWith("<restart_logout>")){
			int logoutTime = Integer.parseInt(serverData.substring(16));
			BlueSaga.restartLogoutTimer(logoutTime);
		}
		
		if(serverData.startsWith("<gain_xp>")){
			String xpInfo = serverData.substring(9);
			int gainedXP = Integer.parseInt(xpInfo);
			BlueSaga.playerCharacter.addXP(gainedXP);
			Sfx.play("notifications/gain_xp");
			Gui.addMessage("You gained "+gainedXP+" XP",BlueSagaColors.GREEN);
			
		}else if(serverData.startsWith("<level_up>")){
			String statsInfo = serverData.substring(10);
			
			Sfx.play("notifications/level_up");
			
			BlueSaga.playerCharacter.levelChange(statsInfo);
		}else if(serverData.startsWith("<level_down>")){
			String statsInfo = serverData.substring(12);
			
			Sfx.play("notifications/level_up");
			
			BlueSaga.playerCharacter.levelChange(statsInfo);
		}
		
		if(serverData.startsWith("<add_damage_label>")){
			String labelInfo[] = serverData.substring(18).split(";");
			String creatureInfo = labelInfo[0];
			String text = labelInfo[1];
			String color = labelInfo[2];
			
			Creature creature = MapHandler.addCreatureToScreen(creatureInfo);
			
			String colorInfo[] = color.split(",");
			Color labelColor = new Color(Integer.parseInt(colorInfo[0]),Integer.parseInt(colorInfo[1]),Integer.parseInt(colorInfo[2]));
			
			creature.addDamageLabel(text, labelColor,0);
		}
	
		if(serverData.startsWith("<changehealthstatus>")){
			String changeInfo[] = serverData.substring(20).split(";");
			Creature TARGET = MapHandler.addCreatureToScreen(changeInfo[0]);
			
			int healthStatus = Integer.parseInt(changeInfo[1]);
			TARGET.setHealthStatus(healthStatus);
		}
		
		if(serverData.startsWith("<update_health>")){
			String healthInfo = serverData.substring(15);
			int health = Integer.parseInt(healthInfo);
			BlueSaga.playerCharacter.setHealth(health);
		}
		
		if(serverData.startsWith("<creature_hit>")){
			String creatureHitInfo[] = serverData.substring(14).split(";");
			Creature TARGET = MapHandler.addCreatureToScreen(creatureHitInfo[0]);
			
			String hitInfo[] = creatureHitInfo[1].split(",");
			int damage = Integer.parseInt(hitInfo[0]);
			String damageType = hitInfo[1];
			String criticalOrMiss = hitInfo[2];
			
			Color dmgColor = new Color(255,255,255);
			
			if(damageType.equals("Healing")){
				// If healing, show amount in green
				dmgColor = BlueSagaColors.GREEN;
			}else{
				// If you are the target, then show damage in red, else white
				if(TARGET.getCreatureType() == CreatureType.Player && BlueSaga.playerCharacter.getDBId() == TARGET.getDBId()){
					dmgColor = BlueSagaColors.RED;
				}
			}
			TARGET.hitByAttack(damage, criticalOrMiss, damageType, dmgColor);
		}
		
		if(serverData.startsWith("<settarget>")){
			String targetInfo[] = serverData.substring(11).split(",");
			CreatureType newTargetType = CreatureType.valueOf(targetInfo[0]);
			int newTargetId = Integer.parseInt(targetInfo[1]);
			
			
			
			CreatureType oldTargetType = BlueSaga.playerCharacter.getAttackTargetType();
			int oldTargetId = BlueSaga.playerCharacter.getAttackTargetId();
			
			// REMOVE OLD TARGET
			if(ScreenHandler.SCREEN_OBJECTS_WITH_ID.get(oldTargetType.toString()+oldTargetId) != null){
				ScreenHandler.SCREEN_OBJECTS_WITH_ID.get(oldTargetType.toString()+oldTargetId).getCreature().setAttacked(false);
			}
		
			// SET NEW TARGET
			if(ScreenHandler.SCREEN_OBJECTS_WITH_ID.get(newTargetType.toString()+newTargetId) != null){
				ScreenHandler.SCREEN_OBJECTS_WITH_ID.get(newTargetType.toString()+newTargetId).getCreature().setAttacked(true);
			}
			
			BlueSaga.playerCharacter.setAttackTarget(newTargetType, newTargetId);
			if(!newTargetType.equals(CreatureType.None)){
				BlueSaga.playerCharacter.setGoToTarget(true);
			}
		}
		
		/*
		if(serverData.startsWith("<pk_over>")){
			BP_CLIENT.playerCharacter.setPlayerKiller(false);
		}else if(serverData.startsWith("<pk_start>")){
			BP_CLIENT.playerCharacter.setPlayerKiller(true);
		}
		if(serverData.startsWith("<pk_status>")){
			String[] pkStatus = serverData.substring(11).split(";");
			int otherPlayerId = Integer.parseInt(pkStatus[0]);
			int pk_status = Integer.parseInt(pkStatus[1]);
			//WORLD_MAP.getOtherPlayerById(otherPlayerId).setOtherPlayerKiller(pk_status);
		}
		*/
		
		if(serverData.startsWith("<pk_status>")){
			String pkInfo[] = serverData.substring(11).split(";");
			
			PlayerCharacter c = (PlayerCharacter) MapHandler.addCreatureToScreen(pkInfo[0]);
			int pkMarker = Integer.parseInt(pkInfo[1]);
			
			if(c != null){
				c.setPkMarker(pkMarker);
			}
			
			if(c.getDBId() == BlueSaga.playerCharacter.getDBId()){
				BlueSaga.playerCharacter.setPkMarker(pkMarker);
				if(pkMarker == 0){
					BlueSaga.restartLogoutTimer(BlueSaga.logoutTime);
				}else if(pkMarker == 1){
					BlueSaga.restartLogoutTimer(BlueSaga.logoutTime);
				}else if(pkMarker == 2){
					BlueSaga.restartLogoutTimer(BlueSaga.playerKillerLogoutTime);
				}
				
			}
		}
		
		if(serverData.startsWith("<revive>")){
			BlueSaga.playerCharacter.revive();
			BlueSaga.playerCharacter.appear();
		}
		
		if(serverData.startsWith("<respawn>")){
			ScreenHandler.SCREEN_TILES.clear();
			ScreenHandler.SCREEN_TILES_B.clear();
			ScreenHandler.SCREEN_OBJECTS_WITH_ID.clear();
			ScreenHandler.SCREEN_OBJECTS_DRAW.clear();
			
			BlueSaga.BG_MUSIC.stop();
			
			String respawnInfo[] = serverData.substring(9).split(",");
			
			BlueSaga.playerCharacter.setX(Integer.parseInt(respawnInfo[0]));
			BlueSaga.playerCharacter.setY(Integer.parseInt(respawnInfo[1]));
			BlueSaga.playerCharacter.setZ(Integer.parseInt(respawnInfo[2]));
			
			if(BlueSaga.playerCharacter.getShip() !=  null){
				BlueSaga.playerCharacter.getShip().setShow(false);
			}
			
			Gui.Chat_Window.addTextLine("event", "", "You've respawned at your last checkpoint");
			
			ScreenHandler.AREA_EFFECT.setAreaEffect(0, false, new Color(255,255,255), false, new Color(255,255,255),"None",6);
			
			BlueSaga.client.sendMessage("screen","true");
		}
		
		else if(serverData.startsWith("<respawn_other_map>")){
			ScreenHandler.setActiveScreen(ScreenType.LOADING);
			BlueSaga.playerCharacter.revive();
		}
		
	}

	
	
	public static void showAttack(String attack_info){
		String attackerTargetAttackInfo[] = attack_info.split("/");
		
		String attackerInfo = attackerTargetAttackInfo[0];
		Creature ATTACKER = MapHandler.addCreatureToScreen(attackerInfo);
		
		String targetInfo = attackerTargetAttackInfo[1];
		Creature TARGET = MapHandler.addCreatureToScreen(targetInfo);
		
		String attackDir = attackerTargetAttackInfo[2];
		
		if(TARGET != null && ATTACKER != null){
			
			// PERFORM ATTACK
			String dirAttackInfo[] = attackDir.split(";");
			
			String attackDirInfo[] = dirAttackInfo[0].split(":");
			int dX = Integer.parseInt(attackDirInfo[0]);
			int dY = Integer.parseInt(attackDirInfo[1]);
			int attackSpeed = Integer.parseInt(attackDirInfo[2]);
			
			float rotation = MathUtils.angleBetween(-dX, -dY);
			ATTACKER.setRotationNow(rotation);
			
			if(dirAttackInfo.length > 1){
				// CREATE PROJECTILE
				ATTACKER.addCreatureAnimation(10, attackSpeed);
			}else{
				ATTACKER.attackTarget(dX, dY,attackSpeed);
			}
		}
		
	}
	
	
	
	
	public static void showDeath(String victimInfo){
		// victimType; victimId; attacketType; attackerId;  
		
		Creature VICTIM = MapHandler.addCreatureToScreen(victimInfo);
		
		
		if(VICTIM != null){
			if(VICTIM.getCreatureType() == CreatureType.Player && VICTIM.getDBId() == BlueSaga.playerCharacter.getDBId()){
				// YOU DIE
				Gui.closeAllWindows();
				Sfx.play("notifications/game_over");
				BlueSaga.logoutTimerOk();
			}else{
				Sfx.playRandomPitch("notifications/monster_death");
			}
			VICTIM.die();
			
			MapHandler.updateScreenObjects();
		}
	}
}