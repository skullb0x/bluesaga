package data_handlers;

import game.BlueSaga;
import graphics.BlueSagaColors;
import gui.Gui;
import sound.Sfx;

public class CraftingHandler extends Handler {

	public CraftingHandler(){
		
	}
	
	public static void handleData(String serverData){
		if(serverData.startsWith("<addrecipe>")){
			Sfx.play("items/add_recipe");
			Gui.MouseItem.clear();
		}else if(serverData.startsWith("<recipes>")){
			String recipesInfo = serverData.substring(9);
			
			Gui.CraftingWindow.load(recipesInfo);
		}else if(serverData.startsWith("<nocraftitem>")){
			String ingredients = serverData.substring(13);
			BlueSaga.actionServerWait = false;
			Gui.addMessage("#messages.crafting.missing_need# "+ingredients, BlueSagaColors.RED);
		}else if(serverData.startsWith("<crafting_done>")){
			Gui.addMessage("#messages.crafting.successful", BlueSagaColors.RED);
			BlueSaga.actionServerWait = false;
		}
	}
}
