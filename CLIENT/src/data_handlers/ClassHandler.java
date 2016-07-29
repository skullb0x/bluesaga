package data_handlers;

import player_classes.BaseClass;
import sound.Sfx;
import utils.GameInfo;
import game.BlueSaga;
import graphics.BlueSagaColors;
import gui.Gui;

public class ClassHandler extends Handler {

	
	public static void handleData(String message){
		if(message.startsWith("<class_levelup>")){
			String classInfo[] = message.substring(15).split(",");
			int classId = Integer.parseInt(classInfo[0]);
			int classLevel = Integer.parseInt(classInfo[1]);
			int nextXP = Integer.parseInt(classInfo[2]);
			
			BaseClass usedClass = BlueSaga.playerCharacter.getClassById(classId);
			
			if(usedClass != null){
				usedClass.level = classLevel;
				usedClass.nextXP = nextXP;
				usedClass.setXp(0);
				
				Gui.addMessage(usedClass.name+" #messages.classes.leveled_up#", BlueSagaColors.getColorFromString(usedClass.bgColor));
			}
		}else if(message.startsWith("<class_xp>")){
			String classInfo[] = message.substring(10).split(",");
			int classId = Integer.parseInt(classInfo[0]);
			int classXp = Integer.parseInt(classInfo[1]);
			
			BaseClass usedClass = BlueSaga.playerCharacter.getClassById(classId);
			
			if(usedClass != null){
				usedClass.setXp(classXp);
			}
		}else if(message.startsWith("<class_learn>")){
			String classInfo[] = message.substring(13).split(",");
			int classId = Integer.parseInt(classInfo[0]);
			int classType = Integer.parseInt(classInfo[1]);
			
			Sfx.play("notifications/learn_class");
			
			if(classType == 1){
				BlueSaga.playerCharacter.setPrimaryClass(GameInfo.classDef.get(classId));
				BlueSaga.playerCharacter.getPrimaryClass().setXp(0);
				BlueSaga.playerCharacter.getPrimaryClass().level = 1;
			}else if(classType == 2){
				BlueSaga.playerCharacter.setSecondaryClass(GameInfo.classDef.get(classId));
				BlueSaga.playerCharacter.getSecondaryClass().setXp(0);
				BlueSaga.playerCharacter.getSecondaryClass().level = 1;
			}
		}
	}

}
