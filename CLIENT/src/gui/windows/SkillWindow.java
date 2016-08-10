package gui.windows;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;

import player_classes.BaseClass;
import abilitysystem.Skill;
import sound.Sfx;
import game.BlueSaga;
import graphics.BlueSagaColors;
import graphics.Font;
import graphics.ImageResource;
import gui.Button;

public class SkillWindow extends Window {

  private String SkillType = "Combat";

  private Button CombatButton;
  private Button JobButton;

  public SkillWindow(int x, int y, int width, int height) {
    super("SkillW", x, y, width, height, true);
    // TODO Auto-generated constructor stub

    CombatButton = new Button("Combat", 85, 12, 90, 35, this);
    JobButton = new Button("Job", 185, 12, 90, 35, this);
  }

  @Override
  public void draw(GameContainer app, Graphics g, int mouseX, int mouseY) {
    if (isVisible()) {
      super.draw(app, g, mouseX, mouseY);

      if (isFullyOpened()) {
        g.setColor(BlueSagaColors.WHITE);
        g.setFont(Font.size10);

        CombatButton.draw(g, mouseX, mouseY);
        JobButton.draw(g, mouseX, mouseY);

        ImageResource.getSprite("gui/menu/skills_label").draw(X + 20 + moveX, Y + 20 + moveY);

        if (SkillType.equals("Job")) {
          g.setFont(Font.size10);
          g.setColor(BlueSagaColors.WHITE);

          int i = 0;
          for (Skill s : BlueSaga.playerCharacter.getSkills().values()) {
            if (s != null) {
              ImageResource.getSprite("skills/icon" + s.getId())
                  .draw(X + 20 + moveX, Y + 65 + moveY + (i) * 30);
              ImageResource.getSprite("gui/world/meter_bg")
                  .draw(X + 50 + moveX, Y + 70 + moveY + (i) * 30);

              g.setWorldClip(X + 50 + moveX, Y + 70 + moveY + (i) * 30, s.getSPBarWidth(107), 20);
              ImageResource.getSprite("gui/world/xp_small_meter")
                  .draw(X + 55 + moveX, Y + 74 + moveY + (i) * 30);
              g.clearWorldClip();

              g.drawString(
                  s.getName() + " - Lvl " + s.getLevel(),
                  X + 180 + moveX,
                  Y + 73 + moveY + (i) * 30);
              i++;
            }
          }
        } else {
          g.setFont(Font.size10);
          g.setColor(BlueSagaColors.WHITE);

          int i = 0;
          for (BaseClass playerClass : BlueSaga.playerCharacter.getPlayerClasses().values()) {
            if (playerClass.available) {
              ImageResource.getSprite("classes/icon" + playerClass.id)
                  .draw(X + 20 + moveX, Y + 65 + moveY + (i) * 30);
              ImageResource.getSprite("gui/world/meter_bg")
                  .draw(X + 50 + moveX, Y + 70 + moveY + (i) * 30);

              g.setWorldClip(
                  X + 50 + moveX, Y + 70 + moveY + (i) * 30, playerClass.getXPBarWidth(107), 20);
              if (playerClass.id < 4) {
                ImageResource.getSprite("gui/world/skill_meter")
                    .draw(X + 55 + moveX, Y + 74 + moveY + (i) * 30);
              } else {
                ImageResource.getSprite("gui/world/xp_small_meter")
                    .draw(X + 55 + moveX, Y + 74 + moveY + (i) * 30);
              }
              g.clearWorldClip();

              g.drawString(
                  playerClass.name + " - Lvl " + playerClass.level,
                  X + 180 + moveX,
                  Y + 73 + moveY + (i) * 30);
              i++;
            }
          }
        }
      }
    }
  }

  @Override
  public void leftMouseClick(Input INPUT) {
    super.leftMouseClick(INPUT);

    int mouseX = INPUT.getAbsoluteMouseX();
    int mouseY = INPUT.getAbsoluteMouseY();

    // CHECK WHICH BOX IS CLICKED

    if (isVisible()) {
      if (CombatButton.isClicked(mouseX, mouseY)) {
        SkillType = "Combat";
        Sfx.play("gui/menu_confirm");
      } else if (JobButton.isClicked(mouseX, mouseY)) {
        SkillType = "Job";
        Sfx.play("gui/menu_confirm");
      }
    }
  }

  @Override
  public void keyLogic(Input INPUT) {

    if (INPUT.isKeyPressed(Input.KEY_K) && !BlueSaga.GUI.Chat_Window.isActive()) {
      toggle();
    }
  }

  public String getSkillType() {
    return SkillType;
  }

  public void setSkillType(String skillType) {
    SkillType = skillType;
  }
}
