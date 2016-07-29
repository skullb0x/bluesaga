package data_handlers;

import sound.Sfx;
import game.BlueSaga;
import gui.Gui;

public class CardHandler extends Handler {
	
	public static void handleData(String serverData){
		if(serverData.startsWith("<card_book>")){
			String cardsInfo = serverData.substring(11);
			Gui.CardBook.load(cardsInfo);
		}else if(serverData.startsWith("<card_place>")){
			Sfx.play("items/add_card");
			BlueSaga.actionServerWait = false;
		}
	}
}
