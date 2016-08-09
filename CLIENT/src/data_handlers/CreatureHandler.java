package data_handlers;

import map.Tile;
import screens.ScreenHandler;
import abilitysystem.StatusEffect;
import sound.Sfx;
import game.BlueSaga;
import components.Item;
import creature.Creature;
import creature.PlayerCharacter;
import creature.Creature.CreatureType;

public class CreatureHandler extends Handler {

  public CreatureHandler() {
    super();
  }

  public static void handleData(String serverData) {

    // OTHER PLAYER ACTIONS
    if (serverData.startsWith("<creaturepos>")) {
      String creatureData = serverData.substring(13);

      String allCreaturesData[] = creatureData.split(";");

      for (String oneCreatureData : allCreaturesData) {
        String creatureMoveInfo[] = oneCreatureData.split("/");

        Creature movedCreature = MapHandler.addCreatureToScreen(creatureMoveInfo[0]);

        String moveInfo[] = creatureMoveInfo[1].split(",");
        int gotoX = Integer.parseInt(moveInfo[0]);
        int gotoY = Integer.parseInt(moveInfo[1]);
        int gotoZ = Integer.parseInt(moveInfo[2]);
        int speed = Integer.parseInt(moveInfo[3]);
        float gotoRotation = Float.parseFloat(moveInfo[4]);

        movedCreature.MyWalkHandler.walkTo(gotoX, gotoY, gotoZ, speed);
        movedCreature.setGotoRotation(gotoRotation);

        if (gotoZ != BlueSaga.playerCharacter.getZ()) {
          movedCreature.dissappear();
        } else {
          movedCreature.setDead(false);
          movedCreature.setHidden(false);
          movedCreature.setRemoved(false);
        }

        MapHandler.updateScreenObjects();
      }

    } else if (serverData.startsWith("<creature_goto>")) {
      String creatureData[] = serverData.substring(15).split(";");
      Creature creature = MapHandler.addCreatureToScreen(creatureData[0]);

      String gotoInfo[] = creatureData[1].split(",");

      int gotoX = Integer.parseInt(gotoInfo[0]);
      int gotoY = Integer.parseInt(gotoInfo[1]);
      int speed = Integer.parseInt(gotoInfo[2]);

      creature.MyWalkHandler.walkFromTo(gotoX, gotoY, speed);

      MapHandler.updateScreenObjects();

    } else if (serverData.startsWith("<creature_seffects>")) {
      String creatureSeInfo[] = serverData.substring(19).split("/");
      Creature creature = MapHandler.addCreatureToScreen(creatureSeInfo[0]);

      String statusEffectsInfo[] = creatureSeInfo[1].split(";");

      for (String se : statusEffectsInfo) {
        String seInfo[] = se.split(",");

        int seId = Integer.parseInt(seInfo[0]);
        int seGraphicsId = Integer.parseInt(seInfo[1]);
        creature.addStatusEffect(new StatusEffect(seId, seGraphicsId));
      }
    } else if (serverData.startsWith("<change_dir>")) {
      String creatureInfo[] = serverData.substring(12).split(";");

      Creature creature = MapHandler.addCreatureToScreen(creatureInfo[0]);
      float gotoRotation = Float.parseFloat(creatureInfo[1]);

      creature.setGotoRotation(gotoRotation);

    } else if (serverData.startsWith("<new_creature>")) {
      String newCreatureInfo = serverData.substring(14);

      Creature creature = MapHandler.addCreatureToScreen(newCreatureInfo);
      creature.appear();
      MapHandler.updateScreenObjects();
    } else if (serverData.startsWith("<creature_remove>")) {
      String creatureInfo = serverData.substring(17);
      Creature removedCreature = MapHandler.addCreatureToScreen(creatureInfo);

      if (removedCreature != null) {
        /*
        if(removedPlayer.getName().contains("!")){
        	BlueSaga.BG_MUSIC.changeSong(BlueSaga.WORLD_MAP.getType());
        }
        */
        removedCreature.dissappear();
        removedCreature.setRemoved(true);

        if (removedCreature.getCreatureType() == CreatureType.Player) {
          PlayerCharacter removedPlayer = (PlayerCharacter) removedCreature;
          if (removedPlayer.getShip() != null) {
            removedPlayer.getShip().setShow(false);
          }
        }

        Tile tile =
            ScreenHandler.SCREEN_TILES.get(
                removedCreature.getX()
                    + ","
                    + removedCreature.getY()
                    + ","
                    + removedCreature.getZ());

        if (tile != null) {
          tile.setOccupant(CreatureType.None, 0);
        }

        MapHandler.updateScreenObjects();
      }
    }

    if (serverData.startsWith("<other_unequip>")) {
      // TargetType; TargetId; ItemId, Itemtype
      String equipInfo[] = serverData.substring(15).split("/");

      Creature creatureEquip = MapHandler.addCreatureToScreen(equipInfo[0]);
      String unequipType = equipInfo[1];

      creatureEquip.MyEquipHandler.unEquipItem(unequipType);

      ScreenHandler.myEmitterManager.SpawnEmitter(
          creatureEquip.getPixelX() + 25, -creatureEquip.getPixelY(), "Equip");
      Sfx.play("gui/equip");
    }

    if (serverData.startsWith("<other_equip>")) {
      // TargetType; TargetId; ItemId, Itemtype
      String equipInfo[] = serverData.substring(13).split("/");

      Creature creatureEquip = MapHandler.addCreatureToScreen(equipInfo[0]);
      String equipItemInfo[] = equipInfo[1].split(",");
      String equipType = equipItemInfo[0];
      int equipId = Integer.parseInt(equipItemInfo[1]);

      Item equipItem = new Item(equipId);
      equipItem.setType(equipType);
      creatureEquip.MyEquipHandler.equipItem(equipItem);

      ScreenHandler.myEmitterManager.SpawnEmitter(
          creatureEquip.getPixelX() + 25, -creatureEquip.getPixelY(), "Equip");
      Sfx.play("gui/equip");
    }

    if (serverData.startsWith("<other_level_up>")) {
      String playerInfo = serverData.substring(16);

      Creature player = MapHandler.addCreatureToScreen(playerInfo);
      player.showLevelUp();
      player.setHealthStatus(4);
      Sfx.play("notifications/level_up", 1.0f, 0.2f);
    }
  }
}
