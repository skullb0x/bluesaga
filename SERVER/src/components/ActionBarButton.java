package components;

import data_handlers.ability_handler.Ability;
import data_handlers.item_handler.Item;

public class ActionBarButton {

  private String ActionType; // Item or Ability

  private Ability Ability;
  private Item Item;

  public ActionBarButton() {
    ActionType = "None";
    Ability = null;
    Item = null;
  }

  public void setAbility(Ability newAbility) {
    ActionType = "Ability";
    Ability = newAbility;
  }

  public void setItem(Item newItem) {
    ActionType = "Item";
    Item = newItem;
  }

  public int getAPwidth(int height) {
    // TODO
    if (ActionType.equals("Ability") && Ability != null) {
      //	return Ability.getAPwidth(height);
    }
    return 0;
  }

  public String getActionType() {
    return ActionType;
  }

  public int getActionId() {
    if (ActionType.equals("Ability")) {
      return Ability.getAbilityId();
    } else if (ActionType.equals("Item")) {
      return Item.getUserItemId();
    }
    return 0;
  }
}
