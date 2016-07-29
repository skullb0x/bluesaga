package map;

import java.util.Iterator;
import java.util.Vector;

import creature.Creature;
import creature.Creature.CreatureType;
import creature.PlayerCharacter;
import data_handlers.ability_handler.StatusEffect;
import data_handlers.item_handler.Item;

public class Tile {
	private String Type;
	private String Name;
	private boolean Passable;
	
	private CreatureType OccupantType;
	private Creature Occupant;
	
	private String objectId = "None";
	private int soulCharacterId = 0; 
	
	private int ContainerId = 0;
	
	private int X;
	private int Y;
	private int Z;
	
	private boolean Transparent;
	
	// DOOR
	private int DoorId;
	private boolean MonsterLocked;
	
	private Door generatedDoor;
	
	// LOOT BAG
	private Vector<Item> LootBag;
	private int LootGold;
	
	// AREA EFFECT
	private int AreaEffectId = 0; 
	
	// TRAP AND TRIGGER
	private int TrapId = 0;
	private Trigger Trigger;
	
	// OVERLAY OBJECT
	
	// STATUSEFFCT
	private Vector<StatusEffect> statusEffects;
	
	
	private boolean damaged = false;
	private boolean healed = true;
	
		
	public Tile(int x, int y, int z){
		setX(x);
		setY(y);
		setZ(z);
		
		LootBag = new Vector<Item>();
	//	healTimer = new Timer();
		setMonsterLocked(false);
		
		statusEffects = new Vector<StatusEffect>();
	}
	
	
	public void setType(String type, String name, int passable){
		Type = type;
		Name = name;
		
		OccupantType = CreatureType.None;
		Occupant = null;
		
		if(passable == 0){
			Passable = false;
		}else{
			Passable = true;
		}
		
		if(name.equals("UL") || name.equals("U") || name.equals("UR") || name.equals("R") || name.equals("DR") || name.equals("D") || name.equals("DL") || name.equals("L") 
			|| name.equals("IUR") || name.equals("IUL") || name.equals("IDR") || name.equals("IDL")){
			setTransparent(true);
		}else{
			setTransparent(false);
		}
		
		DoorId = 0;
	}
	
	public boolean isPassable() {
		if(OccupantType != CreatureType.None){
			if(OccupantType == CreatureType.Player && Type.equals("indoors")){
				return true;
			}
			return false;
		}
		if(getDoorId() > 0){
			return true;
		}
		if(objectId.contains("moveable")){
			return false;
		}
		if(getStatusEffect(41) != null){
			return false;
		}
		return Passable;
	}

	public boolean isPassableIgnoreOccupants(){
		if(objectId.contains("moveable")){
			return false;
		}
		// Check if tile has block statuseffect
		if(getStatusEffect(41) != null){
			return false;
		}
	
		return Passable;
	}
	
	public boolean isPassableNonAggro() {
		if(Type.equals("door") || OccupantType != CreatureType.None){
			return false;
		}
		if(objectId.contains("moveable")){
			return false;
		}
		// Check if tile has block statuseffect
		if(getStatusEffect(41) != null){
			return false;
		}
		return Passable;
	}
	
	
	public boolean isPassableForPlayer(PlayerCharacter player) {
		if(!Type.equals("indoors") && OccupantType != CreatureType.None){
			return false;
		}
		
		if(Type.equals("shallow")){
			if(player.getShip().getShipId() > 0){
				return Passable;
			}else{
				return false;
			}
		}else if(Type.equals("water")){
			if(player.getShip().getShipId() == 2){
				return Passable;
			}else{
				return false;
			}
		}
		// Check if tile has block statuseffect
		if(getStatusEffect(41) != null){
			return false;
		}
		return Passable;
	}
	
	public boolean isPassableType(){
		return Passable;
	}
	
	public void setPassable(boolean passable){
		Passable = passable;
	}
	
	
	/****************************************
     *                                      *
     *         LOOT BAG			            *
     *                                      *
     *                                      *
     ****************************************/
	public void addLoot(Vector<Item> newLoot){
		LootBag.addAll(newLoot);
	}
	
	public Vector<Item> getLoot(){
		return LootBag;
	}
	
	public void addGold(int gold){
		LootGold += gold;
	}
	
	public int getGold(){
		return LootGold;
	}
	
	public void removeLoot(){
		LootGold = 0;
		LootBag.clear();
	}
	
	public void removeLootAtIndex(int index){
		LootBag.remove(index);
	}
	
	public void addLootItem(Item newItem){
		LootBag.add(newItem);
	}
	
	public boolean hasLoot(){
		if(LootBag.size() > 0){
			return true;
		}
		return false;
	}
	
	/****************************************
     *                                      *
     *         STATUS EFFECT				*
     *                                      *
     *                                      *
     ****************************************/
	
	public void damage(){
		damaged = true;
		healed = false;
	
		/*
		healTimer.schedule( new TimerTask(){
			public void run() {
				healed = true;
			}
		}, 30*1000);
		 */
	}

	public synchronized void addStatusEffects(Vector<StatusEffect> newStatusEffects){
		for(Iterator<StatusEffect> iter = newStatusEffects.iterator();iter.hasNext();){  
			StatusEffect s = iter.next();
			StatusEffect mySE = getStatusEffect(s.getId());
			
			if(mySE != null){
				mySE.setCaster(s.getCaster());
				mySE.setAbility(s.getAbility());
				mySE.start();
			}else{
				StatusEffect newSE = new StatusEffect(s.getId());
				newSE.setCaster(s.getCaster());
				newSE.setAbility(s.getAbility());
				newSE.start();
				getStatusEffects().add(newSE);
			}
		}
	}
	
	public synchronized Vector<StatusEffect> getStatusEffects(){
		return statusEffects;
	}
	
	public StatusEffect getStatusEffect(int sId){
		
		for(Iterator<StatusEffect> iter = getStatusEffects().iterator();iter.hasNext();){  
			StatusEffect s = iter.next();
			if(s.getId() == sId){
				return s;
			}
		}
		return null;
	}
	
	
	public synchronized String updateStatusEffect(){
		String sToRemove = "";
		
		for(Iterator<StatusEffect> iter = getStatusEffects().iterator();iter.hasNext();){  
			StatusEffect s = iter.next();
			
			if(!s.isActive()){
				sToRemove += s.getId()+",";
				iter.remove();
			}
		}
		return sToRemove;
	}
	

	public boolean isDamaged(){
		return damaged;
	}
	
	public boolean heal(){
		if(damaged && healed){
			damaged = false;
			return true;
		}
		return false;
	}
	
	
	/****************************************
     *                                      *
     *         GETTER / SETTER				*
     *                                      *
     *                                      *
     ****************************************/
	
	public boolean isWater(){
		if(getType().equals("shallow") 
		|| getType().contains("water")){
			return true;
		}
		return false;
	}
	
	public Creature getOccupant() {
		return Occupant;
	}
	
	public CreatureType getOccupantType() {
		return OccupantType;
	}
	
	public void setOccupant(CreatureType oType, Creature o) {
		OccupantType = oType;
		Occupant = o;
	}
	
	public String getName() {
		return Name;
	}
	
	public void setName(String newName) {
		Name = newName;
	}
	
	public String getType() {
		return Type;
	}
	
	public boolean getPassable(){
		return Passable;
	}


	public int getX() {
		return X;
	}


	public void setX(int x) {
		X = x;
	}


	public int getY() {
		return Y;
	}


	public void setY(int y) {
		Y = y;
	}


	public int getZ() {
		return Z;
	}


	public void setZ(int z) {
		Z = z;
	}


	public String getObjectId() {
		return objectId;
	}


	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}


	public boolean isTransparent() {
		return Transparent;
	}


	public void setTransparent(boolean transparent) {
		Transparent = transparent;
	}


	public int getDoorId() {
		return DoorId;
	}


	public void setDoorId(int doorId) {
		DoorId = doorId;
	}


	public int getAreaEffectId() {
		return AreaEffectId;
	}


	public void setAreaEffectId(int areaEffectId) {
		AreaEffectId = areaEffectId;
	}


	public int getSoulCharacterId() {
		return soulCharacterId;
	}


	public void setSoulCharacterId(int soulCharacterId) {
		this.soulCharacterId = soulCharacterId;
	}


	public int getContainerId() {
		return ContainerId;
	}


	public void setContainerId(int containerId) {
		ContainerId = containerId;
	}


	public int getTrapId() {
		return TrapId;
	}


	public void setTrapId(int trapId) {
		TrapId = trapId;
	}


	public Trigger getTrigger() {
		return Trigger;
	}


	public void setTrigger(Trigger trigger) {
		Trigger = trigger;
	}


	public boolean isMonsterLocked() {
		return MonsterLocked;
	}


	public void setMonsterLocked(boolean monsterLocked) {
		MonsterLocked = monsterLocked;
	}


	public Door getGeneratedDoor() {
		return generatedDoor;
	}


	public void setGeneratedDoor(Door generatedDoor) {
		this.generatedDoor = generatedDoor;
	}
	
}

