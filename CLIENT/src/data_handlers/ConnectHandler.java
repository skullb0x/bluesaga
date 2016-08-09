package data_handlers;

import java.util.Iterator;

import particlesystem.EmitterManager;
import projectile.ProjectileManager;
import screens.CharacterCreate;
import screens.LoginScreen;
import screens.ScreenHandler;
import screens.ScreenHandler.ScreenType;
import utils.LanguageUtils;
import game.BlueSaga;
import game.ClientSettings;
import graphics.BlueSagaColors;
import gui.Gui;
import creature.Npc;
import creature.PlayerCharacter;

public class ConnectHandler extends Handler {

  String OldMapName = "None";

  public ConnectHandler() {
    super();
  }

  public static void handleData(String serverData) {
    if (serverData.startsWith("<connect>")) {
      String connectInfo = serverData.substring(9);
      int checkversion = Integer.parseInt(connectInfo);

      if (checkversion != ClientSettings.VERSION_NR) {
        ScreenHandler.LoadingStatus =
            "         "
                + LanguageUtils.getString("ui.status.new_client_available")
                + "\n"
                + LanguageUtils.getString("ui.status.visit_site");
        ScreenHandler.setActiveScreen(ScreenType.ERROR);
      } else {
        ScreenHandler.LoadingStatus = "";
        LoginScreen.login();
      }
    } else if (serverData.startsWith("<playerinfo>")) {
      String playerInfo = serverData.substring(12);

      ScreenHandler.setActiveScreen(ScreenType.LOADING);
      ScreenHandler.LoadingStatus = "Requesting data from server...";

      if (playerInfo.equals("fail")) {
        ScreenHandler.setActiveScreen(ScreenType.ERROR);
        ScreenHandler.LoadingStatus = "Can't get player data!";
      } else {

        // Removes old chat messages, channels
        Gui.Chat_Window.reset();

        BlueSaga.reciever.lostConnection = false;
        CharacterCreate.setCheckingServer(false);

        // LOAD PLAYER DATA
        String[] load_data;
        load_data = playerInfo.split("/");

        String[] player_info = load_data[0].split(":");

        String[] player_position;
        player_position = player_info[0].split(",");

        int newX = Integer.parseInt(player_position[0]);
        int newY = Integer.parseInt(player_position[1]);
        int newZ = Integer.parseInt(player_position[2]);

        String[] creature_info = player_info[1].split(",");

        int CreatureId = Integer.parseInt(creature_info[0]);

        BlueSaga.playerCharacter = new PlayerCharacter(CreatureId, newX, newY, newZ);
        BlueSaga.playerCharacter.load(playerInfo);

        BlueSaga.lastPlayedDbId = BlueSaga.playerCharacter.getDBId();

        Gui.TutorialDialog.close();

        ScreenHandler.LoadingStatus = "Loading skills info...";
        BlueSaga.client.sendMessage("skilldata", "info");
      }

    } else if (serverData.startsWith("<particledata>")) {

      String particleEmitterData[] = serverData.substring(14).split("%");

      String emitterInfo[] = particleEmitterData[0].split("/");
      String particleInfo[] = particleEmitterData[1].split("/");
      String projectileInfo[] = particleEmitterData[2].split("/");

      if (!ClientSettings.DEV_MODE) {
        BlueSaga.gameDB.updateDB("DELETE FROM Emitter");
        //BlueSaga.gameDB.updateDB("CREATE TABLE \"Emitter\" (\"Id\" INTEGER PRIMARY KEY  NOT NULL ,\"Name\" TEXT,\"Lifetime\" FLOAT,\"EmittionRate\" FLOAT,\"RotationSpeed\" FLOAT,\"MinPos\" VARCHAR,\"MaxPos\" VARCHAR, \"ShowParticle\" BOOL DEFAULT TRUE, \"ParticleId\" INTEGER DEFAULT 0, \"ShowStreak\" BOOL DEFAULT FALSE, \"StreakId\" INTEGER DEFAULT 0)");

        for (String emitter : emitterInfo) {
          String emitterData[] = emitter.split(";");
          BlueSaga.gameDB.updateDB(
              "INSERT INTO \"Emitter\" VALUES("
                  + emitterData[0]
                  + ",'"
                  + emitterData[1]
                  + "',"
                  + emitterData[2]
                  + ","
                  + emitterData[3]
                  + ","
                  + emitterData[4]
                  + ",'"
                  + emitterData[5]
                  + "','"
                  + emitterData[6]
                  + "',"
                  + emitterData[7]
                  + ","
                  + emitterData[8]
                  + ","
                  + emitterData[9]
                  + ","
                  + emitterData[10]
                  + ")");
        }

        BlueSaga.gameDB.updateDB("DELETE FROM Particle");
        //BlueSaga.gameDB.updateDB("CREATE TABLE \"Particle\" (\"Id\" INTEGER PRIMARY KEY  NOT NULL ,\"Name\" TEXT,\"MinDir\" VARCHAR,\"MaxDir\" VARCHAR,\"MinAxisRotSpeed\" INTEGER,\"MaxAxisRotSpeed\" INTEGER,\"MinScale\" FLOAT,\"MaxScale\" FLOAT,\"Lifetime\" FLOAT,\"ImageString\" TEXT,\"VerticalGravity\" FLOAT DEFAULT (0.0) ,\"HorizontalGravity\" FLOAT DEFAULT (0.0) ,\"StartColor\" VARCHAR,\"EndColor\" VARCHAR,\"RotationSpeed\" FLOAT DEFAULT (0.0) ,\"FadeSpeed\" FLOAT DEFAULT (0.5) )\")");

        for (String particle : particleInfo) {
          String particleData[] = particle.split(";");
          String sql =
              "INSERT INTO \"Particle\" VALUES("
                  + particleData[0]
                  + ",'"
                  + particleData[1]
                  + "','"
                  + particleData[2]
                  + "','"
                  + particleData[3]
                  + "',"
                  + particleData[4]
                  + ","
                  + particleData[5]
                  + ","
                  + particleData[6]
                  + ","
                  + particleData[7]
                  + ","
                  + particleData[8]
                  + ",'"
                  + particleData[9]
                  + "',"
                  + particleData[10]
                  + ","
                  + particleData[11]
                  + ",'"
                  + particleData[12]
                  + "','"
                  + particleData[13]
                  + "',"
                  + particleData[14]
                  + ","
                  + particleData[15]
                  + ")";
          BlueSaga.gameDB.updateDB(sql);
        }

        BlueSaga.gameDB.updateDB(
            "CREATE TABLE IF NOT EXISTS projectile (Id INTEGER PRIMARY KEY NOT NULL, GfxName CHAR(20), EmitterId INTEGER DEFAULT 0, HitColor CHAR(20), Sfx CHAR(20))");

        BlueSaga.gameDB.updateDB("DELETE FROM projectile");

        for (String projectile : projectileInfo) {
          String projectileData[] = projectile.split(";");
          String sql =
              "INSERT INTO \"projectile\" VALUES("
                  + projectileData[0]
                  + ",'"
                  + projectileData[1]
                  + "',"
                  + projectileData[2]
                  + ",'"
                  + projectileData[3]
                  + "','"
                  + projectileData[4]
                  + "')";
          BlueSaga.gameDB.updateDB(sql);
        }
      }

      ScreenHandler.myEmitterManager = new EmitterManager(BlueSaga.gameDB);
      ScreenHandler.ProjectileManager = new ProjectileManager();

      ScreenHandler.LoadingStatus = "Loading ability info...";
      BlueSaga.client.sendMessage("abilitydata", "1/info");

    } else if (serverData.startsWith("<actionbar>")) {
      String aInfo[] = serverData.substring(11).split("/");

      int load_more = Integer.parseInt(aInfo[0]);

      if (!aInfo.equals("None")) {
        Gui.loadActionbar(aInfo[1]);
      } else {
        Gui.getActionBar().clear();
      }

      Gui.getActionBar().update();

      if (load_more == 1) {
        ScreenHandler.LoadingStatus = "Loading monster data...";
        BlueSaga.client.sendMessage("mobinfo", "info");
      }
    }

    if (serverData.startsWith("<quitchar>")) {
      BlueSaga.BG_MUSIC.changeSong("title", "title");
      BlueSaga.lastPlayedDbId = 0;
      Gui.Mouse.setType("Pointer");
      Gui.closeAllWindows();
    }

    if (serverData.startsWith("<message>")) {
      String message = serverData.substring(9);

      if (message.contains("#")) {
        String messages[] = message.split("#");

        message = "";

        for (String part : messages) {
          if (part.contains(".")) {
            message += LanguageUtils.getString(part);
          } else {
            message += part;
          }
        }
      }

      Gui.addMessage(message, BlueSagaColors.RED);
    }
  }

  public static void loadDone() {
    Gui.closeAllWindows();
    ScreenHandler.setActiveScreen(ScreenType.WORLD);

    boolean boss = false;

    for (Iterator<Npc> iter = BlueSaga.WORLD_MAP.getOtherPlayers().values().iterator();
        iter.hasNext();
        ) {
      Npc otherPlayer = iter.next();
      if (otherPlayer.getName().contains("!")) {
        boss = true;
        break;
      }
    }
    if (boss) {
      BlueSaga.BG_MUSIC.changeSong("event", "None");
    }
  }
}
