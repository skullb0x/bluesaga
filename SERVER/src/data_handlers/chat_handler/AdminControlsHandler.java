package data_handlers.chat_handler;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;

import map.Tile;
import network.Client;
import network.Server;
import player_classes.BaseClass;
import utils.ServerGameInfo;
import utils.WebHandler;
import utils.XPTables;
import components.JobSkill;
import components.Stats;
import creature.Creature;
import creature.Npc;
import creature.Creature.CreatureType;
import data_handlers.Handler;
import data_handlers.MapHandler;
import data_handlers.battle_handler.BattleHandler;
import data_handlers.item_handler.InventoryHandler;
import data_handlers.item_handler.Item;

public class AdminControlsHandler extends Handler {

  public static boolean handleMessage(Client client, String chatText) {

    // LEVEL 1 - TRUSTED ACCOUNT
    if (client.playerCharacter.getAdminLevel() > 0) {
      if (levelOneControls(client, chatText)) {
        return true;
      }
    }

    // LEVEL 2 - TRUSTED ACCOUNT
    if (client.playerCharacter.getAdminLevel() > 1) {
      if (levelTwoControls(client, chatText)) {
        return true;
      }
    }

    // LEVEL 3 - ADMIN - OWN CHARACTER
    if (client.playerCharacter.getAdminLevel() > 2) {
      if (levelThreeControls(client, chatText)) {
        return true;
      }
    }

    // LEVEL 4 - SUPER-ADMIN (AKA DEMI-GOD) - OWN CHARACTER
    if (client.playerCharacter.getAdminLevel() > 3) {
      if (levelFourControls(client, chatText)) {
        return true;
      }
    }

    // LEVEL 5 - GOD
    if (client.playerCharacter.getAdminLevel() > 4) {
      if (levelFiveControls(client, chatText)) {
        return true;
      }
    }

    /*
    if(specialCommand && !chatText.toLowerCase().equals("/server stop")){
    	Server.userDB.addChatText("admin",client.playerCharacter.getDBId(),0,chatText.toLowerCase());
    }
    */

    return false;
  }

  /**
   * Level 1 admin controls
   * @param client
   * @param chatText
   * @return
   */
  private static boolean levelOneControls(Client client, String chatText) {
    String chatLower = chatText.toLowerCase();
    if (chatLower.equals("/server restart")) {
      Server.restartServer();
      return true;
    }
    return false;
  }

  /**
   * Level 2 admin controls
   * @param client
   * @param chatText
   * @return
   */
  private static boolean levelTwoControls(Client client, String chatText) {
    String chatLower = chatText.toLowerCase();
    if (chatLower.startsWith("/move ")) {
      String playerName = chatLower.substring(6);
      movePlayer(client, playerName);
      return true;
    }
    if (chatLower.startsWith("/mute ")) {
      String playerName = chatLower.substring(6);
      ChatHandler.mutePlayer(client, playerName);
      return true;
    }
    if (chatLower.startsWith("/changename ")) {
      String nameInfo[] = chatText.substring(12).split(",");

      if (nameInfo.length > 1) {
        String charName = nameInfo[0];
        String newName = nameInfo[1];

        ResultSet nameCheck =
            Server.userDB.askDB(
                "select Id from user_character where lower(Name) = '" + newName + "'");
        try {
          if (nameCheck.next()) {
            addOutGoingMessage(client, "message", "Name already exists!");
          } else {
            Server.userDB.updateDB(
                "update user_character set Name = '"
                    + newName
                    + "' where lower(Name) = '"
                    + charName.toLowerCase()
                    + "' limit 1");
            addOutGoingMessage(client, "message", "You changed player's name to " + newName);

            for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
              Client s = entry.getValue();

              if (s.Ready) {
                if (s.playerCharacter.getName().toLowerCase().equals(charName.toLowerCase())) {
                  addOutGoingMessage(
                      s,
                      "backtologin",
                      "Your name is inappropriate and has been changed by admins");

                  s.RemoveMe = true;
                  break;
                }
              }
            }
          }
          nameCheck.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
      return true;
    }
    return false;
  }

  /**
   * Level 3 admin controls
   * @param client
   * @param chatText
   * @return
   */
  private static boolean levelThreeControls(Client client, String chatText) {
    String chatLower = chatText.toLowerCase();
    if (chatLower.startsWith("/goto ")) {
      teleportAdmin(client, chatText);
      return true;
    }
    if (chatLower.startsWith("/gotoxyz ")) {
      teleportAdmin(client, chatText);
      return true;
    }
    if (chatLower.startsWith("/ban ")) {
      String banInfo[] = chatText.substring(5).split(",");

      if (banInfo.length > 1) {
        String charName = banInfo[0];
        int hours = Integer.parseInt(banInfo[1]);
        banPlayer(client, charName, hours);
      }
      return true;
    }
    if (chatLower.startsWith("/givequest ")) {
      String questInfo[] = chatText.substring(11).split(",");
      if (questInfo.length > 1) {
        String charName = questInfo[0];
        int questId = Integer.parseInt(questInfo[1]);
        giveQuest(client, charName, questId);
      }
      return true;
    }

    if (chatLower.startsWith("/banip ")) {
      String charName = chatText.substring(7);
      banIp(client, charName);
      return true;
    }
    return false;
  }

  /**
   * Level 4 admin controls
   * @param client
   * @param chatText
   * @return
   */
  private static boolean levelFourControls(Client client, String chatText) {
    String chatLower = chatText.toLowerCase();
    if (chatLower.startsWith("/night")) {
      MapHandler.worldTimeItr = MapHandler.worldNightTime - 1;
      return true;
    }

    if (chatLower.startsWith("/day")) {
      MapHandler.worldTimeItr = MapHandler.worldDayDuration - 1;
      return true;
    }

    if (chatLower.startsWith("/ability ")) {
      String abilityInfo = chatText.substring(9);
      try {
        int abilityId = Integer.parseInt(abilityInfo);

        if (ServerGameInfo.abilityDef.containsKey(abilityId)) {
          client.playerCharacter.addAbility(ServerGameInfo.abilityDef.get(abilityId));
          addOutGoingMessage(
              client, "abilitydata", "0/" + client.playerCharacter.getAbilitiesAsString());
          addOutGoingMessage(
              client, "message", ServerGameInfo.abilityDef.get(abilityId).getName() + " added!");
        }
      } catch (NumberFormatException e) {
        // NOT A NUMBER!
      }
      return true;
    }

    if (chatLower.startsWith("/bring ")) {
      String charName = chatText.substring(7);
      Client player = findPlayerClient(charName);

      if (player != null) {
        teleportAdmin(
            player,
            "/gotoxyz "
                + client.playerCharacter.getX()
                + ","
                + client.playerCharacter.getY()
                + ","
                + client.playerCharacter.getZ());
      }
      return true;
    }

    if (chatLower.startsWith("/level ")) {
      try {
        int level = Integer.parseInt(chatText.substring(7));

        if (XPTables.nextLevelXP.containsKey(level)) {
          client.playerCharacter.setLevel(level);
          client.playerCharacter.setXP(0);

          String levelUpData = client.playerCharacter.levelUp();

          addOutGoingMessage(client, "level_up", levelUpData);
        }

      } catch (NumberFormatException e) {
        // NOT A NUMBER!
      }
      return true;
    }

    if (chatLower.startsWith("/setclass ")) {
      String classInfo[] = chatText.substring(10).split(",");
      if (classInfo.length == 3) {
        try {
          int classType = Integer.parseInt(classInfo[0]);
          int classId = Integer.parseInt(classInfo[1]);
          int level = Integer.parseInt(classInfo[2]);

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
                    + ", ClassLevel = "
                    + level
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
                    + ","
                    + level
                    + ",0,"
                    + classType
                    + ")");
          }

          BaseClass newClass = new BaseClass(ServerGameInfo.classDef.get(classId));
          newClass.level = level;

          if (classType == 1) {
            client.playerCharacter.setPrimaryClass(newClass);
            addOutGoingMessage(client, "message", "New primary class set!");
          } else if (classType == 2) {
            client.playerCharacter.setSecondaryClass(newClass);
            addOutGoingMessage(client, "message", "New secondary class set!");
          }

        } catch (NumberFormatException e) {
          // NOT A NUMBER!
        }
      } else {
        addOutGoingMessage(
            client,
            "message",
            "/setclass classType (1 = primary, 2 = secondary),classId,classLevel");
      }
      return true;
    }

    if (chatLower.startsWith("/setlevel ")) {
      String levelInfo[] = chatText.substring(10).split(",");
      if (levelInfo.length == 2) {
        String charName = levelInfo[0];
        try {
          int level = Integer.parseInt(levelInfo[1]);

          Client player = findPlayerClient(charName);

          if (player != null) {
            if (XPTables.nextLevelXP.containsKey(level)) {
              player.playerCharacter.setLevel(level);
              player.playerCharacter.setXP(0);

              String levelUpData = player.playerCharacter.levelUp();

              addOutGoingMessage(player, "level_up", levelUpData);
              addOutGoingMessage(
                  client,
                  "message",
                  "Changed player " + player.playerCharacter.getName() + " level to " + level);
            }
          }

        } catch (NumberFormatException e) {
          // NOT A NUMBER!
        }
      }
      return true;
    }

    if (chatLower.startsWith("/spawn ")) {
      String spawnInfo[] = chatText.substring(7).split(",");

      boolean success = true;

      Npc m = null;

      try {
        int spawnId = Integer.parseInt(spawnInfo[0]);
        if (spawnId > 0) {

          if (spawnId < 87 && spawnId != 83) {
            m =
                new Npc(
                    ServerGameInfo.creatureDef.get(spawnId),
                    client.playerCharacter.getX(),
                    client.playerCharacter.getY(),
                    client.playerCharacter.getZ());
            m.setAggroType(2);
          }
        }
      } catch (NumberFormatException e) {
        // NOT A NUMBER!
        success = false;
      }

      if (spawnInfo.length == 3) {
        if (m != null) {
          try {
            int gloomyNr = Integer.parseInt(spawnInfo[1]);
            int titanNr = Integer.parseInt(spawnInfo[2]);
            m.setAggroType(2);
            if (titanNr == 0) {
              m.turnSpecial(gloomyNr);
            } else {
              m.turnSpecial(gloomyNr);
              m.turnTitan(true);
            }

          } catch (NumberFormatException e) {
            // NOT A NUMBER!
            success = false;
          }
        }
      }

      if (success && m != null) {
        Server.WORLD_MAP.addMonsterSpawn(
            m,
            client.playerCharacter.getX(),
            client.playerCharacter.getY(),
            client.playerCharacter.getZ());
      } else {
        addOutGoingMessage(
            client, "message", "/spawn monsterId,gloomyNr,titanOrNot -> ex: /spawn 4,0,0");
      }
      return true;
    }

    if (chatLower.startsWith("/skill ")) {
      String skillLevel[] = chatText.substring(7).split(",");
      if (skillLevel.length > 1) {
        try {
          int skillId = Integer.parseInt(skillLevel[0]);
          int level = Integer.parseInt(skillLevel[1]);

          if (skillId > 0 && level > 0 && level < 200) {
            if (client.playerCharacter.getSkill(skillId) != null) {
              Server.userDB.updateDB(
                  "update character_skill set Level = "
                      + level
                      + " where SkillId = "
                      + skillId
                      + " and CharacterId = "
                      + client.playerCharacter.getDBId());
              client.playerCharacter.getSkill(skillId).setLevel(level);
              addOutGoingMessage(
                  client,
                  "message",
                  client.playerCharacter.getSkill(skillId).getName() + " level set to " + level);
            }
          }
        } catch (NumberFormatException e) {
          // NOT A NUMBER!
        }
      }
      return true;
    }

    if (chatLower.startsWith("/getstat ")) {
      String creatureInfo[] = chatText.substring(9).split(",");

      // CreatureId, StatName

      if (creatureInfo.length == 2) {
        boolean foundStat = false;

        try {
          int creatureId = Integer.parseInt(creatureInfo[0]);
          String statName = creatureInfo[1];

          int statValue = 0;

          Npc c = ServerGameInfo.creatureDef.get(creatureId);

          // Check if item coordinate
          if (c.coords.containsKey(statName)) {
            foundStat = true;
            statValue = c.coords.get(statName);
          }

          // Check if customization coordinate
          if (c.getCustomization().coords.containsKey(statName)) {
            foundStat = true;
            statValue = c.getCustomization().coords.get(statName);
          }

          // Check if stat
          Stats stats = c.getStats();

          if (stats.getHashMap().containsKey(statName.toUpperCase())) {
            foundStat = true;
            statValue = c.getRawStat(statName.toUpperCase());
          }

          if (statName.toUpperCase().equals("GIVEXP")) {
            foundStat = true;
            statValue = c.getGiveXP();
          }

          if (!foundStat) {
            for (int i = 1; i < 7; i++) {
              if (statName.equals("Ability" + i)) {
                foundStat = true;
                break;
              }
            }
          }

          if (foundStat) {
            addOutGoingMessage(
                client, "message", c.getName() + " has " + statValue + " in " + statName);
          } else {
            StringBuilder statMessage = new StringBuilder(1000);
            statMessage.append("Stats does not exist! Available stats: ");

            for (Iterator<String> iter = stats.getHashMap().keySet().iterator(); iter.hasNext(); ) {
              String key = iter.next().toString();
              statMessage.append(key).append(", ");
            }

            for (Iterator<String> iter = c.coords.keySet().iterator(); iter.hasNext(); ) {
              String key = iter.next().toString();
              statMessage.append(key).append(", ");
            }

            for (Iterator<String> iter = c.getCustomization().coords.keySet().iterator();
                iter.hasNext();
                ) {
              String key = iter.next().toString();
              statMessage.append(key).append(", ");
            }

            statMessage.append(
                "GiveXP, Ability1, Ability2, Ability3, Ability4, Ability5, Ability6");

            addOutGoingMessage(client, "message", statMessage.toString());
          }
        } catch (NumberFormatException e) {
          // NOT A NUMBER!
        }

      } else {
        addOutGoingMessage(client, "message", "/getstat creatureId,statName");
      }
      return true;
    }

    if (chatLower.startsWith("/setstat ")) {
      String creatureInfo[] = chatText.substring(9).split(",");

      // CreatureId, StatName, StatValue

      if (creatureInfo.length == 3) {

        try {
          int creatureId = Integer.parseInt(creatureInfo[0]);
          String statName = creatureInfo[1];
          int statValue = Integer.parseInt(creatureInfo[2]);

          Creature c = new Creature(creatureId, 0, 0, 0);
          if (c.getName() != null) {
            boolean foundStat = false;

            // Check if item coordinate
            if (c.coords.containsKey(statName)) {
              foundStat = true;
            }

            // Check if customization coordinate
            if (c.getCustomization().coords.containsKey(statName)) {
              foundStat = true;
            }

            // Check if stat
            Stats stats = c.getStats();

            if (stats.getHashMap().containsKey(statName.toUpperCase())) {
              statName = statName.toUpperCase();
              foundStat = true;
            }

            if (!foundStat) {
              for (int i = 1; i < 7; i++) {
                if (statName.equals("Ability" + i)) {
                  foundStat = true;
                  break;
                }
              }
            }

            if (statName.equals("GiveXP")) {
              foundStat = true;
            }

            if (foundStat) {
              Server.gameDB.updateDB(
                  "update creature set "
                      + statName
                      + " = "
                      + statValue
                      + " where Id = "
                      + creatureId);

              addOutGoingMessage(
                  client, "message", c.getName() + " has " + statValue + " in " + statName);
            } else {
              StringBuilder statMessage = new StringBuilder(1000);
              statMessage.append("Stats does not exist! Available stats: ");

              for (Iterator<String> iter = stats.getHashMap().keySet().iterator();
                  iter.hasNext();
                  ) {
                String key = iter.next().toString();
                statMessage.append(key).append(", ");
              }

              for (Iterator<String> iter = c.coords.keySet().iterator(); iter.hasNext(); ) {
                String key = iter.next().toString();
                statMessage.append(key).append(", ");
              }

              for (Iterator<String> iter = c.getCustomization().coords.keySet().iterator();
                  iter.hasNext();
                  ) {
                String key = iter.next().toString();
                statMessage.append(key).append(", ");
              }

              statMessage.append(
                  "GiveXP, Ability1, Ability2, Ability3, Ability4, Ability5, Ability6");
              addOutGoingMessage(client, "message", statMessage.toString());
            }

          } else {
            addOutGoingMessage(client, "message", "Creature does not exist!");
          }
        } catch (NumberFormatException e) {
          // NOT A NUMBER!
        }

      } else {
        addOutGoingMessage(client, "message", "/setstat creatureId,statName,statValue");
      }
      return true;
    }
    return false;
  }

  /**
   * Level 5 admin controls
   * @param client
   * @param chatText
   * @return
   */
  private static boolean levelFiveControls(Client client, String chatText) {
    String chatLower = chatText.toLowerCase();
    // ADD ADMIN EVENT MESSAGE
    if (chatLower.startsWith("/givexp ")) {
      String playerInfo[] = chatText.substring(8).split(",");

      if (playerInfo.length == 2) {
        try {
          String charName = playerInfo[0].toLowerCase();
          int xp = Integer.parseInt(playerInfo[1]);

          Client player = findPlayerClient(charName);

          if (player != null) {
            BattleHandler.addXP(player, xp);
            player.playerCharacter.saveInfo();
            addOutGoingMessage(
                client,
                "message",
                "You have given " + player.playerCharacter.getName() + " " + xp + " XP");
          } else {
            addOutGoingMessage(client, "message", "Player is not online!");
          }
        } catch (NumberFormatException e) {
          // NOT A NUMBER!
        }

      } else {
        addOutGoingMessage(client, "message", "/givexp playerName,xp");
      }
      return true;
    }

    if (chatLower.startsWith("/givebounty ")) {
      String playerInfo[] = chatText.substring(12).split(",");

      if (playerInfo.length == 2) {
        try {
          String charName = playerInfo[0].toLowerCase();
          int bounty = Integer.parseInt(playerInfo[1]);

          Client player = findPlayerClient(charName);

          if (player != null) {
            player.playerCharacter.setBounty(bounty);
            addOutGoingMessage(player, "setbounty", "" + bounty);
            Server.userDB.updateDB(
                "update user_character set Bounty = "
                    + bounty
                    + " where Id = "
                    + player.playerCharacter.getDBId());
            addOutGoingMessage(
                client,
                "message",
                "You have given " + player.playerCharacter.getName() + " " + bounty + " in bounty");
          } else {
            addOutGoingMessage(client, "message", "Player is not online!");
          }
        } catch (NumberFormatException e) {
          // NOT A NUMBER!
        }

      } else {
        addOutGoingMessage(client, "message", "/givebounty playerName,bounty");
      }
      return true;
    }

    if (chatLower.startsWith("/giveskill ")) {
      String skillInfo[] = chatText.substring(11).split(",");

      if (skillInfo.length == 3) {
        try {
          String charName = skillInfo[0];
          int skillId = Integer.parseInt(skillInfo[1]);
          int skillLevel = Integer.parseInt(skillInfo[2]);

          Client player = findPlayerClient(charName);

          if (player != null) {
            JobSkill skill = player.playerCharacter.getSkill(skillId);

            if (skill != null) {
              skill.setLevel(skillLevel);
              player.playerCharacter.saveInfo();
              addOutGoingMessage(
                  player,
                  "message",
                  "Your skill "
                      + skill.getName()
                      + " has been changes, please re-log to view changes.");
            }
          } else {
            addOutGoingMessage(client, "message", "Player is not online!");
          }

        } catch (NumberFormatException e) {
          // NOT A NUMBER!
        }

      } else {
        addOutGoingMessage(client, "message", "Write /giveskill playerName,skillId,skillLevel");
        addOutGoingMessage(
            client,
            "message",
            "1 = Blades, 2 = Axes, 3 = Bows, 4 = Blunts, 5 = Unarmed, 6 = Fire, 7 = Ice, 8 = Shock, 9 = Cooking, 11 = Fishing, 12 = Herbalism");
      }
      return true;
    }

    if (chatLower.startsWith("/giveadmin ")) {
      String adminInfo[] = chatText.substring(11).split(",");

      if (adminInfo.length == 2) {
        try {
          String charName = adminInfo[0];
          int adminLevel = Integer.parseInt(adminInfo[1]);

          if (adminLevel < 5) {
            Client player = findPlayerClient(charName);

            boolean foundPlayer = false;

            if (player != null) {
              foundPlayer = true;
              player.playerCharacter.setAdminLevel(adminLevel);
              Server.userDB.updateDB(
                  "update user_character set AdminLevel = "
                      + adminLevel
                      + " where Id = "
                      + player.playerCharacter.getDBId());
              addOutGoingMessage(player, "message", "You've been granted admin rights!");

            } else {
              // Player not online, check db for character
              try {
                ResultSet charInfo =
                    Server.userDB.askDB(
                        "select Id from user_character where lower(Name) = '"
                            + charName.toLowerCase()
                            + "'");
                if (charInfo.next()) {
                  foundPlayer = true;
                  Server.userDB.updateDB(
                      "update user_character set AdminLevel = "
                          + adminLevel
                          + " where Id = "
                          + charInfo.getInt("Id"));
                }
                charInfo.close();
              } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
              }
            }

            if (foundPlayer) {
              addOutGoingMessage(
                  client,
                  "message",
                  "You've given " + charName + " admin level " + adminLevel + "!");
            } else {
              addOutGoingMessage(client, "message", "Player not found!");
            }
          }

        } catch (NumberFormatException e) {
          // NOT A NUMBER!
        }
      }
      return true;
    }

    if (chatLower.startsWith("/skin ")) {
      String skinInfo[] = chatText.substring(6).split(",");
      if (skinInfo.length > 1) {
        try {
          int charId = Integer.parseInt(skinInfo[0]);
          int skinId = Integer.parseInt(skinInfo[1]);
          // Check if skin exist
          if (ServerGameInfo.itemDef.containsKey(skinId)) {
            // Check if user exist
            try {
              ResultSet playerInfo =
                  Server.userDB.askDB("select Id, Name from user_character where Id = " + charId);
              if (playerInfo.next()) {
                String playerName = playerInfo.getString("Name");
                Server.userDB.updateDB(
                    "insert into character_skin (CharacterId, ItemId) values ("
                        + charId
                        + ","
                        + skinId
                        + ")");
                addOutGoingMessage(
                    client,
                    "message",
                    "Skin "
                        + ServerGameInfo.itemDef.get(skinId).getName()
                        + " given to "
                        + playerName);
              }
              playerInfo.close();
            } catch (SQLException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }
          }

        } catch (NumberFormatException e) {
          // NOT A NUMBER!
          addOutGoingMessage(client, "message", "wrong syntax: /skin charId, skinId");
        }
      }
      return true;
    }

    if (chatLower.startsWith("/item ")) {
      String itemInfo[] = chatText.substring(6).split(",");

      Item newItem = null;

      if (itemInfo.length > 0) {
        try {
          int itemId = Integer.parseInt(itemInfo[0]);

          if (ServerGameInfo.itemDef.containsKey(itemId)) {
            newItem = new Item(ServerGameInfo.itemDef.get(itemId));
          }

        } catch (NumberFormatException e) {
          // NOT A NUMBER!
        }
      }

      if (itemInfo.length == 3) {
        try {
          int modifierId = Integer.parseInt(itemInfo[1]);
          int magicId = Integer.parseInt(itemInfo[2]);

          if (newItem != null) {
            newItem.setModifierId(modifierId);
            newItem.setMagicId(magicId);
          }
        } catch (NumberFormatException e) {
          // NOT A NUMBER!
        }
      }

      if (newItem != null) {
        InventoryHandler.addItemToInventory(client, newItem);
      } else {
        addOutGoingMessage(client, "message", "Write /item itemId,modifierId,magicId");
      }
      return true;
    }

    if (chatLower.startsWith("/give ")) {
      String giftInfo[] = chatText.substring(6).split(",");

      if (giftInfo.length == 3) {
        int charId = Integer.parseInt(giftInfo[0]);
        int itemId = Integer.parseInt(giftInfo[1]);
        int modifierId = Integer.parseInt(giftInfo[2]);

        // FIND FREE SPACE IN PERSONAL CHEST
        Integer freeBox[][] = new Integer[4][3];

        for (int i = 0; i < 4; i++) {
          for (int j = 0; j < 3; j++) {
            freeBox[i][j] = 0;
          }
        }

        ResultSet chestItems =
            Server.userDB.askDB(
                "select InventoryPos from character_item where CharacterId = " + charId);
        try {
          while (chestItems.next()) {
            String pos[] = chestItems.getString("InventoryPos").split(",");
            int posX = Integer.parseInt(pos[0]);
            int posY = Integer.parseInt(pos[1]);

            freeBox[posX][posY] = 1;
          }
          chestItems.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }

        int freeX = 0;
        int freeY = 0;
        boolean foundSpace = false;

        for (int j = 0; j < 3; j++) {
          for (int i = 0; i < 4; i++) {
            if (freeBox[i][j] == 0) {
              freeX = i;
              freeY = j;
              foundSpace = true;
              break;
            }
          }
          if (foundSpace) {
            break;
          }
        }

        if (foundSpace) {
          Server.userDB.updateDB(
              "insert into character_item (CharacterId, ItemId, Equipped, InventoryPos, Nr, ModifierId, MagicId, GraphicsNr) values ("
                  + charId
                  + ","
                  + itemId
                  + ",0,'"
                  + freeX
                  + ","
                  + freeY
                  + "',1,"
                  + modifierId
                  + ",0,0)");
          addOutGoingMessage(
              client,
              "message",
              "Item "
                  + itemId
                  + ", modifier "
                  + modifierId
                  + " added to personal chest of char "
                  + charId);
        }
      } else {
        addOutGoingMessage(
            client, "message", "Wrong command, written /give charid,itemid,modifierid");
      }
      return true;
    }

    if (chatLower.equals("/server stop")) {
      Server.stopServer();
      return true;
    }
    return false;
  }

  /**
   * Helper methods
   */
  private static void movePlayer(Client client, String playerName) {

    ResultSet playerInfo =
        Server.userDB.askDB(
            "select Id, CheckpointId from user_character where lower(Name) like '"
                + playerName
                + "'");
    try {
      if (playerInfo.next()) {
        int playerId = playerInfo.getInt("Id");
        int CheckpointId = playerInfo.getInt("CheckpointId");

        ResultSet checkpointInfo =
            Server.mapDB.askDB("select X,Y,Z from checkpoint where Id = " + CheckpointId);
        if (checkpointInfo.next()) {
          int checkpointX = checkpointInfo.getInt("X");
          int checkpointY = checkpointInfo.getInt("Y");
          int checkpointZ = checkpointInfo.getInt("Z");

          Server.userDB.updateDB(
              "update user_character set X = "
                  + checkpointX
                  + ", Y = "
                  + checkpointY
                  + ", Z = "
                  + checkpointZ
                  + " where Id = "
                  + playerId);
          addOutGoingMessage(client, "message", playerName + " has been moved back to checkpoint");
        }
        checkpointInfo.close();
      }
      playerInfo.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private static void teleportAdmin(Client client, String message) {
    // /goto name
    int targetX = client.playerCharacter.getX();
    int targetY = client.playerCharacter.getY();
    int targetZ = client.playerCharacter.getZ();

    boolean teleportOk = true;

    if (message.startsWith("/gotoxyz ")) {
      String coord[] = message.substring(9).split(",");
      if (coord.length == 3) {
        try {
          int gotoX = Integer.parseInt(coord[0]);
          int gotoY = Integer.parseInt(coord[1]);
          int gotoZ = Integer.parseInt(coord[2]);

          if (Server.WORLD_MAP.getTile(gotoX, gotoY, gotoZ) != null) {
            targetX = gotoX;
            targetY = gotoY;
            targetZ = gotoZ;
          } else {
            teleportOk = false;
          }
        } catch (NumberFormatException e) {
          teleportOk = false;
        }
      }
    } else {
      String targetName = message.substring(6);

      ResultSet targetInfo =
          Server.userDB.askDB(
              "select X, Y, Z from user_character where lower(Name) like '" + targetName + "'");
      try {
        if (targetInfo.next()) {
          targetX = targetInfo.getInt("X");
          targetY = targetInfo.getInt("Y");
          targetZ = targetInfo.getInt("Z");

          boolean foundSpot = false;

          for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
              if (Server.WORLD_MAP.getTile(targetX + i, targetY + j, targetZ) != null) {
                if (Server.WORLD_MAP.getTile(targetX + i, targetY + j, targetZ).isPassableType()
                    && Server.WORLD_MAP.getTile(targetX + i, targetY + j, targetZ).getDoorId()
                        == 0) {
                  targetX += i;
                  targetY += j;
                  foundSpot = true;
                  break;
                }
              }
            }
            if (foundSpot) {
              break;
            }
          }

          teleportOk = foundSpot;
        }
        targetInfo.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }

    if (teleportOk) {
      client.playerCharacter.setAggro(null);

      // FREE TILE FROM PLAYER
      Tile oldTile =
          Server.WORLD_MAP.getTile(
              client.playerCharacter.getX(),
              client.playerCharacter.getY(),
              client.playerCharacter.getZ());

      if (oldTile != null) {
        oldTile.setOccupant(CreatureType.None, null);
      }

      // CANCEL AGGRO ON ALL CHASING MONSTERS
      for (Iterator<Npc> iter = Server.WORLD_MAP.getMonsters().values().iterator();
          iter.hasNext();
          ) {
        Npc m = iter.next();
        if (m.isAggro()
            && m.getAggroTarget().getCreatureType() == CreatureType.Player
            && m.getAggroTarget().getDBId() == client.playerCharacter.getDBId()) {
          m.turnAggroOff();
        }
      }

      // SEND INFO ABOUT CHANGED AGGRO
      // SEND INFO ABOUT LEAVING AREA
      // TO OTHER PLAYERS

      for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
        Client s = entry.getValue();

        if (s.Ready) {
          if (s.playerCharacter.getDBId() != client.playerCharacter.getDBId()
              && isVisibleForPlayer(
                  s.playerCharacter,
                  client.playerCharacter.getX(),
                  client.playerCharacter.getY(),
                  client.playerCharacter.getZ())) {
            addOutGoingMessage(s, "creature_remove", client.playerCharacter.getSmallData());
          }
        }
      }

      // Move player to new position
      client.playerCharacter.setAggro(null);
      client.playerCharacter.walkTo(targetX, targetY, targetZ);

      // Check if player should have boat or not
      if (Server.WORLD_MAP.getTile(targetX, targetY, targetZ).isWater()) {
        if (client.playerCharacter.getShip() != null) {
          client.playerCharacter.getShip().setShow(true);
        }
      } else {
        if (client.playerCharacter.getShip() != null) {
          client.playerCharacter.getShip().setShow(false);
        }
      }

      Server.WORLD_MAP
          .getTile(targetX, targetY, targetZ)
          .setOccupant(CreatureType.Player, client.playerCharacter);

      Server.userDB.updateDB(
          "update user_character set X = "
              + targetX
              + ", Y = "
              + targetY
              + ", Z = "
              + targetZ
              + " where Id ="
              + client.playerCharacter.getDBId());

      // SEND WALK ONE TILE ANIMATION
      //String walkData = targetX+","+targetY+","+targetZ+","+client.playerCharacter.getStat("SPEED");
      //addOutGoingMessage(client,"canwalk",walkData);

      MapHandler.sendScreenData(client);

      // SEND NEW POSITION TO PLAYERS NEARBY
      for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
        Client s = entry.getValue();

        if (s.Ready) {
          if (s.playerCharacter.getDBId() != client.playerCharacter.getDBId()
              && isVisibleForPlayer(
                  s.playerCharacter,
                  client.playerCharacter.getX(),
                  client.playerCharacter.getY(),
                  client.playerCharacter.getZ())) {
            addOutGoingMessage(s, "creature_appear", client.playerCharacter.getSmallData());
            if (client.playerCharacter.getShip() != null
                && client.playerCharacter.getShip().isShow()) {
              addOutGoingMessage(
                  s,
                  "goboat",
                  client.playerCharacter.getSmallData()
                      + ";"
                      + client.playerCharacter.getShip()
                      + ",1");
            }
          }
        }
      }
    }
  }

  private static void banPlayer(Client client, String playerName, int hours) {
    ResultSet playerInfo =
        Server.userDB.askDB(
            "select Id, UserId from user_character where lower(Name) like '" + playerName + "'");
    try {
      if (playerInfo.next()) {
        int userId = playerInfo.getInt("UserId");

        String check = "i349vdlkna39423nnvdmn32498vckljsf932482094fs213";

        String banMessage = "You have been banned";
        if (hours < 100) {
          banMessage += " for " + hours + " hours";
        }

        try {
          WebHandler.sendPost(
              "http://www.bluesaga.org/server/banPlayer.php",
              "id=" + userId + "&hours=" + hours + "&check=" + check);
          String playerLower = playerName.toLowerCase();
          for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
            Client s = entry.getValue();

            if (s.Ready) {
              if (s.playerCharacter.getName().toLowerCase().equals(playerLower)) {
                addOutGoingMessage(s, "backtologin", banMessage);

                s.RemoveMe = true;
                break;
              }
            }
          }

          if (hours < 100) {
            addOutGoingMessage(
                client, "message", "You have banned " + playerName + " for " + hours + " hours");
          } else {
            addOutGoingMessage(client, "message", "You have banned " + playerName + " forever");
          }

        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
      playerInfo.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private static void banIp(Client client, String playerName) {
    ResultSet playerInfo =
        Server.userDB.askDB(
            "select Id, UserId from user_character where lower(Name) like '" + playerName + "'");
    try {
      if (playerInfo.next()) {

        String check = "i349vdlkna39423nnvdmn32498vckljsf932482094fs213";

        String banMessage = "You have been banned";

        String ip = "not found";

        try {
          String playerLower = playerName.toLowerCase();
          for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
            Client s = entry.getValue();

            if (s.Ready) {
              if (s.playerCharacter.getName().toLowerCase().equals(playerLower)) {
                addOutGoingMessage(s, "backtologin", banMessage);
                ip = s.IP;
                s.RemoveMe = true;
                break;
              }
            }
          }
          if (!ip.equals("not found")) {
            WebHandler.sendPost(
                "http://www.bluesaga.org/server/banIp.php",
                "ip=" + ip + "&check=" + check + "&admin=" + client.playerCharacter.getName());
            addOutGoingMessage(client, "message", "You have banned the ip " + ip);
          } else {
            addOutGoingMessage(client, "message", "IP not found!");
          }
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
      playerInfo.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public static void giveQuest(Client client, String charName, int questId) {
    ResultSet playerInfo =
        Server.userDB.askDB("select Id from user_character where lower(Name) = '" + charName + "'");
    try {
      if (playerInfo.next()) {
        int playerId = playerInfo.getInt("Id");
        // Check if player has quest
        ResultSet questCheck =
            Server.userDB.askDB(
                "select Id from character_quest where QuestId = "
                    + questId
                    + " and CharacterId = "
                    + playerId);
        if (questCheck.next()) {
          // Player has quest, complete it
          Server.userDB.updateDB(
              "update character_quest set Status = 3 where QuestId = "
                  + questId
                  + " and CharacterId = "
                  + playerId);
        } else {
          // Player does not have quest, add it as completed
          Server.userDB.updateDB(
              "insert into character_quest (CharacterId, QuestId, Status) values ("
                  + playerId
                  + ","
                  + questId
                  + ",3)");
        }
        questCheck.close();
        addOutGoingMessage(client, "message", "Completed quest for " + charName);
      } else {
        addOutGoingMessage(client, "message", "Player " + charName + " not found");
      }
      playerInfo.close();
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public static Client findPlayerClient(String charName) {
    Client player = null;

    for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
      Client s = entry.getValue();

      if (s.Ready) {
        if (s.playerCharacter.getName().toLowerCase().equals(charName)) {
          player = s;
          break;
        }
      }
    }
    return player;
  }
}
