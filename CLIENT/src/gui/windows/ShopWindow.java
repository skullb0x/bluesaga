package gui.windows;

import game.BlueSaga;
import graphics.Font;
import gui.Gui;
import gui.dragndrop.DropBox;
import screens.ScreenHandler;
import screens.ScreenHandler.ScreenType;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;

import abilitysystem.Ability;
import components.Item;

public class ShopWindow extends Window {

  private String ShopName;

  private int nrBoxW = 6;
  private int nrBoxH = 6;

  private DropBox[][] Boxes = new DropBox[nrBoxW][nrBoxH];

  private ItemInfoBox SelectedInfoBox;
  private Item selectedItemForInfo;
  private Ability selectedAbilityForInfo;

  public ShopWindow(int x, int y, int width, int height) {
    super("ShopW", x, y, width, height, true);
    // TODO Auto-generated constructor stub

    for (int j = 0; j < nrBoxH; j++) {
      for (int i = 0; i < nrBoxW; i++) {
        Boxes[i][j] = new DropBox(X + 10 + i * 50, Y + 40 + j * 50);
      }
    }

    SelectedInfoBox = new ItemInfoBox(0, 0, 1, 1);
    selectedItemForInfo = null;
  }

  public void load(String shopInfo) {
    String shop_info[] = shopInfo.split(";");

    for (int j = 0; j < nrBoxH; j++) {
      for (int i = 0; i < nrBoxW; i++) {
        Boxes[i][j] = new DropBox(X + 10 + i * 50, Y + 40 + j * 50);
        Boxes[i][j].setType("Shop");
      }
    }

    ShopName = shop_info[0] + "'s Shop";

    int boxX = 0;
    int boxY = 0;

    if (!shop_info[1].equals("None")) {
      String items[] = shop_info[1].split(",");
      for (String itemInfo : items) {
        int itemId = Integer.parseInt(itemInfo);
        Item shopItem = new Item(itemId);
        Boxes[boxX][boxY].setItem(shopItem);
        boxX++;
        if (boxX == nrBoxW) {
          boxX = 0;
          boxY++;
        }
      }
    }
    if (!shop_info[2].equals("None")) {
      String abilities[] = shop_info[2].split(":");
      for (String abilityInfo : abilities) {
        String abilityIdAndColor[] = abilityInfo.split(",");
        int abilityId = Integer.parseInt(abilityIdAndColor[0]);
        Ability shopAbility = new Ability(abilityId);
        int colorR = Integer.parseInt(abilityIdAndColor[1]);
        int colorG = Integer.parseInt(abilityIdAndColor[2]);
        int colorB = Integer.parseInt(abilityIdAndColor[3]);
        int graphicsNr = Integer.parseInt(abilityIdAndColor[4]);

        shopAbility.setColor(new Color(colorR, colorG, colorB));
        shopAbility.setBgColor(new Color(colorR, colorG, colorB));
        shopAbility.setCooldown(0);
        shopAbility.setCooldownLeft(0);
        shopAbility.setGraphicsNr(graphicsNr);
        Boxes[boxX][boxY].setAbility(shopAbility);
        boxX++;
        if (boxX == nrBoxW) {
          boxX = 0;
          boxY++;
        }
      }
    }

    ScreenHandler.setActiveScreen(ScreenType.WORLD);
    open();
    Gui.InventoryWindow.open();
  }

  @Override
  public void leftMouseClick(Input INPUT) {
    super.leftMouseClick(INPUT);

    int mouseX = INPUT.getAbsoluteMouseX();
    int mouseY = INPUT.getAbsoluteMouseY();

    if (isVisible() && !BlueSaga.actionServerWait) {
      if (clickedOn(mouseX, mouseY)) {
        if (Gui.MouseItem.getItem() != null) {
          // SELL ITEM
          BlueSaga.client.sendMessage(
              "sell", "mouse," + Gui.MouseItem.getItem().getUserItemId() + "");
          BlueSaga.actionServerWait = true;
        }
      }
    }
  }

  @Override
  public void rightMouseClick(Input INPUT) {
    super.rightMouseClick(INPUT);

    int mouseX = INPUT.getAbsoluteMouseX();
    int mouseY = INPUT.getAbsoluteMouseY();

    if (isVisible() && !BlueSaga.actionServerWait) {

      boolean clickedBox = false;

      // CHECK WHICH BOX IS CLICKED
      for (int i = 0; i < nrBoxW; i++) {
        for (int j = 0; j < nrBoxH; j++) {

          // IF BOX IS CLICKED, CHECK IF PICKUP OR DROP
          if (Boxes[i][j].isSelected(mouseX, mouseY, moveX, moveY)) {
            clickedBox = true;

            // BUY ITEM OR ABILITY
            if (Boxes[i][j].getItem() != null) {
              BlueSaga.client.sendMessage(
                  "buy", Boxes[i][j].getContentType() + ";" + Boxes[i][j].getItem().getId());
              BlueSaga.actionServerWait = true;
            } else if (Boxes[i][j].getAbility() != null) {
              BlueSaga.client.sendMessage(
                  "buy",
                  Boxes[i][j].getContentType() + ";" + Boxes[i][j].getAbility().getAbilityId());
              BlueSaga.actionServerWait = true;
            }
            break;
          }
        }
        if (clickedBox) {
          break;
        }
      }
    }
  }

  @Override
  public void draw(GameContainer app, Graphics g, int mouseX, int mouseY) {
    if (isVisible()) {
      super.draw(app, g, mouseX, mouseY);

      g.setFont(Font.size12);
      g.setColor(new Color(255, 255, 255, 255));
      g.drawString(ShopName, X + 20 + moveX, Y + 15 + moveY);

      boolean mouseOnItem = false;

      for (int j = 0; j < nrBoxH; j++) {
        for (int i = 0; i < nrBoxW; i++) {
          Boxes[i][j].draw(g, mouseX, mouseY, moveX, moveY);

          //if(Gui.isWindowOnTop(getDepthZ())){
          if (Boxes[i][j].isSelected(mouseX, mouseY, moveX, moveY)) {

            if (!Boxes[i][j].isEmpty()) {
              mouseOnItem = true;

              boolean showInfo = false;

              if (Boxes[i][j].getItem() != null) {
                if (selectedItemForInfo != null) {
                  if (selectedItemForInfo.getId() != Boxes[i][j].getItem().getId()) {
                    showInfo = true;
                  }
                } else {
                  showInfo = true;
                }
                if (showInfo) {
                  selectedAbilityForInfo = null;
                  selectedItemForInfo = Boxes[i][j].getItem();
                  int infoBoxX = X + (i + 1) * 50 + moveX;
                  if (infoBoxX > 800) {
                    infoBoxX -= 200;
                  }

                  SelectedInfoBox = new ItemInfoBox(infoBoxX, Y + j * 50 + moveY, 1, 1);
                  BlueSaga.client.sendMessage("item_info", "shop;" + selectedItemForInfo.getId());
                }
              } else if (Boxes[i][j].getAbility() != null) {
                if (selectedAbilityForInfo != null) {
                  if (selectedAbilityForInfo.getAbilityId()
                      != Boxes[i][j].getAbility().getAbilityId()) {
                    showInfo = true;
                  }
                } else {
                  showInfo = true;
                }
                if (showInfo) {
                  selectedItemForInfo = null;
                  selectedAbilityForInfo = Boxes[i][j].getAbility();
                  int infoBoxX = X + (i + 1) * 50 + moveX;
                  if (infoBoxX > 800) {
                    infoBoxX -= 200;
                  }

                  SelectedInfoBox = new ItemInfoBox(infoBoxX, Y + j * 50 + moveY, 1, 1);
                  BlueSaga.client.sendMessage(
                      "ability_info", "shop;" + selectedAbilityForInfo.getAbilityId());
                }
              }

            } else {
              selectedAbilityForInfo = null;
              selectedItemForInfo = null;
              SelectedInfoBox.close();
            }
          }
          //}
        }
      }

      if (!mouseOnItem) {
        selectedItemForInfo = null;
        SelectedInfoBox.close();
      }
      SelectedInfoBox.draw(app, g, 0, 0);
    }
  }

  @Override
  public void keyLogic(Input INPUT) {
    if (isFullyOpened()) {
      if (INPUT.isKeyPressed(Input.KEY_ESCAPE)) {
        close();
      }
    }
  }

  public void showInfoBox(String info) {
    SelectedInfoBox.load(info);
  }

  @Override
  public void stopMove() {
    if (moveWithMouse) {
      for (int i = 0; i < nrBoxW; i++) {
        for (int j = 0; j < nrBoxH; j++) {
          Boxes[i][j].updatePos(moveX, moveY);
        }
      }
    }
    super.stopMove();
  }
}
