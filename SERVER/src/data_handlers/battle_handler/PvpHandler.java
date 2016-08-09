package data_handlers.battle_handler;

import game.ServerSettings;

import java.util.Map;

import map.AreaEffect;
import network.Client;
import network.Server;
import utils.ServerGameInfo;
import creature.PlayerCharacter;
import data_handlers.BountyHandler;
import data_handlers.Handler;

public class PvpHandler extends Handler {

  public static int playerKillerTime = 5 * 60; // 15 min

  public static boolean canAttackPlayer(PlayerCharacter attacker, PlayerCharacter target) {
    boolean attackOk = true;

    // No attacks indoors
    if (Server.WORLD_MAP
        .getTile(target.getX(), target.getY(), target.getZ())
        .getType()
        .equals("indoors")) {
      attackOk = false;
    }

    if (ServerSettings.PVP) {
      int playerAreaEffectId = attacker.getAreaEffectId();

      // Check if safe zone
      if (ServerGameInfo.areaEffectsDef.containsKey(playerAreaEffectId)) {
        AreaEffect ae = ServerGameInfo.areaEffectsDef.get(playerAreaEffectId);
        if (ae.getGuardedLevel() > 0) {
          attackOk = false;
        }
      }

      // Check if party member
      if (attacker.getParty() != null && target.getParty() != null) {
        if (attacker.getParty().getId() == target.getParty().getId()) {
          attackOk = false;
        }
      }
    } else {
      if (attacker.getDBId() == target.getDBId()) {
        attackOk = true;
      } else if (!Server.WORLD_MAP
          .getTile(target.getX(), target.getY(), target.getZ())
          .getType()
          .equals("arena")) {
        attackOk = false;
      }
    }
    return attackOk;
  }

  public static void playerKillsPlayer(PlayerCharacter attacker, PlayerCharacter target) {
    // GET BOUNTY
    BountyHandler.changeBounty(target, attacker);

    if (target.getPkMarker() == 0) {
      // SET PLAYER KILLER MARK
      setPlayerKillerMark(attacker, 2);
      addOutGoingMessage(attacker.client, "restart_logout", "" + playerKillerTime);
    }
  }

  /****************************************
   * * PLAYER KILLER ! * * *
   ****************************************/
  public static void setPlayerKillerMark(PlayerCharacter attacker, int pkMark) {
    //
    attacker.setPkMarker(pkMark);
    attacker.startPlayerKillerTimer();

    Server.userDB.updateDB(
        "update user_character set PlayerKiller = " + pkMark + " where Id = " + attacker.getDBId());

    String pkData = attacker.getSmallData() + ";" + pkMark;

    for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
      Client s = entry.getValue();
      if (s.Ready
          && isVisibleForPlayer(
              s.playerCharacter, attacker.getX(), attacker.getY(), attacker.getZ())) {
        addOutGoingMessage(s, "pk_status", pkData);
      }
    }
  }

  public static void updatePKTimers() {
    for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
      Client s = entry.getValue();
      if (s.Ready) {
        if (s.playerCharacter != null) {
          if (s.playerCharacter.countdownPKtime()) {
            setPlayerKillerMark(s.playerCharacter, 0);
          }
        }
      }
    }
  }
}
