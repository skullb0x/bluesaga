package menus;

import java.io.File;
import java.util.Arrays;
import java.util.Vector;

import map.Tile;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import game.BP_EDITOR;
import game.Database;
import gui.TextureButton;

public class TextureMenu {

  private Vector<TextureButton> Buttons;
  private int X;
  private int Y;
  private boolean Active = true;
  private String ActivePath = "../CLIENT/src/images/textures/";

  private Vector<String> PathHistory;

  public TextureMenu(int x, int y) {
    X = x;
    Y = y;

    Buttons = new Vector<TextureButton>();
    PathHistory = new Vector<String>();
  }

  public void goBack() {
    if (PathHistory.size() > 0) {
      ActivePath = PathHistory.lastElement();
      PathHistory.remove(PathHistory.size() - 1);
    }
  }

  public void goForward(String path) {
    PathHistory.add(ActivePath);

    ActivePath += path;
  }

  public void load() {

    Buttons.clear();

    File[] files = new File(ActivePath).listFiles();
    Arrays.sort(files);

    int i = 0;

    Buttons.add(
        new TextureButton(X + (i % 8) * 50, (int) (Y + Math.floor(i / 8) * 50), "Back", ""));
    i++;

    Buttons.add(
        new TextureButton(X + (i % 8) * 50, (int) (Y + Math.floor(i / 8) * 50), "Delete", ""));
    i++;

    for (File file : files) {
      if (file.isFile()) {
        int mid = file.getName().lastIndexOf(".");
        String name = file.getName().substring(0, mid);
        String ext = file.getName().substring(mid + 1, file.getName().length());

        /*
        	if(!file.getName().contains("DL") && !file.getName().contains("DR")
        	&& !file.getName().contains("UL") && !file.getName().contains("UR") && !file.getName().contains("U")
        	&& !file.getName().contains("L") && !file.getName().contains("R")
        	&& !file.getName().contains("IUL") && !file.getName().contains("IUR")
        	&& !file.getName().contains("IDL") && !file.getName().contains("IDR")){
        */
        if (ext.equals("png")) {
          if (!name.contains("_1") && !name.contains("_2")) {
            Buttons.add(
                new TextureButton(
                    X + (i % 8) * 50,
                    (int) (Y + Math.floor(i / 8) * 50),
                    name,
                    ActivePath + file.getName()));
            i++;
          }
        }

        /*
        }
        */

      } else if (file.isDirectory()) {
        Buttons.add(
            new TextureButton(
                X + (i % 8) * 50, (int) (Y + Math.floor(i / 8) * 50), file.getName(), "Directory"));
        i++;
      }
    }
  }

  public void draw(Graphics g, int mouseX, int mouseY) {
    if (Active) {
      g.setColor(new Color(238, 82, 65, 255));
      g.fillRect(X, Y, 400, 500);

      g.setColor(new Color(255, 255, 255, 255));

      for (TextureButton button : Buttons) {
        button.draw(g, mouseX, mouseY);
      }
      g.setColor(new Color(255, 255, 255, 255));
      g.drawRect(X, Y, 400, 500);
    }
  }

  public int click(int mouseX, int mouseY, Database gameDB) {
    int buttonIndex = 0;
    for (TextureButton button : Buttons) {
      if (button.clicked(mouseX, mouseY)) {

        if (button.getType().equals("Folder")) {
          goForward(button.getName() + "/");
          BP_EDITOR.loading();
          load();
          return 999;
        } else if (button.getType().equals("Back")) {
          goBack();
          BP_EDITOR.loading();
          load();
          return 999;
        } else if (button.getType().equals("Delete")) {
          return 998;
        }
        return buttonIndex;
      }
      buttonIndex++;
    }
    return 1000;
  }

  public void toggle() {
    if (Active) {
      Active = false;
    } else {
      Active = true;
    }
  }

  public boolean isActive() {
    return Active;
  }

  public Tile getTile(int tileIndex) {
    return Buttons.get(tileIndex).getTile();
  }
}
