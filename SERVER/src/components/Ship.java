package components;

public class Ship {
  private int DbId;
  private int ShipId;
  private boolean Show;

  public Ship(int newId) {
    setShipId(newId);
    setShow(false);
  }

  public int getShipId() {
    return ShipId;
  }

  public void setShipId(int shipId) {
    ShipId = shipId;
  }

  public int getDbId() {
    return DbId;
  }

  public void setDbId(int dbId) {
    DbId = dbId;
  }

  public boolean isShow() {
    return Show;
  }

  public void setShow(boolean show) {
    Show = show;
  }
}
