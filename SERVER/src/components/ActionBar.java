package components;

import java.util.Vector;

import data_handlers.ability_handler.Ability;
import data_handlers.item_handler.Item;

public class ActionBar {

  private Vector<ActionBarButton> Buttons;

  public ActionBar() {

    Buttons = new Vector<ActionBarButton>();

    for (int i = 0; i < 10; i++) {
      Buttons.add(new ActionBarButton());
    }
  }

  public void setAbility(int index, Ability Ability) {
    Buttons.get(index).setAbility(Ability);
  }

  public void setItem(int index, Item newItem) {
    Buttons.get(index).setItem(newItem);
  }

  public ActionBarButton getAction(int index) {
    return Buttons.get(index);
  }
}
