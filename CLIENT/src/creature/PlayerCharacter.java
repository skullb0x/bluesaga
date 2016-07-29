/************************************
 * 									*
 *		CLIENT / CREWMEMBER			*
 *									*
 ************************************/	

package creature;

import game.BlueSaga;
import game.Database;
import graphics.BlueSagaColors;
import graphics.Font;
import graphics.ImageResource;
import gui.Dice;
import gui.Gui;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import animationsystem.CreatureAnimation;
import player_classes.BaseClass;
import utils.GameInfo;
import components.Item;
import components.Quest;
import components.Crew;
import components.Ship;

public class PlayerCharacter extends Creature {

	private int Level;
	private int XP;
	private int nextXP;

	private Timer LevelUpTimer;

	private int UserId = 0;

	private int adminLevel;
	
	private boolean goToTarget;

	private int inventorySize = 4;
	
	private ArrayList<Quest> Quests = new ArrayList<Quest>(); 

	private int Bounty;

	private int pkMarker = 0;

	static Timer timerShowCrew = new Timer();

	private Ship Ship;

	protected Crew Crew;

	private BaseClass baseClass = null;
	private BaseClass primaryClass = null;
	private BaseClass secondaryClass = null;

	private HashMap<Integer, BaseClass> playerClasses;
	
	private Dice dice;
	
	private int bountyRank = 0;
	
	public PlayerCharacter(int creatureId, int newX, int newY, int newZ) {
		super(newX, newY, newZ);

		setType(creatureId);

		Stats.setValue("SPEED", 3);

		Abilities.clear();

		Crew = new Crew(0);

		XP = 0;
		nextXP = 10;
		dice = new Dice();
	}


	public void draw(Graphics g, int centerX, int centerY){

		boolean onABoat = false;
		if(getShip() != null){
			if(getShip().isShow()){
				onABoat = true;
				getShip().draw(centerX, centerY + 10);
		
				// Remove jumping up and down when on a boat
				if(MyWalkHandler.getWalkItr() % 25 >= 11){
					centerY += 5;
				}
			}
		}
		
		super.draw(g, centerX, centerY, null);	


		boolean cloacked = false;
		if(hasStatusEffect(13) || hasStatusEffect(40)){
			cloacked = true;
		}

		// Draw name & crewname
		if(!Dead && !hidden){
			sizeWidthF = SizeWidth;
			sizeHeightF = SizeHeight;
			
			// Animation variables
			float scaleX = 1.0f;
			float scaleY = 1.0f;
			float aniRotation = 0.0f;
			Iterator<CreatureAnimation> it = creatureAnimations.iterator();
			
			dice.draw(g,centerX,centerY-50);
			
			while(it.hasNext()){
				CreatureAnimation cAni = it.next();
				
				if(cAni.isActive()){
					cAni.update();
					
					centerX += cAni.getAnimationX();
					centerY += cAni.getAnimationY();
				
					aniRotation = cAni.getRotation(); 
					
					if(aniRotation != 0){
						aniRotation = (aniRotation + rotation) % 360.0f;
						
						if(aniRotation < 0.0f){
							aniRotation += 360.0f;
						}
						
						if(aniRotation > 270.0f || aniRotation < 90.0f){
				    		animation = backAnimation;
				    	}else{
				    		animation = frontAnimation;
				    	}
					
						directionRing.getImage().setRotation(aniRotation);
					}
					cAni.getSpin();
					
					scaleX = cAni.getScaleX();
					scaleY = cAni.getScaleY();
					
					sizeWidthF *= scaleX;
					sizeHeightF *= scaleY;
				}else{
					it.remove();
				}
			}

			
			// Remove jumping up and down when on a boat
			if(onABoat){
				if(MyWalkHandler.getWalkItr() % 25 >= 11){
					centerY -= 5;
				}
			}
			
			float cXf = centerX - (sizeWidthF)*25.0f;
			float cYf = centerY - (sizeHeightF)*25.0f;

			int cornerX = Math.round(cXf);
			int cornerY = Math.round(cYf);
			
			boolean mouseOver = false;
			
			if(BlueSaga.INPUT.getAbsoluteMouseX() > cornerX && BlueSaga.INPUT.getAbsoluteMouseY() > cornerY
					&& BlueSaga.INPUT.getAbsoluteMouseX() < cornerX+SizeWidth*50 && BlueSaga.INPUT.getAbsoluteMouseY() < cornerY+SizeHeight*50){
				mouseOver = true;
			}
			
			if(!cloacked && mouseOver){

				// Draw name
				g.setFont(Font.size10);

				int nameWidth = Font.size10.getWidth(Name);
				int nameX = centerX - nameWidth/2;

				g.setColor(new Color(0,0,0,150));

				g.drawString(Name, nameX, cornerY - 25);

				if(getAdminLevel() > 3){
					g.setColor(BlueSagaColors.BLUE);
				}else{
					if(getHealthStatus() == 4){
						g.setColor(BlueSagaColors.WHITE);
					}else if(getHealthStatus() == 3){
						g.setColor(new Color(255,249,75));
					}else if(getHealthStatus() == 2){
						g.setColor(BlueSagaColors.ORANGE);
					}else {
						g.setColor(BlueSagaColors.RED);
					}
				}
								
				g.drawString(Name, nameX , cornerY - 26);

				if(getPkMarker() > 0){
					ImageResource.getSprite("gui/battle/pkmarker"+getPkMarker()).draw(nameX - 20, cornerY - 26);
				}
				
				// Draw bounty rank
				if(getBountyRank() > 0){
					ImageResource.getSprite("gui/pvp/ranks/"+getBountyRank()).draw(nameX + nameWidth + 5,cornerY - 24);
					/*
					int bountyRankWidth = Font.size9.getWidth(GameInfo.bountyRanks.get(getBountyRank()).name);
					g.setFont(Font.size9);

					g.setColor(new Color(0,0,0,150));

					int bountyRankX = centerX - bountyRankWidth/2;
					g.drawString(GameInfo.bountyRanks.get(getBountyRank()).name, bountyRankX, cornerY - 12);

					g.setColor(new Color(223,138,255));
					g.drawString(GameInfo.bountyRanks.get(getBountyRank()).name, bountyRankX, cornerY - 13);
					 */
				}

				
				// Draw crew name
				if(!Crew.getName().equals("0")){
					nameWidth = Font.size9.getWidth(Crew.getName());
					g.setFont(Font.size9);

					g.setColor(new Color(0,0,0,150));

					nameX = centerX - nameWidth/2;
					g.drawString(Crew.getName(), nameX, cornerY - 12);

					g.setColor(new Color(200,255,111));
					g.drawString(Crew.getName(), nameX, cornerY - 13);
				}
			}

			// Draw sweat drop if in battle mode
			if(getDBId() == BlueSaga.playerCharacter.getDBId()){
				if(BlueSaga.logoutTimeItr > 0){
					ImageResource.getSprite("gui/emoticons/sweat_drop").getAnimation().getCurrentFrame().getFlippedCopy(lookRight, false).draw(cornerX + (MyEquipHandler.getHeadX()+13)*equipDir, cornerY + MyEquipHandler.getHeadY() + 10); 
				}
			}
		}
	}


	public void drawRestingFade(){
		if(isResting()){
			if(RestingFadeAlpha < 255){
				RestingFadeAlpha+=8;
			}
		}else{
			if(RestingFadeAlpha > 0){
				RestingFadeAlpha-=16;
			}
		}
		if(RestingFadeAlpha > 0){
			ImageResource.getSprite("effects/sleep_fade").draw(0,0,new Color(255,255,255,RestingFadeAlpha));
		}
	}


	public void rollDice(int result){
		dice.roll(result);
	}


	/****************************************
	 *                                      *
	 *             BATTLE	  	            *
	 *                                      *
	 *                                      *
	 ****************************************/

	public void addXP(int newXP) {
		XP += newXP;

		if(XP > nextXP){
			XP = nextXP;
		}
	}

	public int getXPBarWidth(int Max) {
		float XP_f = XP; 
		float nextXP_f = nextXP;

		float XPBarWidth = (XP_f / nextXP_f) * Max;
		return Math.round(XPBarWidth);
	}


	public void levelChange(String levelUpData){
		if(!levelUpData.equals("")){
			String[] levelup_data = levelUpData.split(",");

			// PRIMARY STATS
			Stats.setValue("STRENGTH",Integer.parseInt(levelup_data[0]));
			Stats.setValue("INTELLIGENCE",Integer.parseInt(levelup_data[1]));
			Stats.setValue("AGILITY",Integer.parseInt(levelup_data[2]));

			// HEALTH AND MANA
			Stats.setValue("MAX_HEALTH",Integer.parseInt(levelup_data[3]));
			Stats.setValue("MAX_MANA",Integer.parseInt(levelup_data[4]));

			Health = getTotalStat("MAX_HEALTH");
			Mana = getTotalStat("MAX_MANA");
			Stats.setValue("SPEED", Integer.parseInt(levelup_data[5]));
			Stats.setValue("ARMOR", Integer.parseInt(levelup_data[6]));


			int newLevel = Integer.parseInt(levelup_data[7]);
			nextXP = Integer.parseInt(levelup_data[8]);
			XP = Integer.parseInt(levelup_data[9]);

			if(newLevel > Level){
				ShowLevelUp = true;
				ShowLevelDown = false;
			}else{
				ShowLevelDown = true;
				ShowLevelUp = false;
			}

			Level = newLevel;

			LevelUpTimer = new Timer();

			animationUseAbility.restart();

			setHealthStatus(4);

			LevelUpTimer.schedule( new TimerTask(){
				@Override
				public void run() {
					ShowLevelUp = false;
					ShowLevelDown = false;
				}
			}, 1000);
		}
	}



	/****************************************
	 *                                      *
	 *             MOVEMENT  	            *
	 *                                      *
	 *                                      *
	 ****************************************/

	public void load(String playerInfo){

		// x, y, areaId, gold, nrCrewMembers
		String[] load_data;
		load_data = playerInfo.split("/");

		String[] player_info = load_data[0].split(":");

		String[] main_info;
		main_info = player_info[0].split(",");

		setX(Integer.parseInt(main_info[0]));
		setY(Integer.parseInt(main_info[1]));
		setZ(Integer.parseInt(main_info[2]));
		Bounty = Integer.parseInt(main_info[3]);

		setBountyRank(GameInfo.getBountyRankId(Bounty));
		
		setCreatureType(CreatureType.Player);

		int logoutTime = Integer.parseInt(main_info[4]);

		if(logoutTime > 0){
			BlueSaga.restartLogoutTimer(logoutTime);
		}

		goToTarget = false;

		// LOAD CHARACTER
		String[] creature_info = player_info[1].split(",");

		setCreatureId(Integer.parseInt(creature_info[0]));

		Name = creature_info[1];
		Family = creature_info[2];

		AttackType = creature_info[3];
		Level = Integer.parseInt(creature_info[4]);
		XP = Integer.parseInt(creature_info[5]);

		Stats.reset();

		// PRIMARY STATS
		Stats.setValue("STRENGTH",Integer.parseInt(creature_info[6]));
		Stats.setValue("INTELLIGENCE",Integer.parseInt(creature_info[7]));
		Stats.setValue("AGILITY",Integer.parseInt(creature_info[8]));
		Stats.setValue("SPEED",Integer.parseInt(creature_info[9]));

		// SECONDARY STATS
		Stats.setValue("CRITICAL_HIT",Integer.parseInt(creature_info[10]));
		Stats.setValue("EVASION",Integer.parseInt(creature_info[11]));
		Stats.setValue("ACCURACY",Integer.parseInt(creature_info[12]));

		// HEALTH AND MANA
		Stats.setValue("MAX_HEALTH",Integer.parseInt(creature_info[13]));
		Health = Integer.parseInt(creature_info[14]);

		Stats.setValue("MAX_MANA",Integer.parseInt(creature_info[15]));
		Mana = Integer.parseInt(creature_info[16]);

		// MAGIC STATS
		Stats.setValue("FIRE_DEF",Integer.parseInt(creature_info[17]));
		Stats.setValue("COLD_DEF",Integer.parseInt(creature_info[18]));
		Stats.setValue("SHOCK_DEF",Integer.parseInt(creature_info[19]));
		Stats.setValue("CHEMS_DEF",Integer.parseInt(creature_info[20]));
		Stats.setValue("MIND_DEF",Integer.parseInt(creature_info[21]));
		Stats.setValue("MAGIC_DEF",Integer.parseInt(creature_info[22]));

		// ATTACK STATS
		Stats.setValue("ARMOR",Integer.parseInt(creature_info[23]));

		// Get xp info
		nextXP = Integer.parseInt(creature_info[24]);

		dbId = Integer.parseInt(creature_info[25]);

		setAttackRange(Integer.parseInt(creature_info[26]));
		setHealthStatus(Integer.parseInt(creature_info[27]));

		// CREW INFO
		int crewId = Integer.parseInt(creature_info[28]);
		String crewName = creature_info[29];
		String crewMemberName = creature_info[30];

		Crew = new Crew(crewId);
		Crew.setName(crewName);
		Crew.setMemberState(crewMemberName);

		if(crewId > 0){
			Gui.Chat_Window.addChatChannel("crew");
		}

		// SHIP INFO
		int shipId = Integer.parseInt(creature_info[31]);
		setShip(new Ship(shipId));


		setPkMarker(Integer.parseInt(creature_info[32]));

		// Classes info
		playerClasses = new HashMap<Integer,BaseClass>();
		
		int baseClassId = Integer.parseInt(creature_info[33]);
		
		int classIndex = 34;
		
		for(BaseClass classDef: GameInfo.classDef.values()){
			if(classDef.available){
				BaseClass playerClass = new BaseClass(classDef);
				playerClass.level = Integer.parseInt(creature_info[classIndex]);
				classIndex++;
				playerClass.setXp(Integer.parseInt(creature_info[classIndex]));
				classIndex++;
				playerClass.nextXP = Integer.parseInt(creature_info[classIndex]);
				classIndex++;
				
				playerClasses.put(playerClass.id,playerClass);
					
				int classType = Integer.parseInt(creature_info[classIndex]);
				if(classType == 2){
					secondaryClass = playerClasses.get(playerClass.id);
				}else if(classType == 1){
					primaryClass = playerClasses.get(playerClass.id);
				}
				classIndex++;
			}
		}
		
		setBaseClass(playerClasses.get(baseClassId));
		
		
		setAdminLevel(Integer.parseInt(creature_info[classIndex]));
		
		// LOAD EQUIPMENT

		if(load_data.length > 1){
			String equip_info[] = load_data[1].split(",");

			int headId = Integer.parseInt(equip_info[0]);
			int headSkinId = Integer.parseInt(equip_info[1]);
			int headClassId = Integer.parseInt(equip_info[2]);
			
			int weaponId = Integer.parseInt(equip_info[3]);
			int weaponSkinId = Integer.parseInt(equip_info[4]);
			int weaponClassId = Integer.parseInt(equip_info[5]);
			
			int offHandId = Integer.parseInt(equip_info[6]);
			int offHandSkinId = Integer.parseInt(equip_info[7]);
			int offHandClassId = Integer.parseInt(equip_info[8]);
			
			int amuletId = Integer.parseInt(equip_info[9]);
			int amuletSkinId = Integer.parseInt(equip_info[10]);
			int amuletClassId = Integer.parseInt(equip_info[11]);
			
			int artifactId = Integer.parseInt(equip_info[12]);
			int artifactSkinId = Integer.parseInt(equip_info[13]);
			int artifactClassId = Integer.parseInt(equip_info[14]);
			
			if(headId > 0){
				Item newEquip = new Item(headId);
				newEquip.setType("Head");
				newEquip.setClassId(headClassId);
				MyEquipHandler.equipItem(newEquip);
			}
			getCustomization().setHeadSkinId(headSkinId);
			if(weaponId > 0){
				Item newEquip = new Item(weaponId);
				newEquip.setType("Weapon");
				newEquip.setClassId(weaponClassId);
				MyEquipHandler.equipItem(newEquip);
			}
			getCustomization().setWeaponSkinId(weaponSkinId);
			if(offHandId > 0){
				Item newEquip = new Item(offHandId);
				newEquip.setType("OffHand");
				newEquip.setClassId(offHandClassId);
				MyEquipHandler.equipItem(newEquip);
			}
			getCustomization().setOffHandSkinId(offHandSkinId);
			if(amuletId > 0){
				Item newEquip = new Item(amuletId);
				newEquip.setType("Amulet");
				newEquip.setClassId(amuletClassId);
				MyEquipHandler.equipItem(newEquip);
			}
			getCustomization().setAmuletSkinId(amuletSkinId);
			if(artifactId > 0){
				Item newEquip = new Item(artifactId);
				newEquip.setType("Artifact");
				newEquip.setClassId(artifactClassId);
				MyEquipHandler.equipItem(newEquip);
			}
			getCustomization().setArtifactSkinId(artifactSkinId);

			int mouthFeatureId = Integer.parseInt(equip_info[15]);
			int accessoriesId = Integer.parseInt(equip_info[16]);
			int skinFeatureId = Integer.parseInt(equip_info[17]);

			getCustomization().setMouthFeatureId(mouthFeatureId);
			getCustomization().setAccessoriesId(accessoriesId);
			getCustomization().setSkinFeatureId(skinFeatureId);

		}
	}


	/*
	public void saveCrewInfo(Database gameDB) {
		for(int i = 0; i < nrCrewMembers; i++){		
			members[i].saveStats(gameDB);
		}
	}
	 */




	/****************************************
	 *                                      *
	 *             CREW MEMBERS             *
	 *                                      *
	 *                                      *
	 ****************************************/

	public void resetPosition(int playerX, int playerY){
		X = playerX;
		Y = playerY;
	}




	/****************************************
	 *                                      *
	 *             QUEST		            *
	 *                                      *
	 *                                      *
	 ****************************************/

	public void loadQuests(String questInfo, Database gameDB) {
		/*
		String quest_info[] = questInfo.split("/");

		int nrQuests = Integer.parseInt(quest_info[0]);

		Quests.clear();
		Quest tempQuest;
		ResultSet rs;

		int questId = 0;
		int questStatus = 0;

		if(nrQuests > 0){
			String quest_information[] = quest_info[1].split(";");

			for(int i = 0; i < nrQuests; i++){
				questId = Integer.parseInt(quest_information[i*2]);
				questStatus = Integer.parseInt(quest_information[i*2 + 1]);

				tempQuest = new Quest();
				tempQuest.setId(questId);
				tempQuest.loadQuest(gameDB);
				tempQuest.setStatus(questStatus);

				Quests.add(tempQuest);
			}
		}
		 */
	}

	public ArrayList<String> checkQuests(Database gameDB) {
		ArrayList<String> messages = new ArrayList<String>();

		for(int i = 0; i < Quests.size(); i++){
			// KILL X CREATURES X

			if(Quests.get(i).getType().equals("Kill X creature X")){

				ResultSet rs = null;

				// CHECK IF KILLED ENOUGH MONSTERS OF RIGHT KIND
				if(Quests.get(i).getTargetType().equals("Creature")){
					rs = gameDB.askDB("select Kills from user_kills where UserId = "+UserId+" and CreatureId = "+Quests.get(i).getTargetId());
				}

				try {
					if(rs.next()){

						// IF UNCOMPLETED QUEST
						if(Quests.get(i).getStatus() == 0){
							if(rs.getInt("Kills") >= Quests.get(i).getTargetNumber()){
								messages.add("Completed Quest\n'"+Quests.get(i).getName()+"'!");
								gameDB.updateDB("update quest_data set completed = 1 where QuestId = "+Quests.get(i).getId()+" and UserId = "+UserId);
							}
						}
					}
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

		}

		return messages;
	}

	public void updateQuestStatus(int questId, int completed){
		for(int i = 0; i < Quests.size(); i++){
			if(Quests.get(i).getId() == questId){
				Quests.get(i).setStatus(completed);
			}
		}
	}


	public ArrayList<Quest> getQuests(){
		return Quests;
	}


	
	
	/****************************************
	 *                                      *
	 *             GETTER/SETTER            *
	 *                                      *
	 *                                      *
	 ****************************************/
	public BaseClass getClassById(int classId){
		return playerClasses.get(classId);
	}

	public int getNrQuests() {
		return Quests.size();
	}


	public int getBounty() {
		return Bounty;
	}

	public void setBounty(int newBounty){
		Bounty = newBounty;
	}

	public void setGoToTarget(boolean newValue){
		goToTarget = newValue;
	}

	public boolean getGoToTarget(){
		return goToTarget;
	}



	public int getPkMarker() {
		return pkMarker;
	}


	public void setPkMarker(int pkMarker) {
		this.pkMarker = pkMarker;
	}


	public Ship getShip() {
		return Ship;
	}


	public void setShip(Ship myShip) {
		this.Ship = myShip;
	}

	public void setBountyRank(int bountyRank){
		this.bountyRank = bountyRank;
	}

	public int getBountyRank(){
		return bountyRank;
	}

	public void setCrew(Crew newCrew){
		Crew = newCrew;
	}

	public Crew getCrew(){
		return Crew;
	}

	public BaseClass getBaseClass() {
		return baseClass;
	}


	public void setBaseClass(BaseClass baseClass) {
		this.baseClass = baseClass;
	}


	public BaseClass getPrimaryClass() {
		return primaryClass;
	}


	public void setPrimaryClass(BaseClass primaryClass) {
		this.primaryClass = primaryClass;
	}


	public BaseClass getSecondaryClass() {
		return secondaryClass;
	}


	public void setSecondaryClass(BaseClass secondaryClass) {
		this.secondaryClass = secondaryClass;
	}
	
	public HashMap<Integer,BaseClass> getPlayerClasses(){
		return playerClasses;
	}
	
	public int getAdminLevel() {
		return adminLevel;
	}

	public void setAdminLevel(int adminLevel) {
		this.adminLevel = adminLevel;
	}
	
	public float getXP() {
		return XP;
	}

	public float getNextXP() {
		return nextXP;
	}

	public void setXP(int newXP){
		XP = newXP;
	}

	public int getLevel(){
		return Level;
	}

	public void setLevel(int newLevel){
		Level = newLevel;
	}
}