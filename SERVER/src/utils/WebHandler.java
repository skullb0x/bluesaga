package utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class WebHandler {

  public WebHandler() {}

  public static void sendPost(String targetURL, String urlParameters) throws IOException {
    HttpURLConnection connection = null;

    URL url;
    try {
      try {
        url = new URL(targetURL);
        connection = (HttpURLConnection) url.openConnection();
      } catch (MalformedURLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      // Use post and add the type of post data as URLENCODED
      connection.setRequestMethod("POST");
    } catch (ProtocolException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

    // Optinally add the language and the data content
    connection.setRequestProperty(
        "Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
    connection.setRequestProperty("Content-Language", "en-US");

    // Set the mode as output and disable cache.
    connection.setUseCaches(false);
    connection.setDoInput(true);
    connection.setDoOutput(true);

    //Send request
    DataOutputStream wr;
    try {
      wr = new DataOutputStream(connection.getOutputStream());
      wr.writeBytes(urlParameters);
      wr.flush();
      wr.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    // Get Response
    // Optionally you can get the response of php call.
    InputStream is;
    try {
      is = connection.getInputStream();
      BufferedReader rd = new BufferedReader(new InputStreamReader(is));
      String line;
      StringBuffer response = new StringBuffer();
      while ((line = rd.readLine()) != null) {
        response.append(line);
        response.append('\r');
      }
      rd.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public static void callUrl(String urlText) {
    String USER_AGENT = "Mozilla/5.0";

    URL obj;
    try {
      obj = new URL(urlText);
      HttpURLConnection con = (HttpURLConnection) obj.openConnection();

      //add reuqest header
      con.setRequestMethod("POST");
      con.setRequestProperty("User-Agent", USER_AGENT);
      con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

      String urlParameters = "";

      // Send post request
      con.setDoOutput(true);
      DataOutputStream wr = new DataOutputStream(con.getOutputStream());
      wr.writeBytes(urlParameters);
      wr.flush();
      wr.close();

      //int responseCode = con.getResponseCode();

      BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
      String inputLine;
      StringBuffer response = new StringBuffer();

      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine);
      }
      in.close();

    } catch (MalformedURLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (ProtocolException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    //print result
  }
}
