package gui.windows;

import game.BlueSaga;
import graphics.BlueSagaColors;
import graphics.Font;
import graphics.ImageResource;
import gui.Button;
import gui.list.GuiList;

import java.util.ArrayList;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;

import utils.LanguageUtils;
import components.Quest;

public class QuestWindow extends Window {

  private GuiList List;
  private ArrayList<Quest> Quests = new ArrayList<Quest>();

  private String State = "List";

  private Button BackButton;
  private int selectedQuest;

  public QuestWindow(int x, int y, int width, int height) {
    super("QuestW", x, y, width, height, true);
    // TODO Auto-generated constructor stub
    BackButton = new Button("Back", 20, 345, 100, 35, this);
    selectedQuest = 0;
    State = "List";
    List = new GuiList(X + 20, Y + 50, Width - 40, Height - 50);
  }

  public void load(String questInfo) {

    State = "List";

    List = new GuiList(X + 20, Y + 50, Width - 40, Height - 50);
    Quests.clear();

    if (!questInfo.equals("None")) {
      String questsinfo[] = questInfo.split(";");

      for (String qInfo : questsinfo) {
        String qInfo_data[] = qInfo.split(",");

        // QuestId, QuestName, QuestStatus

        int qId = Integer.parseInt(qInfo_data[0]);
        String qName = qInfo_data[1];
        String qType = qInfo_data[2];
        int qStatus = Integer.parseInt(qInfo_data[3]);
        int qLevel = Integer.parseInt(qInfo_data[4]);

        Quest newQuest = new Quest();

        newQuest.setId(qId);
        newQuest.setName(qName);
        newQuest.setType(qType);
        newQuest.setStatus(qStatus);
        newQuest.setLevel(qLevel);

        Quests.add(newQuest);
        List.addQuest(newQuest, qName);
      }
    }
  }

  @Override
  public void draw(GameContainer app, Graphics g, int mouseX, int mouseY) {
    if (isVisible()) {
      super.draw(app, g, mouseX, mouseY);

      if (isFullyOpened()) {
        g.setFont(Font.size12);

        if (State.equals("List")) {
          ImageResource.getSprite("gui/menu/quests_label").draw(X + 20 + moveX, Y + 20 + moveY);
          List.draw(g, mouseX, mouseY, moveX, moveY);
        } else if (State.equals("Loading")) {
          g.drawString(
              LanguageUtils.getString("ui.status.loading"), X + 20 + moveX, Y + 20 + moveY);
        } else if (State.equals("ShowQuest")) {
          g.setColor(new Color(255, 255, 255, 255));
          g.drawString(Quests.get(selectedQuest).getName(), X + 20 + moveX, Y + 20 + moveY);

          g.drawString(
              "("
                  + LanguageUtils.getString("ui.quest.rec_level")
                  + ": "
                  + Quests.get(selectedQuest).getLevel()
                  + "+ )",
              X + 20 + moveX,
              Y + 50 + moveY);

          if (Quests.get(selectedQuest).getStatus() == 1) {
            g.setColor(BlueSagaColors.WHITE);
            g.drawString(
                LanguageUtils.getString("ui.quest.status_accepted"),
                X + 20 + moveX,
                Y + 80 + moveY);
          } else if (Quests.get(selectedQuest).getStatus() == 2) {
            g.setColor(BlueSagaColors.YELLOW);
            g.drawString(
                LanguageUtils.getString("ui.quest.status_get_reward"),
                X + 20 + moveX,
                Y + 80 + moveY);
          }
          g.setColor(BlueSagaColors.WHITE);
          g.drawString(Quests.get(selectedQuest).getDescription(), X + 20 + moveX, Y + 120 + moveY);
          BackButton.draw(g, mouseX, mouseY);
        }
      }
    }
  }

  @Override
  public void keyLogic(Input INPUT) {
    if (INPUT.isKeyPressed(Input.KEY_Q) && !BlueSaga.GUI.Chat_Window.isActive()) {
      if (!isOpen()) {
        BlueSaga.client.sendMessage("myquests", "info");
      }
      toggle();
    }
  }

  @Override
  public void leftMouseClick(Input INPUT) {
    super.leftMouseClick(INPUT);

    int mouseX = INPUT.getAbsoluteMouseX();
    int mouseY = INPUT.getAbsoluteMouseY();

    if (isFullyOpened()) {

      int listIndex = List.select(mouseX, mouseY, moveX, moveY);

      if (listIndex < 9999) {
        int questId = List.getListItem(listIndex).getQuest().getId();
        BlueSaga.client.sendMessage("questdescr", "" + questId);
        selectedQuest = listIndex;
        State = "Laoding";
      }

      if (BackButton.isClicked(mouseX, mouseY)) {
        State = "List";
      }
    }
  }

  @Override
  public void toggle() {
    if (!isOpen()) {
      //BP_CLIENT.client.sendMessage("myquests", "info");
    }
    super.toggle();
  }

  public void showQuest(String questText) {
    Quests.get(selectedQuest)
        .setDescription(
            LanguageUtils.getString(
                "quests." + Quests.get(selectedQuest).getId() + ".description"));
    State = "ShowQuest";
  }

  @Override
  public void stopMove() {
    if (List != null) {
      List.updatePos(moveX, moveY);
    }
    super.stopMove();
  }
}
