package player_classes;

public class ArcherClass extends BaseClass {

	private static int id = 6;
	
	public ArcherClass() {
		super(id);
	
		name = "Archer";
		
		baseClassId = 3;

		bgColor = "137,219,159";
		
		resetStartStats();
		
		getStartStats().setValue("MAX_HEALTH", 100);
		getStartStats().setValue("MAX_MANA", 50);
		
		getStartStats().setValue("STRENGTH", 5);
		getStartStats().setValue("INTELLIGENCE", 5);
		getStartStats().setValue("AGILITY", 8);
		
		getLevelStats().reset();
		
		getLevelStats().setValue("MAX_HEALTH", 7);
		getLevelStats().setValue("MAX_MANA", 4);
		
		getLevelStats().setValue("STRENGTH", 2);
		getLevelStats().setValue("INTELLIGENCE", 2);
		getLevelStats().setValue("AGILITY", 3);
		
		getLevelStats().setValue("SPEED", 1);
		
	}
}