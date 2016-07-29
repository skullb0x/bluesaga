package sound;

import game.BlueSaga;
import game.Database;
import game.ClientSettings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;

import utils.FileHandler;
import utils.RandomUtils;

public class Sfx {
	
	static HashMap<String,Sound> sfx;
	
	public Sfx(){
	}
	
	public static void load(Database gameDB){
		sfx = new HashMap<String,Sound>();
		

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
						if(ze.getName().contains(".ogg") && !ze.getName().contains("music") && !ze.getName().contains("ambient")){
							String[] pathSplit = ze.getName().split("sfx/");
			        		
			        		String sfxName = pathSplit[1].substring(0,pathSplit[1].length()-4);
			        		
			        		try {
								sfx.put(sfxName, new Sound("sfx/"+sfxName+".ogg"));
							} catch (SlickException e) {
								e.printStackTrace();
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
			String startingPath = BlueSaga.class.getResource("../sfx/").getPath();
			
			ArrayList<File> newList = FileHandler.retriveAllFiles(startingPath);
	        for( File f: newList){
	        	
	        	if(f.getName().substring(f.getName().length()-4, f.getName().length()).equals(".ogg")){
	        		
	        		String path = f.getPath().replace("\\", "/");
	        		
	        		String[] pathSplit = path.split("sfx/");
	        		
	        		String sfxName = pathSplit[1].substring(0,pathSplit[1].length()-4);
	        		
	        		sfxName = sfxName.replace("\\", "/");
	        		
	        		try {
						sfx.put(sfxName, new Sound("sfx/"+sfxName+".ogg"));
					} catch (SlickException e) {
						e.printStackTrace();
					}
	        	}
	        }
		}
		
	}
	
	public static void play(String soundName, float pitch, float volFactor){
		if(ClientSettings.SFX_ON && sfx.get(soundName) != null){
			sfx.get(soundName).play(pitch, ClientSettings.soundVolume*volFactor);
		}
	}
	
	public static void playRandomPitch(String soundName){
		if(ClientSettings.SFX_ON){
			float MEAN = 1.0f; 
	    	float VARIANCE = 0.1f;
	    
	    	try {
	    		float pitch = MEAN + RandomUtils.getFloat(0.0f, 1.0f) * VARIANCE;
		    	if(sfx.get(soundName) != null){
		    		if(!sfx.get(soundName).playing()){
		    			sfx.get(soundName).play(pitch, ClientSettings.soundVolume);
		    		}
		    	}
	    	}catch(java.nio.BufferOverflowException e){
	    		
	    	}
	    }
	}
	
	public void playRandomPitch(String soundName, float volFactor){
		if(ClientSettings.SFX_ON){
			float MEAN = 1.0f; 
	    	float VARIANCE = 0.1f;
	    
	    	float pitch = (float) (MEAN + RandomUtils.getGaussian() * VARIANCE);
			if(sfx.get(soundName) != null){
				sfx.get(soundName).play(pitch, ClientSettings.soundVolume*volFactor);
			}
		}
	}
	
	public void stop(String soundName){
		sfx.get(soundName).stop();
	}
	
	public static void play(String soundName){
		if(ClientSettings.SFX_ON){
			if(sfx.get(soundName) != null){
				sfx.get(soundName).play(1.0f,ClientSettings.soundVolume);
			}
		}
	}
	
	public static Sound getSound(String soundName){
		return sfx.get(soundName);
	}
	

}
