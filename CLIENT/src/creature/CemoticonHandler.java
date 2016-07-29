package creature;

import graphics.ImageResource;

public class CemoticonHandler {
	private int ShowTimeItr = 0;
	private int ShowTimeEnd = 150;
	private String ShownEmoticon = "None";
	
	public CemoticonHandler(Creature aCreature){
		ShowTimeItr = 0;
		
	}
	
	public void draw(int x, int y){
		if(ShowTimeItr > 0){
			if(!ShownEmoticon.equals("None")){
				ShowTimeItr--;
				ImageResource.getSprite("gui/emoticons/emo_"+ShownEmoticon).drawCentered(x,y);
			}else{
				ShowTimeItr = 0;
			}
		}
	}
	
	public void show(String emoticonName){
		ShownEmoticon = emoticonName;
		ShowTimeItr = ShowTimeEnd;
	}

}
