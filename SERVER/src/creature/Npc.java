package creature;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ThreadLocalRandom;

import components.Stats;
import creature.Creature.CreatureType;
import utils.ServerGameInfo;
import utils.RandomUtils;
import data_handlers.ability_handler.Ability;
import data_handlers.ability_handler.AbilityHandler;
import data_handlers.item_handler.Item;
import data_handlers.monster_handler.ai_types.BaseAI;
import data_handlers.monster_handler.MonsterHandler;
import network.Server;

/*
 * 
 * 
 *		ALL NON PLAYER CHARACTERS 
 * 
 * 
 */

public class Npc extends Creature {

	private int respawnTime;
	private int respawnTimeItr;

	private int existDayOrNight = 0; // 0 = both, 1 = night, 2 = day
	
	/** 
	 * AI types
	 */
	
	/**
	 * Melee
	 * Chases their target till they are up close and then attacks
	 */
	
	/** 
	 * Ranged
	 * Keeps their distance from target so they can attack. If the target approaches, 
	 * then the monster steps away.
	 */
	
	/**
	 * Swarmer
	 * Behaves like 
	 * Boss
	 * 
	 */
	
	/**
	 * 0 = not moving and no aggro
	 * 1 = moving and no aggro
	 * 2 = moving and aggro
	 * 3 = npc quest/shop
	 * 4 = moving and aggro but no attackspeed
	 * 5 = guardian
	 */
	private int AggroType; 
	private int AggroRange;
	
	private int SpecialType = 0;
	private String SpecialName = "";

	private int SpawnX;
	private int SpawnY;
	private int SpawnZ;

	private boolean usedStashItem = false;
	private List<Item> StashItems;

	private boolean BountyMonster = false;
	private int Bounty = 0;

	private String MonsterWeaponIds; // Used by monsters only
	private String MonsterOffHandIds;
	private String MonsterHeadIds;

	private int GiveXP;

	private int originalAggroType = 1;

	public HashMap<Creature, Integer> attackersDamage;	

	private boolean elite = false;
	private boolean titan = false;
	private boolean raging = false;
	
	private final BaseAI ai;
	
	public Npc(int creatureId, int newX, int newY, int newZ) {
		super(creatureId,newX,newY,newZ);

		
		//generateLoot(gameDB);

		SpawnX = newX;
		SpawnY = newY;
		SpawnZ = newZ;

		StashItems = new ArrayList<Item>(3);

		attackersDamage = new HashMap<Creature, Integer>();	

		ResultSet rs = Server.gameDB.askDB("select AggroRange,RespawnTime, MonsterWeaponIds, MonsterOffHandIds, MonsterHeadIds, GiveXP, Level from creature where Id = "+creatureId);
		try {
			if(rs.next()){
				setMonsterWeaponIds(rs.getString("MonsterWeaponIds"));
				setMonsterOffHandIds(rs.getString("MonsterOffHandIds"));
				setMonsterHeadIds(rs.getString("MonsterHeadIds"));
				setGiveXP(rs.getInt("GiveXP"));
				setAggroRange(rs.getInt("AggroRange"));
				respawnTime = rs.getInt("RespawnTime");
				Level = rs.getInt("Level");
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		loadEquipment();

		// Hide name of non-aggro creatures (ex: chicken, kitten)
		if(getAggroType() == 4){
			setName("");
		}
		setOriginalAggroType(getAggroType());
		
		elite = false;
		titan = false;
		
		respawnTimeItr = 0;
		
		ai = BaseAI.newAi(this);
	}

	public Npc(Npc copy, int npcX, int npcY, int npcZ){
		super(copy, npcX, npcY, npcZ);
		
		setElite(false);
		setTitan(false);
		
		SpawnX = npcX;
		SpawnY = npcY;
		SpawnZ = npcZ;

		StashItems = new ArrayList<Item>(3);

		attackersDamage = new HashMap<Creature, Integer>();	

		setMonsterWeaponIds(copy.getMonsterWeaponIds());
		setMonsterOffHandIds(copy.getMonsterOffHandIds());
		setMonsterHeadIds(copy.getMonsterHeadIds());
		setGiveXP(copy.getGiveXP());
		setAggroRange(copy.getAggroRange());
		respawnTime = copy.respawnTime;
		Level = copy.getLevel();
		
		loadEquipment();

		setOriginalAggroType(copy.getOriginalAggroType());
		
		elite = false;
		titan = false;
		
		respawnTimeItr = 0;
		
		ai = BaseAI.newAi(this);
	}
	

	public void generateLoot(){
		/*
		ResultSet lootInfo = Server.gameDB.askDB("select LootItems from creature where Id = "+getCreatureId());
		try {
			if(lootInfo.next()){
				if(!lootInfo.getString("LootItems").equals("None")){
					String loot_info[] = lootInfo.getString("LootItems").split(";");


					for(String itemInfo: loot_info){
						String item_info[] = itemInfo.split(",");
						int itemId = Integer.parseInt(item_info[0]);


						Item newLoot = new Item(itemId);

						int chanceToEquip = Server.ThreadLocalRandom.current().nextInt(100);

						if(canEquip(newLoot)){
							equipItem(newLoot);
						}
					}	
				}
			}
			lootInfo.close();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		 */
	}

  public void doAggroBehaviour(Vector<Npc> monsterMoved) {
    ai.doAggroBehaviour(monsterMoved);
  }
	
	public String getFullData(){
		String npcData = super.getFullData()+","+getAggroType()+","+getSpecialType();
		return npcData;
	}
	

	public void setupMonsterBounty(int level){
		setBountyMonster(true);

		int giveXP = getGiveXP() * 3;
		float str = getStat("STRENGTH") * 1.2f;
		float agi = getStat("AGILITY") * 1.2f;
		float intel = getStat("INTELLIGENCE") * 1.2f;
		float spd = getStat("SPEED")*1.2f;
		float healthf = getStat("MAX_HEALTH") * 1.5f;
		float manaf = getStat("MAX_MANA") * 1.5f;

		int randomClass = RandomUtils.getInt(1, 3);

		int nrHealthPotions = RandomUtils.getInt(1, 10);
		if(level < 20){
			for(int i = 0; i < nrHealthPotions; i++){
				addStashItem(new Item(ServerGameInfo.itemDef.get(3)));
			}
		}else{
			for(int i = 0; i < nrHealthPotions; i++){
				addStashItem(new Item(ServerGameInfo.itemDef.get(4)));
			}
		}

		int nrManaPotions = RandomUtils.getInt(1, 10);
		if(level < 20){
			for(int i = 0; i < nrManaPotions; i++){
				addStashItem(new Item(ServerGameInfo.itemDef.get(5)));
			}
		}else{
			for(int i = 0; i < nrManaPotions; i++){
				addStashItem(new Item(ServerGameInfo.itemDef.get(6)));
			}
		}


		if(randomClass == 1){
			// WARRIOR
			str *= 1.5;
			healthf *= 1.5f;

			if(level > 10){
				addAbility(AbilityHandler.getAbility(29));
			}
			if(level > 20){
				addAbility(AbilityHandler.getAbility(10));
			}
			if(level > 30){
				addAbility(AbilityHandler.getAbility(56));
			}

			addAbility(AbilityHandler.getAbility(17));


		}else if(randomClass == 2){
			// MAGE
			intel *= 1.5f;
			manaf *= 2f;

			int randomMage = RandomUtils.getInt(1, 3);

			if(randomMage == 1){
				// FIRE
				if(level > 10){
					addAbility(AbilityHandler.getAbility(23));
				}
				if(level > 20){
					addAbility(AbilityHandler.getAbility(24));
				}
				if(level > 30){
					addAbility(AbilityHandler.getAbility(57));
				}

				addAbility(AbilityHandler.getAbility(1));
				addAbility(AbilityHandler.getAbility(42));


			}else if(randomMage == 2){ 							
				// ICE
				if(level > 10){
					addAbility(AbilityHandler.getAbility(21));
				}
				if(level > 20){
					addAbility(AbilityHandler.getAbility(22));
				}
				if(level > 30){
					addAbility(AbilityHandler.getAbility(58));
				}

				addAbility(AbilityHandler.getAbility(8));
				addAbility(AbilityHandler.getAbility(43));


			}else if(randomMage == 3){ 							
				// SHOCK

				if(level > 10){
					addAbility(AbilityHandler.getAbility(19));
				}
				if(level > 20){
					addAbility(AbilityHandler.getAbility(20));
				}

				addAbility(AbilityHandler.getAbility(9));
				addAbility(AbilityHandler.getAbility(44));

			}
			if(level > 20){
				addAbility(AbilityHandler.getAbility(30));
			}
		}else if(randomClass == 3){
			agi *= 1.5f;
			spd *= 1.5f;
			healthf *= 1.2f;

			if(level > 10){
				addAbility(AbilityHandler.getAbility(16));
			}
			if(level > 20){
				addAbility(AbilityHandler.getAbility(55));
			}
			if(level > 30){
				addAbility(AbilityHandler.getAbility(28));
			}
			addAbility(AbilityHandler.getAbility(54));
		}

		setStat("STRENGTH",Math.round(str));
		setStat("AGILITY",Math.round(agi));
		setStat("INTELLIGENCE",Math.round(intel));
		setStat("SPEED",Math.round(spd));
		setStat("ATTACKSPEED",Math.round(spd));

		setStat("MAX_HEALTH",Math.round(healthf));
		setStat("MAX_MANA",Math.round(manaf));
		changeHealth(Math.round(healthf));
		changeMana(Math.round(manaf));
		setGiveXP(giveXP);
	}


	public void addAttackerDamage(Creature attacker, int damage){
		int totaldam = 0;

		if(Health - damage < 0){
			damage = Health;
		}

		if(damage < 0){
			damage = 0;
		}

		if(attackersDamage.containsKey(attacker)){
			totaldam = attackersDamage.get(attacker);
		}

		totaldam += damage;

		attackersDamage.put(attacker, totaldam);
	}

	public Ability useRandomAbility(){
		if(abilities!=null && abilities.size()>0){
			int random = ThreadLocalRandom.current().nextInt() % abilities.size();
			return abilities.get(random);
		}
		return null;
	}


	public int getSpawnX() {
		return SpawnX;
	}

	public int getSpawnY() {
		return SpawnY;
	}

	public int getSpawnZ() {
		return SpawnZ;
	}

	public void startRespawnTimer(){
		respawnTimeItr = 0;
	}


	public void respawn(){
		X = SpawnX;
		Y = SpawnY;

		revive();
	}

	public void softRespawn(){
		X = SpawnX;
		Y = SpawnY;

		Stats.setValue("HEALTH",getStat("MAX_HEALTH"));
		Stats.setValue("MANA",getStat("MAX_MANA"));

	}

	public void restartRespawn(){
		respawnTimeItr = 0;
	}
	public boolean checkRespawn(){
		respawnTimeItr+=2;
		if(respawnTimeItr >= respawnTime){
			respawn();
			return true;
		}
		return false;
	}

	public void setRespawnTimerReady(){
		respawnTimeItr = respawnTime;
	}

	public void addStashItem(Item newItem){
		StashItems.add(newItem);
	}


	public boolean isBountyMonster() {
		return BountyMonster;
	}


	public void setBountyMonster(boolean bountyMonster) {
		BountyMonster = bountyMonster;
	}


	public int getBounty() {
		return Bounty;
	}


	public void setBounty(int bounty) {
		Bounty = bounty;
	}

	public String getMonsterWeaponIds() {
		return MonsterWeaponIds;
	}


	public void setMonsterWeaponIds(String monsterWeaponIds) {
		MonsterWeaponIds = monsterWeaponIds;
	}


	public String getMonsterOffHandIds() {
		return MonsterOffHandIds;
	}


	public void setMonsterOffHandIds(String monsterOffHandIds) {
		MonsterOffHandIds = monsterOffHandIds;
	}


	public String getMonsterHeadIds() {
		return MonsterHeadIds;
	}


	public void setMonsterHeadIds(String monsterHeadIds) {
		MonsterHeadIds = monsterHeadIds;
	}


	public int getGiveXP() {
		return GiveXP;
	}

	public void setGiveXP(int giveXP) {
		GiveXP = giveXP;
	}
	
	@Override
	public void revive(){
		SpecialType = 0;
		SpecialName = "";
		
		Npc original_monster = ServerGameInfo.creatureDef.get(CreatureId);
		
		Stats = new Stats();
	    
		for(Iterator<String> iter = original_monster.getStats().getHashMap().keySet().iterator();iter.hasNext();){  
		   String key = iter.next().toString();  
		   int value = original_monster.getStats().getHashMap().get(key);  
		   Stats.setValue(key, value);
		}  
	    
	    Health = Stats.getValue("MAX_HEALTH");
	    Mana = Stats.getValue("MAX_MANA");
	
	    abilities = null;
	    for(Ability a: original_monster.getAbilities()){
	    	Ability newAbility = new Ability(ServerGameInfo.abilityDef.get(a.getAbilityId()));
			newAbility.setCaster(CreatureType.Monster, this);
			addAbility(newAbility);
	    }
	    
	    Level = original_monster.getLevel();
	    setSizeWidth(original_monster.getSizeWidth());
		setSizeHeight(original_monster.getSizeHeight());
		setGiveXP(original_monster.getGiveXP());
	    
		AggroType = originalAggroType;
		
		// Check if last monster was elite or titan last time
		if(isElite()){
			turnElite();
		}else if(isTitan()){
			turnTitan(true);
		}
		
		if(Level > 9 && getAggroType() < 3){
			turnSpecial(0);
		}
		
		setAggro(null);
		
		attackersDamage.clear();
		
		if(getFamilyId() == 8 || getAggroType() == 4 || getCreatureId() == 1){
			setStat("ATTACKSPEED", 0);
		}
		
		loadEquipment();
		
		super.revive();
	}

	public void loadEquipment(){
		if(!getMonsterWeaponIds().equals("None")){
			String weapons[] = getMonsterWeaponIds().split(",");
			
			int randomWeaponId = ThreadLocalRandom.current().nextInt(weapons.length);
			
			int weaponId = Integer.parseInt(weapons[randomWeaponId]);
			equipItem(new Item(ServerGameInfo.itemDef.get(weaponId)));
		}
		
		
		if(!getMonsterOffHandIds().equals("None")){
			String offhands[] = getMonsterOffHandIds().split(",");
			
			int randomWeaponId = ThreadLocalRandom.current().nextInt(offhands.length);
			
			int offhandId = Integer.parseInt(offhands[randomWeaponId]);
			equipItem(new Item(ServerGameInfo.itemDef.get(offhandId)));
		}
		
		if(!getMonsterHeadIds().equals("None")){
			String heads[] = getMonsterHeadIds().split(",");
			
			int randomWeaponId = ThreadLocalRandom.current().nextInt(heads.length);
			
			int headId = Integer.parseInt(heads[randomWeaponId]);
			equipItem(new Item(ServerGameInfo.itemDef.get(headId)));
		}
	}
	
	public void turnSpecial(int gloomyNr){
		
		if(gloomyNr == 0){
			if(getAggroType() < 3 && getLevel() > 5){
				gloomyNr = RandomUtils.getInt(0, 140);
			}
		}
		
		if(gloomyNr > 0 && gloomyNr < 7){
			int giveXP = getGiveXP();
			
			Level += 10;
			giveXP *= 3;
			float str = getStat("STRENGTH") * 1.5f;
			float agi = getStat("AGILITY") * 1.5f;
			float intel = getStat("INTELLIGENCE") * 1.5f;
			float spd = getStat("SPEED");
			float atkspd = getStat("ATTACKSPEED");
			float healthf = getStat("MAX_HEALTH") * 2f;
			float manaf = getStat("MAX_MANA") * 4f;

			if(gloomyNr == 1){
				SpecialName = "Burning";
				setStat("FIRE_DEF",80);

				addAbility(AbilityHandler.getAbility(7));
				addAbility(AbilityHandler.getAbility(23));
			}else if(gloomyNr == 2){
				SpecialName = "Frozen";
				setStat("COLD_DEF",80);
				addAbility(AbilityHandler.getAbility(21));
				addAbility(AbilityHandler.getAbility(22));
			}else if(gloomyNr == 3){
				SpecialName = "Shock";
				setStat("SHOCK_DEF",80);
				addAbility(AbilityHandler.getAbility(19));
				addAbility(AbilityHandler.getAbility(20));
			}else if(gloomyNr == 4){
				SpecialName = "Mighty";
				setStat("ARMOR",100);
				addAbility(AbilityHandler.getAbility(17));
				addAbility(AbilityHandler.getAbility(29));
				str *= 1.2f;
			}else if(gloomyNr == 5){
				SpecialName = "Toxic";
				setStat("CHEMS_DEF",80);
				addAbility(AbilityHandler.getAbility(12));
				addAbility(AbilityHandler.getAbility(6));
				intel *= 1.5;
				manaf *= 1.5f;
			}else if(gloomyNr == 6){
				SpecialName = "Flash";
				addAbility(AbilityHandler.getAbility(16));
				addAbility(AbilityHandler.getAbility(28));
				atkspd *= 1.5f;
			}

			setGiveXP(giveXP);

			setStat("STRENGTH",Math.round(str));
			setStat("AGILITY",Math.round(agi));
			setStat("INTELLIGENCE",Math.round(intel));
			setStat("SPEED",Math.round(spd));
			setStat("ATTACKSPEED",Math.round(atkspd));

			setStat("MAX_HEALTH",Math.round(healthf));
			Health = getStat("MAX_HEALTH");
			setStat("MAX_MANA",Math.round(manaf));
			Mana = getStat("MAX_MANA");

			// Only archipelago monsters can become titans
			/*
			if(getZ() <= -100){
				//turnTitan(titan);
			}
			*/
		}else{
			gloomyNr = 0;
		}
		SpecialType = gloomyNr;
	}

	public void turnTitan(boolean titan){
		int titanChance = 200;
		if(titan){
			titanChance = 0;
		}else{
			titanChance = RandomUtils.getInt(0, 200);
		}
		
		
		if(titanChance < 10){
			Level += 50;
			int giveXP = Level * 5;
			
			float newWidth = getSizeWidth()*4f;
			float newHeight = getSizeHeight()*4f;
			
			float str = 1 + (Level-1) * 2f;
			float agi = 1 + (Level-1) * 2f;
			float intel = 1 + (Level-1) * 2f;
			float spd = getStat("SPEED");
			float healthf = Level * 5 * (Level / 10) + 4;
			float manaf = getStat("MAX_MANA") * 4f;

			setSizeWidth(Math.round(newWidth));
			setSizeHeight(Math.round(newHeight));
			
			setGiveXP(giveXP);

			setStat("STRENGTH",Math.round(str));
			setStat("AGILITY",Math.round(agi));
			setStat("INTELLIGENCE",Math.round(intel));
			setStat("SPEED",Math.round(spd));
			setStat("ATTACKSPEED",Math.round(spd));

			setStat("MAX_HEALTH",Math.round(healthf));
			Health = getStat("MAX_HEALTH");
			setStat("MAX_MANA",Math.round(manaf));
			Mana = getStat("MAX_MANA");
			
			SpecialName = SpecialName + " Titan";
			
			// Add healing spell
			addAbility(AbilityHandler.getAbility(86));
			
			setAggroType(1);
			
			unequipItem("Weapon");
			unequipItem("Head");
			unequipItem("Amulet");
			unequipItem("Artifact");
			unequipItem("OffHand");
			
			setTitan(true);
		}
	}

	public void turnElite(){
		
		Level += 10;
		GiveXP = Level * 5;
		
		float newWidth = getSizeWidth()*2f;
		float newHeight = getSizeHeight()*2f;
		
		float str = 1 + (Level-1) * 2f;
		float agi = 1 + (Level-1) * 2f;
		float intel = 1 + (Level-1) * 2f;
		float spd = getStat("SPEED");
		float healthf = Level * 5 * (Level / 10) + 4;
		float manaf = getStat("MAX_MANA") * 4f;

		setSizeWidth(Math.round(newWidth));
		setSizeHeight(Math.round(newHeight));
		
		setStat("STRENGTH",Math.round(str));
		setStat("AGILITY",Math.round(agi));
		setStat("INTELLIGENCE",Math.round(intel));
		setStat("SPEED",Math.round(spd));
		setStat("ATTACKSPEED",Math.round(spd));

		setStat("MAX_HEALTH",Math.round(healthf));
		Health = getStat("MAX_HEALTH");
		setStat("MAX_MANA",Math.round(manaf));
		Mana = getStat("MAX_MANA");
		
		SpecialName = "Elite"+SpecialName;
		
		setElite(true);
		
		unequipItem("Weapon");
		unequipItem("Head");
		unequipItem("Amulet");
		unequipItem("Artifact");
		unequipItem("OffHand");
	}
	
	public void turnRaging(){
		
		Level += 30;
		GiveXP = Level * 7;
		
		float newWidth = getSizeWidth()*2f;
		float newHeight = getSizeHeight()*2f;
		
		float str = 1 + (Level-1) * 2f;
		float agi = 1 + (Level-1) * 2f;
		float intel = 1 + (Level-1) * 2f;
		float spd = getStat("SPEED");
		float healthf = Level * 5 * (Level / 10) + 4;
		float manaf = getStat("MAX_MANA") * 4f;

		setSizeWidth(Math.round(newWidth));
		setSizeHeight(Math.round(newHeight));
		
		setStat("STRENGTH",Math.round(str));
		setStat("AGILITY",Math.round(agi));
		setStat("INTELLIGENCE",Math.round(intel));
		setStat("SPEED",Math.round(spd));
		setStat("ATTACKSPEED",Math.round(spd));

		setStat("MAX_HEALTH",Math.round(healthf));
		Health = getStat("MAX_HEALTH");
		setStat("MAX_MANA",Math.round(manaf));
		Mana = getStat("MAX_MANA");
		
		SpecialName = "Raging"+SpecialName;
		
		setRaging(true);
		
		unequipItem("Weapon");
		unequipItem("Head");
		unequipItem("Amulet");
		unequipItem("Artifact");
		unequipItem("OffHand");
	}
	
	
	public HashMap<Creature, Integer> getAttackersXP(){

		//String xp_share = "";
		// attackerType, attackerId, gained_xp ; ...

		// Remove attackers that aren't in the area
		float totalXP = getGiveXP();
		float totalDamage = 0;

		for (Map.Entry<Creature, Integer> entry : attackersDamage.entrySet()) {
			totalDamage += entry.getValue();
		}

		HashMap<Creature, Integer> attackersXP = new HashMap<Creature, Integer>();

		for (Map.Entry<Creature, Integer> entry : attackersDamage.entrySet()) {
			Creature attacker = entry.getKey();
			float damage = entry.getValue();
			
			int gained_xp = Math.round(totalXP * (damage / totalDamage)); 

			attackersXP.put(attacker, gained_xp);
		}

		return attackersXP;
	}

	/**
	 * Getters and setters
	 */

	public void setAggro(Creature target) {
		super.setAggro(target);
		
		// Update aggroMonster list in MonsterHandler
		if(target != null){
			if(!MonsterHandler.aggroMonsters.contains(this)){
				MonsterHandler.aggroMonsters.add(this);
				ai.becomeAggro();
			}
		} else {
			MonsterHandler.aggroMonsters.remove(this);
		}
	}
	
	public List<Item> getStashItems(){
		return StashItems;
	}


	public boolean isUsedStashItem() {
		return usedStashItem;
	}


	public void setUsedStashItem(boolean usedStashItem) {
		this.usedStashItem = usedStashItem;
	}


	public int getOriginalAggroType() {
		return originalAggroType;
	}


	public void setOriginalAggroType(int originalAggroType) {
		this.originalAggroType = originalAggroType;
	}


	public boolean isTitan() {
		return titan;
	}


	public void setTitan(boolean titan) {
		if(titan){
		//	SpecialName = "Titan";
		}else{
			//SpecialName = "";
		}
		this.titan = titan;
	}


	public int getRespawnDayNight() {
		return existDayOrNight;
	}


	public void setRespawnDayNight(int respawnDayNight) {
		this.existDayOrNight = respawnDayNight;
	}

	public int getExistDayOrNight() {
		return existDayOrNight;
	}

	public void setExistDayOrNight(int existDayOrNight) {
		this.existDayOrNight = existDayOrNight;
	}
	
	public int getAggroType(){
		return AggroType;
	}
	
	public void setAggroType(int newAggroType){
		AggroType = newAggroType;
		
		if(getFamilyId() == 8 || getAggroType() == 4 || getCreatureId() == 1){
			setStat("ATTACKSPEED", 0);
		}
	}
	
	public int getAggroRange() {
		return AggroRange;
	}

	public void setAggroRange(int aggroRange) {
		AggroRange = aggroRange;
	}
	
	public String getName() {
		String fullName = super.getName();
		if(getFamilyId() == 8){
			fullName = "";
		}else if(!SpecialName.equals("")){
			fullName = SpecialName+" "+super.getName();
		}
		return fullName;
	}
	
	
	public int getSpecialType(){
		return SpecialType;
	}

	public boolean isElite() {
		return elite;
	}

	public void setElite(boolean elite) {
		if(elite){
			SpecialName = "Elite";
		}else{
			SpecialName = "";
		}
		this.elite = elite;
	}

	public boolean isRaging() {
		return raging;
	}

	public void setRaging(boolean raging) {
		this.raging = raging;
	}

}