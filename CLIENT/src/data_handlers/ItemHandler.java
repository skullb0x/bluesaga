package data_handlers;

import java.util.Vector;

import org.newdawn.slick.Color;

import abilitysystem.Ability;
import sound.Sfx;
import components.Item;
import creature.Creature;
import creature.Creature.CreatureType;
import game.BlueSaga;
import graphics.BlueSagaColors;
import gui.Gui;
import screens.ScreenHandler;

public class ItemHandler extends Handler {

  public static Vector<Integer> GlovesIds;

  public static void init() {
    GlovesIds = new Vector<Integer>();

    GlovesIds.add(151);
    GlovesIds.add(152);
    GlovesIds.add(69);
    GlovesIds.add(70);
    GlovesIds.add(28);
    GlovesIds.add(29);
    GlovesIds.add(71);
  }

  public static void handleData(String serverData) {

    if (serverData.startsWith("<inventory>")) {
      String inventoryData = serverData.substring(11);
      Gui.InventoryWindow.load(inventoryData);
      BlueSaga.actionServerWait = false;
    }

    if (serverData.startsWith("<addmouseitem>")) {
      String itemInfo[] = serverData.substring(14).split(";");
      int itemId = Integer.parseInt(itemInfo[0]);
      String itemType = itemInfo[1];

      Item mouseItem = new Item(itemId);
      mouseItem.setType(itemType);

      Gui.MouseItem.clear();
      Gui.MouseItem.setItem(mouseItem);
      BlueSaga.actionServerWait = false;

      if (mouseItem.getType().equals("Money")) {
        Sfx.playRandomPitch("notifications/gold");
      } else {
        Sfx.playRandomPitch("gui/move_item");
      }
      Gui.ItemSplitterWindow.close();
    }

    if (serverData.startsWith("<addkeys>")) {
      Gui.MouseItem.clear();
      if (Gui.InventoryWindow.isOpen()) {
        BlueSaga.client.sendMessage("inventory", "info");
      }
      Sfx.play("items/add_keys");
    }

    if (serverData.startsWith("<clearmouse>")) {
      Gui.MouseItem.clear();
    }

    if (serverData.startsWith("<inventory_remove>")) {
      Sfx.playRandomPitch("move_item");
      if (Gui.InventoryWindow.isOpen()) {
        BlueSaga.client.sendMessage("inventory", "info");
      }
      BlueSaga.actionServerWait = false;
    }

    if (serverData.startsWith("<invfull>")) {
      Gui.addMessage("#messages.inventory.full", BlueSagaColors.RED);
    }

    if (serverData.startsWith("<moveitem>")) {
      String itemInfo[] = serverData.substring(10).split(";");

      // movedItemId; movedItemUserItemid; posX; posY; newMouseItemId; newMouseUserItemId

      int itemId = Integer.parseInt(itemInfo[0]);
      int userItemId = Integer.parseInt(itemInfo[1]);
      int posX = Integer.parseInt(itemInfo[2]);
      int posY = Integer.parseInt(itemInfo[3]);
      int nrItems = Integer.parseInt(itemInfo[4]);
      int containerId = Integer.parseInt(itemInfo[5]);

      Item placedItem = new Item(itemId);
      placedItem.setUserItemId(userItemId);

      if (containerId == 0) {
        Gui.InventoryWindow.placeItem(placedItem, posX, posY, nrItems);
      } else {
        Gui.ContainerWindow.addItemAtPos(placedItem, posX, posY);
        Gui.ContainerWindow.getBox(posX, posY).setNumber(nrItems);
      }
      BlueSaga.actionServerWait = false;
      Sfx.playRandomPitch("gui/move_item");
    }

    if (serverData.startsWith("<you_equip>")) {
      String equipinfo[] = serverData.substring(11).split(";");

      // newEquipId; newEquipUserItemId; newEquipType; newEquipSkillType; oldEquipId; oldEquipUserItemId; bonusstats
      int newEquipId = Integer.parseInt(equipinfo[0]);
      int newEquipUserItemId = Integer.parseInt(equipinfo[1]);
      String newEquipType = equipinfo[2];
      int newEquipClassId = Integer.parseInt(equipinfo[3]);
      int oldEquipId = Integer.parseInt(equipinfo[4]);
      int oldEquipUserItemId = Integer.parseInt(equipinfo[5]);
      String oldEquipType = equipinfo[6];
      int attackRange = Integer.parseInt(equipinfo[7]);

      // PICKUP OLD EQUIP TO MOUSE
      if (oldEquipId > 0) {
        Item oldEquip = new Item(oldEquipId);
        oldEquip.setUserItemId(oldEquipUserItemId);
        oldEquip.setType(oldEquipType);
        Gui.MouseItem.setItem(oldEquip);
      } else {
        Gui.MouseItem.setItem(null);
      }

      // GET INFO ABOUT NEW EQUIPPED ITEM
      Item newEquip = new Item(newEquipId);
      newEquip.setUserItemId(newEquipUserItemId);
      newEquip.setType(newEquipType);
      newEquip.setClassId(newEquipClassId);

      // STRENGTH, INTELLIGENCE, AGILITY,
      // CRITICAL_HIT, EVASION, ACCURACY,
      // MAX_HEALTH, MAX_MANA,
      // FIRE_DEF, COLD_DEF, SHOCK_DEF, CHEMS_DEF, MIND_DEF

      BlueSaga.playerCharacter.setAttackRange(attackRange);

      BlueSaga.playerCharacter.MyEquipHandler.equipItem(newEquip);
      Sfx.play("gui/equip");

      Gui.StatusWindow.setEquip(newEquip);

      BlueSaga.actionServerWait = false;

      Gui.getActionBar().update();

      ScreenHandler.myEmitterManager.SpawnEmitter(
          BlueSaga.playerCharacter.getPixelX() + 25,
          -BlueSaga.playerCharacter.getPixelY(),
          "Equip");

    } else if (serverData.startsWith("<you_equip_no>")) {
      Sfx.play("gui/menu_no");
      BlueSaga.actionServerWait = false;

      String message = serverData.substring(14);
      //Gui.getActionBar().update();

      if (message.length() > 0) {
        Gui.addMessage(message, BlueSagaColors.RED);
      }
    }

    if (serverData.startsWith("<you_unequip>")) {
      String unequipInfo[] = serverData.substring(13).split(";");
      int unequipId = Integer.parseInt(unequipInfo[0]);
      int unequipUserItemId = Integer.parseInt(unequipInfo[1]);
      String unequipType = unequipInfo[2];
      int attackRange = Integer.parseInt(unequipInfo[3]);

      BlueSaga.playerCharacter.setAttackRange(attackRange);

      Item unequipItem = new Item(unequipId);
      unequipItem.setUserItemId(unequipUserItemId);
      unequipItem.setType(unequipType);

      BlueSaga.playerCharacter.MyEquipHandler.unEquipItem(unequipItem.getType());
      Gui.StatusWindow.removeEquip(unequipItem.getType());
      BlueSaga.actionServerWait = false;
      Sfx.playRandomPitch("gui/move_item");

      Gui.getActionBar().update();
    }

    // SCROLLS
    if (serverData.startsWith("<use_scroll>")) {
      String scrollInfo[] = serverData.substring(12).split(",");
      int scrollId = Integer.parseInt(scrollInfo[0]);
      String scrollType = scrollInfo[1];
      String scrollLocation = scrollInfo[2];

      Gui.USE_SCROLL = true;
      Gui.USE_SCROLL_ID = scrollId;
      Gui.USE_SCROLL_TYPE = scrollType;
      Gui.USE_SCROLL_LOCATION = scrollLocation;

      Gui.Mouse.setType("Ability");
      BlueSaga.actionServerWait = false;
    }

    // STATUS WINDOW
    if (serverData.startsWith("<statuswindow>")) {
      String statusInfo = serverData.substring(14);
      Gui.StatusWindow.load(statusInfo);
    }

    if (serverData.startsWith("<item_info>")) {
      String info[] = serverData.substring(11).split("@");

      if (info[0].equals("equip")) {
        Gui.StatusWindow.showInfoBox(info[1]);
      } else if (info[0].equals("shop")) {
        Gui.ShopWindow.showInfoBox(info[1]);
      } else if (info[0].equals("container")
          || info[0].equals("personalchest")
          || info[0].equals("closet")) {
        Gui.ContainerWindow.showInfoBox(info[1]);
      } else {
        Gui.InventoryWindow.showInfoBox(info[1]);
      }
    }

    if (serverData.equals("<add_actionbar>no")) {
      BlueSaga.actionServerWait = false;

    } else if (serverData.startsWith("<add_actionbar>")) {
      String actionInfo[] = serverData.substring(15).split(";");
      String actionType = actionInfo[0];
      int actionId = Integer.parseInt(actionInfo[1]);
      int pos = Integer.parseInt(actionInfo[2]);

      Gui.MouseItem.clear();
      if (actionType.equals("Ability")) {
        Ability addedAbility = BlueSaga.playerCharacter.getAbilityById(actionId);

        if (addedAbility != null) {
          // IF OLD ABILITY IN SLOT, PUT IT TO MOUSE
          Ability oldAbility = Gui.getActionBar().getBox(pos).getAbility();

          if (oldAbility != null) {
            Gui.MouseItem.setAbility(oldAbility);
          }

          Gui.getActionBar().getBox(pos).clear();
          Gui.getActionBar().getBox(pos).setAbility(addedAbility);
        }
      } else if (actionType.equals("Item")) {
        Item newItem = new Item(actionId);
        Gui.getActionBar().getBox(pos).setItem(newItem);
      }
      BlueSaga.actionServerWait = false;

      Gui.getActionBar().update();

      if (Gui.InventoryWindow.isOpen()) {
        BlueSaga.client.sendMessage("inventory", "info");
      }
    }

    if (serverData.startsWith("<remove_actionbar>")) {
      int pos = Integer.parseInt(serverData.substring(18));

      Gui.getActionBar().getBox(pos).clear();
      BlueSaga.actionServerWait = false;
    }

    if (serverData.startsWith("<stat>")) {
      String statValue[] = serverData.substring(6).split(";");
      String stat = statValue[0];
      int value = Integer.parseInt(statValue[1]);

      if (stat.equals("Health")) {
        BlueSaga.playerCharacter.setHealth(value);
      } else if (stat.equals("Mana")) {
        BlueSaga.playerCharacter.setMana(value);
      } else {
        BlueSaga.playerCharacter.setStat(stat, value);
      }
    }

    if (serverData.equals("<useitem>no")) {
      BlueSaga.actionServerWait = false;
    } else if (serverData.startsWith("<useitem>")) {
      String useInfo[] = serverData.substring(9).split(";");

      Creature USER = MapHandler.addCreatureToScreen(useInfo[0]);

      String useColor[] = useInfo[1].split(",");
      int R = Integer.parseInt(useColor[0]);
      int G = Integer.parseInt(useColor[1]);
      int B = Integer.parseInt(useColor[2]);
      int healthStatus = Integer.parseInt(useColor[3]);
      String itemInfo[] = useInfo[2].split(",");
      String itemType = itemInfo[0];
      String itemSubType = itemInfo[1];

      if (USER.getCreatureType() == CreatureType.Player
          && USER.getDBId() == BlueSaga.playerCharacter.getDBId()) {
        if (itemSubType.equals("HEALTH") || itemSubType.equals("MANA")) {
          if (Gui.InventoryWindow.isOpen()) {
            Gui.InventoryWindow.startUseTimer(itemType);
          }
          Gui.getActionBar().startUseTimer(itemType);
        }
        if (itemType.equals("Potion")) {
          Sfx.play("items/use_potion");
        } else if (itemType.equals("Eatable")) {
          Sfx.play("items/eat");
        }
      }

      if (USER != null) {
        USER.useItem(new Color(R, G, B));
        USER.setHealthStatus(healthStatus);
      }

      BlueSaga.actionServerWait = false;
    }

    if (serverData.startsWith("<readable>")) {
      String readInfo[] = serverData.substring(10).split(";");
      String bookName = readInfo[0];
      String bookText = readInfo[1];

      Gui.BookWindow.load(bookName, bookText);

      BlueSaga.actionServerWait = false;
    }
  }
}
