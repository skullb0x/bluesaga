package gui;
import game.BP_EDITOR;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.newdawn.slick.Animation;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;


public class Gui {
	
	
	private Image attackEvent;
    
	private ActionBar ActionBar = new ActionBar(440,536);
	
	// NOTIFICATIONS
	private ArrayList<Notification> Notifications;
	private int NotificationsY;
	
	private Timer timer = new Timer();
	  
	
	// LOOT INFO
	private String LootName;
	private int LootGold;
	private Image foundLootBg;
	private boolean FOUND_LOOT;
	
	// GOLD INDICATOR
	private Image goldIndicator;
	
	private Timer lootTimer;

	// GAME OVER 
	private Image Die_Label;
	
	// MAP NAME
	private boolean SHOW_AREA_NAME = false;
	private int AreaNameOpacity = 255;
	private String MapName;
	
	
	public Gui() {
		Notifications = new ArrayList<Notification>();
		Notifications.clear();
		NotificationsY = 20;
		MapName = "";
	}
	
	
	public void init() {
		
		
		lootTimer = new Timer();
		
	}
	
	public ActionBar getActionBar(){
		return ActionBar;
	}
	
	public void loadActionbar(){
	}
	
	public void draw(Graphics g, String mapName, boolean winActive, int Gold, int MouseX, int MouseY) {
	/*	
		if(winActive){
			menu_button_active.draw(20,20);
		}else{
			menu_button.draw(20,20);
		}
		*/
		
		g.setFont(BP_EDITOR.FONTS.size12);
	
	
		
		goldIndicator.draw(20,80);
		g.setColor(new Color(255,250,84,255));
		g.drawString("Gold: "+Gold, 40, 90);
		
		for(int i = 0; i < Notifications.size(); i++){
			Notification notif = Notifications.get(i);
			
			g.setColor(new Color(190,60,60,notif.getOpacity()));
			g.fillRoundRect(700, notif.getY(), 300, 40 + notif.getNrTextLines()*12, 10);
			
			g.setColor(new Color(255,255,255,notif.getOpacity()));
			g.drawString(notif.getText(),720,notif.getY()+20);
			
			notif.update();
			
			if(!notif.isActive()){
				int changeY = 45 + notif.getNrTextLines()*12;
				NotificationsY -= changeY;
				for(int j = i; j < Notifications.size(); j++){
					Notifications.get(j).moveUp(changeY);
				}
			}
		}
		
		for(int i = 0; i < Notifications.size();i++){
			if(!Notifications.get(i).isActive()){
				Notifications.remove(i);
				i--;
			}
		}
		
		
		if(FOUND_LOOT){
			int lootY = 150;
			
			foundLootBg.draw(400,lootY);
			
			lootY += 60;
			
			if(!LootName.equals("none")){
				g.setColor(new Color(255,255,255,255));
				g.drawString(LootName,420,lootY);
				lootY += 20;
			}
			
			if(LootGold > 0){
				g.setColor(new Color(255,250,84,255));
				g.drawString(LootGold + " Gold",420,lootY);
			}
		}
		
		
		
		
		if(SHOW_AREA_NAME){
			
			g.setFont(BP_EDITOR.FONTS.size30);
			
			g.setColor(new Color(165,165,85,AreaNameOpacity));
			g.drawString(MapName, 512 - BP_EDITOR.FONTS.size30.getWidth(MapName)/2, 75);
			g.setColor(new Color(255,250,85,AreaNameOpacity));
			g.drawString(MapName, 510 - BP_EDITOR.FONTS.size30.getWidth(MapName)/2, 73);
			if(AreaNameOpacity < 255){
				AreaNameOpacity -= 2;
			}
			if(AreaNameOpacity <= 0) {
				SHOW_AREA_NAME = false;
			}
		}
		
	}
	
	
	public void drawGameOver(Graphics g){
		Die_Label.draw(415,300);
		g.setColor(new Color(255,255,255,255));
		g.drawString("Press SPACE to respawn at your last checkpoint",340,360);
	}
	
	
	public void foundLoot(String lootName, int gold){
		
		FOUND_LOOT = true;
		LootName = lootName;
		LootGold = gold;
		
	
		if(!LootName.equals("none")){
			addMessage("Added "+LootName+" to inventory!");
		}
		if(LootGold > 0){
			addMessage("Added "+LootGold+" Gold to treasures");
		}
		
		lootTimer.schedule( new TimerTask(){
			public void run() {
				FOUND_LOOT = false;
			}
	      }, 3000);
		
	}
	
	public void addMessage(String newMessage) {
		Notification newNotif;
		newNotif = new Notification(newMessage, NotificationsY);
		NotificationsY += 45 + newNotif.getNrTextLines()*12;
		Notifications.add(newNotif);
	}
	
	
	public void showAttackEvent() {
		attackEvent.draw(450,250);
	}
	
	
	public int keyLogic(Input INPUT){
		return ActionBar.keyLogic(INPUT);
	}
	
	public void showAreaName(String newMapName){
		MapName = newMapName;
		SHOW_AREA_NAME = true;
		AreaNameOpacity = 255;
		timer.schedule( new TimerTask(){
	        public void run() {
	        	AreaNameOpacity -= 2;
	        }
	      }, 1000);
	}
	
	/*
	 * 
	 * 	ACTIONBAR
	 * 
	 * 
	 */
	
	
	public void unselectAction(){
		ActionBar.unselectAction();
	}
	
	public String getSelectedActionType(){
		return ActionBar.getSelectedActionType();
	}
	
	public int getSelectedActionId(){
		return ActionBar.getSelectedActionId();
	}
	
}
