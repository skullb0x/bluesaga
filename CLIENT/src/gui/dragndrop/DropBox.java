package gui.dragndrop;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import abilitysystem.Ability;
import graphics.Font;
import graphics.ImageResource;
import components.Item;

public class DropBox {
	
	private Item myItem;
	private Ability myAbility;
	
	private int X;
	private int Y;
	
	private int Number = 0; // Nr of items in box 
	
	private int cardNumber = 0; // Used only for cards
	
	private int useTimer = 100;
	private int useTimerItr = 0;
	
	public static int Size = 50;
	
	private String Type;
	
	private boolean canUse = false;
	
	public DropBox(int x, int y){
		X = x;
		Y = y;
		Number = 0;
		Type = "";
	}


	public void draw(Graphics g, int mouseX, int mouseY, int moveX, int moveY){
		ImageResource.getSprite("gui/menu/dropbox_bg").draw(X + moveX,Y + moveY);
		
		if(useTimerItr > 0){
			g.setColor(new Color(255,0,0,150));
			float useTimerHeight = (Size * ((float) useTimerItr/ (float) useTimer));
			g.fillRect(X + moveX, Y + moveY + (Size - useTimerHeight), Size, useTimerHeight);
			useTimerItr--;
		}
		
		if(getType().equals("Weapon") || getType().equals("OffHand") || getType().equals("Head") || getType().equals("Amulet") || getType().equals("Artifact")){
			ImageResource.getSprite("gui/menu/equip_"+getType()).draw(X+moveX - 25,Y+moveY - 25,new Color(255,255,255,50));
		}else if(getType().equals("Card")){
			ImageResource.getSprite("gui/menu/card_slot").draw(X+moveX,Y+moveY);
			g.setColor(new Color(100,100,100));
			g.setFont(Font.size8);
			int labelX = X + moveX + 25 - Math.round((Font.size8.getWidth("#"+cardNumber)/2.0f));
			g.drawString("#"+cardNumber, labelX,Y + moveY + 28);
		}
		
		if(isSelected(mouseX, mouseY, moveX, moveY)){
			g.setColor(new Color(255,255,255,50));
			g.fillRect(X + moveX, Y + moveY, Size, Size);
		}
		
		
		if(myItem != null){
			/*
			if(myItem.getGraphicsNr() > 0){
				ImageResource.getSprite("items/skins/"+myItem.getGraphicsNr()).draw(X + moveX - 25,Y + moveY -25);
			}else{
			}
			*/
			ImageResource.getSprite("items/item"+myItem.getId()).draw(X + moveX - 25,Y + moveY -25);
			if(Number > 1){
				g.setColor(new Color(255,255,255));
				g.setFont(Font.size10);
				float textWidth = Font.size10.getWidth(""+Number);
				g.drawString(""+Number, X + moveX + 45 - textWidth,Y + moveY + 35);
			}
			
		}else if(myAbility != null){
			myAbility.drawIcon(g, X + moveX, Y + moveY);
			
			if(!canUse && !getType().equals("Shop")){
				g.setColor(new Color(0,0,0,100));
				g.fillRect(X+moveX,Y+moveY,50,50);
			}
		
			if(isSelected(mouseX, mouseY, moveX, moveY)){
				g.setColor(new Color(255,255,255,50));
				g.fillRect(X + moveX, Y + moveY, Size, Size);
			}
		}
	}
	
	public boolean isSelected(int mouseX, int mouseY, int moveX, int moveY){
		boolean clicked = false;
		if(mouseX > X + moveX && mouseX < X+Size + moveX && mouseY > Y + moveY && mouseY < Y+Size + moveY){
			clicked = true;
		}
		return clicked;
	}

	public String getType() {
		return Type;
	}
	
	public String getContentType(){
		if(myItem != null){
			return "Item";
		}else if(myAbility != null){
			return "Ability";
		}
		return "None";
	}

	public void setType(String newType) {
		Type = newType;
	}

	public int getSize() {
		return Size;
	}

	public Item getItem() {
		return myItem;
	}

	public void setItem(Item newItem) {
		myItem = newItem;
		Number = 1;
	}
		
	public Ability getAbility() {
		return myAbility;
	}

	public void setAbility(Ability newAbility) {
		myAbility = newAbility;
	}
	
	public boolean isEmpty(){
		if(myItem == null && myAbility == null){
			return true;
		}
		return false;
	}

	public void clear(){
		myItem = null;
		myAbility = null;
	}
	
	public void updatePos(int moveX, int moveY){
		X += moveX;
		Y += moveY;
	}
	
	public void setPos(int newX, int newY){
		X = newX;
		Y = newY;
	}
	
	public int getX(){
		return X;
	}
	
	public int getY(){
		return Y;
	}
	
	public void setCanUse(boolean newStatus){
		canUse = newStatus;
	}
	
	public boolean getCanUse(){
		return canUse;
	}


	public int getNumber() {
		return Number;
	}


	public void setNumber(int number) {
		Number = number;
	}
	
	public void startUseTimer(){
		useTimerItr = useTimer;
	}
	
	public boolean isReady(){
		if(useTimerItr > 0){
			return false;
		}
		return true;
	}
	
	public int getCardNumber(){
		return cardNumber;
	}
	
	public void setCardNumber(int cardNumber){
		this.cardNumber = cardNumber;
	}
}
