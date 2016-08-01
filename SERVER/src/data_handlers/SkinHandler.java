package data_handlers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import utils.ServerGameInfo;
import data_handlers.item_handler.Item;
import network.Client;
import network.Server;

public class SkinHandler extends Handler {

	public static void init() {
	}
	
	public static String getClosetContent(Client client){
		StringBuilder content = new StringBuilder(1000);
		
		// GET CONTENT OF CLOSET
		ResultSet closetInfo = Server.userDB.askDB("select ItemId from character_skin where SkinType = 'Item' and CharacterId = "+client.playerCharacter.getDBId());
		
		int sizeW = 5;
		int sizeH = 7;
		
		content.append("Closet,")
		       .append(sizeW).append(',')
		       .append(sizeH).append('/');
		
		try {
			while(closetInfo.next()){
				Item skinItem = new Item(ServerGameInfo.itemDef.get(closetInfo.getInt("ItemId")));
				
				content.append(skinItem.getId()).append(',')
				       .append(skinItem.getName()).append(',')
				       .append(skinItem.getSubType()).append(';');
			}
			closetInfo.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		content.append("184,Remove,All;");
		
		return content.toString();
	}
	
	public static String getCharacterSkins(Client client){
		StringBuilder skins = new StringBuilder(1000);
		ResultSet closetInfo = Server.userDB.askDB("select ItemId from character_skin where CharacterId = "+client.playerCharacter.getDBId()+" and SkinType = 'Character'");
		
		try {
			while(closetInfo.next()){
				ResultSet skinInfo = Server.gameDB.askDB("select Name from creature where Id = "+closetInfo.getInt("ItemId"));
				
				if(skinInfo.next()){
					skins.append(closetInfo.getInt("ItemId")).append(',')
					     .append(skinInfo.getString("Name")).append(';');
				}
				skinInfo.close();
			}
			if(skins.length() > 0){
				skins.setLength(skins.length() - 1);
			}
			closetInfo.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return skins.toString();
	}
	
	public static void handleData(Client client, String message){
		if(message.startsWith("<set_skin>")){
			String skinInfo = message.substring(10);
			int skinId = Integer.parseInt(skinInfo);
			
			if(skinId == 184){
				// REMOVE ALL SKINS
				client.playerCharacter.getCustomization().setHeadSkinId(0);
				client.playerCharacter.getCustomization().setWeaponSkinId(0);
				client.playerCharacter.getCustomization().setOffHandSkinId(0);
				client.playerCharacter.getCustomization().setAmuletSkinId(0);
				client.playerCharacter.getCustomization().setArtifactSkinId(0);
				
				client.playerCharacter.saveInfo();
				
				// SEND SKIN CHANGE TO PLAYERS IN AREA
				for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
					Client s = entry.getValue();
		
					if(s.Ready && isVisibleForPlayer(s.playerCharacter,client.playerCharacter.getX(),client.playerCharacter.getY(),client.playerCharacter.getZ())){
						addOutGoingMessage(s,"set_skin",client.playerCharacter.getSmallData()+";Remove,0");
					}
				}
			}else{
				// CHECK IF PLAYER OWNS SKIN
				ResultSet checkSkin = Server.userDB.askDB("select ItemId from character_skin where ItemId = "+skinId);
				
				try {
					if(checkSkin.next()){
						Item skinItem = new Item(ServerGameInfo.itemDef.get(skinId));
						
						if(skinItem.getType().equals("Skin")){
							if(skinItem.getSubType().equals("Head")){
								client.playerCharacter.getCustomization().setHeadSkinId(skinId);
							}else if(skinItem.getSubType().equals("Weapon")){
								client.playerCharacter.getCustomization().setWeaponSkinId(skinId);
							}else if(skinItem.getSubType().equals("OffHand")){
								client.playerCharacter.getCustomization().setOffHandSkinId(skinId);
							}else if(skinItem.getSubType().equals("Amulet")){
								client.playerCharacter.getCustomization().setAmuletSkinId(skinId);
							}else if(skinItem.getSubType().equals("Artifact")){
								client.playerCharacter.getCustomization().setArtifactSkinId(skinId);
							}
							
							client.playerCharacter.saveInfo();
							
							// SEND SKIN CHANGE TO PLAYERS IN AREA
							for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
								Client s = entry.getValue();
		
								if(s.Ready && isVisibleForPlayer(s.playerCharacter,client.playerCharacter.getX(),client.playerCharacter.getY(),client.playerCharacter.getZ())){
									addOutGoingMessage(s,"set_skin",client.playerCharacter.getSmallData()+";"+skinItem.getSubType()+","+skinItem.getId());
								}
							}
						}
					}
					checkSkin.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}else if(message.startsWith("<change_character_skin>")){
			int skinId = Integer.parseInt(message.substring(23));
			
			// Check if character has skin
			ResultSet skinCheck = Server.userDB.askDB("select ItemId from character_skin where SkinType = 'Character' and ItemId = "+skinId);
			try {
				if(skinCheck.next()){
					client.playerCharacter.setCreatureId(skinId);
					Server.userDB.updateDB("update user_character set CreatureId = "+skinId+" where Id = "+client.playerCharacter.getDBId());
					
					// Send to clients skin change
					for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
						Client s = entry.getValue();

						if(s.Ready && isVisibleForPlayer(s.playerCharacter,client.playerCharacter.getX(),client.playerCharacter.getY(),client.playerCharacter.getZ())){
							addOutGoingMessage(s,"set_character_skin",client.playerCharacter.getSmallData()+";"+skinId);
						}
					}
				}
				
				skinCheck.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else if(message.startsWith("<reset_character_skin>")){
			client.playerCharacter.setCreatureId(client.playerCharacter.getBaseCreatureId());
			
			Server.userDB.updateDB("update user_character set CreatureId = BaseCreatureId where Id = "+client.playerCharacter.getDBId());
			
			// Send to clients skin change
			for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
				Client s = entry.getValue();

				if(s.Ready && isVisibleForPlayer(s.playerCharacter,client.playerCharacter.getX(),client.playerCharacter.getY(),client.playerCharacter.getZ())){
					addOutGoingMessage(s,"set_character_skin",client.playerCharacter.getSmallData()+";"+client.playerCharacter.getBaseCreatureId());
				}
			}
		}
	}
}
