package gui;

import graphics.Font;

import java.util.Timer;
import java.util.TimerTask;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

public class Notification {

  private int opacity;
  protected Timer showTimer;
  protected String text;
  protected String justifiedText;

  private Color bgColor;
  private boolean active;
  protected boolean fade;
  protected boolean startedTimer;

  private int nrTextLines;

  private int goalY;
  protected int y;

  private int duplicates;

  protected boolean request = false;

  public Notification(String newText, int newY, Color newBgColor) {
    text = newText;
    justifiedText = justifyLeft(30, text);

    duplicates = 0;

    bgColor = newBgColor;

    goalY = newY;
    y = newY;

    active = true;
    fade = false;
    startedTimer = false;

    opacity = 200;

    showTimer = new Timer();

    if (y == 80) {
      startedTimer = true;
      showTimer.schedule(
          new TimerTask() {
            @Override
            public void run() {
              fade = true;
            }
          },
          nrTextLines * 3000);
    }
  }

  public void update() {
    if (fade && y == 80) {
      opacity -= 6;
      if (opacity <= 0) {
        opacity = 0;
        active = false;
      }
    }

    if (y > goalY) {
      y -= 4;
    } else {
      y = goalY;
      if (y == 80 && !startedTimer) {
        startedTimer = true;
        showTimer.schedule(
            new TimerTask() {
              @Override
              public void run() {
                fade = true;
              }
            },
            nrTextLines * 1500);
      }
    }
  }

  public String justifyLeft(int width, String st) {
    StringBuffer buf = new StringBuffer(st);
    int lastspace = -1;
    int linestart = 0;
    int i = 0;

    nrTextLines = 0;
    while (i < buf.length()) {
      if (buf.charAt(i) == ' ') lastspace = i;
      if (buf.charAt(i) == '\n') {
        lastspace = -1;
        linestart = i + 1;
        nrTextLines++;
      }
      if (i > linestart + width - 1) {
        if (lastspace != -1) {
          buf.setCharAt(lastspace, '\n');
          nrTextLines++;
          linestart = lastspace + 1;
          lastspace = -1;
        } else {
          buf.insert(i, '\n');
          nrTextLines++;
          linestart = i + 1;
        }
      }
      i++;
    }

    nrTextLines++;

    return buf.toString();
  }

  public void moveUp(int changeY) {
    goalY -= changeY;
    if (goalY < 80) {
      goalY = 80;
    }
  }

  public String getText() {
    return text;
  }

  public int getOpacity() {
    return opacity;
  }

  public int getNrTextLines() {
    return nrTextLines;
  }

  public boolean isActive() {
    return active;
  }

  public int getY() {
    return y;
  }

  public Color getBgColor() {
    return bgColor;
  }

  public void draw(Graphics g) {
    g.setColor(new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), opacity));
    g.fillRoundRect(700, y, 300, 40 + nrTextLines * 15, 10);

    g.setFont(Font.size12);
    g.setColor(new Color(255, 255, 255, opacity));
    g.drawString(justifiedText, 720, y + 20);

    if (duplicates > 0) {
      g.drawString("x" + (duplicates + 1), 960, y + 25);
    }
  }

  public void addDuplicate() {
    duplicates++;
  }

  public int getDuplicates() {
    return duplicates;
  }

  public boolean isRequest() {
    return request;
  }

  public void setRequest(boolean request) {
    this.request = request;
  }
}
