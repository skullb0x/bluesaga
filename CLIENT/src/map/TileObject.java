package map;

import graphics.ImageResource;
import graphics.Sprite;
import screens.ScreenHandler;

import org.newdawn.slick.Graphics;

public class TileObject {

  private String name;

  private Sprite graphics;
  private int width = 1;
  private int height = 1;
  private int X;
  private int Y;
  private int Z;

  public TileObject(String newName, int x, int y, int z) {
    name = newName;
    graphics = ImageResource.getSprite("objects/" + name);

    if (graphics.isAnimated()) {
      width = graphics.getAnimation().getWidth() / 50;
      height = graphics.getAnimation().getHeight() / 50;
    } else {
      width = graphics.getImage().getWidth() / 50;
      height = graphics.getImage().getHeight() / 50;
    }
    setX(x);
    setY(y);
    setZ(z);
  }

  public void draw(Graphics g, int x, int y) {
    graphics.draw(x - (width - 1) * 25, y - ((height - 1) * 50));
    graphics.draw(
        x - (width - 1) * 25, y - ((height - 1) * 50), ScreenHandler.AREA_EFFECT.getTintColor());
  }

  public Sprite getSprite() {
    return graphics;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public String getName() {
    return name;
  }

  public int getX() {
    return X;
  }

  public void setX(int x) {
    X = x;
  }

  public int getY() {
    return Y;
  }

  public void setY(int y) {
    Y = y;
  }

  public int getZ() {
    return Z;
  }

  public void setZ(int z) {
    Z = z;
  }

  public void changeGraphics(String newName) {
    graphics = ImageResource.getSprite("objects/" + newName);
  }
}
