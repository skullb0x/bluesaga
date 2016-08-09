package gui.windows;

import graphics.Font;

import java.util.Vector;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;

import utils.LanguageUtils;

public class ItemInfoBox extends Window {

  public ItemInfoBox(int x, int y, int width, int height) {
    super("ItemInfoBoxW", x, y, width, height, false);

    setBorderColor(new Color(130, 130, 130));
    setBgColor(new Color(27, 27, 27));
    // TODO Auto-generated constructor stub
  }

  private Vector<String> info;
  private Vector<Color> colors;

  public void load(String infoData) {
    info = new Vector<String>();
    colors = new Vector<Color>();

    String[] data = infoData.split("/");

    // ColorR, ColorG, ColorB; Value / ColorR, ColorG, ColorB; Value
    // ...

    int maxWidth = 0;
    int maxHeight = data.length * 15 + 40;

    for (String dataRow : data) {
      String colorAndText[] = dataRow.split(";");

      if (colorAndText.length > 1) {
        if (!colorAndText[0].equals("0")) {
          String colorInfo[] = colorAndText[0].split(",");
          colors.add(
              new Color(
                  Integer.parseInt(colorInfo[0]),
                  Integer.parseInt(colorInfo[1]),
                  Integer.parseInt(colorInfo[2])));

          String textToAdd = "";

          String parts[] = colorAndText[1].split("#");
          for (String part : parts) {
            if (part.contains(".")) {
              textToAdd += LanguageUtils.getString(part);
            } else {
              textToAdd += part;
            }
          }
          info.add(textToAdd);
          if (Font.size10.getWidth(textToAdd) > maxWidth) {
            maxWidth = Font.size10.getWidth(textToAdd);
          }
        }
      }
    }

    maxWidth += 30;

    setSize(maxWidth, maxHeight);

    open();
  }

  @Override
  public void draw(GameContainer app, Graphics g, int mouseX, int mouseY) {
    super.draw(app, g, 0, 0);
    if (isFullyOpened()) {
      g.setFont(Font.size10);

      for (int i = 0; i < info.size(); i++) {
        g.setColor(colors.get(i));
        g.drawString(info.get(i), X + 10, Y + 20 + i * 15);
      }
    }
  }

  @Override
  public void open() {
    super.open();
    //BlueSaga.GUI.MaxWindowsZ--;
    //setDepthZ(BlueSaga.GUI.MaxWindowsZ);
  }
}
