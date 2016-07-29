package data_handlers.ability_handler;

import creature.Creature;

public class AbilityEvent {

	private Ability ABILITY;
	private int startX;
	private int startY;
	private int startZ;
	
	private int goalX;
	private int goalY;
	private int goalZ;
	
	private Creature CASTER;
	
	private int readyItr;
	private int readyItrEnd;
	
	//private Timer MonsterAbilityDelay;
		
	/**
	 * AbilityEvent constructor
	 * @param newAbility
	 * @param Caster
	 * @param goalX
	 * @param goalY
	 * @param goalZ
	 * @param delay - in ms
	 */
	public AbilityEvent(Ability newAbility, Creature Caster, int goalX, int goalY, int goalZ, int delay){
		ABILITY = newAbility;
		CASTER = Caster;
		
		readyItr = 0;
		readyItrEnd = delay;
		
		this.goalX = goalX;
		this.goalY = goalY;
		this.goalZ = goalZ;
		
		//perform();
		
		/*
		MonsterAbilityDelay = new Timer();
		
		MonsterAbilityDelay.schedule( new TimerTask(){
			@Override
			public void run() {
				perform();
			}
		}, 80);
		*/
	}
	
	public boolean checkReady(){
		if(readyItr < readyItrEnd){
			readyItr += 50;
		}else{
			return true;
		}
		return false;
	}
	
	public void perform(){
		startX = CASTER.getX();
		startY = CASTER.getY();
		startZ = CASTER.getZ();
		
		if(ABILITY.isTargetSelf()){
			goalX = CASTER.getX();
			goalY = CASTER.getY();
			goalZ = CASTER.getZ();
		}
		
		if(ABILITY.getProjectileId() > 0 || ABILITY.getProjectileEffectId() > 0){
			// CREATE PROJECTILE
			AbilityHandler.addProjectile(ABILITY, startX, startY, startZ, goalX, goalY, goalZ);
		}else{
			// INSTANT ABILITY
			AbilityHandler.abilityEffect(ABILITY, goalX, goalY, goalZ);
		}
	}
	
}
