package instances;

import java.awt.Point;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import creature.Npc;
import creature.PlayerCharacter;
import map.Door;
import map.Tile;
import network.Server;
import utils.RandomUtils;
import utils.ServerGameInfo;
import utils.ServerMessage;

public class DungeonGenerator {

  private static HashMap<String, Integer> map;

  private Vector<String> level_10_mobs;
  private Vector<String> level_20_mobs;
  private Vector<String> level_30_mobs;
  private Vector<String> level_40_mobs;

  private String monstersId[] = new String[0];

  private String dungeonType = "cave";
  private int areaEffectId = 87;

  public DungeonGenerator() {

    // LEVEL 10+
    level_10_mobs = new Vector<String>();

    // Larvas, Scarabs, Ruby scarabs
    level_10_mobs.add("15,9,10");

    // Spiders, Toxic Spider, Bat
    level_10_mobs.add("6,34,4");

    // Bone piles, Skeletons, Ghoul Rat
    level_10_mobs.add("28,29,8");

    // LEVEL 20+
    level_20_mobs = new Vector<String>();

    //Rock Toad 76
    // Green Slime, Rock Toad, Rat
    level_20_mobs.add("7,5,76");

    // Bunny, Angry flower, Hungry hungry plant,
    level_20_mobs.add("43,41,42");

    // Spear elf, Dart Elf
    level_20_mobs.add("44,45");

    // LEVEL 30+
    level_30_mobs = new Vector<String>();

    // Shroom, Toxic shroom, Giant Shroom
    level_30_mobs.add("21,22,23");

    // Goblin warrior, Goblin archer, Goblin bomber, Ogre
    level_30_mobs.add("14,11,59,48");

    // Necro goblin warrior, Necro goblin archer, Necromancer, Goblin King
    level_30_mobs.add("73,74,71,75");
  }

  public void pirateIslandGenerate(
      HashMap<String, Tile> MapTiles,
      int monsterGroup,
      int entrance1X,
      int entrance1Y,
      int entrance1Z,
      int instanceZ) {
    areaEffectId = 90;
    dungeonType = "cave";

    monstersId = new String[0];

    if (monsterGroup == 1) {
      monstersId = level_10_mobs.get(0).split(",");
    } else if (monsterGroup == 2) {
      monstersId = level_10_mobs.get(1).split(",");
    } else if (monsterGroup == 3) {
      monstersId = level_10_mobs.get(2).split(",");
    } else if (monsterGroup == 4) {
      monstersId = level_20_mobs.get(0).split(",");
    } else if (monsterGroup == 5) {
      monstersId = level_20_mobs.get(1).split(",");
    } else if (monsterGroup == 6) {
      monstersId = level_20_mobs.get(2).split(",");
    } else if (monsterGroup == 6) {
      monstersId = level_20_mobs.get(3).split(",");
    } else if (monsterGroup == 7) {
      monstersId = level_30_mobs.get(0).split(",");
    }

    generateDungeon(MapTiles, entrance1X, entrance1Y, entrance1Z, instanceZ);
  }

  public void generate(HashMap<String, Tile> MapTiles, int level) {
    ServerMessage.printMessage("Generate instance dungeon...", true);

    int entrance1X = 0;
    int entrance1Y = 0;
    int entrance1Z = 0;

    areaEffectId = 87;

    if (level == 10) {
      entrance1X = 5161;
      entrance1Y = 10123;
      entrance1Z = 0;

      areaEffectId = 88;
      dungeonType = "wooddungeon";

      int randomMonsterIndex = RandomUtils.getInt(0, level_10_mobs.size() - 1);
      monstersId = level_10_mobs.get(randomMonsterIndex).split(",");
    } else if (level == 20) {
      entrance1X = 5455;
      entrance1Y = 10448;
      entrance1Z = 0;

      areaEffectId = 89;
      dungeonType = "dirtcave";

      int randomMonsterIndex = RandomUtils.getInt(0, level_20_mobs.size() - 1);
      monstersId = level_20_mobs.get(randomMonsterIndex).split(",");
    } else if (level == 30) {
      entrance1X = 5028;
      entrance1Y = 9516;
      entrance1Z = 0;

      areaEffectId = 90;
      dungeonType = "cave";

      int randomMonsterIndex = RandomUtils.getInt(0, level_30_mobs.size() - 1);
      monstersId = level_30_mobs.get(randomMonsterIndex).split(",");
    } else if (level == 40) {
      int randomMonsterIndex = RandomUtils.getInt(0, level_40_mobs.size() - 1);
      monstersId = level_40_mobs.get(randomMonsterIndex).split(",");
    }

    int instanceZ = -100 - level;

    generateDungeon(MapTiles, entrance1X, entrance1Y, entrance1Z, instanceZ);

    // FOR DEV ONLY
    // Place player at entrance of cave
    //Server.userDB.updateDB("update user_character set X = "+entrance1X+", Y = "+(entrance1Y+1)+", Z = "+entrance1Z+" where Id = 2");
  }

  private void generateDungeon(
      HashMap<String, Tile> MapTiles,
      int entrance1X,
      int entrance1Y,
      int entrance1Z,
      int instanceZ) {

    int brushZ = instanceZ;

    int nrLevels = monstersId.length;
    Vector<String> levelStairs = new Vector<String>();

    int prevEntranceX = entrance1X;
    int prevEntranceY = entrance1Y;

    for (int i = 0; i < nrLevels; i++) {
      map = new HashMap<String, Integer>();

      Server.WORLD_MAP.zLevels.add(brushZ);
      Server.WORLD_MAP.monstersByZ.put(brushZ, new Vector<Npc>());
      Server.WORLD_MAP.playersByZ.put(brushZ, new Vector<PlayerCharacter>());

      Vector<String> monster_to_place = new Vector<String>();

      int brushX = 0;
      int brushY = 0;
      int brushDirection = RandomUtils.getInt(0, 7);

      int levelSize = RandomUtils.getInt(256, 1024);
      int levelSizeItr = levelSize;

      int dirItr = 0;
      int holdDirThreshold = 3;

      int monster_index = i;
      int bossX = 0;
      int bossY = 0;
      int bossZ = 0;

      while (levelSizeItr > 0) {
        // Use a cave brush to draw
        Iterator<Entry<Point, Integer>> brush_it =
            GeneratorBrush.getCaveBrush().entrySet().iterator();

        while (brush_it.hasNext()) {
          Entry<Point, Integer> b = brush_it.next();

          int tileX = brushX + b.getKey().x - 2;
          int tileY = brushY + b.getKey().y - 2;

          int tileNr = b.getValue() - 1;

          if (tileNr > -1) {
            if (!map.containsKey(tileX + "," + tileY + "," + brushZ)) {
              map.put(tileX + "," + tileY + "," + brushZ, tileNr);
            } else if (tileNr < map.get(tileX + "," + tileY + "," + brushZ)) {
              map.put(tileX + "," + tileY + "," + brushZ, tileNr);
            }
          }
        }

        /*
        if(levelSizeItr == Math.round(levelSize/2)){
        	monster_index++;
        	System.out.println("monster index: "+monster_index);
        	if(monster_index >= monstersId.length){
        		monster_index = monstersId.length-1;
        	}
        }
        */

        // Chance of spawning monster
        int chanceOfMonster = RandomUtils.getInt(0, 20);
        if (chanceOfMonster == 0 && !(brushX == 0 && brushY == 0)) {

          int random_monster_index = RandomUtils.getInt(0, monster_index + 1);

          if (random_monster_index >= monstersId.length) {
            random_monster_index--;
          }

          int monsterId = Integer.parseInt(monstersId[random_monster_index]);

          // Lower the chance of a boss monster
          if (ServerGameInfo.creatureDef.get(monsterId) != null) {
            if (ServerGameInfo.creatureDef.get(monsterId).isBoss()) {
              int chanceOfBossMonster = RandomUtils.getInt(0, 3);
              if (chanceOfBossMonster > 0) {
                // Place another monster than boss
                monsterId = Integer.parseInt(monstersId[0]);
              }
            }
          }

          int monsterX = prevEntranceX + brushX;
          int monsterY = prevEntranceY + brushY;

          monster_to_place.add(monsterX + "," + monsterY + "," + brushZ + "," + monsterId);

          if (i == nrLevels - 1) {
            bossX = monsterX;
            bossY = monsterY;
            bossZ = brushZ;
          }
        }

        // Move the brush in a random direction
        if (levelSizeItr > 0) {
          int oldBrushX = brushX;
          int oldBrushY = brushY;

          switch (brushDirection) {
            case 0:
              brushY--;
              break;
            case 1:
              brushY--;
              brushX++;
              break;
            case 2:
              brushX++;
              break;
            case 3:
              brushY++;
              brushX++;
              break;
            case 4:
              brushY++;
              break;
            case 5:
              brushY++;
              brushX--;
              break;
            case 6:
              brushX--;
              break;
            case 7:
              brushY--;
              brushX--;
              break;
          }

          boolean placeTiles = true;
          // Check if there is something there
          while (brush_it.hasNext()) {
            Entry<Point, Integer> b = brush_it.next();

            int tileX = brushX + b.getKey().x - 2;
            int tileY = brushY + b.getKey().y - 2;

            if (b.getValue() > 0) {
              if (map.containsKey(tileX + "," + tileY + "," + brushZ)) {
                placeTiles = false;
                break;
              }
            }
          }

          if (!placeTiles) {
            brushX = oldBrushX;
            brushY = oldBrushY;
            dirItr = holdDirThreshold;
          } else {
            // Lower size left to draw
            levelSizeItr--;
          }

          if (dirItr > holdDirThreshold) {
            brushDirection = RandomUtils.getInt(0, 7);
            dirItr = 0;
          } else {
            dirItr++;
          }
        }
      }

      // Placing ground around last spot so that stairs can fit
      Iterator<Entry<Point, Integer>> brush_it =
          GeneratorBrush.getCaveBrush().entrySet().iterator();

      while (brush_it.hasNext()) {
        Entry<Point, Integer> b = brush_it.next();

        int tileX = brushX + b.getKey().x - 2;
        int tileY = brushY + b.getKey().y - 2;

        int tileNr = b.getValue() - 1;

        if (tileNr > -1) {
          if (!map.containsKey(tileX + "," + tileY + "," + brushZ)) {
            map.put(tileX + "," + tileY + "," + brushZ, tileNr);
          } else if (tileNr < map.get(tileX + "," + tileY + "," + brushZ)) {
            map.put(tileX + "," + tileY + "," + brushZ, tileNr);
          }
        }
      }

      boolean removedTile = true;

      while (removedTile) {
        Iterator<Map.Entry<String, Integer>> fix_it = map.entrySet().iterator();
        removedTile = false;
        while (fix_it.hasNext()) {
          Map.Entry<String, Integer> pairs = fix_it.next();

          String tileCoord[] = pairs.getKey().split(",");
          int keyX = Integer.parseInt(tileCoord[0]);
          int keyY = Integer.parseInt(tileCoord[1]);
          int keyZ = Integer.parseInt(tileCoord[2]);

          if (pairs.getValue() == 1) {
            boolean removeTile = false;

            if (equalTile(map, (keyX - 1) + "," + keyY + "," + keyZ, 0)
                && equalTile(map, (keyX + 1) + "," + keyY + "," + keyZ, 0)) {
              removeTile = true;
            } else if (equalTile(map, (keyX) + "," + (keyY + 1) + "," + keyZ, 0)
                && equalTile(map, (keyX) + "," + (keyY - 1) + "," + keyZ, 0)) {
              removeTile = true;
            }
            if (removeTile) {
              removedTile = true;
              pairs.setValue(0);
            }
          }
        }
      }

      Iterator<Map.Entry<String, Integer>> map_it = map.entrySet().iterator();

      // Place stairs
      int stairsX = prevEntranceX + brushX;
      int stairsY = prevEntranceY + brushY;
      int stairsZ = brushZ;

      levelStairs.add(stairsX + "," + stairsY + "," + stairsZ);

      map_it = map.entrySet().iterator();

      System.out.println("ENTRANCE X,Y,Z: " + prevEntranceX + "," + prevEntranceY);
      while (map_it.hasNext()) {
        Map.Entry<String, Integer> pairs = map_it.next();

        String tileCoord[] = pairs.getKey().split(",");
        int keyX = Integer.parseInt(tileCoord[0]);
        int keyY = Integer.parseInt(tileCoord[1]);
        int keyZ = Integer.parseInt(tileCoord[2]);

        int tileX = prevEntranceX + keyX;
        int tileY = prevEntranceY + keyY;
        int tileZ = keyZ;

        int tileNr = pairs.getValue();

        // If wall, decide wall type
        if (tileNr == 1) {

          String caveType = "";
          String tileName = "1";

          int passable = 0;
          if (equalTile(map, (keyX - 1) + "," + (keyY) + "," + keyZ, 0)
              && equalTile(map, (keyX) + "," + (keyY - 1) + "," + keyZ, 0)
              && equalTile(map, (keyX - 1) + "," + (keyY - 1) + "," + keyZ, 0)) {
            tileName = caveType + "IUL";
          } else if (equalTile(map, (keyX + 1) + "," + (keyY) + "," + keyZ, 0)
              && equalTile(map, (keyX) + "," + (keyY - 1) + "," + keyZ, 0)
              && equalTile(map, (keyX + 1) + "," + (keyY - 1) + "," + keyZ, 0)) {
            tileName = caveType + "IUR";
          } else if (equalTile(map, (keyX - 1) + "," + (keyY) + "," + keyZ, 0)
              && equalTile(map, (keyX) + "," + (keyY + 1) + "," + keyZ, 0)
              && equalTile(map, (keyX - 1) + "," + (keyY + 1) + "," + keyZ, 0)) {
            tileName = caveType + "IDL";
          } else if (equalTile(map, (keyX + 1) + "," + (keyY) + "," + keyZ, 0)
              && equalTile(map, (keyX) + "," + (keyY + 1) + "," + keyZ, 0)
              && equalTile(map, (keyX + 1) + "," + (keyY + 1) + "," + keyZ, 0)) {
            tileName = caveType + "IDR";
          } else if (equalTile(map, (keyX - 1) + "," + (keyY - 1) + "," + keyZ, 0)
              && equalTile(map, (keyX - 1) + "," + (keyY) + "," + keyZ, 1)
              && equalTile(map, (keyX) + "," + (keyY - 1) + "," + keyZ, 1)) {
            tileName = caveType + "DR";
          } else if (equalTile(map, (keyX + 1) + "," + (keyY - 1) + "," + keyZ, 0)
              && equalTile(map, (keyX + 1) + "," + (keyY) + "," + keyZ, 1)
              && equalTile(map, (keyX) + "," + (keyY - 1) + "," + keyZ, 1)) {
            tileName = caveType + "DL";
          } else if (equalTile(map, (keyX + 1) + "," + (keyY + 1) + "," + keyZ, 0)
              && equalTile(map, (keyX + 1) + "," + (keyY) + "," + keyZ, 1)
              && equalTile(map, (keyX) + "," + (keyY + 1) + "," + keyZ, 1)) {
            tileName = caveType + "UL";
          } else if (equalTile(map, (keyX - 1) + "," + (keyY + 1) + "," + keyZ, 0)
              && equalTile(map, (keyX - 1) + "," + (keyY) + "," + keyZ, 1)
              && equalTile(map, (keyX) + "," + (keyY + 1) + "," + keyZ, 1)) {
            tileName = caveType + "UR";
          } else if (equalTile(map, (keyX - 1) + "," + (keyY) + "," + keyZ, 0)) {
            tileName = caveType + "R";
          } else if (equalTile(map, (keyX + 1) + "," + (keyY) + "," + keyZ, 0)) {
            tileName = caveType + "L";
          } else if (equalTile(map, (keyX) + "," + (keyY - 1) + "," + keyZ, 0)) {
            tileName = caveType + "D";
          } else if (equalTile(map, (keyX) + "," + (keyY + 1) + "," + keyZ, 0)) {
            tileName = caveType + "U";
          }

          if (!tileName.equals("1")) {
            Tile newTile = new Tile(tileX, tileY, tileZ);
            newTile.setType(dungeonType, tileName, passable);
            MapTiles.put(tileX + "," + tileY + "," + tileZ, newTile);
          }
        } else if (tileNr == 0) {
          String tileName = "1";

          int passable = 1;

          int floorNr = RandomUtils.getInt(1, 10);

          if (floorNr == 0) {
            floorNr = 1;
          } else if (floorNr == 3) {
            if (dungeonType.equals("wooddungeon")) {
              floorNr = 1;
            }
          } else if (floorNr > 2) {
            floorNr = 1;
          }

          tileName = "" + floorNr;

          Tile newTile = new Tile(tileX, tileY, tileZ);
          newTile.setType(dungeonType, tileName, passable);
          MapTiles.put(tileX + "," + tileY + "," + tileZ, newTile);

          // Chance of spawning barrel
          // Need to be close to wall
          boolean nearWall = false;
          if (equalTile(map, (keyX - 1) + "," + (keyY) + "," + keyZ, 1)
              && equalTile(map, (keyX) + "," + (keyY - 1) + "," + keyZ, 1)) {
            nearWall = true;
          } else if (equalTile(map, (keyX + 1) + "," + (keyY) + "," + keyZ, 1)
              && equalTile(map, (keyX) + "," + (keyY - 1) + "," + keyZ, 1)) {
            nearWall = true;
          } else if (equalTile(map, (keyX - 1) + "," + (keyY) + "," + keyZ, 1)
              && equalTile(map, (keyX) + "," + (keyY + 1) + "," + keyZ, 1)) {
            nearWall = true;
          } else if (equalTile(map, (keyX + 1) + "," + (keyY) + "," + keyZ, 1)
              && equalTile(map, (keyX) + "," + (keyY + 1) + "," + keyZ, 1)) {
            nearWall = true;
          }

          if (nearWall) {
            int chanceOfBarrel = RandomUtils.getInt(0, 10);
            if (chanceOfBarrel == 0) {
              newTile.setObjectId("container/barrel");
              newTile.setPassable(false);
            }
          }
        }
      }

      ServerMessage.printMessage("Placing monsters...", true);

      for (String monster : monster_to_place) {
        String monsterInfo[] = monster.split(",");
        int monsterX = Integer.parseInt(monsterInfo[0]);
        int monsterY = Integer.parseInt(monsterInfo[1]);
        int monsterZ = Integer.parseInt(monsterInfo[2]);
        int monsterId = Integer.parseInt(monsterInfo[3]);

        if (MapTiles.containsKey(monsterX + "," + monsterY + "," + monsterZ)) {
          Npc tempNpc =
              new Npc(ServerGameInfo.creatureDef.get(monsterId), monsterX, monsterY, monsterZ);
          tempNpc.setOriginalAggroType(2);
          tempNpc.setAggroType(2);

          if (bossX == monsterX && bossY == monsterY && bossZ == monsterZ) {
            System.out.println("PLACE BOSS!");
            tempNpc.turnTitan(true);
          } else {
            if (instanceZ <= 200) {
              tempNpc.turnRaging();
            } else {
              tempNpc.turnElite();
            }
          }
          Server.WORLD_MAP.addMonster(tempNpc);
        }
      }

      prevEntranceX = stairsX;
      prevEntranceY = stairsY;

      brushZ--;
    }

    int entranceGotoX = entrance1X;
    int entranceGotoY = entrance1Y - 1;

    // Instance Entrance to level 1
    Door instanceEntranceDoor =
        new Door(entrance1X, entrance1Y, entrance1Z, entranceGotoX, entranceGotoY, instanceZ);
    MapTiles.get(entrance1X + "," + entrance1Y + "," + entrance1Z)
        .setGeneratedDoor(instanceEntranceDoor);
    MapTiles.get(entrance1X + "," + entrance1Y + "," + entrance1Z).setPassable(true);

    Door instanceExitDoor =
        new Door(entrance1X, entrance1Y, instanceZ, entrance1X, entrance1Y + 1, entrance1Z);
    MapTiles.get(entrance1X + "," + entrance1Y + "," + instanceZ)
        .setGeneratedDoor(instanceExitDoor);
    MapTiles.get(entrance1X + "," + entrance1Y + "," + instanceZ)
        .setType(dungeonType, "Stairsup", 1);
    MapTiles.get(entranceGotoX + "," + entranceGotoY + "," + instanceZ)
        .setAreaEffectId(areaEffectId);

    // Clear surrounding tiles from objects
    for (int i = -2; i <= 2; i++) {
      for (int j = -2; j <= 2; j++) {
        int checkX = entrance1X + i;
        int checkY = entrance1Y + j;
        if (MapTiles.get(checkX + "," + checkY + "," + instanceZ) != null) {
          if (!MapTiles.get(checkX + "," + checkY + "," + instanceZ).getObjectId().equals("None")) {
            MapTiles.get(checkX + "," + checkY + "," + instanceZ).setObjectId("None");
            MapTiles.get(checkX + "," + checkY + "," + instanceZ).setPassable(true);
          }
        }
      }
    }

    // Placing and linking level doors

    for (int levelNr = 0; levelNr < nrLevels; levelNr++) {
      String levelStairsXYZ[] = levelStairs.get(levelNr).split(",");
      int stairsX = Integer.parseInt(levelStairsXYZ[0]);
      int stairsY = Integer.parseInt(levelStairsXYZ[1]);
      int stairsZ = Integer.parseInt(levelStairsXYZ[2]);

      int stairsGotoX = stairsX;
      int stairsGotoY = stairsY - 1;
      int stairsGotoZ = stairsZ - 1;

      // Clear surrounding tiles from objects
      for (int i = -2; i <= 2; i++) {
        for (int j = -2; j <= 2; j++) {
          int checkX = stairsX + i;
          int checkY = stairsY + j;
          if (MapTiles.get(checkX + "," + checkY + "," + stairsZ) != null) {
            if (!MapTiles.get(checkX + "," + checkY + "," + stairsZ).getObjectId().equals("None")) {
              MapTiles.get(checkX + "," + checkY + "," + stairsZ).setObjectId("None");
              MapTiles.get(checkX + "," + checkY + "," + stairsZ).setPassable(true);
            }
          }
          if (MapTiles.get(checkX + "," + checkY + "," + stairsGotoZ) != null) {
            if (!MapTiles.get(checkX + "," + checkY + "," + stairsGotoZ)
                .getObjectId()
                .equals("None")) {
              MapTiles.get(checkX + "," + checkY + "," + stairsGotoZ).setObjectId("None");
              MapTiles.get(checkX + "," + checkY + "," + stairsGotoZ).setPassable(true);
            }
          }
        }
      }

      if (levelNr < nrLevels - 1) {
        Door stairsDown =
            new Door(stairsX, stairsY, stairsZ, stairsGotoX, stairsGotoY, stairsGotoZ);
        MapTiles.get(stairsX + "," + stairsY + "," + stairsZ).setGeneratedDoor(stairsDown);
        MapTiles.get(stairsX + "," + stairsY + "," + stairsZ).setType(dungeonType, "Stairsdown", 1);

        Door stairsUp = new Door(stairsX, stairsY, stairsGotoZ, stairsX, stairsY + 1, stairsZ);
        MapTiles.get(stairsX + "," + stairsY + "," + stairsGotoZ).setGeneratedDoor(stairsUp);
        MapTiles.get(stairsX + "," + stairsY + "," + stairsGotoZ)
            .setType(dungeonType, "Stairsup", 1);

      } else {
        Door portal = new Door(stairsX, stairsY, stairsZ, entrance1X, entrance1Y + 2, entrance1Z);
        MapTiles.get(stairsX + "," + stairsY + "," + stairsZ).setGeneratedDoor(portal);
        MapTiles.get(stairsX + "," + stairsY + "," + stairsZ).setObjectId("special/portal");
      }
    }
  }

  private boolean equalTile(HashMap<String, Integer> map, String key, int checkValue) {
    if (map.containsKey(key)) {
      if (map.get(key) == checkValue) {
        return true;
      }
    }
    return false;
  }
}
