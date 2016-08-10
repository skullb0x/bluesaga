package gui.dragndrop;

import org.newdawn.slick.Graphics;

import abilitysystem.Ability;
import graphics.ImageResource;
import components.Item;

public class DragObject {

  private Item myItem;
  private Ability myAbility;

  public DragObject() {
    myItem = null;
    myAbility = null;
  }

  public Item getItem() {
    return myItem;
  }

  public void setItem(Item newItem) {
    myItem = newItem;
  }

  public Ability getAbility() {
    return myAbility;
  }

  public void setAbility(Ability newAbility) {
    myAbility = newAbility;
  }

  public void draw(Graphics g, int mouseX, int mouseY) {
    if (myAbility != null) {
      myAbility.drawIcon(g, mouseX - 25, mouseY - 25);
    } else if (myItem != null) {
      if (myItem.getGraphicsNr() > 0) {
        ImageResource.getSprite("items/skins/" + myItem.getGraphicsNr())
            .getImage()
            .drawCentered(mouseX, mouseY);
      } else {
        ImageResource.getSprite("items/item" + myItem.getId())
            .getImage()
            .drawCentered(mouseX, mouseY);
      }
    }
  }

  public void clear() {
    myItem = null;
    myAbility = null;
  }

  public boolean isEmpty() {
    if (myItem == null && myAbility == null) {
      return true;
    }
    return false;
  }
}
