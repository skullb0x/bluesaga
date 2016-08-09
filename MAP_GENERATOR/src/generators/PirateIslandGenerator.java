package generators;

import java.awt.Point;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Vector;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import brushes.GeneratorBrush;
import utils.RandomUtils;

public class PirateIslandGenerator {

  private static HashMap<String, Integer> mapTiles;

  private static Vector<int[]> nullPoints;

  public static void generate() {

    nullPoints = new Vector<int[]>();

    // Generate archipelago
    int nrIslands = 20; //RandomUtils.getInt(80,150);
    ArchipelagoGenerator.generate(nrIslands);
    mapTiles = ArchipelagoGenerator.getTiles();

    cleanUp();
    placeMountains();
    placeShallowSpots();

    placeCaveEntrances();
  }

  public static void cleanUp() {
    Vector<int[]> nullPoints = new Vector<int[]>();

    System.out.println("Cleaning up...");
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
        System.out.println("Remove " + tileType + ": " + tileX + "," + tileY + "," + tileZ);
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

    System.out.println("Cleaning up done!");
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

  public static void placeCaveEntrances() {
    System.out.println("Placing cave entrances...");
    Iterator<Entry<String, Integer>> it = mapTiles.entrySet().iterator();

    while (it.hasNext()) {
      Entry<String, Integer> t = it.next();

      int tileType = t.getValue();

      if (tileType == 5) {
        int caveChance = RandomUtils.getInt(0, 120);
        if (caveChance == 0) {
          t.setValue(0);
        }
      }
    }
    System.out.println("Placing cave done!");
  }

  public static void placeMountains() {
    // Loop through mountain spots and place mountains
    System.out.println("Placing mountains...");
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
                if (mapTiles.get(coord) == 4 || mapTiles.get(coord) == 5) {
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
      System.out.println("Placed mountain!");
    }
    System.out.println("Placing moutains done!");
  }

  public static void placeShallowSpots() {

    int islandZ = ArchipelagoGenerator.islandZ;

    // Loop through shallow spots and place shallow waters
    System.out.println("Placing shallow spots...");
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
    System.out.println("Placing shallow done!");
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
}
