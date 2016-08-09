package utils;

import java.io.File;
import java.util.ArrayList;

public class FileHandler {
  /**
   * This will retrieve a list of all files from current
   * folder and all sub folders
   *
   * @param rootDir - path of the starting directory
   * @return A list of Files
   */
  public static ArrayList<File> retriveAllFiles(String rootDir) {
    ArrayList<File> tempList = new ArrayList<File>();
    File theRootDir = new File(rootDir);

    //Retrieve all folders (current and any sub)
    ArrayList<File> folders = getAllDir(theRootDir);

    //For all of the folders
    for (int i = 0; i < folders.size(); i++) {

      //Get all the files in the folder
      File[] currentFile = folders.get(i).listFiles();
      for (int k = 0; k < currentFile.length; k++) {

        //If the current file isn't a directory
        //Add it to the list of all files
        if (currentFile[k].isDirectory() == false) {
          tempList.add(currentFile[k]);
        }
      }
    }
    return tempList;
  }

  /**
   * This will retrieve a list of files from only the current
   * directory
   *
   * @param rootDir - path of the starting directory
   * @return A list of Files
   */
  public static ArrayList<File> retriveFiles(String rootDir) {
    File f = new File(rootDir);
    ArrayList<File> temp = new ArrayList<File>();
    File[] currentFile = f.listFiles();

    for (int k = 0; k < currentFile.length; k++) {
      //If the current file isn't a directory
      //Add it to the list of all files
      if (currentFile[k].isDirectory() == false) {
        temp.add(currentFile[k]);
      }
    }
    return temp;
  }

  /**
   * Will iterate through all directories gathering all folders & sub folders
   *
   * @param rootURL - starting File
   * @return A list of ALL folders inside of the root URL
   */
  private static ArrayList<File> getAllDir(File rootURL) {

    ArrayList<File>
        temp = new ArrayList<File>(), //This will hold our queued folders
        fill = new ArrayList<File>(), //List of end results
        subs = new ArrayList<File>(); //Sub folders

    //Add our initial to start search (Breadth First Search)
    temp.add(rootURL);
    while (!temp.isEmpty()) {

      //Dequeue Folder
      File next = temp.remove(0);

      //Add it to the return list if not done so already and not blank
      if (!fill.contains(next) && !next.getAbsolutePath().equals("")) {
        fill.add(next);
      }

      //Get sub folders
      subs = getSubs(next);

      //for each folder, add it to temp if not done so already
      for (File s : subs) {
        if (!temp.contains(s)) {
          temp.add(s);
        }
      }
      //clear for next iteration
      subs.clear();
    }
    return fill;
  }

  /**
   * This method will retrieve all the sub folders from the current directory
   * that was passed in
   * @param cur - Current directory that the user is in
   * @return A list of folders
   */
  private static ArrayList<File> getSubs(File cur) {

    //Get a list of all the files in folder
    ArrayList<File> temp = new ArrayList<File>();
    File[] fileList = cur.listFiles();

    //for each file in the folder
    for (int i = 0; i < fileList.length; i++) {

      //If the file is a Directory(folder) add it to return, if not done so already
      File choose = fileList[i];
      if (choose.isDirectory() && !temp.contains(choose)) {
        temp.add(choose);
      }
    }
    return temp;
  }
}
