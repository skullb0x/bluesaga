package game;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.TrueTypeFont;

public class TextField {

  private org.newdawn.slick.gui.TextField myTextField;
  private String myLable;
  private org.newdawn.slick.Font myFont;
  private int myX;
  private int myY;

  TextField(
      String aLable, int aX, int aY, int aWidth, String aInitialValue, GameContainer aContainer) {
    myX = aX;
    myY = aY;
    int headingOffset = 20;
    java.awt.Font awtFont = new java.awt.Font("Times New Roman", java.awt.Font.BOLD, 14);
    myFont = new TrueTypeFont(awtFont, false);
    myTextField =
        new org.newdawn.slick.gui.TextField(
            aContainer, myFont, myX, myY + headingOffset, aWidth, 20);
    myTextField.setBackgroundColor(Color.white);
    myTextField.setTextColor(Color.black);
    myTextField.setText(aInitialValue);

    myLable = aLable;
  }

  public void Render(GameContainer aContainer, Graphics aGraphics) {
    myFont.drawString(myX, myY, myLable);
    myTextField.render(aContainer, aGraphics);
  }

  public String GetLable() {
    return myLable;
  }

  public String GetText() {

    return myTextField.getText();
  }
}
