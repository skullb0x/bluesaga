package data_handlers;

import network.Client;
import network.Server;

import java.util.Map;

public class FriendsHandler extends Handler {
	
	
	public static void init() {
	}
	
	public static void handleData(Client client, String message){
		if(client.playerCharacter != null){
			if(message.startsWith("<playersonline>")){
				String sendInfo = "";
				
				int total_online = 0;
				
				String playerInfo = "";
				
				
				for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
					Client s = entry.getValue();
			 
					if(s.Ready){
						total_online++;
						if(s.playerCharacter.getDBId() != client.playerCharacter.getDBId() && client.playerCharacter.getFriendsList().contains(s.playerCharacter.getDBId())){
							playerInfo += s.playerCharacter.getName()+";";
						}
					}
				}
				
				sendInfo = total_online+"/"+playerInfo;
				
				if(!sendInfo.equals("")){
					addOutGoingMessage(client,"playersonline",sendInfo);
				}
			}else if(message.startsWith("<add_friend>")){
				String friendsName = message.substring(12);
				
				// CHECK IF FRIEND IS ALREADY IN THE LIST
				int status = Server.userDB.checkFriend(friendsName, client);
				
				if(status == 0){
					addOutGoingMessage(client,"message","#messages.friends.already_in_list");
				}else if(status == -1){
					addOutGoingMessage(client,"message","#messages.friends.player_not_exist");
				}else if(status == -2){
					addOutGoingMessage(client,"message","#messages.friends.cant_add_yourself");
				}else if(status == -3){
					addOutGoingMessage(client,"message","#messages.friends.cant_add_admins");
				}else {
					addOutGoingMessage(client,"message","#messages.friends.friend_added");
				}
			
			}
		}
	}
	
}
