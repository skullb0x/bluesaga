package gui.windows;

import java.util.Timer;
import java.util.TimerTask;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;

import components.Item;
import game.BlueSaga;
import graphics.Font;
import graphics.ImageResource;
import gui.Gui;
import gui.dragndrop.DropBox;

public class InventoryWindow extends Window {
	
	private int inventorySize = 4;
	
	private DropBox Boxes[][];
	
	private ItemInfoBox SelectedInfoBox;
	
	private Item selectedItemForInfo;
	
	private Timer splitWindowTimer = new Timer();
	
	public InventoryWindow(int x, int y, int width, int height) {
		super("InventoryW", x, y, width, height, true);
		
		Boxes = new DropBox[inventorySize][inventorySize];
		
		for(int i = 0; i < inventorySize; i++){
			for(int j = 0; j < inventorySize; j++){
				Boxes[i][j] = new DropBox(x+i*DropBox.Size+10,y+j*DropBox.Size + 40);
				Boxes[i][j].setType("Inventory");
			}
		}
		
		SelectedInfoBox = new ItemInfoBox(0,0,1,1);
		selectedItemForInfo = null;
	}
		
	
	public void load(String inventoryInfo){
		String inventory_info[] = inventoryInfo.split("/");
		
		inventorySize = Integer.parseInt(inventory_info[0]);
		
		Boxes = new DropBox[inventorySize][inventorySize];
		
		for(int i = 0; i < inventorySize; i++){
			for(int j = 0; j < inventorySize; j++){
				Boxes[i][j] = new DropBox(X+i*DropBox.Size+10,Y+j*DropBox.Size + 40);
				Boxes[i][j].setType("Inventory");
			}
		}
		
		setWidth(inventorySize*50 + 20);
		setHeight(inventorySize*50 + 50);
		
		aniWidth = Width;
		aniHeight = Height;
		
		
		for(int i = 0; i < inventorySize; i++){
			for(int j = 0; j < inventorySize; j++){
				Boxes[i][j].clear();
			}
		}
		if(inventory_info.length > 1){
			String inventoryItems = inventory_info[1];
			
			if(!inventoryItems.equals("")){
				String invData[] = inventoryItems.split(";");
				
				for(String itemData: invData){
					String itemInfo[] = itemData.split(",");
					int useritemId = Integer.parseInt(itemInfo[0]);
					int itemId = Integer.parseInt(itemInfo[1]);
					int Nr = Integer.parseInt(itemInfo[2]);
					int posX = Integer.parseInt(itemInfo[3]);
					int posY = Integer.parseInt(itemInfo[4]);
					
					if(posX < inventorySize && posY < inventorySize){
						Item newItem = new Item(itemId);
						newItem.setUserItemId(useritemId);
						Boxes[posX][posY].setItem(newItem);
						Boxes[posX][posY].setNumber(Nr);
					}
				}
			}
		}
	}
	
	public void placeItem(Item newItem, int posX, int posY, int nrItems){
		Boxes[posX][posY].setItem(newItem);
		Boxes[posX][posY].setNumber(nrItems);
	}
	
	public Item getItem(int posX, int posY){
		return Boxes[posX][posY].getItem();
	}
	
	public void startUseTimer(String type){
		for(int i = 0; i < inventorySize; i++){
			for(int j = 0; j < inventorySize; j++){
				if(Boxes[i][j].getItem() != null){
					if(type.equals("HEALTH")){
						if(Boxes[i][j].getItem().getId() == 3 || Boxes[i][j].getItem().getId() == 4){
							Boxes[i][j].startUseTimer();
						}
					}else if(type.equals("MANA")){
						if(Boxes[i][j].getItem().getId() == 5 || Boxes[i][j].getItem().getId() == 6){
							Boxes[i][j].startUseTimer();
						}
					}
				}
			}
		}
	}
	
	
	@Override
	public void draw(GameContainer app, Graphics g, int mouseX, int mouseY){
		if(isVisible()){
			super.draw(app, g, mouseX, mouseY);
			
			if(isFullyOpened()){
				
				g.setFont(Font.size12);
				g.setColor(new Color(255,255,255,255));
				ImageResource.getSprite("gui/menu/inventory_label").draw(X + moveX, Y + 7 + moveY);
				
				boolean mouseOnItem = false;
				
				for(int i = 0; i < inventorySize; i++){
					for(int j = 0; j < inventorySize; j++){
						Boxes[i][j].draw(g, mouseX, mouseY, moveX, moveY);
						
						//if(Gui.isWindowOnTop(getDepthZ())){
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
						//}
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

	public void showInfoBox(String info){
		SelectedInfoBox.load(info);
	}
	
	
	@Override
	public void keyLogic(Input INPUT){
		if(INPUT.isKeyPressed(Input.KEY_I)){
			toggle();
			
			if(isOpen()){
				clearItems();
				BlueSaga.client.sendMessage("inventory", "info");
			}else{
				SelectedInfoBox.close();
			}
		}
	}
	
	public void clearItems(){
		for(int i = 0; i < inventorySize; i++){
			for(int j = 0; j < inventorySize; j++){
				Boxes[i][j].clear();
			}
		}
	}
	
	@Override
	public void leftMouseClick(Input INPUT){
		super.leftMouseClick(INPUT);
		
		int mouseX = INPUT.getAbsoluteMouseX();
		int mouseY = INPUT.getAbsoluteMouseY();
		
		if(clickedOn(mouseX,mouseY) && !BlueSaga.actionServerWait){
			boolean clickedOnItem = false;
			
			// CHECK WHICH BOX IS CLICKED
			for(int i = 0; i < inventorySize; i++){
				for(int j = 0; j < inventorySize; j++){
					
					// IF BOX IS CLICKED, CHECK IF PICKUP OR DROP
					if(Boxes[i][j].isSelected(mouseX, mouseY, moveX, moveY)){
						clickedOnItem = true;
						
						if((INPUT.isKeyDown(Input.KEY_LSHIFT) || INPUT.isKeyDown(Input.KEY_RSHIFT)) && Gui.MouseItem.isEmpty()){
							// ITEM SPLITTER
							if(Boxes[i][j].getItem() != null && Boxes[i][j].getNumber() > 1){
					        	Gui.ItemSplitterWindow.setInventoryPos(i+","+j);
								splitWindowTimer.schedule( new TimerTask(){
							        @Override
									public void run() {
										Gui.ItemSplitterWindow.open();
									}
							      }, 300);
							}
						}else if(!Gui.MouseItem.isEmpty() && Gui.MouseItem.getItem() != null){
						
							// ADD ITEM TO INVENTORY
							// ItemId; posX; posY
							BlueSaga.client.sendMessage("moveitem", Gui.MouseItem.getItem().getId()+";"+i+";"+j+";0");
							Gui.MouseItem.clear();
							BlueSaga.actionServerWait = true;
						}else if(!Boxes[i][j].isEmpty()){
							
							// PICK UP ITEM
							Boxes[i][j].clear();
							BlueSaga.client.sendMessage("addmouseitem", i+";"+j+";0");
							BlueSaga.actionServerWait = true;
						
						}
						break;
					}
				}
				if(clickedOnItem){
					break;
				}
			}
		}
	}
	
	
	@Override
	public void rightMouseClick(Input INPUT){
		super.rightMouseClick(INPUT);
		
		int mouseX = INPUT.getAbsoluteMouseX();
		int mouseY = INPUT.getAbsoluteMouseY();
		
		
		if(isVisible()){
		
			if(!BlueSaga.actionServerWait){
				boolean clickedBox = false;
				
				// CHECK WHICH BOX IS CLICKED
				for(int i = 0; i < inventorySize; i++){
					for(int j = 0; j < inventorySize; j++){
						
						// IF BOX IS CLICKED, CHECK IF PICKUP OR DROP
						if(Boxes[i][j].isSelected(mouseX, mouseY, moveX, moveY)){
							clickedBox = true;
							
							if(!Boxes[i][j].isEmpty()){
								if(Gui.ShopWindow.isFullyOpened() && Gui.MouseItem.isEmpty()){
									// SELL ITEM
									BlueSaga.client.sendMessage("sell", i+","+j);
									
									BlueSaga.actionServerWait = true;
								}else if(Boxes[i][j].isReady()){
									BlueSaga.client.sendMessage("useitem", i+";"+j+";"+Boxes[i][j].getItem().getId());
									BlueSaga.actionServerWait = true;
								}
							}
							
							break;
						}
					}
					if(clickedBox){
						break;
					}
				}	
			}
		}
	}
	
	@Override
	public void stopMove(){
		if(moveWithMouse){
			for(int i = 0; i < inventorySize; i++){
				for(int j = 0; j < inventorySize; j++){
					Boxes[i][j].updatePos(moveX,moveY);
				}
			}
		}
		super.stopMove();
	}
	
}
