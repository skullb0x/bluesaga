package gui;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

public class MapButton {

	private int X = 0;
	private int Y = 0;
	private int width = 180;
	private int height = 20;
	
	private int AreaId;
	private String Name;
	
	
	public MapButton(int areaId, String name, int x, int y) {
		AreaId = areaId;
		Name = name;
		X = x;
		Y = y;
	}
	
	
	public boolean clicked(int mouseX, int mouseY){
		if(mouseX > X && mouseX < X + width && mouseY > Y && mouseY < Y+height){
			return true;
		}
		return false;
	}
	
	public void draw(Graphics g, int mouseX, int mouseY){
		
		if(X < mouseX && X+width > mouseX && Y < mouseY && Y+height > mouseY){
			g.setColor(new Color(255,255,255,100));
			g.fillRect(X,Y,width,height);
			g.setColor(new Color(0,0,0,255));
		}else{
			g.setColor(new Color(255,255,255,255));
		}
		
		g.drawString(Name,X+10,Y);
	}
	
	public int getAreaId(){
		return AreaId;
	}
	
	
}
