package player_classes;

public class IceMageClass extends BaseClass {

  private static int id = 8;

  public IceMageClass() {
    super(id);

    name = "Coldbringer";

    baseClassId = 2;

    bgColor = "106,222,255";

    resetStartStats();

    getStartStats().setValue("MAX_HEALTH", 100);
    getStartStats().setValue("MAX_MANA", 50);

    getStartStats().setValue("STRENGTH", 5);
    getStartStats().setValue("INTELLIGENCE", 8);
    getStartStats().setValue("AGILITY", 5);

    getLevelStats().reset();

    getLevelStats().setValue("MAX_HEALTH", 5);
    getLevelStats().setValue("MAX_MANA", 8);

    getLevelStats().setValue("STRENGTH", 1);
    getLevelStats().setValue("INTELLIGENCE", 4);
    getLevelStats().setValue("AGILITY", 2);

    getLevelStats().setValue("SPEED", 1);
  }
}
