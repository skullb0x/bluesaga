package data_handlers.crafting_handler;

import java.util.Vector;

import data_handlers.item_handler.Item;

public class Recipe {
  private CraftingStation craftingStation;
  private Vector<Item> materials = new Vector<Item>();
  private Item product;

  public Recipe(int id, Item product) {
    this.product = product;
  }

  public void addMaterial(Item material) {
    materials.add(material);
  }

  public Item getProduct() {
    return product;
  }

  public CraftingStation getCraftingStation() {
    return craftingStation;
  }

  public void setCraftingStation(CraftingStation craftingStation) {
    this.craftingStation = craftingStation;
  }

  public Vector<Item> getMaterials() {
    return materials;
  }
}
