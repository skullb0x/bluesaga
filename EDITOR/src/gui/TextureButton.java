package gui;

import game.BP_EDITOR;
import graphics.Sprite;
import map.Tile;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

public class TextureButton {

  public final static Color BLACK = new Color(0, 0, 0, 255);
  public final static Color WHITE = new Color(255, 255, 255, 255);

  private Sprite ButtonImage;
  private int X = 0;
  private int Y = 0;
  private int width = 50;
  private int height = 50;
  private String imagePath;
  private String name;
  private String type;

  private Tile TILE;

  public TextureButton(int x, int y, String newName, String newPath) {
    X = x;
    Y = y;

    imagePath = newPath;
    if (newName.equals("Back")) {
      imagePath = "gui/editor/Directory";
      name = newName;
      type = "Back";

    } else if (imagePath.equals("Directory")) {
      imagePath = "gui/editor/Directory";
      name = newName;
      type = "Folder";

    } else if (newName.equals("Delete")) {
      imagePath = "gui/editor/deleteButton";
      name = newName;
      type = "Delete";

    } else {
      String path[] = newPath.split("/images/");
      imagePath = path[1];

      if (imagePath.contains("_0")) {
        imagePath = imagePath.substring(0, imagePath.length() - 6);
      } else {
        imagePath = imagePath.substring(0, imagePath.length() - 4);
      }

      type = "Image";
      name = newName;

      String folderName[] = imagePath.substring(9, imagePath.length()).split("/");
      String tileType = folderName[0];

      TILE = new Tile(x, y, 0);
      boolean passable = BP_EDITOR.isTilePassable(TILE);
      TILE.setType(tileType, name, passable);
      TILE.setMENU(true);
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
      g.setColor(BLACK);
      g.setFont(BP_EDITOR.FONTS.size8);
      g.drawString(name, X + 10, Y + 30);
    } else if (type.equals("Back")) {
      ButtonImage.draw(X, Y);
      g.setColor(BLACK);
      g.setFont(BP_EDITOR.FONTS.size8);
      g.drawString("..", X + 10, Y + 30);
    } else if (type.equals("Delete")) {
      ButtonImage.draw(X, Y);
      ButtonImage.draw(X, Y);
      g.setColor(WHITE);
      g.drawRect(X, Y, width, height);
    } else {
      ButtonImage.draw(X, Y);
      g.setColor(WHITE);
      g.drawRect(X, Y, width, height);
      if (BP_EDITOR.canFixEdges(name)) {
        g.setColor(BLACK);
        g.setFont(BP_EDITOR.FONTS.size12bold);
        g.drawString("F", X + 7, Y + 7);
        g.setColor(WHITE);
        g.setFont(BP_EDITOR.FONTS.size12bold);
        g.drawString("F", X + 5, Y + 5);
      }
    }

    if (X < mouseX && X + width > mouseX && Y < mouseY && Y + height > mouseY) {
      g.setColor(new Color(255, 255, 255, 100));
      g.fillRect(X, Y, width, height);
    }
  }

  public String getName() {
    return name;
  }

  public String getImagePath() {
    return imagePath;
  }

  public String getType() {
    return type;
  }

  public Tile getTile() {
    return TILE;
  }
}
