package data_handlers.card_handler;

import network.Client;
import network.Server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import creature.Creature.CreatureType;
import data_handlers.Handler;
import data_handlers.MapHandler;
import data_handlers.item_handler.ContainerHandler;
import data_handlers.item_handler.Item;
import utils.RandomUtils;
import utils.ServerGameInfo;
import utils.ServerMessage;
import utils.TimeUtils;
import utils.WebHandler;

public class CardHandler extends Handler {
	
	public static HashMap<String,Card> cards = new HashMap<String,Card>();
	public static HashMap<Integer,Card> card_ids = new HashMap<Integer,Card>();
	public static HashMap<Integer,Card> itemid_cards = new HashMap<Integer,Card>();
	
	public static Timer restartTimer;
	
	public static void setup(){
		ServerMessage.printMessage("Creating cards...",true);
		Server.gameDB.updateDB("DELETE FROM item WHERE Type = 'Collector Card'");
		
		int maxLevel = 20;
		ResultSet monsterInfo = Server.gameDB.askDB("SELECT Id, Name, Level FROM creature WHERE Level <= "+maxLevel+" AND LootCopper > 0 ORDER BY Level ASC");
		int cardId = 315; 
		try {
			while(monsterInfo.next()){
				Server.gameDB.updateDB("INSERT INTO item (Id, Name, Type, ReqLevel, DamageType, StatusEffects, Stackable, Value) VALUES ("+cardId+",'"+monsterInfo.getString("Name")+" Card','Collector Card',0,'None','None',1, 0)");
				cardId++;
			}
			monsterInfo.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		ServerMessage.printMessage("Creating cards done!",true);
	}
	
	public static void init(){

		restartTimer = new Timer();
		
		ResultSet cards_id = Server.gameDB.askDB("SELECT Id, ItemId, GraphicsId, Type FROM card ORDER BY Id ASC");
		
		try {
			while(cards_id.next()){
				Card newCard = new Card(cards_id.getInt("Id"));
				cards.put(cards_id.getString("Type")+cards_id.getInt("GraphicsId"),newCard);
				itemid_cards.put(cards_id.getInt("ItemId"),newCard);
				card_ids.put(cards_id.getInt("Id"), newCard);
			}
			cards_id.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void handleData(Client client, String message) {
		if(message.startsWith("<card_book>")){
			sendCardBookContent(client);
		}else if (message.startsWith("<card_place>")) {
			String cardInfo[] = message.substring(12).split(",");
			 
			Integer.parseInt(cardInfo[0]);
			int cardSlot = Integer.parseInt(cardInfo[1]);
			
			
			// Check if player has card on mouse
			Item cardItem = client.playerCharacter.getMouseItem();
			if(cardItem != null){
				
				Card playerCard = itemid_cards.get(cardItem.getId());
				
				boolean addItemBackToMouse = true;
				if(cardItem.getType().equals("Collector Card")){
					
					if(client.playerCharacter.getCardBook().contains(playerCard)){
						// Slot already taken
						addOutGoingMessage(client,"message","#ui.cardbook.slot_taken");
					}else{
						// Check if it is the right slot
						if(playerCard.id == cardSlot){
							// Add card to book
							Server.userDB.updateDB("INSERT INTO character_card (CardId, CharacterId, DateTime) VALUES ("+playerCard.id+","+client.playerCharacter.getDBId()+",'"+TimeUtils.now()+"')");
							client.playerCharacter.getCardBook().add(playerCard);
							sendCardBookContent(client);
						
							// Send card to player
							addOutGoingMessage(client,"card_place","");
					
							// Send card acquisition to website
							//if(!Server.DEV_MODE){
								String event = "?check=tjolahopp612&playerId="+client.playerCharacter.getDBId()+"&playerName="+client.playerCharacter.getName()+"&eventType=card_add&eventTargetName="+playerCard.id+"&eventTargetType=card&eventTargetId="+playerCard.id;
								WebHandler.callUrl("http://www.bluesaga.org/server/addEvent.php"+event);
							//}
							
							// Remove card from mouse
							Server.userDB.updateDB("DELETE FROM character_item WHERE ItemId = "+cardItem.getId()+" and InventoryPos = 'Mouse' and CharacterId = "+client.playerCharacter.getDBId());
							client.playerCharacter.setMouseItem(null);
							addItemBackToMouse = false;
						
							// Check if wins game
							checkEndGame(client);
						}else{
							// Wrong slot
							addOutGoingMessage(client,"message","#ui.cardbook.wrong_slot");
						}
					}
				}
				if(addItemBackToMouse){
					addOutGoingMessage(client,"addmouseitem",cardItem.getId()+";"+cardItem.getType());
				}
			}
			
		}else if(message.startsWith("<add_card_to_mouse>")){
			try{
				int cardId = Integer.parseInt(message.substring(19));
				
				boolean hasCard = false;
				for(Card card: client.playerCharacter.getCardBook()){
					if(card.id == cardId){
						hasCard = true;
						break;
					}
				}
				
				// Check if player has card
				if(hasCard){
					
					Item mouseItem = new Item(ServerGameInfo.itemDef.get(card_ids.get(cardId).itemId));
					
					ContainerHandler.addItemToMouse(client, mouseItem);
					
					removeCard(client, cardId);
					
				}else{
					client.playerCharacter.setMouseItem(null);
					addOutGoingMessage(client,"clearmouse","");
				}
				
			}catch(NumberFormatException e){
				addOutGoingMessage(client,"clearmouse","");
			}
		}
	}
	
	
	public static void removeCard(Client client, int cardId){
		Server.userDB.updateDB("DELETE FROM character_card WHERE CardId = "+cardId+" AND CharacterId = "+client.playerCharacter.getDBId());
		for(Card card: client.playerCharacter.getCardBook()){
			if(card.id == cardId){
				client.playerCharacter.getCardBook().remove(card);
				break;
			}
		}
		sendCardBookContent(client);
	}
	
	public static void sendCardBookContent(Client client){
		String cardsInfo = "";
		for(Card card: client.playerCharacter.getCardBook()){
			cardsInfo += card.id+","+card.itemId+";";
		}
		if(cardsInfo.length() > 0){
			cardsInfo = cardsInfo.substring(0,cardsInfo.length()-1);
		}
		addOutGoingMessage(client, "card_book", cardsInfo);
	}
	
	
	public static Item monsterDropCard(int monsterId){
		Card droppedCard = cards.get("Creature"+monsterId);
		
		Item droppedCardItem = null;

		if(droppedCard != null){
			// Check if any cards left to drop
			if(droppedCard.copiesLeft > 0){
				
				// Caculate dropchance
				int dropChance = 20*(1 + cards.size() - droppedCard.rarity);
				
				int dropOrNot = RandomUtils.getInt(0, dropChance);
				
				if(dropOrNot == 0){
					droppedCard.copiesLeft--;
					droppedCardItem = ServerGameInfo.itemDef.get(droppedCard.itemId);
				}
			}
		}
		
		return droppedCardItem;
	}
	
	// Player drops random card
	public static Item playerDropCard(Client client){
		Item droppedCardItem = null;
		
		if(client.playerCharacter.getCardBook().size() > 0){
			int cardNr = RandomUtils.getInt(0, client.playerCharacter.getCardBook().size()-1);
			Card droppedCard = client.playerCharacter.getCardBook().get(cardNr);
			droppedCardItem = ServerGameInfo.itemDef.get(droppedCard.itemId);
	
			// Send card loss to website
			//if(!Server.DEV_MODE){
				String event = "?check=tjolahopp612&playerId="+client.playerCharacter.getDBId()+"&playerName="+client.playerCharacter.getName()+"&eventType=card_lost&eventTargetName="+droppedCard.id+"&eventTargetType=card&eventTargetId="+droppedCard.id;
				WebHandler.callUrl("http://www.bluesaga.org/server/addEvent.php"+event);
			//}
			
			Server.userDB.updateDB("delete from character_card where CardId = "+droppedCard.id+" and CharacterId = "+client.playerCharacter.getDBId());
			client.playerCharacter.getCardBook().remove(cardNr);
		}
		return droppedCardItem;
	}
	
	
	public static void checkEndGame(Client cardPicker){
		
		Vector<Integer> collectedCards = new Vector<Integer>();
		
		boolean winGame = false;
		
		for(Card card: cardPicker.playerCharacter.getCardBook()){
			if(!collectedCards.contains(card.id)){
				// Count card
				collectedCards.add(card.id);
			}
		}

		// If player has all cards, then win game
		if(collectedCards.size() == cards.size()){
			winGame = true;
		}
		
		// A player won the game
		if(winGame){
			// Send end movie to all players
			for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
				Client c = entry.getValue();
	 	
				if(c.Ready){
					addOutGoingMessage(c,"cutscene","3;"+cardPicker.playerCharacter.getSmallData());
					MapHandler.sendCreatureInfo(c, CreatureType.Player, cardPicker.playerCharacter.getDBId());
				}
			}

			// Send end statistics to website
			//if(!Server.DEV_MODE){
				String event = "?check=tjolahopp612&winnerId="+cardPicker.playerCharacter.getDBId()+"&nrCards="+cards.size();
				WebHandler.callUrl("http://www.bluesaga.org/server/resetCards.php"+event);
			//}
			
			// Remove all cards from players
			Server.userDB.updateDB("DELETE FROM character_card");
			for(Card card: cards.values()){
				Server.userDB.updateDB("DELETE FROM character_item WHERE ItemId = "+card.itemId);
			}
			
			// Restart server, pauses all activity on server
			Server.SERVER_RESTARTING = true;
			
			restartTimer.schedule( new TimerTask(){
				@Override
				public void run() {
					Server.restartServer();
				}
			}, 20 * 1000);
		}
	}
}