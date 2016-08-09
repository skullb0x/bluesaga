package data_handlers;

import java.sql.ResultSet;
import java.sql.SQLException;

import components.JobSkill;
import player_classes.BaseClass;
import utils.ServerGameInfo;
import utils.RandomUtils;
import utils.XPTables;
import network.Client;
import network.Server;

public class ClassHandler extends Handler {

  /**
   * Used to gain XP on base class when using weapon
   * @param client
   * @param classId
   * @param training
   */
  public static void gainBaseXP(Client client, int classId, boolean training) {
    int xp = 1;

    if (classId > 0 && classId < 4) {
      BaseClass playerClass = client.playerCharacter.getClassById(classId);
      if (playerClass != null) {
        if (training) {
          int chanceOfXP = RandomUtils.getInt(0, 4);
          if (chanceOfXP > 0) {
            xp = 0;
          }
        }
        if (xp > 0) {
          if (playerClass.addXP(xp)) {
            // classId, classLevel, classNextXP
            addOutGoingMessage(
                client,
                "class_levelup",
                playerClass.id + "," + playerClass.level + "," + playerClass.nextXP);
          } else {
            addOutGoingMessage(client, "class_xp", playerClass.id + "," + playerClass.getXp());
          }
        }

        Server.userDB.updateDB(
            "update character_class set ClassXP = "
                + playerClass.getXp()
                + ", ClassLevel = "
                + playerClass.level
                + " where CharacterId = "
                + client.playerCharacter.getDBId()
                + " and ClassId = "
                + playerClass.id);
      }
    }
  }

  /**
   * Gain XP on sub classes when using mana with abilities
   * @param client
   * @param classId
   * @param training
   */
  public static void gainSubXP(Client client, int classId, int mana) {
    int xp = mana;

    BaseClass playerClass = client.playerCharacter.getClassById(classId);
    if (playerClass != null) {
      // Gain XP to class
      if (xp > 0) {
        if (playerClass.addXP(xp)) {
          // classId, classLevel, classNextXP
          addOutGoingMessage(
              client,
              "class_levelup",
              playerClass.id + "," + playerClass.level + "," + playerClass.nextXP);
        } else {
          addOutGoingMessage(client, "class_xp", playerClass.id + "," + playerClass.getXp());
        }
        Server.userDB.updateDB(
            "update character_class set ClassXP = "
                + playerClass.getXp()
                + ", ClassLevel = "
                + playerClass.level
                + " where CharacterId = "
                + client.playerCharacter.getDBId()
                + " and ClassId = "
                + playerClass.id);
      }
    } else {
      // Gain SP to job skill
      JobSkill playerSkill = client.playerCharacter.getSkill(classId);
      if (playerSkill != null) {
        if (playerSkill.addSP(1)) {
          // skillId, skillLevel, SP, SPnext
          addOutGoingMessage(
              client,
              "sp_levelup",
              playerSkill.getId()
                  + ","
                  + playerSkill.getLevel()
                  + ","
                  + playerSkill.getSP()
                  + ","
                  + XPTables.nextLevelSP.get(playerSkill.getLevel() + 1));
        } else {
          addOutGoingMessage(client, "set_sp", playerSkill.getId() + "," + playerSkill.getSP());
        }
        Server.userDB.updateDB(
            "update character_skill set SP = "
                + playerSkill.getSP()
                + ", Level = "
                + playerSkill.getLevel()
                + " where CharacterId = "
                + client.playerCharacter.getDBId()
                + " and SkillId = "
                + classId);
      }
    }
  }

  /**
   * Learn class
   * @param client
   * @param classId
   * @param classType
   * @return
   */
  public static boolean learnClass(Client client, int classId, int classType) {
    boolean changed = false;

    boolean classChangeOk = true;

    if (classType == 1) {
      // Change primary class

      if (client.playerCharacter.getPrimaryClass() != null) {
        if (client.playerCharacter.getPrimaryClass().id == classId) {
          classChangeOk = false;
          addOutGoingMessage(client, "message", "#messages.classes.already_have");
        }
      }

      if (client.playerCharacter.getSecondaryClass() != null) {
        if (client.playerCharacter.getSecondaryClass().id == classId) {
          classChangeOk = false;
          addOutGoingMessage(client, "message", "#messages.classes.already_have");
        }
      }

      // Check that class has right base class
      if (client.playerCharacter.getBaseClass().id
          != ServerGameInfo.classDef.get(classId).baseClassId) {
        classChangeOk = false;
        addOutGoingMessage(
            client,
            "message",
            "#messages.classes.cant_learn#"
                + client.playerCharacter.getBaseClass().name
                + ".\n#messages.classes.learn_secondary");
      }

    } else if (classType == 2) {
      // Change secondary class

      if (client.playerCharacter.getSecondaryClass() != null) {
        if (client.playerCharacter.getSecondaryClass().id == classId) {
          classChangeOk = false;
          addOutGoingMessage(client, "message", "#messages.classes.already_have");
        }
      }

      if (client.playerCharacter.getPrimaryClass() != null) {
        if (client.playerCharacter.getPrimaryClass().id == classId) {
          classChangeOk = false;
          addOutGoingMessage(client, "message", "#messages.classes.already_have");
        }
      }
    }

    if (classChangeOk) {
      changed = true;

      boolean hasClass = false;
      // Save class change
      ResultSet classCheck =
          Server.userDB.askDB(
              "select Id from character_class where ClassId = "
                  + classId
                  + " and CharacterId = "
                  + client.playerCharacter.getDBId());

      try {
        if (classCheck.next()) {
          hasClass = true;
        }
        classCheck.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }

      Server.userDB.updateDB(
          "update character_class set Type = 0 where CharacterId = "
              + client.playerCharacter.getDBId()
              + " and Type = "
              + classType);

      if (hasClass) {
        Server.userDB.updateDB(
            "update character_class set Type = "
                + classType
                + " where CharacterId = "
                + client.playerCharacter.getDBId()
                + " and ClassId = "
                + classId);
      } else {
        Server.userDB.updateDB(
            "insert into character_class (CharacterId, ClassId, ClassLevel, ClassXP, Type) values ("
                + client.playerCharacter.getDBId()
                + ","
                + classId
                + ",1,0,"
                + classType
                + ")");
      }

      if (classType == 1) {
        client.playerCharacter.setPrimaryClass(ServerGameInfo.classDef.get(classId));
      } else if (classType == 2) {
        client.playerCharacter.setSecondaryClass(ServerGameInfo.classDef.get(classId));
      }
      client.playerCharacter.loadAbilities();
      addOutGoingMessage(
          client, "abilitydata", "0/" + client.playerCharacter.getAbilitiesAsString());

      addOutGoingMessage(client, "class_learn", classId + "," + classType);
      addOutGoingMessage(
          client, "abilitydata", "0/" + client.playerCharacter.getAbilitiesAsString());
      ConnectHandler.sendActionbar(client, false);
    }
    return changed;
  }
}
