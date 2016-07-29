package data_handlers;

import creature.Creature;
import creature.PlayerCharacter;
import game.BlueSaga;
import graphics.BlueSagaColors;
import gui.Gui;

public class ChatHandler extends Handler {

	
	public ChatHandler(){
		super();
	}
	
	
	public static void handleData(String serverData){
		// CHAT DATA
		if(serverData.startsWith("<newchat>")){
			String chatinfo[] = serverData.substring(9).split(";");
			String chatChannel = chatinfo[0].toLowerCase();
			String sender = chatinfo[1];
			
			Gui.Chat_Window.addChatChannel(chatChannel);
			
			// Set channel as active if you are the sender
			if(sender.toLowerCase().equals(BlueSaga.playerCharacter.getName().toLowerCase())){
				Gui.Chat_Window.setActiveChatChannel(chatChannel);
			}
			
			if(chatinfo.length > 2){
				Gui.Chat_Window.addTextLine(chatChannel, chatinfo[1], chatinfo[2]);
			}
		}else if(serverData.startsWith("<chatremove>")){
			String chatChannel = serverData.substring(12);
			Gui.Chat_Window.removeChatChannel(chatChannel);
		}else if(serverData.startsWith("<chaterror>")){
			String errorInfo[] = serverData.substring(11).split(";");
			String chatChannel = errorInfo[0];
			String errorMessage = errorInfo[1];
			Gui.Chat_Window.addChatChannel(chatChannel);
			Gui.Chat_Window.addTextLine(chatChannel, "error", errorMessage);
		}else if(serverData.startsWith("<bugaccepted>")){
			Gui.Chat_Window.addTextLine("admin", "admin", "bug report sent, thank you!");
		}else if(serverData.startsWith("<plzaccepted>")){
			Gui.Chat_Window.addTextLine("admin", "admin", "request sent, thank you!");
		}
		
		// BUG REPORTING
		if(serverData.startsWith("<newbug>")){
			String bugInfo[] = serverData.substring(8).split("/");
			Gui.addMessage(bugInfo[0]+" reported "+bugInfo[1],BlueSagaColors.GREEN);
		}else if(serverData.startsWith("<newplz>")){
			String bugInfo[] = serverData.substring(8).split("/");
			Gui.addMessage(bugInfo[0]+" reported "+bugInfo[1],BlueSagaColors.GREEN);
		}
		
		// EMOTICONS
		if(serverData.startsWith("<emoticon>")){
			String emoInfo[] = serverData.substring(10).split(";");
			Creature user = MapHandler.addCreatureToScreen(emoInfo[0]);
			String emoticon = emoInfo[1];
			user.MyEmoticonHandler.show(emoticon);
		}
		
		// ROLL DICE
		if(serverData.startsWith("<rolldice>")){
			String rollInfo[] = serverData.substring(10).split(";");
			PlayerCharacter user = (PlayerCharacter) MapHandler.addCreatureToScreen(rollInfo[0]);
			int diceResult = Integer.parseInt(rollInfo[1]);
			user.rollDice(diceResult);
		}
		
	}
}
