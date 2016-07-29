package data_handlers;

import game.BlueSaga;
import game.ClientSettings;
import graphics.BlueSagaColors;
import graphics.Font;
import graphics.ImageResource;
import screens.ScreenHandler;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;

import utils.LanguageUtils;
import components.Item;

public class FishingHandler extends Handler {

	private static boolean FishingGameActive = false;
	private static Item FishingGameFish = null;
	private static int FishingGameItr = 0;
	private static int FishGameX = 0;
	private static int FishGameY = 0;
	
	public FishingHandler() {
		super();
	}
	
	public static void handleData(String message){
		if(message.startsWith("<fishgame>")){
			String fishInfo[] = message.substring(10).split(",");
			int fishId = Integer.parseInt(fishInfo[0]);
			int fishX = Integer.parseInt(fishInfo[1]);
			int fishY = Integer.parseInt(fishInfo[2]);
			int fishZ = Integer.parseInt(fishInfo[3]);
			
			startFishingGame(fishId, fishX, fishY, fishZ);
			
		}
	}

	public static boolean isFishingGameActive() {
		return FishingGameActive;
	}

	public static void setFishingGameActive(boolean fishingGameActive) {
		FishingGameActive = fishingGameActive;
	}
	
	private static void startFishingGame(int fishId, int fishX, int fishY, int fishZ){
		FishingGameFish = new Item(fishId);
		FishingGameItr = 1;
		setFishingGameActive(true);
		FishGameX = fishX;
		FishGameY = fishY;
	//	FishGameZ = fishZ;
	}
	
	public static boolean drawFishingGame(Graphics g, GameContainer app){
		boolean mouseHover = false;
		
		int mouseX = app.getInput().getAbsoluteMouseX();
		int mouseY = app.getInput().getAbsoluteMouseY();
		
		if(isFishingGameActive()){
			int renderPosX = FishGameX*ClientSettings.TILE_SIZE + ScreenHandler.myCamera.getX() - 25;
			int renderPosY = FishGameY*ClientSettings.TILE_SIZE + ScreenHandler.myCamera.getY() - 25;
			
			ImageResource.getSprite("gui/world/flash").draw(renderPosX, renderPosY);
			ImageResource.getSprite("items/item"+FishingGameFish.getId()).draw(renderPosX-25,renderPosY-25);
			FishingGameItr++;
			
			g.setFont(Font.size16);
			if(FishingGameItr % 20 < 10){
				g.setColor(BlueSagaColors.WHITE);
			}else{
				g.setColor(BlueSagaColors.BLACK);
			}
			g.drawString(LanguageUtils.getString("ui.fishing.catch"), renderPosX-5, renderPosY+50);
			
			if(FishingGameItr > 80){
				FishingGameItr = 0;
				setFishingGameActive(false);
			}
			
			if(mouseX > renderPosX && mouseX < renderPosX+50 && mouseY > renderPosY && mouseY < renderPosY+50){
				mouseHover = true;
			}
			
		}
		return mouseHover;
	}
	
	public static void catchFish(int tileX, int tileY){
		if(isFishingGameActive()){
			if(tileX == FishGameX && tileY == FishGameY){
				BlueSaga.client.sendMessage("fishgamecatch", "start");
			}
		}
	}
	
}
