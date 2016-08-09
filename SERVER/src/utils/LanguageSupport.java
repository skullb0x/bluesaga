package utils;

import game.ServerSettings;
import network.Server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import utils.json.JSONObject;

public class LanguageSupport {
  public static void loadLanguageFile(String language) {

    File jarFile =
        new File(Server.class.getProtectionDomain().getCodeSource().getLocation().getPath());

    String server_path = jarFile.getAbsolutePath();

    server_path = server_path.replace("BPserver.jar", "");

    if (ServerSettings.DEV_MODE) {
      server_path += "/../game_text.json";
    } else {
      server_path += "game_text.json";
    }

    System.out.println("Read language file: " + server_path);

    try {
      BufferedReader br = new BufferedReader(new FileReader(server_path));
      StringBuilder sb = new StringBuilder();
      String line;
      line = br.readLine();

      while (line != null) {
        sb.append(line);
        line = br.readLine();
      }
      String everything = sb.toString();

      JSONObject jsonObj = new JSONObject(everything);

      /*
           if(Settings.DEV_MODE){

            System.out.println("Generate language file for quests...");

            ResultSet quest_info = Server.mapDB.askDB("select Id, Name, RewardMessage, QuestMessage, Description from quest order by Id asc");

            JSONObject questsJSON = new JSONObject();
            try {
      	while(quest_info.next()){
      		JSONObject questJSON = new JSONObject();
      		questJSON.accumulate("name",quest_info.getString("Name"));
      		questJSON.accumulate("quest_message",quest_info.getString("QuestMessage"));
      		questJSON.accumulate("reward_message",quest_info.getString("RewardMessage"));
      		questJSON.accumulate("description",quest_info.getString("Description"));

      		questsJSON.accumulate(""+quest_info.getInt("Id"), questJSON);
      	}
      	quest_info.close();
      } catch (JSONException e) {
      	// TODO Auto-generated catch block
      	e.printStackTrace();
      	System.out.println("Error creating translation file");
      } catch (SQLException e) {
      	// TODO Auto-generated catch block
      	e.printStackTrace();
      }

            jsonObj.accumulate("quests", questsJSON);

            br.close();

            String client_path = jarFile.getAbsolutePath() + "/../../CLIENT/src/game_text.txt";

            FileWriter file = new FileWriter(client_path);
            try {
                file.write(jsonObj.toString(4));
                System.out.println("Successfully Copied JSON Object to CLIENT: "+client_path);
            } catch (IOException e) {
                e.printStackTrace();

            } finally {
                file.flush();
                file.close();
            }
           }
        */
      LanguageUtils.loadJson(jsonObj, false);

    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
