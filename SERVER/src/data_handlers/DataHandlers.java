package data_handlers;

import game.ServerSettings;
import login.WebsiteServerStatus;
import network.Client;
import network.Server;

import java.io.IOException;
import java.net.SocketException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import data_handlers.ability_handler.AbilityHandler;
import data_handlers.ability_handler.StatusEffectHandler;
import data_handlers.battle_handler.BattleHandler;
import data_handlers.battle_handler.PvpHandler;
import data_handlers.card_handler.CardHandler;
import data_handlers.chat_handler.ChatHandler;
import data_handlers.crafting_handler.CraftingHandler;
import data_handlers.item_handler.ContainerHandler;
import data_handlers.item_handler.ItemHandler;
import data_handlers.monster_handler.MonsterHandler;
import data_handlers.party_handler.PartyHandler;
import utils.CrashLogger;

public class DataHandlers {
	private static ConcurrentLinkedQueue<Message> incomingMessages;
	private static ConcurrentLinkedQueue<Message> outgoingMessages;

	public static void init(){
		incomingMessages = new ConcurrentLinkedQueue<Message>();
		outgoingMessages = new ConcurrentLinkedQueue<Message>();
		
		MonsterHandler.init();
		AbilityHandler.init();
		ContainerHandler.init();
		ChatHandler.init();
		FishingHandler.init();
		TrapHandler.init();
		CraftingHandler.init();
		PartyHandler.init();
		CardHandler.init();
	}

	
	public static void update(long tick){
		
		// Every 50 ms
		MonsterHandler.update(tick);
		BattleHandler.updateRangedCooldowns();
		
		AbilityHandler.updateProjectiles();
		AbilityHandler.updateAbilityEvents();
		AbilityHandler.updatePlayerCasting();
		
		// Every 200 ms
		if(tick % 4 == 0){
			BattleHandler.update();
			AbilityHandler.updateCooldowns();
		}
		
		// Every 1000 ms
		if(tick % 20 == 0){
			TrapHandler.update(); 
			StatusEffectHandler.updateStatusEffects();
			StatusEffectHandler.updateTileStatusEffects();
			PvpHandler.updatePKTimers();
			MapHandler.updateNightTime();
		}
		
		// Every 10000 ms
		if(tick % 200 == 0){
			ContainerHandler.checkContainerRespawn();
		}
	
		// Every minute
		if(tick % 1200 == 0){
			if(!ServerSettings.DEV_MODE){
				int nrPlayers = Server.clients.size();
				WebsiteServerStatus.UpdateServerStatus(ServerSettings.SERVER_ID, nrPlayers);
			}
		}
	}
	
	
	public static void addIncomingMessage(Message message){
		incomingMessages.add(message);
	}


	//public static void handleData(Client client, String message){

	public static void processIncomingData(){

		for(Iterator<Message> i = incomingMessages.iterator(); i.hasNext(); ) {
			Message m = i.next();

			Client client = m.client;
			String message = "<"+m.type+">"+m.message;

			ConnectHandler.handleData(client, message);

			LoginHandler.handleData(client, message);

			WalkHandler.handleData(client,message);

			SkillHandler.handleData(client, message);

			AbilityHandler.handleData(client, message);

			ItemHandler.handleData(client, message);

			MapHandler.handleData(client,message);

			MonsterHandler.handleData(client,message);

			BattleHandler.handleData(client, message);

			ContainerHandler.handleData(client, message);

			ChatHandler.handleData(client, message);

			BountyHandler.handleData(client, message);

			FriendsHandler.handleData(client, message);

			QuestHandler.handleData(client, message);

			ShopHandler.handleData(client, message);

			MusicHandler.handleData(client, message);

			FishingHandler.handleData(client, message);

			GatheringHandler.handleData(client, message);

			SkinHandler.handleData(client, message);

			CraftingHandler.handleData(client, message);
			
			TutorialHandler.handleData(client,message);
			
			PartyHandler.handleData(client, message);
			
			CardHandler.handleData(client, message);
			
			i.remove();
		}
	}


	public static void addOutgoingMessage(Message message){
		outgoingMessages.add(message);
	}

	public static void processOutgoingData(){
		for(Iterator<Message> i = outgoingMessages.iterator(); i.hasNext(); ) {
			Message m = i.next();

			sendMessage(m);

			i.remove();
		}
	}

	private static boolean sendMessage(Message message){
		boolean sendOk = true;
		Client client = message.client;

		try{
			try{
				String dataToSend = "<"+message.type+">"+message.message;
				
				byte[] byteMsg = (dataToSend).getBytes();
				if(client.out != null){
					client.out.writeObject(byteMsg);
					client.out.reset();
					client.out.flush();
				}
				
				
			}catch(SocketException e){
				sendOk = false;
			}
		}
		catch(IOException ioException){
			CrashLogger.uncaughtException(ioException);
			sendOk = false;
		}
		return sendOk;
	}
}