package data_handlers;

import org.newdawn.slick.Color;

import sound.Sfx;
import game.BlueSaga;
import gui.Gui;

public class BountyHandler extends Handler {

	
	public BountyHandler() {
		super();
	}

	public static void handleData(String serverData) {

		if (serverData.startsWith("<mostwanted>")) {
		
			String wantedData = serverData.substring(12);
			
			Gui.mostWantedWindow.load(wantedData);
			Gui.mostWantedWindow.open();
		}else if(serverData.startsWith("<setbounty>")){
			String bountyData = serverData.substring(11);
		
			int bounty = Integer.parseInt(bountyData);
			int bountyChange = bounty - BlueSaga.playerCharacter.getBounty();
			
			Gui.showBountyChange(BlueSaga.playerCharacter.getBounty(),bountyChange);
			BlueSaga.playerCharacter.setBounty(bounty);
			
		}else if(serverData.startsWith("<bountyplaced>")){
			String bountyStatus = serverData.substring(14);
			
			if(bountyStatus.equals("notfound")){
				Gui.BountyWindow.setStatusText("Player not found");
			}else if(bountyStatus.equals("nogold")){
				Gui.BountyWindow.setStatusText("Not enough gold");
			}else{
				String bountyInfo[] = bountyStatus.split(";");
				Sfx.play("notifications/gold");
				Gui.BountyWindow.close();
				Gui.addMessage("#messages.bounty.placed_bounty# "+bountyInfo[0], new Color(50,50,50));
				if(Gui.InventoryWindow.isOpen()){
					BlueSaga.client.sendMessage("inventory", "info");
				}
			}
		}
	}

}
