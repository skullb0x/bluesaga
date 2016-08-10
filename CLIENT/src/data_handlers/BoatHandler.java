package data_handlers;

import components.Ship;
import creature.PlayerCharacter;
import game.BlueSaga;
import graphics.BlueSagaColors;
import gui.Gui;

public class BoatHandler extends Handler {

  public BoatHandler() {
    super();
    // TODO Auto-generated constructor stub

  }

  public static void handleData(String serverData) {
    if (serverData.startsWith("<getboat>")) {
      String boatInfo[] = serverData.substring(9).split(",");
      String boatName = boatInfo[0];
      int boatId = Integer.parseInt(boatInfo[1]);
      BlueSaga.playerCharacter.setShip(new Ship(boatId));
      Gui.addMessage("#messages.quest.aquired_boat# '" + boatName + "'", BlueSagaColors.RED);

    } else if (serverData.startsWith("<goboat>")) {
      String userBoatInfo[] = serverData.substring(8).split(";");
      PlayerCharacter user = (PlayerCharacter) MapHandler.addCreatureToScreen(userBoatInfo[0]);

      String boatInfo[] = userBoatInfo[1].split(",");
      try {
        int shipId = Integer.parseInt(boatInfo[0]);
        int show = Integer.parseInt(boatInfo[1]);

        if (shipId > 0 && show == 1) {
          Ship userShip = new Ship(shipId);
          userShip.setShow(true);
          user.setShip(userShip);
        } else {
          if (user.getShip() != null) {
            user.getShip().setShow(false);
          }
        }
      } catch (NumberFormatException e) {

      }
    }
  }
}
