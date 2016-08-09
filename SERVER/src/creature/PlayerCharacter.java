/************************************
 * 									*
 *		SERVER / CHARACTER			*
 *									*
 ************************************/	

package creature;
import game.ServerSettings;
import network.Client;
import network.Server;
import game.Database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;

import player_classes.BaseClass;
import utils.DateCalc;
import utils.GameInfo;
import utils.ServerGameInfo;
import utils.TimeUtils;
import utils.XPTables;
import components.ActionBar;
import components.Quest;
import components.JobSkill;
import components.Ship;
import creature.Creature.CreatureType;
import components.Crew;
import data_handlers.Handler;
import data_handlers.ability_handler.Ability;
import data_handlers.ability_handler.AbilityHandler;
import data_handlers.battle_handler.BattleHandler;
import data_handlers.battle_handler.PvpHandler;
import data_handlers.card_handler.Card;
import data_handlers.card_handler.CardHandler;
import data_handlers.item_handler.Item;
import data_handlers.party_handler.Party;

public class PlayerCharacter extends Creature {

	public int blueSagaId;
	public Client client;
	
	private int BaseCreatureId;
	
	private BaseClass baseClass;
	private BaseClass primaryClass;
	private BaseClass secondaryClass;
	
	private HashMap<Integer,BaseClass> playerClasses;
	
	// Attack cooldown for ranged classes
	private int rangedAttackCooldown = 7;
	private int rangedAttackItr = 0;
	
	private ActionBar ActionBar;

	private CopyOnWriteArrayList<Quest> Quests = new CopyOnWriteArrayList<Quest>(); 

	private int Bounty;

	private int CheckpointId;

	private Vector<Integer> FriendsList;

	private int AdminLevel = 0;

	private int areaEffectId;

	private Item MouseItem;

	private int inventorySize = 4;

	private Item[][] Inventory = new Item[inventorySize][inventorySize];

	private int tutorialNr = 0;

	private Party party;

	// Chat channels subscriptions
	private Vector<String> chatChannels;
	
	// SHIP
	private Ship ship;
	protected Crew Crew;
	
	// PK
	private int pkMarker = 0;
	private int PlayerKillerTime;
	
	// Card book
	private Vector<Card> cardBook = new Vector<Card>();
	
	private String lastOnline = "";
	

	public PlayerCharacter(Client client, int charType, int newX, int newY, int newZ) {
		super(charType, newX, newY, newZ);

		this.client = client;

		Level = 1;

		FriendsList = new Vector<Integer>();

		ActionBar = new ActionBar();

		Inventory = new Item[inventorySize][inventorySize];

		Crew = new Crew(0);
		
		chatChannels = new Vector<String>();
		
		setMouseItem(null);
	}


	public void load(int charId, Client client){
		dbId = charId;

		// LOAD LOCATION AND GOLD
		ResultSet rs = Server.userDB.askDB("select * from user_character where Id = "+dbId);

		try {
			if(rs.next()){
				blueSagaId = rs.getInt("BlueSagaId");
				lastOnline = rs.getString("LastOnline");
				
				Bounty = rs.getInt("Bounty");
				CheckpointId = rs.getInt("CheckpointId");
				setPkMarker(rs.getInt("PlayerKiller"));

				setName(rs.getString("Name"));
				setAdminLevel(rs.getInt("AdminLevel"));

				setBaseCreatureId(rs.getInt("BaseCreatureId"));
				
				if(getPkMarker() > 0 && getAdminLevel() != 5){
					startPlayerKillerTimer();
				}

				Ship newShip = new Ship(rs.getInt("ShipId")); 
				setShip(newShip);

				// LOAD CLASSES
				loadClasses(rs.getInt("BaseClassId"));
				
				loadStats(dbId);

				client.Muted = rs.getString("Muted");
				
				// LOAD SKILLS
				loadSkills();

				// LOAD ABILITIES
				loadAbilities();

				// LOAD INVENTORY AND EQUIP
				inventorySize = rs.getInt("InventorySize");
				loadInventory();

				// LOAD QUESTS
				loadQuests();

				// LOAD FRIENDSLIST
				loadFriendsList(client);
				
				// LOAD CARD BOOK
				loadCardBook(client);
				
				// LOAD CREW INFO
				ResultSet crewRS = Server.userDB.askDB("select crew.Id as crewId, crew.Name as crewName, character_crew.MemberState as memberState from crew, character_crew where CharacterId = "+getDBId()+" and character_crew.CrewId = crew.Id");
				if(crewRS.next()){
					Crew = new Crew(crewRS.getInt("crewId"));
					Crew.setName(crewRS.getString("crewName"));
					Crew.setMemberState(crewRS.getString("memberState"));
				}else{
					Crew = new Crew(0);
				}
				crewRS.close();

				setTutorialNr(rs.getInt("TutorialNr"));
			}
			rs.close();
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}

  public void saveInfo() {

    StringBuilder statement = new StringBuilder(1000);

    statement.append("update user_character set ")
             .append("Bounty= ").append(Bounty)
             .append(", PlayerKiller = ").append(pkMarker)
             .append(", Level = ").append(Level)
             .append(", XP = ").append(XP)
             .append(", HEALTH = ").append(Health)
             .append(", MANA = ").append(Mana)
             .append(", HeadSkinId = ").append(getCustomization().getHeadSkinId())
             .append(", WeaponSkinId = ").append(getCustomization().getWeaponSkinId())
             .append(", OffHandSkinId = ").append(getCustomization().getOffHandSkinId())
             .append(", AmuletSkinId = ").append(getCustomization().getAmuletSkinId())
             .append(", ArtifactSkinId = ").append(getCustomization().getArtifactSkinId())
             .append(" where Id = ").append(dbId);

    Server.userDB.updateDB(statement.toString());
  }


	public String getFullData(){
		int ShipId = 0;
		if(getShip() != null){
			if(getShip().isShow()){
				ShipId = getShip().getShipId();
			}else{
			}
		}

		String playerData = super.getFullData()+","+ShipId+","+Crew.getId()+","+Crew.getName()+","+Crew.getMemberState()+","+ getPkMarker()+","+getAdminLevel()+","+GameInfo.getBountyRankId(Bounty);

		return playerData;
	}


	/****************************************
	 *                                      *
	 *             NETWORK	                *
	 *                                      *
	 *                                      *
	 ****************************************/

	public String getInfo(){
		// x, y, z, 
		StringBuilder info = new StringBuilder(1000);
		info.append(X).append(',')
		    .append(Y).append(',')
		    .append(Z).append(',')
		    .append(Bounty).append(',')
		    .append(PlayerKillerTime)
		
		    .append(':')
		
		    .append(CreatureId).append(',')
		    .append(Name).append(',')
		    .append(Family).append(',')
		    .append(AttackType).append(',')
		    .append(Level).append(',')
		    .append(XP).append(',')

		    .append(Stats.getValue("STRENGTH")).append(',')
		    .append(Stats.getValue("INTELLIGENCE")).append(',')
		    .append(Stats.getValue("AGILITY")).append(',')
		    .append(Stats.getValue("SPEED")).append(',')
		
		    .append(Stats.getValue("CRITICAL_HIT")).append(',')
		    .append(Stats.getValue("EVASION")).append(',')
		    .append(Stats.getValue("ACCURACY")).append(',')

		    .append(Stats.getValue("MAX_HEALTH")).append(',')
		    .append(Health).append(',')
		    .append(Stats.getValue("MAX_MANA")).append(',')
		    .append(Mana).append(',')

		    .append(Stats.getValue("FIRE_DEF")).append(',')
		    .append(Stats.getValue("COLD_DEF")).append(',')
		    .append(Stats.getValue("SHOCK_DEF")).append(',')
		    .append(Stats.getValue("CHEMS_DEF")).append(',')
		    .append(Stats.getValue("MIND_DEF")).append(',')
		    .append(Stats.getValue("MAGIC_DEF")).append(',')

		    .append(Stats.getValue("ARMOR")).append(',')

		    .append(nextXP).append(',')

		    .append(dbId).append(',')

		    .append(getAttackRange()).append(',')
		    .append(getHealthStatus()).append(',')

		// CREW INFO
		    .append(Crew.getId()).append(',')
		    .append(Crew.getName()).append(',')
		    .append(Crew.getMemberState()).append(',');

		// SHIP INFO
		int shipId = 0;

		if(getShip() != null){
			shipId = getShip().getShipId();
		}
		info.append(shipId).append(',')
		
		    .append(getPkMarker()).append(',')
		
		// Classes info
		    .append(baseClass.id).append(',');
		
		for(BaseClass baseClass: ServerGameInfo.classDef.values()){
			if(baseClass.available){
				BaseClass playerClass = playerClasses.get(baseClass.id);
				info.append(playerClass.level).append(',')
				    .append(playerClass.getXp()).append(',')
				    .append(playerClass.nextXP);
				if(primaryClass != null && primaryClass.id == playerClass.id){
					info.append(",1,");
				}else if(secondaryClass != null && secondaryClass.id == playerClass.id){
					info.append(",2,");
				}else{
					info.append(",0,");
				}
			}
		}
		
		info.append(getAdminLevel()).append('/');

		// SEND EQUIPMENT INFO

		Item HeadItem = getEquipment("Head");
		Item WeaponItem = getEquipment("Weapon");
		Item OffHandItem = getEquipment("OffHand");
		Item AmuletItem = getEquipment("Amulet");
		Item ArtifactItem = getEquipment("Artifact");

		if(HeadItem != null){
			info.append(HeadItem.getId()).append(',')
					.append(getCustomization().getHeadSkinId()).append(',')
					.append(HeadItem.getClassId()).append(',');
		}else{
			info.append("0,")
			    .append(getCustomization().getHeadSkinId())
			    .append(",0,");
		}

		if(WeaponItem != null){
			info.append(WeaponItem.getId()).append(',')
					.append(getCustomization().getWeaponSkinId()).append(',')
					.append(WeaponItem.getClassId()).append(',');
		}else{
			info.append("0,")
			    .append(getCustomization().getWeaponSkinId())
			    .append(",0,");
		}

		if(OffHandItem != null){
			info.append(OffHandItem.getId()).append(',')
					.append(getCustomization().getOffHandSkinId()).append(',')
					.append(OffHandItem.getClassId()).append(',');
		}else{
			info.append("0,")
			    .append(getCustomization().getOffHandSkinId())
			    .append(",0,");
		}

		if(AmuletItem != null){
			info.append(AmuletItem.getId()).append(',')
					.append(getCustomization().getAmuletSkinId()).append(',')
					.append(AmuletItem.getClassId()).append(',');
		}else{
			info.append("0,")
			    .append(getCustomization().getAmuletSkinId())
			    .append(",0,");
		}

		if(ArtifactItem != null){
			info.append(ArtifactItem.getId()).append(',')
					.append(getCustomization().getArtifactSkinId()).append(',')
					.append(ArtifactItem.getClassId()).append(',');
		}else{
			info.append("0,")
			    .append(getCustomization().getArtifactSkinId())
			    .append(",0,");
		}

		info.append(getCustomization().getMouthFeatureId()).append(',')
		    .append(getCustomization().getAccessoriesId()).append(',')
		    .append(getCustomization().getSkinFeatureId());

		return info.toString();
	}

	// INFO TO SEND TO OTHER CLIENTS
	public String getSmallInfo(){
		return X + "," + Y + ',' + getEquipmentInfo() + ',' + dbId + ','
		     + Bounty + ((PlayerKillerTime > 0) ? ",1" : ",0");
	}

	// POSITION INFO
	public String getPosition() {
		return X+","+Y;
	}

	
	/****************************************
	 *                                      *
	 *          LOAD SKILLS		            *
	 *                                      *
	 ****************************************/
	
	public void loadClasses(int baseClassId){

		baseClass = null;
		primaryClass = null;
		secondaryClass = null;
		
		// Add base classes
		for(int classId = 1; classId < 4; classId++){

		}
	
		playerClasses = new HashMap<Integer,BaseClass>();
		for(BaseClass defClass: ServerGameInfo.classDef.values()){
			if(defClass.available){
				boolean hasClass = false;
				ResultSet classCheck = Server.userDB.askDB("select Id from character_class where ClassId = "+defClass.id+" and CharacterId = "+client.playerCharacter.getDBId());
				try {
					if(classCheck.next()){
						hasClass = true;
					}
					classCheck.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				if(!hasClass){
					Server.userDB.updateDB("insert into character_class (CharacterId, ClassId, ClassLevel, ClassXP, Type) values ("+client.playerCharacter.getDBId()+","+defClass.id+",1,0,0)");
				}
				
				BaseClass playerClass = new BaseClass(defClass);
				playerClasses.put(defClass.id,playerClass);
			}
		}
		
		baseClass = playerClasses.get(baseClassId);
		
		ResultSet rs = Server.userDB.askDB("select * from character_class where CharacterId = "+dbId);

		try {
			while(rs.next()){
				int classId = rs.getInt("ClassId");

				// Which type of class to load
				if(playerClasses.containsKey(classId)){
					playerClasses.get(classId).level = rs.getInt("ClassLevel");
					playerClasses.get(classId).setXp(rs.getInt("ClassXP"));
					playerClasses.get(classId).nextXP = GameInfo.classNextXP.get(rs.getInt("ClassLevel")+1);
		
					if(rs.getInt("Type") == 1){
						primaryClass = playerClasses.get(classId);
					}else if(rs.getInt("Type") == 2){
						secondaryClass = playerClasses.get(classId);
					}
				}
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/****************************************
	 *                                      *
	 *          LOAD SKILLS		            *
	 *                                      *
	 ****************************************/

	public void loadSkills(){
		
		jobSkills.clear();

		ResultSet rs = Server.userDB.askDB("select * from character_skill where CharacterId = "+dbId);

		try {
			while(rs.next()){
				int skillId = rs.getInt("SkillId");
				
				if(ServerGameInfo.skillDef.containsKey(skillId)){
					JobSkill newSkill = new JobSkill(ServerGameInfo.skillDef.get(skillId));

					newSkill.setLevel(rs.getInt("Level"));
					newSkill.setSP(rs.getInt("SP"));

					jobSkills.put(newSkill.getId(),newSkill);
				}
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// Check if some skills are missing
		for(JobSkill s: ServerGameInfo.skillDef.values()){
			if(!jobSkills.containsKey(s.getId())){
				// PLAYER DOESN'T HAVE SKILL, ADD IT WITH 0 SP AND LEVEL 1
				Server.userDB.updateDB("insert into character_skill (SkillId, CharacterId, SP, Level) values("+s.getId()+","+dbId+",0,1)");

				JobSkill newSkill = new JobSkill(s);

				newSkill.setLevel(1);
				newSkill.setSP(0);
				
				jobSkills.put(newSkill.getId(),newSkill);
			}
		}
	}


	/****************************************
	 *                                      *
	 *          LOAD ABILITIES              *
	 *                                      *
	 ****************************************/

	public void loadAbilities(){
		ResultSet rs = Server.userDB.askDB("select * from character_ability where CharacterId = "+dbId);

		abilities = null;

		List<Integer> abilitiesToRemove = new ArrayList<Integer>();
		
		boolean hasSoulStone = false;
		
		try {
			while(rs.next()){
				Ability newAB = AbilityHandler.getAbility(rs.getInt("AbilityId"));
				if(newAB != null){
					// Check if player can use ability
					if(newAB.getClassId() == 0 || hasClass(newAB.getClassId()) || newAB.getFamilyId() == getFamilyId() || newAB.getAbilityId() == 31){
						if(newAB.getAbilityId() == 31){
							hasSoulStone = true;
						}
						newAB.setDbId(rs.getInt("Id"));
						newAB.setCooldownLeft(rs.getInt("CooldownLeft"));
						newAB.setCaster(CreatureType.Player,this);
						addAbility(newAB);
					}else{
						abilitiesToRemove.add(newAB.getAbilityId());
					}
				}
			}
			rs.close();
		}catch (SQLException e) {
			e.printStackTrace();
		}
		
		if(getAdminLevel() == 5){
			addAbility(AbilityHandler.getAbility(31));
			addAbility(AbilityHandler.getAbility(39));
			addAbility(AbilityHandler.getAbility(83));
			addAbility(AbilityHandler.getAbility(84));
			addAbility(AbilityHandler.getAbility(54));
		}
		
		// Check if soul stone should be added
		if(!hasSoulStone){
			ResultSet shopCheck = Server.userDB.askDB("select SoulStone from user_settings where UserId = "+client.UserId);
			try {
				if(shopCheck.next()){
					if(shopCheck.getInt("SoulStone") == 1){
						// Add soul stone!
						Server.userDB.updateDB("insert into character_ability (CharacterId,AbilityId,CooldownLeft) values ("+getDBId()+",31,0)");
						addAbility(AbilityHandler.getAbility(31));
					}
				}
				shopCheck.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		// Remove incorrect abilities
		for(Integer abilityId: abilitiesToRemove){
			Server.userDB.updateDB("delete from character_ability where CharacterId = "+dbId+" and AbilityId = "+abilityId);
			Server.userDB.updateDB("delete from character_actionbar where CharacterId = "+dbId+" and ActionType = 'Ability' and ActionId = "+abilityId);
		}
	}

	/****************************************
	 *                                      *
	 *             CARD BOOK                *
	 *                                      *
	 ****************************************/
	public Vector<Card> getCardBook(){
		return cardBook;
	}

	public void loadCardBook(Client client){
		cardBook.clear();
		
		ResultSet cardsInfo = Server.userDB.askDB("SELECT CardId FROM character_card WHERE CharacterId = "+client.playerCharacter.getDBId()+" ORDER BY CardId ASC");
		try {
			while(cardsInfo.next()){
				cardBook.add(CardHandler.card_ids.get(cardsInfo.getInt("CardId")));
			}
			cardsInfo.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		// Check how long ago player was logged in
		// If more than 24 hours, lose a random card
		
		int nrDaysOffline = DateCalc.daysBetween(lastOnline,TimeUtils.now());
		while(nrDaysOffline > 0){
			// Remove a card
			Item droppedCardItem = CardHandler.playerDropCard(this.client);
			if(droppedCardItem != null){
				Handler.addOutGoingMessage(client, "message", droppedCardItem.getName()+" #messages.cards.lost_card");
			}
			nrDaysOffline--;
		}
	
	}
	


	/*******************************************
	 * 
	 * 				FRIENDSLIST
	 * 
	 ******************************************/

	public void loadFriendsList(Client client){
		FriendsList.clear();
		ResultSet friends = Server.userDB.askDB("select FriendCharacterId from user_friend where UserId = "+client.UserId+" order by Date asc");
		try {
			while(friends.next()){
				FriendsList.add(friends.getInt("FriendCharacterId"));
			}
			friends.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	public Vector<Integer> getFriendsList(){
		return FriendsList;
	}

	/****************************************
	 *                                      *
	 *             INVENTORY	            *
	 *                                      *
	 *                                      *
	 ****************************************/

	public boolean hasCopper(int copper){
		int myCopper = 0;

		for(int i = 0; i < inventorySize; i++){
			for(int j = 0; j < inventorySize; j++){
				if(Inventory[i][j] != null){
					if(Inventory[i][j].getId() == 36){
						myCopper += Inventory[i][j].getStacked();
					}
					if(Inventory[i][j].getId() == 35){
						myCopper += Inventory[i][j].getStacked()*100;
					}
					if(Inventory[i][j].getId() == 34){
						myCopper += Inventory[i][j].getStacked()*10000;
					}
					if(myCopper >= copper){
						return true;
					}
				}
			}
		}
		return false;
	}

	public void loadInventory() {
		ResultSet rs = Server.userDB.askDB("select Id, ItemId, InventoryPos, Equipped, Nr, ModifierId, MagicId from character_item where CharacterId = "+dbId+" order by Id desc");


		Item tempItem;

		unequipItem("Head");
		unequipItem("Weapon");
		unequipItem("OffHand");
		unequipItem("Amulet");
		unequipItem("Artifact");

		Inventory = new Item[inventorySize][inventorySize];

		try {
			while(rs.next()){
				if(ServerGameInfo.itemDef.containsKey(rs.getInt("ItemId"))){
					tempItem = new Item(ServerGameInfo.itemDef.get(rs.getInt("ItemId")));
					tempItem.setUserItemId(rs.getInt("Id"));
					tempItem.setStacked(rs.getInt("Nr"));
					tempItem.setModifierId(rs.getInt("ModifierId"));
					tempItem.setMagicId(rs.getInt("MagicId"));


					if(rs.getInt("Equipped") == 1){
						tempItem.equip();
						equipItem(tempItem);
					}else if(rs.getString("InventoryPos").equals("Mouse")){
						setMouseItem(tempItem);
					}else{
						String invPos[] = rs.getString("InventoryPos").split(",");
						int posX = Integer.parseInt(invPos[0]);
						int posY = Integer.parseInt(invPos[1]);
						if(posX < inventorySize && posY < inventorySize){
							Inventory[posX][posY] = tempItem;
						}
					}
				}
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		updateBonusStats();
	}

	public boolean isInventoryFull(Item itemType) {
		for(int i = 0; i < inventorySize; i++){
			for(int j = 0; j < inventorySize; j++){
				if(itemType != null && Inventory[i][j] != null){
					if(Inventory[i][j].getType().equals(itemType.getType()) && Inventory[i][j].getStacked() < itemType.getStackable()){
						return false;
					}
				}else if(Inventory[i][j] == null){
					return false;
				}
			}
		}
		return true;
	}

	public Item getInventoryItem(int posX, int posY){
		return Inventory[posX][posY];
	}

	public void setInventoryItem(int posX, int posY,Item newItem){
		Inventory[posX][posY] = newItem;
	}

	public String hasItem(int itemId){
		for(int i = 0; i < inventorySize; i++){
			for(int j = 0; j < inventorySize; j++){
				if(Inventory[i][j] != null){
					if(Inventory[i][j].getId() == itemId){
						return i+","+j;
					}
				}
			}
		}
		return "no";
	}

	public boolean hasItem(int itemId, int nr){
		int itemCount = nr;
		for(int i = 0; i < inventorySize; i++){
			for(int j = 0; j < inventorySize; j++){
				if(Inventory[i][j] != null){
					if(Inventory[i][j].getId() == itemId){
						itemCount -= Inventory[i][j].getStacked();
						if(itemCount <= 0){
							return true;
						}
					}
				}
			}
		}
		return false;
	}


	/****************************************
	 *                                      *
	 *             QUEST		            *
	 *                                      *
	 *                                      *
	 ****************************************/

	public void loadQuests() {
		ResultSet rs = Server.userDB.askDB("select * from character_quest where CharacterId = "+dbId+" order by QuestId asc");

		Quests.clear();

		Quest tempQuest;

		try {
			while(rs.next()){
				tempQuest = new Quest(ServerGameInfo.questDef.get(rs.getInt("QuestId")));
				tempQuest.setStatus(rs.getInt("Status"));

				Quests.add(tempQuest);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void addQuest(int QuestId) {
		Server.userDB.updateDB("insert into character_quest (QuestId, CharacterId, Status) values("+QuestId+","+dbId+",1)");

		loadQuests();
	}


	public ArrayList<String> checkQuests(Database gameDB) {
		ArrayList<String> messages = new ArrayList<String>();

		for(int i = 0; i < Quests.size(); i++){
			// KILL X CREATURES X

			if(Quests.get(i).getType().equals("Kill X creature X")){

				ResultSet rs = null;

				// CHECK IF KILLED ENOUGH MONSTERS OF RIGHT KIND
				if(Quests.get(i).getTargetType().equals("Creature")){
					rs = gameDB.askDB("select Kills from character_kills where CharacterId = "+dbId+" and CreatureId = "+Quests.get(i).getTargetId());

					try {
						if(rs.next()){

							// IF UNCOMPLETED QUEST
							if(Quests.get(i).getStatus() == 0){
								if(rs.getInt("Kills") >= Quests.get(i).getTargetNumber()){
									messages.add("Completed Quest\n'"+Quests.get(i).getName()+"'!");
									gameDB.updateDB("update character_quest set Completed = 1 where QuestId = "+Quests.get(i).getId()+" and CharacterId = "+dbId);
								}
							}
						}
						rs.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}


			}

		}

		return messages;
	}


	public CopyOnWriteArrayList<Quest> getQuests(){
		return Quests;
	}

	public Quest getQuestById(int questId){
		for(Quest q: Quests){
			if(q.getId() == questId){
				return q;
			}
		}
		return null;
	}


	/****************************************
	 *                                      *
	 *             GETTER/SETTER            *
	 *                                      *
	 *                                      *
	 ****************************************/




	public String getAbilitiesAsString(){

		// nrAbilities; Id,Name,ClassId,ManaCost,Cooldown,CooldownLeft,Range,Price,TargetSelf,Instant,EquipReq 
		String AbilityInfo = ""+abilities.size();

		for(Ability A : abilities){
			AbilityInfo += "/"+A.getAbilityId()+"="+A.getName()+"="+A.getClassId()+"="+A.getColor().getRed()+"="+A.getColor().getGreen()+"="+A.getColor().getBlue()+"="+A.getManaCost()+"="+A.getCooldown()+"="+A.getCooldownLeft()+"="+A.getRange()+"="+A.getPrice()+"="+A.isTargetSelf()+"="+A.isInstant()+"="+A.getEquipReq()+"="+A.getGraphicsNr()+"="+A.getAoE();
		}

		return AbilityInfo;
	}


	public void setCheckpointId(int newCheckpointId){
		CheckpointId = newCheckpointId;
	}

	public int getCheckpointId(){
		return CheckpointId;
	}


	/*
	public int getNrQuests() {
		return Quests.size();
	}
	 */





	public void setBounty(int newBounty){
		Bounty = newBounty;
	}

	public int getBounty() {
		return Bounty;
	}

	public void changeBounty(int change){
		Bounty += change;
		if(Bounty < 0){
			Bounty = 0;
		}
	}



	public void loadStats(int characterId){

		dbId = characterId;

		// Load Stats
		ResultSet rs = Server.userDB.askDB("select * from user_character where Id = "+dbId);

		try {
			while(rs.next()){

				Level = rs.getInt("Level");
				XP = rs.getInt("XP");

				if(Level > ServerSettings.LEVEL_CAP){
					Level = ServerSettings.LEVEL_CAP;
					XP = 0;
				}
				
				Stats.reset();
				
				for (Entry<String, Integer> entry : baseClass.getStartStats().getHashMap().entrySet()) {
				    String key = entry.getKey();
				    int value = entry.getValue();
				    
				    Stats.setValue(key, value + (Level - 1) * baseClass.getLevelStats().getValue(key));
				}
				
				Name = rs.getString("Name");

				Health = rs.getInt("HEALTH");
				Mana = rs.getInt("MANA");

				if(Health < 1){
					Health = getStat("MAX_HEALTH");
					Mana = getStat("MAX_MANA");
				}
				if(Mana < 0){
					Mana = 0;
				}
				
				getCustomization().setMouthFeatureId(rs.getInt("MouthFeatureId"));
				getCustomization().setAccessoriesId(rs.getInt("AccessoriesId"));
				
				getCustomization().setSkinFeatureId(rs.getInt("SkinFeatureId"));

				getCustomization().setHeadSkinId(rs.getInt("HeadSkinId"));
				getCustomization().setWeaponSkinId(rs.getInt("WeaponSkinId"));
				getCustomization().setOffHandSkinId(rs.getInt("OffHandSkinId"));
				getCustomization().setAmuletSkinId(rs.getInt("AmuletSkinId"));
				getCustomization().setArtifactSkinId(rs.getInt("ArtifactSkinId"));

			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		if(XPTables.nextLevelXP.containsKey(Level+1)){
			// Get xp info
			nextXP = XPTables.nextLevelXP.get(Level+1);
		}

		if(XP >= nextXP){
			XP -= nextXP;
			levelUp();
		}

		saveInfo();
	}	

	public void loadActionBar(){

		ResultSet rs = Server.userDB.askDB("select * from character_actionbar where CharacterId = "+dbId+" order by OrderNr ASC");

		ActionBar = new ActionBar();

		try {
			while(rs.next()){
				if(rs.getString("ActionType").equals("Ability")){
					ActionBar.setAbility(rs.getInt("OrderNr"), getAbilityById(rs.getInt("ActionId")));
				}


			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}


	}

	public String getActionBarString(){
		String actionbarString = "";

		for(int i = 0; i < 10; i++){
			actionbarString += ActionBar.getAction(i).getActionType() + ",";
			actionbarString += ActionBar.getAction(i).getActionId() + ";";
		}

		return actionbarString;
	}


	public void setCharacterId(int newId){
		dbId = newId;
	}


	public boolean hasClass(int classId){
		
		if(primaryClass != null){
			if(primaryClass.id == classId){
				return true;
			}
			if(primaryClass.baseClassId == classId){
				return true;
			}
		}
		
		if(secondaryClass != null){
			if(secondaryClass.id == classId){
				return true;
			}
			if(secondaryClass.baseClassId == classId){
				return true;
			}
		}
		if(baseClass != null){
			if(baseClass.id == classId){
				return true;
			}
		}
		return false;
	}
	
	public BaseClass getClassById(int classId){
		return playerClasses.get(classId);
	}
	
	/**
	 * Gets level of class with id classId, returns 0 if class not available
	 * @param classId
	 * @return
	 */
	public int getClassLevel(int classId){
		if(primaryClass != null){
			if(primaryClass.id == classId){
				return primaryClass.level;
			}
		}
		if(secondaryClass != null){
			if(secondaryClass.id == classId){
				return secondaryClass.level;
			}
		}
		if(baseClass != null){
			if(baseClass.id == classId){
				return baseClass.level;
			}
		}
		
		return 0;
	}
	
	/****************************************
	 *                                      *
	 *             XP / LEVEL UP		  	*
	 *                                      *
	 *                                      *
	 ****************************************/

	public boolean loseXP(int lostXP, Client client){

		boolean loseLevel = false;

		int totalXP = 0;
		totalXP = XPTables.totalLevelXP.get(Level);


		totalXP += XP;


		totalXP -= lostXP;

		// LOSE XP
		int newLevel = XPTables.getLevelByXP(totalXP);
		int levelXP = 0;

		if(newLevel > 1){
			levelXP = XPTables.totalLevelXP.get(newLevel);
		}


		int newXP =  totalXP - levelXP;

		setXP(newXP);

		// LOSE STATS IF LEVEL DECREASED
		if(newLevel < Level){
			setLevel(newLevel);

			String levelDownData = levelDown();
			if(client.Ready){
				Handler.addOutGoingMessage(client, "level_down", levelDownData);
			}

			loseLevel = true;
		}else{
			if(client.Ready){
				Handler.addOutGoingMessage(client, "set_xp", ""+newXP);
			}
		}
		return loseLevel;
	}

	public void loseSP(int skillId, Client client){

		JobSkill mySkill = getSkill(skillId);

		if(mySkill != null){
			int levelSP = 0;

			if(mySkill.getLevel() > 1){
				levelSP = XPTables.totalLevelSP.get(mySkill.getLevel());
			}

			int totalSkillSP = levelSP + mySkill.getSP();
			int lostSP = (int) Math.ceil(totalSkillSP * 0.01f);

			totalSkillSP -= lostSP;

			// LOSE XP
			int newLevel = XPTables.getLevelBySP(totalSkillSP);

			int newLevelSP = 0;
			if(newLevel > 1){
				newLevelSP = XPTables.totalLevelSP.get(newLevel);
			}

			int newSP =  totalSkillSP - newLevelSP;

			mySkill.setSP(newSP);

			int retrieveSP = (int) Math.ceil(lostSP * 0.5f);

			Server.userDB.updateDB("update character_skill set Level = "+newLevel+", SP = "+newSP+" where SkillId = "+skillId+" and CharacterId = "+client.playerCharacter.getDBId());
			Server.userDB.updateDB("insert into character_soul_sp (SkillId, CharacterId, SP) values ("+skillId+","+client.playerCharacter.getDBId()+","+retrieveSP+")");

			// LOSE STATS IF LEVEL DECREASED
			if(newLevel < mySkill.getLevel()){
				mySkill.setLevel(newLevel);
				if(client.Ready){
					Handler.addOutGoingMessage(client, "skill_down", skillId+";"+newLevel+";"+newSP);
				}
			}else{
				if(client.Ready){
					Handler.addOutGoingMessage(client, "set_sp", ""+skillId+","+newSP);
				}
			}
		}
	}



	public boolean addXP(float newXP) {
		boolean levelUp = false;
		if(Level < ServerSettings.LEVEL_CAP){
			XP += newXP;

			while(XP >= nextXP){
				if(Level < ServerSettings.LEVEL_CAP){
					XP = XP - nextXP;
					levelUp = true;
					Level++;
					nextXP = XPTables.nextLevelXP.get(Level+1);	
				}
			}
		}
		return levelUp;
	}

	public int getXPBarWidth(int Max) {
		float XPBarWidth = (XP / nextXP) * Max;
		return Math.round(XPBarWidth);
	}

	public String levelDown(){

		String levelDownData = "";

		
		for (Entry<String, Integer> entry : ServerGameInfo.classDef.get(baseClass.id).getLevelStats().getHashMap().entrySet()) {
		    String key = entry.getKey();
		    int value = entry.getValue();
		    
		    Stats.setValue(key, getRawStat(key)-value);
		}
		
		
		updateBonusStats();

		Health = getStat("MAX_HEALTH");
		Mana = getStat("MAX_MANA");

		levelDownData += getStat("STRENGTH")+","+getStat("INTELLIGENCE")+","+getStat("AGILITY")+",";
		levelDownData += getStat("MAX_HEALTH")+",";
		levelDownData += getStat("MAX_MANA")+",";
		levelDownData += getStat("SPEED")+",";
		levelDownData += getStat("ARMOR")+",";

		levelDownData += Level+",";

		nextXP = XPTables.nextLevelXP.get(Level+1);
		levelDownData += Math.round(nextXP)+",";
		levelDownData += XP;

		return levelDownData;
	}


	public String levelUp(){

		String levelUpData = "";

		for (Entry<String, Integer> entry : ServerGameInfo.classDef.get(baseClass.id).getLevelStats().getHashMap().entrySet()) {
		    String key = entry.getKey();
		    int value = entry.getValue();
		    int startValue = ServerGameInfo.classDef.get(baseClass.id).getStartStats().getValue(key);
		    
		    Stats.setValue(key, startValue + (Level - 1) * value);
		}
		
		Health = getRawStat("MAX_HEALTH");
		Mana = getRawStat("MAX_MANA");


		levelUpData += getRawStat("STRENGTH")+","+getRawStat("INTELLIGENCE")+","+getRawStat("AGILITY")+",";
		levelUpData += getRawStat("MAX_HEALTH")+",";
		levelUpData += getRawStat("MAX_MANA")+",";
		levelUpData += getRawStat("SPEED")+",";
		levelUpData += getRawStat("ARMOR")+",";

		levelUpData += Level+",";

		nextXP = XPTables.nextLevelXP.get(Level+1);
		levelUpData += Math.round(nextXP)+",";
		levelUpData += XP;

		return levelUpData;
	}


	/****************************************
	 *                                      *
	 *             MOVEMENT  	            *
	 *                                      *
	 *                                      *
	 ****************************************/


	public void teleportTo(int newX, int newY){
		X = newX;
		Y = newY;
	}



	/**
	 * PLAYER KILLER PK
	 * @return
	 */


	public int getPkMarker() {
		return pkMarker;
	}

	public void setPkMarker(int pkMarker) {
		this.pkMarker = pkMarker;
	}

	public void startPlayerKillerTimer(){
		if(getPkMarker() == 0){
			PlayerKillerTime = 0;
		}else if(getPkMarker() == 1){
			PlayerKillerTime = BattleHandler.playerHitTime;
		}else{
			PlayerKillerTime = PvpHandler.playerKillerTime;
		}

	}
	
	public boolean canEquip(Item equipItem){
		boolean equipOk = super.canEquip(equipItem);
		
		if(equipOk && equipItem.getClassId() > 0){
			if(!hasClass(equipItem.getClassId())){
				equipOk = false;
			}
		}
		return equipOk;
	}

	public boolean countdownPKtime(){
		if(PlayerKillerTime > 0){
			PlayerKillerTime--;
			if(PlayerKillerTime == 0){
				return true;
			}
		}
		return false;
	}

	public void walkTo(int newX, int newY, int newZ){
		super.walkTo(newX, newY, newZ);
	}
	
	public void resetRangedAttackCooldown(boolean diagonalMove){
		if(diagonalMove){
			float rangedAttackItrf = (float) (Math.sqrt(2)*(rangedAttackCooldown));
			rangedAttackItr = Math.round(rangedAttackItrf);
		}else{
			rangedAttackItr = rangedAttackCooldown;
		}
	}
	
	public boolean isRangedAttackReady(){
		if(rangedAttackItr <= 0){
			return true;
		}	
		return false;
	}
	
	public void cooldownRangedAttack(){
		if(rangedAttackItr > 0){
			rangedAttackItr--;
		}	
	}
	
	/**
	 * Getters and setters
	 * @return
	 */

	
	
	public int getXP() {
		return XP;
	}

	public int getNextXP() {
		return nextXP;
	}

	public void setXP(int newXP){
		XP = newXP;
	}

	public void setLevel(int newLevel){
		Level = newLevel;

		// CALCULATE NEW STATS
	}
	
	public int getPlayerKillerTime() {
		return PlayerKillerTime;
	}


	public void setPlayerKillerTime(int playerKillerTime) {
		PlayerKillerTime = playerKillerTime;
	}


	public int getAreaEffectId() {
		return areaEffectId;
	}


	public void setAreaEffectId(int areaEffectId) {
		this.areaEffectId = areaEffectId;
	}

	public Ship getShip() {
		return ship;
	}


	public void setShip(Ship ship) {
		this.ship = ship;
	}

	
	public void setCrew(Crew newCrew){
		Crew = newCrew;
	}
	
	public Crew getCrew(){
		return Crew;
	}


	public BaseClass getBaseClass(){
		return baseClass;
	}
	
	public void setBaseClass(BaseClass baseClass){
		this.baseClass = baseClass;
	}

	
	public BaseClass getSecondaryClass() {
		return secondaryClass;
	}


	public void setSecondaryClass(BaseClass secondaryClass) {
		this.secondaryClass = secondaryClass;
	}


	public BaseClass getPrimaryClass() {
		return primaryClass;
	}


	public void setPrimaryClass(BaseClass primaryClass) {
		this.primaryClass = primaryClass;
	}



	public Item getMouseItem() {
		return MouseItem;
	}


	public void setMouseItem(Item mouseItem) {
		MouseItem = mouseItem;
	}





	public int getAdminLevel() {
		return AdminLevel;
	}


	public void setAdminLevel(int adminLevel) {
		AdminLevel = adminLevel;
	}

	public int getInventorySize() {
		return inventorySize;
	}


	public void setInventorySize(int inventorySize) {
		this.inventorySize = inventorySize;
	}


	public int getTutorialNr() {
		return tutorialNr;
	}


	public void setTutorialNr(int tutorialNr) {
		this.tutorialNr = tutorialNr;
	}


	public Party getParty() {
		return party;
	}



	public void setParty(Party party) {
		this.party = party;
	}

	public int getBaseCreatureId() {
		return BaseCreatureId;
	}

	public void setBaseCreatureId(int baseCreatureId) {
		BaseCreatureId = baseCreatureId;
	}
	
	/**
	 * Chat channels subscriptions
	 */
	public void addChatChannel(String chatChannel){
		String chatLower = chatChannel.toLowerCase();
		if(!chatChannels.contains(chatLower)){
			chatChannels.add(chatLower);
		}
	}
	
	public void removeChatChannel(String chatChannel){
		String chatLower = chatChannel.toLowerCase();
		if(chatChannels.contains(chatLower)){
			chatChannels.remove(chatLower);
		}
	}
	
	public boolean hasChatChannel(String chatChannel){
		if(chatChannels.contains(chatChannel.toLowerCase())){
			return true;
		}
		return false;
	}
	
	public Vector<String> getChatChannels() {
		return chatChannels;
	}

}