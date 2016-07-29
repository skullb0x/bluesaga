package data_handlers;

import game.BlueSaga;
import game.ClientSettings;
import graphics.BlueSagaColors;
import graphics.Font;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;


public class GatheringHandler extends Handler {

	private static boolean gatheringActive;
	
	private static int gatherX = 0;
	private static int gatherY = 0;
	private static int gatherZ = 0;

	private static int gatheringTimeTotal = 200;
	private static int gatheringTimeItr = 0;
	
	public GatheringHandler(){
		super();
		setGatheringActive(false);
	}
	
	public static void handleData(String message){
		
	}
	
	public static void draw(Graphics g,int cameraX, int cameraY, int mouseX, int mouseY){
		
		if(isGatheringActive()){
			int renderPosX = gatherX*ClientSettings.TILE_SIZE + cameraX - 62;
			int renderPosY = gatherY*ClientSettings.TILE_SIZE + cameraY - 70;
			
			
			// DARK BORDER
			g.setColor(new Color(164,42,42,255));
			g.fillRoundRect(renderPosX, renderPosY, 120,42,10);
			
			// LIGHTER COLOR
			g.setColor(new Color(236,86,86,255));
			g.fillRoundRect(renderPosX + 4, renderPosY + 4, 112, 34, 5);
			
			
			g.setColor(new Color(255,255,255));
			g.setFont(Font.size10);
			g.drawString("Gathering...",renderPosX+25, renderPosY+10);
			
			g.setColor(BlueSagaColors.YELLOW);
			
			int progressWidth = Math.round(((float) gatheringTimeItr / (float) gatheringTimeTotal) * 100.0f);
			g.fillRoundRect(renderPosX + 10, renderPosY + 25, progressWidth, 7, 10);
			
			gatheringTimeItr++;
			if(gatheringTimeItr >= gatheringTimeTotal){
				setGatheringActive(false);
				BlueSaga.client.sendMessage("gathering",gatherX+","+gatherY+","+gatherZ);
			}
		}
	}
	
	public static void startGathering(String gatherInfo){
		String tileInfo[] = gatherInfo.split(",");
		
		gatheringTimeItr = 0;
		
		gatherX = Integer.parseInt(tileInfo[1]);
		gatherY = Integer.parseInt(tileInfo[2]);
		gatherZ = Integer.parseInt(tileInfo[3]);
		
		setGatheringActive(true);
	}
	
	public static void stopGathering(){
		gatherX = 0;
		gatherY = 0;
		gatherZ = 0;
		gatheringTimeItr = 0;
		setGatheringActive(false);
	}
	
	public static boolean isGatheringActive(){
		return gatheringActive;
	}
	
	public static void setGatheringActive(boolean state){
		gatheringActive = state;
	}
	
}
