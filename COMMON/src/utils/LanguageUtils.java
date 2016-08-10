package utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Stack;

import utils.json.JSONException;
import utils.json.JSONObject;

public class LanguageUtils {

  public static LinkedHashMap<String, String> languageResource =
      new LinkedHashMap<String, String>();
  public static LinkedHashMap<String, String> originalResource =
      new LinkedHashMap<String, String>();

  private static Stack<String> language_key = new Stack<String>();

  // Recursive function that goes through a json object and stores
  // its key and values in the hashmap
  public static void loadJson(JSONObject json, boolean original) {
    Iterator<?> json_keys = json.keys();

    while (json_keys.hasNext()) {
      String json_key = (String) json_keys.next();

      try {
        language_key.push(json_key);
        loadJson(json.getJSONObject(json_key), original);
      } catch (JSONException e) {
        String key = "";
        for (String sub_key : language_key) {
          key += sub_key + ".";
        }
        key = key.substring(0, key.length() - 1);

        language_key.pop();
        if (original) {
          originalResource.put(key, json.getString(json_key));
        } else {
          languageResource.put(key, json.getString(json_key));
        }
      }
    }
    if (language_key.size() > 0) {
      language_key.pop();
    }
  }

  public static HashMap<String, String> getLang() {
    return languageResource;
  }

  public static String getString(String key) {
    if (languageResource.get(key) == null
        || languageResource.get(key).equals("")
        || languageResource.get(key) == null) {
      if (originalResource.get(key) != null) {
        return originalResource.get(key);
      } else {
        System.out.println("text missing: " + key);
        return "";
      }
    }
    return languageResource.get(key);
  }
}
