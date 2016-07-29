package data_handlers.battle_handler;

import utils.RandomUtils;
import creature.Creature;
import creature.PlayerCharacter;
import creature.Creature.CreatureType;
import data_handlers.item_handler.Item;

public class DamageCalculator {

	// Check if miss, evade or hit
	public static String calculateAttack(Creature ATTACKER, Creature TARGET) {

		String damageInfo = "";

		int damage = 0;
		int hitOrMiss = RandomUtils.getInt(0, 100) - 5;

		if(TARGET.isResting()){
			hitOrMiss = 0;
		}

		if (hitOrMiss > ATTACKER.getStat("ACCURACY")) {
			damage = 0;
			damageInfo = "miss;0";
		} else {

			int evaded = RandomUtils.getInt(0, 100) + 2;

			if(TARGET.isResting()){
				evaded = 100;
			}

			if(evaded < TARGET.getStat("EVASION")){
				damageInfo = "evade;0";
			}else{

				damage = calculateDamage(ATTACKER, TARGET);
				int criticalChance = RandomUtils.getInt(0, 100);

				
				float diffAngle = Math.abs(ATTACKER.getRotation() - TARGET.getRotation());

				// Check if the attack is from behind
				if(diffAngle < 90.0f){
					int critBonusChance = (int) Math.round(80.0f - (diffAngle/2.0));
					criticalChance -= critBonusChance;
				}

				if(TARGET.isResting()){
					criticalChance = 0;
				}

				if (damage > 0 && criticalChance <= ATTACKER.getStat("CRITICAL_HIT")) {
					damageInfo = "true;";
					damage *= 1.5;
				} else {
					damageInfo = "false;";
				}

				damageInfo += Integer.toString(damage);
			}
		}

		return damageInfo;
	}

	// CALCULATE DAMAGE
	public static int calculateDamage(Creature ATTACKER, Creature TARGET){
		int damage = 0;

		// GET ATTACK STAT
		String attackStat = "STRENGTH";

		int classId = 0;

		Item weapon = ATTACKER.getEquipment("Weapon");

		int weaponMinBaseDmg = 0;
		int weaponMaxBaseDmg = 0;

		if(weapon != null) {
			attackStat = weapon.getDamageStat();
			classId = weapon.getClassId();
			weaponMinBaseDmg = weapon.getStatValue("MinDamage");
			weaponMaxBaseDmg = weapon.getStatValue("MaxDamage");
		}

		String damageType = ATTACKER.getAttackType();

		int damageMin = 0;
		int damageMax = 0;

		float attackMinF = 0.0f;
		float attackMaxF = 0.0f;
		
		// Armor or resistance modifier
		float armorF = 1.0f;
		armorF = getDamageArmor(damageType,ATTACKER,TARGET);
		
		if(ATTACKER.getCreatureType() == CreatureType.Monster){
		
			// MONSTER DAMAGE FORMULA
			attackMinF = (ATTACKER.getStat(attackStat) + weaponMinBaseDmg/3.0f) * 0.7f;
			attackMaxF = ATTACKER.getStat(attackStat) + weaponMaxBaseDmg/3.0f;

			damageMin = (int) Math.floor(attackMinF * armorF * 0.7f);
			damageMax = (int) Math.floor(attackMaxF * armorF * 0.7f + 1.0f);

		}else if(ATTACKER.getCreatureType() == CreatureType.Player){
			PlayerCharacter playerAttacker = (PlayerCharacter) ATTACKER;
			
			// PLAYER DAMAGE FORMULA

			// (ATK+12)/12 * (SkillLevel+16)/16 * WeaponMin/10 * (60/(60+Armor))

			float ATKmin = (ATTACKER.getStat(attackStat) + 12.0f) / 12.0f;
			float ATKmax = (ATTACKER.getStat(attackStat) + 10.0f) / 10.0f;

			float classFac = 1.0f;

			if(classId > 0){
				if(playerAttacker.getClassById(classId) != null){
					classId = playerAttacker.getClassById(classId).baseClassId;
					classFac = (playerAttacker.getClassById(classId).level+ 12.0f) / 13.0f; 
				}
			}

			float WeaponMin = weaponMinBaseDmg / 10.0f;
			float WeaponMax = weaponMaxBaseDmg / 10.0f;

			attackMinF = ATKmin * classFac * WeaponMin;
			attackMaxF = ATKmax * classFac * WeaponMax;

			damageMin = (int) Math.floor(attackMinF * armorF);
			damageMax = (int) Math.floor(attackMaxF * armorF + 1.0f);
		}

		// HALF DAMAGE IF PVP AND NOT IN LOST ARCHIPELAGO
		if(ATTACKER.getCreatureType() == CreatureType.Player && TARGET.getCreatureType() == CreatureType.Player && TARGET.getZ() > -200){
			damageMin = (int) Math.floor(damageMin / 2.0f);
			damageMax = (int) Math.floor(damageMax / 2.0f);
		}

		if(damageMin < 0){
			damageMin = 0;
		}
		if(damageMax < 0){
			damageMax = 0;
		}
		damage = RandomUtils.getInt(damageMin, damageMax);
		
		return damage;
	}
	
	public static float getDamageArmor(String damageType, Creature ATTACKER, Creature TARGET){
		float armorF = 1.0f;
		
		float targetArmorF = TARGET.getStat("ARMOR");
		
		// ARMOR OR RESISTANCE
		if(damageType.equals("STRIKE") || damageType.equals("SLASH") || damageType.equals("PIERCE")){
			// PHYSICAL DAMAGE, USE ARMOR
			
			// IF PIERCE, PIERCING THROUGH ARMOR
			float pierceFac = 1.0f;
			if(damageType.equals("PIERCE")){
				pierceFac *= 0.8f; // -20% of ARMOR if PIERCE
			}
			if(ATTACKER.hasStatusEffect(32)){
				// ARMOR PIERCING STATUS EFFECT
				pierceFac *= 0.5f; // -50% of ARMOR or - 55%
			}
			armorF = (60.0f / (60.0f + targetArmorF*pierceFac)); 

		}else{
			// MAGICAL DAMAGE, USE RESISTANCE
			float res = (100.0f - TARGET.getStat(damageType+"_DEF")) / 100.0f;

			armorF = res; 
		}
		return armorF;
	}
	
}
