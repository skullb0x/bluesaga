package creature;

import org.newdawn.slick.util.pathfinding.Mover;

import creature.Creature.CreatureType;

public class PathMover implements Mover {
  private CreatureType Type;
  private int TargetX;
  private int TargetY;
  private int TargetZ;

  public PathMover(CreatureType type) {
    Type = type;
  }

  public CreatureType getType() {
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
