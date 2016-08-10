package data_handlers.item_handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import components.Stats;

import data_handlers.ability_handler.StatusEffect;
import network.Server;

public class Item {

  private int Id; // Id in item table
  private String Name;
  private String Type;
  private String SubType;
  private String Material;
  private String Family;
  private String Color;

  private String Description;

  private int dbId; // Id in user_item table
  private boolean Equipped;

  private boolean equipable;

  private boolean consumeable = false; // Potion, eatable, scroll, snowball etc

  private int Value;

  private int ClassId = 0;

  private String attackType;
  private String DamageStat;

  private int Range;

  private Stats Stats = new Stats();

  // SPECIAL ITEM STATS
  private int ModifierId = 0;
  private int MagicId = 0;

  private HashMap<String, Integer> Requirements = new HashMap<String, Integer>();

  private int ProjectileId;

  private int Stackable;
  private int Stacked = 1;

  private int ContainerId = 0;

  private boolean Sellable;

  private boolean TwoHands;

  private int ScrollUseId = 0;

  private Vector<StatusEffect> statusEffects;

  public Item() {
    Stats.reset();
    statusEffects = new Vector<StatusEffect>();
  }

  public Item(Item copy) {
    equipable = false;
    Color = "255,255,255";

    Stats.reset();
    statusEffects = new Vector<StatusEffect>();

    setId(copy.getId());
    setName(copy.getName());
    setType(copy.getType());
    setSubType(copy.getSubType());
    setMaterial(copy.getMaterial());

    setAttackType(copy.getAttackType());

    for (Iterator<String> iter = copy.getStats().getHashMap().keySet().iterator();
        iter.hasNext();
        ) {
      String key = iter.next().toString();
      int value = copy.getStats().getHashMap().get(key);
      Stats.setValue(key, value);
    }

    setValue(copy.getValue());

    setDamageStat(copy.getDamageStat());

    setRange(copy.getRange());

    setContainerId(copy.getContainerId());

    setProjectileId(copy.getProjectileId());

    setDescription(copy.getDescription());
    setTwoHands(copy.isTwoHands());
    setClassId(copy.getClassId());

    setRequirements(copy.getRequirements());
    setEquipable(copy.isEquipable());

    setSellable(copy.isSellable());

    setStackable(copy.getStackable());

    setScrollUseId(copy.getScrollUseId());

    for (StatusEffect se : copy.getStatusEffects()) {
      statusEffects.add(new StatusEffect(se.getId()));
    }

    if (getType().equals("Eatable")
        || getType().equals("Potion")
        || getType().equals("Scroll")
        || getType().equals("Material")) {
      setConsumeable(true);
    }
  }

  public void load(ResultSet rs) {

    equipable = false;
    Color = "255,255,255";
    Stats.reset();
    statusEffects.clear();

    try {
      Id = rs.getInt("Id");
      Name = rs.getString("Name");
      Type = rs.getString("Type");
      SubType = rs.getString("SubType");
      Material = rs.getString("Material");

      attackType = rs.getString("AttackType");

      Stats.setValues(rs);

      Stats.addValue("MinDamage", rs.getInt("MinDamage"));
      Stats.addValue("MaxDamage", rs.getInt("MaxDamage"));

      Value = rs.getInt("Value");

      DamageStat = rs.getString("DamageType");

      Range = rs.getInt("Range");

      ContainerId = 0;

      ProjectileId = rs.getInt("ProjectileId");

      setDescription(rs.getString("Description"));

      if (rs.getInt("TwoHands") == 0) {
        setTwoHands(false);
      } else {
        setTwoHands(true);
      }

      setClassId(rs.getInt("ClassId"));

      Requirements.put("ReqLevel", rs.getInt("ReqLevel"));
      Requirements.put("ReqStrength", rs.getInt("ReqStrength"));
      Requirements.put("ReqIntelligence", rs.getInt("ReqIntelligence"));
      Requirements.put("ReqAgility", rs.getInt("ReqAgility"));

      if (Type.equals("Head")
          || Type.equals("Weapon")
          || Type.equals("OffHand")
          || Type.equals("Amulet")
          || Type.equals("Artifact")) {
        equipable = true;
      }

      if (rs.getInt("Sellable") == 0) {
        setSellable(false);
      } else {
        setSellable(true);
      }

      setStackable(rs.getInt("Stackable"));
      setStacked(1);

      setScrollUseId(rs.getInt("ScrollUseId"));

      if (!rs.getString("StatusEffects").equals("None")) {
        String statusEffectIds[] = rs.getString("StatusEffects").split(",");
        for (String statusEffectId : statusEffectIds) {
          int statusEffectIdInt = Integer.parseInt(statusEffectId);
          statusEffects.add(new StatusEffect(statusEffectIdInt));
        }
      }
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public void equip() {
    Equipped = true;
  }

  public void unEquip() {
    Equipped = false;
  }

  public int getDamage(String AttackType) {
    return Stats.getValue(AttackType);
  }

  /****************************************
   *                                      *
   *             GETTER/SETTER            *
   *                                      *
   *                                      *
   ****************************************/
  public Stats getStats() {
    return Stats;
  }

  public int getStatValue(String StatType) {
    int value = 0;
    if (Stats.getHashMap().containsKey(StatType)) {
      value = Stats.getValue(StatType);
    }
    return value;
  }

  public int getUserItemId() {
    return dbId;
  }

  public void setUserItemId(int newUserItemId) {
    dbId = newUserItemId;
  }

  public boolean isEquipped() {
    return Equipped;
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

  public void setId(int id) {
    Id = id;
  }

  public int getId() {
    return Id;
  }

  public int getValue() {
    return Value;
  }

  public void setValue(int newvalue) {
    Value = newvalue;
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

  public int getRange() {
    return Range;
  }

  public String getDamageStat() {
    return DamageStat;
  }

  public String getStatsAsString() {
    StringBuilder bonusStats = new StringBuilder(500);

    bonusStats
        .append(Stats.getValue("STRENGTH"))
        .append(',')
        .append(Stats.getValue("INTELLIGENCE"))
        .append(',')
        .append(Stats.getValue("AGILITY"))
        .append(',')
        .append(Stats.getValue("SPEED"))
        .append(',')
        .append(Stats.getValue("CRITICAL_HIT"))
        .append(',')
        .append(Stats.getValue("EVASION"))
        .append(',')
        .append(Stats.getValue("ACCURACY"))
        .append(',')
        .append(Stats.getValue("MAX_HEALTH"))
        .append(',')
        .append(Stats.getValue("MAX_MANA"))
        .append(',')
        .append(Stats.getValue("FIRE_DEF"))
        .append(',')
        .append(Stats.getValue("COLD_DEF"))
        .append(',')
        .append(Stats.getValue("SHOCK_DEF"))
        .append(',')
        .append(Stats.getValue("CHEMS_DEF"))
        .append(',')
        .append(Stats.getValue("MIND_DEF"))
        .append(',')
        .append(Stats.getValue("ARMOR"));

    return bonusStats.toString();
  }

  public int getProjectileId() {
    return ProjectileId;
  }

  public void setProjectileId(int projectileId) {
    ProjectileId = projectileId;
  }

  public int getStackable() {
    return Stackable;
  }

  public void setStackable(int stackable) {
    Stackable = stackable;
  }

  public int getStacked() {
    return Stacked;
  }

  public void setStacked(int stacked) {
    Stacked = stacked;
  }

  public int getContainerId() {
    return ContainerId;
  }

  public void setContainerId(int containerId) {
    ContainerId = containerId;
  }

  public boolean isSellable() {
    return Sellable;
  }

  public void setSellable(boolean sellable) {
    Sellable = sellable;
  }

  public boolean isTwoHands() {
    return TwoHands;
  }

  public void setTwoHands(boolean twoHands) {
    TwoHands = twoHands;
  }

  public int getModifierId() {
    return ModifierId;
  }

  public void setModifierId(int modifierId) {

    ModifierId = modifierId;
    if (ModifierId > 0) {
      ResultSet rs =
          Server.gameDB.askDB(
              "select Name, StatBonus, Color from item_modifier where Id = " + modifierId);

      // Different percent for different equip types
      HashMap<String, Float> modifiers = new HashMap<String, Float>();
      modifiers.put("Weapon", 10.0f);
      modifiers.put("OffHand", 16.0f);
      modifiers.put("Head", 16.0f);
      modifiers.put("Amulet", 20.0f);
      modifiers.put("Artifact", 20.0f);

      try {
        if (rs.next()) {

          Color = rs.getString("Color");
          Name = rs.getString("Name") + " " + Name;

          for (Map.Entry<String, Integer> stat : Stats.getHashMap().entrySet()) {

            String statName = stat.getKey();
            int statValue = stat.getValue();

            if (statValue != 0 && !(getType().equals("Weapon") && statName.equals("ATTACKSPEED"))) {
              float statBonus = (rs.getInt("StatBonus") * modifiers.get(getType())) / 100.0f;
              int newStat = Math.round(statValue * (1.0f + statBonus));

              if (newStat < 0) {
                newStat = 0;
              }

              Stats.setValue(statName, newStat);
            }
          }
        }
        rs.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }

  public int getMagicId() {
    return MagicId;
  }

  public void setMagicId(int magicId) {
    MagicId = magicId;

    ResultSet rs = Server.gameDB.askDB("select ExtraBonus from item_magic where Id = " + magicId);

    try {
      if (rs.next()) {
        int levelModif = 1;

        if (getRequirement("ReqLevel") > 0) {
          levelModif = getRequirement("ReqLevel");
        }

        float statBonus = levelModif * 0.25f;
        int statBonusInt = (int) Math.ceil(statBonus);

        if (magicId == 1) {
          if (getType().equals("Weapon")) {
            StatusEffect seEffect = new StatusEffect(1);
            seEffect.setRepeatDamage(statBonusInt);
            statusEffects.add(seEffect);
          } else {
            Stats.setValue("FIRE_DEF", Stats.getValue("FIRE_DEF") + statBonusInt);
          }
        } else if (magicId == 2) {
          if (getType().equals("Weapon")) {
            StatusEffect seEffect = new StatusEffect(6);
            seEffect.setRepeatDamage(Math.round(statBonusInt * 0.75f));
            statusEffects.add(seEffect);
          } else {
            Stats.setValue("COLD_DEF", Stats.getValue("COLD_DEF") + statBonusInt);
          }
        } else if (magicId == 3) {
          if (getType().equals("Weapon")) {
            StatusEffect seEffect = new StatusEffect(7);
            seEffect.setRepeatDamage(statBonusInt);
            statusEffects.add(seEffect);
          } else {
            Stats.setValue("SHOCK_DEF", Stats.getValue("SHOCK_DEF") + statBonusInt);
          }
        } else if (magicId == 4) {
          Stats.setValue("STRENGTH", Stats.getValue("STRENGTH") + statBonusInt);
        } else if (magicId == 5) {
          if (getType().equals("Weapon")) {
            statusEffects.add(new StatusEffect(4));
          } else {
            Stats.setValue("CHEMS_DEF", Stats.getValue("CHEMS_DEF") + statBonusInt);
          }
        } else if (magicId == 6) {
          Stats.setValue("SPEED", Stats.getValue("SPEED") + statBonusInt);
          Stats.setValue("ATTACKSPEED", Stats.getValue("ATTACKSPEED") + statBonusInt);
        }
      }
      rs.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public int getScrollUseId() {
    return ScrollUseId;
  }

  public void setScrollUseId(int scrollUseId) {
    ScrollUseId = scrollUseId;
  }

  public int getSoldValue() {
    int price = (int) Math.floor(getValue() / 2.0f);

    if (getModifierId() > 0) {
      if (getModifierId() == 1) {
        price = Math.round(price / 4.0f);
      } else if (getModifierId() == 2) {
        price = Math.round(price / 2.5f);
      } else if (getModifierId() > 2) {
        price = Math.round(price * (1.0f + (getModifierId() - 2) * 0.1f));
      }
    }
    return price;
  }

  public String getDescription() {
    return Description;
  }

  public void setDescription(String description) {
    Description = description;
  }

  public int getClassId() {
    return ClassId;
  }

  public void setClassId(int classId) {
    ClassId = classId;
  }

  public int getDbId() {
    return dbId;
  }

  public void setDbId(int dbId) {
    this.dbId = dbId;
  }

  public void setEquipped(boolean equipped) {
    Equipped = equipped;
  }

  public void setEquipable(boolean equipable) {
    this.equipable = equipable;
  }

  public void setAttackType(String attackType) {
    this.attackType = attackType;
  }

  public void setDamageStat(String damageStat) {
    DamageStat = damageStat;
  }

  public void setRange(int range) {
    Range = range;
  }

  public void setStats(Stats stats) {
    Stats = stats;
  }

  public void setRequirements(HashMap<String, Integer> requirements) {
    Requirements = requirements;
  }

  public Vector<StatusEffect> getStatusEffects() {
    return statusEffects;
  }

  public boolean isConsumeable() {
    return consumeable;
  }

  public void setConsumeable(boolean consumeable) {
    this.consumeable = consumeable;
  }
}
