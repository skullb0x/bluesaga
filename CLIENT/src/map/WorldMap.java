package map;

import game.BlueSaga;
import game.ClientSettings;
import screens.ScreenHandler;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Timer;
import java.util.Vector;

import org.newdawn.slick.util.pathfinding.Mover;
import org.newdawn.slick.util.pathfinding.PathFindingContext;
import org.newdawn.slick.util.pathfinding.TileBasedMap;

import sound.Sfx;
import creature.Creature;
import creature.Npc;
import creature.PathMover;
import data_handlers.MapHandler;

public class WorldMap implements TileBasedMap {
  private HashMap<String, Tile> MapTiles;

  private int MapSize;
  private int MapId;

  private int MapEffect;

  private String Name;

  private boolean[][] visited;

  private int pathMapStartX;
  private int pathMapStartY;

  private String MapType;

  HashMap<Integer, Npc> Npcs = new HashMap<Integer, Npc>();
  HashMap<Integer, Npc> OtherPlayers = new HashMap<Integer, Npc>();

  Vector<Creature> MapCreatures = new Vector<Creature>();

  static Timer removePlayerTimer = new Timer(); // checking if Players should be removed

  /****************************************
   *                                       *
   *           INIT/LOAD INFO		        *
   *                                       *
   *                                       *
   ****************************************/
  public class MonsterComparator implements Comparator<Creature> {
    @Override
    public int compare(Creature m1, Creature m2) {
      return m2.getY() - m1.getY();
    }
  }

  public WorldMap() {
    /*
    try {
    	TILE_MAP = new TiledMap("maps/forest.tmx");
    } catch (SlickException e) {
    	e.printStackTrace();
    }
    */

  }

  public void createPathMap() {

    visited = new boolean[ClientSettings.TILE_HALF_W * 2][ClientSettings.TILE_HALF_H * 2];

    MapTiles = new HashMap<String, Tile>();

    pathMapStartX = BlueSaga.playerCharacter.getX() - ClientSettings.TILE_HALF_W;
    pathMapStartY = BlueSaga.playerCharacter.getY() - ClientSettings.TILE_HALF_H;

    for (Tile t : ScreenHandler.SCREEN_TILES.values()) {
      int tileX = t.getX() - pathMapStartX;
      int tileY = t.getY() - pathMapStartY;

      MapTiles.put(tileX + "," + tileY, t);
    }
  }

  public int getPathMapStartX() {
    return pathMapStartX;
  }

  public int getPathMapStartY() {
    return pathMapStartY;
  }

  public void changeMonstersAggroState(String aggroData) {
    if (aggroData.length() > 0) {
      String aggro_data[] = aggroData.split(";");

      for (String mobAggroData : aggro_data) {
        // aggroStatus, dbId (, Health)

        //aggroStatus = aggro or not
        //aggroType = move passive, move aggro, npc

        String mobAggroInfo[] = mobAggroData.split("/");

        Npc mob = (Npc) MapHandler.addCreatureToScreen(mobAggroInfo[0]);

        String aggroInfo[] = mobAggroInfo[1].split(",");

        int aggroStatus = Integer.parseInt(aggroInfo[0]);
        int aggroType = Integer.parseInt(aggroInfo[1]);
        int healthStatus = Integer.parseInt(aggroInfo[2]);

        mob.setAggroType(aggroType);

        mob.setResting(false);

        if (aggroStatus == 1) {
          mob.setAggro(true);
          mob.setHealthStatus(healthStatus);
          Sfx.play("monsters/monster" + mob.getCreatureId(), 1.0f, 1.0f);
        } else {
          mob.setAggro(false);
        }
      }
    }
  }

  /****************************************
   *                                       *
   *           PROJECTILES			        *
   *                                       *
   *                                       *
   ****************************************/
  public void updateMapCreatures(boolean rebuild) {
    if (rebuild) {
      MapCreatures.clear();
      for (Creature c : Npcs.values()) {
        MapCreatures.add(c);
      }

      for (Creature c : OtherPlayers.values()) {
        MapCreatures.add(c);
      }

      MapCreatures.add(BlueSaga.playerCharacter);
    }

    Collections.sort(MapCreatures, new MonsterComparator());
  }

  /****************************************
   *                                       *
   *           GETTER/SETTER		        *
   *                                       *
   *                                       *
   ****************************************/
  public Npc getOtherPlayer(int playerId) {
    return OtherPlayers.get(playerId);
  }

  public HashMap<Integer, Npc> getOtherPlayers() {
    return OtherPlayers;
  }

  public Npc getMonster(int monsterId) {
    return Npcs.get(monsterId);
  }

  public String getClickedTile(int mouseX, int mouseY, int cameraX, int cameraY) {
    String mouseInfo = "0";

    int tileX = (int) Math.floor((-cameraX + mouseX + 25) / ClientSettings.TILE_SIZE);
    int tileY = (int) Math.floor((-cameraY + mouseY + 25) / ClientSettings.TILE_SIZE);

    mouseInfo = tileX + ";" + tileY;

    return mouseInfo;
  }

  public HashMap<String, Tile> getMapTiles() {
    return MapTiles;
  }

  public Vector<Creature> getMapCreatures() {
    return MapCreatures;
  }

  public HashMap<Integer, Npc> getMonsters() {
    return Npcs;
  }

  public Npc getNpc(int npcId) {
    return Npcs.get(npcId);
  }

  public String getName() {
    return Name;
  }

  public String getType() {
    return MapType;
  }

  public int getMapId() {
    return MapId;
  }

  public int getMapSize() {
    return MapSize;
  }

  public int getMapEffect() {
    return MapEffect;
  }

  /****************************************
   * * PATHFINDING * * *
   ****************************************/

  /**
   * Clear the array marking which tiles have been visted by the path finder.
   */
  public void clearVisited() {
    for (int x = 0; x < getWidthInTiles(); x++) {
      for (int y = 0; y < getHeightInTiles(); y++) {
        visited[x][y] = false;
      }
    }
  }

  /**
   * @see TileBasedMap#visited(int, int)
   */
  public boolean visited(int x, int y) {
    return visited[x][y];
  }

  /**
   * @see TileBasedMap#blocked(Mover, int, int)
   */
  @Override
  public boolean blocked(PathFindingContext pathMover, int x, int y) {

    PathMover mover = (PathMover) pathMover.getMover();

    if (mover.getType().equals("PlayerWalk")) {
      if (MapTiles.get(x + "," + y) != null) {
        if (MapTiles.get(x + "," + y).isPassable()) {
          return false;
        }
      } else {
        return false;
      }
    } else if (mover.getType().equals("PlayerAttack")) {
      if (MapTiles.get(x + "," + y) != null) {
        if (MapTiles.get(x + "," + y).isPassable()
            || (x == mover.getTargetX() - pathMapStartX
                && y == mover.getTargetY() - pathMapStartY)) {
          return false;
        }
      } else {
        return false;
      }
    }
    return true;
  }

  /**
   * @see TileBasedMap#getCost(Mover, int, int, int, int)
   */
  @Override
  public float getCost(PathFindingContext arg0, int arg1, int arg2) {
    return 1;
  }

  /**
   * @see TileBasedMap#getHeightInTiles()
   */
  @Override
  public int getHeightInTiles() {
    return ClientSettings.TILE_HALF_H * 2;
  }

  /**
   * @see TileBasedMap#getWidthInTiles()
   */
  @Override
  public int getWidthInTiles() {
    return ClientSettings.TILE_HALF_W * 2;
  }

  /**
   * @see TileBasedMap#pathFinderVisited(int, int)
   */
  @Override
  public void pathFinderVisited(int x, int y) {
    visited[x][y] = true;
  }
}
