package gui;

import game.ClientSettings;
import graphics.BlueSagaColors;
import graphics.Font;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

public class ToolTip {
  private String Text = "";
  private boolean Active;
  private int ShowDelayItr;
  private int ShowDelay = 20;

  private int TextWidth = 0;
  private int TextHeight = 0;

  public ToolTip() {
    setActive(false);
    ShowDelayItr = 0;
  }

  public String getText() {
    return Text;
  }

  public void setText(String text) {
    Text = text;
    TextWidth = Font.size10.getWidth(Text) + 20;
    TextHeight = Font.size10.getHeight(Text) + 20;
  }

  public void draw(Graphics g, int x, int y) {
    ShowDelayItr++;
    if (ShowDelayItr >= ShowDelay) {
      g.setColor(
          new Color(
              BlueSagaColors.RED.getRed(),
              BlueSagaColors.RED.getGreen(),
              BlueSagaColors.RED.getBlue(),
              150));
      int tooltipX = x;
      if (x + TextWidth > ClientSettings.SCREEN_WIDTH) {
        tooltipX = x - (x + TextWidth - ClientSettings.SCREEN_WIDTH) - 10;
      }
      g.setFont(Font.size10);
      g.fillRoundRect(tooltipX, y, TextWidth, TextHeight, 10);
      g.setColor(new Color(255, 255, 255));
      g.drawString(Text, tooltipX + 10, y + 10);
    }
  }

  public boolean isActive() {
    return Active;
  }

  public void setActive(boolean active) {
    ShowDelayItr = 0;
    Active = active;
  }
}
