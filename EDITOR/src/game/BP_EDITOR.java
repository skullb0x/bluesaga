package game;

import gui.Font;
import gui.Gui;
import gui.MouseCursor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import map.Tile;
import map.TileObject;
import map.WorldMap;
import menus.AreaEffectMenu;
import menus.DoorMenu;
import menus.MapMenu;
import menus.MonsterMenu;
import menus.NewMenu;
import menus.ObjectMenu;
import menus.TextureMenu;
import menus.TriggerMenu;

import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.AppGameContainer;
import components.Creature;
import components.Monster;
import graphics.ImageResource;

public class BP_EDITOR extends BasicGame {

  private static AppGameContainer app;

  // INIT AND RESOLUTION
  private static int SCREEN_WIDTH = 1024; // 1024
  private static int SCREEN_HEIGHT = 640; // 640

  private static boolean FULL_SCREEN = false;
  private static final int FRAME_RATE = 60;
  public static int TILE_SIZE = 50;

  public static boolean SHOW_PASSABLE = true;

  public static Font FONTS;

  public static Database mapDB;
  public static Database gameDB;

  private HashMap<String, Color> MiniMap = new HashMap<String, Color>();
  private boolean SHOW_MINI_MAP = false;

  public static ImageResource GFX;

  // CONTROL
  private boolean DELETE_MODE;

  private static Input INPUT;

  public static int PLAYER_X = 5000;
  public static int PLAYER_Y = 10000;
  public static int PLAYER_Z = 0;

  private static int MAX_Z = 1;

  private static int DAY_NIGHT_TIME = 0; // 0 = both, 1 = night, 2 = day

  private static int SlowInputItr = 0;
  private static Timer loadingTimer;

  private static Tile MouseTile;
  private static Monster MouseMonster;
  private static TileObject MouseObject;
  private static Vector<Tile> UNDO;
  private static boolean DeleteMonster = false;
  private static boolean DeleteObject = false;
  private static boolean DeleteTexture = false;

  public static boolean FixEdges = false;

  private static AreaEffectMenu AREA_EFFECT_MENU;
  private static TriggerMenu TRIGGER_MENU;
  public static int AREA_EFFECT_ID = -1;

  public static int TRIGGER_TRAP_ID = 0;
  public static int TRIGGER_DOOR_ID = 0;

  private static int TOGGLE_PASSABLE = 0;

  public static boolean PLACE_TRIGGER = false;

  private static int BrushSize = 1;

  // SCREEN DATA
  public static WorldMap WORLD_MAP;

  public static Tile SCREEN_TILES[][][];
  public static TileObject SCREEN_OBJECTS[][][];
  public static int TILE_HALF_W = 11;
  public static int TILE_HALF_H = 7;

  private static int ACTIVE_MAP = 1;

  // GUI
  private static Gui GUI;
  private static Image LoadingLogo;
  private static MouseCursor Mouse;

  private static TextureMenu TEXTURE_MENU;
  private static MonsterMenu MONSTER_MENU;
  private static MapMenu MAP_MENU;
  private static NewMenu NEW_MENU;
  private static ObjectMenu OBJECT_MENU;
  private static DoorMenu DOOR_MENU;

  public static boolean Loading = true;
  public static boolean PLACE_DOOR = false;
  public static Random randomGenerator = new Random();

  public BP_EDITOR() {
    super("Blue Saga Map Editor");
  }

  public void init(GameContainer container) throws SlickException {
    loadingTimer = new Timer();
    UNDO = new Vector<Tile>();
    DELETE_MODE = false;

    // CONNECT TO DB
    try {
      mapDB = new Database("../SERVER/mapDB");
      gameDB = new Database("../SERVER/gameDB");

       mapDB.askDB("create index if not exists pos on area_tile (x,y,z)");
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }

    try (ResultSet editorOptions = mapDB.askDB("select X,Y,Z from editor_option")) {
      if (editorOptions.next()) {
        PLAYER_X = editorOptions.getInt("X");
        PLAYER_Y = editorOptions.getInt("Y");
        PLAYER_Z = editorOptions.getInt("Z");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    GFX = new ImageResource();
    GFX.load();

    Mouse = new MouseCursor();

    TEXTURE_MENU = new TextureMenu(600, 40);
    TEXTURE_MENU.load();

    MAP_MENU = new MapMenu(380, 70);
    MAP_MENU.load();

    MONSTER_MENU = new MonsterMenu(500, 70);
    MONSTER_MENU.load();

    OBJECT_MENU = new ObjectMenu(680, 70);
    OBJECT_MENU.load();

    FONTS = new Font();

    DOOR_MENU = new DoorMenu(400, 240, container);
    NEW_MENU = new NewMenu(400, 240, container);

    AREA_EFFECT_MENU = new AreaEffectMenu(container);

    TRIGGER_MENU = new TriggerMenu(container);

    LoadingLogo = GFX.getSprite("gamelogo").getImage();

    // GUI INIT
    GUI = new Gui();
    GUI.init();

    container.setMouseCursor("../CLIENT/src/images/gui/cursors/cursor_hidden.png", 5, 5);

    MouseTile = null;

    SCREEN_TILES = new Tile[TILE_HALF_W * 2][TILE_HALF_H * 2][MAX_Z];
    SCREEN_OBJECTS = new TileObject[TILE_HALF_W * 2][TILE_HALF_H * 2][MAX_Z];

    WORLD_MAP = new WorldMap();
    WORLD_MAP.loadMap(ACTIVE_MAP);

    for (int i = 0; i < 22; i++) {
      for (int j = 0; j < 14; j++) {
        for (int k = 0; k < MAX_Z; k++) {
          SCREEN_TILES[i][j][k] = new Tile(i, j, k);
          SCREEN_OBJECTS[i][j][k] = null;
        }
      }
    }

    loadScreen();

    loading();
  }

  public void loadMiniMap() {
    MiniMap.clear();
    try (ResultSet mapInfo =
        mapDB.askDB(
            "select X, Y, Z, Type from area_tile where Z == "
                + PLAYER_Z
                + " AND X % 2 = 0 AND Y % 2 = 0")
    ) {
      while (mapInfo.next()) {
        Color mapColor;
        if (mapInfo.getString("Type").equals("water")) {
          mapColor = new Color(65, 114, 187);
        } else if (mapInfo.getString("Type").equals("shallow")) {
          mapColor = new Color(144, 188, 255);
        } else if (mapInfo.getString("Type").equals("beach")) {
          mapColor = new Color(255, 250, 94);
        } else if (mapInfo.getString("Type").equals("cliff")) {
          mapColor = new Color(150, 108, 58);
        } else {
          mapColor = new Color(166, 217, 124);
        }

        //int mapX = (int) Math.floor((float) mapInfo.getInt("X") / 2.0f);
        //int mapY = (int) Math.floor((float) mapInfo.getInt("Y") / 2.0f);

        int mapX = mapInfo.getInt("X");
        int mapY = mapInfo.getInt("Y");

        MiniMap.put(mapX + "," + mapY + "," + mapInfo.getInt("Z"), mapColor);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public static void loadScreen() {
    /*
    for(int i = 0; i < 22; i++){
      for(int j = 0; j < 14; j++){

        ResultSet tileInfo = mapDB.askDB("select X, Y, Z, Type, Number from area_tile where X = "+(PLAYER_X-TILE_HALF_W+i)+" and Y = "+(PLAYER_Y-TILE_HALF_H+j)+" and Z = "+PLAYER_Z);
        try {
          if(tileInfo.next()){
            SCREEN_TILES[i][j].setType(tileInfo.getString("Type"), tileInfo.getInt("Number"));
          }else{
            SCREEN_TILES[i][j].setType("None", 0);
          }
          tileInfo.close();
        } catch (SQLException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }

      }
    }
    */

    for (int i = 0; i < 22; i++) {
      for (int j = 0; j < 14; j++) {
        for (int k = 0; k < MAX_Z; k++) {
          SCREEN_TILES[i][j][k].setType("none", "none", false);
          SCREEN_TILES[i][j][k].setOccupant(null);
          SCREEN_OBJECTS[i][j][k] = null;
        }
      }
    }

    try (ResultSet mapInfo =
        mapDB.askDB(
            "select X, Y, Z, Type, Name, AreaEffectId, Passable, ObjectId, DoorId from area_tile where X >= "
                + (PLAYER_X - TILE_HALF_W)
                + " and X < "
                + (PLAYER_X + TILE_HALF_W)
                + " and Y >= "
                + (PLAYER_Y - TILE_HALF_H)
                + " and Y < "
                + (PLAYER_Y + TILE_HALF_H)
                + " and Z = "
                + PLAYER_Z
                + " order by Y asc, X asc, Z asc")

    ) {
      while (mapInfo.next()) {
        int tileX = mapInfo.getInt("X") - (PLAYER_X - TILE_HALF_W);
        int tileY = mapInfo.getInt("Y") - (PLAYER_Y - TILE_HALF_H);
        int tileZ = mapInfo.getInt("Z") - PLAYER_Z;

        boolean passable = false;
        if (mapInfo.getInt("Passable") == 1) {
          passable = true;
        }
        SCREEN_TILES[tileX][tileY][tileZ]
            .setType(mapInfo.getString("Type"), mapInfo.getString("Name"), passable);
        SCREEN_TILES[tileX][tileY][tileZ].setZ(mapInfo.getInt("Z"));
        SCREEN_TILES[tileX][tileY][tileZ].setAreaEffectId(mapInfo.getInt("AreaEffectId"));

        if (!mapInfo.getString("ObjectId").equals("None")) {
          TileObject newObject = new TileObject(mapInfo.getString("ObjectId"));
          newObject.setZ(mapInfo.getInt("Z"));
          SCREEN_OBJECTS[tileX][tileY][tileZ] = newObject;
        }

        SCREEN_TILES[tileX][tileY][tileZ].setDoorId(mapInfo.getInt("DoorId"));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    try (ResultSet containerInfo =
        mapDB.askDB(
            "select X,Y,Z,Type from area_container where X >= "
                + (PLAYER_X - TILE_HALF_W)
                + " and X < "
                + (PLAYER_X + TILE_HALF_W)
                + " and Y >= "
                + (PLAYER_Y - TILE_HALF_H)
                + " and Y < "
                + (PLAYER_Y + TILE_HALF_H)
                + " and Z = "
                + PLAYER_Z
                + " order by Y asc, X asc, Z asc")
    ) {
      while (containerInfo.next()) {
        int tileX = containerInfo.getInt("X") - (PLAYER_X - TILE_HALF_W);
        int tileY = containerInfo.getInt("Y") - (PLAYER_Y - TILE_HALF_H);
        int tileZ = containerInfo.getInt("Z") - (PLAYER_Z);
        String containerType = containerInfo.getString("Type");

        TileObject newObject = new TileObject(containerType);
        newObject.setZ(containerInfo.getInt("Z"));
        SCREEN_OBJECTS[tileX][tileY][tileZ] = newObject;
      }
      containerInfo.close();
    } catch (SQLException e1) {
      e1.printStackTrace();
    }

    try (ResultSet monsterInfo =
        mapDB.askDB(
            "select SpawnX, SpawnY, SpawnZ, CreatureId from area_creature where SpawnX >= "
                + (PLAYER_X - TILE_HALF_W)
                + " and SpawnX < "
                + (PLAYER_X + TILE_HALF_W)
                + " and SpawnY >= "
                + (PLAYER_Y - TILE_HALF_H)
                + " and SpawnY < "
                + (PLAYER_Y + TILE_HALF_H)
                + " and SpawnZ = "
                + PLAYER_Z
                + " and SpawnCriteria = "
                + DAY_NIGHT_TIME)
    ) {
      while (monsterInfo.next()) {
        int tileX = monsterInfo.getInt("SpawnX") - (PLAYER_X - TILE_HALF_W);
        int tileY = monsterInfo.getInt("SpawnY") - (PLAYER_Y - TILE_HALF_H);
        int tileZ = 0;
        int creatureId = monsterInfo.getInt("CreatureId");

        SCREEN_TILES[tileX][tileY][tileZ].setOccupant(new Creature(creatureId, 0, 0));
      }
      monsterInfo.close();
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    try (ResultSet trapInfo =
        mapDB.askDB(
            "select Id,TrapId,X,Y,Z from area_trap where X >= "
                + (PLAYER_X - TILE_HALF_W)
                + " and X < "
                + (PLAYER_X + TILE_HALF_W)
                + " and Y >= "
                + (PLAYER_Y - TILE_HALF_H)
                + " and Y < "
                + (PLAYER_Y + TILE_HALF_H)
                + " and Z = "
                + PLAYER_Z
                + " order by Y asc, X asc, Z asc")
    ) {
      while (trapInfo.next()) {
        int tileX = trapInfo.getInt("X") - (PLAYER_X - TILE_HALF_W);
        int tileY = trapInfo.getInt("Y") - (PLAYER_Y - TILE_HALF_H);
        int tileZ = 0;

        SCREEN_OBJECTS[tileX][tileY][tileZ].setTrapId(trapInfo.getInt("Id"));
      }
      trapInfo.close();
    } catch (SQLException e1) {
      e1.printStackTrace();
    }

    try (ResultSet triggerInfo =
        mapDB.askDB(
            "select Id,X,Y,Z from trigger where X >= "
                + (PLAYER_X - TILE_HALF_W)
                + " and X < "
                + (PLAYER_X + TILE_HALF_W)
                + " and Y >= "
                + (PLAYER_Y - TILE_HALF_H)
                + " and Y < "
                + (PLAYER_Y + TILE_HALF_H)
                + " and Z = "
                + PLAYER_Z
                + " order by Y asc, X asc, Z asc")
    ) {
      while (triggerInfo.next()) {
        int tileX = triggerInfo.getInt("X") - (PLAYER_X - TILE_HALF_W);
        int tileY = triggerInfo.getInt("Y") - (PLAYER_Y - TILE_HALF_H);
        int tileZ = 0;

        SCREEN_TILES[tileX][tileY][tileZ].setTriggerId(triggerInfo.getInt("Id"));
      }
      triggerInfo.close();
    } catch (SQLException e1) {
      e1.printStackTrace();
    }
  }

  public static void loading() {
    Loading = true;
    loadingTimer.schedule(
        new TimerTask() {
          public void run() {
            Loading = false;
          }
        },
        50);
  }

  public void update(GameContainer container, int delta) throws SlickException {
    // CHECK IF NEW INFO FROM SERVER HAS COME

    keyLogic(container);
  }

  public void render(GameContainer container, Graphics g) throws SlickException {

    FONTS.loadGlyphs();

    if (PLAYER_Z >= 10) {
      GFX.getSprite("effects/clouds").draw(0, 0);
    } else if (PLAYER_Z == 0) {
      GFX.getSprite("effects/void").draw(0, 0);
    }

    if (!NEW_MENU.isGenerating()) {
      for (int i = 0; i < 22; i++) {
        for (int j = 0; j < 14; j++) {
          SCREEN_TILES[i][j][0].draw(g, i * TILE_SIZE, j * TILE_SIZE);
        }
      }

      for (int j = 0; j < 14; j++) {
        for (int i = 0; i < 22; i++) {
          if (SCREEN_OBJECTS[i][j][0] != null) {
            SCREEN_OBJECTS[i][j][0].draw(g, i * TILE_SIZE, j * TILE_SIZE);
          }
        }
      }
    }

    if (DAY_NIGHT_TIME == 1) {
      g.setColor(new Color(0, 0, 0, 150));
      g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
    } else if (DAY_NIGHT_TIME == 2) {
      g.setColor(new Color(255, 255, 255, 150));
      g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
    }

    if (SHOW_MINI_MAP) {
      Iterator it = MiniMap.entrySet().iterator();
      while (it.hasNext()) {
        Map.Entry pairs = (Map.Entry) it.next();
        String[] coord = pairs.getKey().toString().split(",");
        int x = (Integer.parseInt(coord[0]) - 5000) / 2 + 100;
        int y = (Integer.parseInt(coord[1]) - 9700) / 2 + 200;
        Integer.parseInt(coord[2]);

        g.setColor((Color) pairs.getValue());
        g.fillRect(x, y, 1, 1);
        //   ServerMessage.printMessage(pairs.getKey() + " = " + pairs.getValue());
      }

      int playerx = (PLAYER_X - 5000) / 2 + 100 - 6;
      int playery = (PLAYER_Y - 9700) / 2 + 200 - 6;

      g.setColor(new Color(255, 0, 0));
      g.drawRect(playerx, playery, 10, 6);
    }

    if (!SHOW_MINI_MAP) {
      g.setFont(FONTS.size8);
      g.setColor(new Color(255, 255, 255));
      g.drawString("T: Textures menu", 30, 50);
      g.drawString("M: Monsters menu", 30, 70);
      g.drawString("O: Objects menu", 30, 90);
      g.drawString("P: Passable Tool", 30, 110);
      g.drawString("F1: Hide/View Passable", 30, 130);
      g.drawString("1/2: Brushsize -/+", 30, 150);
      g.drawString("0: Place Door", 30, 170);
      g.drawString("E: Place Area Effect", 30, 190);
      g.drawString("R: Place Trigger", 30, 210);
      g.drawString("F: Fix edges - " + FixEdges, 30, 230);
      g.drawString("N: Both/Day/Night spawns " + DAY_NIGHT_TIME, 30, 250);

      g.drawString("PgUp/PgDown: Z +/-", 30, 270);

      if (INPUT != null) {

        int screenX = (int) Math.floor(INPUT.getAbsoluteMouseX() / TILE_SIZE);
        int screenY = (int) Math.floor(INPUT.getAbsoluteMouseY() / TILE_SIZE);

        int tileX = screenX + PLAYER_X - TILE_HALF_W;
        int tileY = screenY + PLAYER_Y - TILE_HALF_H;

        g.setColor(new Color(255, 255, 255));
        g.drawString(tileX + "," + tileY + "," + PLAYER_Z, 540, 20);

        if (MouseTile != null) {
          MouseTile.draw(g, screenX * 50, screenY * 50);
        }
        if (MouseMonster != null) {
          MouseMonster.draw(g, screenX * 50, screenY * 50);
        }

        if (MouseObject != null) {
          MouseObject.draw(g, screenX * 50, screenY * 50);
        }

        if (TOGGLE_PASSABLE == 1) {
          g.setColor(new Color(255, 0, 0, 100));
          g.fillRect(screenX * 50, screenY * 50, 50 * BrushSize, 50 * BrushSize);
        } else if (TOGGLE_PASSABLE == 2) {
          g.setColor(new Color(255, 255, 255, 100));
          g.fillRect(screenX * 50, screenY * 50, 50 * BrushSize, 50 * BrushSize);
        }

        if (PLACE_DOOR) {
          g.setColor(new Color(0, 0, 255, 200));
          g.fillRect(screenX * 50, screenY * 50, 50, 50);
        }

        g.setColor(new Color(255, 255, 255, 255));
        g.drawRect(screenX * 50, screenY * 50, 50 * BrushSize, 50 * BrushSize);

        if (!Loading) {
          TEXTURE_MENU.draw(g, INPUT.getAbsoluteMouseX(), INPUT.getAbsoluteMouseY());
          MONSTER_MENU.draw(g, INPUT.getAbsoluteMouseX(), INPUT.getAbsoluteMouseY());
          OBJECT_MENU.draw(g, INPUT.getAbsoluteMouseX(), INPUT.getAbsoluteMouseY());
          DOOR_MENU.draw(g, container);
          AREA_EFFECT_MENU.draw(g, container);
          TRIGGER_MENU.draw(g, container);
        }

        if (AREA_EFFECT_ID > -1) {
          g.setColor(new Color(255, 104, 235));
          g.drawRect(screenX * 50, screenY * 50, 50, 50);
          g.setFont(FONTS.size12bold);
          g.drawString("" + AREA_EFFECT_ID, screenX * 50 + 25, screenY * 50 + 25);
        }

        if (PLACE_TRIGGER) {
          g.setColor(new Color(0, 255, 0));
          g.drawRect(screenX * 50, screenY * 50, 50, 50);
        }
      }
    }
    if (INPUT != null) {
      Mouse.draw(INPUT.getAbsoluteMouseX(), INPUT.getAbsoluteMouseY());
    }
  }

  public static void main(String[] args) throws Exception {
    app = new AppGameContainer(new BP_EDITOR());
    app.setDisplayMode(SCREEN_WIDTH, SCREEN_HEIGHT, FULL_SCREEN);
    app.setTargetFrameRate(FRAME_RATE);
    app.setShowFPS(true);
    app.setAlwaysRender(true);
    app.setVSync(true);

    app.start();
  }

  /*
   *
   * 	KEYBOARD & MOUSE
   *
   */

  private void keyLogic(GameContainer GC) {
    INPUT = GC.getInput();

    if (INPUT.isKeyPressed(Input.KEY_N)) {
      DAY_NIGHT_TIME++;
      if (DAY_NIGHT_TIME > 2) {
        DAY_NIGHT_TIME = 0;
      }
      loadScreen();
    }

    if (INPUT.isKeyPressed(Input.KEY_ESCAPE)) {
      if (closeMenus()) {
        mapDB.updateDB(
            "update editor_option set X = " + PLAYER_X + ", Y = " + PLAYER_Y + ", Z = " + PLAYER_Z);
        app.exit();
      }
    }

    if (!DOOR_MENU.isActive()) {
      if (SlowInputItr == 3) {

        SlowInputItr = 0;

        if (INPUT.isKeyDown(Input.KEY_A)) {
          PLAYER_X--;
          loadScreen();
        } else if (INPUT.isKeyDown(Input.KEY_D)) {
          PLAYER_X++;
          loadScreen();
        } else if (INPUT.isKeyDown(Input.KEY_W)) {
          PLAYER_Y--;
          loadScreen();
        } else if (INPUT.isKeyDown(Input.KEY_S)) {
          PLAYER_Y++;
          loadScreen();
        }
      }

      SlowInputItr++;

      if (INPUT.isKeyPressed(Input.KEY_DELETE)) {
        if (DELETE_MODE) {
          DELETE_MODE = false;
        } else {
          DELETE_MODE = true;
        }
      }

      if (INPUT.isKeyPressed(Input.KEY_UP)) {
        PLAYER_Z++;
        if (SHOW_MINI_MAP) {
          loadMiniMap();
        }
        loadScreen();
      } else if (INPUT.isKeyPressed(Input.KEY_DOWN)) {
        PLAYER_Z--;
        if (SHOW_MINI_MAP) {
          loadMiniMap();
        }
        loadScreen();
      }

      if (INPUT.isKeyPressed(Input.KEY_F1)) {
        if (SHOW_PASSABLE) {
          SHOW_PASSABLE = false;
        } else {
          SHOW_PASSABLE = true;
        }
      }

      if (INPUT.isKeyPressed(Input.KEY_F)) {
        if (FixEdges) {
          FixEdges = false;
        } else {
          FixEdges = true;
        }
      }

      if (INPUT.isKeyPressed(Input.KEY_1) && BrushSize>1) {
        BrushSize--;
      }
      if (INPUT.isKeyPressed(Input.KEY_2) && BrushSize<8) {
        BrushSize++;
      }

      if (INPUT.isKeyPressed(Input.KEY_M)) {
        if (!SHOW_MINI_MAP) {
          loadMiniMap();
          SHOW_MINI_MAP = true;
          closeMenus();
        } else {
          SHOW_MINI_MAP = false;
        }
      }

      if (INPUT.isKeyPressed(Input.KEY_O)) {
        closeMenus();
        OBJECT_MENU.toggle();
      }

      if (INPUT.isKeyPressed(Input.KEY_C)) {
        closeMenus();
        MONSTER_MENU.toggle();
      }

      if (INPUT.isKeyPressed(Input.KEY_T)) {
        closeMenus();
        TEXTURE_MENU.toggle();
      }

      if (INPUT.isKeyPressed(Input.KEY_E)) {
        clearModes();
        closeMenus();
        AREA_EFFECT_MENU.toggle();
      }

      if (INPUT.isKeyPressed(Input.KEY_P)) {
        TOGGLE_PASSABLE = (TOGGLE_PASSABLE + 1) % 3;
        SHOW_PASSABLE = true;
        MouseTile = null;
      }

      if (INPUT.isKeyPressed(Input.KEY_R)) {
        closeMenus();
        if (PLACE_TRIGGER) {
          PLACE_TRIGGER = false;
        } else {
          PLACE_TRIGGER = true;
          TRIGGER_MENU.toggle();
        }
      }

      if (INPUT.isKeyPressed(Input.KEY_0) && !AREA_EFFECT_MENU.isActive()) {
        closeMenus();
        if (PLACE_DOOR) {
          PLACE_DOOR = false;
        } else {
          PLACE_DOOR = true;
        }
      }

      if (INPUT.isMousePressed(0)) {

        int mouseX = INPUT.getAbsoluteMouseX();
        int mouseY = INPUT.getAbsoluteMouseY();

        int screenX = (int) Math.floor(mouseX / TILE_SIZE);
        int screenY = (int) Math.floor(mouseY / TILE_SIZE);

        int tileX = screenX + PLAYER_X - TILE_HALF_W;
        int tileY = screenY + PLAYER_Y - TILE_HALF_H;

        boolean selectTile = false;

        if (SHOW_MINI_MAP) {

          PLAYER_X = 5000 - 200 + mouseX * 2 - 10;
          PLAYER_Y = 9700 - 200 + mouseY * 2 - 6 - 200;
          loadScreen();

        } else if (TEXTURE_MENU.isActive()) {
          int clickButtonIndex = TEXTURE_MENU.click(mouseX, mouseY, mapDB);
          //ServerMessage.printMessage("clicked Index: "+clickButtonIndex);
          if (clickButtonIndex < 1000) {
            selectTile = true;
            if (clickButtonIndex < 998) {
              clearModes();
              TOGGLE_PASSABLE = 0;
              MouseTile = new Tile(0, 0, 0);
              MouseTile.setType(
                  TEXTURE_MENU.getTile(clickButtonIndex).getType(),
                  TEXTURE_MENU.getTile(clickButtonIndex).getName(),
                  MouseTile.isPassable());
            } else if (clickButtonIndex == 998) {
              clearModes();
              DeleteTexture = true;
            }
          }
        } else if (MONSTER_MENU.isActive()) {
          int clickButtonIndex = MONSTER_MENU.click(mouseX, mouseY);
          if (clickButtonIndex < 1000) {
            selectTile = true;
            if (clickButtonIndex < 999) {
              clearModes();
              MouseMonster =
                  new Monster(MONSTER_MENU.getMonster(clickButtonIndex).getId(), 0, 0, "no");
              //ServerMessage.printMessage("TILE INFO: "+MouseTile.getType()+""+MouseTile.getId());
            } else {
              clearModes();
              DeleteMonster = true;
            }
          }
        } else if (OBJECT_MENU.isActive()) {
          int clickButtonIndex = OBJECT_MENU.click(mouseX, mouseY, mapDB);
          //ServerMessage.printMessage("clicked Index: "+clickButtonIndex);
          if (clickButtonIndex < 1000) {
            selectTile = true;
            if (clickButtonIndex < 999) {
              clearModes();
              MouseObject = OBJECT_MENU.getClickedTileObject(mouseX, mouseY);
            } else if (clickButtonIndex == 999) {
              selectTile = true;
              clearModes();
              DeleteObject = true;
            }
            BrushSize = 1;
          }
        }

        // PLACE TILE, MONSTER OR CONTAINER
        if (!selectTile) {
          if (AREA_EFFECT_ID > -1) {
            mapDB.updateDB(
                "update area_tile set AreaEffectId = "
                    + AREA_EFFECT_ID
                    + " where X = "
                    + tileX
                    + " and Y = "
                    + tileY
                    + " and Z = "
                    + PLAYER_Z);

            SCREEN_TILES[screenX][screenY][0].setAreaEffectId(AREA_EFFECT_ID);
          } else if (PLACE_DOOR) {
            int doorId = DOOR_MENU.createDoor(tileX, tileY, PLAYER_Z);

            SCREEN_TILES[screenX][screenY][0].setDoorId(doorId);

            DOOR_MENU.open();
          } else {

            if (MouseTile != null) {
              addTiles(screenX, screenY);

              //fixEdges(MouseTile.getType().equals("grass"));

            } else if (MouseObject != null) {
              if (!SCREEN_TILES[screenX][screenY][0].getType().equals("None")) {

                // PLACE CONTAINER
                if (MouseObject.getName().contains("container")) {
                  mapDB.updateDB(
                      "insert into area_container (X,Y,Z,Type,Items,Fixed) values ("
                          + tileX
                          + ","
                          + tileY
                          + ","
                          + PLAYER_Z
                          + ",'"
                          + MouseObject.getName()
                          + "','',1)");
                  mapDB.updateDB(
                      "update area_tile set Passable = 0 where X = "
                          + tileX
                          + " and Y = "
                          + tileY
                          + " and Z = "
                          + PLAYER_Z);
                  TileObject newObject = new TileObject(MouseObject.getName());
                  newObject.setZ(PLAYER_Z);
                  SCREEN_OBJECTS[screenX][screenY][0] = newObject;
                  SCREEN_TILES[screenX][screenY][0].setPassable(false);
                } else if (MouseObject.getName().contains("moveable")) {
                  mapDB.updateDB(
                      "update area_tile set ObjectId = '"
                          + MouseObject.getName()
                          + "' where X = "
                          + tileX
                          + " and Y = "
                          + tileY
                          + " and Z = "
                          + PLAYER_Z);
                  TileObject newObject = new TileObject(MouseObject.getName());
                  newObject.setZ(PLAYER_Z);
                  SCREEN_OBJECTS[screenX][screenY][0] = newObject;

                } else {
                  // PLACE OBJECT OVER AREA
                  mapDB.updateDB("BEGIN TRANSACTION");
                  try {
                    for (int i = screenX; i < screenX + BrushSize; i += 2) {
                      for (int j = screenY; j < screenY + BrushSize; j += 2) {
                        tileX = i + PLAYER_X - TILE_HALF_W;
                        tileY = j + PLAYER_Y - TILE_HALF_H;

                        mapDB.updateDB(
                            "update area_tile set ObjectId = '"
                                + MouseObject.getName()
                                + "', Passable = 0 where X = "
                                + tileX
                                + " and Y = "
                                + tileY
                                + " and Z = "
                                + PLAYER_Z);

                        if (BrushSize > 1) {
                          mapDB.updateDB(
                              "update area_tile set ObjectId = '"
                                  + MouseObject.getName()
                                  + "', Passable = 0 where X = "
                                  + (tileX + 1)
                                  + " and Y = "
                                  + (tileY + 1)
                                  + " and Z = "
                                  + PLAYER_Z);
                        }
                        //TileObject newObject = new TileObject(MouseObject.getName());
                        //newObject.setZ(PLAYER_Z);
                        //SCREEN_OBJECTS[screenX][screenY][1] = newObject;
                        //SCREEN_TILES[screenX][screenY][1].setPassable(false);

                        if (MouseObject.getName().contains("trap")) {
                          MouseObject.getName().split("_");
                          mapDB.updateDB(
                              "update area_tile set Passable = 1 where X = "
                                  + tileX
                                  + " and Y = "
                                  + tileY
                                  + " and Z = "
                                  + PLAYER_Z);
                          mapDB.updateDB(
                              "insert into area_trap (TrapId,X,Y,Z) values (1,"
                                  + tileX
                                  + ","
                                  + tileY
                                  + ","
                                  + PLAYER_Z
                                  + ")");
                        }
                      }
                    }
                    mapDB.updateDB("END TRANSACTION");
                  } catch (Exception ex) {
                    mapDB.updateDB("ROLLBACK TRANSACTION");
                  }

                  if (BrushSize > 2) {
                    // MAKE ALL TILES IN BETWEEN OBJECTS NOT PASSABLE
                    tileX = screenX + PLAYER_X - TILE_HALF_W;
                    tileY = screenY + PLAYER_Y - TILE_HALF_H;
                    mapDB.updateDB(
                        "update area_tile set Passable = 0 where X > "
                            + tileX
                            + " and X < "
                            + (tileX + BrushSize)
                            + " and Y > "
                            + (tileY)
                            + " and Y < "
                            + (tileY + BrushSize)
                            + " and Z = "
                            + PLAYER_Z);
                  }
                  loadScreen();
                }
              }

            } else if (MouseMonster != null) {

              mapDB.updateDB(
                  "delete from area_creature where SpawnX = "
                      + tileX
                      + " and SpawnY = "
                      + tileY
                      + " and SpawnZ = "
                      + PLAYER_Z);
              mapDB.updateDB(
                  "insert into area_creature (AreaId, CreatureId, MobLevel, SpawnX, SpawnY, SpawnZ, AggroType, Name, SpawnCriteria) values (0,"
                      + MouseMonster.getId()
                      + ","
                      + MouseMonster.getMobLevel()
                      + ","
                      + tileX
                      + ","
                      + tileY
                      + ","
                      + PLAYER_Z
                      + ",2,'',"
                      + DAY_NIGHT_TIME
                      + ")");

              /*
              TileObject newObject = new TileObject(MouseObject.getName());
              newObject.setZ(PLAYER_Z);
              SCREEN_OBJECTS[screenX][screenY][1] = newObject;
              SCREEN_TILES[screenX][screenY][1].setPassable(false);
              */
              loadScreen();
            } else if (DeleteMonster) {
              mapDB.updateDB(
                  "delete from area_creature where SpawnX = "
                      + tileX
                      + " and SpawnY = "
                      + tileY
                      + " and SpawnZ = "
                      + PLAYER_Z);
              loadScreen();
            } else if (DeleteObject) {
              mapDB.updateDB("BEGIN TRANSACTION");
              try {
                for (int i = screenX; i < screenX + BrushSize; i++) {
                  for (int j = screenY; j < screenY + BrushSize; j++) {
                    tileX = i + PLAYER_X - TILE_HALF_W;
                    tileY = j + PLAYER_Y - TILE_HALF_H;

                    mapDB.updateDB(
                        "update area_tile set ObjectId = 'None', Passable = 1 where X = "
                            + tileX
                            + " and Y = "
                            + tileY
                            + " and Z = "
                            + PLAYER_Z);
                    mapDB.updateDB(
                        "delete from area_container where X = "
                            + tileX
                            + " and Y = "
                            + tileY
                            + " and Z = "
                            + PLAYER_Z);
                    mapDB.updateDB(
                        "delete from area_trap where X = "
                            + tileX
                            + " and Y = "
                            + tileY
                            + " and Z = "
                            + PLAYER_Z);
                  }
                }
                mapDB.updateDB("END TRANSACTION");
              } catch (Exception ex) {
                mapDB.updateDB("ROLLBACK TRANSACTION");
              }

              loadScreen();
            } else if (DeleteTexture) {
              mapDB.updateDB("BEGIN TRANSACTION");
              try {
                for (int i = screenX; i < screenX + BrushSize; i++) {
                  for (int j = screenY; j < screenY + BrushSize; j++) {
                    tileX = i + PLAYER_X - TILE_HALF_W;
                    tileY = j + PLAYER_Y - TILE_HALF_H;

                    if (SCREEN_TILES[i][j][0].getDoorId() > 0) {
                      mapDB.updateDB(
                          "delete from door where Id = " + SCREEN_TILES[i][j][0].getDoorId());
                    }

                    mapDB.updateDB(
                        "delete from area_tile where X = "
                            + tileX
                            + " and Y = "
                            + tileY
                            + " and Z = "
                            + PLAYER_Z);
                  }
                }
                mapDB.updateDB("END TRANSACTION");
              } catch (Exception ex) {
                mapDB.updateDB("ROLLBACK TRANSACTION");
              }
              loadScreen();
            } else if (TOGGLE_PASSABLE > 0) {
              mapDB.updateDB("BEGIN TRANSACTION");
              try {
                for (int i = screenX; i < screenX + BrushSize; i++) {
                  for (int j = screenY; j < screenY + BrushSize; j++) {

                    tileX = i + PLAYER_X - TILE_HALF_W;
                    tileY = j + PLAYER_Y - TILE_HALF_H;

                    mapDB.updateDB(
                        "update area_tile set Passable = "
                            + (TOGGLE_PASSABLE - 1)
                            + " where X = "
                            + tileX
                            + " and Y = "
                            + tileY
                            + " and Z = "
                            + PLAYER_Z);
                  }
                }
                mapDB.updateDB("END TRANSACTION");
              } catch (Exception ex) {
                mapDB.updateDB("ROLLBACK TRANSACTION");
              }
              loadScreen();
            }

            if (PLACE_TRIGGER) {
              mapDB.updateDB(
                  "insert into trigger (X,Y,Z,TrapId,DoorId) values ("
                      + tileX
                      + ","
                      + tileY
                      + ","
                      + PLAYER_Z
                      + ","
                      + BP_EDITOR.TRIGGER_TRAP_ID
                      + ","
                      + BP_EDITOR.TRIGGER_DOOR_ID
                      + ")");
            }
          }
        }
      }

      if (INPUT.isMousePressed(1)) {
        clearModes();
      }

    } else if (DOOR_MENU.isActive()) {
      DOOR_MENU.keyLogic(INPUT);
    }

    if (AREA_EFFECT_MENU.isActive()) {
      AREA_EFFECT_MENU.keyLogic(INPUT);
    }

    if (TRIGGER_MENU.isActive()) {
      TRIGGER_MENU.keyLogic(INPUT);
    }

    INPUT.clearKeyPressedRecord();
  }

  public static void clearModes() {
    DeleteTexture = false;
    DeleteMonster = false;
    DeleteObject = false;
    MouseMonster = null;
    MouseObject = null;
    MouseTile = null;
    MouseObject = null;
  }

  public static boolean isEdge(int screenX, int screenY, int screenZ) {
    if (SCREEN_TILES[screenX][screenY][0].getName().contains("D")
        || SCREEN_TILES[screenX][screenY][0].getName().contains("U")
        || SCREEN_TILES[screenX][screenY][0].getName().contains("L")
        || SCREEN_TILES[screenX][screenY][0].getName().contains("R")) {
      return true;
    }
    return false;
  }

  public static boolean canFixEdges(String tileName) {
    return (tileName.contains("D")
        && !tileName.contains("Stairs")
        && !tileName.contains("Entrance")
        && !tileName.contains("Exit")
        && !tileName.contains("wall"));
  }

  public static void addTiles(int screenX, int screenY) {

    String tileName = MouseTile.getName();
    if (FixEdges && BrushSize>1 && canFixEdges(tileName)) {

      String tileType = MouseTile.getType();
      int lastChar = MouseTile.getName().length() - 1;

      while (lastChar>0 &&  Character.isUpperCase(tileName.charAt(lastChar-1))) {
        -- lastChar;
      }
      String otherType = tileName.substring(0, lastChar);
      //String saveTileType = MouseTile.getType();

      for (int i = screenX; i < screenX + BrushSize; i++) {
        for (int j = screenY; j < screenY + BrushSize; j++) {
          SCREEN_TILES[i][j][0].setType(tileType, "1", true);
        }
      }

      boolean passable = false;

      mapDB.updateDB("BEGIN TRANSACTION");
      try {
        for (int i = screenX; i < screenX + BrushSize; i++) {
          for (int j = screenY; j < screenY + BrushSize; j++) {
            if (i > 0 && i < 21 && j > 0 && j < 13) {
              if (SCREEN_TILES[i - 1][j][0].getType().equals(tileType)
                  && SCREEN_TILES[i][j - 1][0].getType().equals(tileType)
                  && !SCREEN_TILES[i - 1][j - 1][0].getType().equals(tileType)) {
                // IUL
                tileName = otherType + "IDR";
              } else if (SCREEN_TILES[i + 1][j][0].getType().equals(tileType)
                  && SCREEN_TILES[i][j - 1][0].getType().equals(tileType)
                  && !SCREEN_TILES[i + 1][j - 1][0].getType().equals(tileType)) {
                // IUR
                tileName = otherType + "IDL";
              } else if (SCREEN_TILES[i - 1][j][0].getType().equals(tileType)
                  && SCREEN_TILES[i][j + 1][0].getType().equals(tileType)
                  && !SCREEN_TILES[i - 1][j + 1][0].getType().equals(tileType)) {
                // IDL
                tileName = otherType + "IUR";
              } else if (SCREEN_TILES[i + 1][j][0].getType().equals(tileType)
                  && SCREEN_TILES[i][j + 1][0].getType().equals(tileType)
                  && !SCREEN_TILES[i + 1][j + 1][0].getType().equals(tileType)) {
                // IDR
                tileName = otherType + "IUL";
              } else if (!SCREEN_TILES[i - 1][j][0].getType().equals(tileType)
                  && !SCREEN_TILES[i][j - 1][0].getType().equals(tileType)) {
                // UL
                tileName = otherType + "UL";
              } else if (!SCREEN_TILES[i + 1][j][0].getType().equals(tileType)
                  && !SCREEN_TILES[i][j - 1][0].getType().equals(tileType)) {
                // UR
                tileName = otherType + "UR";
              } else if (!SCREEN_TILES[i + 1][j][0].getType().equals(tileType)
                  && !SCREEN_TILES[i][j + 1][0].getType().equals(tileType)) {
                // DR
                tileName = otherType + "DR";
              } else if (!SCREEN_TILES[i - 1][j][0].getType().equals(tileType)
                  && !SCREEN_TILES[i][j + 1][0].getType().equals(tileType)) {
                // DR
                tileName = otherType + "DL";
              } else if (!SCREEN_TILES[i - 1][j][0].getType().equals(tileType)) {
                // L
                tileName = otherType + "L";
              } else if (!SCREEN_TILES[i + 1][j][0].getType().equals(tileType)) {
                // R
                tileName = otherType + "R";
              } else if (!SCREEN_TILES[i][j + 1][0].getType().equals(tileType)) {
                // D
                tileName = otherType + "D";
              } else if (!SCREEN_TILES[i][j - 1][0].getType().equals(tileType)) {
                // U
                tileName = otherType + "U";
              } else {
                //tileName = SCREEN_TILES[i][j][1].getName();
                //tileType = SCREEN_TILES[i][j][1].getType();

                int randomTile = randomGenerator.nextInt(100) + 1;
                if (BP_EDITOR.GFX.getSprite("textures/" + tileType + "/" + randomTile) == null) {
                  randomTile = 1;
                }
                tileName = Integer.toString(randomTile);
              }

              Tile checkTile = new Tile(0, 0, PLAYER_Z);
              if (checkTile.setType(tileType, tileName, false)) {
                passable = isTilePassable(checkTile);
                SCREEN_TILES[i][j][0].setType(tileType, tileName, passable);
                int passableInt = 0;
                if (passable) {
                  passableInt = 1;
                }

                int tileX = i + PLAYER_X - TILE_HALF_W;
                int tileY = j + PLAYER_Y - TILE_HALF_H;

                try {
                  mapDB.update(
                      "insert into area_tile (Type, Name, X, Y, Z, Passable) values ('"
                          + tileType
                          + "', '"
                          + tileName
                          + "',"
                          + tileX
                          + ","
                          + tileY
                          + ","
                          + PLAYER_Z
                          + ","
                          + passableInt
                          + ")");
                } catch (SQLException ex) {
                  mapDB.update(
                      "update area_tile set Type = '"
                          + tileType
                          + "', Name = '"
                          + tileName
                          + "', Passable = "
                          + passableInt
                          + " where X = "
                            + tileX
                            + " and Y = "
                            + tileY
                            + " and Z = "
                            + PLAYER_Z);
                }
              }
            }
          }
        }
        mapDB.updateDB("END TRANSACTION");
      } catch (Exception ex) {
        mapDB.updateDB("ROLLBACK TRANSACTION");
      }

    } else {
      String tileType = MouseTile.getType();
      String saveName = tileName;

      mapDB.updateDB("BEGIN TRANSACTION");
      try {
        for (int i = screenX; i < screenX + BrushSize; i++) {
          for (int j = screenY; j < screenY + BrushSize; j++) {
            Tile checkTile = new Tile(0, 0, PLAYER_Z);

            if (tileName.equals("1")) {
              int randomTile = randomGenerator.nextInt(100) + 1;
              if (BP_EDITOR.GFX.getSprite("textures/" + tileType + "/" + randomTile) == null) {
                randomTile = 1;
              }
              saveName = Integer.toString(randomTile);
            }

            if (checkTile.setType(tileType, saveName, false)) {
              boolean passable = false;

              passable = isTilePassable(checkTile);
              SCREEN_TILES[i][j][0].setType(tileType, saveName, passable);
              int passableInt = 0;
              if (passable) {
                passableInt = 1;
              }

              int tileX = i + PLAYER_X - TILE_HALF_W;
              int tileY = j + PLAYER_Y - TILE_HALF_H;

              try {
                mapDB.update(
                    "insert into area_tile (Type, Name, X, Y, Z, Passable) values ('"
                        + tileType
                        + "', '"
                        + tileName
                        + "',"
                        + tileX
                        + ","
                        + tileY
                        + ","
                        + PLAYER_Z
                        + ","
                        + passableInt
                        + ")");
              } catch (SQLException ex) {
                mapDB.update(
                    "update area_tile set Type = '"
                        + tileType
                        + "', Name = '"
                        + tileName
                        + "', Passable = "
                        + passableInt
                        + " where X = "
                          + tileX
                          + " and Y = "
                          + tileY
                          + " and Z = "
                          + PLAYER_Z);
              }
            }
          }
        }
        mapDB.updateDB("END TRANSACTION");
      } catch (Exception ex) {
        mapDB.updateDB("ROLLBACK TRANSACTION");
      }
    }
  }

  public static boolean isTilePassable(Tile checkTile) {
    boolean Passable = true;

    if (!isInteger(checkTile.getName())) {
      Passable = false;
    }

    if (checkTile.getType().equals("beach")) {
      Passable = true;
    }

    if (checkTile.getType().equals("none")) {
      Passable = false;
    }

    if (checkTile.getName().contains("Stairs")) {
      Passable = true;
    }

    if (checkTile.getType().contains("patch")) {
      Passable = true;
    }

    if (checkTile.getType().equals("shallow")) {
      Passable = true;
    }

    return Passable;
  }

  public static boolean isInteger(String s) {
    try {
      Integer.parseInt(s);
    } catch (NumberFormatException e) {
      return false;
    }
    // only got here if we didn't return false
    return true;
  }

  public static boolean closeMenus() {
    boolean allClosed = true;
    clearModes();

    TOGGLE_PASSABLE = 0;

    if (NEW_MENU.isActive()) {
      NEW_MENU.toggle();
      allClosed = false;
    }
    if (MAP_MENU.isActive()) {
      MAP_MENU.toggle();
      allClosed = false;
    }
    if (MONSTER_MENU.isActive()) {
      MONSTER_MENU.toggle();
      allClosed = false;
    }
    if (TEXTURE_MENU.isActive()) {
      TEXTURE_MENU.toggle();
      allClosed = false;
    }
    if (OBJECT_MENU.isActive()) {
      OBJECT_MENU.toggle();
      allClosed = false;
    }
    return allClosed;
  }
}
