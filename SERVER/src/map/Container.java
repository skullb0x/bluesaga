package map;

public class Container {

  private int Id;
  private String type;
  private boolean open = false;
  private boolean passable = false;
  private int respawnTime = 2600; // x 2 sek
  private int respawnTimeItr = 0;

  public Container(String newType) {
    type = newType;
  }

  public void open() {
    open = true;
    if (type.equals("barrel")) {
      passable = true;
    }
    respawnTimeItr = 0;
  }

  public boolean isOpen() {
    return open;
  }

  public boolean isPassable() {
    return passable;
  }

  public String getType() {
    return type;
  }

  public int getId() {
    return Id;
  }

  public void setId(int id) {
    Id = id;
  }

  public void checkRespawn() {
    if (open) {
      if (respawnTimeItr < respawnTime) {
        respawnTimeItr++;
      } else {
        open = false;
        if (type.equals("barrel")) {
          passable = false;
        }
      }
    }
  }
}
