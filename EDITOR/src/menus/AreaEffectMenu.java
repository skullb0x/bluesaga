package menus;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.gui.TextField;

import game.BP_EDITOR;

public class AreaEffectMenu {

  private boolean Active;

  private TextField nrField;

  private int startX = 400;
  private int startY = 350;

  public AreaEffectMenu(GameContainer app) {
    Active = false;

    nrField = new TextField(app, BP_EDITOR.FONTS.size12, startX + 18, startY + 45, 150, 20);
    nrField.setBackgroundColor(new Color(0, 0, 0, 80));
    nrField.setBorderColor(new Color(0, 0, 0, 0));
    nrField.setFocus(false);
    nrField.setCursorVisible(true);
  }

  public void draw(Graphics g, GameContainer app) {
    if (Active) {

      g.setColor(new Color(238, 82, 65, 255));
      g.fillRect(startX, startY, 213, 255);

      g.setColor(new Color(255, 255, 255, 255));

      g.drawString("Area Effect Id:", startX + 20, startY + 20);
      nrField.render(app, g);
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
      nrField.setText("");
      nrField.setFocus(true);
    }
  }

  public void keyLogic(Input INPUT) {
    if (INPUT.isKeyPressed(Input.KEY_ESCAPE)) {
      Active = false;
    }

    if (INPUT.isKeyPressed(Input.KEY_ENTER) || INPUT.isKeyPressed(Input.KEY_NUMPADENTER)) {
      String valueStr = nrField.getText();
      if (valueStr.equals("")) {
          BP_EDITOR.AREA_EFFECT_ID = -1;
      }
      else {
        try {
          BP_EDITOR.AREA_EFFECT_ID = Integer.parseInt(valueStr);
        }
        catch (NumberFormatException ex) {
          System.err.println("ERROR - Not a AREA_EFFECT_ID: " + valueStr);
        }

        if (BP_EDITOR.AREA_EFFECT_ID > 0) {
          ResultSet effectRS =
              BP_EDITOR.mapDB.askDB(
                  "select Id from area_effect where Id = " + BP_EDITOR.AREA_EFFECT_ID);

          try {
            if (effectRS.next()) {

            } else {
              BP_EDITOR.mapDB.updateDB(
                  "insert into area_effect (Id, AreaName) values ("
                      + BP_EDITOR.AREA_EFFECT_ID
                      + ",'')");

              ResultSet newEffectRS =
                  BP_EDITOR.mapDB.askDB("select Id from area_effect order by Id desc");
              if (newEffectRS.next()) {
                BP_EDITOR.AREA_EFFECT_ID = newEffectRS.getInt("Id");
              }
              newEffectRS.close();
            }
            effectRS.close();
          } catch (SQLException e) {
            e.printStackTrace();
          }
        }
      }
      toggle();
    }
  }
}
