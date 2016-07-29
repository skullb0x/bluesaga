package gui.list;

import graphics.BlueSagaColors;
import graphics.Font;
import graphics.ImageResource;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import utils.StringUtils;
import components.Item;
import components.Quest;
import creature.Creature;

public class ListButton {

	int ListIndex;
	
	int X;
	int Y;
	
	int offsetY = 0;
	
	int Width;
	int Height;
	
	Item myItem = null;
	Quest myQuest = null;
	Creature myCreature = null;
	
	boolean shopLink;
	boolean checkLink;
	boolean bountyLink;

	String myLabel = "";
	
	String questStatus = "";
	
	public ListButton(int x, int y, int newWidth, int newHeight){
		X = x;
		Y = y;
		Width = newWidth;
		Height = newHeight;
		shopLink = false;
		checkLink = false;
		bountyLink = false;
	}
	
	
	public void draw(Graphics g, int x, int y, int mouseX, int mouseY){
		offsetY = y - Y;
		
		if(mouseX > x && mouseX < x + Width && mouseY > y && mouseY < y + Height){
			g.setColor(new Color(255,255,255,100));
			g.fillRoundRect(x-5, y-5, Width+10, Height-5, 8);
		}

		g.setColor(new Color(255,255,255,255));
		g.setFont(Font.size12);
		if(myItem != null){
			ImageResource.getSprite("items/item"+myItem.getId()).draw(x-30, y-42);
			g.drawString("> "+myLabel, x+50, y);
		}else if(myQuest != null){
			
			if(Width < 400){
				myLabel = myQuest.getName();
		
				// ellipsis text if button too small
				myLabel = StringUtils.ellipsize(myLabel, (Width - 20) / 10);
			
				if(myQuest.getStatus() == 0){
					g.setColor(BlueSagaColors.YELLOW);
				}else if(myQuest.getStatus() == 1){
					g.setColor(BlueSagaColors.WHITE);
				}else if(myQuest.getStatus() == 2){
					g.setColor(BlueSagaColors.YELLOW);
				}else if(myQuest.getStatus() == 3){
					g.setColor(BlueSagaColors.GREEN);
				}
				
				g.drawString("> "+myLabel, x, y);
				
			}else{
				if(myQuest.getType().equals("Instructions") || myQuest.getType().equals("Story")){
					g.drawString("> "+myLabel, x, y);
				}else if(myQuest.getType().equals("Learn Class")){
					g.setColor(new Color(200,255,111));
					g.drawString("CHANGE CLASS: ", x, y);
					int statusWidth = Font.size12.getWidth("CHANGE CLASS: ");
					g.setColor(BlueSagaColors.WHITE);
					g.drawString(myLabel,x+statusWidth+10,y);
				}else{
					if(myQuest.getStatus() == 0){
						g.setColor(BlueSagaColors.YELLOW);
					}else if(myQuest.getStatus() == 1){
						g.setColor(BlueSagaColors.WHITE);
					}else if(myQuest.getStatus() == 2){
						g.setColor(BlueSagaColors.YELLOW);
					}else if(myQuest.getStatus() == 3){
						g.setColor(BlueSagaColors.GREEN);
					}
					g.drawString(questStatus.toUpperCase()+" ", x, y);
					
					int statusWidth = Font.size12.getWidth(questStatus);
					
					g.setColor(BlueSagaColors.WHITE);
					g.drawString(myLabel,x+statusWidth+10,y);
					
					String recLevel = "LVL "+myQuest.getLevel()+"+";
					g.drawString(recLevel,x + Width - Font.size12.getWidth(recLevel)-10, y);
				}
			}
		}else if(myCreature != null){
			ImageResource.getSprite("creatures/m"+myCreature.getCreatureId()).draw(x, y-5);
			g.drawString(myLabel, x + 60, y);
		}else{
			g.drawString(myLabel, x, y);
		}
	}
	
	public void setQuest(Quest newQuest){
		myQuest = newQuest;
		
		if(myQuest.getStatus() == 0){
			questStatus = "NEW QUEST:";
		}else if(myQuest.getStatus() == 1){
			questStatus = "ACCEPTED:";
		}else if(myQuest.getStatus() == 2){
			questStatus = "GET REWARD:";
		}else if(myQuest.getStatus() == 3){
			questStatus = "COMPLETED:";
		}
	}
	
	public Quest getQuest(){
		return myQuest;
	}
	
	public void setItem(Item newItem){
		myItem = newItem;
	}
	
	public Item getItem(){
		return myItem;
	}
	
	public void setShop(boolean shopValue){
		shopLink = shopValue;
	}
	
	public void setCheckIn(boolean checkInValue){
		checkLink = checkInValue;
	}
	
	public void setCreature(Creature creature){
		myCreature = creature;
	}
	
	public Creature getCreature(){
		return myCreature;
	}
	
	public boolean isShopLink(){
		return shopLink;
	}

	public boolean isCheckLink(){
		return checkLink;
	}
	
	public boolean isBountyLink() {
		return bountyLink;
	}

	public void setBountyLink(boolean bountyLink) {
		this.bountyLink = bountyLink;
	}

	
	
	public void setLabel(String newLabel){
		myLabel = newLabel;
	}
	
	public String getLabel(){
		return myLabel;
	}
	
	public boolean clicked(int mouseX, int mouseY, int moveX, int moveY){
		if(mouseX > X + moveX && mouseX < X + Width + moveX && mouseY > Y + offsetY + moveY && mouseY < Y + offsetY + Height + moveY){
			return true;
		}
		return false;
	}
	
	
	
	public void updatePos(int moveX, int moveY){
		X += moveX;
		Y += moveY;
	}
	
}
