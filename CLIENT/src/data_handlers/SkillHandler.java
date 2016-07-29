package data_handlers;

import abilitysystem.Skill;
import sound.Sfx;
import game.BlueSaga;
import graphics.BlueSagaColors;
import gui.Gui;
import screens.ScreenHandler;

public class SkillHandler extends Handler {

	public SkillHandler() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
	public static void handleData(String serverData){
		if(serverData.startsWith("<skilldata>")){
			String skilldata = serverData.substring(11);
			
			if(!skilldata.equals("")){
				String skillInfo[] = skilldata.split(";");
				
				
				// SkillId, SkillName, SkillLevel, SkillSP ; ...
				
				for(String skill: skillInfo){
					
					String skill_info[] = skill.split(",");
					int skillId = Integer.parseInt(skill_info[0]);
					String skillName = skill_info[1];
					int level = Integer.parseInt(skill_info[2]);
					int sp = Integer.parseInt(skill_info[3]);
					int spnext = Integer.parseInt(skill_info[4]);
					
					Skill newSkill = new Skill(skillId);
					newSkill.setName(skillName);
					newSkill.setLevel(level);
					newSkill.setSP(sp);
					newSkill.setSPnext(spnext);
					
					BlueSaga.playerCharacter.addSkill(newSkill);
				}
			}
			
			
			ScreenHandler.LoadingStatus = "Loading particle info...";
			BlueSaga.client.sendMessage("particledata", "info");
		
		}else if(serverData.startsWith("<set_sp>")){
		
			String spInfo[] = serverData.substring(8).split(",");
			int skillId = Integer.parseInt(spInfo[0]);
			int SP = Integer.parseInt(spInfo[1]);
		
			Skill playerSkill = BlueSaga.playerCharacter.getSkillById(skillId);
			if(playerSkill != null){
				playerSkill.setSP(SP);
			}
		}else if(serverData.startsWith("<sp_levelup>")){
			String spInfo[] = serverData.substring(12).split(",");
			
			// skillName, skillLevel, SP, SPnext
			int skillId = Integer.parseInt(spInfo[0]);
			int skillLevel = Integer.parseInt(spInfo[1]);
			int SP = Integer.parseInt(spInfo[2]);
			int SPnext = Integer.parseInt(spInfo[3]);
			
			BlueSaga.playerCharacter.getSkillById(skillId).setLevel(skillLevel);
			BlueSaga.playerCharacter.getSkillById(skillId).setSP(SP);
			BlueSaga.playerCharacter.getSkillById(skillId).setSPnext(SPnext);
			
			Gui.addMessage(BlueSaga.playerCharacter.getSkillById(skillId).getName()+" #messages.classes.leveled_up", BlueSagaColors.BLUE);
			
			Sfx.play("notifications/level_up");
		}else if(serverData.startsWith("<skill_down>")){
			String skillInfo[] = serverData.substring(12).split(";");
			int skillId = Integer.parseInt(skillInfo[0]);
			int newLevel = Integer.parseInt(skillInfo[1]);
			int newSP = Integer.parseInt(skillInfo[2]);
			
			Skill playerSkill = BlueSaga.playerCharacter.getSkillById(skillId);
			if(playerSkill != null){
				playerSkill.setLevel(newLevel);
				playerSkill.setSP(newSP);
				Gui.addMessage("#messages.classes.lost_level# "+playerSkill.getName(), BlueSagaColors.RED);
			}
		}
	}

}
