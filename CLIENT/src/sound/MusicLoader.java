package sound;

import game.ClientSettings;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.newdawn.slick.Music;
import org.newdawn.slick.SlickException;

public class MusicLoader extends Thread {

	private String songName;
	private HashMap<String,Music> songs;
	private Timer playSongTimer;
	
	
	public MusicLoader(HashMap<String,Music> songs, String song){
		songName = song;
		this.songs = songs;
		playSongTimer = new Timer();
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			if(!songs.containsKey(songName)){
			    for(Music m: songs.values()){
			    	m.stop();
			    	m = null;
			    }
				songs.clear();
			    Runtime basurero = Runtime.getRuntime(); 
			    basurero.gc();
			    basurero.gc();
				songs.put(songName, new Music("music/"+songName+".ogg"));
				playSongTimer.schedule( new TimerTask(){
			        @Override
					public void run() {
			        	if(songName.equals(BgMusic.activeSong)){
			        		songs.get(BgMusic.activeSong).loop(1.0f,ClientSettings.musicVolume);
			        		songs.get(BgMusic.activeSong).setVolume(ClientSettings.musicVolume);
			        	}
			        }
			      }, 1000);
			}else{
				if(songName.equals(BgMusic.activeSong)){
					songs.get(BgMusic.activeSong).loop(1.0f,ClientSettings.musicVolume);
	        		songs.get(BgMusic.activeSong).setVolume(ClientSettings.musicVolume);
				}
        	}
		} catch (SlickException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
}
