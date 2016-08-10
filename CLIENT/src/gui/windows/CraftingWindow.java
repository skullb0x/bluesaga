package gui.windows;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;

import utils.LanguageUtils;
import components.Item;
import game.BlueSaga;
import graphics.BlueSagaColors;
import graphics.Font;
import gui.list.GuiList;

public class CraftingWindow extends Window {

  private String name;
  private GuiList List;

  public CraftingWindow(int x, int y, int width, int height) {
    super("Crafting", x, y, width, height, true);

    super.setMovable(true);
    List = new GuiList(X + 20, Y + 50, Width - 40, Height - 50);
  }

  public void load(String info) {
    List.reset();

    String craftingInfo[] = info.split("/");
    name = craftingInfo[0];

    if (craftingInfo.length > 1) {
      String recipesInfo[] = craftingInfo[1].split(";");

      for (String recipeInfo : recipesInfo) {
        String recipeIdName[] = recipeInfo.split(",");
        int itemId = Integer.parseInt(recipeIdName[0]);
        String itemName = recipeIdName[1];
        List.addItem(new Item(itemId), itemName);
      }
    }
    open();
  }

  @Override
  public void draw(GameContainer app, Graphics g, int mouseX, int mouseY) {
    if (isVisible()) {
      super.draw(app, g, mouseX, mouseY);

      if (isFullyOpened()) {
        g.setFont(Font.size12);
        g.setColor(BlueSagaColors.YELLOW);
        g.drawString(name, X + 20 + moveX, Y + 20 + moveY);

        g.setColor(BlueSagaColors.WHITE);
        if (List.getNrListItems() > 0) {
          List.draw(g, mouseX, mouseY, moveX, moveY);
        } else {
          g.drawString(
              LanguageUtils.getString("ui.crafting.no_recipes"), X + 20 + moveX, Y + 50 + moveY);
        }
      }
    }
  }

  public void leftMouseClick(Input INPUT) {
    super.leftMouseClick(INPUT);

    int mouseX = INPUT.getAbsoluteMouseX();
    int mouseY = INPUT.getAbsoluteMouseY();

    if (isFullyOpened() && !BlueSaga.actionServerWait) {

      int listIndex = List.select(mouseX, mouseY, moveX, moveY);

      if (listIndex < 9999) {
        int itemId = List.getListItem(listIndex).getItem().getId();
        BlueSaga.client.sendMessage("craftitem", "" + itemId);
        BlueSaga.actionServerWait = true;
      }
    }
  }

  @Override
  public void stopMove() {
    if (List != null) {
      List.updatePos(moveX, moveY);
    }
    super.stopMove();
  }
}
