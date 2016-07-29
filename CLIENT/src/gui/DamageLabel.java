package gui;

import graphics.Font;
import graphics.ImageResource;
import screens.ScreenHandler;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.UnicodeFont;

public class DamageLabel {

	private String LabelText;
	private Color TextColor;
	private int Y;
	private int X;
	private int changeY;
	private int Alpha;
	private int AbilityId;
	private UnicodeFont LabelFont = Font.size18;
	
	public DamageLabel(String text, Color textColor, int x, int y, int abilityId){
		LabelText = text;
		TextColor = textColor;
		X = x;
		Y = y;
		
		AbilityId = abilityId;
		
		changeY = 0;
		Alpha = 255;
	}
	
	public void draw(Graphics g, int playerX, int playerY){
		int cameraX = ScreenHandler.myCamera.getX();
		int cameraY = ScreenHandler.myCamera.getY();
		
		int labelX = playerX - ((playerX-cameraX)-X) - Math.round((float) LabelFont.getWidth(LabelText)/2) - 25;// - cameraX;
		int labelY = playerY - ((playerY-cameraY)-Y) + changeY - 50;// - cameraY - changeY;
		
		g.setFont(LabelFont);
		
		
		if(AbilityId > 0){
			ImageResource.getSprite("abilities/ability_icon"+AbilityId).draw(labelX-30,labelY-15,new Color(255,255,255,Alpha));
		}
		
		// TEXT SHADOW
		g.setColor(new Color(0,0,0,Alpha));
		if(AbilityId > 0){
			g.drawString(LabelText, labelX+22, labelY+2);
		}else{
			g.drawString(LabelText, labelX+2, labelY+2);
		}

		// TEXT
		g.setColor(new Color(TextColor.getRed(),TextColor.getGreen(),TextColor.getBlue(), Alpha));
		if(AbilityId > 0){
			g.drawString(LabelText, labelX+20, labelY);
		}else{
			g.drawString(LabelText, labelX, labelY);
		}
	
		changeY--;
		Alpha = 305 + changeY;
	}
	
	public int getAlpha(){
		return Alpha;
	}

	
	public void setFont(UnicodeFont newFont){
		LabelFont = newFont;
	}
	
	
}
