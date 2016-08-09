package data_handlers;

import java.util.HashMap;

import creature.Creature;
import creature.Creature.CreatureType;
import game.BlueSaga;
import gui.Gui;
import screens.ScreenHandler;

public class MonsterHandler extends Handler {

  public static HashMap<Integer, Creature> creatureDefinitions;

  public static void init() {
    creatureDefinitions = new HashMap<Integer, Creature>();
  }

  public static void handleData(String serverData) {
    if (serverData.startsWith("<mobinfo>")) {
      creatureDefinitions.clear();

      String mobsInfo[] = serverData.substring(9).split(";");
      for (String mobInfo : mobsInfo) {
        String mobData[] = mobInfo.split(",");

        // Id, Name, SizeW, SizeH, HeadX, HeadY, WeaponX, WeaponY, OffHandX, OffHandY, AmuletX, AmuletY, ArtifactX, ArtifactY

        Creature newCreature = new Creature(0, 0, 0);

        int cId = Integer.parseInt(mobData[0]);
        String cName = mobData[1];
        int cSizeW = Integer.parseInt(mobData[2]);
        int cSizeH = Integer.parseInt(mobData[3]);
        int cHeadX = Integer.parseInt(mobData[4]);
        int cHeadY = Integer.parseInt(mobData[5]);
        int cWeaponX = Integer.parseInt(mobData[6]);
        int cWeaponY = Integer.parseInt(mobData[7]);
        int cOffHandX = Integer.parseInt(mobData[8]);
        int cOffHandY = Integer.parseInt(mobData[9]);
        int cAmuletX = Integer.parseInt(mobData[10]);
        int cAmuletY = Integer.parseInt(mobData[11]);
        int cArtifactX = Integer.parseInt(mobData[12]);
        int cArtifactY = Integer.parseInt(mobData[13]);

        int cMouthFeatureX = Integer.parseInt(mobData[14]);
        int cMouthFeatureY = Integer.parseInt(mobData[15]);
        int cAccessoriesX = Integer.parseInt(mobData[16]);
        int cAccessoriesY = Integer.parseInt(mobData[17]);
        int cSkinFeatureX = Integer.parseInt(mobData[18]);
        int cSkinFeatureY = Integer.parseInt(mobData[19]);

        newCreature.setType(cId);
        newCreature.setName(cName);
        newCreature.setSizeWidth(cSizeW);
        newCreature.setSizeHeight(cSizeH);
        newCreature.MyEquipHandler.setHeadX(cHeadX);
        newCreature.MyEquipHandler.setHeadY(cHeadY);
        newCreature.MyEquipHandler.setWeaponX(cWeaponX);
        newCreature.MyEquipHandler.setWeaponY(cWeaponY);
        newCreature.MyEquipHandler.setOffHandX(cOffHandX);
        newCreature.MyEquipHandler.setOffHandY(cOffHandY);
        newCreature.MyEquipHandler.setAmuletX(cAmuletX);
        newCreature.MyEquipHandler.setAmuletY(cAmuletY);
        newCreature.MyEquipHandler.setArtifactX(cArtifactX);
        newCreature.MyEquipHandler.setArtifactY(cArtifactY);

        newCreature.getCustomization().setMouthFeatureX(cMouthFeatureX);
        newCreature.getCustomization().setMouthFeatureY(cMouthFeatureY);
        newCreature.getCustomization().setAccessoriesX(cAccessoriesX);
        newCreature.getCustomization().setAccessoriesY(cAccessoriesY);
        newCreature.getCustomization().setSkinFeatureX(cSkinFeatureX);
        newCreature.getCustomization().setSkinFeatureY(cSkinFeatureY);

        creatureDefinitions.put(cId, newCreature);
      }

      BlueSaga.playerCharacter.updateItemCoordinates();

      ScreenHandler.LoadingStatus = "Loading map data...";

      Gui.MapWindow.load();

    }
    /*
    else if(serverData.startsWith("<mobpos>")){
    	String mobsdata[] = serverData.substring(8).split(";");

    	for(String mobdata: mobsdata){
    		String creatureInfo[] = mobdata.split("/");

    		Creature mob = BlueSaga.MapHandler.addCreatureToScreen(creatureInfo[0]);

    		String moveInfo[] = creatureInfo[1].split(",");
    		int newX = Integer.parseInt(moveInfo[0]);
    		int newY = Integer.parseInt(moveInfo[1]);
    		int cSpeed = Integer.parseInt(moveInfo[2]);

    		mob.MyWalkHandler.walkTo(newX,newY,cSpeed);
    	}
    }
    */
    else if (serverData.startsWith("<aggroinfo>")) {
      String mobaggro = serverData.substring(11);
      BlueSaga.WORLD_MAP.changeMonstersAggroState(mobaggro);
    } else if (serverData.startsWith("<respawnmonster>")) {
      String respawnInfo = serverData.substring(16);

      String creatureInfo[] = respawnInfo.split(",");

      int cX = Integer.parseInt(creatureInfo[3]);
      int cY = Integer.parseInt(creatureInfo[4]);
      int cZ = Integer.parseInt(creatureInfo[5]);

      Creature m = MapHandler.addCreatureToScreen(respawnInfo);
      m.setX(cX);
      m.setY(cY);
      m.setZ(cZ);
      m.revive();
      MapHandler.updateScreenObjects();
      m.appear();
    } else if (serverData.startsWith("<monstergone>")) {
      String monsterInfo = serverData.substring(13);
      Creature m = MapHandler.addCreatureToScreen(monsterInfo);
      ScreenHandler.SCREEN_OBJECTS_WITH_ID
          .get("Monster" + m.getDBId())
          .getCreature()
          .setRemoved(true);
      if (ScreenHandler.SCREEN_TILES.get(m.getX() + "," + m.getY() + "," + m.getZ()) != null) {
        ScreenHandler.SCREEN_TILES
            .get(m.getX() + "," + m.getY() + "," + m.getZ())
            .setOccupant(CreatureType.None, 0);
      }
      MapHandler.updateScreenObjects();
      m.clearDamageLabels();
      m.setDead(false);
      m.dissappear();
    }
  }
}
