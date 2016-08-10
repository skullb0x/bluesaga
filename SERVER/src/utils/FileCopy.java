package utils;

import game.ServerSettings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileCopy {

  public static void copyFile(File src, File dest) throws IOException {

    //if file, then copy it
    //Use bytes stream to support all file types
    InputStream in = new FileInputStream(src);
    OutputStream out = new FileOutputStream(dest);

    byte[] buffer = new byte[1024];

    int length;
    //copy the file content in bytes
    while ((length = in.read(buffer)) > 0) {
      out.write(buffer, 0, length);
    }

    in.close();
    out.close();
  }

  public static void backupDB() {
    if (!ServerSettings.DEV_MODE) {
      String filePath = new File("").getAbsolutePath();

      File originalDB = new File(filePath + "/usersDB.db");
      String fileName = "/db_backups/usersDB_" + TimeUtils.getDate("yyyyMMdd_HHmmss") + ".db";
      File copyDB = new File(filePath + fileName);

      ServerMessage.printMessage("Make backup of usersDB.db...", false);

      try {
        FileCopy.copyFile(originalDB, copyDB);
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
        ServerMessage.printMessage("Failed to backup db!", false);
      }

      ServerMessage.printMessage("Done saving backup: " + fileName, false);
    }
  }
}
