package data_handlers;

import creature.PlayerCharacter;
import game.ServerSettings;
import network.Client;

public class Handler {

  public static void init() {}

  public static void startTimers() {}

  public static void addOutGoingMessage(Client client, String type, String msg) {

    Message newMessage = new Message(client, type, msg);
    DataHandlers.addOutgoingMessage(newMessage);

    /*
    if(!Server.SERVER_RESTARTING){
    	if(!client.sendingData){
    		client.sendingData = true;
    		sendData(client,"<"+type+">"+msg);

    		if(client.MessageQueue.size() > 0){
    			String nextData = client.MessageQueue.get(0);
    			client.MessageQueue.remove(0);
    			sendData(client,nextData);
    		}
    		client.sendingData = false;

    	}else {
    		client.MessageQueue.add("<"+type+">"+msg);
    	}
    }
    */
  }

  public static boolean isVisibleForPlayer(PlayerCharacter player, int x, int y, int z) {
    boolean visible = false;

    if (player != null) {
      if (player.getZ() == z
          && Math.abs(player.getX() - x) < ServerSettings.TILE_HALF_W + 2
          && Math.abs(player.getY() - y) < ServerSettings.TILE_HALF_H + 3) {
        visible = true;
      }
    }
    return visible;
  }
}
