package data_handlers;

import java.util.Timer;
import java.util.TimerTask;







import sound.Sfx;
import graphics.BlueSagaColors;
import gui.Gui;

public class QuestHandler extends Handler{

	private static Timer completeTimer;
	private static String latestQuestName;
	
	public QuestHandler(){
		super();
	}

	public static void handleData(String serverData){
		// NPC - QUEST INFO
		if(serverData.startsWith("<talknpc>")){
			String npcInfo = serverData.substring(9);
			Gui.QUEST_DIALOG.loadQuests(npcInfo);
		}else if(serverData.startsWith("<questinfo>")){
			String questInfo[] = serverData.substring(11).split(";");
			
			int questId = Integer.parseInt(questInfo[0]);
			String questType = questInfo[1];
			String questMessage = questInfo[2];
			int questStatus = Integer.parseInt(questInfo[3]);
			
			Gui.QUEST_DIALOG.showQuestInfo(questId,questType,questMessage, questStatus);
			
		}else if(serverData.startsWith("<quest>")){
			String questInfo[] = serverData.substring(7).split(";");
			String action = questInfo[0];
			String questName = questInfo[1];
			
			if(action.equals("add")){
				Gui.addMessage(questName+ " - #messages.quest.added", BlueSagaColors.RED);
			}else if(action.equals("complete")){
				latestQuestName = questName;
				completeTimer = new Timer();
				completeTimer.schedule( new TimerTask(){
			        @Override
					public void run() {
			        	Sfx.play("notifications/quest_updated");
			        	Gui.addMessage(latestQuestName+ " - #messages.quest.completed", BlueSagaColors.RED);
			        }
			      }, 2000);
			}else if(action.equals("update")){
				String questUpdate = questInfo[2];
				
				Sfx.play("notifications/quest_updated");
				String message = questName + " - #messages.quest.updated#" + System.getProperty("line.separator") + questUpdate;
				Gui.addMessage(message, BlueSagaColors.RED);
			}
		}
		
		if(serverData.startsWith("<myquests>")){
			String questInfo = serverData.substring(10);
			Gui.QuestWindow.load(questInfo);
			Gui.QuestWindow.open();
			
		}
		
		if(serverData.startsWith("<questdesc>")){
			String questText = serverData.substring(11);
			Gui.QuestWindow.showQuest(questText);
			
		}
	}
}