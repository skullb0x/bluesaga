package gui.list;

import java.util.Vector;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import components.Item;
import components.Quest;
import creature.Creature;

public class GuiList {
	
	private int cursorPos;
	
	private Vector<ListButton> listButtons = new Vector<ListButton>();
	
	private int X;
	private int Y;
	
	private int Width;
	private int buttonHeight = 30;
	
	
	public GuiList(int x, int y, int width, int height){
		X = x;
		Y = y;
		
		Width = width;
		cursorPos = 0;
		
		listButtons.clear();
	}
	
	public void reset(){
		cursorPos = 0;
		listButtons.clear();
	}
	
	public void setButtonHeight(int buttonHeight){
		this.buttonHeight = buttonHeight;
	}
	
	
	public void addItem(Item newItem, String label){
		ListButton newButton = new ListButton(X, Y + listButtons.size()*buttonHeight, Width, buttonHeight);
		newButton.setItem(newItem);
		newButton.setLabel(label);
		listButtons.add(newButton);
	}
	
	public void addQuest(Quest newQuest, String label){
		ListButton newButton = new ListButton(X, Y + listButtons.size()*buttonHeight, Width, buttonHeight);
		newButton.setQuest(newQuest);
		newButton.setLabel(label);
		listButtons.add(newButton);
	}
	
	public void addShop(String label){
		ListButton newButton = new ListButton(X, Y + listButtons.size()*buttonHeight, Width, buttonHeight);
		newButton.setLabel(label);
		newButton.setShop(true);
		listButtons.add(newButton);
	}

	public void addCheckIn(String label){
		ListButton newButton = new ListButton(X, Y + listButtons.size()*buttonHeight, Width, buttonHeight);
		newButton.setLabel(label);
		newButton.setCheckIn(true);
		listButtons.add(newButton);
	}
	
	public void addBounty(String label){
		ListButton newButton = new ListButton(X, Y + listButtons.size()*buttonHeight, Width, buttonHeight);
		newButton.setLabel(label);
		newButton.setBountyLink(true);
		listButtons.add(newButton);
	}	
	
	public void addButton(String label){
		ListButton newButton = new ListButton(X, Y + listButtons.size()*buttonHeight, Width, buttonHeight);
		newButton.setLabel(label);
		listButtons.add(newButton);
	}	
	
	public void addCreature(Creature newCreature, String label){
		ListButton newButton = new ListButton(X, Y + listButtons.size()*buttonHeight, Width, buttonHeight);
		newButton.setCreature(newCreature);
		newButton.setLabel(label);
		listButtons.add(newButton);
	}
	
	

	public int select(int mouseX, int mouseY, int moveX, int moveY){
		int selectedIndex = 9999;
		
		for(int i = 0; i < listButtons.size(); i++){
			if(listButtons.get(i).clicked(mouseX, mouseY, moveX, moveY)){
				selectedIndex = i;
				break;
			}
		}
		
		return selectedIndex;
	}
	
	
	public void draw(Graphics g, int mouseX, int mouseY, int moveX, int moveY){
		
	
		
		//g.setColor(new Color(108,33,33,255));
		//g.fillRoundRect(x, y, Width, Height, 10);
		
		// SHOW INVENTORY ITEMS OF RIGHT TYPE
		g.setColor(new Color(255,255,255,255));
		
		for(int i = 0; i < listButtons.size(); i++){
			ListButton b = listButtons.get(i);
			
			int buttonX = X;
			int buttonY = Y + i*buttonHeight;
			
			b.draw(g, buttonX + moveX, buttonY + moveY, mouseX, mouseY);
		}
		
	}
	
	
	public Quest getQuestWithId(int questId){
		for(ListButton b: listButtons){
			if(b.getQuest() != null){
				if(b.getQuest().getId() == questId){
					return b.getQuest();
				}
			}
		}
		return null;
	}
	
	public ListButton getListItem(int index) {
		return listButtons.get(index);
	}
	
	public void hideCursor() {
	}
	
	public void showCursor() {
	}
	
	public int getCursorPos() {
		return cursorPos;
	}
	
	public int getNrListItems() {
		return listButtons.size();
	}
	
	public void setX(int newValue){
		X = newValue;
	}
	
	public void setY(int newValue){
		Y = newValue;
	}
	
	public void updatePos(int moveX, int moveY){
		for(ListButton b: listButtons){
			b.updatePos(moveX,moveY);
		}
		
		X += moveX;
		Y += moveY;
	}
	
}
