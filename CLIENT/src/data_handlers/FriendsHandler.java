package data_handlers;

import gui.Gui;

public class FriendsHandler extends Handler {

	
	public FriendsHandler(){
		super();
	}
	
	public static void handleData(String serverData){
		if(serverData.startsWith("<playersonline>")){
			String playerInfo = serverData.substring(15);
			
			Gui.PlayersWindow.loadPlayers(playerInfo);
		}
	}
	
}
