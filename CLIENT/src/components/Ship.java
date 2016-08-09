package components;

import graphics.ImageResource;

public class Ship {
  private int DbId;
  private int ShipId;
  private boolean Show;

  public Ship(int shipId) {
    setShipId(shipId);
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

  public void draw(int x, int y) {
    if (isShow()) {
      ImageResource.getSprite("ships/splash").getAnimation().updateNoDraw();
      ImageResource.getSprite("ships/splash")
          .getAnimation()
          .getCurrentFrame()
          .drawCentered(x, y + 10);
      ImageResource.getSprite("ships/" + ShipId).getImage().drawCentered(x, y + 10);
    }
  }

  public boolean isShow() {
    return Show;
  }

  public void setShow(boolean show) {
    Show = show;
  }
}
