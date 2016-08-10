package gui;

import game.BP_EDITOR;

import org.newdawn.slick.Animation;
import org.newdawn.slick.Image;

public class MouseCursor {

  private String type;

  private Animation abilityAnimation;
  private Animation pickupAnimation;
  private Animation attackAnimation;
  private Image pointImage;

  public MouseCursor() {

    type = "Pointer";

    pointImage = BP_EDITOR.GFX.getSprite("gui/cursors/cursor_pointer").getImage();

    pickupAnimation = BP_EDITOR.GFX.getSprite("gui/cursors/cursor_pickup").getAnimation();

    attackAnimation = BP_EDITOR.GFX.getSprite("gui/cursors/cursor_attack").getAnimation();

    abilityAnimation = BP_EDITOR.GFX.getSprite("gui/cursors/cursor_ability").getAnimation();
  }

  public void setType(String newType) {
    type = newType;
  }

  public String getType() {
    return type;
  }

  public void draw(int mouseX, int mouseY) {
    if (type.equals("Pointer")) {
      pointImage.draw(mouseX - 10, mouseY - 10);
    } else if (type.equals("Attack")) {
      attackAnimation.draw(mouseX - 25, mouseY - 25);
    } else if (type.equals("Pickup")) {
      pickupAnimation.draw(mouseX - 25, mouseY - 25);
    } else if (type.equals("Ability")) {
      abilityAnimation.draw(mouseX - 25, mouseY - 25);
    }
  }
}
