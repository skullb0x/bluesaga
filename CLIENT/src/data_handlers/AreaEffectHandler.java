package data_handlers;

import org.newdawn.slick.Color;

import game.BlueSaga;
import screens.ScreenHandler;

public class AreaEffectHandler extends Handler {
	
	public static boolean nightTime = false;
	
	public AreaEffectHandler() {
		super();
	}

	public static void handleData(String serverData){
		if(serverData.startsWith("<area_effect>")){
			
			String effectInfo[] = serverData.substring(13).split(",");
			
			int areaEffectId = Integer.parseInt(effectInfo[0]);
			
			String areaName = effectInfo[1];
			
			boolean tintbool = false; 
			int tint = Integer.parseInt(effectInfo[2]);
			
			int tintR = Integer.parseInt(effectInfo[3]);
			int tintG = Integer.parseInt(effectInfo[4]);
			int tintB = Integer.parseInt(effectInfo[5]);
					
			if(tint == 1){
				tintbool = true;
			}
			
			boolean fogbool = false; 
			int fog = Integer.parseInt(effectInfo[6]);
			if(fog == 1){
				fogbool = true;
			}
			
			int fogR = Integer.parseInt(effectInfo[7]);
			int fogG = Integer.parseInt(effectInfo[8]);
			int fogB = Integer.parseInt(effectInfo[9]);
			
			
			if(!areaName.equals("")){
				ScreenHandler.AREA_EFFECT.showAreaName(areaName);
			}
		
			String songName = effectInfo[10];
			String ambientName = effectInfo[11];

			String ParticleType = effectInfo[12];
			
			int guardLevel = Integer.parseInt(effectInfo[13]);
			
			ScreenHandler.AREA_EFFECT.setAreaEffect(areaEffectId, tintbool, new Color(tintR,tintG,tintB), fogbool, new Color(fogR,fogG,fogB),ParticleType, guardLevel);
			
			BlueSaga.BG_MUSIC.changeSong(songName,ambientName);
		
		}else if(serverData.startsWith("<night>start")){
			nightTime = true;
			BlueSaga.BG_MUSIC.changeSong("night", "night");
			ScreenHandler.AREA_EFFECT.setTintColor(new Color(185,150,255));
			ScreenHandler.AREA_EFFECT.setParticleType("firefly");
					
		}else if(serverData.equals("<night>stop")){
			nightTime = false;
			ScreenHandler.AREA_EFFECT.removeTintColor();
			ScreenHandler.AREA_EFFECT.removeParticles();
		}else if(serverData.startsWith("<night>now")){
			nightTime = true;
			BlueSaga.BG_MUSIC.changeSong("night", "night");
			ScreenHandler.AREA_EFFECT.setTintColorNow(new Color(185,150,255));
			ScreenHandler.AREA_EFFECT.setParticleType("firefly");
			//BlueSaga.BG_MUSIC.changeSong("night", "night");
		}else if(serverData.equals("<night>stopnow")){
			ScreenHandler.AREA_EFFECT.setTintColor(new Color(255,255,255));
			ScreenHandler.AREA_EFFECT.removeParticlesNow();
		}
	}
	
}
