package gui;

import game.BP_EDITOR;
import graphics.Sprite;
import map.TileObject;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

public class ObjectButton {

  private Sprite ButtonImage;
  private int X = 0;
  private int Y = 0;
  private int width = 50;
  private int height = 50;
  private String imagePath;
  private String name;
  private String type;

  private TileObject TILE_OBJECT;

  public ObjectButton(int x, int y, String newName, boolean directory) {
    X = x;
    Y = y;

    if (directory) {
      imagePath = "gui/editor/Directory";
      name = newName;
      type = "Folder";
    } else if (newName.equals("Delete")) {
      imagePath = "gui/editor/deleteButton";
      name = newName;
      type = "Delete";
    } else {
      type = "Image";
      name = newName;

      imagePath = "objects/" + newName;

      TILE_OBJECT = new TileObject(newName);
      TILE_OBJECT.setMENU(true);
    }

    if (newName.equals("..")) {
      type = "Back";
    }

    ButtonImage = BP_EDITOR.GFX.getSprite(imagePath);
  }

  public boolean clicked(int mouseX, int mouseY) {
    if (mouseX > X && mouseX < X + width && mouseY > Y && mouseY < Y + height) {
      return true;
    }
    return false;
  }

  public void draw(Graphics g, int mouseX, int mouseY) {

    if (type.equals("Folder")) {
      ButtonImage.draw(X, Y);
      g.setColor(new Color(0, 0, 0, 255));
      g.setFont(BP_EDITOR.FONTS.size8);
      g.drawString(name, X + 10, Y + 30);
    } else if (!type.equals("Back")) {
      ButtonImage.draw(X, Y);
    } else {
      ButtonImage.draw(X, Y);
      g.setColor(new Color(0, 0, 0, 255));
      g.setFont(BP_EDITOR.FONTS.size8);
      g.drawString("..", X + 10, Y + 30);
    }
    if (X < mouseX && X + width > mouseX && Y < mouseY && Y + height > mouseY) {
      g.setColor(new Color(255, 255, 255, 100));
      g.fillRect(X, Y, width, height);
    }
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  public TileObject getTileObject() {
    return TILE_OBJECT;
  }
}
