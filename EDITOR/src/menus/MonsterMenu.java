package menus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import components.Monster;
import game.BP_EDITOR;
import gui.MonsterButton;

public class MonsterMenu {

	private Vector<MonsterButton> Buttons;
	private int X;
	private int Y;
	private boolean Active = false;
	private Vector<Monster> MONSTERS;
	
	private Image deleteIcon;
	
	
	public MonsterMenu (int x, int y){
		X = x;
		Y = y;

		Buttons = new Vector<MonsterButton>();
	
		MONSTERS = new Vector<Monster>();
	}
	
	
	public void load(){
		
		deleteIcon = BP_EDITOR.GFX.getSprite("gui/editor/deleteButton").getImage();
		
		Buttons.clear();
		
		int x = 0;
		int y = 0;
		
		ResultSet rs = BP_EDITOR.gameDB.askDB("select Id from creature order by Id asc");
		
		Buttons.add(new MonsterButton(X+x,Y+y,null));
		x+=50;
		
		try {
			while(rs.next()){
				Monster newMonster = new Monster(rs.getInt("Id"),0,0,"no");
				MonsterButton newButton = new MonsterButton(X+x,Y+20+y,newMonster);
			
				Buttons.add(newButton);
				
				y += 15;
				if(y > 440){
					x += 140;
					y = 0;
				}
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	
	public void draw(Graphics g, int mouseX, int mouseY){
		if(Active){
			g.setColor(new Color(238,82,65,255));
			g.fillRect(X, Y, 500, 500);
			
			g.setColor(new Color(255,255,255,255));
			
		
			g.setFont(BP_EDITOR.FONTS.size12);
			for (MonsterButton button : Buttons) {
				if(button.getMonster() == null){
					deleteIcon.draw(X,Y);
				}
				button.draw(g,mouseX,mouseY);
			}
		}
	}
	
	public int click(int mouseX, int mouseY){
		int buttonIndex = 0;
		for (MonsterButton button : Buttons) {
			if(button.clicked(mouseX, mouseY)){
				if(button.getMonster() == null){
					return 999;
				}
				return buttonIndex;
			}
			buttonIndex++;
		}
		return 1000;
	}
	
	public void toggle(){
		if(Active){
			Active = false;
		}else{
			Active = true;
		}
	}
	
	public boolean isActive(){
		return Active;
	}
	
	public Monster getMonster(int tileIndex){
		return Buttons.get(tileIndex).getMonster();
	}
}
