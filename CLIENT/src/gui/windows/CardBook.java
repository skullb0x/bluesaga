package gui.windows;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;

import utils.LanguageUtils;
import game.BlueSaga;
import graphics.BlueSagaColors;
import graphics.Font;
import gui.Gui;
import gui.dragndrop.DropBox;
import components.Item;

public class CardBook extends Window {

	private DropBox Boxes[][];
	private static int sizeWidth = 4;
	private static int sizeHeight = 5;
	
	private ItemInfoBox SelectedInfoBox;
	private Item selectedItemForInfo;
	
	public CardBook(int x, int y) {
		super("Cardbook", x, y, sizeWidth*50 + 20, sizeHeight*50 + 50, true);
		
		setBorderColor(new Color(142,95,18));
		setBgColor(new Color(255,212,109));
	
		Boxes = new DropBox[sizeHeight][sizeWidth];
		
		int cardNumber = 1;
		
		for(int i = 0; i < sizeHeight; i++){
			for(int j = 0; j < sizeWidth; j++){
				Boxes[i][j] = new DropBox(x+j*DropBox.Size+10,y+i*DropBox.Size + 40);
				Boxes[i][j].setType("Card");
				Boxes[i][j].setCardNumber(cardNumber);
				cardNumber++;
			}
		}
		
		SelectedInfoBox = new ItemInfoBox(0,0,1,1);
		selectedItemForInfo = null;
	}
	
	public void load(String cardBookInfo){
		for(int i = 0; i < sizeHeight; i++){
			for(int j = 0; j < sizeWidth; j++){
				Boxes[i][j].clear();
			}
		}
		
		
		if(cardBookInfo.length() > 0){
			String cards[] = cardBookInfo.split(";");
			
			for(String cardInfo: cards){
				String cardId_ItemId[] = cardInfo.split(",");
				int cardId = Integer.parseInt(cardId_ItemId[0]);
				int cardItemId = Integer.parseInt(cardId_ItemId[1]);
				
				if(cardItemId > 0){
					Item newItem = new Item(cardItemId);
					int posX = (cardId-1) % sizeWidth;
					int posY = (int) Math.floor((cardId-1) / sizeWidth);
					Boxes[posY][posX].setItem(newItem);
				}
			}
		}
	}
	
	
	
	
	public void draw(GameContainer app, Graphics g, int mouseX, int mouseY){
		if(isVisible()){
			super.draw(app, g, mouseX, mouseY);
			
			if(isFullyOpened()){
				
				g.setFont(Font.size12);
				g.setColor(BlueSagaColors.BLACK);
				g.drawString(LanguageUtils.getString("ui.cardbook.name"), X + 10 + moveX, Y + 17 + moveY);
				
				boolean mouseOnItem = false;
				
				for(int i = 0; i < sizeHeight; i++){
					for(int j = 0; j < sizeWidth; j++){
						Boxes[i][j].draw(g, mouseX, mouseY, moveX, moveY);
					
						if(Boxes[i][j].isSelected(mouseX, mouseY, moveX, moveY)){
	
							if(Boxes[i][j].getItem() != null){
								mouseOnItem = true;
								
								boolean showInfo = false;
								if(selectedItemForInfo != null){
									if(selectedItemForInfo.getUserItemId() != Boxes[i][j].getItem().getUserItemId()){
										showInfo = true;
									}
								}else{
									showInfo = true;
								}
								
								if(showInfo){
									selectedItemForInfo = Boxes[i][j].getItem();
									//int nrItems = Boxes[i][j].getNumber();
									
									int infoBoxX = X + (i+1) * 50 + moveX; 
									if(infoBoxX > 800){
										infoBoxX -= 200;
									}
									
									SelectedInfoBox = new ItemInfoBox(infoBoxX, Y + j*50 + moveY,1,1);
									if(Gui.ShopWindow.isFullyOpened()){
										BlueSaga.client.sendMessage("item_info", "invshop;"+selectedItemForInfo.getUserItemId());
									}else{
										BlueSaga.client.sendMessage("item_info", "inv;"+selectedItemForInfo.getUserItemId());
									}
								}
							}else{
								selectedItemForInfo = null;
								SelectedInfoBox.close();
							}
						}
					
					}
				}
			
				if(!mouseOnItem){
					selectedItemForInfo = null;
					SelectedInfoBox.close();
				}
				SelectedInfoBox.draw(app, g, 0, 0);
				
				g.setFont(Font.size12);
				
			}
		}
	}
	
	@Override
	public void leftMouseClick(Input INPUT){
		super.leftMouseClick(INPUT);
		
		int mouseX = INPUT.getAbsoluteMouseX();
		int mouseY = INPUT.getAbsoluteMouseY();
		
		if(clickedOn(mouseX,mouseY) && !BlueSaga.actionServerWait){
			boolean clickedOnCard = false;
			
			// CHECK WHICH BOX IS CLICKED
			for(int i = 0; i < sizeHeight; i++){
				for(int j = 0; j < sizeWidth; j++){
					
					// IF BOX IS CLICKED, CHECK IF PICKUP OR DROP
					if(Boxes[i][j].isSelected(mouseX, mouseY, moveX, moveY)){
						clickedOnCard = true;
						
						if(!Gui.MouseItem.isEmpty() && Gui.MouseItem.getItem() != null && Gui.MouseItem.getItem().getType().equals("Collector Card")){
							// ADD CARD TO CARDBOOK
							// ItemId, posX, posY, pageNumber
							BlueSaga.client.sendMessage("card_place", Gui.MouseItem.getItem().getId()+","+Boxes[i][j].getCardNumber());
							Gui.MouseItem.clear();
							BlueSaga.actionServerWait = true;
						}else if(!Boxes[i][j].isEmpty() && Gui.MouseItem.isEmpty()){
							// PICK UP CARD
							Boxes[i][j].clear();
							BlueSaga.client.sendMessage("add_card_to_mouse", Boxes[i][j].getCardNumber()+"");
							BlueSaga.actionServerWait = true;
						}
						break;
					}
				}
				if(clickedOnCard){
					break;
				}
			}
		}
		
	}
	
	
	public void keyLogic(Input INPUT){
		if(INPUT.isKeyPressed(Input.KEY_O)){
			toggle();
			
			if(isOpen()){
				BlueSaga.client.sendMessage("card_book", "info");
			}
		}
	}
	
	@Override
	public void stopMove(){
		if(moveWithMouse){
			for(int i = 0; i < sizeHeight; i++){
				for(int j = 0; j < sizeWidth; j++){
					Boxes[i][j].updatePos(moveX,moveY);
				}
			}
		}
		super.stopMove();
	}
	
}