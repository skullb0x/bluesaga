package data_handlers.item_handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import utils.ServerGameInfo;
import utils.MathUtils;
import utils.RandomUtils;
import data_handlers.DataHandlers;
import data_handlers.Handler;
import data_handlers.Message;
import data_handlers.SkinHandler;
import map.Moveable;
import map.Tile;
import network.Client;
import network.Server;

public class ContainerHandler extends Handler {

	public static HashMap<String,Container> CONTAINERS = new HashMap<String, Container>();

	public static HashMap<String,Moveable> MOVEABLES = new HashMap<String,Moveable>();

	public static void init() {
		CONTAINERS.clear();
		MOVEABLES.clear();
		
		DataHandlers.register("opencontainer", m -> handleOpenContainer(m));
		DataHandlers.register("container_addmouseitem", m -> handleAddMouseItem(m));
		DataHandlers.register("container_placeitem", m -> handlePlaceItem(m));
		DataHandlers.register("fastloot", m -> handleFastLoot(m));
	}
	
	public static void handleOpenContainer(Message m) {
		Client client = m.client;
		String containerInfo[] = m.message.split(",");
	
		if(containerInfo[0].contains("container") || containerInfo[0].contains("gathering")){
			int tileX = Integer.parseInt(containerInfo[1]);
			int tileY = Integer.parseInt(containerInfo[2]);
			int tileZ = Integer.parseInt(containerInfo[3]);
	
			checkContainer(client, Server.WORLD_MAP.getTile(tileX, tileY, tileZ));
		}
	}
	
	public static void handleAddMouseItem(Message m) {
		Client client = m.client;
		String itemInfo[] = m.message.split(";");
	
		String containerId = itemInfo[0];
		String containerPos[] = containerId.split(",");
		int containerX = Integer.parseInt(containerPos[0]);
		int containerY = Integer.parseInt(containerPos[1]);
		int containerZ = Integer.parseInt(containerPos[2]);
	
		String position = itemInfo[1];
	
		addItemFromContainerToMouse(client, containerId, position);
	
		addOutGoingMessage(client,"container_content",getContainerContent(containerX,containerY,containerZ));
	}
	
	public static void handlePlaceItem(Message m) {
		Client client = m.client;
		String itemInfo[] = m.message.split(";");
		String containerId = itemInfo[0];
		String position = itemInfo[1];
	
		String containerPos[] = containerId.split(",");
		int containerX = Integer.parseInt(containerPos[0]);
		int containerY = Integer.parseInt(containerPos[1]);
		int containerZ = Integer.parseInt(containerPos[2]);
	
	
		// CHECK IF USER HAS ITEM ON MOUSE
		if(client.playerCharacter.getMouseItem() != null){
			if(!client.playerCharacter.getMouseItem().getType().equals("Collector Card")){
				if(CONTAINERS.get(containerId) != null){
					Server.userDB.updateDB("delete from character_item where InventoryPos = 'Mouse' and CharacterId = "+client.playerCharacter.getDBId());
	
					Item itemToMove = client.playerCharacter.getMouseItem();
	
					String positionXY[] = position.split(",");
					int posX = Integer.parseInt(positionXY[0]);
					int posY = Integer.parseInt(positionXY[1]);
	
					boolean placeItem = true;
	
					// CHECK IF ITEM ALREADY EXIST AT LOCATION
					if(CONTAINERS.get(containerId).getItems().get(position) != null){
						Item oldItem = CONTAINERS.get(containerId).getItems().get(position);
	
						if(oldItem.getId() == itemToMove.getId() && oldItem.getStacked() < oldItem.getStackable()){
							// IF SAME TYPE OF ITEM THEN STACK THEM
							placeItem = false;
							
							int nrItems = itemToMove.getStacked();
							int nrItemsToAdd = nrItems;
							
							if(nrItems + oldItem.getStacked() > oldItem.getStackable()){
								nrItemsToAdd = oldItem.getStackable() - oldItem.getStacked();
							}
							
							// IF SAME TYPE OF ITEM THEN STACK THEM
							oldItem.setStacked(oldItem.getStacked()+nrItemsToAdd);
							
							
							// IF ITEMS LEFT AFTER FILLING UP STACK
							int nrItemsLeft = nrItems - nrItemsToAdd;
							
							if(nrItemsLeft > 0){
								Server.userDB.updateDB("update character_item set Nr = "+nrItemsLeft+" where ItemId = "+oldItem.getId()+" and InventoryPos = 'Mouse' and CharacterId = "+client.playerCharacter.getDBId());
								client.playerCharacter.getMouseItem().setStacked(nrItemsLeft);
								addOutGoingMessage(client,"addmouseitem", oldItem.getId()+";"+oldItem.getType());
							}else{
								Server.userDB.updateDB("delete from character_item where ItemId = "+oldItem.getId()+" and InventoryPos = 'Mouse' and CharacterId = "+client.playerCharacter.getDBId());
								client.playerCharacter.setMouseItem(null);
							}
							
						}else{
							// SWITCH ITEMS
							addItemFromContainerToMouse(client,containerId,position);
						}
					}
	
					
					if(placeItem){
						CONTAINERS.get(containerId).addItemAtPos(posX, posY, itemToMove);
					}
	
					addOutGoingMessage(client,"container_content",getContainerContent(containerX,containerY,containerZ));
				}
			}else{
				addOutGoingMessage(client,"message","#messages.cards.not_chest");
			}
		}
	}
	
	public static void handleFastLoot(Message m) {
		Client client = m.client;
		String containerInfo[] = m.message.split(",");
		int containerX = Integer.parseInt(containerInfo[0]);
		int containerY = Integer.parseInt(containerInfo[1]);
		int containerZ = Integer.parseInt(containerInfo[2]);
	
		int itemX = Integer.parseInt(containerInfo[3]);
		int itemY = Integer.parseInt(containerInfo[4]);
	
		if(CONTAINERS.containsKey(containerX+","+containerY+","+containerZ)){
			if(CONTAINERS.get(containerX+","+containerY+","+containerZ).getItems() != null){
				if(CONTAINERS.get(containerX+","+containerY+","+containerZ).getItems().get(itemX+","+itemY) != null){
					// REMOVE FROM CONTAINER
					Item newItem = CONTAINERS.get(containerX+","+containerY+","+containerZ).getItems().get(itemX+","+itemY);
					CONTAINERS.get(containerX+","+containerY+","+containerZ).getItems().remove(itemX+","+itemY);
	
					// SEND NEW CONTAINER CONTENT TO PLAYER
					String content = getContainerContent(containerX,containerY,containerZ);
					addOutGoingMessage(client, "container_content",content);
					
					InventoryHandler.addItemToInventory(client, newItem);
				}
			}
		}
	}

	public static void addItemFromContainerToMouse(Client client, String containerId, String position){
		if(CONTAINERS.get(containerId) != null){
			if(CONTAINERS.get(containerId).getItems().get(position) != null){

				Item mouseItem = CONTAINERS.get(containerId).getItems().get(position);

				if(mouseItem != null){
					String positionXY[] = position.split(",");
					int posX = Integer.parseInt(positionXY[0]);
					int posY = Integer.parseInt(positionXY[1]);
					
					CONTAINERS.get(containerId).removeItem(posX, posY);
					addItemToMouse(client, mouseItem);
					
				}
			}				
		}
	}
	
	
	public static void addItemToMouse(Client client, Item mouseItem){
		// ADD ITEM TO PLAYER MOUSE
		Server.userDB.updateDB("insert into character_item (CharacterId, ItemId, Equipped, InventoryPos, Nr, ModifierId, MagicId) values ("+client.playerCharacter.getDBId()+","+mouseItem.getId()+",0,'Mouse',"+mouseItem.getStacked()+","+mouseItem.getModifierId()+","+mouseItem.getMagicId()+")");
				
		ResultSet newItemInfo = Server.userDB.askDB("select Id from character_item where CharacterId = "+client.playerCharacter.getDBId()+" order by Id desc limit 1");
		try {
			if(newItemInfo.next()){
				mouseItem.setUserItemId(newItemInfo.getInt("Id"));
				client.playerCharacter.setMouseItem(mouseItem);
				addOutGoingMessage(client,"addmouseitem",mouseItem.getId()+";"+mouseItem.getType());
			}
			newItemInfo.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public static String getContainerContent(int tileX, int tileY, int tileZ){
		// GET CONTENT IN CONTAINER
		Container TheContainer = CONTAINERS.get(tileX+","+tileY+","+tileZ);

		if(TheContainer != null){
			StringBuilder content = new StringBuilder(1000);
			content.append(TheContainer.getName()).append(';')
				     .append(TheContainer.getType()).append(';')
				     .append(TheContainer.getSizeW()).append(';')
				     .append(TheContainer.getSizeH()).append(';')
				     .append(tileX).append(',')
				     .append(tileY).append(',')
				     .append(tileZ).append(':');

			for (Entry<String, Item> entry : TheContainer.getItems().entrySet()) {
				Item item = entry.getValue();
				if (item != null) {
					content.append(item.getId()).append(',')
				         .append(item.getStacked()).append(',')
				         .append(entry.getKey()).append(',')
				         .append(item.getUserItemId()).append(';');
				}
			}
			return content.toString();
		} else {
			return "None";
		}
	}

	public static void checkContainer(Client client, Tile TILE){
		if(TILE != null && client.playerCharacter != null){
			String objectId = TILE.getObjectId();

			// CHECK IF CONTAINER EXIST AT POSITION
			if(objectId.contains("container") || objectId.contains("gathering")){

				// CHECK IF PLAYER IS CLOSE ENOUGH
				if(Math.abs(TILE.getX() - client.playerCharacter.getX()) + Math.abs(TILE.getY() - client.playerCharacter.getY()) <= 2){
					
					int dX = client.playerCharacter.getX() - TILE.getX();
					int dY = client.playerCharacter.getY() - TILE.getY();
					
					float angle = MathUtils.angleBetween(dX, dY);
					
					client.playerCharacter.setRotation(angle);
					client.playerCharacter.setGotoRotation(angle);
					
					
					if(objectId.contains("chestplayer")){
						// PERSONAL CHEST

						String content = getPersonalChestContent(client);

						addOutGoingMessage(client, "open_container", client.playerCharacter.getSmallData()+";"+objectId+","+TILE.getX()+","+TILE.getY()+","+TILE.getZ()+","+client.playerCharacter.getAttackSpeed());
						addOutGoingMessage(client, "container_content",content);
					}else if(objectId.contains("chest")){
						// RED CHESTS
						boolean openAlready = false;

						// CHECK PLAYER HAS OPENED CHEST BEFORE
						ResultSet chestInfo = Server.userDB.askDB("select ContainerId from character_container where ContainerId = "+TILE.getContainerId()+" and CharacterId = "+client.playerCharacter.getDBId());
						try {
							if(chestInfo.next()){

								openAlready = true;

							}
							chestInfo.close();
						} catch (SQLException e) {
							e.printStackTrace();
						}

						if(!openAlready){
							// GET ITEMS

							// GIVE ITEMS AUTOMATICALLY TO PLAYER
							// GET LOOT FROM CHEST
							ResultSet chestLootInfo = Server.mapDB.askDB("select Items from area_container where Id = "+TILE.getContainerId());


							try {
								if(chestLootInfo.next()){
									if(!chestLootInfo.getString("Items").equals("")){

										String allLoot[] = chestLootInfo.getString("Items").split(",");

										for(String loot: allLoot){
											int itemId = Integer.parseInt(loot);

											Item droppedItem = new Item(ServerGameInfo.itemDef.get(itemId));

											InventoryHandler.addItemToInventory(client, droppedItem);
										}
										
									}

									// SAVE CHEST AS OPENED FOR PLAYER
									Server.userDB.updateDB("insert into character_container (CharacterId, ContainerId) values ("+client.playerCharacter.getDBId()+","+TILE.getContainerId()+")");
									addOutGoingMessage(client,"open_container", client.playerCharacter.getSmallData()+";container/chest,"+TILE.getX()+","+TILE.getY()+","+TILE.getZ()+","+client.playerCharacter.getAttackSpeed());
								}
								chestLootInfo.close();
							} catch (NumberFormatException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (SQLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}

					}else if(objectId.contains("closet")){
						// Closet for item skin change

						String content = SkinHandler.getClosetContent(client);
						addOutGoingMessage(client, "open_container", client.playerCharacter.getSmallData()+";"+objectId+","+TILE.getX()+","+TILE.getY()+","+TILE.getZ()+","+client.playerCharacter.getAttackSpeed());

						addOutGoingMessage(client, "closet_content",content);

					}else if(objectId.contains("mirror")){
						// Mirror for character skin change
						String content = SkinHandler.getCharacterSkins(client);
						addOutGoingMessage(client,"character_skins",content);
						
					}else{
						if(!objectId.contains("open")){
							// If container is closed, open it

							TILE.setObjectId(objectId+"_open");

							// Check if container exists in memory
							if(CONTAINERS.get(TILE.getX()+","+TILE.getY()+","+TILE.getZ()) == null){
								
								// RANDOMIZE CONTENT IN CONTAINER
								Container newContainer = new Container(objectId);

								// GET LOOT IN AREA
								int areaEffectId = client.playerCharacter.getAreaEffectId();
								String areaItems = "None";
								int areaCopper = 0;
								
								ResultSet areaEffectInfo = Server.mapDB.askDB("select AreaItems, AreaCopper from area_effect where Id = "+areaEffectId);
								try {
									if(areaEffectInfo.next()){

										areaItems = areaEffectInfo.getString("AreaItems");
										areaCopper = areaEffectInfo.getInt("AreaCopper");

										if(TILE.getZ() <= -100){
											//
										}else if(!areaItems.equals("None")){
											int dropItemChance = RandomUtils.getInt(0, 100);

											if(dropItemChance < 20){
												String itemIds[] = areaItems.split(",");
												for(String itemId: itemIds){
													int id = Integer.parseInt(itemId);
													newContainer.addItem(new Item(ServerGameInfo.itemDef.get(id)));
												}
											}
										}

										int dropCopperChance = RandomUtils.getInt(0, 100);

										if(dropCopperChance < 40){

											if(areaCopper > 0){
												areaCopper = RandomUtils.getInt(0, areaCopper);

												CoinConverter cc = new CoinConverter(areaCopper);

												if(cc.getGold() > 0){
													Item GoldItem = new Item(ServerGameInfo.itemDef.get(34));
													GoldItem.setStacked(cc.getGold());
													newContainer.addItem(GoldItem);
												}
												if(cc.getSilver() > 0){
													Item SilverItem = new Item(ServerGameInfo.itemDef.get(35));
													SilverItem.setStacked(cc.getSilver());
													newContainer.addItem(SilverItem);
												}
												if(cc.getCopper() > 0){
													Item CopperItem = new Item(ServerGameInfo.itemDef.get(36));
													CopperItem.setStacked(cc.getCopper());
													newContainer.addItem(CopperItem);
												}
											}
										}
									}
									areaEffectInfo.close();
								} catch (NumberFormatException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (SQLException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								CONTAINERS.put(TILE.getX()+","+TILE.getY()+","+TILE.getZ(), newContainer);
							}


							for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
								Client other = entry.getValue();

								if(other.Ready){
									if(isVisibleForPlayer(other.playerCharacter,TILE.getX(),TILE.getY(),TILE.getZ())){
										addOutGoingMessage(other, "open_container", client.playerCharacter.getSmallData()+";"+objectId+","+TILE.getX()+","+TILE.getY()+","+TILE.getZ()+","+client.playerCharacter.getAttackSpeed());
									}
								}	
							}

						}
						
						
						// GET CONTENT IN CONTAINER
						String content = getContainerContent(TILE.getX(),TILE.getY(),TILE.getZ());
						addOutGoingMessage(client, "container_content",content);
					}		
				}

			}

		}
	}

	public static void addItemToContainer(Item newItem, int tileX, int tileY, int tileZ){
		if(CONTAINERS.get(tileX+","+tileY+","+tileZ) != null){
			CONTAINERS.get(tileX+","+tileY+","+tileZ).addItem(newItem);
		}else{
			Container newC = new Container("container/smallbag");
			newC.addItem(newItem);
			CONTAINERS.put(tileX+","+tileY+","+tileZ,newC);
		}
	}


	public static String getPersonalChestContent(Client client){
		// GET CONTENT OF PERSONAL CHEST

		int chestSize = client.chestSize;
		
		ResultSet chestInfo = Server.userDB.askDB("select Id, ItemId, Nr, Pos from user_chest where UserId = "+client.UserId);
		
		
		StringBuilder content = new StringBuilder(1000);
		content.append("Personal Chest;chest;")
		       .append(chestSize).append(';')
		       .append(chestSize)
		       .append(";0,0,0:");

		try {
			while(chestInfo.next()){
				content.append(chestInfo.getInt("ItemId")).append(',')
				       .append(chestInfo.getInt("Nr")).append(',')
				       .append(chestInfo.getString("Pos")).append(',')
				       .append(chestInfo.getInt("Id")).append(';');
			}
			chestInfo.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return content.toString();
	}

	public static void checkContainerRespawn(){

		// CHECK MOVEABLE RESPAWNS

		HashMap<String, Moveable> newMoveables = new HashMap<String, Moveable>();

		for(Iterator<Entry<String, Moveable>> iter = MOVEABLES.entrySet().iterator();iter.hasNext();){ 
			Entry<String, Moveable> entry = iter.next();

			int posX = entry.getValue().getX();
			int posY = entry.getValue().getY();
			int posZ = entry.getValue().getZ();

			boolean respawnOk = true;

			/*
			// Check that no players are nearby
			for (Entry<Integer, Client> entry2 : Server.clients.entrySet()) {
				Client other = entry2.getValue();

				if(other.Ready){
					if(isVisibleForPlayer(other.playerCharacter,posX,posY,posZ)){
						respawnOk = false;
						break;
					}
				}	
			}
			*/
			
			if(respawnOk){
				if(!entry.getValue().checkRespawn()){
					newMoveables.put(entry.getKey(), entry.getValue());
				}else{
					// RESPAWN MOVEABLE
					Server.WORLD_MAP.getTile(posX, posY, posZ).setObjectId("None");
					Server.WORLD_MAP.getTile(entry.getValue().getSpawnX(), entry.getValue().getSpawnY(), entry.getValue().getSpawnZ()).setObjectId(entry.getValue().getName());
	
					newMoveables.put(entry.getValue().getSpawnX()+","+entry.getValue().getSpawnY()+","+entry.getValue().getSpawnZ(), new Moveable(entry.getValue().getName(),entry.getValue().getSpawnX(),entry.getValue().getSpawnY(),entry.getValue().getSpawnZ()));
	
					// SEND MOVE BACK TO SPAWN TO CLIENTS
					for (Entry<Integer, Client> entry3 : Server.clients.entrySet()) {
						Client other = entry3.getValue();
	
						if(other.Ready){
							if(isVisibleForPlayer(other.playerCharacter,posX,posY,posZ) || isVisibleForPlayer(other.playerCharacter,entry.getValue().getSpawnX(),entry.getValue().getSpawnY(),entry.getValue().getSpawnZ())){
								addOutGoingMessage(other, "reset_moveable", entry.getValue().getName()+";"+posX+","+posY+","+posZ+";"+entry.getValue().getSpawnX()+","+entry.getValue().getSpawnY()+","+entry.getValue().getSpawnZ());
							}
						}	
					}	
				}
			}else{
				newMoveables.put(entry.getKey(), entry.getValue());
			}
		}

		MOVEABLES = newMoveables;


		// CHECK CONTAINER RESPAWNS

		HashMap<String, Container> newContainers = new HashMap<String, Container>();

		for(Iterator<Entry<String, Container>> iter = CONTAINERS.entrySet().iterator();iter.hasNext();){ 
			Entry<String, Container> entry = iter.next();

			String pos[] = entry.getKey().split(",");
			int posX = Integer.parseInt(pos[0]);
			int posY = Integer.parseInt(pos[1]);
			int posZ = Integer.parseInt(pos[2]);

			boolean respawnOk = true;

			if(!entry.getValue().getType().contains("harvest")){
				for (Entry<Integer, Client> entry2 : Server.clients.entrySet()) {
					Client other = entry2.getValue();

					if(other.Ready){
						if(isVisibleForPlayer(other.playerCharacter,posX,posY,posZ)){
							respawnOk = false;
							break;
						}
					}	
				}
			}

			if(respawnOk){
				if(!entry.getValue().checkRespawn()){

					if(entry.getValue().getType().contains("smallbag") && entry.getValue().getItems().size() == 0){
						// IF EMPTY BAG REMOVE ANYWAY
						Server.WORLD_MAP.getTile(posX, posY, posZ).setObjectId("None");
					}else{
						newContainers.put(entry.getKey(), entry.getValue());
					}
				}else{

					String objectId = Server.WORLD_MAP.getTile(posX, posY, posZ).getObjectId();

					if(objectId.contains("smallbag")){
						// REMOVE SMALL BAG
						Server.WORLD_MAP.getTile(posX, posY, posZ).setObjectId("None");
					}else{
						if(objectId.contains("open")){
							objectId = objectId.substring(0,objectId.length()-5);
						}

						Server.WORLD_MAP.getTile(posX, posY, posZ).setObjectId(objectId);



						for (Entry<Integer, Client> entry3 : Server.clients.entrySet()) {
							Client other = entry3.getValue();

							if(other.Ready){
								if(isVisibleForPlayer(other.playerCharacter,posX,posY,posZ)){
									addOutGoingMessage(other, "reset_container", objectId+","+entry.getKey());
								}
							}	
						}	
					}
				}
			}else{
				newContainers.put(entry.getKey(), entry.getValue());
			}
		}

		CONTAINERS = newContainers;

	}

	public static HashMap<String, Container> getContainers(){
		return CONTAINERS;
	}
}
