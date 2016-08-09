package game;

import network.Server;
import utils.CrashLogger;

public class BPserver {

  /******************************
   * 							*
   *    GLOBAL SERVER METHODS	*
   * 							*
   ******************************/
  public static void main(String args[]) throws Exception {

    for (int i = 0; i < args.length; i++) {
      if ("-dev".equals(args[i])) {
        ServerSettings.DEV_MODE = true;
      } else {
        System.err.println("Unknown parameter: " + args[i]);
        System.exit(1);
      }
    }

    Thread.setDefaultUncaughtExceptionHandler(
        new Thread.UncaughtExceptionHandler() {
          @Override
          public void uncaughtException(Thread t, Throwable e) {
            // Log crashes
            CrashLogger.uncaughtException(e);

            // Restart server if uncaught exception occur
            Server.restartServer();
          }
        });

    Server s =
        new Server() {
          @Override
          protected void update(int delta) {}

          @Override
          protected void init() {}
        };

    s.start();
  }
}
