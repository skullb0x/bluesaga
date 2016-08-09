package components;

import java.util.HashMap;

public class Item {

  private int Id; // Id in item table
  private String Name;
  private String Type;
  private String SubType;
  private String Material;
  private String Family;
  private String Color;

  private int GraphicsNr = 0;

  private int dbId; // Id in user_item table
  private int CrewMemberId; // CrewMemberId in user_item table

  private boolean equipable;

  int Range;

  private int Value;

  private String attackType;

  private int classId;

  private int ModifierId;
  private int MagicId;

  private Stats Stats = new Stats();
  private Stats BonusStats = new Stats();

  private HashMap<String, Integer> Requirements = new HashMap<String, Integer>();

  public Item(int newId) {
    Id = newId;
  }

  public void equip(int creatureId) {
    CrewMemberId = creatureId;
  }

  public void unEquip() {
    CrewMemberId = 0;
  }

  /****************************************
   *                                      *
   *             GETTER/SETTER            *
   *                                      *
   *                                      *
   ****************************************/
  public HashMap<String, Integer> getStats() {
    return Stats.getHashMap();
  }

  public int getStatValue(String StatType) {
    return Stats.getValue(StatType);
  }

  public int getUserItemId() {
    return dbId;
  }

  public void setUserItemId(int newUserItemId) {
    dbId = newUserItemId;
  }

  public int getCrewMemberId() {
    return CrewMemberId;
  }

  public String getName() {
    return Name;
  }

  public void setName(String name) {
    Name = name;
  }

  public String getType() {
    return Type;
  }

  public void setType(String type) {
    Type = type;
  }

  public String getSubType() {
    return SubType;
  }

  public void setSubType(String subType) {
    SubType = subType;
  }

  public String getMaterial() {
    return Material;
  }

  public void setMaterial(String material) {
    Material = material;
  }

  public String getFamily() {
    return Family;
  }

  public void setFamily(String family) {
    Family = family;
  }

  public String getColor() {
    return Color;
  }

  public void setColor(String color) {
    Color = color;
  }

  public void setStats(Stats newStats) {
    Stats.clear();
    Stats = newStats;
  }

  public int getId() {
    return Id;
  }

  public int getValue() {
    return Value;
  }

  public String getAttackType() {
    return attackType;
  }

  public boolean isEquipable() {
    return equipable;
  }

  public int getRequirement(String Type) {
    return Requirements.get(Type);
  }

  public HashMap<String, Integer> getRequirements() {
    return Requirements;
  }

  public int getClassId() {
    return classId;
  }

  public int getRange() {
    return Range;
  }

  public int getModifierId() {
    return ModifierId;
  }

  public void setModifierId(int modifierId) {
    ModifierId = modifierId;
  }

  public int getMagicId() {
    return MagicId;
  }

  public void setMagicId(int magicId) {
    MagicId = magicId;
  }

  public Stats getBonusStats() {
    return BonusStats;
  }

  public void setBonusStats(Stats bonusStats) {
    BonusStats = bonusStats;
  }

  public int getGraphicsNr() {
    return GraphicsNr;
  }

  public void setGraphicsNr(int graphicsNr) {
    GraphicsNr = graphicsNr;
  }

  public void setClassId(int classId) {
    this.classId = classId;
  }
}
