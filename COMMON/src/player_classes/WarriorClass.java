package player_classes;

public class WarriorClass extends BaseClass {

	private static int id = 1;
	
	public WarriorClass() {
		super(id);
		
		name = "Warrior";		
		
		baseClassId = 1;
		
		bgColor = "210,210,210";
		
		resetStartStats();
		
		getStartStats().setValue("MAX_HEALTH", 100);
		getStartStats().setValue("MAX_MANA", 50);
		
		getStartStats().setValue("STRENGTH", 8);
		getStartStats().setValue("INTELLIGENCE", 5);
		getStartStats().setValue("AGILITY", 5);
		
		getLevelStats().reset();
		
		getLevelStats().setValue("MAX_HEALTH", 10);
		getLevelStats().setValue("MAX_MANA", 2);
		
		getLevelStats().setValue("STRENGTH", 2);
		getLevelStats().setValue("INTELLIGENCE", 1);
		getLevelStats().setValue("AGILITY", 1);
		
		getLevelStats().setValue("SPEED", 1);
	}
}