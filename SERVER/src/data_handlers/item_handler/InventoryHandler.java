package data_handlers.item_handler;

import java.sql.ResultSet;
import java.sql.SQLException;

import utils.ServerGameInfo;
import network.Client;
import network.Server;
import data_handlers.DataHandlers;
import data_handlers.Handler;
import data_handlers.Message;
import data_handlers.QuestHandler;

public class InventoryHandler extends Handler {

  public static void init() {
    DataHandlers.register("inventory", m -> handleInventory(m));
    DataHandlers.register("addmouseitem", m -> handleAddMouseItem(m));
    DataHandlers.register("splitmouse", m -> handleSplitMouse(m));
    DataHandlers.register("moveitem", m -> handleMoveItem(m));
  }

  public static void handleInventory(Message m) {
    Client client = m.client;
    sendInventoryInfo(client);
  }

  public static void handleAddMouseItem(Message m) {
    Client client = m.client;
    String itemInfo[] = m.message.split(";");
    int posX = Integer.parseInt(itemInfo[0]);
    int posY = Integer.parseInt(itemInfo[1]);

    // 0 = Inventory, 1 = Personal Chest
    int containerType = Integer.parseInt(itemInfo[2]);

    if (containerType == 0) {
      // INVENTORY
      // GET ITEM AT posX, posY IN PLAYER'S INVENTORY
      ResultSet rs =
          Server.userDB.askDB(
              "select Id, ItemId, Nr, ModifierId, MagicId from character_item where InventoryPos = '"
                  + posX
                  + ","
                  + posY
                  + "' and CharacterId = "
                  + client.playerCharacter.getDBId());

      try {
        if (rs.next()) {

          // PLAYER HAS ITEM AT THAT POSITION
          Server.userDB.updateDB(
              "update character_item set InventoryPos = 'Mouse' where Id = " + rs.getInt("Id"));

          if (ServerGameInfo.itemDef.containsKey(rs.getInt("ItemId"))) {
            Item mouseItem = new Item(ServerGameInfo.itemDef.get(rs.getInt("ItemId")));
            mouseItem.setUserItemId(rs.getInt("Id"));
            mouseItem.setStacked(rs.getInt("Nr"));
            mouseItem.setModifierId(rs.getInt("ModifierId"));
            mouseItem.setMagicId(rs.getInt("MagicId"));

            client.playerCharacter.setMouseItem(mouseItem);

            addOutGoingMessage(
                client, "addmouseitem", rs.getInt("ItemId") + ";" + mouseItem.getType());

            if (containerType == 0) {
              // SEND INVENTORY INFO
              client.playerCharacter.loadInventory();
              sendInventoryInfo(client);
            } else {
              // SEND PERSONAL CHEST INFO
              String content = ContainerHandler.getPersonalChestContent(client);
              addOutGoingMessage(client, "container_content", content);
            }
          }
        }
        rs.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    } else if (containerType == 1) {
      // PERSONAL CHEST
      // GET ITEM AT posX, posY IN PLAYER'S PERSONAL CHEST
      ResultSet rs =
          Server.userDB.askDB(
              "select Id, ItemId, Nr, ModifierId, MagicId from user_chest where Pos = '"
                  + posX
                  + ","
                  + posY
                  + "' and UserId = "
                  + client.UserId);

      try {
        if (rs.next()) {

          // PLAYER HAS ITEM AT THAT POSITION

          if (ServerGameInfo.itemDef.containsKey(rs.getInt("ItemId"))) {
            Item mouseItem = new Item(ServerGameInfo.itemDef.get(rs.getInt("ItemId")));
            mouseItem.setUserItemId(rs.getInt("Id"));
            mouseItem.setStacked(rs.getInt("Nr"));
            mouseItem.setModifierId(rs.getInt("ModifierId"));
            mouseItem.setMagicId(rs.getInt("MagicId"));

            // ADD ITEM TO MOUSE
            Server.userDB.updateDB(
                "insert into character_item (CharacterId, ItemId, Equipped, InventoryPos, Nr, ModifierId, MagicId) values("
                    + client.playerCharacter.getDBId()
                    + ","
                    + mouseItem.getId()
                    + ",0,'Mouse',"
                    + mouseItem.getStacked()
                    + ","
                    + mouseItem.getModifierId()
                    + ","
                    + mouseItem.getMagicId()
                    + ")");

            client.playerCharacter.setMouseItem(mouseItem);

            // REMOVE ITEM FROM PERSONAL CHEST
            Server.userDB.updateDB(
                "delete from user_chest where Id = " + mouseItem.getUserItemId());

            addOutGoingMessage(
                client, "addmouseitem", rs.getInt("ItemId") + ";" + mouseItem.getType());

            if (containerType == 0) {
              // SEND INVENTORY INFO
              client.playerCharacter.loadInventory();
              sendInventoryInfo(client);
            } else {
              // SEND PERSONAL CHEST INFO
              String content = ContainerHandler.getPersonalChestContent(client);
              addOutGoingMessage(client, "container_content", content);
            }
          }
        }
        rs.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }

  public static void handleSplitMouse(Message m) {
    Client client = m.client;
    boolean okToSplit = false;

    if (client.playerCharacter.getMouseItem() == null) {
      String splitInfo[] = m.message.split(";");
      String invPos = splitInfo[0];

      int number = 0;
      try {
        number = Integer.parseInt(splitInfo[1]);
        number = (int) Math.floor(number);
      } catch (NumberFormatException e) {
        number = 0;
      }

      if (number > 0) {
        //CHECK IF ITEM ON POSITION IN INVENTORY
        ResultSet itemCheck =
            Server.userDB.askDB(
                "select Id, ItemId, Nr from character_item where InventoryPos = '"
                    + invPos
                    + "' and CharacterId = "
                    + client.playerCharacter.getDBId());

        try {
          if (itemCheck.next()) {
            // CHECK IF ENOUGH IN NUMBER
            if (itemCheck.getInt("Nr") > number) {
              // SPLIT
              Server.userDB.updateDB(
                  "update character_item set Nr = Nr - "
                      + number
                      + " where Id = "
                      + itemCheck.getInt("Id"));
              Server.userDB.updateDB(
                  "insert into character_item (CharacterId, ItemId, Equipped, InventoryPos, Nr) values ("
                      + client.playerCharacter.getDBId()
                      + ","
                      + itemCheck.getInt("itemId")
                      + ",0,'Mouse',"
                      + number
                      + ")");
              okToSplit = true;
            } else if (itemCheck.getInt("Nr") == number) {
              // MOVE ALL TO MOUSE
              Server.userDB.updateDB(
                  "update character_item set InventoryPos = 'Mouse' where Id = "
                      + itemCheck.getInt("Id"));
              okToSplit = true;
            }
            if (okToSplit) {
              if (ServerGameInfo.itemDef.containsKey(itemCheck.getInt("ItemId"))) {
                Item mouseItem = new Item(ServerGameInfo.itemDef.get(itemCheck.getInt("ItemId")));
                mouseItem.setStacked(number);
                client.playerCharacter.setMouseItem(mouseItem);
                client.playerCharacter.loadInventory();
                sendInventoryInfo(client);
                addOutGoingMessage(
                    client, "addmouseitem", mouseItem.getId() + ";" + mouseItem.getType());
              }
            }
          }
          itemCheck.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public static void handleMoveItem(Message m) {
    Client client = m.client;
    String itemInfo[] = m.message.split(";");
    int posX = Integer.parseInt(itemInfo[1]);
    int posY = Integer.parseInt(itemInfo[2]);

    // 0 = Inventory, 1 = Personal Chest
    int containerType = Integer.parseInt(itemInfo[3]);

    // CHECK IF USER HAS ITEM ON MOUSE
    if (client.playerCharacter.getMouseItem() != null) {

      Item itemToMove = client.playerCharacter.getMouseItem();

      int oldInvItemUserItemId = 0;
      int oldInvItemId = 0;
      int nrItems = 1;

      ResultSet mouseItemInfo =
          Server.userDB.askDB(
              "select Nr, ModifierId, MagicId from character_item where CharacterId = "
                  + client.playerCharacter.getDBId()
                  + " and InventoryPos = 'Mouse'");
      try {
        if (mouseItemInfo.next()) {
          nrItems = mouseItemInfo.getInt("Nr");
          itemToMove.setStacked(nrItems);
          itemToMove.setMagicId(mouseItemInfo.getInt("MagicId"));
          itemToMove.setModifierId(mouseItemInfo.getInt("ModifierId"));
        }
        mouseItemInfo.close();
      } catch (SQLException e1) {
        e1.printStackTrace();
      }

      if (containerType == 0) {
        // IF INVENTORY

        boolean addSpecialItem = addSpecialItemToInventory(client, itemToMove);

        if (!addSpecialItem) {
          // CHECK IF ITEM ALREADY EXIST AT LOCATION
          ResultSet checkInvPos =
              Server.userDB.askDB(
                  "select Id, ItemId, Nr from character_item where CharacterId = "
                      + client.playerCharacter.getDBId()
                      + " and InventoryPos = '"
                      + posX
                      + ","
                      + posY
                      + "'");
          try {
            if (checkInvPos.next()) {
              // ITEM ALREADY THERE!

              oldInvItemUserItemId = checkInvPos.getInt("Id");
              oldInvItemId = checkInvPos.getInt("ItemId");
              Item oldInvItem = new Item(ServerGameInfo.itemDef.get(oldInvItemId));
              oldInvItem.setStacked(checkInvPos.getInt("Nr"));

              if (oldInvItem.getId() == itemToMove.getId()
                  && oldInvItem.getStacked() < oldInvItem.getStackable()) {

                int nrItemsToAdd = nrItems;

                if (nrItems + oldInvItem.getStacked() > oldInvItem.getStackable()) {
                  nrItemsToAdd = oldInvItem.getStackable() - oldInvItem.getStacked();
                }

                // IF SAME TYPE OF ITEM THEN STACK THEM
                Server.userDB.updateDB(
                    "update character_item set Nr = Nr + "
                        + nrItemsToAdd
                        + " where ItemId = "
                        + oldInvItemId
                        + " and InventoryPos = '"
                        + posX
                        + ","
                        + posY
                        + "' and CharacterId = "
                        + client.playerCharacter.getDBId());

                // IF ITEMS LEFT AFTER FILLING UP STACK
                int nrItemsLeft = nrItems - nrItemsToAdd;

                if (nrItemsLeft > 0) {
                  Server.userDB.updateDB(
                      "update character_item set Nr = "
                          + nrItemsLeft
                          + " where ItemId = "
                          + oldInvItemId
                          + " and InventoryPos = 'Mouse' and CharacterId = "
                          + client.playerCharacter.getDBId());
                  client.playerCharacter.getMouseItem().setStacked(nrItemsLeft);
                  addOutGoingMessage(
                      client, "addmouseitem", oldInvItemId + ";" + oldInvItem.getType());
                } else {
                  Server.userDB.updateDB(
                      "delete from character_item where ItemId = "
                          + oldInvItemId
                          + " and InventoryPos = 'Mouse' and CharacterId = "
                          + client.playerCharacter.getDBId());
                  client.playerCharacter.setMouseItem(null);
                }

                nrItems = oldInvItem.getStacked() + nrItemsToAdd;

              } else {
                // MOVE IT TO NEW POSITION
                Server.userDB.updateDB(
                    "update character_item set InventoryPos = '"
                        + posX
                        + ","
                        + posY
                        + "' where InventoryPos = 'Mouse' and CharacterId = "
                        + client.playerCharacter.getDBId());

                // PUT EXISTING ITEM TO MOUSE
                addOutGoingMessage(
                    client, "addmouseitem", oldInvItemId + ";" + oldInvItem.getType());

                Server.userDB.updateDB(
                    "update character_item set InventoryPos = 'Mouse' where Id = "
                        + oldInvItemUserItemId);
                client.playerCharacter.setMouseItem(oldInvItem);
              }
            } else {
              // FREE SPACE!

              // MOVE IT TO NEW POSITION
              Server.userDB.updateDB(
                  "update character_item set InventoryPos = '"
                      + posX
                      + ","
                      + posY
                      + "' where InventoryPos = 'Mouse' and CharacterId = "
                      + client.playerCharacter.getDBId());
              client.playerCharacter.setMouseItem(null);
            }
            checkInvPos.close();
          } catch (SQLException e) {
            e.printStackTrace();
          }

          addOutGoingMessage(
              client,
              "moveitem",
              itemToMove.getId()
                  + ";"
                  + itemToMove.getUserItemId()
                  + ";"
                  + posX
                  + ";"
                  + posY
                  + ";"
                  + nrItems
                  + ";"
                  + containerType);
        }
      } else if (containerType == 1) {
        // PERSONAL CHEST
        // CHECK IF ITEM ALREADY EXIST AT LOCATION
        ResultSet checkInvPos =
            Server.userDB.askDB(
                "select Id, ItemId, Nr, ModifierId, MagicId from user_chest where UserId = "
                    + client.UserId
                    + " and Pos = '"
                    + posX
                    + ","
                    + posY
                    + "'");
        try {
          if (checkInvPos.next()) {
            // ITEM ALREADY THERE!
            oldInvItemUserItemId = checkInvPos.getInt("Id");
            oldInvItemId = checkInvPos.getInt("ItemId");
            Item oldInvItem = new Item(ServerGameInfo.itemDef.get(oldInvItemId));
            oldInvItem.setStacked(checkInvPos.getInt("Nr"));
            oldInvItem.setModifierId(checkInvPos.getInt("ModifierId"));
            oldInvItem.setMagicId(checkInvPos.getInt("MagicId"));

            if (oldInvItem.getId() == itemToMove.getId()
                && oldInvItem.getStacked() < oldInvItem.getStackable()) {

              int nrItemsToAdd = nrItems;

              if (nrItems + oldInvItem.getStacked() > oldInvItem.getStackable()) {
                nrItemsToAdd = oldInvItem.getStackable() - oldInvItem.getStacked();
              }

              // IF SAME TYPE OF ITEM THEN STACK THEM
              Server.userDB.updateDB(
                  "update user_chest set Nr = Nr + "
                      + nrItemsToAdd
                      + " where ItemId = "
                      + oldInvItemId
                      + " and Pos = '"
                      + posX
                      + ","
                      + posY
                      + "' and UserId = "
                      + client.UserId);

              // IF ITEMS LEFT AFTER FILLING UP STACK
              int nrItemsLeft = nrItems - nrItemsToAdd;

              if (nrItemsLeft > 0) {
                Server.userDB.updateDB(
                    "update character_item set Nr = "
                        + nrItemsLeft
                        + " where ItemId = "
                        + oldInvItemId
                        + " and InventoryPos = 'Mouse' and CharacterId = "
                        + client.playerCharacter.getDBId());
                client.playerCharacter.getMouseItem().setStacked(nrItemsLeft);
                addOutGoingMessage(
                    client, "addmouseitem", oldInvItemId + ";" + oldInvItem.getType());
              } else {
                Server.userDB.updateDB(
                    "delete from character_item where ItemId = "
                        + oldInvItemId
                        + " and InventoryPos = 'Mouse' and CharacterId = "
                        + client.playerCharacter.getDBId());
                client.playerCharacter.setMouseItem(null);
              }

              nrItems = oldInvItem.getStacked() + nrItemsToAdd;

            } else {
              // MOVE NEW ITEM TO POSITION
              Server.userDB.updateDB(
                  "update user_chest set ItemId = "
                      + itemToMove.getId()
                      + ", Nr = "
                      + nrItems
                      + ", ModifierId = "
                      + itemToMove.getModifierId()
                      + ", MagicId = "
                      + itemToMove.getMagicId()
                      + " where Id = "
                      + oldInvItemUserItemId);

              // DELETE NEW ITEM FROM MOUSE
              Server.userDB.updateDB(
                  "delete from character_item where InventoryPos = 'Mouse' and CharacterId = "
                      + client.playerCharacter.getDBId());

              // PUT OLD ITEM TO MOUSE
              Server.userDB.updateDB(
                  "insert into character_item (CharacterId, ItemId, Nr, Equipped, InventoryPos, ModifierId, MagicId) values ("
                      + client.playerCharacter.getDBId()
                      + ","
                      + oldInvItem.getId()
                      + ","
                      + oldInvItem.getStacked()
                      + ",0,'Mouse',"
                      + oldInvItem.getModifierId()
                      + ","
                      + oldInvItem.getMagicId()
                      + ")");
              addOutGoingMessage(client, "addmouseitem", oldInvItemId + ";" + oldInvItem.getType());
              client.playerCharacter.setMouseItem(oldInvItem);
            }
          } else {
            // FREE SPACE
            // MOVE NEW ITEM TO POSITION
            Server.userDB.updateDB(
                "insert into user_chest (UserId, ItemId, Nr, Pos, ModifierId, MagicId) values ("
                    + client.UserId
                    + ","
                    + itemToMove.getId()
                    + ","
                    + nrItems
                    + ",'"
                    + posX
                    + ","
                    + posY
                    + "',"
                    + itemToMove.getModifierId()
                    + ","
                    + itemToMove.getMagicId()
                    + ")");

            // DELETE ITEM FROM MOUSE
            Server.userDB.updateDB(
                "delete from character_item where InventoryPos = 'Mouse' and CharacterId = "
                    + client.playerCharacter.getDBId());
            client.playerCharacter.setMouseItem(null);
          }
          checkInvPos.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }

        addOutGoingMessage(
            client,
            "moveitem",
            itemToMove.getId()
                + ";"
                + itemToMove.getUserItemId()
                + ";"
                + posX
                + ";"
                + posY
                + ";"
                + nrItems
                + ";"
                + containerType);
      }

      client.playerCharacter.loadInventory();

      sendInventoryInfo(client);

      if (containerType == 1) {
        // SEND PERSONAL CHEST INFO
        String content = ContainerHandler.getPersonalChestContent(client);
        addOutGoingMessage(client, "container_content", content);
      }

      // ONLY INVENTORY
      if (containerType == 0) {
        // movedItemId; posX; posY; nrItems
        QuestHandler.updateItemQuests(client, itemToMove.getId());
      }
    }
  }

  public static void sendInventoryInfo(Client client) {
    StringBuilder inventoryInfo = new StringBuilder(1000);
    inventoryInfo.append(client.playerCharacter.getInventorySize()).append('/');

    ResultSet invInfo =
        Server.userDB.askDB(
            "select Id, ItemId, InventoryPos, Nr from character_item where CharacterId = "
                + client.playerCharacter.getDBId()
                + " and Equipped = 0 and InventoryPos <> 'None' and InventoryPos <> 'Mouse'");

    try {
      while (invInfo.next()) {
        ResultSet itemInfo =
            Server.gameDB.askDB("select Type from item where Id = " + invInfo.getInt("ItemId"));
        if (itemInfo.next()) {
          inventoryInfo
              .append(invInfo.getInt("Id"))
              .append(',')
              .append(invInfo.getInt("ItemId"))
              .append(',')
              .append(invInfo.getInt("Nr"))
              .append(',')
              .append(invInfo.getString("InventoryPos"))
              .append(';');
        }
        itemInfo.close();
      }
      invInfo.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    addOutGoingMessage(client, "inventory", inventoryInfo.toString());
  }

  public static boolean addSpecialItemToInventory(Client client, Item itemToMove) {
    boolean addSpecialItem = false;

    if (itemToMove.getType().equals("Key")) {
      // IF KEY, ADD TO KEYCHAIN
      addSpecialItem = true;

      ResultSet keyInfo =
          Server.userDB.askDB(
              "select KeyId from character_key where KeyId = "
                  + itemToMove.getId()
                  + " and CharacterId = "
                  + client.playerCharacter.getDBId());

      try {
        if (keyInfo.next()) {
          addSpecialItem = false;
          addOutGoingMessage(client, "message", "#messages.inventory.have_key");
        }
        keyInfo.close();
      } catch (SQLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      if (addSpecialItem) {
        Server.userDB.updateDB(
            "insert into character_key (KeyId, CharacterId) values ("
                + itemToMove.getId()
                + ","
                + client.playerCharacter.getDBId()
                + ")");
        addOutGoingMessage(
            client, "message", itemToMove.getName() + " #messages.inventory.added_to_keychain");

        Server.userDB.updateDB(
            "delete from character_item where CharacterId = "
                + client.playerCharacter.getDBId()
                + " and InventoryPos = 'Mouse'");
        client.playerCharacter.setMouseItem(null);
        addOutGoingMessage(client, "addkeys", "");
      }
    } else if (itemToMove.getType().equals("Recipe")) {
      // IF RECIPE, ADD TO CRAFTBOOK
      addSpecialItem = true;

      ResultSet recipeInfo =
          Server.userDB.askDB(
              "select Id from character_recipe where RecipeId = "
                  + itemToMove.getId()
                  + " and CharacterId = "
                  + client.playerCharacter.getDBId());

      try {
        if (recipeInfo.next()) {
          addSpecialItem = false;
          addOutGoingMessage(client, "message", "#messages.inventory.have_recipe");
        }
        recipeInfo.close();
      } catch (SQLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      if (addSpecialItem) {
        Server.userDB.updateDB(
            "insert into character_recipe (RecipeId, CharacterId) values ("
                + itemToMove.getId()
                + ","
                + client.playerCharacter.getDBId()
                + ")");
        addOutGoingMessage(
            client, "message", itemToMove.getName() + " #messages.inventory.added_to_craftbook");

        Server.userDB.updateDB(
            "delete from character_item where CharacterId = "
                + client.playerCharacter.getDBId()
                + " and InventoryPos = 'Mouse'");
        client.playerCharacter.setMouseItem(null);
        addOutGoingMessage(client, "addrecipe", "");
      }
    }
    return addSpecialItem;
  }

  public static void removeCopperFromInventory(Client client, int copperToRemove) {

    // REMOVE COPPER
    ResultSet moneyUpdate;

    moneyUpdate =
        Server.userDB.askDB(
            "select Id, Nr from character_item where ItemId = 36 and CharacterId = "
                + client.playerCharacter.getDBId());

    try {
      while (moneyUpdate.next() && copperToRemove > 0) {
        int copperLeft = copperToRemove - moneyUpdate.getInt("Nr");
        if (copperLeft >= 0) {
          Server.userDB.updateDB(
              "delete from character_item where Id = " + moneyUpdate.getInt("Id"));
          copperToRemove = copperLeft;
        } else {
          int restCopper = moneyUpdate.getInt("Nr") - copperToRemove;
          copperToRemove = 0;
          Server.userDB.updateDB(
              "update character_item set Nr = "
                  + restCopper
                  + " where Id = "
                  + moneyUpdate.getInt("Id"));
        }
      }
      moneyUpdate.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    // REMOVE SILVER
    if (copperToRemove > 0) {
      moneyUpdate =
          Server.userDB.askDB(
              "select Id, Nr from character_item where ItemId = 35 and CharacterId = "
                  + client.playerCharacter.getDBId());

      try {
        while (moneyUpdate.next() && copperToRemove > 0) {
          int copperLeft = copperToRemove - moneyUpdate.getInt("Nr") * 100;
          if (copperLeft >= 0) {
            Server.userDB.updateDB(
                "delete from character_item where Id = " + moneyUpdate.getInt("Id"));
            copperToRemove = copperLeft;
          } else {
            int restCopper = moneyUpdate.getInt("Nr") * 100 - copperToRemove;
            int silverLeft = (int) Math.floor((float) restCopper / 100);
            int copperRest = restCopper % 100;
            copperToRemove = 0;
            if (silverLeft > 0) {
              Server.userDB.updateDB(
                  "update character_item set Nr = "
                      + silverLeft
                      + " where Id = "
                      + moneyUpdate.getInt("Id"));
            } else {
              Server.userDB.updateDB(
                  "delete from character_item where Id = " + moneyUpdate.getInt("Id"));
            }
            if (copperRest > 0) {
              Item CopperItem = new Item(ServerGameInfo.itemDef.get(36));
              CopperItem.setStacked(copperRest);
              addItemToInventory(client, CopperItem);
            }
          }
        }
        moneyUpdate.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }

    // REMOVE GOLD
    if (copperToRemove > 0) {
      moneyUpdate =
          Server.userDB.askDB(
              "select Id, Nr from character_item where ItemId = 34 and CharacterId = "
                  + client.playerCharacter.getDBId());

      try {
        while (moneyUpdate.next() && copperToRemove > 0) {
          int copperLeft = copperToRemove - moneyUpdate.getInt("Nr") * 10000;
          if (copperLeft >= 0) {
            Server.userDB.updateDB(
                "delete from character_item where Id = " + moneyUpdate.getInt("Id"));
            copperToRemove = copperLeft;
          } else {
            int restCopper = moneyUpdate.getInt("Nr") * 10000 - copperToRemove;
            int goldLeft = (int) Math.floor((float) restCopper / 10000);
            int copperRest = restCopper % 10000;
            copperToRemove = 0;
            if (goldLeft > 0) {
              Server.userDB.updateDB(
                  "update character_item set Nr = "
                      + goldLeft
                      + " where Id = "
                      + moneyUpdate.getInt("Id"));
            } else {
              Server.userDB.updateDB(
                  "delete from character_item where Id = " + moneyUpdate.getInt("Id"));
            }
            if (copperRest > 0) {
              if (copperRest > 100) {
                int silverCoins = (int) Math.floor((float) copperRest / 100);
                Item SilverItem = new Item(ServerGameInfo.itemDef.get(35));
                SilverItem.setStacked(silverCoins);
                addItemToInventory(client, SilverItem);
                copperRest -= silverCoins * 100;
              }
              if (copperRest > 0) {
                Item CopperItem = new Item(ServerGameInfo.itemDef.get(36));
                CopperItem.setStacked(copperRest);
                addItemToInventory(client, CopperItem);
              }
            }
          }
        }
        moneyUpdate.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }

  public static void addItemToInventory(Client client, Item newItem) {
    boolean addSpecialItem = addSpecialItemToInventory(client, newItem);

    if (!addSpecialItem) {
      //ADD ITEM TO PLAYER INVENTORY

      String newItemPos = getFreePosInInventory(client, newItem);
      if (!newItemPos.equals("None")) {
        if (newItem.getType().equals("Money")) {
          addOutGoingMessage(client, "sell", "" + newItem.getStacked() + " " + newItem.getName());
        } else {
          addOutGoingMessage(
              client, "message", newItem.getName() + " #messages.inventory.added_to_inventory");
        }

        String itemPosInfo[] = newItemPos.split(";");
        String stacked = itemPosInfo[0];
        String InventoryPos = itemPosInfo[1];
        int totalItems = Integer.parseInt(itemPosInfo[2]);

        if (stacked.equals("newpos")) {
          Server.userDB.updateDB(
              "insert into character_item (CharacterId, ItemId, Equipped, Nr, InventoryPos, ModifierId, MagicId) values("
                  + client.playerCharacter.getDBId()
                  + ","
                  + newItem.getId()
                  + ",0,"
                  + totalItems
                  + ",'"
                  + InventoryPos
                  + "',"
                  + newItem.getModifierId()
                  + ","
                  + newItem.getMagicId()
                  + ")");
        } else {
          Server.userDB.updateDB(
              "update character_item set Nr = "
                  + totalItems
                  + " where ItemId = "
                  + newItem.getId()
                  + " and CharacterId = "
                  + client.playerCharacter.getDBId()
                  + " and InventoryPos = '"
                  + InventoryPos
                  + "'");
        }

        client.playerCharacter.loadInventory();
        sendInventoryInfo(client);

      } else {
        ItemHandler.loseItemOnGround(client, newItem);
      }
    }
    QuestHandler.updateItemQuests(client, newItem.getId());
  }

  public static boolean useItem(Client client, Item usedItem, int posX, int posY) {
    boolean useItemSuccess = false;

    // CHECK IF ITEM EXIST IN INVENTORY
    ResultSet rs =
        Server.userDB.askDB(
            "select Id, ItemId, Nr, InventoryPos from character_item where ItemId = "
                + usedItem.getId()
                + " and InventoryPos = '"
                + posX
                + ","
                + posY
                + "' and CharacterId = "
                + client.playerCharacter.getDBId());

    try {
      if (rs.next()) {
        // USE ITEM AND REMOVE IT
        int userItemId = rs.getInt("Id");

        if (usedItem != null) {
          usedItem.setUserItemId(userItemId);

          if (usedItem.getType().equals("Scroll")) {
            // SCROLL
            addOutGoingMessage(
                client,
                "use_scroll",
                usedItem.getUserItemId() + "," + usedItem.getSubType() + ",Inventory");
          } else if (usedItem.getType().equals("Readable")) {
            // READABLE ITEM
            addOutGoingMessage(
                client, "readable", usedItem.getName() + ";" + usedItem.getDescription());
          } else {
            // USEABLE ITEM
            if (client.playerCharacter.useItem(usedItem)) {
              if (rs.getInt("Nr") > 1) {
                Server.userDB.updateDB(
                    "update character_item set Nr = Nr - 1 where Id = " + userItemId);
              } else {
                Server.userDB.updateDB("delete from character_item where Id = " + userItemId);
              }
              client.playerCharacter.loadInventory();
              client.playerCharacter.saveInfo();

              // SEND REMOVE ITEM FROM INVENTORY
              addOutGoingMessage(client, "inventory_remove", rs.getString("InventoryPos"));
              useItemSuccess = true;

              QuestHandler.updateUseItemQuests(client, usedItem.getId());
            }
          }
        }
      }
      rs.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return useItemSuccess;
  }

  public static int countPlayerItem(Client client, int itemId) {
    int nrItems = 0;

    ResultSet itemInfo =
        Server.userDB.askDB(
            "select ItemId, Nr from character_item where CharacterId = "
                + client.playerCharacter.getDBId()
                + " and ItemId = "
                + itemId
                + " and InventoryPos <> 'None' and InventoryPos <> 'Mouse' and InventoryPos <> 'Actionbar'");
    try {
      while (itemInfo.next()) {
        nrItems += itemInfo.getInt("Nr");
      }
      itemInfo.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return nrItems;
  }

  public static boolean removeNumberOfItems(Client client, int itemId, int nrItems) {
    boolean success = false;

    ResultSet itemInfo =
        Server.userDB.askDB(
            "select Id, Nr from character_item where CharacterId = "
                + client.playerCharacter.getDBId()
                + " and ItemId = "
                + itemId
                + " and InventoryPos <> 'Mouse' and Equipped = 0 and InventoryPos <> 'Actionbar'");
    try {
      while (itemInfo.next() && nrItems > 0) {
        int nrLeftInInv = itemInfo.getInt("Nr") - nrItems;

        int removedNr = 0;

        if (nrLeftInInv > 0) {
          removedNr = nrItems;
          Server.userDB.updateDB(
              "update character_item set Nr = "
                  + nrLeftInInv
                  + " where Id = "
                  + itemInfo.getInt("Id"));
        } else {
          removedNr = itemInfo.getInt("Nr");
          Server.userDB.updateDB("delete from character_item where Id = " + itemInfo.getInt("Id"));
        }

        nrItems -= removedNr;
      }
      itemInfo.close();

      if (nrItems <= 0) {
        success = true;
      }

      itemInfo.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return success;
  }

  public static String getFreePosInInventory(Client client, Item newItem) {
    // FIRST CHECK IF ITEM IS STACKABLE
    // THEN CHECK IF ITEM CAN BE STACKED ON SAME ITEM
    // OTHERWISE PLACE ON NEW SPOT
    int inventorySize = client.playerCharacter.getInventorySize();

    if (client.playerCharacter != null) {
      client.playerCharacter.loadInventory();

      if (newItem.getStacked() <= newItem.getStackable()) {
        if (newItem.getStackable() > 1) {
          for (int i = 0; i < inventorySize; i++) {
            for (int j = 0; j < inventorySize; j++) {
              Item invItem = client.playerCharacter.getInventoryItem(i, j);
              if (invItem != null) {
                if (invItem.getId() == newItem.getId()
                    && invItem.getStacked() + newItem.getStacked() <= invItem.getStackable()) {
                  int totalItems = invItem.getStacked() + newItem.getStacked();
                  return "stacked;" + i + "," + j + ";" + totalItems;
                }
              }
            }
          }
        }

        for (int i = 0; i < inventorySize; i++) {
          for (int j = 0; j < inventorySize; j++) {
            if (client.playerCharacter.getInventoryItem(i, j) == null) {
              return "newpos;" + i + "," + j + ";" + newItem.getStacked();
            }
          }
        }
      }
    }
    return "None";
  }

  public void removeItemFromInventory(Client client, int posX, int posY, int nr) {
    ResultSet removeInfo =
        Server.userDB.askDB(
            "select Nr from character_item where InventoryPos = '"
                + posX
                + ","
                + posY
                + "' and CharacterId = "
                + client.playerCharacter.getDBId());

    try {
      if (removeInfo.next()) {
        if (removeInfo.getInt("Nr") > 1) {
          Server.userDB.updateDB(
              "update character_item set Nr = Nr - 1 where InventoryPos = '"
                  + posX
                  + ","
                  + posY
                  + "' and CharacterId = "
                  + client.playerCharacter.getDBId());
        } else {
          Server.userDB.updateDB(
              "delete from character_item where InventoryPos = '"
                  + posX
                  + ","
                  + posY
                  + "' and CharacterId = "
                  + client.playerCharacter.getDBId());
        }

        client.playerCharacter.loadInventory();
        // SEND REMOVE ITEM FROM INVENTORY
        addOutGoingMessage(client, "inventory_remove", "remove");
      }
      removeInfo.close();
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
