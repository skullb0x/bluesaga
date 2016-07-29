package data_handlers;

import java.sql.ResultSet;
import java.sql.SQLException;

import utils.RandomUtils;
import utils.ServerGameInfo;
import utils.XPTables;
import components.JobSkill;
import network.Client;
import network.Server;

public class SkillHandler extends Handler {

	public static void init() {

	}

	public static void handleData(Client client, String message){
		if(message.startsWith("<particledata>")){
			String emitterData = "";
			ResultSet emitterInfo = Server.gameDB.askDB("select * from Emitter");
			try {
				while(emitterInfo.next()){
					emitterData += emitterInfo.getInt("Id")+";"+emitterInfo.getString("Name")+";"+emitterInfo.getFloat("LifeTime")+";"+emitterInfo.getFloat("EmittionRate")+";"+emitterInfo.getFloat("RotationSpeed")+";"+emitterInfo.getString("MinPos")+";"+emitterInfo.getString("MaxPos")+";"+emitterInfo.getInt("ShowParticle")+";"+emitterInfo.getInt("ParticleId")+";"+emitterInfo.getInt("ShowStreak")+";"+emitterInfo.getInt("StreakId")+"/";
				}
				emitterInfo.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			String particleData = "%";
			ResultSet particleInfo = Server.gameDB.askDB("select * from Particle");
			try {
				while(particleInfo.next()){
					particleData += particleInfo.getInt("Id")+";"+particleInfo.getString("Name")+";"+particleInfo.getString("MinDir")+";";
					particleData += particleInfo.getString("MaxDir")+";"+particleInfo.getInt("MinAxisRotSpeed")+";"+particleInfo.getInt("MaxAxisRotSpeed")+";";
					particleData += particleInfo.getFloat("MinScale")+";"+particleInfo.getFloat("MaxScale")+";"+particleInfo.getFloat("LifeTime")+";";
					particleData += particleInfo.getString("ImageString")+";"+particleInfo.getFloat("VerticalGravity")+";"+particleInfo.getFloat("HorizontalGravity")+";";
					particleData += particleInfo.getString("StartColor")+";"+particleInfo.getString("EndColor")+";"+particleInfo.getFloat("RotationSpeed")+";"+particleInfo.getFloat("FadeSpeed")+"/";
				}
				particleInfo.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			String projectileData = "%";
			ResultSet projectileInfo = Server.gameDB.askDB("select * from projectile");
			try {
				while(projectileInfo.next()){
					projectileData += projectileInfo.getInt("Id")+";"+projectileInfo.getString("GfxName")+";"+projectileInfo.getInt("EmitterId")+";"+projectileInfo.getString("HitColor")+";"+projectileInfo.getString("Sfx")+"/";
				}
				projectileInfo.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}

			addOutGoingMessage(client,"particledata",emitterData+particleData+projectileData);

		}else if(message.startsWith("<skilldata>")){
			if(client.playerCharacter != null){
				String skillData = "";

				// GET SKILL INFO
				ResultSet skillInfo = Server.userDB.askDB("select SkillId, Level, SP from character_skill where CharacterId = "+client.playerCharacter.getDBId()+" order by SkillId asc");

				// SkillId, SkillName, SkillLevel, SkillSP, SkillSPnext ; ...

				int nextSP = 0;

				try {
					while(skillInfo.next()){
						JobSkill skillDef = ServerGameInfo.skillDef.get(skillInfo.getInt("SkillId"));
				
						if(skillDef != null){
							nextSP = XPTables.nextLevelSP.get(skillInfo.getInt("Level") + 1);
							skillData += skillInfo.getInt("SkillId")+","+skillDef.getName()+","+skillInfo.getInt("Level")+","+skillInfo.getInt("SP")+","+nextSP+";";	
						}
						
					}
					skillInfo.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				addOutGoingMessage(client,"skilldata",skillData);
			}
		}
	}

	public static void gainSP(Client client, int skillId, boolean training){
		int sp = 1;

		JobSkill skill = client.playerCharacter.getSkill(skillId);
		if(skill != null){
			
			// TODO
			if(training){
				int chanceOfSP = RandomUtils.getInt(0,4);
				if(chanceOfSP > 0){
					sp = 0;
				}
			}
			if(sp > 0){
				if(skill != null){
					if(skill.addSP(sp)){
						// skillName, skillLevel, SP, SPnext
						int nextSP = XPTables.nextLevelSP.get(skill.getLevel()+1);
						addOutGoingMessage(client, "sp_levelup", skill.getName()+","+skill.getLevel()+","+skill.getSP()+","+nextSP);
					}else {
						addOutGoingMessage(client,"set_sp",skill.getId()+","+skill.getSP());	
					}	

					Server.userDB.updateDB("update character_skill set SP = "+skill.getSP()+", Level = "+skill.getLevel()+" where CharacterId = "+client.playerCharacter.getDBId()+" and SkillId = "+skillId);
				}
			}
		}
	}

	public static void addSP(Client client, int skillId, int sp){

		JobSkill skill = client.playerCharacter.getSkill(skillId);
		if(skill != null){
			if(skill.addSP(sp)){
				// skillName, skillLevel, SP, SPnext
				int nextSP = XPTables.nextLevelXP.get(skill.getLevel()+1);
				addOutGoingMessage(client, "sp_levelup", skill.getName()+","+skill.getLevel()+","+skill.getSP()+","+nextSP);
			}else {
				addOutGoingMessage(client,"set_sp",skillId+","+skill.getSP());	
			}	

			Server.userDB.updateDB("update character_skill set SP = "+skill.getSP()+", Level = "+skill.getLevel()+" where CharacterId = "+client.playerCharacter.getDBId()+" and SkillId = "+skillId);
		}
		//client.playerCharacter.saveInfo();
	}
}
