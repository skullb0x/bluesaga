package game;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import utils.json.JSONObject;

public class ClientSettings {

  // GENERAL
  public static int VERSION_NR = 724;
  public static boolean DEV_MODE = false;
  public static String SERVER_IP = "localhost";
  public static int PORT = 26342;

  public static String clientFileName = "gameData.jar";

  // VIDEO
  public static short SCREEN_WIDTH = 1024; // 1024
  public static short SCREEN_HEIGHT = 640; // 640

  public static short WINDOW_WIDTH = 1024;
  public static short WINDOW_HEIGHT = 640;

  public static short FULLSCREEN_WIDTH = 1024;
  public static short FULLSCREEN_HEIGHT = 768;

  public static boolean FULL_SCREEN = false;
  public static final short FRAME_RATE = 60;

  public static final short TILE_SIZE = 50;

  public static final short TILE_HALF_W = 18;
  public static final short TILE_HALF_H = 10;

  // AUDIO
  public static boolean MUSIC_ON = true;
  public static boolean SFX_ON = true;

  public static float musicVolume = 1.0f;
  public static float soundVolume = 1.0f;

  public static void toggleFullScreen() {
    if (FULL_SCREEN) {
      FULL_SCREEN = false;
      SCREEN_WIDTH = WINDOW_WIDTH;
      SCREEN_HEIGHT = WINDOW_HEIGHT;
    } else {
      FULL_SCREEN = true;
      SCREEN_WIDTH = FULLSCREEN_WIDTH;
      SCREEN_HEIGHT = FULLSCREEN_HEIGHT;
    }
  }

  public static JSONObject loadTranslationLanguageFile(String path) {

    JSONObject jsonObj = null;
    path = path.replace(clientFileName + "/", "");

    // Look for language file in path
    File f = new File(path);

    BufferedReader br = null;
    if (f.exists()) {
      // Loading translation file...
      FileReader in;
      try {
        in = new FileReader(path);
        br = new BufferedReader(in);
      } catch (FileNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      // Read language file from path
      // File exists, use translation
      try {
        StringBuilder sb = new StringBuilder();
        String line;
        line = br.readLine();

        while (line != null) {
          sb.append(line);
          line = br.readLine();
        }
        String everything = sb.toString();

        jsonObj = new JSONObject(everything);

        br.close();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    return jsonObj;
  }

  public static JSONObject loadOriginalLanguageFile() {

    JSONObject jsonObj = null;

    BufferedReader br = null;

    // Loading original text file...

    String path = "/game_text.txt";
    InputStream in = BlueSaga.class.getResourceAsStream(path);
    br = new BufferedReader(new InputStreamReader(in));

    // Read language file from path
    // File exists, use translation
    try {
      StringBuilder sb = new StringBuilder();
      String line;
      line = br.readLine();

      while (line != null) {
        sb.append(line);
        line = br.readLine();
      }
      String everything = sb.toString();

      jsonObj = new JSONObject(everything);

      br.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return jsonObj;
  }

  /**
   * Set the display mode to be used
   *
   * @param width The width of the display required
   * @param height The height of the display required
   * @param fullscreen True if we want fullscreen mode
   */
  public static void setDisplayMode(int width, int height, boolean fullscreen) {

    // return if requested DisplayMode is already set
    if ((Display.getDisplayMode().getWidth() == width)
        && (Display.getDisplayMode().getHeight() == height)
        && (Display.isFullscreen() == fullscreen)) {
      return;
    }

    try {
      DisplayMode targetDisplayMode = null;

      if (fullscreen) {
        DisplayMode[] modes = Display.getAvailableDisplayModes();
        int freq = 0;

        for (int i = 0; i < modes.length; i++) {
          DisplayMode current = modes[i];

          if ((current.getWidth() == width) && (current.getHeight() == height)) {
            if ((targetDisplayMode == null) || (current.getFrequency() >= freq)) {
              if ((targetDisplayMode == null)
                  || (current.getBitsPerPixel() > targetDisplayMode.getBitsPerPixel())) {
                targetDisplayMode = current;
                freq = targetDisplayMode.getFrequency();
              }
            }

            // if we've found a match for bpp and frequence against the
            // original display mode then it's probably best to go for this one
            // since it's most likely compatible with the monitor
            if ((current.getBitsPerPixel() == Display.getDesktopDisplayMode().getBitsPerPixel())
                && (current.getFrequency() == Display.getDesktopDisplayMode().getFrequency())) {
              targetDisplayMode = current;
              break;
            }
          }
        }
      } else {
        targetDisplayMode = new DisplayMode(width, height);
      }

      if (targetDisplayMode == null) {
        System.out.println(
            "Failed to find value mode: " + width + "x" + height + " fs=" + fullscreen);
        return;
      }

      Display.setDisplayMode(targetDisplayMode);
      Display.setFullscreen(fullscreen);

    } catch (LWJGLException e) {
      System.out.println(
          "Unable to setup mode " + width + "x" + height + " fullscreen=" + fullscreen + e);
    }
  }
}
