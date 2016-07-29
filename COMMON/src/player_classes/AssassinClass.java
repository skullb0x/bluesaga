package player_classes;

public class AssassinClass extends BaseClass {

	private static int id = 12;
	
	public AssassinClass() {
		super(id);
	
		name = "Assassin";
		
		baseClassId = 3;
		
		bgColor = "107,107,107";
		textColor = "255,255,255";
		
		available = false;
		
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