package gui.windows;

import graphics.BlueSagaColors;
import graphics.Font;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;

public class BookWindow extends Window {

  private String Name;
  private String Text;

  public BookWindow(int x, int y, int width, int height, boolean ShowCloseButton) {
    super("BookW", x, y, width, height, ShowCloseButton);
    setBorderColor(new Color(142, 95, 18));
    setBgColor(new Color(255, 212, 109));

    // TODO Auto-generated constructor stub
  }

  public void load(String name, String text) {
    setName(name);
    setText(text);
    open();
  }

  public static String justifyLeft(int width, String st) {
    StringBuffer buf = new StringBuffer(st);
    int lastspace = -1;
    int linestart = 0;
    int i = 0;

    //tempTextLines = 1;
    while (i < buf.length()) {
      if (buf.charAt(i) == ' ') lastspace = i;
      if (buf.charAt(i) == '\n') {
        lastspace = -1;
        linestart = i + 1;
        //tempTextLines++;
      }
      if (i > linestart + width - 1) {
        if (lastspace != -1) {
          buf.setCharAt(lastspace, '\n');
          linestart = lastspace + 1;
          lastspace = -1;
          //tempTextLines++;
        } else {
          buf.insert(i, '\n');
          linestart = i + 1;
          // tempTextLines++;
        }
      }
      i++;
    }

    return buf.toString();
  }

  @Override
  public void draw(GameContainer app, Graphics g, int mouseX, int mouseY) {
    if (isVisible()) {
      super.draw(app, g, mouseX, mouseY);
      if (isFullyOpened()) {
        g.setFont(Font.size12);
        g.setColor(BlueSagaColors.BLACK);
        g.drawString(Name, X + 20 + moveX, Y + 20 + moveY);

        g.setFont(Font.size10);
        g.setColor(BlueSagaColors.BLACK);
        g.drawString(Text, X + 20 + moveX, Y + 60 + moveY);
      }
    }
  }

  @Override
  public void leftMouseClick(Input INPUT) {
    super.leftMouseClick(INPUT);
  }

  @Override
  public String getName() {
    return Name;
  }

  @Override
  public void setName(String name) {
    Name = name;
  }

  public String getText() {
    return Text;
  }

  public void setText(String text) {
    Text = justifyLeft(40, text);
  }
}
