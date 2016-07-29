package data_handlers;

import network.Client;
import network.Server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import utils.ServerGameInfo;
import data_handlers.ability_handler.Ability;
import data_handlers.item_handler.CoinConverter;
import data_handlers.item_handler.InventoryHandler;
import data_handlers.item_handler.Item;

public class ShopHandler extends Handler {

	public static void init() {
	}

	public static void handleData(Client client, String message){
		if(client.playerCharacter !=  null){
			if(message.startsWith("<shop>")){
				int shopId = Integer.parseInt(message.substring(6));

				String shopItemInfo = "None";
				String shopAbilitiesInfo = "";

				// CHECK IF SHOP EXIST AND HAS ITEMS
				boolean foundShop = false;

				ResultSet rs = Server.gameDB.askDB("select NpcId, Items, Abilities from shop where Id = "+shopId);

				int npcId = 0;
				String npcName = "";

				try {
					if(rs.next()){
						shopItemInfo = rs.getString("Items");
						String shopAbilities = rs.getString("Abilities");
						npcId = rs.getInt("NpcId");

						// GET NPC INFO
						ResultSet npcInfo = Server.mapDB.askDB("select Name from area_creature where Id = "+npcId);

						try {
							if(npcInfo.next()){
								npcName = npcInfo.getString("Name");
							}
							npcInfo.close();
						} catch (SQLException e) {
							e.printStackTrace();
						}	


						if(!shopAbilities.equals("None")){
							String abilityIds[] = shopAbilities.split(",");
							for(String aId: abilityIds){
								int aIdInt = Integer.parseInt(aId);
								ResultSet abilityInfo = Server.gameDB.askDB("select ClassId, GraphicsNr from ability where Id = "+aIdInt);
								if(abilityInfo.next()){
									String color = "0,0,0";
									if(abilityInfo.getInt("ClassId") > 0){
										color = ServerGameInfo.classDef.get(abilityInfo.getInt("ClassId")).bgColor;
									}
									shopAbilitiesInfo += aId+","+color+","+abilityInfo.getInt("GraphicsNr")+":";
								}
								abilityInfo.close();
							}
						}else{
							shopAbilitiesInfo = "None";
						}


						foundShop = true;
					}
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}

				if(foundShop){
					addOutGoingMessage(client,"shop",npcName+";"+shopItemInfo+";"+shopAbilitiesInfo);
					InventoryHandler.sendInventoryInfo(client);
				}
			}


			if(message.startsWith("<buy>")){
				String buyInfo[] = message.substring(5).split(";");
				String buyType = buyInfo[0];
				int itemId = Integer.parseInt(buyInfo[1]);

				client.playerCharacter.loadInventory();
				if(buyType.equals("Item")){
					ResultSet rs = Server.gameDB.askDB("select Value, Name, Type, SubType from item where Id = "+itemId);
					try {
						if(rs.next()){
							if(rs.getString("Type").equals("Customization")){
								// CHECK IF PLAYER CAN AFFORD IT
								if(client.playerCharacter.hasCopper(rs.getInt("Value"))){
									// CHECK IF USER IS PREMIUM
									InventoryHandler.removeCopperFromInventory(client,rs.getInt("Value"));

									if(rs.getString("SubType").equals("Mouth Feature")){
										client.playerCharacter.getCustomization().setMouthFeatureId(itemId);
										Server.userDB.updateDB("update user_character set MouthFeatureId = "+itemId+" where Id = "+client.playerCharacter.getDBId());
									}else if(rs.getString("SubType").equals("Accessories")){
										client.playerCharacter.getCustomization().setAccessoriesId(itemId);
										Server.userDB.updateDB("update user_character set AccessoriesId = "+itemId+" where Id = "+client.playerCharacter.getDBId());
									}else if(rs.getString("SubType").equals("Skin Feature")){
										client.playerCharacter.getCustomization().setSkinFeatureId(itemId);
										Server.userDB.updateDB("update user_character set SkinFeatureId = "+itemId+" where Id = "+client.playerCharacter.getDBId());
									}else if(rs.getString("SubType").equals("Remove")){
										client.playerCharacter.getCustomization().setSkinFeatureId(0);
										client.playerCharacter.getCustomization().setAccessoriesId(0);
										client.playerCharacter.getCustomization().setMouthFeatureId(0);
										Server.userDB.updateDB("update user_character set SkinFeatureId = 0, AccessoriesId = 0, MouthFeatureId = 0 where Id = "+client.playerCharacter.getDBId());
									}

									for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
										Client other = entry.getValue();

										if(other.Ready){
											if(isVisibleForPlayer(other.playerCharacter,client.playerCharacter.getX(),client.playerCharacter.getY(), client.playerCharacter.getZ())){
												addOutGoingMessage(other,"newcustomize",client.playerCharacter.getSmallData()+";"+rs.getString("SubType")+","+itemId);
											}
										}
									}
								}else {
									addOutGoingMessage(client,"shoperror","nogold");
								}
							}else if(rs.getString("Type").equals("Money")){
								// EXCHANGE MONEY VALUES
								if(itemId == 34){
									// GOLD
									// REMOVE 100 SILVER
									if(InventoryHandler.countPlayerItem(client, 35) >= 100){
										InventoryHandler.removeNumberOfItems(client, 35, 100);
										InventoryHandler.addItemToInventory(client, new Item(ServerGameInfo.itemDef.get(34)));
									}else{
										addOutGoingMessage(client,"message","#messages.shop.not_enough_silver");
									}
								}else if(itemId == 35){
									// SILVER
									// REMOVE 100 COPPER
									if(InventoryHandler.countPlayerItem(client, 36) >= 100){
										InventoryHandler.removeNumberOfItems(client, 36, 100);
										InventoryHandler.addItemToInventory(client, new Item(ServerGameInfo.itemDef.get(35)));
								
									}else{
										addOutGoingMessage(client,"message","#messages.shop.not_enough_copper");
									}

								}else if(itemId == 36){
									// COPPER
									if(InventoryHandler.countPlayerItem(client, 35) >= 1){
										// REMOVE ONE SILVER
										InventoryHandler.removeNumberOfItems(client, 35, 1);
										Item copperCoins = new Item(ServerGameInfo.itemDef.get(36));
										copperCoins.setStacked(100);
										InventoryHandler.addItemToInventory(client, copperCoins);
									}else if(InventoryHandler.countPlayerItem(client, 34) >= 1){
										// REMOVE ONE GOLD
										InventoryHandler.removeNumberOfItems(client, 34, 1);
										Item silverCoins = new Item(ServerGameInfo.itemDef.get(35));
										silverCoins.setStacked(99);
										InventoryHandler.addItemToInventory(client, silverCoins);
										Item copperCoins = new Item(ServerGameInfo.itemDef.get(36));
										silverCoins.setStacked(100);
										InventoryHandler.addItemToInventory(client, copperCoins);
									}else{
										addOutGoingMessage(client,"message","#messages.shop.no_money");
									}
								}

							}else{

								// CHECK IF PLAYER CAN AFFORD IT
								if(client.playerCharacter.hasCopper(rs.getInt("Value"))){
									// CHECK IF INVENTORY ISN'T FULL
									if(!client.playerCharacter.isInventoryFull(new Item(ServerGameInfo.itemDef.get(itemId)))){
										// ADD ITEM AND REMOVE GOLD TO PLAYER
										InventoryHandler.removeCopperFromInventory(client,rs.getInt("Value"));
										InventoryHandler.addItemToInventory(client,new Item(ServerGameInfo.itemDef.get(itemId)));
									
										addOutGoingMessage(client,"buy","item/"+rs.getString("Name")+"/"+rs.getInt("Value"));
									}else{
										addOutGoingMessage(client,"shoperror","inventoryfull");
									}
								}else {
									addOutGoingMessage(client,"shoperror","nogold");
								}
							}
						}else{
							addOutGoingMessage(client,"shoperror","noitem");
						}
						rs.close();
					} catch (SQLException e) {
						e.printStackTrace();
						addOutGoingMessage(client,"shoperror","error");
					}
				}else if(buyType.equals("Ability")){
					int abilityId = itemId;
					Ability shopAbility = new Ability(ServerGameInfo.abilityDef.get(abilityId));

					// CHECK IF PLAYER CAN AFFORD IT
					if(client.playerCharacter.hasCopper(shopAbility.getPrice())){
						// CHECK IF PLAYER HAS THE ABILITY ALREADY
						if(client.playerCharacter.hasAbility(shopAbility.getName()) == 9999){

							// CHECK IF PLAYER HAS ABILITY REQUIREMENTS
							boolean hasReq = true;
						
							if(shopAbility.getClassId() > 0){
								if(!client.playerCharacter.hasClass(shopAbility.getClassId())){
									hasReq = false;
								}else{
									// Check class level
									if(client.playerCharacter.getClassById(shopAbility.getClassId()) != null){
										if(client.playerCharacter.getClassById(shopAbility.getClassId()).level < shopAbility.getClassLevel()){
											hasReq = false;
										}
									}else{
										hasReq = false;
									}
								}
							}

							if(shopAbility.getFamilyId() > 0){
								if(shopAbility.getFamilyId() != client.playerCharacter.getFamilyId()){
									hasReq = false;
								}
							}

							if(hasReq){
								// ADD ABILITY AND REMOVE GOLD TO PLAYER
								Server.userDB.updateDB("insert into character_ability (CharacterId, AbilityId, CooldownLeft) values ("+client.playerCharacter.getDBId()+","+abilityId+",0)");
								InventoryHandler.removeCopperFromInventory(client,shopAbility.getPrice());
								client.playerCharacter.loadInventory();
								client.playerCharacter.loadAbilities();

								Ability A = client.playerCharacter.getAbilityById(abilityId);
								if(A != null){
									String AbilityInfo = A.getAbilityId()+"="+A.getName()+"="+A.getClassId()+"="+A.getColor().getRed()+"="+A.getColor().getGreen()+"="+A.getColor().getBlue()+"="+A.getManaCost()+"="+A.getCooldown()+"="+A.getCooldownLeft()+"="+A.getRange()+"="+A.getPrice()+"="+A.isTargetSelf()+"="+A.isInstant()+"="+A.getEquipReq()+"="+A.getGraphicsNr()+"="+A.getAoE();
									addOutGoingMessage(client,"buy","ability/"+A.getName()+"/"+shopAbility.getPrice()+"/"+AbilityInfo);
								}
							}else{
								addOutGoingMessage(client,"shoperror","noreq");
							}
						}else{
							addOutGoingMessage(client,"shoperror","haveability");
						}
					}else {
						addOutGoingMessage(client,"shoperror","nogold");
					}
				}else{
					addOutGoingMessage(client,"shoperror","noability");
				}
				InventoryHandler.sendInventoryInfo(client);

			}

			if(message.startsWith("<sell>")){
				int itemId = 0;
				Item soldItem = null;

				boolean sellFromMouse = false;
				String invPos = "";
				
				if(message.contains("mouse")){
					String itemInfo[] = message.substring(6).split(",");
					itemId = Integer.parseInt(itemInfo[1]);
					soldItem = client.playerCharacter.getMouseItem();
					sellFromMouse = true;
				}else{
					invPos = message.substring(6);

					// CHECK IF PLAYER HAS ITEM
					ResultSet rs = Server.userDB.askDB("select ItemId, ModifierId, MagicId, Nr from character_item where InventoryPos = '"+invPos+"' and CharacterId = "+client.playerCharacter.getDBId());

					try {
						if(rs.next()){
							itemId = rs.getInt("ItemId");
							soldItem = new Item(ServerGameInfo.itemDef.get(itemId));
							soldItem.setModifierId(rs.getInt("ModifierId"));
							soldItem.setMagicId(rs.getInt("MagicId"));
							soldItem.setStacked(rs.getInt("Nr"));
						}
						rs.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}


				if(soldItem != null){
					int value = soldItem.getSoldValue(); 


					// CHECK IF ITEM IS SELLABLE
					if(soldItem.isSellable()){
						// CHECK IF PLAYER IS AT LEAST LVL 2
						if(client.playerCharacter.getLevel() > 1){

							// REMOVE ITEM
							if(sellFromMouse){
								Server.userDB.updateDB("delete from character_item where InventoryPos = 'Mouse' and CharacterId = "+client.playerCharacter.getDBId());
								value *= client.playerCharacter.getMouseItem().getStacked();
								client.playerCharacter.setMouseItem(null);
								addOutGoingMessage(client,"clearmouse","");
							}else if(!invPos.equals("")){
								if(soldItem.getStacked() > 1){
									Server.userDB.updateDB("update character_item set Nr = Nr - 1 where InventoryPos = '"+invPos+"' and CharacterId = "+client.playerCharacter.getDBId());
								}else{
									Server.userDB.updateDB("delete from character_item where InventoryPos = '"+invPos+"' and CharacterId = "+client.playerCharacter.getDBId());
								}
							}
							
							// ADD GOLD TO PLAYER
							CoinConverter cc = new CoinConverter(value);

							if(cc.getGold() > 0){
								Item GoldItem = new Item(ServerGameInfo.itemDef.get(34));
								GoldItem.setStacked(cc.getGold());
								InventoryHandler.addItemToInventory(client,GoldItem);
							}
							if(cc.getSilver() > 0){
								Item SilverItem = new Item(ServerGameInfo.itemDef.get(35));
								SilverItem.setStacked(cc.getSilver());
								InventoryHandler.addItemToInventory(client,SilverItem);
							}
							if(cc.getCopper() > 0){
								Item CopperItem = new Item(ServerGameInfo.itemDef.get(36));
								CopperItem.setStacked(cc.getCopper());
								InventoryHandler.addItemToInventory(client,CopperItem);
							}

						}else{
							addOutGoingMessage(client,"shoperror","lowlevel");
						}
					}else{
						addOutGoingMessage(client,"shoperror","notwanted");
					}
				}
			}
		}
	}
}