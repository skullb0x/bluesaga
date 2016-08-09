package creature;

import game.ClientSettings;
import screens.ScreenHandler;

import java.util.Timer;
import java.util.TimerTask;

import creature.Creature.CreatureType;
import sound.Sfx;
import utils.RandomUtils;

public class CwalkHandler {

  protected int oldX;
  protected int oldY;

  private float moveX;
  private float moveY;
  private float walkItr;
  private boolean walkDiagonal;

  private boolean Walking = false;

  private int MOVE_MONSTER_DELAY = 12;

  static Timer timerMove = new Timer();

  private Creature MyCreature;

  public CwalkHandler(Creature aCreature) {
    MyCreature = aCreature;
    moveX = 0;
    moveY = 0;

    oldX = MyCreature.getX();
    oldY = MyCreature.getY();

    walkDiagonal = false;
  }

  public void move() {
    walkItr--;

    if (moveX > 0) {
      if (walkDiagonal) {
        moveX -= 1.42f;
      } else {
        moveX -= 2.0f;
      }
    } else if (moveX < 0) {
      if (walkDiagonal) {
        moveX += 1.42f;
      } else {
        moveX += 2.0f;
      }
    }
    if (moveY > 0) {
      if (walkDiagonal) {
        moveY -= 1.42f;
      } else {
        moveY -= 2.0f;
      }
    } else if (moveY < 0) {
      if (walkDiagonal) {
        moveY += 1.42f;
      } else {
        moveY += 2.0f;
      }
    }

    if ((Math.round(moveX) == 0 && Math.round(moveY) == 0) || walkItr < 0) {
      moveX = 0.0f;
      moveY = 0.0f;
      int posX = MyCreature.getPixelX() + (ClientSettings.TILE_SIZE / 2);
      int posY = -MyCreature.getPixelY() + (ClientSettings.TILE_SIZE / 2) + 5;

      if (ScreenHandler.SCREEN_TILES.get(
              MyCreature.getX() + "," + MyCreature.getY() + "," + MyCreature.getZ())
          != null) {
        ScreenHandler.SCREEN_TILES
            .get(MyCreature.getX() + "," + MyCreature.getY() + "," + MyCreature.getZ())
            .showDust(posX, posY);

        String tileType =
            ScreenHandler.SCREEN_TILES
                .get(MyCreature.getX() + "," + MyCreature.getY() + "," + MyCreature.getZ())
                .getType();

        if (tileType.contains("shallow") || tileType.contains("water")) {
          tileType = "sea";
          if (RandomUtils.getInt(0, 3) > 0) {
            tileType = "nosound";
          }
        }

        boolean showParticles = true;
        boolean bridge = false;
        boolean carpet = false;

        if (ScreenHandler.SCREEN_TILES.get(
                MyCreature.getX() + "," + MyCreature.getY() + "," + MyCreature.getZ())
            != null) {
          if (ScreenHandler.SCREEN_TILES
              .get(MyCreature.getX() + "," + MyCreature.getY() + "," + MyCreature.getZ())
              .getName()
              .contains("path")) {
            tileType = "path";
            showParticles = false;
          }

          if (ScreenHandler.SCREEN_TILES
              .get(MyCreature.getX() + "," + MyCreature.getY() + "," + MyCreature.getZ())
              .getName()
              .contains("carpet")) {
            carpet = true;
          }

          if (ScreenHandler.SCREEN_OBJECTS_WITH_ID.get(
                  MyCreature.getX() + "," + MyCreature.getY() + "," + MyCreature.getZ())
              != null) {
            if (ScreenHandler.SCREEN_OBJECTS_WITH_ID
                .get(MyCreature.getX() + "," + MyCreature.getY() + "," + MyCreature.getZ())
                .getObject()
                .getName()
                .contains("bridge")) {
              showParticles = false;
              bridge = true;
            }
          }
          if (showParticles) {
            ScreenHandler.SCREEN_TILES
                .get(MyCreature.getX() + "," + MyCreature.getY() + "," + MyCreature.getZ())
                .showDust(
                    MyCreature.getX() * ClientSettings.TILE_SIZE + 25,
                    MyCreature.getY() * ClientSettings.TILE_SIZE + 40);
          }
        }

        if (bridge) {
          tileType = "indoors";
        } else if (carpet) {
          tileType = "carpet";
        }

        if (MyCreature.getSizeWidth() + MyCreature.getSizeHeight() > 6) {
          ScreenHandler.myCamera.shake();
          Sfx.playRandomPitch("walk/big");
        } else {
          Sfx.playRandomPitch("walk/" + tileType);
        }
      }

      setWalking(false);

    } else {

      timerMove.schedule(
          new TimerTask() {
            @Override
            public void run() {
              move();
            }
          },
          MOVE_MONSTER_DELAY);
    }
  }

  public void walkTo(int newX, int newY, int newZ, int speed) {
    setWalking(true);

    int oldX = MyCreature.getX();
    int oldY = MyCreature.getY();

    MyCreature.setX(newX);
    MyCreature.setY(newY);
    MyCreature.setZ(newZ);

    walkItr = 35;

    if (MyCreature.getCreatureType() == CreatureType.Monster) {

      if (((Npc) MyCreature).isAggro()) {
        speed *= 2;
      }
    }

    MOVE_MONSTER_DELAY = 14 * 1000 / (speed + 900);

    if (oldX < MyCreature.getX()) {
      moveX = -ClientSettings.TILE_SIZE;
    }

    if (oldX > MyCreature.getX()) {
      moveX = ClientSettings.TILE_SIZE;
    }
    if (oldY < MyCreature.getY()) {
      moveY = -ClientSettings.TILE_SIZE;
    }

    if (oldY > MyCreature.getY()) {
      moveY = ClientSettings.TILE_SIZE;
    }

    if (moveX != 0 && moveY != 0) {
      walkDiagonal = true;
    } else {
      walkDiagonal = false;
    }

    move();
  }

  public void walkFromTo(int newX, int newY, int speed) {
    setWalking(true);

    int oldX = MyCreature.getX();
    int oldY = MyCreature.getY();

    MyCreature.setX(newX);
    MyCreature.setY(newY);

    int gotoXdist = newX - oldX;
    int gotoYdist = newY - oldY;

    walkItr = Math.round(Math.sqrt(Math.pow(gotoXdist, 2) + Math.pow(gotoYdist, 2))) * 35;

    MOVE_MONSTER_DELAY = 14 * 1000 / (speed + 900);

    moveX = -ClientSettings.TILE_SIZE * gotoXdist;
    moveY = -ClientSettings.TILE_SIZE * gotoYdist;

    if (moveX != 0 && moveY != 0) {
      walkDiagonal = true;
    } else {
      walkDiagonal = false;
    }

    move();
  }

  public void resetMove() {
    moveX = 0;
    moveY = 0;
  }

  public void setMoveX(int newMoveX) {
    moveX = newMoveX;
  }

  public int getMoveX() {
    return Math.round(moveX);
  }

  public void setMoveY(int newMoveY) {
    moveY = newMoveY;
  }

  public int getMoveY() {
    return Math.round(moveY);
  }

  public void setWalkDiagonal(boolean newState) {
    walkDiagonal = true;
  }

  public float getWalkItr() {
    return walkItr;
  }

  public void setOldX(int x) {
    oldX = x;
  }

  public void setOldY(int y) {
    oldY = y;
  }

  public boolean isWalking() {
    return Walking;
  }

  public void setWalking(boolean walking) {
    Walking = walking;
    if (!Walking) {
      walkItr = 0;
    }
  }
}
