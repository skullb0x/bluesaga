package game;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.gui.TextField;

public class WarningTextManager {

  private TextField myWarningField;
  private float myTimeToShow;
  private float myTimeLeft;
  private org.newdawn.slick.Font myFont;

  public WarningTextManager(int aX, int aY, GameContainer aContainer) {
    java.awt.Font awtFont = new java.awt.Font("Times New Roman", java.awt.Font.BOLD, 14);
    myFont = new TrueTypeFont(awtFont, false);
    myWarningField = new TextField(aContainer, myFont, aX, aY, 100, 100);
    myWarningField.setBackgroundColor(new org.newdawn.slick.Color(0, 0, 0, 0));
    myWarningField.setBorderColor(new org.newdawn.slick.Color(0, 0, 0, 0));
    myWarningField.setTextColor(new org.newdawn.slick.Color(255, 0, 0, 255));
    myTimeToShow = 5.0f;
    myTimeLeft = 0.0f;
  }

  public void Update(float aElapsedtime) {
    if (myTimeLeft > 0.0f) {
      myTimeLeft -= aElapsedtime;
    }
  }

  public void Render(GameContainer aContainer, Graphics aGraphics) {
    if (myTimeLeft > 0.0f) {

      myWarningField.render(aContainer, aGraphics);
    }
  }

  public void ShowText(String aText) {
    myWarningField.setText(aText);
    myTimeLeft = myTimeToShow;
  }
}
