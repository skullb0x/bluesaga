package data_handlers.crafting_handler;

import java.util.Vector;

public class CraftingStation {
  private String name;
  private Vector<Recipe> recipes = new Vector<Recipe>();
  private int skillId;

  public CraftingStation(String name) {
    this.setName(name);
  }

  public void addRecipe(Recipe recipe) {
    recipes.add(recipe);
  }

  public Vector<Recipe> getRecipes() {
    return recipes;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getSkillId() {
    return skillId;
  }

  public void setSkillId(int skillId) {
    this.skillId = skillId;
  }
}
