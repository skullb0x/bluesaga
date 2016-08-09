package gui.windows;

import game.BlueSaga;
import graphics.BlueSagaColors;
import graphics.Font;
import gui.list.GuiList;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;

import utils.LanguageUtils;
import creature.Creature;

public class CharacterSkinWindow extends Window {
  private GuiList List;

  public CharacterSkinWindow(int x, int y, int width, int height) {
    super("Character skins", x, y, width, height, true);
    // TODO Auto-generated constructor stub

    super.setMovable(true);
    List = new GuiList(X + 20, Y + 50, Width - 40, Height - 50);
    List.setButtonHeight(60);
  }

  public void load(String info) {
    List.reset();

    boolean loadSuccess = true;

    if (!info.equals("")) {
      String skins[] = info.split(";");

      for (String skin : skins) {
        String skinInfo[] = skin.split(",");
        try {
          int skinId = Integer.parseInt(skinInfo[0]);
          String skinName = skinInfo[1];
          Creature charSkin = new Creature(0, 0, 0);
          charSkin.setType(skinId);

          List.addCreature(charSkin, skinName);
        } catch (NumberFormatException e) {
          loadSuccess = false;
        }
      }
    }

    if (List.getNrListItems() > 0) {
      List.addButton("Show my natural look");
    }

    if (loadSuccess) {
      open();
    }
  }

  @Override
  public void draw(GameContainer app, Graphics g, int mouseX, int mouseY) {
    if (isVisible()) {
      super.draw(app, g, mouseX, mouseY);

      if (isFullyOpened()) {
        g.setFont(Font.size12);
        g.setColor(BlueSagaColors.YELLOW);
        g.drawString(getName(), X + 20 + moveX, Y + 20 + moveY);

        g.setColor(BlueSagaColors.WHITE);
        if (List.getNrListItems() > 0) {
          List.draw(g, mouseX, mouseY, moveX, moveY);
        } else {
          g.drawString(
              LanguageUtils.getString("ui.skins.no_character_skins"),
              X + 20 + moveX,
              Y + 50 + moveY);
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
        if (List.getListItem(listIndex).getCreature() != null) {
          int skinId = List.getListItem(listIndex).getCreature().getCreatureId();
          BlueSaga.client.sendMessage("change_character_skin", "" + skinId);
        } else {
          BlueSaga.client.sendMessage("reset_character_skin", "reset");
        }
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
