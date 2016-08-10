package instances;

import java.awt.Point;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;

import map.Door;
import map.Tile;
import network.Server;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import creature.Npc;
import utils.RandomUtils;
import utils.ServerGameInfo;
import utils.Spiral;

public class PirateIslandGenerator {

  private static HashMap<String, Integer> mapTiles;

  private static int entranceDoorId = 483;

  private static int pirateIslandX = 5000;
  private static int pirateIslandY = 10000;
  private static int pirateIslandZ = -200;

  private static Vector<int[]> nullPoints;

  private static int nrMonsters = 20;

  private static Vector<Integer> monstersToPlace = new Vector<Integer>();
  private static Vector<Integer> monstersSpawned = new Vector<Integer>();

  public static void generate(HashMap<String, Tile> worldMapTiles) {

    nullPoints = new Vector<int[]>();

    // Generate archipelago
    int nrIslands = 30; //RandomUtils.getInt(80,150);
    ArchipelagoGenerator.generate(nrIslands);
    mapTiles = ArchipelagoGenerator.getTiles();

    cleanUp();
    placeMountains();
    placeShallowSpots();

    fixTextures(worldMapTiles);

    monstersSpawned = new Vector<Integer>();

    monstersToPlace.add(15);
    monstersToPlace.add(9);
    monstersToPlace.add(10);

    monstersToPlace.add(6);
    monstersToPlace.add(34);
    monstersToPlace.add(4);

    monstersToPlace.add(30); // Pink Squid

    monstersToPlace.add(28);
    monstersToPlace.add(29);
    monstersToPlace.add(8);

    monstersToPlace.add(5);
    monstersToPlace.add(7);
    monstersToPlace.add(76);

    monstersToPlace.add(43);
    monstersToPlace.add(41);
    monstersToPlace.add(42);

    monstersToPlace.add(44);
    monstersToPlace.add(45);

    monstersToPlace.add(21);
    monstersToPlace.add(22);
    monstersToPlace.add(23);

    int nrCaves = 7;

    nrCaves = placeCaveEntrances(worldMapTiles, nrCaves);

    placeTrees(worldMapTiles);

    placeOutdoorMonsters(worldMapTiles);
  }

  private static void placeOutdoorMonsters(HashMap<String, Tile> worldMapTiles) {

    for (int monsterId : monstersToPlace) {
      //if(!monstersSpawned.contains(monsterId)){

      int monsterChance = RandomUtils.getInt(1000, 4000);

      for (Tile t : worldMapTiles.values()) {

        monsterChance--;
        if (monsterChance <= 0) {
          boolean spawnMonster = false;
          boolean spawnSquid = false;

          if (t.isWater() && monsterId == 30) {
            spawnSquid = true;
          } else if (!t.isWater() && monsterId != 30) {
            spawnMonster = true;
          }

          if (spawnMonster || spawnSquid) {
            Spiral s = new Spiral(20, 20);
            List<Point> l = s.spiral();

            for (Point p : l) {
              int worldMapX = (int) p.getX() + t.getX();
              int worldMapY = (int) p.getY() + t.getY();
              int worldMapZ = pirateIslandZ;

              int spawnChance = RandomUtils.getInt(0, 30);

              if (spawnChance == 0) {

                if (worldMapTiles.get(worldMapX + "," + worldMapY + "," + worldMapZ) != null) {

                  // Titan chance
                  int chanceOfTitan = RandomUtils.getInt(0, 60);
                  boolean titan = false;
                  if (chanceOfTitan == 0) {
                    titan = true;
                  }

                  boolean spawnOk = false;

                  if (spawnSquid
                      && worldMapTiles
                          .get(worldMapX + "," + worldMapY + "," + worldMapZ)
                          .isWater()) {
                    // SPAWN SQUID
                    spawnOk = true;
                  } else if (spawnMonster
                      && !worldMapTiles
                          .get(worldMapX + "," + worldMapY + "," + worldMapZ)
                          .isWater()) {
                    // OTHER MONSTERS
                    if (worldMapTiles
                        .get(worldMapX + "," + worldMapY + "," + worldMapZ)
                        .isPassable()) {
                      spawnOk = true;
                    }
                  }

                  if (spawnOk) {
                    Npc tempNpc =
                        new Npc(
                            ServerGameInfo.creatureDef.get(monsterId),
                            worldMapX,
                            worldMapY,
                            worldMapZ);
                    tempNpc.setOriginalAggroType(2);
                    tempNpc.setAggroType(2);

                    tempNpc.turnTitan(titan);
                    tempNpc.turnRaging();

                    Server.WORLD_MAP.addMonster(tempNpc);
                  }
                }
              }
            }
            monstersSpawned.add(monsterId);

            break;
          }
        }
      }
    }
    //}
  }

  private static void placeTrees(HashMap<String, Tile> MapTiles) {
    Iterator<Entry<String, Integer>> it = mapTiles.entrySet().iterator();

    HashMap<String, String> trees = new HashMap<String, String>();

    while (it.hasNext()) {
      Entry<String, Integer> t = it.next();

      String tCoord[] = t.getKey().split(",");

      int keyX = Integer.parseInt(tCoord[0]);
      int keyY = Integer.parseInt(tCoord[1]);
      int keyZ = Integer.parseInt(tCoord[2]);

      int tileNr = t.getValue();

      if (tileNr == 4 || tileNr == 6) {
        int chanceOfTrees = RandomUtils.getInt(0, 200);
        if (chanceOfTrees == 0 && (keyX + keyY) % 2 == 0) {
          int nrTrees = RandomUtils.getInt(4, 100);
          int treeType = RandomUtils.getInt(1, 4);
          int treeX = keyX;
          int treeY = keyY;
          int treeZ = keyZ;
          while (nrTrees > 0) {

            if (mapTiles.get(treeX + "," + treeY + "," + treeZ) != null
                && (mapTiles.get(treeX + "," + treeY + "," + treeZ) == 4
                    || mapTiles.get(treeX + "," + treeY + "," + treeZ) == 6)) {
              String treeId = "tree1";

              if (treeType == 1) {
                int randomTree = RandomUtils.getInt(1, 2);
                if (randomTree == 2) {
                  treeId = "tree2";
                }
              } else if (treeType == 2) {
                treeId = "tree3";
              } else if (treeType == 3) {
                int randomTree = RandomUtils.getInt(1, 2);
                if (randomTree == 1) {
                  treeId = "tree6";
                } else if (randomTree == 2) {
                  treeId = "tree7";
                }
              }

              int rottenTree = RandomUtils.getInt(0, 20);

              if (rottenTree == 0) {
                treeId = "tree4";
              } else if (rottenTree == 1) {
                treeId = "tree5";
              }

              trees.put(treeX + "," + treeY + "," + treeZ, treeId);
            }
            int direction = RandomUtils.getInt(1, 8);
            if (direction == 1) {
              treeY -= 2;
            } else if (direction == 2) {
              treeY--;
              treeX++;
            } else if (direction == 3) {
              treeX += 2;
            } else if (direction == 4) {
              treeY++;
              treeX++;
            } else if (direction == 5) {
              treeY += 2;
            } else if (direction == 6) {
              treeY++;
              treeX--;
            } else if (direction == 7) {
              treeX -= 2;
            } else if (direction == 8) {
              treeY--;
              treeX--;
            }
            nrTrees--;
          }
        }
      }
    }

    Iterator<Entry<String, String>> tree_it = trees.entrySet().iterator();

    while (tree_it.hasNext()) {
      Entry<String, String> t = tree_it.next();

      String tCoord[] = t.getKey().split(",");

      int keyX = Integer.parseInt(tCoord[0]) + pirateIslandX;
      int keyY = Integer.parseInt(tCoord[1]) + pirateIslandY;
      int keyZ = Integer.parseInt(tCoord[2]) + pirateIslandZ;

      String treeType = t.getValue();

      if (MapTiles.get(keyX + "," + keyY + "," + keyZ) != null) {
        MapTiles.get(keyX + "," + keyY + "," + keyZ).setObjectId("tree/" + treeType);
        MapTiles.get(keyX + "," + keyY + "," + keyZ).setPassable(false);
      }
    }
  }

  private static void fixTextures(HashMap<String, Tile> worldMapTiles) {
    Iterator<Entry<String, Integer>> it = mapTiles.entrySet().iterator();
    int keyX = 0;
    int keyY = 0;
    int keyZ = 0;

    int worldMapX = keyX + pirateIslandX;
    int worldMapY = keyY + pirateIslandY;
    int worldMapZ = keyZ + pirateIslandZ;

    boolean saveEntrancePos = false;
    boolean placeEntrance = false;

    int entranceX = 0;
    int entranceY = 0;
    int entranceZ = 0;

    while (it.hasNext()) {
      Entry<String, Integer> t = it.next();

      String tCoord[] = t.getKey().split(",");

      keyX = Integer.parseInt(tCoord[0]);
      keyY = Integer.parseInt(tCoord[1]);
      keyZ = Integer.parseInt(tCoord[2]);

      worldMapX = keyX + pirateIslandX;
      worldMapY = keyY + pirateIslandY;
      worldMapZ = keyZ + pirateIslandZ;

      int tileNr = t.getValue();

      String tileName = "1";
      String lowerType = "";
      String tileType = "water";
      int passable = 1;

      if (tileNr == 2) {
        lowerType = "water";
        tileType = "shallow";
      } else if (tileNr == 3) {
        lowerType = "shallow";
        tileType = "sand";
      } else if (tileNr == 4) {
        if (!saveEntrancePos) {
          saveEntrancePos = true;
          Server.mapDB.updateDB(
              "UPDATE door SET GotoX = "
                  + worldMapX
                  + ", GotoY = "
                  + worldMapY
                  + ", GotoZ = "
                  + worldMapZ
                  + " WHERE Id = "
                  + entranceDoorId);
          //Server.userDB.updateDB("UPDATE user_character SET X = "+worldMapX+", Y = "+worldMapY+", Z = "+worldMapZ+" WHERE Id = 2");

          entranceX = worldMapX;
          entranceY = worldMapY;
          entranceZ = worldMapZ;
        }
        lowerType = "sand";
        tileType = "grass";
      } else if (tileNr == 5) {
        lowerType = "grass";
        tileType = "grasscliff";
        passable = 0;
      }

      if (tileNr < 6) {
        if (equalTile(mapTiles, (keyX - 1) + "," + (keyY) + "," + keyZ, tileNr)
            && equalTile(mapTiles, (keyX) + "," + (keyY - 1) + "," + keyZ, tileNr)
            && !equalTile(mapTiles, (keyX + 1) + "," + (keyY) + "," + keyZ, tileNr)
            && !equalTile(mapTiles, (keyX) + "," + (keyY + 1) + "," + keyZ, tileNr)) {
          tileName = lowerType + "DR";
        } else if (equalTile(mapTiles, (keyX + 1) + "," + (keyY) + "," + keyZ, tileNr)
            && equalTile(mapTiles, (keyX) + "," + (keyY - 1) + "," + keyZ, tileNr)
            && !equalTile(mapTiles, (keyX - 1) + "," + (keyY) + "," + keyZ, tileNr)
            && !equalTile(mapTiles, (keyX) + "," + (keyY + 1) + "," + keyZ, tileNr)) {
          tileName = lowerType + "DL";
        } else if (equalTile(mapTiles, (keyX - 1) + "," + (keyY) + "," + keyZ, tileNr)
            && equalTile(mapTiles, (keyX) + "," + (keyY + 1) + "," + keyZ, tileNr)
            && !equalTile(mapTiles, (keyX + 1) + "," + (keyY) + "," + keyZ, tileNr)
            && !equalTile(mapTiles, (keyX) + "," + (keyY - 1) + "," + keyZ, tileNr)) {
          tileName = lowerType + "UR";
        } else if (equalTile(mapTiles, (keyX + 1) + "," + (keyY) + "," + keyZ, tileNr)
            && equalTile(mapTiles, (keyX) + "," + (keyY + 1) + "," + keyZ, tileNr)
            && !equalTile(mapTiles, (keyX - 1) + "," + (keyY) + "," + keyZ, tileNr)
            && !equalTile(mapTiles, (keyX) + "," + (keyY - 1) + "," + keyZ, tileNr)) {
          tileName = lowerType + "UL";
        } else if (equalTile(mapTiles, (keyX - 1) + "," + (keyY) + "," + keyZ, tileNr)
            && equalTile(mapTiles, (keyX) + "," + (keyY + 1) + "," + keyZ, tileNr)
            && !equalTile(mapTiles, (keyX - 1) + "," + (keyY + 1) + "," + keyZ, tileNr)) {
          tileName = lowerType + "IUR";
        } else if (equalTile(mapTiles, (keyX + 1) + "," + (keyY) + "," + keyZ, tileNr)
            && equalTile(mapTiles, (keyX) + "," + (keyY + 1) + "," + keyZ, tileNr)
            && !equalTile(mapTiles, (keyX + 1) + "," + (keyY + 1) + "," + keyZ, tileNr)) {
          tileName = lowerType + "IUL";
        } else if (equalTile(mapTiles, (keyX + 1) + "," + (keyY) + "," + keyZ, tileNr)
            && equalTile(mapTiles, (keyX) + "," + (keyY - 1) + "," + keyZ, tileNr)
            && !equalTile(mapTiles, (keyX + 1) + "," + (keyY - 1) + "," + keyZ, tileNr)) {
          tileName = lowerType + "IDL";
        } else if (equalTile(mapTiles, (keyX - 1) + "," + (keyY) + "," + keyZ, tileNr)
            && equalTile(mapTiles, (keyX) + "," + (keyY - 1) + "," + keyZ, tileNr)
            && !equalTile(mapTiles, (keyX - 1) + "," + (keyY - 1) + "," + keyZ, tileNr)) {
          tileName = lowerType + "IDR";
        } else if (equalTile(mapTiles, (keyX - 1) + "," + (keyY) + "," + keyZ, tileNr)
            && !equalTile(mapTiles, (keyX + 1) + "," + (keyY) + "," + keyZ, tileNr)) {
          tileName = lowerType + "R";
        } else if (equalTile(mapTiles, (keyX + 1) + "," + (keyY) + "," + keyZ, tileNr)
            && !equalTile(mapTiles, (keyX - 1) + "," + (keyY) + "," + keyZ, tileNr)) {
          tileName = lowerType + "L";
        } else if (equalTile(mapTiles, (keyX) + "," + (keyY - 1) + "," + keyZ, tileNr)
            && !equalTile(mapTiles, (keyX) + "," + (keyY + 1) + "," + keyZ, tileNr)) {
          tileName = lowerType + "D";
        } else if (equalTile(mapTiles, (keyX) + "," + (keyY + 1) + "," + keyZ, tileNr)
            && !equalTile(mapTiles, (keyX) + "," + (keyY - 1) + "," + keyZ, tileNr)) {
          tileName = lowerType + "U";
        } else {
          if (tileNr == 4) {
            // Random grass
            int randomNr = RandomUtils.getInt(1, 20);
            if (randomNr > 7) {
              randomNr = 1;
            }
            tileName = "" + randomNr;
          }
        }
      }

      Tile newTile = new Tile(worldMapX, worldMapY, worldMapZ);
      newTile.setType(tileType, tileName, passable);
      worldMapTiles.put(worldMapX + "," + worldMapY + "," + worldMapZ, newTile);

      if (saveEntrancePos && !placeEntrance) {
        placeEntrance = true;
        Door door = new Door(entranceX, entranceY, entranceZ, 5135, 10030, 0);
        newTile.setObjectId("special/portal");
        newTile.setGeneratedDoor(door);
        newTile.setPassable(true);
      }
    }

    it = mapTiles.entrySet().iterator();
    keyX = 0;
    keyY = 0;
    keyZ = 0;

    // Set area effect at entrance
    worldMapTiles.get(entranceX + "," + entranceY + "," + entranceZ).setAreaEffectId(91);

    while (it.hasNext()) {
      Entry<String, Integer> t = it.next();

      String tCoord[] = t.getKey().split(",");

      keyX = Integer.parseInt(tCoord[0]);
      keyY = Integer.parseInt(tCoord[1]);
      keyZ = Integer.parseInt(tCoord[2]);

      int tileNr = t.getValue();

      worldMapX = keyX + pirateIslandX;
      worldMapY = keyY + pirateIslandY;
      worldMapZ = keyZ + pirateIslandZ;

      if (tileNr == 6) {
        // Random grass
        int randomNr = RandomUtils.getInt(1, 20);
        if (randomNr > 7) {
          randomNr = 1;
        }

        worldMapTiles
            .get(worldMapX + "," + worldMapY + "," + worldMapZ)
            .setType("grass", "" + randomNr, 1);
      }
    }
  }

  public static void cleanUp() {
    Vector<int[]> nullPoints = new Vector<int[]>();

    Iterator<Entry<String, Integer>> it = mapTiles.entrySet().iterator();
    while (it.hasNext()) {
      Entry<String, Integer> t = it.next();

      String tCoord[] = t.getKey().split(",");

      int tileX = Integer.parseInt(tCoord[0]);
      int tileY = Integer.parseInt(tCoord[1]);
      int tileZ = Integer.parseInt(tCoord[2]);

      int tileType = t.getValue();

      new HashMap<Integer, Integer>();
      if (!checkTileOk(tileX, tileY, tileZ, tileType)) {
        tileType++;
        t.setValue(tileType);
      }
    }

    for (int[] point : nullPoints) {
      // Count non-null neighbours
      int nrNonNull = 0;
      for (int i = -1; i < 2; i++) {
        for (int j = -1; j < 2; j++) {
          if (i != 0 || j != 0) {
            if (mapTiles.get((point[0] + i) + "," + (point[1] + j) + "," + point[2]) != null) {
              nrNonNull++;
            }
          }
        }
      }
      if (nrNonNull >= 5) {
        mapTiles.put(point[0] + "," + point[1] + "," + point[2], 0);
      }
    }
  }

  /*
  public static int checkTileOk(int tileX, int tileY, int tileZ, int value){
  	int tileOk = -1;

  	// horizontal
  	if(mapTiles.get((tileX-1)+","+(tileY)+","+tileZ) != null && mapTiles.get((tileX-1)+","+(tileY)+","+tileZ) == value){
  		if(mapTiles.get((tileX+1)+","+(tileY)+","+tileZ) != null){
  			if(mapTiles.get((tileX+1)+","+(tileY)+","+tileZ) == value){
  				tileOk = -1;
  			}else{
  				tileOk = mapTiles.get((tileX+1)+","+(tileY)+","+tileZ);
  			}
  		}
  	}

  	// vertical
  	if(mapTiles.get((tileX)+","+(tileY-1)+","+tileZ) != null && mapTiles.get((tileX)+","+(tileY-1)+","+tileZ) == value){
  		if(mapTiles.get((tileX)+","+(tileY+1)+","+tileZ) != null){
  			if(mapTiles.get((tileX)+","+(tileY+1)+","+tileZ) == value){
  				tileOk = -1;
  			}else{
  				tileOk = mapTiles.get((tileX)+","+(tileY+1)+","+tileZ);
  			}
  		}
  	}

  	// ul
  	if(mapTiles.get((tileX)+","+(tileY-1)+","+tileZ) != null && mapTiles.get((tileX)+","+(tileY-1)+","+tileZ) == value){
  		if(mapTiles.get((tileX-1)+","+(tileY)+","+tileZ) != null){
  			if(mapTiles.get((tileX-1)+","+(tileY)+","+tileZ) == value){
  				tileOk = -1;
  			}else{
  				tileOk = mapTiles.get((tileX-1)+","+(tileY)+","+tileZ);
  			}
  		}
  	}

  	// ur
  	if(mapTiles.get((tileX)+","+(tileY-1)+","+tileZ) != null && mapTiles.get((tileX)+","+(tileY-1)+","+tileZ) == value){
  		if(mapTiles.get((tileX+1)+","+(tileY)+","+tileZ) != null){
  			if(mapTiles.get((tileX+1)+","+(tileY)+","+tileZ) == value){
  				tileOk = -1;
  			}else{
  				tileOk = mapTiles.get((tileX+1)+","+(tileY)+","+tileZ);
  			}
  		}
  	}

  	// dl
  	if(mapTiles.get((tileX)+","+(tileY+1)+","+tileZ) != null && mapTiles.get((tileX)+","+(tileY+1)+","+tileZ) == value){
  		if(mapTiles.get((tileX-1)+","+(tileY)+","+tileZ) != null){
  			if(mapTiles.get((tileX-1)+","+(tileY)+","+tileZ) == value){
  				tileOk = -1;
  			}else{
  				tileOk = mapTiles.get((tileX-1)+","+(tileY)+","+tileZ);
  			}
  		}
  	}

  	// dr
  	if(mapTiles.get((tileX)+","+(tileY+1)+","+tileZ) != null && mapTiles.get((tileX)+","+(tileY+1)+","+tileZ) == value){
  		if(mapTiles.get((tileX+1)+","+(tileY)+","+tileZ) != null){
  			if(mapTiles.get((tileX+1)+","+(tileY)+","+tileZ) == value){
  				tileOk = -1;
  			}else{
  				tileOk = mapTiles.get((tileX+1)+","+(tileY)+","+tileZ);
  			}
  		}
  	}

  	return tileOk;
  }
   */

  public static boolean checkTileOk(int tileX, int tileY, int tileZ, int value) {
    boolean tileOk = false;

    // horizontal
    if (mapTiles.get((tileX - 1) + "," + (tileY) + "," + tileZ) != null
        && mapTiles.get((tileX - 1) + "," + (tileY) + "," + tileZ) == value) {
      if (mapTiles.get((tileX + 1) + "," + (tileY) + "," + tileZ) != null) {
        if (mapTiles.get((tileX + 1) + "," + (tileY) + "," + tileZ) == value) {
          tileOk = true;
        }
      } else {
        int[] nullPoint = {tileX + 1, tileY, tileZ};
        nullPoints.add(nullPoint);
      }
    }

    // vertical
    if (mapTiles.get((tileX) + "," + (tileY - 1) + "," + tileZ) != null
        && mapTiles.get((tileX) + "," + (tileY - 1) + "," + tileZ) == value) {
      if (mapTiles.get((tileX) + "," + (tileY + 1) + "," + tileZ) != null) {
        if (mapTiles.get((tileX) + "," + (tileY + 1) + "," + tileZ) == value) {
          tileOk = true;
        }
      } else {
        int[] nullPoint = {tileX, tileY + 1, tileZ};
        nullPoints.add(nullPoint);
      }
    }

    // ul
    if (mapTiles.get((tileX) + "," + (tileY - 1) + "," + tileZ) != null
        && mapTiles.get((tileX) + "," + (tileY - 1) + "," + tileZ) == value) {
      if (mapTiles.get((tileX - 1) + "," + (tileY) + "," + tileZ) != null) {
        if (mapTiles.get((tileX - 1) + "," + (tileY) + "," + tileZ) == value) {
          tileOk = true;
        }
      } else {
        int[] nullPoint = {tileX - 1, tileY, tileZ};
        nullPoints.add(nullPoint);
      }
    }

    // ur
    if (mapTiles.get((tileX) + "," + (tileY - 1) + "," + tileZ) != null
        && mapTiles.get((tileX) + "," + (tileY - 1) + "," + tileZ) == value) {
      if (mapTiles.get((tileX + 1) + "," + (tileY) + "," + tileZ) != null) {
        if (mapTiles.get((tileX + 1) + "," + (tileY) + "," + tileZ) == value) {
          tileOk = true;
        }
      } else {
        int[] nullPoint = {tileX + 1, tileY, tileZ};
        nullPoints.add(nullPoint);
      }
    }

    // dl
    if (mapTiles.get((tileX) + "," + (tileY + 1) + "," + tileZ) != null
        && mapTiles.get((tileX) + "," + (tileY + 1) + "," + tileZ) == value) {
      if (mapTiles.get((tileX - 1) + "," + (tileY) + "," + tileZ) != null) {
        if (mapTiles.get((tileX - 1) + "," + (tileY) + "," + tileZ) == value) {
          tileOk = true;
        }
      } else {
        int[] nullPoint = {tileX - 1, tileY, tileZ};
        nullPoints.add(nullPoint);
      }
    }

    // dr
    if (mapTiles.get((tileX) + "," + (tileY + 1) + "," + tileZ) != null
        && mapTiles.get((tileX) + "," + (tileY + 1) + "," + tileZ) == value) {
      if (mapTiles.get((tileX + 1) + "," + (tileY) + "," + tileZ) != null) {
        if (mapTiles.get((tileX + 1) + "," + (tileY) + "," + tileZ) == value) {
          tileOk = true;
        }
      } else {
        int[] nullPoint = {tileX + 1, tileY, tileZ};
        nullPoints.add(nullPoint);
      }
    }

    return tileOk;
  }

  public static int placeCaveEntrances(HashMap<String, Tile> worldMapTiles, int nrCaves) {

    Vector<String> caveEntrances = new Vector<String>();

    for (Point m : ArchipelagoGenerator.mountains) {
      int mX = m.x;
      int mY = m.y;

      Spiral s = new Spiral(20, 20);
      List<Point> l = s.spiral();

      for (Point p : l) {
        if (p.getX() != 0 || p.getY() != 0) {
          int worldMapX = (int) (mX + p.getX()) + pirateIslandX;
          int worldMapY = (int) (mY + p.getY()) + pirateIslandY;
          int worldMapZ = pirateIslandZ;

          if (worldMapTiles.get(worldMapX + "," + worldMapY + "," + worldMapZ) != null) {
            if (worldMapTiles
                .get(worldMapX + "," + worldMapY + "," + worldMapZ)
                .getName()
                .equals("grassD")) {
              worldMapTiles
                  .get(worldMapX + "," + worldMapY + "," + worldMapZ)
                  .setType("grasscliff", "grassEntranced", 1);
              caveEntrances.add(worldMapX + "," + worldMapY + "," + worldMapZ);
              nrCaves--;
              break;
            }
          }
        }
      }

      if (nrCaves <= 0) {
        break;
      }
    }

    DungeonGenerator dg = new DungeonGenerator();

    int monsterGroup = 1;

    for (String caveCoord : caveEntrances) {
      String caveXYZ[] = caveCoord.split(",");

      int tileX = Integer.parseInt(caveXYZ[0]);
      int tileY = Integer.parseInt(caveXYZ[1]);
      int tileZ = Integer.parseInt(caveXYZ[2]);

      dg.pirateIslandGenerate(
          worldMapTiles, monsterGroup, tileX, tileY, tileZ, pirateIslandZ - (monsterGroup * 5));

      if (monsterGroup == 1) {
        monstersSpawned.add(15);
        monstersSpawned.add(9);
        monstersSpawned.add(10);
      } else if (monsterGroup == 2) {
        monstersSpawned.add(6);
        monstersSpawned.add(34);
        monstersSpawned.add(4);
      } else if (monsterGroup == 3) {
        monstersSpawned.add(28);
        monstersSpawned.add(29);
        monstersSpawned.add(8);
      } else if (monsterGroup == 4) {
        monstersSpawned.add(7);
        monstersSpawned.add(5);
        monstersSpawned.add(76);
      } else if (monsterGroup == 5) {
        monstersSpawned.add(43);
        monstersSpawned.add(41);
        monstersSpawned.add(42);
      } else if (monsterGroup == 6) {
        monstersSpawned.add(44);
        monstersSpawned.add(45);
      } else if (monsterGroup == 7) {
        monstersSpawned.add(21);
        monstersSpawned.add(22);
        monstersSpawned.add(23);
      }

      monsterGroup++;
    }

    return nrMonsters;
  }

  public static void placeHoles(HashMap<String, Tile> worldMapTiles, int nrHoles) {

    new Vector<String>();

    int distanceToCave = RandomUtils.getInt(400, 2000);

    Iterator<Entry<String, Integer>> it = mapTiles.entrySet().iterator();
    while (it.hasNext()) {
      Entry<String, Integer> t = it.next();

      String tCoord[] = t.getKey().split(",");

      int tileX = Integer.parseInt(tCoord[0]);
      int tileY = Integer.parseInt(tCoord[1]);
      int tileZ = Integer.parseInt(tCoord[2]);

      int tileType = t.getValue();

      distanceToCave--;

      if (tileType == 4 && distanceToCave <= 0) {
        distanceToCave = RandomUtils.getInt(400, 2000);
        int worldMapX = tileX + pirateIslandX;
        int worldMapY = tileY + pirateIslandY;
        int worldMapZ = tileZ + pirateIslandZ;

        nrHoles--;
        worldMapTiles
            .get(worldMapX + "," + worldMapY + "," + worldMapZ)
            .setType("grass", "hole", 1);
        System.out.println("Hole at: " + worldMapX + "," + worldMapY + "," + worldMapZ);
      }
      if (nrHoles <= 0) {
        break;
      }
    }
  }

  public static void placeMountains() {
    // Loop through mountain spots and place mountains
    int islandZ = ArchipelagoGenerator.islandZ;
    for (Point p : ArchipelagoGenerator.mountains) {
      int mX = p.x;
      int mY = p.y;

      int mountainSize = RandomUtils.getInt(500, 2000);

      while (mountainSize > 0) {
        Iterator<Entry<Point, Integer>> it = GeneratorBrush.getHeightBrush().entrySet().iterator();

        // Check if can place mountain
        boolean placeMountain = true;
        while (it.hasNext()) {
          Entry<Point, Integer> b = it.next();
          String coord = (mX + b.getKey().x) + "," + (mY + b.getKey().y) + "," + islandZ;
          if (mapTiles.get(coord) != null) {
            if (mapTiles.get(coord) < 4) {
              placeMountain = false;
              break;
            }
          } else {
            placeMountain = false;
            break;
          }
          mountainSize--;
        }

        // Place mountain
        if (placeMountain) {
          it = GeneratorBrush.getHeightBrush().entrySet().iterator();
          while (it.hasNext()) {
            Entry<Point, Integer> b = it.next();

            if (b.getValue() > 0) {
              String coord = (mX + b.getKey().x) + "," + (mY + b.getKey().y) + "," + islandZ;

              if (mapTiles.get(coord) != null) {
                if (mapTiles.get(coord) > 3 && mapTiles.get(coord) < b.getValue()) {
                  mapTiles.put(coord, b.getValue());
                }
              }
            }
          }
        }

        // Randomize direction of mountain
        // Randomize the direction of the cross path
        int direction = RandomUtils.getInt(1, 8);
        if (direction == 1) {
          mY--;
        } else if (direction == 2) {
          mY--;
          mX++;
        } else if (direction == 3) {
          mX++;
        } else if (direction == 4) {
          mY++;
          mX++;
        } else if (direction == 5) {
          mY++;
        } else if (direction == 6) {
          mY++;
          mX--;
        } else if (direction == 7) {
          mX--;
        } else if (direction == 8) {
          mY--;
          mX--;
        }
      }
    }
  }

  public static void placeShallowSpots() {

    int islandZ = ArchipelagoGenerator.islandZ;

    // Loop through shallow spots and place shallow waters
    for (Point p : ArchipelagoGenerator.shallow_spots) {
      int mX = p.x;
      int mY = p.y;

      int shallowSize = RandomUtils.getInt(100, 600);

      while (shallowSize > 0) {
        Iterator<Entry<Point, Integer>> it = GeneratorBrush.getShallowBrush().entrySet().iterator();

        // Check if can place shallow spots
        boolean placeShallowSpot = true;
        while (it.hasNext()) {
          Entry<Point, Integer> b = it.next();
          String coord = (mX + b.getKey().x) + "," + (mY + b.getKey().y) + "," + islandZ;
          if (mapTiles.get(coord) == null) {
            placeShallowSpot = false;
            break;
          }
        }

        if (placeShallowSpot) {
          it = GeneratorBrush.getShallowBrush().entrySet().iterator();
          // Place shallow spots
          while (it.hasNext()) {
            Entry<Point, Integer> b = it.next();

            if (b.getValue() > 0) {
              String coord = (mX + b.getKey().x) + "," + (mY + b.getKey().y) + "," + islandZ;

              if (mapTiles.get(coord) != null) {
                if (mapTiles.get(coord) == 1) {
                  mapTiles.put(coord, b.getValue());
                }
              }
              shallowSize--;
            }
          }
        }

        // Randomize direction of mountain
        // Randomize the direction of the cross path
        int direction = RandomUtils.getInt(1, 8);
        if (direction == 1) {
          mY--;
        } else if (direction == 2) {
          mY--;
          mX++;
        } else if (direction == 3) {
          mX++;
        } else if (direction == 4) {
          mY++;
          mX++;
        } else if (direction == 5) {
          mY++;
        } else if (direction == 6) {
          mY++;
          mX--;
        } else if (direction == 7) {
          mX--;
        } else if (direction == 8) {
          mY--;
          mX--;
        }
      }
    }
  }

  public static void draw(Graphics g) {
    Iterator<Entry<String, Integer>> it = mapTiles.entrySet().iterator();

    while (it.hasNext()) {
      Entry<String, Integer> t = it.next();

      String tCoord[] = t.getKey().split(",");

      int tileX = Integer.parseInt(tCoord[0]);
      int tileY = Integer.parseInt(tCoord[1]);
      Integer.parseInt(tCoord[2]);

      int tileType = t.getValue();

      if (tileType == 0) {
        // Debug
        g.setColor(new Color(255, 0, 0));
      } else if (tileType == 1) {
        // Deep water
        g.setColor(new Color(24, 100, 165));
      } else if (tileType == 2) {
        // Shallow water
        g.setColor(new Color(90, 161, 222));
      } else if (tileType == 3) {
        // Beach
        g.setColor(new Color(242, 255, 153));
      } else if (tileType == 4) {
        // Grass
        g.setColor(new Color(150, 220, 150));
      } else if (tileType == 5) {
        // Height 1
        g.setColor(new Color(180, 136, 100));
      } else if (tileType == 6) {
        // Height 2
        g.setColor(new Color(180, 250, 180));
      }

      /*
      else if(tileType == 7){
      	// Height 3
      	g.setColor(new Color(175,175,175));
      }else if(tileType == 8){
      	// Height 4
      	g.setColor(new Color(255,255,255));
      }
      */
      g.fillRect(tileX, tileY, 1, 1);
    }
    /*
    g.setColor(new Color(180,100,50));

    for(int[] point: nullPoints){
    	g.fillRect(point[0],point[1],1,1);
    }
    */
  }

  private static boolean equalTile(HashMap<String, Integer> map, String key, int checkValue) {
    if (map.containsKey(key)) {
      if (map.get(key) >= checkValue) {
        return true;
      }
    }
    return false;
  }
}
