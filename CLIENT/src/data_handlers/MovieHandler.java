package data_handlers;

import creature.Creature;
import game.BlueSaga;
import movies.CardMovie;
import movies.CurseMovie;
import movies.IntroMovie;
import screens.ScreenHandler;

public class MovieHandler extends Handler {
  public MovieHandler() {
    super();
  }

  public static void handleData(String serverData) {
    if (serverData.startsWith("<cutscene>")) {
      String movieInfo[] = serverData.substring(10).split(";");

      int movieId = Integer.parseInt(movieInfo[0]);

      ScreenHandler.cutScene = null;

      if (movieId == 1) {
        ScreenHandler.cutScene = new IntroMovie(BlueSaga.playerCharacter.getCreatureId());
        ScreenHandler.cutScene.play();
      } else if (movieId == 2) {
        ScreenHandler.cutScene = new CurseMovie(BlueSaga.playerCharacter);
        ScreenHandler.cutScene.play();
      } else if (movieId == 3) {
        String winnerInfo = movieInfo[1];
        Creature winnerPlayer = MapHandler.addCreatureToScreen(winnerInfo);
        ScreenHandler.cutScene = new CardMovie(winnerPlayer);
        ScreenHandler.cutScene.play();
      }
    }
  }
}
