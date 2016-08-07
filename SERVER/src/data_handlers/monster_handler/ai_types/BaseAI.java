package data_handlers.monster_handler.ai_types;

import java.util.Vector;

import creature.Creature;
import creature.Npc;

public class BaseAI {
	
	protected final Npc me;
	
	protected BaseAI(Npc monster) {
		me = monster;
	}
	
	public void doAggroBehaviour(Vector<Npc> monsterMoved) {}
	
	public void becomeAggro() {}
	
	public static BaseAI newAi(Npc me) {
		if (me.getAttackRange() > 2) {
			return new Ranged(me);
		} else if (me.getCreatureId() == 63 || me.getCreatureId() == 77) {
			return new Shy(me);
		} else {
			return new Melee(me);
		}
	}
}
