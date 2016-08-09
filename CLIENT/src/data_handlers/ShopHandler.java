package data_handlers;

import abilitysystem.Ability;
import sound.Sfx;
import creature.Creature;
import game.BlueSaga;
import graphics.BlueSagaColors;
import gui.Gui;

public class ShopHandler extends Handler {

  public ShopHandler() {
    super();
  }

  public static void handleData(String serverData) {

    if (serverData.startsWith("<shop>")) {

      String shop_info = serverData.substring(6);
      Gui.ShopWindow.load(shop_info);
      BlueSaga.actionServerWait = false;

    } else if (serverData.startsWith("<buy>")) {

      String buy_info[] = serverData.substring(5).split("/");

      String itemType = buy_info[0];
      String price = buy_info[2];

      if (itemType.equals("item")) {
        Gui.MouseItem.clear();
        Gui.addMessage(price + " #messages.shop.copper_removed", BlueSagaColors.YELLOW);
        Sfx.play("notifications/gold");
      } else if (itemType.equals("ability")) {
        String abilityInfo[] = buy_info[3].split("=");
        int abilityId = Integer.parseInt(abilityInfo[0]);

        Ability newAB = new Ability(abilityId);
        newAB.load(buy_info[3]);

        BlueSaga.playerCharacter.addAbility(newAB);

        Gui.addMessage(
            "#messages.quest.gained# '" + newAB.getName() + "' #messages.quest.ability",
            BlueSagaColors.RED);
      }
      Sfx.play("notifications/gold");

      BlueSaga.actionServerWait = false;
    } else if (serverData.startsWith("<shoperror>")) {
      BlueSaga.actionServerWait = false;
      String errorMessage = serverData.substring(11);
      if (errorMessage.equals("nogold")) {
        Gui.addMessage("#messages.shop.not_enough_money", BlueSagaColors.RED);
      } else if (errorMessage.equals("noability")) {
        Gui.addMessage("#messages.shop.ability_not_exist", BlueSagaColors.RED);
      } else if (errorMessage.equals("noitem")) {
        Gui.addMessage("#messages.shop.item_not_exist", BlueSagaColors.RED);
      } else if (errorMessage.equals("inventoryfull")) {
        Gui.addMessage("#messages.inventory.full", BlueSagaColors.RED);
      } else if (errorMessage.equals("haveability")) {
        Gui.addMessage("#messages.shop.ability_already_aquired", BlueSagaColors.RED);
      } else if (errorMessage.equals("noreq")) {
        Gui.addMessage("#messages.shop.not_have_requirements", BlueSagaColors.RED);
      } else if (errorMessage.equals("notwanted")) {
        Gui.addMessage("#messages.shop.no_sell", BlueSagaColors.RED);
      } else if (errorMessage.equals("lowlevel")) {
        Gui.addMessage("#messages.shop.need_level_sell", BlueSagaColors.RED);
      }
    } else if (serverData.startsWith("<newcustomize>")) {
      String creatureCustomizeInfo[] = serverData.substring(14).split(";");
      String creatureInfo = creatureCustomizeInfo[0];

      Creature creature = MapHandler.addCreatureToScreen(creatureInfo);

      String customizeInfo[] = creatureCustomizeInfo[1].split(",");
      String customizeType = customizeInfo[0];
      int customizeId = Integer.parseInt(customizeInfo[1]);

      if (customizeType.equals("Mouth Feature")) {
        creature.getCustomization().setMouthFeatureId(customizeId);
      } else if (customizeType.equals("Accessories")) {
        creature.getCustomization().setAccessoriesId(customizeId);
      } else if (customizeType.equals("Skin Feature")) {
        creature.getCustomization().setSkinFeatureId(customizeId);
      } else if (customizeType.equals("Remove")) {
        creature.getCustomization().setMouthFeatureId(0);
        creature.getCustomization().setAccessoriesId(0);
        creature.getCustomization().setSkinFeatureId(0);
      }
      if (Gui.InventoryWindow.isOpen()) {
        BlueSaga.client.sendMessage("inventory", "info");
      }
    }

    if (serverData.startsWith("<sell>")) {

      String name = serverData.substring(6);

      //BP_CLIENT.MouseItem.clear();

      Sfx.play("notifications/gold");

      Gui.addMessage(name + " #messages.inventory.added_to_inventory", BlueSagaColors.YELLOW);

      BlueSaga.actionServerWait = false;
    }
  }
}
