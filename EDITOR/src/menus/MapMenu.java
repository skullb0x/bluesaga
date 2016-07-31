package menus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import components.Monster;
import game.BP_EDITOR;
import gui.MapButton;

public class MapMenu {

	private Vector<MapButton> Buttons;
	private int X;
	private int Y;
	private boolean Active = false;
	private Vector<Monster> MONSTERS;
	
	private Image deleteIcon;
	
	
	public MapMenu (int x, int y){
		X = x;
		Y = y;

		Buttons = new Vector<MapButton>();
	
		MONSTERS = new Vector<Monster>();
	}
	
	
	public void load(){
		
		deleteIcon = BP_EDITOR.GFX.getSprite("gui/editor/deleteButton").getImage();
		
		Buttons.clear();
		
		int x = 0;
		int y = 20;
		
		ResultSet rs = BP_EDITOR.mapDB.askDB("select Id, Name from area_tile order by Id asc");
		
		try {
			while(rs.next()){
				
				Buttons.add(new MapButton(rs.getInt("Id"),rs.getString("Name"),X+x,Y+y));
				
				y += 20;
				
				if(y > 400){
					x += 150;
					y = 20;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	
	public void draw(Graphics g, int mouseX, int mouseY){
		if(Active){
			g.setColor(new Color(238,82,65,255));
			g.fillRect(X, Y, 600, 500);
			
			g.setColor(new Color(255,255,255,255));
			g.setFont(BP_EDITOR.FONTS.size8);
			
			for (MapButton button : Buttons) {
				button.draw(g,mouseX,mouseY);
			}
			g.setColor(new Color(255,255,255,255));
			g.drawRect(X, Y, 600, 500);
		}
	}
	
	public int click(int mouseX, int mouseY){
		for (MapButton button : Buttons) {
			if(button.clicked(mouseX, mouseY)){
				return button.getAreaId();
			}
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
	
}
