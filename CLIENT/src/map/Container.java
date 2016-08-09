package map;

import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

public class Container {

  private String type;
  private boolean open = false;

  private Image graphics_closed;
  private Image graphics_opened;

  public Container(String newType) {
    type = newType;

    try {
      graphics_opened = new Image("images/objects/" + type + "_open.png");
      graphics_closed = new Image("images/objects/" + type + ".png");
    } catch (SlickException e) {
      e.printStackTrace();
    }
  }

  public Image getImage() {
    if (open) {
      return graphics_opened;
    }
    return graphics_closed;
  }

  public void draw(int x, int y) {
    if (open) {
      graphics_opened.draw(x, y);
    } else {
      graphics_closed.draw(x, y);
    }
  }

  public void setOpen(boolean openStatus) {
    open = openStatus;
  }

  public void open() {
    open = true;
  }

  public boolean isOpen() {
    return open;
  }

  public String getType() {
    return type;
  }
}
