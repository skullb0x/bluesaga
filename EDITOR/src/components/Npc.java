package components;

import game.Database;

import java.sql.ResultSet;
import java.sql.SQLException;


import org.newdawn.slick.Animation;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;



public class Npc {
	
	private int Id;
	private String GfxName;
	private int X;
	private int Y;
	private int RequestQuestId;
	private int Size;
	
	private String Name;
	private Image [] graphics;
	
	protected Animation animation;
	
	protected Animation talkBubble;
	protected Animation newQuestBubble;
	protected Animation completeQuestBubble;
	
	private Image Shadow;
	
	private String questStatus = "none";
	
	public Npc(int newId, Database gameDB) {
		
		Id = newId;
		try {
			ResultSet rs = gameDB.askDB("select npc.GfxName as nGfxName, creature.Size as cSize, npc.Name as nName, npc.X as nX, npc.Y as nY, npc.RecruitQuestId as nRequestQuestid from creature, npc where npc.Id = "+newId);
			
			while (rs.next()) {
				Name = rs.getString("nName");
		    	GfxName = rs.getString("nGfxName");
				X = rs.getInt("nX");
		    	Y = rs.getInt("nY");
		    	RequestQuestId = rs.getInt("nRequestQuestid");
		    	Size = rs.getInt("cSize");
		    }
		
			rs.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	    
		try {
			graphics = new Image[2];      
			graphics[0] = new Image("images/npc/"+GfxName+"_0.png");
			graphics[1] = new Image("images/npc/"+GfxName+"_1.png");
		
			animation = new Animation(graphics, 500, true);
			
			graphics = new Image[2];      
			graphics[0] = new Image("images/gui/emoticons/newquest_0.png");
			graphics[1] = new Image("images/gui/emoticons/newquest_1.png");
		
			newQuestBubble = new Animation(graphics, 500, true);
		
			graphics = new Image[2];      
			graphics[0] = new Image("images/gui/emoticons/completequest_0.png");
			graphics[1] = new Image("images/gui/emoticons/completequest_1.png");
		
			completeQuestBubble = new Animation(graphics, 500, true);
			
			graphics = new Image[4];
			graphics[0] = new Image("images/gui/emoticons/talk_0.png");
			graphics[1] = new Image("images/gui/emoticons/talk_1.png");
			graphics[2] = new Image("images/gui/emoticons/talk_2.png");
			graphics[3] = new Image("images/gui/emoticons/talk_3.png");
			
			talkBubble = new Animation(graphics,500,true);
			
			
			Shadow = new Image("images/monsters/shadow"+Size+".png");
			
		} catch (SlickException e) {
			e.printStackTrace();
		}
	}
	
	public void draw(Graphics g, int x, int y, int playerX, int playerY){
		Shadow.draw(x,y+5);
		animation.draw(x, y-5); 
		
		if(Math.abs(playerX-X) + Math.abs(playerY-Y) == 1){
			talkBubble.draw(x+10,y-30);
		}else if(questStatus.equals("New")){
			newQuestBubble.draw(x+10,y-30);
		}else if(questStatus.equals("Reward")){
			completeQuestBubble.draw(x+10,y-30);
		}
	}
	
	
	public int getId() {
		return Id;
	}
	
	public int getX(){
		return X;
	}
	
	public int getY() {
		return Y;
	}
	
	public String getName() {
		return Name;
	}
	
	public void setQuestStatus(String newQuestStatus){
		questStatus = newQuestStatus;
	}
	
	public int getRequestQuestId() {
		return RequestQuestId;
	}
}