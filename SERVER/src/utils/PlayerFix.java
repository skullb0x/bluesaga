package utils;

import java.sql.ResultSet;
import java.sql.SQLException;

import game.ServerSettings;
import network.Server;

public class PlayerFix {

  public static void dbFix(boolean removeChars) {

    if (!ServerSettings.DEV_MODE) {
      if (removeChars) {
        /* Permanent deletion */
        // Players under level 10
        // Not bought anything
        // All items are removed

        /* Soft deletion */
        // Players from level 10 or above
        // If player has bought anything
        // All items except equipped are removed
        // Bought skins are left untouched

        ServerMessage.printMessage("Removing deleted characters...", false);

        // Soft-deleting all characters that have been deleted three days ago or more
        String threeDaysAgo = TimeUtils.getTimeAdd(-3 * 24 * 60 * 60);

        Server.userDB.updateDB(
            "update user_character set Deleted = 'Yes' where Deleted < '" + threeDaysAgo + "'");

        ServerMessage.printMessage("Re-indexing character items...", false);

        // Re-indexing inventory items
        ResultSet items = Server.userDB.askDB("select Id from character_item");
        int newId = 1;
        try {
          while (items.next()) {
            Server.userDB.updateDB(
                "update character_item set Id = " + newId + " where Id = " + items.getInt("Id"));
            newId++;
          }
          items.close();
        } catch (SQLException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }

        // Re-indexing chest items
        items = Server.userDB.askDB("select Id from user_chest");
        newId = 1;
        try {
          while (items.next()) {
            Server.userDB.updateDB(
                "update user_chest set Id = " + newId + " where Id = " + items.getInt("Id"));
            newId++;
          }
          items.close();
        } catch (SQLException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * Permanently remove character and items
   * @param charId
   */
  public static void permDeleteChar(int charId) {
    if (charId > 0) {
      ServerMessage.printMessage("Deleting char: " + charId, false);
      Server.userDB.updateDB("delete from character_ability where CharacterId = " + charId);
      Server.userDB.updateDB("delete from character_actionbar where CharacterId = " + charId);
      Server.userDB.updateDB("delete from character_class where CharacterId = " + charId);
      Server.userDB.updateDB("delete from character_container where CharacterId = " + charId);
      Server.userDB.updateDB("delete from character_crew where CharacterId = " + charId);
      Server.userDB.updateDB("delete from character_item where CharacterId = " + charId);
      Server.userDB.updateDB("delete from character_key where CharacterId = " + charId);
      Server.userDB.updateDB("delete from character_kills where CharacterId = " + charId);
      Server.userDB.updateDB("delete from character_portal where CharacterId = " + charId);
      Server.userDB.updateDB("delete from character_quest where CharacterId = " + charId);
      Server.userDB.updateDB("delete from character_recipe where CharacterId = " + charId);
      Server.userDB.updateDB("delete from character_skill where CharacterId = " + charId);
      Server.userDB.updateDB("delete from character_soul where CharacterId = " + charId);
      Server.userDB.updateDB("delete from user_character where Id = " + charId);
      Server.userDB.updateDB("delete from user_friend where FriendCharacterId = " + charId);
    }
  }

  /**
   * Soft delete character
   * @param charId
   */
  public static void softDeleteChar(int charId) {
    if (charId > 0) {
      //Server.userDB.updateDB("delete from character_ability where CharacterId = "+charId);
      Server.userDB.updateDB("delete from character_actionbar where CharacterId = " + charId);
      Server.userDB.updateDB("delete from character_class where CharacterId = " + charId);
      Server.userDB.updateDB("delete from character_container where CharacterId = " + charId);
      Server.userDB.updateDB("delete from character_crew where CharacterId = " + charId);
      Server.userDB.updateDB(
          "delete from character_item where CharacterId = " + charId + " and Equipped = 0");
      Server.userDB.updateDB("delete from character_key where CharacterId = " + charId);
      Server.userDB.updateDB("delete from character_kills where CharacterId = " + charId);
      Server.userDB.updateDB("delete from character_portal where CharacterId = " + charId);
      Server.userDB.updateDB("delete from character_quest where CharacterId = " + charId);
      Server.userDB.updateDB("delete from character_recipe where CharacterId = " + charId);
      //Server.userDB.updateDB("delete from character_skill where CharacterId = "+charId);
      Server.userDB.updateDB("delete from character_soul where CharacterId = " + charId);
      //Server.userDB.updateDB("delete from user_character where Id = "+charId);
      Server.userDB.updateDB("delete from user_friend where FriendCharacterId = " + charId);
    }
  }
}
