package data_handlers.chat_handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import utils.RandomUtils;
import utils.TimeUtils;
import data_handlers.DataHandlers;
import data_handlers.Handler;
import data_handlers.MapHandler;
import data_handlers.Message;
import data_handlers.monster_handler.MonsterHandler;
import game.ServerSettings;
import network.Client;
import network.Server;

public class ChatHandler extends Handler {

	private static Set<String> Emoticons = new TreeSet<String>();
	private static String emoHelp = "To show emoticon type '/emo name'.";
	public static Collection<String> BadWords = new ArrayList<String>();
	public static Collection<String> BadSubWords = new ArrayList<String>();
	private static List<String> CuteWords = new ArrayList<String>();

	private static Set<Integer> mutedUsers = new HashSet<Integer>();
	
	
	public static void init() {

		mutedUsers.clear();
		
		Emoticons.add("angry");
		Emoticons.add("fail");
		Emoticons.add("joke");
		Emoticons.add("lol");
		Emoticons.add("love");
		Emoticons.add("sad");
		Emoticons.add("smile");
		Emoticons.add("weird");

		StringBuilder sb = new StringBuilder(1000);
		sb.append(emoHelp).append(" Available emoticons are: ");
		for(String emo: Emoticons){
			sb.append(emo).append(", ");
		}
		sb.setLength(sb.length()-2);
		emoHelp = sb.toString();
		
		ResultSet badWordsInfo = Server.gameDB.askDB("select Word from bad_words");
		try {
			while(badWordsInfo.next()){
				String bw = badWordsInfo.getString("Word").toLowerCase();
				BadWords.add(bw);
				BadSubWords.add(bw + " ");
				BadSubWords.add(" " + bw);
				BadSubWords.add(" " + bw + " ");
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
		
		DataHandlers.register("newchat", m -> handleNewChat(m));
	}
	
	public static void handleNewChat(Message m) {
		if(m.client.playerCharacter == null) return;
		Client client = m.client;
		boolean specialCommand = false;
		
		String chatInfo[] = m.message.split(";");
		
		if(chatInfo.length == 2){
			String chatChannel = chatInfo[0].toLowerCase();
			String chatText = chatInfo[1];

			chatText = chatText.replace("'", "");
			String chatLower = chatText.toLowerCase();
			
			if(chatText.startsWith("/")){
				specialCommand = true;
			}
			
			// SPECIAL REFRESH
			if(chatLower.equals("/r")){
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
			if(chatLower.startsWith("/emo")){
				specialCommand = true;

				if(chatLower.equals("/emo")){
					addOutGoingMessage(client,"message",emoHelp);
				}else{
					String emoticon = chatLower.substring(5);

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

			if(chatLower.startsWith("/channels")){
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
			
			if(chatLower.startsWith("/rolldice")){
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
			
			
			if(chatLower.startsWith("/quit")){
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
				chatLower = chatMessage.toLowerCase();

				for(String badword: BadWords){
					if(chatLower.equals(badword)){
						chatMessage = RandomUtils.getAny(CuteWords);
					}else if(chatLower.startsWith(badword)){
						String randomCuteWord = RandomUtils.getAny(CuteWords);
						chatMessage = randomCuteWord + chatMessage.substring(badword.length());
					}else if(chatLower.endsWith(badword)){
						String randomCuteWord = RandomUtils.getAny(CuteWords);
						chatMessage = chatMessage.substring(0,chatMessage.length()-badword.length()) + randomCuteWord;
					}
				}
				for(String badword: BadSubWords){
					int found = chatLower.indexOf(badword);
					if(found>=0){
						String randomCuteWord = RandomUtils.getAny(CuteWords);
						int end = found + badword.length();
						if(badword.startsWith(" ")){
							++ found;
						}
						if(badword.endsWith(" ")){
							-- end;
						}
						chatMessage = chatMessage.substring(0,found) + randomCuteWord + chatMessage.substring(end);
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