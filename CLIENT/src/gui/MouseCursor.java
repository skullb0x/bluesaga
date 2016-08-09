package gui;

import graphics.ImageResource;

import org.newdawn.slick.Animation;
import org.newdawn.slick.Color;
import org.newdawn.slick.Image;

public class MouseCursor {

  private String type;

  private Animation abilityAnimation;
  private Animation pickupAnimation;
  private Animation attackAnimation;
  private Animation talkAnimation;
  private Image pointImage;

  public MouseCursor() {

    type = "Pointer";

    pointImage = ImageResource.getSprite("gui/cursors/cursor_pointer").getImage();

    pickupAnimation = ImageResource.getSprite("gui/cursors/cursor_pickup").getAnimation();

    attackAnimation = ImageResource.getSprite("gui/cursors/cursor_attack").getAnimation();

    abilityAnimation = ImageResource.getSprite("gui/cursors/cursor_ability").getAnimation();

    talkAnimation = ImageResource.getSprite("gui/cursors/cursor_talk").getAnimation();
  }

  public void setType(String newType) {
    type = newType;
  }

  public String getType() {
    return type;
  }

  public void setAoE(String aoe) {}

  public void draw(int mouseX, int mouseY, int alpha) {
    if (type.equals("Pointer")) {
      pointImage.draw(mouseX - 5, mouseY - 5, new Color(255, 255, 255, alpha));
    } else if (type.equals("Attack")) {
      attackAnimation.draw(mouseX - 25, mouseY - 25, new Color(255, 255, 255, alpha));
    } else if (type.equals("Pickup")) {
      pickupAnimation.draw(mouseX - 15, mouseY - 25, new Color(255, 255, 255, alpha));
    } else if (type.equals("Ability")) {
      abilityAnimation.draw(mouseX - 25, mouseY - 25, new Color(255, 255, 255, alpha));
    } else if (type.equals("Talk")) {
      talkAnimation.draw(mouseX - 25, mouseY - 25, new Color(255, 255, 255, alpha));
    }
  }
}
