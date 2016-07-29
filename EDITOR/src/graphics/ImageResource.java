package graphics;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import game.BP_EDITOR;
import game.EditorSettings;

public class ImageResource {

	private HashMap<String,Sprite> gfx;
	
	public ImageResource(){
		gfx = new HashMap<String,Sprite>();
	}
	
	/**
     * This will retrieve a list of all files from current
     * folder and all sub folders
     *
     * @param rootDir - path of the starting directory
     * @return A list of Files
     */
     public static ArrayList<File> retriveAllFiles( String rootDir){
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
                 if( currentFile[k].isDirectory() == false){
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
     public static ArrayList<File> retriveFiles (String rootDir){
         File f = new File( rootDir );
         ArrayList<File> temp = new ArrayList<File>();
         File[] currentFile = f.listFiles();
         
	     for (int k = 0; k < currentFile.length; k++) {
	             //If the current file isn't a directory
	             //Add it to the list of all files
	             if( currentFile[k].isDirectory() == false){
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
	
	     ArrayList<File> temp = new ArrayList<File>(), //This will hold our queued folders
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
	         if ( choose.isDirectory() && !temp.contains(choose)) {
	             temp.add(choose);
	         }
	     }
	     return temp;
	}
		
	public void load(){
		
		// Directory path here
		
		//String project_path = BP_EDITOR.class.getResource("../").getPath();
		//client_path = project_path.substring(0,project_path.length()-15) + "CLIENT/src/images/";
		
		System.out.println("CLIENT PATH: "+EditorSettings.clientImagePath);
		
		// FOR WINDOWS		
		String startingPath = EditorSettings.clientImagePath;
		String client_path = EditorSettings.clientImagePath;
		
		//"./bin/images/";
		
		ArrayList<File> newList = retriveAllFiles(startingPath);
        for( File f: newList){
        	//ServerMessage.printMessage("file path: "+f.getPath());
        	
        	if(f.getName().substring(f.getName().length()-4, f.getName().length()).equals(".png")){
        		
        		String path = f.getPath().replace("\\", "/");
        		
        		String[] pathSplit = path.split("images/");
        		
        		String gfxName = pathSplit[1].substring(0,pathSplit[1].length()-4);
        		
        		gfxName = gfxName.replace("\\", "/");
        		
        		// CHECK IF IMAGE IS ANIMATED
        		if(gfxName.contains("_0")){
        			
        			//GET NR OF ANIMATIONS
        			String onlyName[] = gfxName.split("_0");
        			String fname = onlyName[0];        			
        			
        			int nrAni = 1;

    				//ServerMessage.printMessage("check animation: "+startingPath+fname+"_1.png");
    				//ServerMessage.printMessage("fname: "+fname);

        			for(int i = 1; i < 10; i++){
        				File fani = new File(startingPath+fname+"_"+i+".png");
        				if(fani.exists()){
        					nrAni = i+1;
        				}
        			}
        			        			
        			//ServerMessage.printMessage("nr ani: "+nrAni);
        			gfxName = gfxName.substring(0,gfxName.length()-2);
        			
        			gfx.put(gfxName, new Sprite(client_path+gfxName, nrAni));
        			
        		}else if(!gfxName.substring(gfxName.length()-2,gfxName.length()-1).equals("_")){
        			//ServerMessage.printMessage("char: |"+gfxName.substring(gfxName.length()-2,gfxName.length()-1)+"|");
        			
        			gfx.put(gfxName, new Sprite(client_path+gfxName));
        			
        		}else{
        			//ServerMessage.printMessage("not loaded: "+gfxName);
        		}
        	}
        }
        
	}
	
	public Sprite getSprite(String spriteName){
		return gfx.get(spriteName);
	}
}
