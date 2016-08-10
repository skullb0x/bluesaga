package data_handlers.party_handler;

import java.util.Iterator;
import java.util.Vector;

import network.Client;

public class Party {
  private int id;
  private Vector<Client> players;

  public Party(int id) {
    this.setId(id);
    players = new Vector<Client>();
  }

  public void addPlayer(Client player) {
    players.add(player);
  }

  public void removePlayer(Client player) {
    Iterator<Client> partyIterator = players.iterator();

    while (partyIterator.hasNext()) {
      Client p = partyIterator.next();
      if (p.playerCharacter != null) {
        if (p.playerCharacter.getDBId() == player.playerCharacter.getDBId()) {
          partyIterator.remove();
          break;
        }
      }
    }
  }

  public void clear() {
    players.clear();
  }

  public Vector<Client> getPlayers() {
    return players;
  }

  public int getNrMembers() {
    int nrMembers = 0;
    if (players != null) {
      nrMembers = players.size();
    }
    return nrMembers;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }
}
