package data_handlers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.function.Consumer;

import components.Quest;
import utils.ServerMessage;
import utils.TimeUtils;
import game.ServerSettings;
import network.Client;
import network.Server;
import creature.Npc;
import creature.PlayerCharacter;
import creature.Creature.CreatureType;
import data_handlers.ability_handler.Ability;
import data_handlers.battle_handler.BattleHandler;
import data_handlers.party_handler.PartyHandler;

public class ConnectHandler extends Handler {
	
	public static void init(){
		DataHandlers.register("connection", m -> handleConnection(m));
		DataHandlers.register("playerinfo", m -> handlePlayerInfo(m));
		DataHandlers.register("actionbar", m -> handleActionBar(m));
		DataHandlers.register("ready", m -> handleReady(m));
		DataHandlers.register("keepalive", m -> handleKeepAlive(m));
		DataHandlers.register("quitchar", m -> handleQuitCharacter(m));
	}
	
	public static void handleConnection(Message m) {
		Client client = m.client;
		if("hello".equals(m.message)){
			client.sendingData = false;
			addOutGoingMessage(client,"connect", ServerSettings.CLIENT_VERSION);
		}
	}
	
	public static void handlePlayerInfo(Message m) {
		Client client = m.client;
		int characterId = Integer.parseInt(m.message);
		sendPlayerCharacterInfo(client, characterId);
	}
	
	public static void handleActionBar(Message m) {
		Client client = m.client;
		sendActionbar(client,true);
	}
			
	public static void handleReady(Message msg) {
		Client client = msg.client;
		if("no".equals(msg.message)){
			client.Ready = false;
			return ;
		}
		
		if("ready".equals(msg.message) && client.playerCharacter != null){
			
			client.Ready = true;
			
			if(Server.WORLD_MAP.getTile(client.playerCharacter.getX(),client.playerCharacter.getY(),client.playerCharacter.getZ()) != null){
			
				// IF HAS BOAT, SET BOAT
				if(Server.WORLD_MAP.getTile(client.playerCharacter.getX(),client.playerCharacter.getY(),client.playerCharacter.getZ()).isWater()){
					
					boolean bridge = false;
							
							if(Server.WORLD_MAP.getTile(client.playerCharacter.getX(),client.playerCharacter.getY(),client.playerCharacter.getZ()).getObjectId().contains("bridge")){
								bridge = true;
							}
						
							if(!bridge){
								if(client.playerCharacter.getShip() != null){
							if(client.playerCharacter.getShip().getShipId() > 0){
								client.playerCharacter.getShip().setShow(true);
								
								for (Entry<Integer, Client> entry : Server.clients.entrySet()) {
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
				}
				
				Server.WORLD_MAP.addPlayerToZ(client.playerCharacter, client.playerCharacter.getZ());
				
				Server.WORLD_MAP.getTile(client.playerCharacter.getX(),client.playerCharacter.getY(),client.playerCharacter.getZ()).setOccupant(CreatureType.Player, client.playerCharacter);
			}
			
			// NOTIFY FRIENDS OF ONLINE STATUS
			if(client.playerCharacter.getAdminLevel() < 5){
				for (Entry<Integer, Client> entry : Server.clients.entrySet()) {
					Client other = entry.getValue();
			 
					if(other.Ready){
						if(other.playerCharacter.getDBId() != client.playerCharacter.getDBId()){
							addOutGoingMessage(other,"message",client.playerCharacter.getName()+" #messages.connect.is_now_online");
						}
					}
				}
			}
			
			// Remove aggro from player when he logs in
			for(Iterator<Npc> iter = Server.WORLD_MAP.getMonsters().values().iterator();iter.hasNext();){  
				Npc m = iter.next();
				if(m.isAggro()){
					if(m.getAggroTarget().getCreatureType() == CreatureType.Player && m.getAggroTarget().getDBId() == client.playerCharacter.getDBId()){
						m.setAggro(null);
					}
				}
			}
			
			// IF NO ACTIVE QUESTS AND HAVN'T COMPLETE THE UNDEAD THREAT, 
			// THEN NOTIFY PLAYER TO TALK TO INN-KEEPER
			boolean firstTime = true;
			boolean showNoobQuestWarning = true;
			
			for(Quest q: client.playerCharacter.getQuests()){
				if(q.getId() == 36){
					firstTime = false;
				}
				if(q.getStatus() == 1){
					showNoobQuestWarning = false;
				}
			}
			
			if(client.playerCharacter.getQuestById(6) != null){
				showNoobQuestWarning = false;
			}
			
			if(client.playerCharacter.getX() != 5005 || client.playerCharacter.getY() != 9984 || client.playerCharacter.getZ() != 2){
				firstTime = false;
			}
			
			if(client.playerCharacter.getTutorialNr() > 0){
				firstTime = false;
			}else{
				showNoobQuestWarning = false;
			}
			
			// Add world chat
			client.playerCharacter.addChatChannel("#world");
			
			// IF NEW PLAYER SEND INTRO CUTSCENE
			if(firstTime){
				addOutGoingMessage(client,"cutscene","1");
			
				// If first time playing, show tutorials
				if(client.playerCharacter.getTutorialNr() == 0){
					addOutGoingMessage(client,"tutorial","0");
				}
			}else if(showNoobQuestWarning){
				addOutGoingMessage(client,"message","#messages.connect.talk_to_inn_keeper");
			}
		}else{
			// Remove client
			removeClient(client);
		}
	}
		
	public static void handleKeepAlive(Message m) {
		Client client = m.client;
		addOutGoingMessage(client,"keepalive","hello");
	}
	
	public static void handleQuitCharacter(Message m) {
		Client client = m.client;
		addOutGoingMessage(client,"quitchar","quit");
	
		logoutCharacter(client);
		
		String character_info = LoginHandler.getCharacterInfo(client);
		addOutGoingMessage(client,"login",client.UserId+";"+client.UserMail+":"+character_info);
	}
	
	public static void logoutCharacter(Client client){
		client.Ready = false;
		
		if(client.playerCharacter != null){
			PartyHandler.leaveParty(client);
			
			// SAVE COOLDOWNS LEFT ON ABILITIES
			for(Iterator<Ability> iter = client.playerCharacter.getAbilities().iterator();iter.hasNext();){  
				Ability a = iter.next();
				
				if(a != null){
					Server.userDB.updateDB("update character_ability set CooldownLeft = "+a.getCooldownLeft()+" where AbilityId = "+a.getAbilityId()+" and CharacterId = "+client.playerCharacter.getDBId());
				}
			}
			
			// UPDATE LAST ONLINE TIME
			Server.userDB.updateDB("update user_character set PlayerKiller = 0, LastOnline = '"+TimeUtils.now()+"' where Id = "+client.playerCharacter.getDBId());
			
			// MAKE TILE UNOCCUPIED
			if(Server.WORLD_MAP.getTile(client.playerCharacter.getX(), client.playerCharacter.getY(), client.playerCharacter.getZ()) != null){
				Server.WORLD_MAP.getTile(client.playerCharacter.getX(), client.playerCharacter.getY(), client.playerCharacter.getZ()).setOccupant(CreatureType.None, null);
			}
			
			Server.WORLD_MAP.removePlayerFromZ(client.playerCharacter, client.playerCharacter.getZ());
		
			// SEND INFO ABOUT LOGOUT TO OTHER PLAYERS
			// SEND CANCELED BATTLE AND MONSTER AGGRO CHANGE
			for (Entry<Integer, Client> entry : Server.clients.entrySet()) {
				Client s = entry.getValue();
				if(s.Ready){
					if(s.playerCharacter.getDBId() != client.playerCharacter.getDBId() ){
						if(isVisibleForPlayer(s.playerCharacter, client.playerCharacter.getX(), client.playerCharacter.getY(), client.playerCharacter.getZ())){
							addOutGoingMessage(s,"creature_remove",client.playerCharacter.getSmallData());
						}
					}
				}
			}	
			
			client.playerCharacter.saveInfo();
		}
	}
	
	public static void sendActionbar(Client client, boolean loadMore){
		String info = "";
		
		// ACTIONBAR DATA
		boolean foundAbility = false;
		ResultSet aInfo = Server.userDB.askDB("select ActionType, ActionId, OrderNr from character_actionbar where CharacterId = "+client.playerCharacter.getDBId()+" order by OrderNr asc");
		try {
			while(aInfo.next()){
				//TODO: CHECK IF CAN HAVE ABILITY
				foundAbility = true;
				info += aInfo.getInt("OrderNr")+","+aInfo.getString("ActionType")+","+aInfo.getInt("ActionId")+";";
			}
			aInfo.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if(!foundAbility){
			info += "None";
		}	
		// 1/ means load more when done
		if(loadMore){
			addOutGoingMessage(client,"actionbar","1/"+info);
		}else{
			addOutGoingMessage(client,"actionbar","0/"+info);
		}
	}
	
	public static void sendPlayerCharacterInfo(Client client, int characterId){

		// CHECK IF USER OWNS CHARACTER
		if(Server.userDB.checkCharacterOwnership(characterId, client.UserId)){
			
			ResultSet charInfo = Server.userDB.askDB("select CreatureId, X, Y, Z, AreaEffectId from user_character where UserId = "+client.UserId+" and Id = "+characterId);
			
			try {
				if(charInfo.next()){
					client.playerCharacter = new PlayerCharacter(client, charInfo.getInt("CreatureId"),charInfo.getInt("X"),charInfo.getInt("Y"), charInfo.getInt("Z"));
					client.playerCharacter.load(characterId, client);
					client.playerCharacter.setCreatureType(CreatureType.Player);
					
					if(client.playerCharacter != null){
						
						boolean respawnAtCheckpoint = false;
						
						// Respawn player if player position is null
						if(Server.WORLD_MAP.getTile(client.playerCharacter.getX(),client.playerCharacter.getY(),client.playerCharacter.getZ()) == null){
							respawnAtCheckpoint = true;
						}
						
						/*
						// Respawn player if in lost archipelago
						if(!Server.DEV_MODE && client.playerCharacter.getZ() <= -200){
							respawnAtCheckpoint = true;
						}
						*/
						
						if(respawnAtCheckpoint){
							BattleHandler.respawnPlayer(client.playerCharacter);
						}
						
						String info = client.playerCharacter.getInfo();
						
						addOutGoingMessage(client,"playerinfo",info);
						addOutGoingMessage(client,"update_bonusstats",client.playerCharacter.getBonusStatsAsString());
							
						if(client.playerCharacter.getMouseItem() != null){
							addOutGoingMessage(client, "addmouseitem",""+client.playerCharacter.getMouseItem().getId()+";"+client.playerCharacter.getMouseItem().getType());
						}
						
						if(charInfo.getInt("AreaEffectId") > 0){
							WalkHandler.sendAreaEffect(client, charInfo.getInt("AreaEffectId"));
						}
					}
				}else{
					// Error: Can't find character in DB!
				}
				charInfo.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}else{
			// Error: Player doesn't have that character!
		}
	}
	
	public synchronized static void removeClient(Client client) {
		if(!client.HasQuit){
			// REMOVE AGGRO FORM ALL PLAYERS THAT HAS PLAYER AS TARGET
			client.HasQuit = true;
			client.Ready = false;
			client.closeSocket();
			
			if(client.playerCharacter != null){
				ServerMessage.printMessage(TimeUtils.now()+": "+ client.playerCharacter.getName()+"("+client.playerCharacter.getDBId()+") disconnects.",false);
			
				// RESPAWN AT CHECKPOINT IF DEAD
	    		if(client.playerCharacter.isDead()){
	    			client.playerCharacter.revive();
	    			BattleHandler.respawnPlayer(client.playerCharacter);
	    		}
	    		
	    		
	    		for (Entry<Integer, Client> entry : Server.clients.entrySet()) {
	    			Client s = entry.getValue();
			 
	    			if(s.Ready){
						if(s.playerCharacter != null){
							if(s.playerCharacter.isAggro()){
								if(s.playerCharacter.getAggroTarget().getCreatureType() == CreatureType.Player && s.playerCharacter.getAggroTarget().getDBId() == client.playerCharacter.getDBId()){
									s.playerCharacter.setAggro(null);
								}
							}
						}
					}
				}
			}
			
			logoutCharacter(client);
			client.RemoveMe = true;
		}
	}
	
}
