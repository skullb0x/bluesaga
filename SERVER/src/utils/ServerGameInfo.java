package utils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import map.AreaEffect;
import network.Server;
import player_classes.*;
import components.Family;
import components.Quest;
import components.JobSkill;
import creature.Npc;
import data_handlers.ability_handler.Ability;
import data_handlers.ability_handler.StatusEffect;
import data_handlers.item_handler.Item;

public class ServerGameInfo {


	public static HashMap<Integer,Item> itemDef;

	public static HashMap<Integer,Ability> abilityDef;
	public static HashMap<Integer,JobSkill> skillDef;

	public static HashMap<Integer,Family> familyDef;
	public static HashMap<Integer,BaseClass> classDef;

	public static HashMap<Integer,AreaEffect> areaEffectsDef;

	public static HashMap<Integer,Quest> questDef;

	public static HashMap<Integer,Npc> creatureDef;

	public static void load(){
		// LOAD ITEMS
		itemDef = new HashMap<Integer, Item>();

		ResultSet itemInfo = Server.gameDB.askDB("select * from item");

		try {
			while(itemInfo.next()){
				Item newItem = new Item();
				newItem.load(itemInfo);

				itemDef.put(itemInfo.getInt("Id"),newItem);
			}
			itemInfo.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// LOAD CLASSES INFO
		classDef = new HashMap<Integer,BaseClass>();

		classDef.put(1, new WarriorClass());
		classDef.put(2, new MageClass());
		classDef.put(3, new HunterClass());
		classDef.put(4, new KnightClass());
		classDef.put(5, new FireMageClass());
		classDef.put(6, new ArcherClass());
		classDef.put(7, new TankClass());
		classDef.put(8, new IceMageClass());
		classDef.put(9, new DruidClass());
		classDef.put(10, new BerserkerClass());
		classDef.put(11, new ShockMageClass());
		classDef.put(12, new AssassinClass());
		classDef.put(14, new WhiteMageClass());

		// LOAD ABILITIES
		abilityDef = new HashMap<Integer,Ability>();

		ResultSet abilityInfo = Server.gameDB.askDB("select * from ability");

		try {
			while(abilityInfo.next()){
				Ability newAbility = new Ability();
				newAbility.load(abilityInfo);

				for(StatusEffect se: newAbility.getStatusEffects()){
					se.setAbility(newAbility);
				}
				
				abilityDef.put(abilityInfo.getInt("Id"),newAbility);
			}
			abilityInfo.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// LOAD SKILLS INFO
		skillDef = new HashMap<Integer,JobSkill>();

		ResultSet skillInfo = Server.gameDB.askDB("select * from skill");

		try {
			while(skillInfo.next()){
				JobSkill newSkill = new JobSkill();
				newSkill.load(skillInfo);

				skillDef.put(skillInfo.getInt("Id"),newSkill);
			}
			skillInfo.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}



		// LOAD FAMILIES INFO
		familyDef = new HashMap<Integer,Family>();
		familyDef.put(1, new Family(1,"Forest Creatures",3));
		familyDef.put(2, new Family(2,"Mountain Dwellers",6));
		familyDef.put(3, new Family(3,"Undead",1));
		familyDef.put(4, new Family(4,"Pit Spawns",5));
		familyDef.put(5, new Family(5,"Snow Beasts",4));
		familyDef.put(6, new Family(6,"Sky Roamers",2));
		familyDef.put(7, new Family(7,"Ocean Monsters",0));
		familyDef.put(8, new Family(8,"Practice Targets",0));

		
		
		// Load AreaEffect info
		areaEffectsDef = new HashMap<Integer,AreaEffect>();

		ResultSet areaEffectInfo = Server.mapDB.askDB("select * from area_effect");

		try {
			while(areaEffectInfo.next()){
				AreaEffect ae = new AreaEffect(areaEffectInfo.getInt("Id"));

				ae.setAreaName(areaEffectInfo.getString("AreaName"));

				ae.setTint(areaEffectInfo.getInt("Tint"));
				ae.setTintColor(areaEffectInfo.getString("TintColor"));

				ae.setFog(areaEffectInfo.getInt("Fog"));
				ae.setFogColor(areaEffectInfo.getString("FogColor"));

				ae.setSong(areaEffectInfo.getString("Song"));

				ae.setAreaItems(areaEffectInfo.getString("AreaItems"));
				ae.setAreaCopper(areaEffectInfo.getInt("AreaCopper"));

				ae.setParticles(areaEffectInfo.getString("Particles"));
				ae.setGuardedLevel(areaEffectInfo.getInt("GuardLevel"));
			
				areaEffectsDef.put(ae.getId(), ae);
			}
			areaEffectInfo.close();

		} catch (SQLException e1) {
			e1.printStackTrace();
		}


		// Load quest info
		questDef = new HashMap<Integer,Quest>();

		ResultSet questInfo = Server.mapDB.askDB("select * from quest");

		try {
			while(questInfo.next()){
				Quest newQuest = new Quest(questInfo.getInt("Id"));
				newQuest.loadQuest(questInfo);
				questDef.put(newQuest.getId(),newQuest);
			}
			questInfo.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}


		// LOAD ITEM INFO


		// LOAD MONSTERS INFO
		creatureDef = new HashMap<Integer,Npc>();
		
		ResultSet monsterInfo = Server.gameDB.askDB("select Id from creature");
		
		try {
			while(monsterInfo.next()){
				Npc newMob = new Npc(monsterInfo.getInt("Id"),0,0,0);
				creatureDef.put(monsterInfo.getInt("Id"), newMob);
			}
			monsterInfo.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
	}
}
