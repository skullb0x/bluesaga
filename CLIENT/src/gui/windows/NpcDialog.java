
package gui.windows;

import game.BlueSaga;
import graphics.Font;
import graphics.ImageResource;
import gui.Button;
import gui.Gui;
import gui.list.GuiList;
import screens.ScreenHandler;
import screens.ScreenHandler.ScreenType;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;

import utils.LanguageUtils;
import components.Quest;

public class NpcDialog extends Window {

  //private int NpcId;
  private int npcX;
  private int npcY;

  private String NpcName;

  private int MenuState = 0; // 0 = list all quest and shop, 1 - X show page X in questMessage

  private ArrayList<Quest> Quests = new ArrayList<Quest>();

  private int ShopId;
  private int CheckIn;
  private int BountyId;

  private GuiList List;
  private int SelectedIndex;
  private Quest SelectedQuest;

  private Vector<String> QuestMessage = new Vector<String>();

  private Button EndButton;
  private Button OkButton;
  private Button CancelButton;

  private Button LearnPrimaryButton;
  private Button LearnSecondaryButton;
  private Timer bountyWindowTimer = new Timer();

  public NpcDialog(int x, int y, int width, int height) {

    super("NpcDialogW", x, y, width, height, false);

    ShopId = 0;

    OkButton = new Button("ACCEPT", 110, Height - 40, 130, 35, this);
    EndButton = new Button("END CONVERSATION", 150, Height - 40, 220, 35, this);

    LearnPrimaryButton = new Button("LEARN AS PRIMARY", 130, Height - 40, 165, 35, this);
    LearnSecondaryButton = new Button("LEARN AS SECONDARY", 300, Height - 40, 186, 35, this);

    CancelButton = new Button("NO WAY", 250, Height - 40, 130, 35, this);

    setMovable(false);
  }

  public void loadQuests(String questInfo) {

    String npcInfo[] = questInfo.split("/");

    String NpcIdentity[] = npcInfo[0].split(";");
    //NpcId = Integer.parseInt(NpcIdentity[0]);
    npcX = Integer.parseInt(NpcIdentity[1]);
    npcY = Integer.parseInt(NpcIdentity[2]);
    NpcName = NpcIdentity[3];

    String questsinfo = npcInfo[1];

    ShopId = Integer.parseInt(npcInfo[2]);
    CheckIn = Integer.parseInt(npcInfo[3]);
    BountyId = Integer.parseInt(npcInfo[4]);

    int DialogChoices = 0;

    Quests.clear();

    if (!questsinfo.equals("None")) {
      String quests_info[] = questsinfo.split(";");

      for (String qInfo : quests_info) {
        String qInfo_data[] = qInfo.split(",");

        // QuestId, QuestName, QuestStatus
        int qId = Integer.parseInt(qInfo_data[0]);
        String qType = qInfo_data[2];
        int qLevel = Integer.parseInt(qInfo_data[3]);
        int qStatus = Integer.parseInt(qInfo_data[4]);

        Quest newQuest = new Quest();

        newQuest.setId(qId);
        newQuest.setName(LanguageUtils.getString("quests." + qId + ".name"));
        newQuest.setType(qType);
        newQuest.setLevel(qLevel);
        newQuest.setStatus(qStatus);

        Quests.add(newQuest);
        DialogChoices++;
      }
    }

    if (ShopId > 0) {
      DialogChoices++;
    }

    if (CheckIn > 0) {
      DialogChoices++;
    }

    if (BountyId > 0) {
      DialogChoices++;
    }

    int minHeight = 150;
    int newHeight = DialogChoices * 30 + 60;
    if (newHeight < minHeight) {
      newHeight = minHeight;
    }

    Height = newHeight;

    setY(640 - Height - 20);
    //List.setY(Y+20);

    List = new GuiList(X + 20, Y + 20, Width - 40, DialogChoices * 30);

    for (Quest q : Quests) {
      List.addQuest(q, q.getName());
    }

    if (ShopId > 0) {
      List.addShop(LanguageUtils.getString("ui.buttons.buy_sell_items"));
    }

    if (CheckIn > 0) {
      List.addCheckIn(LanguageUtils.getString("ui.buttons.check_in"));
    }

    if (BountyId > 0) {
      List.addBounty(LanguageUtils.getString("ui.bounty.place_bounty"));
    }

    OkButton.setY(Height - 40);
    CancelButton.setY(Height - 40);
    EndButton.setY(Height - 40);

    MenuState = 0;

    SelectedIndex = 0;
    open();
  }

  public void showQuestInfo(int questId, String questType, String questMessage, int questStatus) {
    SelectedQuest = List.getQuestWithId(questId);

    if (questStatus < 2) {
      questMessage = LanguageUtils.getString("quests." + questId + ".quest_message");
    } else {
      questMessage = LanguageUtils.getString("quests." + questId + ".reward_message");
    }

    String fixedMessage = Quest.justifyLeft(50, questMessage);
    SelectedQuest.setMessage(fixedMessage);
    SelectedQuest.setStatus(questStatus);
    SelectedQuest.setType(questType);

    QuestMessage.clear();

    String questMessagePages[] = SelectedQuest.getMessage().split("/");

    for (String page : questMessagePages) {
      QuestMessage.add(page);
    }

    if (questType.equals("Learn Class")) {
      CancelButton =
          new Button(
              LanguageUtils.getString("ui.buttons.no_thanks").toUpperCase(),
              20,
              Height - 40,
              104,
              35,
              this);
    } else if (questStatus == 0
        && !questType.equals("Story")
        && !questType.equals("Instructions")) {
      CancelButton =
          new Button(
              LanguageUtils.getString("ui.buttons.no_way").toUpperCase(),
              250,
              Height - 40,
              130,
              35,
              this);
    } else if (questStatus == 1) {
      CancelButton =
          new Button(
              LanguageUtils.getString("ui.buttons.on_my_way").toUpperCase(),
              160,
              Height - 40,
              200,
              35,
              this);
    } else if (questStatus > 1 || questType.equals("Story") || questType.equals("Instructions")) {
      CancelButton =
          new Button(
              LanguageUtils.getString("ui.buttons.end_conversation").toUpperCase(),
              150,
              Height - 40,
              220,
              35,
              this);
    }

    MenuState = 1;
  }

  /****************************************
   *                                      *
   *         DRAW / GUI		            *
   *                                      *
   *                                      *
   ****************************************/
  @Override
  public void draw(GameContainer app, Graphics g, int mouseX, int mouseY) {
    if (isVisible()) {
      super.draw(app, g, 0, 0);

      if (isFullyOpened()) {
        g.setFont(Font.size18);
        g.setColor(new Color(0, 0, 0, 150));
        g.drawString(NpcName, X + 20, Y - 20);
        g.setColor(Color.white);
        g.drawString(NpcName, X + 18, Y - 22);

        if (MenuState == 0) {
          if (List.getNrListItems() > 0) {
            List.draw(g, mouseX, mouseY, moveX, moveY);
          } else {
            g.setColor(Color.white);
            g.drawString("...", X + 20, Y + 20);
          }
          EndButton.draw(g, mouseX, mouseY);
        } else if (MenuState > 0) {
          g.setColor(Color.white);
          g.setFont(Font.size12);

          g.drawString(QuestMessage.get(MenuState - 1), X + 20, Y + 20);

          if (MenuState < QuestMessage.size()) {
            ImageResource.getSprite("gui/menu/next_message").draw(X + Width - 40, Y + Height - 40);
          } else {
            if (SelectedQuest.getType().equals("Story")
                || SelectedQuest.getType().equals("Instructions")) {
              CancelButton.draw(g, mouseX, mouseY);
            } else if (SelectedQuest.getType().equals("Learn Class")) {
              LearnPrimaryButton.draw(g, mouseX, mouseY);
              LearnSecondaryButton.draw(g, mouseX, mouseY);
              CancelButton.draw(g, mouseX, mouseY);
            } else {

              if (SelectedQuest.getStatus() == 0) {
                OkButton.draw(g, mouseX, mouseY);
                CancelButton.draw(g, mouseX, mouseY);
              } else if (SelectedQuest.getStatus() > 0) {
                CancelButton.draw(g, mouseX, mouseY);
              }
            }
          }
        }

        //BP_CLIENT.GFX.getSprite("npc/npc_boob").draw(X - 130, Y - 50);

      }
    }
  }

  @Override
  public void keyLogic(Input INPUT) {

    if (INPUT.isKeyPressed(Input.KEY_ESCAPE)) {
      goBack();
    }
  }

  @Override
  public void leftMouseClick(Input INPUT) {
    super.leftMouseClick(INPUT);

    int mouseX = INPUT.getAbsoluteMouseX();
    int mouseY = INPUT.getAbsoluteMouseY();

    if (isVisible() && !BlueSaga.actionServerWait) {
      if (MenuState == 0 && EndButton.isClicked(mouseX, mouseY)) {
        close();
      } else if (clickedOn(mouseX, mouseY)) {
        if (MenuState == 0) {
          // CHECK IF SELECTED ANY QUESTS OR SHOP
          int selectedIndex = List.select(mouseX, mouseY, moveX, moveY);

          if (selectedIndex < 9999) {
            if (List.getListItem(selectedIndex).getQuest() != null) {
              int questId = List.getListItem(selectedIndex).getQuest().getId();

              SelectedIndex = selectedIndex;
              BlueSaga.client.sendMessage("quest", "info;" + questId);
            } else if (List.getListItem(selectedIndex).isShopLink()) {
              // SHOP
              BlueSaga.client.sendMessage("shop", ShopId + "");
              close();
            } else if (List.getListItem(selectedIndex).isCheckLink()) {
              // CHECK IN
              BlueSaga.client.sendMessage("checkin", CheckIn + "");
              close();
            } else if (List.getListItem(selectedIndex).isBountyLink()) {
              // BOUNTY
              // TIMER!!!
              bountyWindowTimer.schedule(
                  new TimerTask() {
                    @Override
                    public void run() {
                      close();
                      Gui.BountyWindow.open();
                      ScreenHandler.setActiveScreen(ScreenType.WORLD);
                    }
                  },
                  100);
            }
          }
        } else {

          if (mouseX > X && mouseX < X + Width && mouseY > Y && mouseY < Y + Height) {
            if (SelectedIndex < 9999 && MenuState < QuestMessage.size()) {
              MenuState++;
            }
          }

          if (MenuState == QuestMessage.size()) {

            boolean reOpen = false;

            if (SelectedQuest != null) {
              if (SelectedQuest.getType().equals("Learn Class")) {
                if (LearnPrimaryButton.isClicked(mouseX, mouseY)) {
                  BlueSaga.client.sendMessage("learn_class", SelectedQuest.getId() + ",1");
                } else if (LearnSecondaryButton.isClicked(mouseX, mouseY)) {
                  BlueSaga.client.sendMessage("learn_class", SelectedQuest.getId() + ",2");
                } else if (CancelButton.isClicked(mouseX, mouseY)) {
                  close();
                  reOpen = true;
                }
              } else {
                // CHECK IF CLICKED ACCEPT / CANCEL BUTTON
                if (OkButton.isClicked(mouseX, mouseY)) {

                  if (SelectedQuest.getStatus() == 0) {

                    // SEND ACCEPT QUEST
                    BlueSaga.client.sendMessage("quest", "add;" + SelectedQuest.getId());
                    close();
                  }
                  reOpen = true;
                } else if (CancelButton.isClicked(mouseX, mouseY)) {

                  close();
                  reOpen = true;
                }
              }
            }
            if (reOpen) {
              BlueSaga.client.sendMessage("talknpc", npcX + ";" + npcY);
            }
          }
        }
      } else {
        close();
      }
    }
  }

  public void backToStart() {
    MenuState = 0;
  }

  private void goBack() {
    if (MenuState > 0) {
      MenuState--;
    } else {
      ScreenHandler.setActiveScreen(ScreenType.WORLD);
      close();
    }
  }

  @Override
  public void open() {
    super.open();
  }

  /****************************************
   *                                      *
   *         GETTER / SETTER	            *
   *                                      *
   *                                      *
   ****************************************/

  /*
  public void setNpcId(int newNpcId){
  	NpcId = newNpcId;
  }
   */

  public int getMenuState() {
    return MenuState;
  }
}
