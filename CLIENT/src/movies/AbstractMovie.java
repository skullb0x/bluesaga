package movies;

import game.BlueSaga;
import game.ClientSettings;
import graphics.BlueSagaColors;
import graphics.Font;
import gui.Gui;
import screens.ScreenHandler;
import screens.ScreenHandler.ScreenType;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import utils.LanguageUtils;

public abstract class AbstractMovie {

  private int x = 212;
  private int y = 160;

  private int width = 600;
  private int height = 320;

  private int timeItr = 0;

  private int duration = 0;

  protected int fadeAlpha = 0;

  private boolean active;

  protected boolean skipped = false;

  private String endMessage = "";

  protected boolean canSkip = true;

  public void play() {
    ScreenHandler.setActiveScreen(ScreenType.CUT_SCENE);
    BlueSaga.BG_MUSIC.stop();
    Gui.stopMoveWindows();
    timeItr = 0;
    setActive(true);
  }

  public void replay() {
    play();
  }

  public void update() {
    if (timeItr < duration && fadeAlpha < 255) {
      timeItr++;
    } else {
      setActive(false);
      if (!getEndMessage().equals("")) {
        Gui.addMessage(getEndMessage(), BlueSagaColors.RED);
      }
    }
  }

  public void skipMovie() {
    if (canSkip) {
      skipped = true;
      endMovie();
    }
  }

  public void endMovie() {
    if (fadeAlpha == 0) {
      fadeAlpha = 1;
    }
  }

  public void draw(Graphics g) {
    g.setColor(new Color(0, 0, 0));
    g.fillRect(0, 0, ClientSettings.SCREEN_WIDTH, ClientSettings.SCREEN_HEIGHT);

    g.setFont(Font.size16);
    g.setColor(new Color(255, 255, 255));
    if (canSkip) {
      g.drawString(LanguageUtils.getString("movies.press_escape"), 790, 605);
    } else {
      g.drawString(LanguageUtils.getString("movies.server_restart"), 730, 605);
    }
  }

  /**
   * Getters and Setters
   * @return
   */
  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public int getX() {
    return x;
  }

  public void setX(int x) {
    this.x = x;
  }

  public int getY() {
    return y;
  }

  public void setY(int y) {
    this.y = y;
  }

  public int getDuration() {
    return duration;
  }

  public void setDuration(int duration) {
    this.duration = duration;
  }

  public int getTimeItr() {
    return timeItr;
  }

  public void setTimeItr(int timeItr) {
    this.timeItr = timeItr;
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public String getEndMessage() {
    return endMessage;
  }

  public void setEndMessage(String endMessage) {
    this.endMessage = endMessage;
  }
}
