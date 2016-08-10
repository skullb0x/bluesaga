package player_classes;

public class BerserkerClass extends BaseClass {

  private static int id = 10;

  public BerserkerClass() {
    super(id);

    name = "Berserker";

    baseClassId = 1;

    bgColor = "255,150,0";

    available = false;

    resetStartStats();

    getStartStats().setValue("MAX_HEALTH", 100);
    getStartStats().setValue("MAX_MANA", 50);

    getStartStats().setValue("STRENGTH", 8);
    getStartStats().setValue("INTELLIGENCE", 5);
    getStartStats().setValue("AGILITY", 5);

    getLevelStats().reset();

    getLevelStats().setValue("MAX_HEALTH", 10);
    getLevelStats().setValue("MAX_MANA", 2);

    getLevelStats().setValue("STRENGTH", 4);
    getLevelStats().setValue("INTELLIGENCE", 1);
    getLevelStats().setValue("AGILITY", 2);

    getLevelStats().setValue("SPEED", 1);
  }
}
