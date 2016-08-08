package data_handlers.item_handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import utils.ServerGameInfo;
import data_handlers.DataHandlers;
import data_handlers.Handler;
import data_handlers.Message;
import data_handlers.QuestHandler;
import data_handlers.ability_handler.Ability;
import network.Client;
import network.Server;

public class ActionbarHandler extends Handler {
	
	public static void init() {
		DataHandlers.register("add_actionbar", m -> handleAddActionBar(m));
		DataHandlers.register("remove_actionbar", m -> handleRemoveActionBar(m));
	}
	
	public static void handleAddActionBar(Message m) {
		Client client = m.client;
		String actionInfo[] = m.message.split(";");
		String actionType = actionInfo[0];
		int actionId = Integer.parseInt(actionInfo[1]);
		int pos = Integer.parseInt(actionInfo[2]);

		boolean addOk = false;

		int nrItems = 0;

		// CHECK IF ABILITY/ITEM IS OWNED BY PLAYER
		if(actionType.equals("Ability")){

			Ability a = client.playerCharacter.getAbilityById(actionId);

			boolean hasReq = true;

			if(a != null){
				
				if(a.getClassId() > 0){
					if(!client.playerCharacter.hasClass(a.getClassId())){
						hasReq = false;
					}
				}

				if(a.getFamilyId() > 0){
					if(client.playerCharacter.getFamilyId() != a.getFamilyId()){
						hasReq = false;
					}
				}
			}else{
				hasReq = false;
			}

			if(hasReq || client.playerCharacter.getAdminLevel() == 5){
				addOk = true;
			}

		}else if(actionType.equals("Item")){
			addOk = true;

			// CHECK IF PLAYER HAS ITEM AND HAS IT ON THE MOUSE

			Item MouseItem = client.playerCharacter.getMouseItem();

			if(MouseItem != null){
				if(MouseItem.getType().equals("Potion") || MouseItem.getType().equals("Scroll") || MouseItem.getType().equals("Eatable")){

					ResultSet actionCheck = Server.userDB.askDB("select Id, ItemId, Nr from character_item where Id = "+MouseItem.getUserItemId()+" and CharacterId = "+client.playerCharacter.getDBId()+" and InventoryPos = 'Mouse'");

					try {
						if(actionCheck.next()){
							nrItems = actionCheck.getInt("Nr");
							if(MouseItem.getId() != actionCheck.getInt("ItemId")){
								addOk = false;
							}
						}else{
							addOk = false;
						}
						actionCheck.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}	
				}else{
					addOk = false;
				}
			}else{
				addOk = false;
			}
		}		

		if(addOk){
			
			// CHECK IF ACTIONBAR ALREADY HAS SIMILAR ITEM OR ABILITY IN IT
			// OR IF POSITION HAS ITEM/ABILITY ALREADY
			ResultSet actionbarCheck = Server.userDB.askDB("select Id, OrderNr, ActionType, ActionId from character_actionbar where CharacterId = "+client.playerCharacter.getDBId()+" and ((ActionType = '"+actionType+"' and ActionId = "+actionId+") or OrderNr = "+pos+")");

			try {
				while(actionbarCheck.next()){
					// REMOVE ITEM/ABILITY THAT OCCUPIES ACTIONBAR BOX
					Server.userDB.updateDB("delete from character_actionbar where Id = "+actionbarCheck.getInt("Id"));

					if(actionbarCheck.getString("ActionType").equals("Item")){
						int itemId = actionbarCheck.getInt("ActionId");

						// MOVE THE ITEM TO INVENTORY
						InventoryHandler.addItemToInventory(client, new Item(ServerGameInfo.itemDef.get(itemId)));
					}
					addOutGoingMessage(client,"remove_actionbar", ""+actionbarCheck.getInt("OrderNr"));
				}
				actionbarCheck.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}

								
			// REMOVE MOUSE ITEM AND 
			// IF ITEM IS STACKED, PLACE REMAINDER IN INVENTORY
			if(actionType.equals("Item")){
				if(nrItems > 1){
					Server.userDB.updateDB("update character_item set Nr = Nr - 1 where ItemId = "+actionId+" and InventoryPos = 'Mouse' and CharacterId = "+client.playerCharacter.getDBId());

					Item placeItem = new Item(ServerGameInfo.itemDef.get(actionId));
					placeItem.setStacked(nrItems-1);

					InventoryHandler.addItemToInventory(client, placeItem);
				}

				Server.userDB.updateDB("delete from character_item where ItemId = "+actionId+" and InventoryPos = 'Mouse' and CharacterId = "+client.playerCharacter.getDBId());
				client.playerCharacter.setMouseItem(null);
			}

			// SET ITEM TO NEW POS IN ACTIONBAR
			Server.userDB.updateDB("insert into character_actionbar (ActionType, ActionId, CharacterId, OrderNr) values ('"+actionType+"',"+actionId+","+client.playerCharacter.getDBId()+","+pos+")");

			// SEND CONFIRMATION TO CLIENT
			addOutGoingMessage(client, "add_actionbar", actionType+";"+actionId+";"+pos);
		}else{
			// CAN'T ADD TO ACTIONBAR
			addOutGoingMessage(client, "add_actionbar", "no");
		}
	}
	
	public static void handleRemoveActionBar(Message m) {
		Client client = m.client;
		int pos = Integer.parseInt(m.message);

		ResultSet itemInfo = Server.userDB.askDB("select ActionType, ActionId from character_actionbar where CharacterId = "+client.playerCharacter.getDBId()+" and OrderNr = "+pos);
		try {
			if(itemInfo.next()){
				client.playerCharacter.setMouseItem(null);
				if(itemInfo.getString("ActionType").equals("Item")){
					int itemId = itemInfo.getInt("ActionId");

					Item newMouseItem = new Item(ServerGameInfo.itemDef.get(itemId));
					Server.userDB.updateDB("insert into character_item (CharacterId, ItemId, Equipped, InventoryPos, Nr) values ("+client.playerCharacter.getDBId()+","+itemId+",0,'Mouse',1)");
					
					ResultSet mouseItemId = Server.userDB.askDB("select Id from character_item where InventoryPos = 'Mouse' and CharacterId = "+client.playerCharacter.getDBId());
					
					if(mouseItemId.next()){
						newMouseItem.setUserItemId(mouseItemId.getInt("Id"));
					}
					mouseItemId.close();
				
					client.playerCharacter.setMouseItem(newMouseItem);
					addOutGoingMessage(client,"addmouseitem",itemId+";"+newMouseItem.getType()+";0");
				
				}else if(itemInfo.getString("ActionType").equals("Ability")){

				}
			}
			itemInfo.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		Server.userDB.updateDB("delete from character_actionbar where CharacterId = "+client.playerCharacter.getDBId()+" and OrderNr = "+pos);
		addOutGoingMessage(client,"remove_actionbar", ""+pos);
	}
	
	/**
	 * Use item from actionbar
	 */
	public static boolean useItem(Client client, Item usedItem){
		boolean useItemSuccess = false;

		if(usedItem.getType().equals("Scroll")){
			// Send use scroll
			addOutGoingMessage(client, "use_scroll", usedItem.getId()+","+usedItem.getSubType()+",Actionbar");
		}else{
			// CHECK IF ITEM EXIST FIRST IN INVENTORY
			ResultSet rs = Server.userDB.askDB("select Id, InventoryPos, Nr from character_item where ItemId = "+usedItem.getId()+" and InventoryPos <> 'None' and CharacterId = "+client.playerCharacter.getDBId());

			try {
				if(rs.next()){
					// USE ITEM AND REMOVE IT
					int userItemId = rs.getInt("Id");

					usedItem.setUserItemId(userItemId);

					// USEABLE / POTIONS
					if(client.playerCharacter.useItem(usedItem)){
						if(rs.getInt("Nr") > 1){
							Server.userDB.updateDB("update character_item set Nr = Nr - 1 where Id = "+userItemId);
						}else{
							Server.userDB.updateDB("delete from character_item where Id = "+userItemId);
						}

						// SEND REMOVE ITEM FROM INVENTORY
						addOutGoingMessage(client, "inventory_remove", "remove");

						useItemSuccess = true;
						QuestHandler.updateUseItemQuests(client,usedItem.getId());
					}
				}
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}

			if(!useItemSuccess){
				
				// IF NOT, CHECK ACTIONBAR, AND REMOVE IT FROM ACTIONBAR
				rs = Server.userDB.askDB("select ActionId, OrderNr from character_actionbar where ActionType = 'Item' and ActionId = "+usedItem.getId()+" and CharacterId = "+client.playerCharacter.getDBId());
				try {
					if(rs.next()){
						if(client.playerCharacter.useItem(usedItem)){
							Server.userDB.updateDB("delete from character_actionbar where ActionType = 'Item' and ActionId = "+usedItem.getId()+" and CharacterId = "+client.playerCharacter.getDBId());

							// SEND REMOVE ITEM FROM ACTIONBAR
							addOutGoingMessage(client,"remove_actionbar", rs.getInt("OrderNr")+"");
							useItemSuccess = true;
						}
					}
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		
		return useItemSuccess;
	}
}