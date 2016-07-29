package data_handlers;

import gui.Gui;


public class PartyHandler extends Handler {

	public static boolean inaParty = false;
	
	public PartyHandler(){
		
	}
	
	public static void handleData(String message){
		if(message.startsWith("<party_members>")){
			String partyInfo = message.substring(15);
			Gui.PlayersWindow.loadPartyMembers(partyInfo);
		}else if(message.startsWith("<party_chat>")){
			String partyInfo = message.substring(12);
			if(partyInfo.equals("active")){
				inaParty = true;
				Gui.Chat_Window.addChatChannel("party");
				Gui.Chat_Window.addTextLine("event", "", "Party channel added.");
				Gui.Chat_Window.addTextLine("event", "", "Press TAB to change channel.");
			}else{
				inaParty = false;
				Gui.Chat_Window.removeChatChannel("party");
				Gui.Chat_Window.addTextLine("event", "", "Party channel removed.");
				
			}
		}else if(message.startsWith("<request>")){
			String requestInfo[] = message.substring(9).split(";");
			
			String request_type = requestInfo[0];
			int request_id = Integer.parseInt(requestInfo[1]);
			String request_message = requestInfo[2];
			
			if(request_type.equals("join_party")){
				Gui.addRequest(request_type, request_id, request_message);
			}
		}
	}
}
