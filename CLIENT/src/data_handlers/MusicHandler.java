package data_handlers;

public class MusicHandler {

  public float change = 0.113f;

  public MusicHandler() {
    super();
    // TODO Auto-generated constructor stub
  }

  public static void handleData(String serverData) {
    if (serverData.startsWith("<playnote>")) {
      String noteInfo[] = serverData.substring(10).split(";");

      //String playerData = noteInfo[0];

      //Creature c = BlueSaga.MapHandler.addCreatureToScreen(playerData);

      String playInfo[] = noteInfo[1].split(",");

      Integer.parseInt(playInfo[0]);
      Integer.parseInt(playInfo[1]);

      //BlueSaga.MIDI_PLAYER.playNote(instrumentId,note,20);

    }
  }
}
