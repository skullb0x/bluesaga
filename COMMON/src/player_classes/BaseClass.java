package player_classes;

import java.util.Map.Entry;

import utils.GameInfo;
import components.Stats;

public class BaseClass {
  public int id;
  public String name;

  public int baseClassId = 0;

  public String bgColor = "0,0,0";
  public String textColor = "20,20,20";

  private Stats startStats = new Stats();
  private Stats levelStats = new Stats();

  private int xp = 0;
  public int nextXP = 100;

  public int level = 1;

  public boolean available = true;

  /**
   * Constructor
   * @param id
   */
  public BaseClass(int id) {
    this.id = id;
  }

  public BaseClass(BaseClass copy) {
    this.id = copy.id;
    this.name = copy.name;
    this.baseClassId = copy.baseClassId;
    this.bgColor = copy.bgColor;
    this.textColor = copy.textColor;
    this.nextXP = copy.nextXP;

    for (Entry<String, Integer> entry : copy.getStartStats().getHashMap().entrySet()) {
      String key = entry.getKey();
      int value = entry.getValue();

      startStats.getHashMap().put(key, value);
    }

    for (Entry<String, Integer> entry : copy.getLevelStats().getHashMap().entrySet()) {
      String key = entry.getKey();
      int value = entry.getValue();

      levelStats.getHashMap().put(key, value);
    }
  }

  public void resetStartStats() {
    startStats.reset();
    startStats.addValue("SPEED", 100);
    startStats.addValue("ATTACKSPEED", 100);
    startStats.addValue("ACCURACY", 90);
    startStats.addValue("EVASION", 10);
    startStats.addValue("CRITICAL_HIT", 1);
  }

  public boolean addXP(int addedXP) {
    xp += addedXP;
    if (xp >= nextXP) {
      xp = 0;
      level++;
      nextXP = GameInfo.classNextXP.get(level);
      return true;
    }

    return false;
  }

  public int getXPBarWidth(int Max) {
    float fxp = xp;
    float fMaxXP = nextXP;
    float spBarWidth = (fxp / fMaxXP) * Max;
    return Math.round(spBarWidth);
  }

  /**
   * Getters and setters
   * @return
   */
  public Stats getStartStats() {
    return startStats;
  }

  public Stats getLevelStats() {
    return levelStats;
  }

  public void setName(String name) {}

  public int getXp() {
    return xp;
  }

  public void setXp(int xp) {
    this.xp = xp;
  }
}
