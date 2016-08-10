package data_handlers.party_handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import data_handlers.DataHandlers;
import data_handlers.Handler;
import data_handlers.Message;
import network.Client;
import network.Server;

public class PartyHandler extends Handler {

  public static Vector<Party> parties;
  public static int levelRangeCap = 15;

  private static int partyIndex = 0;

  public static void init() {
    parties = new Vector<Party>();

    DataHandlers.register("party_members", m -> handlePartyMembers(m));
    DataHandlers.register("add_to_party", m -> handleAddToParty(m));
    DataHandlers.register("join_party", m -> handleJoinParty(m));
  }

  public static void handlePartyMembers(Message m) {
    Client client = m.client;

    if (client.playerCharacter.getParty() != null) {
      StringBuilder partyMembers = new StringBuilder(1000);
      Party p = client.playerCharacter.getParty();
      partyMembers.append(p.getNrMembers() - 1).append('/'); // Don't count self

      for (Client member : p.getPlayers()) {
        if (member.playerCharacter.getDBId() != client.playerCharacter.getDBId()) {
          partyMembers.append(member.playerCharacter.getName()).append(',');
        }
      }
      addOutGoingMessage(client, "party_members", partyMembers.toString());
    } else {
      addOutGoingMessage(client, "party_members", "0/");
    }
  }

  public static void handleAddToParty(Message m) {
    Client client = m.client;
    String playerName = m.message;

    int playerDbId = 0;

    // CHECK IF PLAYER EXISTS AND GET PLAYER ID
    ResultSet playerInfo =
        Server.userDB.askDB(
            "SELECT Id FROM user_character WHERE LOWER(Name) LIKE '"
                + playerName.toLowerCase()
                + "'");
    try {
      if (playerInfo.next()) {
        playerDbId = playerInfo.getInt("Id");
      }
      playerInfo.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    if (playerDbId > 0 && playerDbId != client.playerCharacter.getDBId()) {
      // IF PLAYER EXISTS
      Client player = null;

      // CHECK IF PLAYER IS ONLINE
      for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
        Client s = entry.getValue();

        if (s.Ready) {
          if (s.playerCharacter != null) {
            if (s.playerCharacter.getDBId() == playerDbId) {
              player = s;
              break;
            }
          }
        }
      }

      if (player != null) {
        // IF PLAYER ONLINE
        // Check if player has the right level

        // Get highest level of party
        int highest_level = 0;
        Party playerParty = client.playerCharacter.getParty();
        if (playerParty != null) {
          for (Client c : playerParty.getPlayers()) {
            if (highest_level < c.playerCharacter.getLevel()) {
              highest_level = c.playerCharacter.getLevel();
            }
          }
        } else {
          highest_level = client.playerCharacter.getLevel();
        }

        boolean levelOk = false;
        if (Math.abs(highest_level - player.playerCharacter.getLevel()) < 10) {
          levelOk = true;
        }
        if (levelOk) {
          // CHECK IF PLAYER IS IN A PARTY ALREADY
          boolean inParty = false;

          for (Party p : parties) {
            for (Client c : p.getPlayers()) {
              if (c.playerCharacter != null && player.playerCharacter != null) {
                if (c.playerCharacter.getDBId() == player.playerCharacter.getDBId()) {
                  inParty = true;
                  break;
                }
              }
            }
            if (inParty) {
              break;
            }
          }

          if (!inParty) {
            // ADD TO PARTY!

            // Send "add to party"-request

            Party newParty = client.playerCharacter.getParty();

            // CHECK IF CLIENT IS IN PARTY, OTHERWISE CREATE A PARTY
            if (newParty == null) {

              partyIndex++;

              // CREATE PARTY
              newParty = new Party(partyIndex);

              newParty.addPlayer(client);
              client.playerCharacter.setParty(newParty);
              addOutGoingMessage(client, "message", "#messages.party.created_party");
              addOutGoingMessage(client, "party_chat", "active");

              parties.add(newParty);
            }

            // ADD PLAYER TO PARTY
            if (newParty != null) {
              addOutGoingMessage(
                  player,
                  "request",
                  "join_party;"
                      + partyIndex
                      + ";#messages.party.join_party#"
                      + client.playerCharacter.getName()
                      + "#messages.party.party");
            }

          } else {
            addOutGoingMessage(client, "message", "#messages.party.already_in_party");
          }
        } else {
          addOutGoingMessage(client, "message", "#messages.party.level_req");
        }
      } else {
        addOutGoingMessage(client, "message", "#messages.party.player_not_online");
      }
    } else if (playerDbId == client.playerCharacter.getDBId()) {
      addOutGoingMessage(client, "message", "#messages.party.add_self");
    } else {
      addOutGoingMessage(client, "message", "#messages.party.player_not_exit");
    }
  }

  public static void handleJoinParty(Message m) {
    Client client = m.client;
    int partyIndex = Integer.parseInt(m.message);

    // Leave party if already in one
    leaveParty(client, partyIndex);

    // Add to party
    for (Party p : parties) {
      if (p.getId() == partyIndex) {

        p.addPlayer(client);
        client.playerCharacter.setParty(p);

        for (Client member : p.getPlayers()) {
          if (member.Ready) {
            addOutGoingMessage(
                member,
                "message",
                client.playerCharacter.getName() + " #messages.party.joined_party");
          }
        }
        addOutGoingMessage(client, "message", "#messages.party.now_member");
        addOutGoingMessage(client, "party_chat", "active");
        break;
      }
    }
  }

  public static void leaveParty(Client client) {
    leaveParty(client, -1);
  }

  public static void leaveParty(Client client, int partyIndexToIgnore) {
    // If player in a party, remove player from party
    if (client.playerCharacter != null) {
      Party playerParty = client.playerCharacter.getParty();
      if (playerParty != null) {
        if (playerParty.getId() != partyIndexToIgnore) {
          playerParty.removePlayer(client);

          // Send message to other members
          for (Client member : playerParty.getPlayers()) {
            if (member.Ready) {
              addOutGoingMessage(
                  member,
                  "message",
                  client.playerCharacter.getName() + " #messages.party.left_party");
            }
          }

          // If just one player or less left in party
          // disband and remove party
          if (playerParty.getNrMembers() <= 1) {

            for (Client member : playerParty.getPlayers()) {
              member.playerCharacter.setParty(null);
              addOutGoingMessage(member, "party_chat", "close");
              addOutGoingMessage(member, "message", "#messages.party.disbanded");
            }
            playerParty.clear();

            PartyHandler.parties.remove(playerParty);
          }

          client.playerCharacter.setParty(null);
        }
      }
    }
  }
}
