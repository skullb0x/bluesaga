package data_handlers;

import game.ServerSettings;
import network.Client;
import network.Server;

import java.sql.ResultSet;
import java.sql.SQLException;
import utils.WebHandler;
import creature.PlayerCharacter;

public class BountyHandler extends Handler {

	private static int baseGain = 10;
	
	public static void init() {
		DataHandlers.register("getmostwanted", m -> handleMostWanted(m));
	}
	
	public static void handleMostWanted(Message m) {
		Client client = m.client;
		ResultSet rs = Server.userDB.askDB("select Name, Bounty from user_character where Bounty > 0 order by Bounty desc limit 10");
	
		StringBuilder wantedData = new StringBuilder(1000);
		
		try {
			while(rs.next()){
				wantedData.append(rs.getString("Name")).append(',')
				          .append(rs.getInt("Bounty")).append(';');
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		if (wantedData.length()>0) {
			addOutGoingMessage(client,"mostwanted",wantedData.toString());
		} else {
			addOutGoingMessage(client,"mostwanted","none");
		}
	}
	
		/*
		if(message.startsWith("<placebounty>")){
			String bountyInfo[] = message.substring(13).split(";");
			String targetName = bountyInfo[0];
			int bountySum = Integer.parseInt(bountyInfo[1]);
			
			// CHECK IF PLAYER HAS THE MONEY
			
			if(bountySum > 0 && client.playerCharacter.hasCopper(bountySum)){
				
				// CHECK IF PLAYER EXIST
				ResultSet playerCheck = Server.userDB.askDB("select Id, Bounty, Name from user_character where lower(Name) = '"+targetName+"'");
				try {
					if(playerCheck.next()){
						// PLAYER EXISTS
						// PLACE BOUNTY
						int totalBounty = playerCheck.getInt("Bounty")+bountySum;
						
						int CharacterId = playerCheck.getInt("Id");
						
						Server.userDB.updateDB("update user_character set Bounty = "+totalBounty+" where Id = "+CharacterId);
						
						if(!Server.DEV_MODE){
							WebHandler.callUrl("http://www.bluesaga.org/server/updateBounty.php?id="+CharacterId+"&bounty="+totalBounty);
						}
						
						InventoryHandler.removeCopperFromInventory(client,bountySum);
						
						for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
							Client s = entry.getValue();
		 
							if(s.Ready){
								if(s.playerCharacter.getDBId() == CharacterId){
									addOutGoingMessage(s,"bountychange",""+bountySum);
									break;
								}
							}
						}
						addOutGoingMessage(client, "bountyplaced", playerCheck.getString("Name")+";"+bountySum);
					}else{
						addOutGoingMessage(client, "bountyplaced", "notfound");
					}
					playerCheck.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}else{
				addOutGoingMessage(client, "bountyplaced", "nogold");
			}
		}
		 */
	
	public static void changeBounty(PlayerCharacter victim, PlayerCharacter killer){
		int victimBounty = victim.getBounty();
		int killerBounty = killer.getBounty();
		
		// Level and Bounty difference affect bounty gain and loss
		float levelFac = (float) victim.getLevel() / (float) killer.getLevel();
		float bountyFac = (victim.getBounty()) / (killer.getBounty() + 1.0f) / (baseGain * 2.0f);
		
		int bountyChange = Math.round(baseGain * levelFac + bountyFac);
		
		victimBounty -= bountyChange;
		if(victimBounty < 0){
			victimBounty = 0;
		}
		killerBounty += bountyChange;
		if(killerBounty < 0){
			killerBounty = 0;
		}
		
		victim.setBounty(victimBounty);
		Server.userDB.updateDB("update user_character set Bounty = "+victimBounty+" where Id = "+victim.getDBId());
		
		killer.setBounty(killerBounty);
		Server.userDB.updateDB("update user_character set Bounty = "+killerBounty+" where Id = "+killer.getDBId());
		
		if(!ServerSettings.DEV_MODE){
			WebHandler.callUrl("http://www.bluesaga.org/server/updateBounty.php?id="+victim.getDBId()+"&bounty="+victimBounty);
			WebHandler.callUrl("http://www.bluesaga.org/server/updateBounty.php?id="+killer.getDBId()+"&bounty="+killerBounty);
		}
		
		if(victim.client.Ready){
			addOutGoingMessage(victim.client,"setbounty",""+victimBounty);
		}
		if(killer.client.Ready){
			addOutGoingMessage(killer.client,"setbounty",""+killerBounty);
		}
	}
}
