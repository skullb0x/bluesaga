package gui.windows;

import java.util.HashMap;
import java.util.Map.Entry;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;

import utils.LanguageUtils;
import components.Item;
import components.Stats;
import game.BlueSaga;
import graphics.BlueSagaColors;
import graphics.Font;
import graphics.ImageResource;
import gui.Gui;
import gui.dragndrop.DropBox;

public class StatusWindow extends Window {

	
	private Image StatusLabel;

	
	private Image meter_bg;
	private Image xp_meter;
	
	private Image heart_big;
	private Image star_big;
	
	private HashMap<String, DropBox> Equipment = new HashMap<String, DropBox>();
	
	private Stats EquipStatsInfo;
	private HashMap<String, Color> EquipStatsInfoColor;

	private ItemInfoBox SelectedInfoBox;
	private Item selectedItemForInfo;
	
	
	public StatusWindow(int x, int y, int width, int height) {
		super("StatusW", x, y, width, height, true);
		
		StatusLabel = ImageResource.getSprite("gui/menu/status_label").getImage();

		meter_bg = ImageResource.getSprite("gui/world/meter_bg").getImage();
		xp_meter = ImageResource.getSprite("gui/world/xp_small_meter").getImage();
		heart_big = ImageResource.getSprite("gui/world/heart_big").getImage();
		star_big = ImageResource.getSprite("gui/world/star_big").getImage();
		
		int equipX = 280;
		int equipY = 260;
		
		// EQUIPMENTS
		DropBox HeadBox = new DropBox(equipX,equipY);
		HeadBox.setType("Head");
		Equipment.put("Head", HeadBox);
		
		DropBox WeaponBox = new DropBox(equipX-50,equipY+50);
		WeaponBox.setType("Weapon");
		Equipment.put("Weapon", WeaponBox);
		
		DropBox OffHandBox = new DropBox(equipX+50,equipY+50);
		OffHandBox.setType("OffHand");
		Equipment.put("OffHand", OffHandBox);
		
		DropBox AmuletBox = new DropBox(equipX,equipY+100);
		AmuletBox.setType("Amulet");
		Equipment.put("Amulet", AmuletBox);
		
		DropBox ArtifactBox = new DropBox(equipX,equipY+50);
		ArtifactBox.setType("Artifact");
		Equipment.put("Artifact", ArtifactBox);
		
		EquipStatsInfo = new Stats();
		EquipStatsInfoColor = new HashMap<String, Color>();
		EquipStatsInfo.reset();
		
		SelectedInfoBox = new ItemInfoBox(0,0,1,1);
		selectedItemForInfo = null;
		
	}
	
	public void load(String statusInfo){
		
		String equipInfo[] = statusInfo.split(",");
		
		clearEquip();
		
		
		int headEquipId = Integer.parseInt(equipInfo[0]);
		int headEquipUserItemId = Integer.parseInt(equipInfo[1]);
		
		if(headEquipId > 0){
			Item HeadItem = new Item(headEquipId);
			HeadItem.setUserItemId(headEquipUserItemId);
			HeadItem.setType("Head");
			setEquip(HeadItem);
		}
		
		int weaponEquipId = Integer.parseInt(equipInfo[2]);
		int weaponEquipUserItemId = Integer.parseInt(equipInfo[3]);
		
		if(weaponEquipId > 0){
			Item WeaponItem = new Item(weaponEquipId);
			WeaponItem.setUserItemId(weaponEquipUserItemId);
			WeaponItem.setType("Weapon");
			setEquip(WeaponItem);
		}
		
		int offHandEquipId = Integer.parseInt(equipInfo[4]);
		int offHandEquipUserItemId = Integer.parseInt(equipInfo[5]);
		
		if(offHandEquipId > 0){
			Item OffHandItem = new Item(offHandEquipId);
			OffHandItem.setUserItemId(offHandEquipUserItemId);
			OffHandItem.setType("OffHand");
			setEquip(OffHandItem);
		}
		
		int amuletEquipId = Integer.parseInt(equipInfo[6]);
		int amuletEquipUserItemId = Integer.parseInt(equipInfo[7]);
		
		if(amuletEquipId > 0){
			Item AmuletItem = new Item(amuletEquipId);
			AmuletItem.setUserItemId(amuletEquipUserItemId);
			AmuletItem.setType("Amulet");
			setEquip(AmuletItem);
		}
		
		int artifactEquipId = Integer.parseInt(equipInfo[8]);
		int artifactEquipUserItemId = Integer.parseInt(equipInfo[9]);
		
		if(artifactEquipId > 0){
			Item ArtifactItem = new Item(artifactEquipId);
			ArtifactItem.setUserItemId(artifactEquipUserItemId);
			ArtifactItem.setType("Artifact");
			setEquip(ArtifactItem);
		}
	}

	public void setEquip(Item newEquip){
		for(Entry<String, DropBox> e: Equipment.entrySet()){
			if(e.getValue().getType().equals(newEquip.getType())){
				e.getValue().setItem(newEquip);
			}
		}
	}
	
	public void removeEquip(String equipType){
		for(Entry<String, DropBox> e: Equipment.entrySet()){
			if(e.getValue().getType().equals(equipType)){
				e.getValue().setItem(null);
			}
		}
	}

	public void clearEquip(){
		for(Entry<String, DropBox> e: Equipment.entrySet()){
			e.getValue().setItem(null);
		}
	}
	
	public void updateEquipInfo() {
		EquipStatsInfo.reset();
		EquipStatsInfoColor.clear();
	}
	
	
	@Override
	public void draw(GameContainer app, Graphics g, int mouseX, int mouseY){
		if(isVisible()){
			super.draw(app, g, mouseX, mouseY);
	
			int startX = X+20 + moveX;
			int startY = Y+20 + moveY;
			
			if(isFullyOpened()){
				int textX = startX;
				int textY = startY;
				
				StatusLabel.draw(textX, textY);
		
				// GENERAL INFO
				g.setFont(Font.size12);
				g.setColor(BlueSagaColors.YELLOW);
		
				// NAME
				g.drawString(" -  "+BlueSaga.playerCharacter.getName().toUpperCase()+" (lvl "+BlueSaga.playerCharacter.getLevel()+")", textX + 150, textY + 2);
				
				g.setColor(new Color(0, 0, 0, 255));
				textY += 40;
				
				
				// HEALTH
				/*
				meter_bg.draw(textX+10,textY);
				g.setWorldClip(textX+15,textY,BP_CLIENT.playerCharacter.getHealthBarWidth(107),20);
				hp_meter.draw(textX+15,textY+5);
				g.clearWorldClip();
				*/
				heart_big.draw(textX,textY);
				g.drawString(BlueSaga.playerCharacter.getHealthAsString(), textX + 30, textY + 5);
	
				textY += 30;
				
				// MANA
				/*
				meter_bg.draw(textX,textY);
				g.setWorldClip(textX,textY,BP_CLIENT.playerCharacter.getManaBarWidth(107),20);
				mana_meter.draw(textX,textY);
				g.clearWorldClip();
				*/
				star_big.draw(textX,textY);
				g.drawString(BlueSaga.playerCharacter.getManaAsString(), textX + 30, textY + 5);
				
				
				textY += 40;
				
				// XPBAR
				meter_bg.draw(textX, textY);
				g.setWorldClip(textX+5,textY,BlueSaga.playerCharacter.getXPBarWidth(107),25);
				xp_meter.draw(textX+5,textY+4);
				g.clearWorldClip();
							
				textY += 25;
				
				// XP
				g.setFont(Font.size8);
				int xpLeft = Math.round(BlueSaga.playerCharacter.getNextXP() - BlueSaga.playerCharacter.getXP());
				g.drawString(xpLeft + " XP to next level", textX + 5, textY);
				
				
				
				// PRIMARY STATS
				textX = startX + 170;
				textY = startY + 45;
				
				g.setColor(new Color(0,0,0));
				g.setFont(Font.size12);
				g.drawString("STR:", textX, textY);
				g.setFont(Font.size12bold);
				setBonusStatColor(g,"STRENGTH");
				g.drawString(""+BlueSaga.playerCharacter.getTotalStat("STRENGTH"), textX+50 , textY);
		
				g.setColor(new Color(0,0,0));
				g.setFont(Font.size12);
				g.drawString("ACC:", textX + 100, textY);
				g.setFont(Font.size12bold);
				setBonusStatColor(g,"ACCURACY");
				g.drawString(""+BlueSaga.playerCharacter.getTotalStat("ACCURACY"), textX+150 , textY);
		
				textY += 22;
				
				g.setColor(new Color(0,0,0));
				g.setFont(Font.size12);
				g.drawString("INT:", textX, textY);
				g.setFont(Font.size12bold);
				setBonusStatColor(g,"INTELLIGENCE");
				g.drawString(""+BlueSaga.playerCharacter.getTotalStat("INTELLIGENCE"), textX+50 , textY);
				
				
				g.setColor(new Color(0,0,0));
				g.setFont(Font.size12);
				g.drawString("EVA:", textX + 100, textY);
				g.setFont(Font.size12bold);
				setBonusStatColor(g,"EVASION");
				g.drawString(""+BlueSaga.playerCharacter.getTotalStat("EVASION"), textX+150 , textY);
				
				textY += 22;
				
				g.setColor(new Color(0,0,0));
				g.setFont(Font.size12);
				g.drawString("AGI:", textX, textY);
				g.setFont(Font.size12bold);
				setBonusStatColor(g,"AGILITY");
				g.drawString(""+BlueSaga.playerCharacter.getTotalStat("AGILITY"), textX+50 , textY);
				
				g.setColor(new Color(0,0,0));
				g.setFont(Font.size12);
				g.drawString("CRIT:", textX + 100, textY);
				g.setFont(Font.size12bold);
				setBonusStatColor(g,"CRITICAL_HIT");
				g.drawString((float) BlueSaga.playerCharacter.getTotalStat("CRITICAL_HIT")+" %", textX+150 , textY);
				
				
				textY += 22;
				
				g.setColor(new Color(0,0,0));
				g.setFont(Font.size12);
				g.drawString("SPD (ATK):", textX, textY);
				g.setFont(Font.size12bold);
				setBonusStatColor(g,"SPEED");
				g.drawString(""+BlueSaga.playerCharacter.getTotalStat("SPEED")+" ("+ BlueSaga.playerCharacter.MyEquipHandler.getWeaponSpeed() +")", textX+100 , textY);
	
				
				// Draw Base Class
				
				textY = startY + 160;
				
				Color baseClassColor = new Color(255,234,116);
				Color baseClassTextColor = new Color(20,20,20);
				String baseLabel = "No Base Class";
				int baseClassXpWidth = 0;
				
				if(BlueSaga.playerCharacter.getBaseClass() != null){
					baseClassColor = BlueSagaColors.getColorFromString(BlueSaga.playerCharacter.getBaseClass().bgColor);;
					baseClassTextColor = BlueSagaColors.getColorFromString(BlueSaga.playerCharacter.getBaseClass().textColor);;
				
					baseLabel = BlueSaga.playerCharacter.getBaseClass().name+" LVL "+BlueSaga.playerCharacter.getBaseClass().level;
					baseClassXpWidth = (int) (((float) BlueSaga.playerCharacter.getBaseClass().getXp() / (float) BlueSaga.playerCharacter.getBaseClass().nextXP) * 160);
				}
				
				ImageResource.getSprite("gui/menu/class_bg").draw(startX, textY, baseClassColor);
				
				// Draw class label
				g.setFont(Font.size10);
				int labelX = startX + 80 - Font.size10.getWidth(baseLabel) / 2;
				g.setColor(baseClassTextColor);
				g.drawString(baseLabel, labelX, textY + 15);
				
				// Draw xp progress on class
				g.setWorldClip(startX,textY+moveY,baseClassXpWidth,40);
				ImageResource.getSprite("gui/menu/class_bg").draw(startX, textY, new Color(255,255,255,100));
				g.clearWorldClip();
				
				// Draw Primary Class
				textY += 45;
				
				Color primaryClassColor = new Color(255,234,116);
				Color primaryClassTextColor = new Color(20,20,20);
				String primaryLabel = LanguageUtils.getString("ui.classes.no_primary_class");
				int primaryClassXpWidth = 0;
				
				if(BlueSaga.playerCharacter.getPrimaryClass() != null){
					primaryClassColor = BlueSagaColors.getColorFromString(BlueSaga.playerCharacter.getPrimaryClass().bgColor);;
					primaryClassTextColor = BlueSagaColors.getColorFromString(BlueSaga.playerCharacter.getPrimaryClass().textColor);;
				
					primaryLabel = BlueSaga.playerCharacter.getPrimaryClass().name+" LVL "+BlueSaga.playerCharacter.getPrimaryClass().level;
					primaryClassXpWidth = (int) (((float) BlueSaga.playerCharacter.getPrimaryClass().getXp() / (float) BlueSaga.playerCharacter.getPrimaryClass().nextXP) * 160);
				}
				
				ImageResource.getSprite("gui/menu/class_bg").draw(startX, textY, primaryClassColor);
				
				// Draw class label
				g.setFont(Font.size10);
				labelX = startX + 80 - Font.size10.getWidth(primaryLabel) / 2;
				g.setColor(primaryClassTextColor);
				g.drawString(primaryLabel, labelX, textY + 15);
				
				// Draw xp progress on class
				g.setWorldClip(startX,textY+moveY,primaryClassXpWidth,40);
				ImageResource.getSprite("gui/menu/class_bg").draw(startX, textY, new Color(255,255,255,100));
				g.clearWorldClip();
				
				// Draw Secondary Class
				
				textY += 45;
				
				Color secondaryClassColor = new Color(255,234,116);
				Color secondaryClassTextColor = new Color(20,20,20);
				String secondaryLabel = LanguageUtils.getString("ui.classes.no_secondary_class");
				int secondaryClassXpWidth = 0;
				
				if(BlueSaga.playerCharacter.getSecondaryClass() != null){
					secondaryClassColor = BlueSagaColors.getColorFromString(BlueSaga.playerCharacter.getSecondaryClass().bgColor);
					secondaryClassTextColor = BlueSagaColors.getColorFromString(BlueSaga.playerCharacter.getSecondaryClass().textColor);
					secondaryLabel = BlueSaga.playerCharacter.getSecondaryClass().name+" LVL "+BlueSaga.playerCharacter.getSecondaryClass().level;
					secondaryClassXpWidth = (int) (((float) BlueSaga.playerCharacter.getSecondaryClass().getXp() / (float) BlueSaga.playerCharacter.getSecondaryClass().nextXP) * 160);
					
				}
				
				ImageResource.getSprite("gui/menu/class_bg").draw(startX, textY,secondaryClassColor);
				
				// Draw secondary class label
				labelX = Font.size10.getWidth(secondaryLabel) / 2;
				g.setColor(secondaryClassTextColor);
				g.drawString(secondaryLabel, startX + 80 - labelX, textY + 15);
				
				// Draw xp progress on class
				g.setWorldClip(startX,textY+moveY,secondaryClassXpWidth,40);
				ImageResource.getSprite("gui/menu/class_bg").draw(startX, textY, new Color(255,255,255,100));
				g.clearWorldClip();
				
				// DRAW DEF STATS
				g.setFont(Font.size10);
				g.setColor(BlueSagaColors.BLACK);
				
				textX = startX;
				textY = startY + 325;
				int spacing = 50;
				
				g.setColor(BlueSagaColors.RED);
				g.fillRect(textX-5, textY-5, 60, 26);
				
				g.setFont(Font.size12);
				setBonusStatColor(g,"ARMOR");	
				ImageResource.getSprite("gui/menu/icon_armor").draw(textX, textY);
				if(BlueSaga.playerCharacter.getTotalStat("ARMOR") > 999){
					g.drawString("MAX", textX+22, textY-1);
				}else{
					g.drawString(""+(BlueSaga.playerCharacter.getTotalStat("ARMOR")), textX+22, textY+1);
				}
				
				textX += spacing;
				
				g.setColor(BlueSagaColors.RED.brighter(0.1f));
				g.fillRect(textX-5, textY-5, 60, 26);
				
				ImageResource.getSprite("gui/menu/icon_fire").draw(textX, textY);
				setBonusStatColor(g,"FIRE_DEF");
				g.drawString(""	+ (BlueSaga.playerCharacter.getTotalStat("FIRE_DEF")), textX+22, textY+1);
				
				textX += spacing;
	
				g.setColor(BlueSagaColors.RED);
				g.fillRect(textX-5, textY-5, 60, 26);
				
				ImageResource.getSprite("gui/menu/icon_cold").draw(textX, textY);
				setBonusStatColor(g,"COLD_DEF");
				g.drawString(""	+ (BlueSaga.playerCharacter.getTotalStat("COLD_DEF")), textX+22, textY+1);
				
				textX += spacing;
	
				g.setColor(BlueSagaColors.RED.brighter(0.1f));
				g.fillRect(textX-5, textY-5, 60, 26);
			
				ImageResource.getSprite("gui/menu/icon_shock").draw(textX, textY);
				setBonusStatColor(g,"SHOCK_DEF");
				g.drawString(""	+ (BlueSaga.playerCharacter.getTotalStat("SHOCK_DEF")), textX+22, textY+1);
				
				textX += spacing;
	
				g.setColor(BlueSagaColors.RED);
				g.fillRect(textX-5, textY-5, 60, 26);
				
				ImageResource.getSprite("gui/menu/icon_chems").draw(textX, textY);
				setBonusStatColor(g,"CHEMS_DEF");
				g.drawString(""	+ (BlueSaga.playerCharacter.getTotalStat("CHEMS_DEF")), textX+22, textY+1);
	
				textX += spacing;
	
				g.setColor(BlueSagaColors.RED.brighter(0.1f));
				g.fillRect(textX-5, textY-5, 60, 26);
			
				ImageResource.getSprite("gui/menu/icon_mind").draw(textX, textY);
				setBonusStatColor(g,"MIND_DEF");
				g.drawString(""	+ (BlueSaga.playerCharacter.getTotalStat("MIND_DEF")), textX+22, textY+1);
				
				textX += spacing;

				g.setColor(BlueSagaColors.RED);
				g.fillRect(textX-5, textY-5, 60, 26);
				
				ImageResource.getSprite("gui/menu/icon_magic").draw(textX, textY);
				setBonusStatColor(g,"MAGIC_DEF");
				g.drawString(""	+ (BlueSaga.playerCharacter.getTotalStat("MAGIC_DEF")), textX+22, textY+1);
	
				
				// EQUIPMENT SLOTS
				drawEquipment(app, g, mouseX, mouseY);
				
				
			}
		}
	}
	
	public void drawEquipment(GameContainer app, Graphics g, int mouseX, int mouseY){
		
		boolean mouseOnItem = false;
		
		for(Entry<String, DropBox> e: Equipment.entrySet()){
			
			e.getValue().draw(g, mouseX, mouseY, moveX, moveY);
			
	        if(e.getValue().isSelected(mouseX, mouseY, moveX, moveY)){
		        if(e.getValue().getItem() != null){
		        	mouseOnItem = true;
		        	
		        	boolean showInfo = false;
					if(selectedItemForInfo != null){
						if(selectedItemForInfo.getUserItemId() != e.getValue().getItem().getUserItemId()){
							showInfo = true;
						}
					}else{
						showInfo = true;
					}
					if(showInfo){
						selectedItemForInfo = e.getValue().getItem();
						int infoBoxX = e.getValue().getX() + 50; 
						int infoBoxY = e.getValue().getY();
								
						
						SelectedInfoBox = new ItemInfoBox(infoBoxX, infoBoxY,1,1);
						BlueSaga.client.sendMessage("item_info", "equip;"+selectedItemForInfo.getUserItemId());
					}
				}else{
					selectedItemForInfo = null;
					SelectedInfoBox.close();
				}
	        }
		}
		
		if(!mouseOnItem){
			selectedItemForInfo = null;
			SelectedInfoBox.close();
		}
		
		SelectedInfoBox.draw(app, g, 0, 0);
	}
	
	public void showInfoBox(String info){
		SelectedInfoBox.load(info);
	}
	
	@Override
	public void keyLogic(Input INPUT){
		if(INPUT.isKeyPressed(Input.KEY_C) && !Gui.Chat_Window.isActive()){
			
			toggle();
			
			if(isOpen()){
				
				BlueSaga.client.sendMessage("statuswindow", "info");
			}
		}
	}
	
	@Override
	public void leftMouseClick(Input INPUT){
		super.leftMouseClick(INPUT);
		
		int mouseX = INPUT.getAbsoluteMouseX();
		int mouseY = INPUT.getAbsoluteMouseY();
		
		
		// CHECK WHICH BOX IS CLICKED
		
		if(isVisible() && !BlueSaga.actionServerWait){
			for(Entry<String, DropBox> e: Equipment.entrySet()){
				if(checkBoxClick(e.getValue(), mouseX, mouseY)){
					break;
				}
			}
		}
	}
	
	public void rightMouseClick(int mouseX, int mouseY){
		/*
		// CHECK WHICH BOX IS CLICKED
		
		if(isVisible() && !BlueSaga.actionServerWait){
			for(Entry<String, DropBox> e: Equipment.entrySet()){
				if(checkBoxClick(e.getValue(), mouseX, mouseY)){
					break;
				}
			}
		}
		*/
	}
	

	public boolean unequipClick(DropBox box, int mouseX, int mouseY){
		boolean boxClick = false;
		if(box.isSelected(mouseX, mouseY, moveX, moveY)){
			if(!box.isEmpty()){
				// PICK UP ITEM
				BlueSaga.actionServerWait = true;
				BlueSaga.client.sendMessage("unequipToInv",box.getType());
			}
			boxClick = true;
		}
		return boxClick;
	}
	
	
	public boolean checkBoxClick(DropBox box, int mouseX, int mouseY){
		boolean boxClick = false;
		if(box.isSelected(mouseX, mouseY, moveX, moveY)){
			if(Gui.MouseItem.getItem() != null){
				//CHECK IF ITEM IS OF RIGHT TYPE
				if(Gui.MouseItem.getItem().getType().equals(box.getType())){
					BlueSaga.actionServerWait = true;
				
					// EQUIP ITEM
					BlueSaga.client.sendMessage("equip",Gui.MouseItem.getItem().getId()+"");
				
				}else{
					Gui.addMessage("#messages.inventory.cant_equip_slot", BlueSagaColors.RED);
				}
			}else if(!box.isEmpty()){
				// PICK UP ITEM
				
				BlueSaga.actionServerWait = true;
				
				BlueSaga.client.sendMessage("unequip",box.getType());
				
			}
			boxClick = true;
		}
		return boxClick;
	}
	

	@Override
	public void stopMove(){
		if(moveWithMouse){
			for(Entry<String, DropBox> e: Equipment.entrySet()){
		        e.getValue().updatePos(moveX,moveY);
			}
		}
		super.stopMove();
			
	}
	
	public void setBonusStatColor(Graphics g, String StatType){
		if(BlueSaga.playerCharacter.getBonusStat(StatType) > 0){
			g.setColor(BlueSagaColors.WHITE);
		}else if(BlueSaga.playerCharacter.getBonusStat(StatType) < 0){
			g.setColor(BlueSagaColors.RED);
		}else {
			g.setColor(BlueSagaColors.BLACK);
		}
	}
	
	
}
