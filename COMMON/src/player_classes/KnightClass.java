package player_classes;

public class KnightClass extends BaseClass {

  private static int id = 4;

  public KnightClass() {
    super(id);

    name = "Knight";

    baseClassId = 1;

    bgColor = "82,175,228";

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
