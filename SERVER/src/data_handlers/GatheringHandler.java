package data_handlers;

import java.util.Map;

import utils.ServerGameInfo;
import utils.RandomUtils;
import data_handlers.item_handler.Container;
import data_handlers.item_handler.ContainerHandler;
import data_handlers.item_handler.Item;
import map.Tile;
import network.Client;
import network.Server;

public class GatheringHandler extends Handler {

	public static void init() {
	}
	
	public static void handleData(Client client, String serverData){
		if(serverData.startsWith("<gathering>")){
			if(client.playerCharacter != null){
				String gatheringInfo[] = serverData.substring(11).split(",");
				
				int tileX = Integer.parseInt(gatheringInfo[0]);
				int tileY = Integer.parseInt(gatheringInfo[1]);
				int tileZ = Integer.parseInt(gatheringInfo[2]);
				
				// CHECK TILE
				Tile SourceTile = Server.WORLD_MAP.getTile(tileX, tileY, tileZ);
				
				if(SourceTile != null){
					// CHECK IF TILE IS A RESOURCE
					if(SourceTile.getObjectId().contains("gathering")){
						// CHECK THAT RESOURCE ISNT USED
						if(!SourceTile.getObjectId().contains("_open")){
							// CHECK IF PLAYER IS NEXT TO IT
							if(client.playerCharacter.getZ() == tileZ){
								int dist = (int) Math.floor(Math.sqrt(Math.pow(tileX - client.playerCharacter.getX(),2) + Math.pow(tileY - client.playerCharacter.getY(),2)));
								if(dist < 2){
									
									// GET RESOURCE NAME
									String resourceName = SourceTile.getObjectId().substring(10);
									
									// ORANGA BUSH
									if(resourceName.equals("oranga")){
										generateHarvest(client, "Oranga Bush", SourceTile.getObjectId(), tileX, tileY, tileZ, 102, 178);
									}else if(resourceName.equals("piccoberries")){
										generateHarvest(client, "Picco Berries", SourceTile.getObjectId(), tileX, tileY, tileZ, 102, 203);
									}else if(resourceName.equals("flowerluna")){
										generateHarvest(client, "Luna Petal", SourceTile.getObjectId(), tileX, tileY, tileZ, 102, 212);
									}else if(resourceName.equals("soulbush")){
										generateHarvest(client, "Soul Bush", SourceTile.getObjectId(), tileX, tileY, tileZ, 102, 313);
									}
								}
							}
						}else{
							//IF USED GET HARVEST THAT MAY BE LEFT
							if(ContainerHandler.CONTAINERS.get(tileX+","+tileY+","+tileZ) != null){
								// GET CONTENT IN CONTAINER
								String content = ContainerHandler.getContainerContent(tileX,tileY,tileZ);
								addOutGoingMessage(client, "container_content",content);
							}				
						}
					}
				}
			}
		}
	}
	
	public static void generateHarvest(Client client, String SourceName, String objectId, int tileX, int tileY, int tileZ, int skillId, int resourceId){
		int skillLevel = client.playerCharacter.getSkill(skillId).getLevel();
		
		boolean canHarvest = true;
		int nrGathered = RandomUtils.getInt(0,(int) Math.ceil(skillLevel/1.5f));
		
		if(nrGathered == 0){
			// 80% chance to get something anyway
			int chance = RandomUtils.getInt(1, 100);
			if(chance <= 85){
				nrGathered = 1;
			}
		}
		
		if(nrGathered > 5){
			nrGathered = 5;
		}
		
		canHarvest = false;
		if(SourceName.equals("Soul Bush")){
			if(skillLevel > 1){
				canHarvest = true;
			}
		}else if(SourceName.equals("Luna Petal")){
			if(skillLevel > 4){
				nrGathered = 1;
				canHarvest = true;
			}
		}else{
			canHarvest = true;
		}
		
		
		if(canHarvest){
			Tile TILE = Server.WORLD_MAP.getTile(tileX, tileY, tileZ);
			
			TILE.setObjectId(objectId+"_open");
			
			// CHECK IF CONTAINER ALREADY IN MEMORY
			if(ContainerHandler.CONTAINERS.get(TILE.getX()+","+TILE.getY()+","+TILE.getZ()) == null){
				// IF NOT GENERATE HARVEST
				SkillHandler.gainSP(client, skillId, false);
				
				Container newContainer = new Container("harvest");
			
				for(int i = 0; i < nrGathered; i++){
					newContainer.addItem(new Item(ServerGameInfo.itemDef.get(resourceId)));
				}
				
				newContainer.setName(SourceName);
				
				ContainerHandler.CONTAINERS.put(TILE.getX()+","+TILE.getY()+","+TILE.getZ(), newContainer);
			}
			
			for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
				Client other = entry.getValue();
			
				if(other.Ready){
					if(isVisibleForPlayer(other.playerCharacter,TILE.getX(),TILE.getY(),TILE.getZ())){
						addOutGoingMessage(other, "open_container", client.playerCharacter.getSmallData()+";"+objectId+","+TILE.getX()+","+TILE.getY()+","+TILE.getZ()+","+client.playerCharacter.getAttackSpeed());
					}
				}	
			}
			
			// GET CONTENT IN CONTAINER
			String content = ContainerHandler.getContainerContent(TILE.getX(),TILE.getY(),TILE.getZ());
			addOutGoingMessage(client, "container_content",content);
		}else{
			addOutGoingMessage(client, "message", "#messages.gathering.need_higher_level");
		}
	}
}
