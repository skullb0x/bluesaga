package gui;

import game.BP_EDITOR;

import java.util.Vector;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;

public class GuiList {
	
	private Image Cursor;
	private int cursorPos;
	
	private Vector<String> Items = new Vector<String>();
	
	private int drawX;
	private int drawY;
	
	private int Width;
	private int Height;
	
	private int NrShow;
	
	private boolean ShowCursor = true;
	
	public GuiList(int width, int height){
		Width = width;
		Height = height;
		
		NrShow = (int) Math.floor(Height / 20);
		
		cursorPos = 0;
		
		Cursor = BP_EDITOR.GFX.getSprite("gui/cursors/cursor_pointer").getImage();
		
	}
	
	public void reset(){
		Items.clear();
		
		cursorPos = 0;
	}
	
	public void addItem(String newItem){
		Items.add(newItem);
	}
	
	
	public void moveCursor(String dir){
		if(dir == "UP"){
			cursorPos--;
			if(cursorPos < 0){
				cursorPos = Items.size()-1;
			}
		}else if(dir == "DOWN"){
			cursorPos++;
			if(cursorPos == Items.size()){
				cursorPos = 0;
			}
		}
	}
	
	public void draw(Graphics g, int x, int y){
		
		//g.setColor(new Color(108,33,33,255));
		//g.fillRoundRect(x, y, Width, Height, 10);
		
		// SHOW INVENTORY ITEMS OF RIGHT TYPE
		g.setColor(new Color(255,255,255,255));
		
		g.setWorldClip(x,y,Width,Height);
		
		for(int i = 0; i < Items.size(); i++){
			
			if(cursorPos < NrShow){
				drawX = x;
				drawY = y+i*20;
			}else {
				drawX = x;
				drawY = y+(i - (cursorPos - NrShow+1))*20;
			}
				
			g.drawString(Items.get(i),drawX+20,drawY+10);
		}
		
		// DRAW SCROLLBAR BG
		g.setColor(new Color(108,33,33,255));
		g.fillRoundRect(x+Width-15, y+4, 8, Height-12, 4);
		
		// DRAW SCROLLBAR SLIDE
		if(Items.size() > NrShow){
			g.setColor(new Color(255,255,255,255));
			g.fillRoundRect(x+Width-15, y + 4 + cursorPos*(Height / Items.size()), 8, (Height / Items.size()), 4);
		}
		
		g.setWorldClip(0,0,1024,640);	
		
		if(ShowCursor){
			if(cursorPos < NrShow){
				Cursor.draw(x-30,y+cursorPos*20 + 12);
			}else {
				Cursor.draw(x-30,y+(NrShow-1)*20 + 12);
			}
		}
		
	}
	
	
	public String getSelectedItem() {
		if(Items.size() > 0){
			return Items.get(cursorPos);
		}
		return "";
	}
	
	
	
	public void hideCursor() {
		ShowCursor = false;
	}
	
	public void showCursor() {
		ShowCursor = true;
	}
	
	public int getCursorPos() {
		return cursorPos;
	}
	
	public int getNrItems() {
		return Items.size();
	}
}
