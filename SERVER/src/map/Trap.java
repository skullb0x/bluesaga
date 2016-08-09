package map;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Trap {
  private int Id;

  private int TrapId;
  private String NameOn;
  private String NameOff;

  private int X;
  private int Y;
  private int Z;

  private boolean Active;

  private boolean On;

  private int Damage;
  private String DamageType;
  private int AbilityId;
  private int Repeat;
  private int EffectSpan = 1; // x 1 sec

  private int effectItr = 0;
  private int effectReady = EffectSpan;

  private int repeatItr = 0;

  public Trap() {}

  public void load(ResultSet info) {
    try {
      setTrapId(info.getInt("Id"));

      String name = info.getString("Name");
      NameOn = (name + "_on").intern();
      NameOff = (name + "_off").intern();

      if (info.getInt("Active") == 0) {
        setActive(false);
      } else {
        setActive(true);
      }
      setDamage(info.getInt("Damage"));
      setDamageType(info.getString("DamageType").intern());
      setAbilityId(info.getInt("AbilityId"));
      setRepeat(info.getInt("Repeat"));
      setEffectSpan(info.getInt("EffectSpan"));

      effectReady = EffectSpan;
      effectItr = effectReady;

      repeatItr = 0;

      On = false;

    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public int getId() {
    return Id;
  }

  public void setId(int id) {
    Id = id;
  }

  public int getX() {
    return X;
  }

  public void setX(int x) {
    X = x;
  }

  public int getZ() {
    return Z;
  }

  public void setZ(int z) {
    Z = z;
  }

  public int getY() {
    return Y;
  }

  public void setY(int y) {
    Y = y;
  }

  public boolean isActive() {
    return Active;
  }

  public void setActive(boolean active) {
    Active = active;
  }

  public boolean isReady() {
    if (effectItr < effectReady) {
      effectItr++;
    } else {
      return true;
    }
    return false;
  }

  public void trigger() {
    repeatItr++;
    effectItr = 0;
    toggleOn();
    if (Repeat > 0 && repeatItr > Repeat) {
      setActive(false);
      setOn(false);
    }
  }

  public int getDamage() {
    return Damage;
  }

  public void setDamage(int damage) {
    Damage = damage;
  }

  public String getDamageType() {
    return DamageType;
  }

  public void setDamageType(String damageType) {
    DamageType = damageType;
  }

  public int getAbilityId() {
    return AbilityId;
  }

  public void setAbilityId(int abilityId) {
    AbilityId = abilityId;
  }

  public int getRepeat() {
    return Repeat;
  }

  public void setRepeat(int repeat) {
    Repeat = repeat;
  }

  public int getEffectSpan() {
    return EffectSpan;
  }

  public void setEffectSpan(int effectSpan) {
    EffectSpan = effectSpan;
  }

  public boolean isOn() {
    return On;
  }

  public void setOn(boolean on) {
    On = on;
  }

  public void toggleOn() {
    if (On) {
      On = false;
    } else {
      On = true;
    }
  }

  public String getNameOn() {
    return NameOff;
  }

  public String getNameOff() {
    return NameOff;
  }

  public int getTrapId() {
    return TrapId;
  }

  public void setTrapId(int trapId) {
    TrapId = trapId;
  }
}
