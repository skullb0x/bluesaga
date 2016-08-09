package data_handlers;

import network.Client;
import network.Server;

public class TutorialHandler extends Handler {

  public static void updateTutorials(int tutorialNr, Client client) {
    if (client.playerCharacter.getTutorialNr() == tutorialNr) {
      boolean saveTutorialProgress = true;
      if (tutorialNr == 0) {
        // CLOSE WALK TUTORIAL
        addOutGoingMessage(client, "tutorial_end", "");

      } else if (tutorialNr == 1) {
        // LEARN TO TALK
        addOutGoingMessage(client, "tutorial", "1");

      } else if (tutorialNr == 2) {
        // CLOSE TALK TUTORIAL
        addOutGoingMessage(client, "tutorial_end", "");

      } else if (tutorialNr == 3) {
        // SHOW TARGET TUTORIAL
        addOutGoingMessage(client, "tutorial", "2");

      } else if (tutorialNr == 4) {
        // CLOSE TARGET TUTORIAL
        addOutGoingMessage(client, "tutorial_end", "");

      } else if (tutorialNr == 5) {
        // SHOW RETURN TO INN-KEEPER TUTORIAL
        addOutGoingMessage(client, "tutorial", "3");

      } else if (tutorialNr == 6) {
        // CLOSE RETURN TO INN-KEEPER TUTORIAL
        addOutGoingMessage(client, "tutorial_end", "");

      } else if (tutorialNr == 7) {
        // SHOW DEATH EXPLANATION
        addOutGoingMessage(client, "tutorial", "4");

      } else if (tutorialNr == 8) {
        // CLOSE DEATH EXPLANATION
        addOutGoingMessage(client, "tutorial_end", "");

      } else {
        saveTutorialProgress = false;
      }

      if (saveTutorialProgress) {
        client.playerCharacter.setTutorialNr(tutorialNr + 1);
        Server.userDB.updateDB(
            "update user_character set TutorialNr = "
                + (tutorialNr + 1)
                + " where Id = "
                + client.playerCharacter.getDBId());
      }
    }
  }
}
