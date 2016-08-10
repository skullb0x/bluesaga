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
    DataHandlers.register("particledata", m -> handleParticleData(m));
    DataHandlers.register("skilldata", m -> handleSkillData(m));
  }

  public static void handleParticleData(Message m) {
    Client client = m.client;
    StringBuilder msg = new StringBuilder(4400);
    ResultSet emitterInfo = Server.gameDB.askDB("select * from Emitter");
    try {
      while (emitterInfo.next()) {
        msg.append(emitterInfo.getInt("Id"))
            .append(';')
            .append(emitterInfo.getString("Name"))
            .append(';')
            .append(emitterInfo.getFloat("LifeTime"))
            .append(';')
            .append(emitterInfo.getFloat("EmittionRate"))
            .append(';')
            .append(emitterInfo.getFloat("RotationSpeed"))
            .append(';')
            .append(emitterInfo.getString("MinPos"))
            .append(';')
            .append(emitterInfo.getString("MaxPos"))
            .append(';')
            .append(emitterInfo.getInt("ShowParticle"))
            .append(';')
            .append(emitterInfo.getInt("ParticleId"))
            .append(';')
            .append(emitterInfo.getInt("ShowStreak"))
            .append(';')
            .append(emitterInfo.getInt("StreakId"))
            .append('/');
      }
      emitterInfo.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    msg.append('%');
    ResultSet particleInfo = Server.gameDB.askDB("select * from Particle");
    try {
      while (particleInfo.next()) {
        msg.append(particleInfo.getInt("Id"))
            .append(';')
            .append(particleInfo.getString("Name"))
            .append(';')
            .append(particleInfo.getString("MinDir"))
            .append(';')
            .append(particleInfo.getString("MaxDir"))
            .append(';')
            .append(particleInfo.getInt("MinAxisRotSpeed"))
            .append(';')
            .append(particleInfo.getInt("MaxAxisRotSpeed"))
            .append(';')
            .append(particleInfo.getFloat("MinScale"))
            .append(';')
            .append(particleInfo.getFloat("MaxScale"))
            .append(';')
            .append(particleInfo.getFloat("LifeTime"))
            .append(';')
            .append(particleInfo.getString("ImageString"))
            .append(';')
            .append(particleInfo.getFloat("VerticalGravity"))
            .append(';')
            .append(particleInfo.getFloat("HorizontalGravity"))
            .append(';')
            .append(particleInfo.getString("StartColor"))
            .append(';')
            .append(particleInfo.getString("EndColor"))
            .append(';')
            .append(particleInfo.getFloat("RotationSpeed"))
            .append(';')
            .append(particleInfo.getFloat("FadeSpeed"))
            .append('/');
      }
      particleInfo.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    msg.append('%');
    ResultSet projectileInfo = Server.gameDB.askDB("select * from projectile");
    try {
      while (projectileInfo.next()) {
        msg.append(projectileInfo.getInt("Id"))
            .append(';')
            .append(projectileInfo.getString("GfxName"))
            .append(';')
            .append(projectileInfo.getInt("EmitterId"))
            .append(';')
            .append(projectileInfo.getString("HitColor"))
            .append(';')
            .append(projectileInfo.getString("Sfx"))
            .append('/');
      }
      projectileInfo.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    addOutGoingMessage(client, "particledata", msg.toString());
  }

  public static void handleSkillData(Message m) {
    Client client = m.client;
    if (client.playerCharacter != null) {
      StringBuilder skillData = new StringBuilder(1000);

      // GET SKILL INFO
      ResultSet skillInfo =
          Server.userDB.askDB(
              "select SkillId, Level, SP from character_skill where CharacterId = "
                  + client.playerCharacter.getDBId()
                  + " order by SkillId asc");

      // SkillId, SkillName, SkillLevel, SkillSP, SkillSPnext ; ...

      int nextSP = 0;

      try {
        while (skillInfo.next()) {
          JobSkill skillDef = ServerGameInfo.skillDef.get(skillInfo.getInt("SkillId"));

          if (skillDef != null) {
            nextSP = XPTables.nextLevelSP.get(skillInfo.getInt("Level") + 1);
            skillData
                .append(skillInfo.getInt("SkillId"))
                .append(',')
                .append(skillDef.getName())
                .append(',')
                .append(skillInfo.getInt("Level"))
                .append(',')
                .append(skillInfo.getInt("SP"))
                .append(',')
                .append(nextSP)
                .append(';');
          }
        }
        skillInfo.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
      addOutGoingMessage(client, "skilldata", skillData.toString());
    }
  }

  public static void gainSP(Client client, int skillId, boolean training) {
    int sp = 1;

    JobSkill skill = client.playerCharacter.getSkill(skillId);
    if (skill != null) {

      // TODO
      if (training) {
        int chanceOfSP = RandomUtils.getInt(0, 4);
        if (chanceOfSP > 0) {
          sp = 0;
        }
      }
      if (sp > 0) {
        if (skill != null) {
          if (skill.addSP(sp)) {
            // skillName, skillLevel, SP, SPnext
            int nextSP = XPTables.nextLevelSP.get(skill.getLevel() + 1);
            addOutGoingMessage(
                client,
                "sp_levelup",
                skill.getName() + ',' + skill.getLevel() + ',' + skill.getSP() + ',' + nextSP);
          } else {
            addOutGoingMessage(client, "set_sp", skill.getId() + "," + skill.getSP());
          }

          Server.userDB.updateDB(
              "update character_skill set SP = "
                  + skill.getSP()
                  + ", Level = "
                  + skill.getLevel()
                  + " where CharacterId = "
                  + client.playerCharacter.getDBId()
                  + " and SkillId = "
                  + skillId);
        }
      }
    }
  }

  public static void addSP(Client client, int skillId, int sp) {

    JobSkill skill = client.playerCharacter.getSkill(skillId);
    if (skill != null) {
      if (skill.addSP(sp)) {
        // skillName, skillLevel, SP, SPnext
        int nextSP = XPTables.nextLevelXP.get(skill.getLevel() + 1);
        addOutGoingMessage(
            client,
            "sp_levelup",
            skill.getName() + ',' + skill.getLevel() + ',' + skill.getSP() + ',' + nextSP);
      } else {
        addOutGoingMessage(client, "set_sp", skillId + "," + skill.getSP());
      }

      Server.userDB.updateDB(
          "update character_skill set SP = "
              + skill.getSP()
              + ", Level = "
              + skill.getLevel()
              + " where CharacterId = "
              + client.playerCharacter.getDBId()
              + " and SkillId = "
              + skillId);
    }
    //client.playerCharacter.saveInfo();
  }
}
