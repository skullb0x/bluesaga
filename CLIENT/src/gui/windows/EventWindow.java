package gui.windows;

import java.util.Vector;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;

import components.Quest;
import game.BlueSaga;
import graphics.Font;
import graphics.ImageResource;
import gui.Gui;

public class EventWindow extends Window {

  private String eventImage;
  private Vector<String> EventMessage = new Vector<String>();
  private int pageNr = 0;

  private static int Width = 500;
  private static int Height = 140;

  private static int X = 1024 / 2 - Width / 2;
  private static int Y = 150;

  public EventWindow() {
    super("EventW", X, Y, Width, Height, false);
  }

  public void load(String eventInfo) {
    String event_info[] = eventInfo.split(";");

    eventImage = event_info[0];
    String eventMessagePages[] = event_info[1].split("/");

    EventMessage.clear();
    pageNr = 0;
    for (String page : eventMessagePages) {
      EventMessage.add(Quest.justifyLeft(45, page));
    }
    Gui.closeAllWindows();
    open();
  }

  @Override
  public void draw(GameContainer app, Graphics g, int mouseX, int mouseY) {
    if (isVisible()) {
      super.draw(app, g, mouseX, mouseY);

      if (isFullyOpened()) {
        int textX = 20;
        if (!eventImage.equals("None")) {
          ImageResource.getSprite("events/" + eventImage).draw(X + 20, Y + 20);
          textX += 120;
        }

        g.setColor(Color.white);
        g.setFont(Font.size12);

        g.drawString(EventMessage.get(pageNr), X + textX, Y + 20);

        if (pageNr < EventMessage.size() - 1) {
          ImageResource.getSprite("gui/menu/next_message").draw(X + Width - 40, Y + Height - 40);
        }
      }
    }
  }

  @Override
  public void leftMouseClick(Input INPUT) {
    super.leftMouseClick(INPUT);

    int mouseX = INPUT.getAbsoluteMouseX();
    int mouseY = INPUT.getAbsoluteMouseY();

    if (isVisible() && !BlueSaga.actionServerWait) {

      if (mouseX > X && mouseX < X + Width && mouseY > Y && mouseY < Y + Height) {
        if (pageNr < EventMessage.size() - 1) {
          pageNr++;
        } else {
          close();
        }
      }
    }
  }
}
