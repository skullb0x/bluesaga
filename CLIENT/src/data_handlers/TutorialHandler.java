package data_handlers;

import gui.Gui;

public class TutorialHandler extends Handler {

  public TutorialHandler() {
    super();
  }

  public static void handleData(String serverData) {
    if (serverData.startsWith("<tutorial>")) {
      int tutorialId = Integer.parseInt(serverData.substring(10));
      Gui.TutorialDialog.viewStep(tutorialId);
    } else if (serverData.startsWith("<tutorial_end>")) {
      Gui.TutorialDialog.close();
    }
  }
}
