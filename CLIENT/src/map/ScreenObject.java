package map;

import org.newdawn.slick.Graphics;

import creature.Creature;
import creature.Npc;
import creature.PlayerCharacter;
import creature.Creature.CreatureType;

public class ScreenObject {
  private Creature myCreature = null;
  private TileObject myObject = null;
  private String type = "None";

  public ScreenObject() {
    type = "None";
  }

  public void setObjectCreature(Creature newCreature) {
    myCreature = newCreature;
    myObject = null;
    setType("Creature");
  }

  public void setObjectCreature(TileObject newObject) {
    myObject = newObject;
    myCreature = null;
    setType("Object");
  }

  public int getX() {
    if (type.equals("Object")) {
      return myObject.getX();
    } else {
      return myCreature.getX();
    }
  }

  public int getY() {
    if (type.equals("Object")) {
      return myObject.getY();
    } else {
      return myCreature.getY();
    }
  }

  public int getZ() {
    if (type.equals("Object")) {
      return myObject.getZ();
    } else {
      return myCreature.getZ();
    }
  }

  public void setType(String newType) {
    type = newType;
  }

  public String getType() {
    return type;
  }

  public Creature getCreature() {
    return myCreature;
  }

  public TileObject getObject() {
    return myObject;
  }

  public void clear() {
    type = "None";
    myCreature = null;
    myObject = null;
  }

  public void draw(Graphics g, int x, int y) {
    if (type.equals("Object")) {
      myObject.draw(g, x, y);
    } else if (type.equals("Creature")) {
      if (myCreature.getCreatureType() == CreatureType.Monster) {
        ((Npc) myCreature).draw(g, x, y);
      } else {
        ((PlayerCharacter) myCreature).draw(g, x, y);
      }
    }
  }
}
