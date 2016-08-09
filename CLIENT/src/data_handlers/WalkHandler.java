package data_handlers;

import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import map.ScreenObject;
import map.Tile;
import map.WorldMap;
import screens.ScreenHandler;

import org.newdawn.slick.Color;
import org.newdawn.slick.util.pathfinding.AStarPathFinder;
import org.newdawn.slick.util.pathfinding.Path;

import sound.Sfx;
import utils.LanguageUtils;
import creature.Creature;
import creature.PathMover;
import creature.PlayerCharacter;
import game.BlueSaga;
import game.ClientSettings;
import graphics.BlueSagaColors;
import gui.Gui;

public class WalkHandler extends Handler {

  private static Timer walkTimer = new Timer();

  // PATH FINDING
  private static AStarPathFinder pathfinder;
  private static Path lastPath;
  private static WorldMap PathMap;

  // CONTAINER INTERACTION
  public static boolean OPEN_CONTAINER = false;
  public static String OPEN_CONTAINER_ID = "None";

  // MOVEMENT
  public static Vector<String> walkPath = new Vector<String>();

  public WalkHandler() {
    super();
  }

  public static boolean canWalk(int gotoX, int gotoY) {
    boolean performWalk = true;

    float gotoRotation = BlueSaga.playerCharacter.getGotoRotation();

    if (BlueSaga.playerCharacter.getAnimations().size() > 0) {
      performWalk = false;
    }

    if (performWalk && BlueSaga.playerCharacter.getTotalStat("SPEED") > 0) {

      int dirX = gotoX - BlueSaga.playerCharacter.getX();
      int dirY = gotoY - BlueSaga.playerCharacter.getY();

      if (dirX < 0 && dirY < 0) {
        gotoRotation = 315;
      } else if (dirX > 0 && dirY < 0) {
        gotoRotation = 45;
      } else if (dirX < 0 && dirY > 0) {
        gotoRotation = 225;
      } else if (dirX > 0 && dirY > 0) {
        gotoRotation = 135;
      } else if (dirX < 0) {
        gotoRotation = 270;
      } else if (dirX > 0) {
        gotoRotation = 90;
      } else if (dirY > 0) {
        gotoRotation = 180;
      } else {
        gotoRotation = 0;
      }

      Tile GotoTile =
          ScreenHandler.SCREEN_TILES.get(
              gotoX + "," + gotoY + "," + BlueSaga.playerCharacter.getZ());
      if (GotoTile != null) {

        // DO NOT LET PLAYER GO OUT FROM INN BEFORE TALKING TO INN-KEEPER
        if (Gui.TutorialDialog.isVisible()
            && Gui.TutorialDialog.getStep() == 1
            && !GotoTile.getName().equals("1")
            && BlueSaga.playerCharacter.getZ() == 1) {
          performWalk = false;
        }

        // CHECK IF TILE HAS BLOCK STATUSEFFECT
        if (GotoTile.getStatusEffect(41) != null) {
          performWalk = false;
        }

        if (GotoTile.getType().contains("shallow")) {
          performWalk = false;
          if (BlueSaga.playerCharacter.getShip() != null) {
            if (BlueSaga.playerCharacter.getShip().getShipId() > 0) {
              performWalk = true;
            }
          }
          if (!performWalk
              && !Gui.hasMessage(LanguageUtils.getString("messages.walking.need_boat"))) {
            Gui.addMessage("#messages.walking.need_boat", BlueSagaColors.RED);
          }
        } else if (GotoTile.getType().contains("water")
            && !GotoTile.getType().contains("watercave")) {
          performWalk = false;
          if (BlueSaga.playerCharacter.getShip() != null) {
            if (BlueSaga.playerCharacter.getShip().getShipId() > 1) {
              performWalk = true;
            }
          }
          if (!performWalk
              && !Gui.hasMessage(LanguageUtils.getString("messages.walking.need_better_boat"))) {
            Gui.addMessage("#messages.walking.need_better_boat", BlueSagaColors.RED);
          }
        }

        if (!GotoTile.isPassable()) {
          performWalk = false;
        }

        // CHECK IF MOVEABLE
        ScreenObject GotoObject =
            ScreenHandler.SCREEN_OBJECTS_WITH_ID.get(
                gotoX + "," + gotoY + "," + BlueSaga.playerCharacter.getZ());
        if (GotoObject != null) {
          if (GotoObject.getType().equals("Object")) {
            if (GotoObject.getObject().getName().contains("moveable")) {
              // CHECK IF NEXT TILE IN LINE
              // WITH MOVEABLE IS PASSABLE
              int nextX = gotoX + dirX;
              int nextY = gotoY + dirY;
              Tile NextTile =
                  ScreenHandler.SCREEN_TILES.get(
                      nextX + "," + nextY + "," + BlueSaga.playerCharacter.getZ());
              if (NextTile != null) {
                if (!NextTile.isPassable()) {
                  performWalk = false;
                } else if (ScreenHandler.SCREEN_OBJECTS_WITH_ID.get(
                        nextX + "," + nextY + "," + BlueSaga.playerCharacter.getZ())
                    != null) {
                  performWalk = false;
                }
              }
            }
          }
        }
      } else {
        performWalk = false;
      }
    } else {
      performWalk = false;
    }

    if (performWalk) {
      Gui.closeWalkWindows();

      GatheringHandler.stopGathering();

      BlueSaga.playerCharacter.setGotoRotation(gotoRotation);

      if (BlueSaga.playerCharacter.isResting()) {
        BlueSaga.playerCharacter.setResting(false);
        BlueSaga.client.sendMessage("rest", "stop");
      }

      if (Gui.ShopWindow.isOpen()) {
        Gui.ShopWindow.close();
      }

      // Close walk tutorial if open
      if (Gui.TutorialDialog.getStep() == 0) {
        Gui.TutorialDialog.close();
      }

      // MOVE SCREEN
      BlueSaga.playerCharacter.MyWalkHandler.walkTo(
          gotoX,
          gotoY,
          BlueSaga.playerCharacter.getZ(),
          BlueSaga.playerCharacter.getTotalStat("SPEED"));

      MapHandler.updateScreenObjects();
    }
    return performWalk;
    //BlueSaga.waitWalk = false;

  }

  public static void handleData(String serverData) {

    if (serverData.startsWith("<nowalk>")) {
      String coordInfo[] = serverData.substring(8).split(",");
      int playerX = Integer.parseInt(coordInfo[0]);
      int playerY = Integer.parseInt(coordInfo[1]);
      int playerZ = Integer.parseInt(coordInfo[2]);

      //BP_CLIENT.playerCharacter.setWalking(true);

      BlueSaga.playerCharacter.MyWalkHandler.walkTo(playerX, playerY, playerZ, 200);

      walkTimer.schedule(
          new TimerTask() {
            @Override
            public void run() {
              BlueSaga.playerCharacter.MyWalkHandler.setWalking(false);
              walkPath.clear();
            }
          },
          100);
      BlueSaga.playerCharacter.setGoToTarget(false);
    } else if (serverData.startsWith("<canwalk>")) {
      Gui.closeWalkWindows();

      GatheringHandler.stopGathering();

      if (BlueSaga.playerCharacter.isResting()) {
        BlueSaga.playerCharacter.setResting(false);
        BlueSaga.client.sendMessage("rest", "stop");
      }

      if (Gui.ShopWindow.isOpen()) {
        Gui.ShopWindow.close();
      }

      String walkData = serverData.substring(9);

      String playerInfo[] = walkData.split(",");
      int playerX = Integer.parseInt(playerInfo[0]);
      int playerY = Integer.parseInt(playerInfo[1]);
      int playerZ = Integer.parseInt(playerInfo[2]);
      int playerSpeed = Integer.parseInt(playerInfo[3]);

      // MOVE SCREEN
      BlueSaga.playerCharacter.MyWalkHandler.walkTo(playerX, playerY, playerZ, playerSpeed);

      MapHandler.updateScreenObjects();

    } else if (serverData.startsWith("<tilerow>")) {
      String tilesInfo = serverData.substring(9);
      MapHandler.updateScreenTiles(tilesInfo);
    } else if (serverData.startsWith("<unlockdoor>")) {
      String keyName = serverData.substring(12);
      Sfx.play("notifications/unlock_door");
      Gui.addMessage("#messages.inventory.you_used# " + keyName, BlueSagaColors.RED);
    }

    // REMOVE SOUL
    if (serverData.startsWith("<remove_soul>")) {
      String soulInfo[] = serverData.substring(13).split(",");
      int soulX = Integer.parseInt(soulInfo[0]);
      int soulY = Integer.parseInt(soulInfo[1]);
      int soulZ = Integer.parseInt(soulInfo[2]);

      ScreenHandler.SCREEN_TILES.get(soulX + "," + soulY + "," + soulZ).setSoul(false);
      BlueSaga.playerCharacter.useAbilityAnimate(0, new Color(136, 255, 203));
    }
  }

  public static void updatePlayerWalk(PlayerCharacter playerCharacter) {
    if (!playerCharacter.MyWalkHandler.isWalking()) {

      if (playerCharacter.getGoToTarget()) {

        walkPath.clear();
        ScreenObject targetObject =
            ScreenHandler.SCREEN_OBJECTS_WITH_ID.get(
                playerCharacter.getAttackTargetType().toString()
                    + playerCharacter.getAttackTargetId());

        if (targetObject != null) {
          Creature target = targetObject.getCreature();

          int tileX = target.getX();
          int tileY = target.getY();
          int tileZ = playerCharacter.getZ();

          if (Math.round(
                  Math.sqrt(
                      Math.pow(tileX - playerCharacter.getX(), 2)
                          + Math.pow(tileY - playerCharacter.getY(), 2)))
              > playerCharacter.getAttackRange()) {
            WalkHandler.findPath(tileX, tileY, tileZ, "PlayerAttack");
          } else {
            playerCharacter.setGoToTarget(false);
          }
        } else {
          playerCharacter.setGoToTarget(false);
        }
      }

      if (!walkPath.isEmpty()) {
        if (playerCharacter.isDead()) {
          walkPath.clear();
        } else {

          boolean continuePath = true;

          /*
          if(playerCharacter.getGoToTarget()){

          	ScreenObject targetObject = SCREEN_OBJECTS_WITH_ID.get(playerCharacter.getAttackTargetType()+playerCharacter.getAttackTargetId());

          	if(targetObject != null){
          		Creature target = targetObject.getCreature();

          		int tileX = target.getX();
          			int tileY = target.getY();
          			int tileZ = playerCharacter.getZ();

          			if(Math.sqrt(Math.pow(tileX - playerCharacter.getX(),2) + Math.pow(tileY - playerCharacter.getY(),2)) <= playerCharacter.getAttackRange()){
          			continuePath = false;
          			playerCharacter.setGoToTarget(false);
          		}
          	}
          }

           */

          if (continuePath) {
            String walkInfo[] = walkPath.get(0).split(":");
            int gotoX = playerCharacter.getX() + Integer.parseInt(walkInfo[0]);
            int gotoY = playerCharacter.getY() + Integer.parseInt(walkInfo[1]);

            if (playerCharacter.hasStatusEffect(27)) {
              gotoX = playerCharacter.getX() - Integer.parseInt(walkInfo[0]);
              gotoY = playerCharacter.getY() - Integer.parseInt(walkInfo[1]);
            }

            if (WalkHandler.canWalk(gotoX, gotoY)) {
              BlueSaga.client.sendMessage("canwalk", walkPath.get(0));
            }
            walkPath.remove(0);
          } else {
            walkPath.clear();
          }
        }
      } else {
        if (OPEN_CONTAINER) {
          OPEN_CONTAINER = false;
          if (OPEN_CONTAINER_ID.contains("container")) {
            BlueSaga.client.sendMessage("opencontainer", OPEN_CONTAINER_ID);
          } else if (OPEN_CONTAINER_ID.contains("gathering")) {
            if (OPEN_CONTAINER_ID.contains("_open")) {
              BlueSaga.client.sendMessage("opencontainer", OPEN_CONTAINER_ID);
            } else {
              String gatherInfo[] = OPEN_CONTAINER_ID.split(",");
              BlueSaga.client.sendMessage(
                  "gathering", gatherInfo[1] + "," + gatherInfo[2] + "," + gatherInfo[3]);
            }
          } else if (OPEN_CONTAINER_ID.contains("crafting")) {
            BlueSaga.client.sendMessage("getrecipe", OPEN_CONTAINER_ID);
          }
          OPEN_CONTAINER_ID = "None";
        }
      }
    }
  }

  public static void findPath(int tileX, int tileY, int tileZ, String walkType) {

    // MAKE A PATHFINDING MAP
    PathMap = new WorldMap();
    PathMap.createPathMap();

    pathfinder = new AStarPathFinder(PathMap, ClientSettings.TILE_HALF_W * 2, true);

    PathMover pathMover = new PathMover(walkType);
    pathMover.setTarget(tileX, tileY, tileZ);

    //playerCharacter.setGoToTarget(false);

    lastPath = new Path();
    lastPath =
        pathfinder.findPath(
            pathMover,
            BlueSaga.playerCharacter.getX() - PathMap.getPathMapStartX(),
            BlueSaga.playerCharacter.getY() - PathMap.getPathMapStartY(),
            tileX - PathMap.getPathMapStartX(),
            tileY - PathMap.getPathMapStartY());

    if (lastPath != null && lastPath.getLength() < 25) {

      walkPath.clear();

      int oldX = BlueSaga.playerCharacter.getX();
      int oldY = BlueSaga.playerCharacter.getY();

      int nextX = oldX;
      int nextY = oldY;

      for (int i = 1; i < lastPath.getLength(); i++) {
        nextX = lastPath.getStep(i).getX() + PathMap.getPathMapStartX();
        nextY = lastPath.getStep(i).getY() + PathMap.getPathMapStartY();

        String nextInfo = "";

        if (nextX < oldX) {
          nextInfo = "-1:";
        } else if (nextX > oldX) {
          nextInfo = "1:";
        } else {
          nextInfo = "0:";
        }

        if (nextY < oldY) {
          nextInfo += "-1";
        } else if (nextY > oldY) {
          nextInfo += "1";
        } else {
          nextInfo += "0";
        }

        oldX = nextX;
        oldY = nextY;

        walkPath.add(nextInfo);
      }

      /*
      if(walkPath.size() > 0 && !playerCharacter.MyWalkHandler.isWalking() && !MapHandler.FADED_SCREEN){
      	waitPath = true;
      	client.sendMessage("canwalk", walkPath.get(0));
      	walkPath.remove(0);
      }
       */
    }
  }
}
