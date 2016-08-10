package gui;

import java.util.Timer;
import java.util.TimerTask;

public class Notification {

  private int opacity;
  private Timer showTimer;
  private String text;

  private boolean active;
  private boolean fade;
  private boolean startedTimer;

  private int nrTextLines;

  private int goalY;
  private int y;

  public Notification(String newText, int newY) {
    text = newText;
    text = justifyLeft(30, text);

    goalY = newY;
    y = newY;

    active = true;
    fade = false;
    startedTimer = false;

    opacity = 200;

    showTimer = new Timer();

    if (y == 20) {
      startedTimer = true;
      showTimer.schedule(
          new TimerTask() {
            public void run() {
              fade = true;
            }
          },
          nrTextLines * 1500);
    }
  }

  public void update() {
    if (fade && y == 20) {
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
      if (y == 20 && !startedTimer) {
        startedTimer = true;
        showTimer.schedule(
            new TimerTask() {
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
    if (goalY < 20) {
      goalY = 20;
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
}
