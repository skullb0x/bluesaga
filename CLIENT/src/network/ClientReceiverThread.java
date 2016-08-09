package network;

import game.BlueSaga;
import gui.Gui;
import screens.ScreenHandler;
import screens.ScreenHandler.ScreenType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.SocketTimeoutException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

/**
 * Fetches messages from Server.
 *
 * @author vinsent
 *
 */
public class ClientReceiverThread extends Thread {

  private ObjectInputStream in;

  private Vector<String> info = new Vector<String>();

  private int info_itr = 0;

  public boolean lostConnection = false;

  private Timer reconnectTimer;
  private int reconnectCountdownLimit = 1;
  private int reconnectCountdownSec = 0;

  /**
   * Stores reference to socket for communication and Logic to deliver data
   * to.
   *
   * @param socket
   * @param logic
   */
  public ClientReceiverThread(ObjectInputStream input) {
    lostConnection = false;
    reconnectCountdownLimit = 1;
    in = input;
    reconnectTimer = new Timer();
  }

  @Override
  public void run() {

    waitForData();
  }

  public void waitForData() {
    String getMsg = "";

    int lostConnectionNr = 0;

    while (!BlueSaga.HAS_QUIT) {
      try {
        try {
          getMsg = new String((byte[]) in.readObject());
        } catch (SocketTimeoutException e) {
          ScreenHandler.setActiveScreen(ScreenType.ERROR);
          ScreenHandler.LoadingStatus = "Your connection is slow, lost connection with server";
        }
        info.add(getMsg);

      } catch (IOException e) {
        lostConnectionNr++;

        if (lostConnectionNr > 20 && ScreenHandler.getActiveScreen() != ScreenType.LOGIN) {
          if (ScreenHandler.getActiveScreen() == ScreenType.WORLD) {
            Gui.MapWindow.saveMiniMap(0);
          }
          ScreenHandler.setActiveScreen(ScreenType.ERROR);
          ScreenHandler.LoadingStatus = "Reconnect in " + reconnectCountdownLimit + " seconds";
          lostConnection = true;

          startReconnectCountdown();
          break;
        }
        //e.printStackTrace();
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }
    }
  }

  public void startReconnectCountdown() {
    reconnectCountdownSec = reconnectCountdownLimit;
    reconnectCountdownLimit++;
    if (!BlueSaga.HAS_QUIT) {
      reconnectCountdown();
    }
  }

  private void reconnectCountdown() {
    reconnectTimer.schedule(
        new TimerTask() {
          @Override
          public void run() {
            if (!BlueSaga.HAS_QUIT) {
              if (reconnectCountdownSec <= 0) {
                lostConnection = false;
                ScreenHandler.setActiveScreen(ScreenType.LOADING);

                BlueSaga.reconnect();

              } else {
                reconnectCountdownSec--;
                ScreenHandler.LoadingStatus =
                    "Lost connection with server! Reconnect in "
                        + reconnectCountdownSec
                        + " seconds";
                if (BlueSaga.LoginSuccess) {
                  reconnectCountdown();
                }
              }
            }
          }
        },
        1000);
  }

  public String getInfo() {
    String data = "";
    if (info_itr < info.size()) {
      data = info.get(info_itr);
      info_itr++;
    } else {
      info_itr = 0;
      info.clear();
    }

    return data;
  }

  public void pause() {}

  public void unpause() {}

  public boolean lostConnection() {
    return lostConnection;
  }
}
