package data_handlers.chat_handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import utils.RandomUtils;
import data_handlers.Handler;
import data_handlers.MapHandler;
import data_handlers.monster_handler.MonsterHandler;
import game.ServerSettings;
import network.Client;
import network.Server;

public class ChatHandler extends Handler {


	private static Vector<String> Emoticons = new Vector<String>();
	public static Vector<String> BadWords = new Vector<String>();
	private static Vector<String> CuteWords = new Vector<String>();

	private static Vector<Integer> mutedUsers = new Vector<Integer>();
	
	
	public static void init() {

		mutedUsers = new Vector<Integer>();
		
		Emoticons.add("angry");
		Emoticons.add("fail");
		Emoticons.add("joke");
		Emoticons.add("lol");
		Emoticons.add("love");
		Emoticons.add("sad");
		Emoticons.add("smile");
		Emoticons.add("weird");

		ResultSet badWordsInfo = Server.gameDB.askDB("select Word from bad_words");
		try {
			while(badWordsInfo.next()){
				BadWords.add(badWordsInfo.getString("Word").toLowerCase());
			}
			badWordsInfo.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		ResultSet cuteWordsInfo = Server.gameDB.askDB("select Word from cute_words");
		try {
			while(cuteWordsInfo.next()){
				CuteWords.add(cuteWordsInfo.getString("Word").toLowerCase());
			}
			cuteWordsInfo.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void handleData(Client client, String message){
		// CHAT EVENTS
		if(client.playerCharacter != null){
			if(message.startsWith("<newchat>")){
				boolean specialCommand = false;
				
				String chatInfo[] = message.substring(9).split(";");
				
				if(chatInfo.length == 2){
					String chatChannel = chatInfo[0].toLowerCase();
					String chatText = chatInfo[1];

					chatText = chatText.replace("'", "");
					
					if(chatText.startsWith("/")){
						specialCommand = true;
					}
					
					// SPECIAL REFRESH
					if(chatText.toLowerCase().equals("/r")){
						specialCommand = true;
						MapHandler.sendScreenData(client);
					}

					// ADMIN CONTROLS
					boolean adminCommand = false;
					try{
						adminCommand = AdminControlsHandler.handleMessage(client, chatText);
					}catch(NullPointerException e){

					}catch(NumberFormatException e){
					
					}
					
					if(!specialCommand){
						specialCommand = adminCommand;
					}

					// Emoticons
					if(chatText.toLowerCase().startsWith("/emo")){
						specialCommand = true;

						if(chatText.toLowerCase().equals("/emo")){
							String emoHelp = "To show emoticon type '/emo name'. Available emoticons are: ";
							for(String emo: Emoticons){
								emoHelp += emo+", ";
							}
							emoHelp = emoHelp.substring(0,emoHelp.length()-2);
							addOutGoingMessage(client,"message",emoHelp);
						}else{
							String emoticon = chatText.toLowerCase().substring(5);

							if(Emoticons.contains(emoticon)){
								// IF RESTING, ABORT RESTING
								if(client.playerCharacter.isResting()){
									MonsterHandler.changeMonsterSleepState(client.playerCharacter, false);
								}

								// SEND EMOTICONS TO PLAYERS IN AREA
								for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
									Client s = entry.getValue();

									if(s.Ready){
										if(isVisibleForPlayer(s.playerCharacter,client.playerCharacter.getX(),client.playerCharacter.getY(),client.playerCharacter.getZ())){
											addOutGoingMessage(s,"emoticon",client.playerCharacter.getSmallData()+";"+emoticon);
										}
									}
								}
								Server.userDB.addChatText("emo",client.playerCharacter.getDBId(),0,emoticon);
							}
						}
					}

					if(chatText.toLowerCase().startsWith("/channels")){
						// Go through all chat channels and count their subscribers
						specialCommand = true;
						
						HashMap<String,Integer> chatChannels = new HashMap<String, Integer>();
						
						for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
							Client s = entry.getValue();

							if(s.Ready){
								for(String channel: s.playerCharacter.getChatChannels()){
									int nrSubscribers = 0;
									if(chatChannels.containsKey(channel)){
										nrSubscribers = chatChannels.get(channel);
									}
									nrSubscribers++;
									chatChannels.put(channel, nrSubscribers);
								}
							}
						}
						
					    addOutGoingMessage(client,"newchat",chatChannel+";event;channels:");
						
					    if(chatChannels.size() > 0){
					    	Iterator it = chatChannels.entrySet().iterator();
						    while (it.hasNext()) {
						        Map.Entry pairs = (Map.Entry)it.next();
						        addOutGoingMessage(client,"newchat",chatChannel+";event;"+pairs.getKey()+" ("+pairs.getValue()+")");
							}	
					    }else{
					        addOutGoingMessage(client,"newchat",chatChannel+";event;none active");
						}
					}
					
					if(chatText.toLowerCase().startsWith("/rolldice")){
						specialCommand = true;
						int diceResult = RandomUtils.getInt(1, 6);
						
						for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
							Client s = entry.getValue();

							if(s.Ready){
								if(isVisibleForPlayer(s.playerCharacter, client.playerCharacter.getX(), client.playerCharacter.getY(), client.playerCharacter.getZ())){
									addOutGoingMessage(s,"rolldice",client.playerCharacter.getSmallData()+";"+diceResult);
								}
								
							}
						}
					}
					
					
					if(chatText.toLowerCase().startsWith("/quit")){
						specialCommand = true;
						if(chatChannel.contains("@") || chatChannel.contains("#")){
							client.playerCharacter.removeChatChannel(chatChannel);
							addOutGoingMessage(client,"chatremove",chatChannel.toLowerCase());
						}
					}
					
					
					// Handle chat message if not a special command or player is muted
					if(!specialCommand && !mutedUsers.contains(client.UserId)){
						
						// Filter bad words from chat message
						String chatMessage = "";

						chatMessage = chatText;

						for(String badword: BadWords){
							if(chatMessage.toLowerCase().equals(badword) || chatMessage.toLowerCase().contains(" "+badword) || chatMessage.toLowerCase().contains(" "+badword+" ") || chatMessage.toLowerCase().contains(badword+" ")){
								String randomCuteWord = CuteWords.get(RandomUtils.getInt(0,CuteWords.size()-1));
								chatMessage = chatMessage.toLowerCase().replace(badword, randomCuteWord);
							}
						}
					
						// Check if user wants to create a new channel
						if(chatMessage.startsWith("@") || chatMessage.startsWith("#")){
							String newChannelInfo[] = chatMessage.split(" ",2);
							chatChannel = newChannelInfo[0].toLowerCase();
							if(newChannelInfo.length > 1){
								chatMessage = newChannelInfo[1];
							}else{
								chatMessage = "";
							}
						}
						
						
						// Check if custom channel or private message
						if(chatChannel.startsWith("#")){
							client.playerCharacter.addChatChannel(chatChannel);
							
							// Custom channel, send to players that subscribe to it
							for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
								Client s = entry.getValue();

								if(s.Ready){
									if(s.playerCharacter.hasChatChannel(chatChannel)){
										addOutGoingMessage(s,"newchat",chatChannel+";"+client.playerCharacter.getName()+";"+chatMessage);
									}
								}
							}
						}else if(chatChannel.startsWith("@")){
							// Private message
							String receiverName = chatChannel.toLowerCase().substring(1);
							String senderNameChannel = "@"+client.playerCharacter.getName().toLowerCase();
							
							boolean playerOnline = false;
							
							for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
								Client s = entry.getValue();

								if(s.Ready){
									String playerName = s.playerCharacter.getName().toLowerCase();
									
									if(playerName.equals(receiverName)){
										playerOnline = true;
										addOutGoingMessage(s,"newchat",senderNameChannel+";"+client.playerCharacter.getName()+";"+chatMessage);
										break;
									}
								}
							}
							if(!playerOnline){
								addOutGoingMessage(client,"chaterror",chatChannel+";"+receiverName+" is not online");
							}
							addOutGoingMessage(client,"newchat",chatChannel+";"+client.playerCharacter.getName()+";"+chatMessage);
						}else{
							// Announcements, crew, party and local chat
						
							for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
								Client s = entry.getValue();

								if(s.Ready){
									if(chatChannel.equals("local")){
										if(isVisibleForPlayer(s.playerCharacter,client.playerCharacter.getX(),client.playerCharacter.getY(),client.playerCharacter.getZ())){
											addOutGoingMessage(s,"newchat",chatChannel+";"+client.playerCharacter.getName()+";"+chatMessage);
										}
									}else if(chatChannel.equals("crew") && client.playerCharacter.getCrew().getId() != 0 && client.playerCharacter.getCrew().getId() == s.playerCharacter.getCrew().getId()){
										addOutGoingMessage(s,"newchat",chatChannel+";"+client.playerCharacter.getName()+";"+chatMessage);
									}else if(chatChannel.equals("announce")){
										addOutGoingMessage(s,"newchat",chatChannel+";"+client.playerCharacter.getName()+";"+chatMessage);
									}else if(chatChannel.equals("party")){
										if(client.playerCharacter.getParty() != null){
											if(client.playerCharacter.getParty().getPlayers().contains(s)){
												addOutGoingMessage(s,"newchat",chatChannel+";"+client.playerCharacter.getName()+";"+chatMessage);
											}
										}
									}
								}
							}
						}
					}else if(!specialCommand){
						addOutGoingMessage(client,"message", "#messages.chat.muted");
					}
					
					// LOG ADMIN COMMANDS
					/*
					if(specialCommand){
						if(client.playerCharacter != null){
							Server.userDB.addChatText("admin",client.playerCharacter.getDBId(),0,chatText);
						}
					}
					*/
				}
				
			}
		}
	}


	public static boolean isVisibleForLocalChat(Client client, int x, int y, int z){
		boolean visible = false;

		if(client.playerCharacter.getZ() == z && Math.abs(client.playerCharacter.getX() - x) < ServerSettings.TILE_HALF_W+20 && Math.abs(client.playerCharacter.getY() - y) < ServerSettings.TILE_HALF_H+20){
			visible = true;
		}

		return visible;
	}

	public static void mutePlayer(Client client, String playerName){
		
		for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
			Client s = entry.getValue();
			if(s.Ready){
				if(s.playerCharacter.getName().toLowerCase().equals(playerName.toLowerCase())){
					if(!mutedUsers.contains(s.UserId)){
						mutedUsers.add(s.UserId);
						addOutGoingMessage(s,"#message","messages.chat.muted");
						addOutGoingMessage(client,"message","You have muted "+s.playerCharacter.getName()+" til next server restart");
						break;
					}
				}
			}
		}
	}
	
}