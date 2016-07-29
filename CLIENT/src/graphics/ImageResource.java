package graphics;

import game.BlueSaga;
import game.ClientSettings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import utils.FileHandler;

public class ImageResource {

	private static HashMap<String,Sprite> gfx;
	
	
	public static void load(){
		gfx = new HashMap<String,Sprite>();
		
		if(!ClientSettings.DEV_MODE){
			// FOR RUNNABLE JAR
			String pathToZip = BlueSaga.class.getProtectionDomain().getCodeSource().getLocation().getPath();
			try {
				pathToZip = URLDecoder.decode(pathToZip, "UTF-8");
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			ZipInputStream zis;
			try {
				zis = new ZipInputStream(new FileInputStream(pathToZip));
				try {
					for (ZipEntry ze; (ze = zis.getNextEntry()) != null;) {
						if(ze.getName().contains(".png")){
							
							String[] pathSplit = ze.getName().split("images/");
			        		
			        		String gfxName = pathSplit[1].substring(0,pathSplit[1].length()-4);
			        		
			        		// CHECK IF IMAGE IS ANIMATED
			        		if(gfxName.contains("_0")){
			        			
			        			//GET NR OF ANIMATIONS
			        			String onlyName[] = gfxName.split("_0");
			        			String fname = onlyName[0];        			
			        			
			        			int nrAni = 1;
	
			           			for(int i = 1; i < 10; i++){
			           				String pathToFile = pathSplit[0]+"images/"+fname+"_"+i+".png";
			           				
			           				JarFile jar = new JarFile(pathToZip);
			           				JarEntry entry = jar.getJarEntry(pathToFile);
			           				if (entry != null) {
			           					nrAni = i+1;
						        	}
			           				jar.close();
			           			}
			        			        			
			        			gfxName = gfxName.substring(0,gfxName.length()-2);
			        			
			        			
			        			gfx.put(gfxName, new Sprite("images/"+gfxName, nrAni));
			        			
			        			
			        		}else if(!gfxName.substring(gfxName.length()-2,gfxName.length()-1).equals("_")){
			        			gfx.put(gfxName, new Sprite("images/"+gfxName));
			        		}
						}
						
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
		    // FOR ECLIPSE
			String startingPath = BlueSaga.class.getResource("../images/").getPath();
			
			
			ArrayList<File> newList = FileHandler.retriveAllFiles(startingPath);
			
	        for( File f: newList){
	    
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
	
	           			for(int i = 1; i < 10; i++){
	        				File fani = new File(startingPath+fname+"_"+i+".png");
	        				if(fani.exists()){
	        					nrAni = i+1;
	        				}
	        			}
	        			        			
	        			gfxName = gfxName.substring(0,gfxName.length()-2);
	        			
	        			gfx.put(gfxName, new Sprite("images/"+gfxName, nrAni));
	        			
	        			
	        		}else if(!gfxName.substring(gfxName.length()-2,gfxName.length()-1).equals("_")){
	        			gfx.put(gfxName, new Sprite("images/"+gfxName));
	        		}
	        	}
	        }
		}
        
	}
	
	public static Sprite getSprite(String spriteName){
		return gfx.get(spriteName);
	}
	
}
