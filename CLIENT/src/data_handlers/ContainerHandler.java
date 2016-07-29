package data_handlers;

import sound.Sfx;
import utils.MathUtils;
import components.Item;
import map.ScreenObject;
import map.TileObject;
import screens.ScreenHandler;
import creature.Creature;
import game.BlueSaga;
import gui.Gui;

public class ContainerHandler extends Handler {

	public ContainerHandler() {
		super();
		// TODO Auto-generated constructor stub
	}

	public static void handleData(String serverData){
		/*
		if(serverData.startsWith("<inventory>")){
		}
		*/
		
		if(serverData.startsWith("<open_container>")){
			String open_info[] = serverData.substring(16).split(";");
			
			Creature user = MapHandler.addCreatureToScreen(open_info[0]);
			
			String cont_info[] = open_info[1].split(",");
			String containerName = cont_info[0];
			int tileX = Integer.parseInt(cont_info[1]);
			int tileY = Integer.parseInt(cont_info[2]);
			int tileZ = Integer.parseInt(cont_info[3]);
			Float.parseFloat(cont_info[4]);
			
			int dX = tileX - user.getX();
			int dY = tileY - user.getY();
			
			float rotation = MathUtils.angleBetween(-dX, -dY);
			
			user.setRotationNow(rotation);
			
			user.attackTarget(dX, dY, 150);
			
			// CHECK IF YOU ARE THE ONE OPENING
			if(user.getDBId() == BlueSaga.playerCharacter.getDBId() && !containerName.contains("closet")){
				Gui.InventoryWindow.clearItems();
				BlueSaga.client.sendMessage("inventory", "info");
				Gui.InventoryWindow.open();
			}
			
			String container_sfxname = "chest";
			
			if(containerName.contains("container")){
				if(containerName.contains("chestplayer")){
					container_sfxname = "chest";
				}else if(containerName.contains("bag")){
					container_sfxname = "bag";
				}else if(containerName.contains("chest")){
					container_sfxname = "chest";
				}else if(containerName.contains("barrel")){
					container_sfxname = "barrel";
				}
			}else if(containerName.contains("gathering")){
				container_sfxname = "herbs";
			}
			
			
			
			Sfx.play("battle/"+container_sfxname+"_open");
			
			
			ScreenObject newContainer = new ScreenObject();
			newContainer.setObjectCreature(new TileObject(containerName+"_open",tileX,tileY,tileZ));
			ScreenHandler.SCREEN_OBJECTS_WITH_ID.put(tileX+","+tileY+","+tileZ, newContainer);
			MapHandler.updateScreenObjects();
		}
		
		
		if(serverData.startsWith("<container_content>")){
			String contentMsg = serverData.substring(19);
			
			if(!contentMsg.equals("None")){
				String contentInfo[] = contentMsg.split(":");
				
				String containerSize[] = contentInfo[0].split(";");
				
				String name = containerSize[0];
				//String type = containerSize[1];
				int sizeW = Integer.parseInt(containerSize[2]);
				int sizeH = Integer.parseInt(containerSize[3]);
				
				String containerXYZ = containerSize[4];
				
				
				
				int cX = 150;
				int cY = 300 - sizeH*50 + 100;

				if(name.equals("Personal Chest")){
					cX = 20;
					cY = 90;
				}
				
				Gui.ContainerWindow.init(name,containerXYZ,cX,cY,sizeW,sizeH);
				
				if(contentInfo.length > 1){
					String items[] = contentInfo[1].split(";");
					
					for(String itemInfo: items){
						String itemPos[] = itemInfo.split(",");
						int itemId = Integer.parseInt(itemPos[0]);
						int nrStacked = Integer.parseInt(itemPos[1]);
						int posX = Integer.parseInt(itemPos[2]);
						int posY = Integer.parseInt(itemPos[3]);
						int userItemId = Integer.parseInt(itemPos[4]);
						
						Item lootItem = new Item(itemId);
						lootItem.setUserItemId(userItemId);
						
						if(Gui.ContainerWindow.addItemAtPos(lootItem, posX, posY)){
							Gui.ContainerWindow.getBox(posX,posY).setNumber(nrStacked);
						}
						
					}
				}
				
				Gui.ContainerWindow.open();

				if(Gui.InventoryWindow.isOpen()){
					Gui.InventoryWindow.clearItems();
					BlueSaga.client.sendMessage("inventory", "info");
				}
			}
			
			BlueSaga.actionServerWait = false;
		}
		
		// LOOTBAG
		if(serverData.startsWith("<droploot>")){
			String objectInfo[] = serverData.substring(10).split(",");
			Sfx.play("battle/bag_open");
			
			String objectId = objectInfo[0];
			int lootX = Integer.parseInt(objectInfo[1]);
			int lootY = Integer.parseInt(objectInfo[2]);
			int lootZ = Integer.parseInt(objectInfo[3]);
			
			MapHandler.addObjectToScreen(lootX,lootY,lootZ,objectId);
			MapHandler.updateScreenObjects();
		}
		
		
		if(serverData.startsWith("<reset_container>")){
			String pos[] = serverData.substring(17).split(",");
			String objectId = pos[0];
			int posX = Integer.parseInt(pos[1]);
			int posY = Integer.parseInt(pos[2]);
			int posZ = Integer.parseInt(pos[3]);
			
			if(ScreenHandler.SCREEN_OBJECTS_WITH_ID.get(posX+","+posY+","+posZ) != null){
				if(objectId.contains("bag")){
					ScreenHandler.SCREEN_OBJECTS_WITH_ID.remove(posX+","+posY+","+posZ);
					MapHandler.updateScreenObjects();
				}else{
					ScreenHandler.SCREEN_OBJECTS_WITH_ID.get(posX+","+posY+","+posZ).setObjectCreature(new TileObject(objectId,posX,posY,posZ));
				}
			}
		}
		
	}
}
