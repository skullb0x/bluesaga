package menus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;

import gui.GuiList;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.gui.TextField;

import game.BP_EDITOR;
import game.Database;

public class NewMenu {

  private int X;
  private int Y;
  private boolean Active;

  private boolean Generating;

  private TextField nameField;
  private TextField sizeField;
  private GuiList typeList;

  private Timer timer;

  public NewMenu(int x, int y, GameContainer app) {
    Active = false;
    Generating = false;
    timer = new Timer();

    X = x;
    Y = y;

    nameField = new TextField(app, BP_EDITOR.FONTS.size12, X + 18, Y + 45, 150, 20);
    nameField.setBackgroundColor(new Color(0, 0, 0, 80));
    nameField.setBorderColor(new Color(0, 0, 0, 0));
    nameField.setFocus(false);
    nameField.setCursorVisible(true);

    sizeField = new TextField(app, BP_EDITOR.FONTS.size12, X + 18, Y + 95, 50, 20);
    sizeField.setBackgroundColor(new Color(0, 0, 0, 80));
    sizeField.setBorderColor(new Color(0, 0, 0, 0));
    sizeField.setFocus(false);
    sizeField.setCursorVisible(true);
    sizeField.setMaxLength(3);

    typeList = new GuiList(200, 100);

    ResultSet mapType = BP_EDITOR.mapDB.askDB("select distinct(Type) from area_tile");
    try {
      while (mapType.next()) {
        typeList.addItem(mapType.getString("Type"));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void draw(Graphics g, GameContainer app) {
    if (Active) {
      if (!Generating) {
        g.setColor(new Color(238, 82, 65, 255));
        g.fillRect(X, Y, 213, 255);

        g.setColor(new Color(255, 255, 255, 255));

        g.drawString("Name:", X + 20, Y + 20);
        nameField.render(app, g);

        g.drawString("Size:", X + 20, Y + 70);
        sizeField.render(app, g);

        g.drawString("Type:", X + 20, Y + 130);
        typeList.draw(g, X, Y + 150);
      } else {
        g.setColor(new Color(238, 82, 65, 255));
        g.fillRect(X, Y, 260, 100);
        g.setColor(new Color(255, 255, 255, 255));
        g.drawString("Generating new map...", X + 20, Y + 40);
      }
    }
  }

  public boolean isActive() {
    return Active;
  }

  public void toggle() {
    if (Active) {
      Active = false;
    } else {
      Generating = false;
      Active = true;
      nameField.setFocus(true);
    }
  }

  public void keyLogic(Input INPUT, Database gameDB) {
    if (INPUT.isKeyPressed(Input.KEY_UP)) {
      typeList.moveCursor("UP");
    } else if (INPUT.isKeyPressed(Input.KEY_DOWN)) {
      typeList.moveCursor("DOWN");
    } else if (INPUT.isKeyPressed(Input.KEY_ESCAPE)) {
      Active = false;
    }

    if (INPUT.isKeyPressed(Input.KEY_TAB)) {
      if (nameField.hasFocus()) {
        nameField.setFocus(false);
        sizeField.setFocus(true);
      } else {
        nameField.setFocus(true);
        sizeField.setFocus(false);
      }
    }

    if (INPUT.isKeyPressed(Input.KEY_ENTER)) {
      if (!nameField.getText().equals("") && !sizeField.getText().equals("")) {
        createMap();
      }
    }
  }

  public void createMap() {

    Generating = true;

    timer.schedule(
        new TimerTask() {
          public void run() {
            Generating = false;
            toggle();
          }
        },
        50);
  }

  public boolean isGenerating() {
    return Generating;
  }
}
