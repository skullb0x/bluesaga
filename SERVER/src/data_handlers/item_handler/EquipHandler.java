package data_handlers.item_handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import creature.Creature;
import network.Client;
import network.Server;
import data_handlers.Handler;

public class EquipHandler extends Handler {
	
	public static void handleData(Client client, String message){
		if(message.startsWith("<equip>")){
			String equip_info = message.substring(7);

			int itemId = Integer.parseInt(equip_info);

			boolean equipOk = false;
			Item equippedItem = null;

			// CHECK IF PLAYER HAS ITEM THAT WILL BE EQUIPPED
			if(client.playerCharacter.getMouseItem() != null && client.playerCharacter.getMouseItem().getId() == itemId){ 

				ResultSet itemInfo = Server.userDB.askDB("select Id, ItemId from character_item where CharacterId = "+client.playerCharacter.getDBId()+" and InventoryPos = 'Mouse' and ItemId = "+itemId);

				try {
					if(itemInfo.next()){
						equipOk = true;
						equippedItem = client.playerCharacter.getMouseItem(); 
						equippedItem.setUserItemId(itemInfo.getInt("Id"));
					}
					itemInfo.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			if(equipOk){
				// PLAYER HAS ITEM!
				itemId = equippedItem.getId();

				// CHECK IF PLAYER CAN HAVE ITEM
				equipOk = client.playerCharacter.canEquip(equippedItem);
				
				// CHECK IF ITEM IS 2-HANDS AND OFFHAND SLOT IS FREE
				boolean twoHands = false;
				
				if(equipOk){
					if(equippedItem.isTwoHands() && client.playerCharacter.getEquipment("OffHand") != null){
						equipOk = false;
					}
					
					if(equippedItem.getType().equals("OffHand")){
						if(client.playerCharacter.getEquipment("Weapon") != null){
							if(client.playerCharacter.getEquipment("Weapon").isTwoHands()){
								equipOk = false;
							}
						}
					}
					
					if(!equipOk){
						twoHands = true;
						addOutGoingMessage(client,"you_equip_no","This item is two-hands! You can't carry an offhand and a two hands weapon at the same time.");
					}
				}
				
				
				if(equipOk){
						
					client.playerCharacter.setMouseItem(null);

					// CHECK IF PLAYER ALREADY HAS ITEM OF SAME TYPE EQUIPPED
					Item oldEquip = client.playerCharacter.getEquipment(equippedItem.getType());
					if(oldEquip != null){
						oldEquip.unEquip();
						client.playerCharacter.unequipItem(equippedItem.getType());
						client.playerCharacter.setMouseItem(oldEquip);
						Server.userDB.updateDB("update character_item set Equipped = 0, InventoryPos = 'Mouse' where Id = "+oldEquip.getUserItemId());
					}

					// EQUIP NEW
					equippedItem.equip();
					client.playerCharacter.equipItem(equippedItem);
					
					Server.userDB.updateDB("update character_item set Equipped = 1, InventoryPos = 'None' where CharacterId = "+client.playerCharacter.getDBId()+" and Id = "+equippedItem.getUserItemId());

					// SEND INFO ABOUT WHAT EQUIP TO PICK UP TO MOUSE
					int oldEquipId = 0;
					int oldEquipUserItemId = 0;
					String oldEquipType = equippedItem.getType();

					if(oldEquip != null){
						oldEquipId = oldEquip.getId();
						oldEquipUserItemId = oldEquip.getUserItemId();
					}


					// newEquipId; newEquipUserItemId; oldEquipId; oldEquipUserItemId; defboost
					addOutGoingMessage(client,"update_bonusstats",client.playerCharacter.getBonusStatsAsString());
					addOutGoingMessage(client,"you_equip",equippedItem.getId()+";"+equippedItem.getUserItemId()+";"+equippedItem.getType()+";"+equippedItem.getClassId()+";"+oldEquipId+";"+oldEquipUserItemId+";"+oldEquipType+";"+client.playerCharacter.getAttackRange());

					// SEND EQUIP INFO TO OTHER CLIENTS IN AREA
					// SEND INFO ABOUT MOVE TO ALL CLIENTS IN SAME AREA
					String equipInfo = client.playerCharacter.getSmallData()+"/"+equippedItem.getType()+","+equippedItem.getId();

					for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
						Client other = entry.getValue();

						if(other.Ready){
							if(other.playerCharacter.getDBId() != client.playerCharacter.getDBId()){
								if(isVisibleForPlayer(other.playerCharacter,client.playerCharacter.getX(),client.playerCharacter.getY(), client.playerCharacter.getZ())){
									addOutGoingMessage(other,"other_equip",equipInfo);
								}
							}
						}
					}
				}else if(!twoHands){
					addOutGoingMessage(client,"you_equip_no","You don't have the requirements for this item!");
				}
			}
		}

		if(message.startsWith("<unequip>")){
			String unequipType = message.substring(9);
			Item unequippedItem = client.playerCharacter.getEquipment(unequipType);
			if(unequippedItem != null){
				client.playerCharacter.setMouseItem(unequippedItem);
				client.playerCharacter.unequipItem(unequipType);
				Server.userDB.updateDB("update character_item set Equipped = 0, InventoryPos = 'Mouse' where Id = "+unequippedItem.getUserItemId());

				
				// unEquipId; unEquipUserItemId; unEquipType; bonusStats
				addOutGoingMessage(client,"update_bonusstats",client.playerCharacter.getBonusStatsAsString());
				addOutGoingMessage(client,"you_unequip",unequippedItem.getId()+";"+unequippedItem.getUserItemId()+";"+unequippedItem.getType()+";"+client.playerCharacter.getAttackRange());

				// Add item to mouse
				addOutGoingMessage(client, "addmouseitem", unequippedItem.getId()+";"+unequippedItem.getType());
				
				// SEND EQUIP INFO TO OTHER CLIENTS IN AREA
				String equipInfo = client.playerCharacter.getSmallData()+"/"+unequippedItem.getType();

				for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
					Client other = entry.getValue();

					if(other.Ready){
						if(other.playerCharacter.getDBId() != client.playerCharacter.getDBId()){
							if(isVisibleForPlayer(other.playerCharacter,client.playerCharacter.getX(),client.playerCharacter.getY(),client.playerCharacter.getZ())){
								addOutGoingMessage(other,"other_unequip",equipInfo);
							}
						}
					}
				}
			}
		}

		if(message.equals("<statuswindow>info")){
			// EQUIP INFO
			String equipInfo = "";

			int headEquipId = 0;
			int headEquipUserItemId = 0;
			if(client.playerCharacter.getEquipment("Head") != null){
				headEquipId = client.playerCharacter.getEquipment("Head").getId();
				headEquipUserItemId = client.playerCharacter.getEquipment("Head").getUserItemId();
			}
			int weaponEquipId = 0;
			int weaponEquipUserItemId = 0;
			if(client.playerCharacter.getEquipment("Weapon") != null){
				weaponEquipId = client.playerCharacter.getEquipment("Weapon").getId();
				weaponEquipUserItemId = client.playerCharacter.getEquipment("Weapon").getUserItemId();
			}
			int offHandEquipId = 0;
			int offHandEquipUserItemId = 0;
			if(client.playerCharacter.getEquipment("OffHand") != null){
				offHandEquipId = client.playerCharacter.getEquipment("OffHand").getId();
				offHandEquipUserItemId = client.playerCharacter.getEquipment("OffHand").getUserItemId();
			}
			int amuletEquipId = 0;
			int amuletEquipUserItemId = 0;
			if(client.playerCharacter.getEquipment("Amulet") != null){
				amuletEquipId = client.playerCharacter.getEquipment("Amulet").getId();
				amuletEquipUserItemId = client.playerCharacter.getEquipment("Amulet").getUserItemId();
			}
			int artifactEquipId = 0;
			int artifactEquipUserItemId = 0;
			if(client.playerCharacter.getEquipment("Artifact") != null){
				artifactEquipId = client.playerCharacter.getEquipment("Artifact").getId();
				artifactEquipUserItemId = client.playerCharacter.getEquipment("Artifact").getUserItemId();
			}
			equipInfo = headEquipId+","+headEquipUserItemId+","+weaponEquipId+","+weaponEquipUserItemId+","+offHandEquipId+","+offHandEquipUserItemId+","+amuletEquipId+","+amuletEquipUserItemId+","+artifactEquipId+","+artifactEquipUserItemId;

			addOutGoingMessage(client,"statuswindow",equipInfo);
		}
	}
	
	public static void checkRequirements(Client client){
		// INACTIVATED LOSE EQUIP
		int randomLost = 10; 
		Creature TARGET = client.playerCharacter;
		
		/*
		if(client.playerCharacter.getPkMarker() > 0){
			randomLost = RandomUtils.getInt(0, 10);
		}
		*/
		
		Item headItem = TARGET.getEquipment("Head");
		
		if(headItem != null){
			boolean loseItem = false;
			if(!TARGET.canEquip(headItem)){
				loseItem = true;
			}else if(randomLost == 0){
				loseItem = true;
			}

			if(loseItem){
				TARGET.unequipItem("Head");
				Server.userDB.updateDB("delete from character_item where Id = "+headItem.getUserItemId());

				// unEquipId; unEquipUserItemId; unEquipType; bonusStats
				if(client.Ready){
					addOutGoingMessage(client,"you_unequip",headItem.getId()+";"+headItem.getUserItemId()+";"+headItem.getType()+";"+client.playerCharacter.getAttackRange());
				}
				InventoryHandler.addItemToInventory(client, headItem);
			}
		}
		Item weaponItem = TARGET.getEquipment("Weapon");
		if(weaponItem != null){
			boolean loseItem = false;
			if(!TARGET.canEquip(weaponItem)){
				loseItem = true;
			}else if(randomLost == 1){
				loseItem = true;
			}

			if(loseItem){
				TARGET.unequipItem("Weapon");
				Server.userDB.updateDB("delete from character_item where Id = "+weaponItem.getUserItemId());

				// unEquipId; unEquipUserItemId; unEquipType; bonusStats
				if(client.Ready){
					addOutGoingMessage(client,"you_unequip",weaponItem.getId()+";"+weaponItem.getUserItemId()+";"+weaponItem.getType()+";"+client.playerCharacter.getAttackRange());
				}
				InventoryHandler.addItemToInventory(client, weaponItem);
			}
		}
		Item offhandItem = TARGET.getEquipment("OffHand");
		if(offhandItem != null){
			boolean loseItem = false;
			if(!TARGET.canEquip(offhandItem)){
				loseItem = true;
			}else if(randomLost == 2){
				loseItem = true;
			}

			if(loseItem){
				TARGET.unequipItem("OffHand");
				Server.userDB.updateDB("delete from character_item where Id = "+offhandItem.getUserItemId());

				// unEquipId; unEquipUserItemId; unEquipType; bonusStats
				if(client.Ready){
					addOutGoingMessage(client,"you_unequip",offhandItem.getId()+";"+offhandItem.getUserItemId()+";"+offhandItem.getType()+";"+client.playerCharacter.getAttackRange());
				}
				InventoryHandler.addItemToInventory(client, offhandItem);
			}
		}
		Item amuletItem = TARGET.getEquipment("Amulet");
		if(amuletItem != null){
			boolean loseItem = false;
			if(!TARGET.canEquip(amuletItem)){
				loseItem = true;
			}else if(randomLost == 3){
				loseItem = true;
			}

			if(loseItem){
				TARGET.unequipItem("Amulet");
				Server.userDB.updateDB("delete from character_item where Id = "+amuletItem.getUserItemId());

				// unEquipId; unEquipUserItemId; unEquipType; bonusStats
				if(client.Ready){
					addOutGoingMessage(client,"you_unequip",amuletItem.getId()+";"+amuletItem.getUserItemId()+";"+amuletItem.getType()+";"+client.playerCharacter.getAttackRange());
				}
				InventoryHandler.addItemToInventory(client, amuletItem);
			}
		}
		Item artifactItem = TARGET.getEquipment("Artifact");
		if(artifactItem != null){
			boolean loseItem = false;
			if(!TARGET.canEquip(artifactItem)){
				loseItem = true;
			}else if(randomLost == 4){
				loseItem = true;
			}

			if(loseItem){
				TARGET.unequipItem("Artifact");
				Server.userDB.updateDB("delete from character_item where Id = "+artifactItem.getUserItemId());

				// unEquipId; unEquipUserItemId; unEquipType; bonusStats
				if(client.Ready){
					addOutGoingMessage(client,"you_unequip",artifactItem.getId()+";"+artifactItem.getUserItemId()+";"+artifactItem.getType()+";"+client.playerCharacter.getAttackRange());
				}
				InventoryHandler.addItemToInventory(client, artifactItem);
			}
		}
		addOutGoingMessage(client,"update_bonusstats",client.playerCharacter.getBonusStatsAsString());
	}
}