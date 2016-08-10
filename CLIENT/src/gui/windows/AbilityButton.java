package gui.windows;

import org.newdawn.slick.Graphics;

import abilitysystem.Ability;
import gui.Button;

public class AbilityButton extends Button {

  private Ability MyAbility;

  public AbilityButton(String label, int x, int y, int width, int height, Window parentWindow) {
    super(label, x, y, width, height, parentWindow);
    // TODO Auto-generated constructor stub
  }

  @Override
  public void draw(Graphics g, int mouseX, int mouseY) {
    super.draw(g, mouseX, mouseY);

    int posX = X;
    int posY = Y;

    if (ParentWindow != null) {
      posX = X + ParentWindow.X + ParentWindow.getMoveX();
      posY = Y + ParentWindow.Y + ParentWindow.getMoveY();
    }
    MyAbility.drawIcon(g, posX, posY);
  }

  public Ability getAbility() {
    return MyAbility;
  }

  public void setAbility(Ability theAbility) {
    MyAbility = theAbility;
  }
}
