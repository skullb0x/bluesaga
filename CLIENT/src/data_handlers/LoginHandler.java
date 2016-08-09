package data_handlers;

import utils.LanguageUtils;
import game.BlueSaga;
import gui.Gui;
import screens.CharacterCreate;
import screens.CharacterSelection;
import screens.LoginScreen;
import screens.ScreenHandler;
import screens.ScreenHandler.ScreenType;

public class LoginHandler extends Handler {

  public LoginHandler() {
    super();
  }

  public static void handleData(String serverData) {
    if (serverData.startsWith("<login>wrong")) {
      //String attemptInfo[] = serverData.substring(7).split(",");
      //int attempts = Integer.parseInt(attemptInfo[1]);
      BlueSaga.stopClient();
      LoginScreen.setStatusMessage("Wrong username or password");
      BlueSaga.LoginSuccess = false;
      ScreenHandler.setActiveScreen(ScreenType.LOGIN);
      LoginScreen.restartCountdown();
    } else if (serverData.startsWith("<login>banned")) {
      String banInfo[] = serverData.split(",");
      int hours = Integer.parseInt(banInfo[1]);
      if (hours < 100) {
        LoginScreen.setStatusMessage("This account has been blocked for " + hours + " hours");
      } else {
        LoginScreen.setStatusMessage("This account has been blocked");
      }
      BlueSaga.stopClient();
      BlueSaga.LoginSuccess = false;
      ScreenHandler.setActiveScreen(ScreenType.LOGIN);
      LoginScreen.restartCountdown();
    } else if (serverData.startsWith("<login>blocked")) {
      LoginScreen.setStatusMessage("Account blocked!");
      LoginScreen.restartCountdown();

    } else if (serverData.startsWith("<login>needconfirm")) {
      LoginScreen.setStatusMessage("Account needs to be activated! Check your e-mail.");
      LoginScreen.restartCountdown();

    } else if (serverData.startsWith("<login>error,#")) {
      String errorMessage = serverData.substring(14);
      LoginScreen.setStatusMessage(LanguageUtils.getString(errorMessage));
      ScreenHandler.setActiveScreen(ScreenType.LOGIN);
      BlueSaga.LoginSuccess = false;
      LoginScreen.restartCountdown();
    } else if (serverData.startsWith("<login>")) {
      BlueSaga.LoginSuccess = true;
      LoginScreen.clickedLogin = false;

      ScreenHandler.setActiveScreen(ScreenType.LOADING);

      String login_info[] = serverData.substring(7).split(":");

      String account_info[] = login_info[0].split(";");

      BlueSaga.client.setUserId(Integer.parseInt(account_info[0]));
      BlueSaga.client.setUserMail(account_info[1]);

      LoginScreen.unfocusFields();

      CharacterSelection.load(login_info[1]);

      if (BlueSaga.reciever.lostConnection && BlueSaga.lastPlayedDbId > 0) {
        BlueSaga.client.sendMessage("playerinfo", "" + BlueSaga.lastPlayedDbId);
      }
    }

    if (serverData.startsWith("<backtologin>")) {
      BlueSaga.HAS_QUIT = true;
      BlueSaga.stopClient();
      ScreenHandler.setActiveScreen(ScreenType.LOGIN);
      BlueSaga.lastPlayedDbId = 0;
      BlueSaga.BG_MUSIC.changeSong("title", "title");
      Gui.Mouse.setType("Pointer");
      Gui.closeAllWindows();

      String message = serverData.substring(13);
      LoginScreen.focusLoginField();
      LoginScreen.setStatusMessage(message);
    }

    if (serverData.startsWith("<passwordchanged>")) {
      String status = serverData.substring(17);

      if (status.equals("ok")) {
        CharacterSelection.goBack();
      } else {
        CharacterSelection.setStatus("Your old password is wrong!");
      }
    }

    if (serverData.startsWith("<newchar>")) {
      String cInfo = serverData.substring(9);
      CharacterCreate.load(cInfo);
    }

    if (serverData.startsWith("<createchar>")) {
      String CreateStatus = serverData.substring(12);
      if (CreateStatus.equals("exists")) {
        CharacterCreate.setStatus("name already taken!");
        CharacterCreate.setCheckingServer(false);
      } else if (CreateStatus.equals("error")) {
        CharacterCreate.setStatus("An error occured!");
        CharacterCreate.setCheckingServer(false);
      } else if (CreateStatus.equals("incorrect")) {
        CharacterCreate.setStatus("Incorrect name!");
        CharacterCreate.setCheckingServer(false);
      } else {
        CharacterCreate.setStatus("creating character...");
        CharacterCreate.disableInputFields();
        int charId = Integer.parseInt(CreateStatus);
        BlueSaga.client.sendMessage("playerinfo", "" + charId);
      }
    }

    if (serverData.startsWith("<deletechar>")) {
      String message[] = serverData.substring(12).split(",");
      int characterId = Integer.parseInt(message[0]);
      int delState = Integer.parseInt(message[1]);

      CharacterSelection.changeCharStatus(characterId, delState);
    }
  }
}
