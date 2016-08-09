package data_handlers;

import sound.Sfx;
import gui.Gui;
import components.Item;
import creature.Creature;

public class SkinHandler extends Handler {

  public SkinHandler() {
    super();
    // TODO Auto-generated constructor stub
  }

  public static void handleData(String serverData) {
    if (serverData.startsWith("<closet_content>")) {
      String closet_info[] = serverData.substring(16).split("/");

      String closetInfo[] = closet_info[0].split(",");

      String name = closetInfo[0];
      int sizeW = Integer.parseInt(closetInfo[1]);
      int sizeH = Integer.parseInt(closetInfo[2]);

      int cX = 20;
      int cY = 90;

      Gui.ContainerWindow.init(name, "0,0,0", cX, cY, sizeW, sizeH);

      if (closet_info.length > 1) {
        String items[] = closet_info[1].split(";");

        for (String itemInfo : items) {
          String item_info[] = itemInfo.split(",");
          int itemId = Integer.parseInt(item_info[0]);
          String itemName = item_info[1];
          String subType = item_info[2];

          Item skinItem = new Item(itemId);
          skinItem.setName(itemName);
          skinItem.setSubType(subType);
          Gui.ContainerWindow.addItem(skinItem);
        }
      }
      Sfx.play("battle/closet_open");
      Gui.ContainerWindow.open();

    } else if (serverData.startsWith("<set_skin>")) {
      String creatureSkinInfo[] = serverData.substring(10).split(";");
      Creature c = MapHandler.addCreatureToScreen(creatureSkinInfo[0]);

      String skinInfo[] = creatureSkinInfo[1].split(",");
      String skinType = skinInfo[0];
      int skinId = Integer.parseInt(skinInfo[1]);

      if (skinType.equals("Remove")) {
        c.getCustomization().clearSkin();
      } else {
        c.getCustomization().setSkin(skinType, skinId);
      }
    } else if (serverData.startsWith("<character_skins>")) {
      String skins = serverData.substring(17);
      Gui.CharacterSkinWindow.load(skins);
    } else if (serverData.startsWith("<set_character_skin>")) {
      String creatureSkinInfo[] = serverData.substring(20).split(";");
      Creature c = MapHandler.addCreatureToScreen(creatureSkinInfo[0]);

      try {
        int skinId = Integer.parseInt(creatureSkinInfo[1]);
        c.setCreatureId(skinId);
      } catch (NumberFormatException e) {

      }
    }
  }
}
