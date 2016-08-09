package creature;

import org.newdawn.slick.util.pathfinding.Mover;

public class PathMover implements Mover {
  private String Type;
  private int TargetX;
  private int TargetY;
  private int TargetZ;

  public PathMover(String type) {
    Type = type;
  }

  public String getType() {
    return Type;
  }

  public void setTarget(int targetX, int targetY, int targetZ) {
    TargetX = targetX;
    TargetY = targetY;
    TargetZ = targetZ;
  }

  public int getTargetX() {
    return TargetX;
  }

  public int getTargetY() {
    return TargetY;
  }

  public int getTargetZ() {
    return TargetZ;
  }
}
