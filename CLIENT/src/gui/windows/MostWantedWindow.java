package gui.windows;

import graphics.Font;
import graphics.ImageResource;

import java.util.Iterator;
import java.util.LinkedHashMap;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;

import utils.LanguageUtils;

public class MostWantedWindow extends Window {

  private boolean Active;

  private LinkedHashMap<String, Integer> Bounties;

  private Image name_plate;
  private Image mostwanted_label;
  private Image bounty_label;

  public MostWantedWindow(int x, int y, int width, int height) {
    super("MostWantedW", x, y, width, height, true);

    X = 305;
    Y = 95;
    Active = false;

    Bounties = new LinkedHashMap<String, Integer>();

    name_plate = ImageResource.getSprite("gui/pvp/bounty_name_bg").getImage();
    mostwanted_label = ImageResource.getSprite("gui/pvp/most_wanted_label").getImage();
    bounty_label = ImageResource.getSprite("gui/pvp/bounty_label").getImage();
  }

  public void load(String wantedData) {
    Bounties.clear();

    if (!wantedData.equals("none")) {
      String wanted_info[] = wantedData.split(";");

      for (String wantedInfo : wanted_info) {
        String playerInfo[] = wantedInfo.split(",");
        Bounties.put(playerInfo[0], Integer.parseInt(playerInfo[1]));
      }
    }
  }

  @Override
  public void draw(GameContainer app, Graphics g, int mouseX, int mouseY) {
    if (isVisible()) {
      super.draw(app, g, mouseX, mouseY);
      if (isFullyOpened()) {
        int totalX = X + moveX;
        int totalY = Y + moveY;

        mostwanted_label.draw(totalX + 90, totalY + 15);

        if (Bounties.isEmpty()) {
          g.setColor(new Color(255, 255, 255, 255));
          g.setFont(Font.size20);
          g.drawString(LanguageUtils.getString("ui.bounty.no_bounties"), totalX + 60, totalY + 70);
        } else {
          bounty_label.draw(totalX + 323, totalY + 55);

          g.setFont(Font.size16);
          g.setColor(new Color(0, 0, 0, 255));

          int i = 0;

          for (Iterator<String> iter = Bounties.keySet().iterator(); iter.hasNext(); ) {

            name_plate.draw(totalX + 15, totalY + 80 + i * 35);

            String key = iter.next().toString();
            int value = Bounties.get(key);

            g.drawString(key, totalX + 45, totalY + 86 + i * 35);

            int textWidth = Font.size20.getWidth(value + " cc");

            g.drawString(value + " cc", totalX + 415 - textWidth, totalY + 86 + i * 35);

            i++;
          }
        }
      }
    }
  }

  @Override
  public void leftMouseClick(Input INPUT) {
    super.leftMouseClick(INPUT);
  }

  @Override
  public void stopMove() {
    super.stopMove();
  }

  @Override
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
}
