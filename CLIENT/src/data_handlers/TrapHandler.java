package data_handlers;

import sound.Sfx;
import map.TileObject;
import screens.ScreenHandler;

public class TrapHandler extends Handler {

  public TrapHandler() {
    super();
  }

  public static void handleData(String serverData) {
    if (serverData.startsWith("<trap_trigger>")) {
      String trapInfo[] = serverData.substring(14).split(",");

      int trapX = Integer.parseInt(trapInfo[0]);
      int trapY = Integer.parseInt(trapInfo[1]);
      int trapZ = Integer.parseInt(trapInfo[2]);

      String trapName = trapInfo[3];

      if (ScreenHandler.SCREEN_OBJECTS_WITH_ID.get(trapX + "," + trapY + "," + trapZ) != null) {
        TileObject so =
            ScreenHandler.SCREEN_OBJECTS_WITH_ID.get(trapX + "," + trapY + "," + trapZ).getObject();
        if (so != null) {
          so.changeGraphics(trapName);
          Sfx.playRandomPitch("traps/" + trapName);
        }
      }
    }
  }
}
