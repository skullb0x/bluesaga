package menus;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.gui.TextField;

import game.BP_EDITOR;

public class TriggerMenu {
  private boolean Active;

  private TextField trapField;
  private TextField doorField;

  private int startX = 400;
  private int startY = 350;

  public TriggerMenu(GameContainer app) {
    Active = false;

    trapField = new TextField(app, BP_EDITOR.FONTS.size12, startX + 68, startY + 45, 100, 20);
    trapField.setBackgroundColor(new Color(0, 0, 0, 80));
    trapField.setBorderColor(new Color(0, 0, 0, 0));
    trapField.setFocus(false);
    trapField.setCursorVisible(true);

    doorField = new TextField(app, BP_EDITOR.FONTS.size12, startX + 68, startY + 70, 100, 20);
    doorField.setBackgroundColor(new Color(0, 0, 0, 80));
    doorField.setBorderColor(new Color(0, 0, 0, 0));
    doorField.setFocus(false);
    doorField.setCursorVisible(true);
  }

  public void draw(Graphics g, GameContainer app) {
    if (Active) {

      g.setColor(new Color(238, 82, 65, 255));
      g.fillRect(startX, startY, 213, 255);

      g.setColor(new Color(255, 255, 255, 255));

      g.drawString("Trap Id:", startX + 10, startY + 45);
      trapField.render(app, g);

      g.drawString("Door Id:", startX + 10, startY + 70);
      doorField.render(app, g);
    }
  }

  public boolean isActive() {
    return Active;
  }

  public void toggle() {
    if (Active) {
      Active = false;
    } else {
      Active = true;
      trapField.setText("0");
      doorField.setText("0");
      trapField.setFocus(true);
    }
  }

  public void keyLogic(Input INPUT) {
    if (INPUT.isKeyPressed(Input.KEY_ESCAPE)) {
      Active = false;
    }

    if (INPUT.isKeyPressed(Input.KEY_TAB)) {
      if (trapField.hasFocus()) {
        doorField.setFocus(true);
        trapField.setFocus(false);
      } else {
        doorField.setFocus(false);
        trapField.setFocus(true);
      }
    }

    if (INPUT.isKeyPressed(Input.KEY_ENTER)) {
      if (!trapField.getText().equals("") && !doorField.getText().equals("")) {
        BP_EDITOR.TRIGGER_TRAP_ID = Integer.parseInt(trapField.getText());
        BP_EDITOR.TRIGGER_DOOR_ID = Integer.parseInt(doorField.getText());
        BP_EDITOR.PLACE_TRIGGER = true;

        toggle();
      }
    }
  }
}
