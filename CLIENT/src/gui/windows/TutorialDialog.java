package gui.windows;

import java.util.Vector;

import graphics.BlueSagaColors;
import graphics.Font;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;

public class TutorialDialog extends Window {

  private int step = 999;

  private Vector<String> tutorials;

  private Vector<String> tutorialText;

  public TutorialDialog() {
    super("Tutorial Dialog", 312, 100, 400, 100, false);

    setMovable(false);

    // TODO Auto-generated constructor stub
    tutorials = new Vector<String>();

    tutorials.add("CLICK WITH THE MOUSE/OR USE THE KEYBOARD TO WALK");
    tutorials.add("RIGHT-CLICK ON THE MOUSTACHE-BLOB/TO TALK TO HIM");
    tutorials.add("RIGHT-CLICK ONCE ON THE PRACTICE TARGET/TO START ATTACKING IT");
    tutorials.add("RETURN TO MOUSTACHE BLOB/AND TALK TO HIM IN ORDER/TO GET YOUR REWARD");
    tutorials.add(
        "WHEN YOU DIE, YOU LOSE SOME XP AND YOUR INVENTORY ITEMS./RETURN TO WHERE YOU DIED TO COLLECT YOUR ITEMS AND SOUL.");
  }

  public void draw(GameContainer app, Graphics g, int mouseX, int mouseY) {
    if (isVisible()) {
      super.draw(app, g, mouseX, mouseY);

      g.setFont(Font.size18);
      g.setColor(BlueSagaColors.WHITE);

      int textY = Y + 30;
      if (tutorials.get(step) != null) {
        for (String text : tutorialText) {
          g.drawString(text, 512 - Font.size18.getWidth(text) / 2.0f, textY);
          textY += 30;
        }
      }
    }
  }

  public void viewStep(int step) {
    if (tutorials.get(step) != null) {
      String[] tutorialParts = tutorials.get(step).split("/");

      int maxWidth = 0;

      tutorialText = new Vector<String>();
      for (String part : tutorialParts) {
        tutorialText.add(part);
        if (Font.size18.getWidth(part) > maxWidth) {
          maxWidth = Font.size18.getWidth(part);
        }
      }

      setWidth(maxWidth + 80);
      setX(512 - ((maxWidth + 80) / 2));
      setHeight((tutorialText.size() - 1) * 30 + 18 + 60 + 4);

      open();
      this.step = step;
    }
  }

  public int getStep() {
    return step;
  }

  public void setStep(int step) {
    this.step = step;
  }
}
