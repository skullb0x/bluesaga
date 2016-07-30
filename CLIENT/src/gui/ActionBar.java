package gui;

import game.BlueSaga;
import graphics.BlueSagaColors;
import graphics.Font;
import gui.dragndrop.DropBox;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;

import utils.GameInfo;
import abilitysystem.Ability;
import components.Item;
import data_handlers.AbilityHandler;

public class ActionBar {

	private int X;
	private int Y;

	private DropBox[] Boxes = new DropBox[8];

	private int SelectedAction = 10;

	private boolean Visible;

	public ActionBar(int x, int y){
		X = x;
		Y = y;

		setVisible(true);

		for(int i = 0; i < 8; i++){
			Boxes[i] = new DropBox(X+i*50,Y);
		}
	}

	public void clear(){
		for(int i = 0; i < 8; i++){
			Boxes[i] = new DropBox(X+i*50,Y);
		}
	}

	public void load(String data){
		if(!data.equals("None")){
			String actionData[] = data.split(";");

			for(String action: actionData){
				String actionInfo[] = action.split(",");

				int orderNr = Integer.parseInt(actionInfo[0]);
				String actionType = actionInfo[1];
				int actionId = Integer.parseInt(actionInfo[2]);

				if(actionType.equals("Item")){
					Item newItem = new Item(actionId);
					Boxes[orderNr].setItem(newItem);
				}else if(actionType.equals("Ability")){
					Boxes[orderNr].setAbility(BlueSaga.playerCharacter.getAbilityById(actionId));
				}
			}
		}
	}

	public void update(){
		for(int i = 0; i < 8; i++){
			DropBox b = Boxes[i];
			if(b.getAbility() != null){
				boolean canUse = true;
				
				if(!b.getAbility().getEquipReq().equals("None")){
					
					String equipRequired[] = b.getAbility().getEquipReq().split(";");
					
					for(int e = 0; e < equipRequired.length; e++){
						String equipInfo[] = equipRequired[e].split(":");
						String equipType = equipInfo[0];
						
						boolean onlyClass = false;
						int classId = 0;
						if(equipInfo[1].contains("only")){
							onlyClass = true;
							equipInfo[1] = equipInfo[1].substring(4);
						}
						
						classId = Integer.parseInt(equipInfo[1]);
					
						if(equipType.equals("Weapon")){
							int weaponClassId = 0;
							if(BlueSaga.playerCharacter.MyEquipHandler.getEquipment(equipType) != null){
								weaponClassId = BlueSaga.playerCharacter.MyEquipHandler.getEquipment(equipType).getClassId();
							}
							if(classId > 0){
								// Fishing bug fixed [Arkhist]
								if(classId == 101) {
									if (BlueSaga.playerCharacter.MyEquipHandler.getEquipment(equipType) != null) {
											if(BlueSaga.playerCharacter.MyEquipHandler.getEquipment(equipType).getId() == 173
													|| BlueSaga.playerCharacter.MyEquipHandler.getEquipment(equipType).getId() == 193) {
												canUse = true;
											}
									}
									
								}else {
									int weaponBaseClassId = 0;
									if(GameInfo.classDef.get(weaponClassId) != null){
										weaponBaseClassId = GameInfo.classDef.get(weaponClassId).baseClassId;
									}
									
									if(weaponClassId == classId || (!onlyClass && weaponBaseClassId == classId)){
										canUse = true;
									}else{
										canUse = false;
									}
								}
							}
						}
						if(canUse){
							break;
						}
					}	
				}else{
				}

				b.setCanUse(canUse);
			}
		}
	}

	public void updateSoulstone(){
		for(int i = 0; i < 8; i++){
			DropBox b = Boxes[i];
			if(b.getAbility() != null){
				// SOUL STONE
				if(b.getAbility().getAbilityId() == 31){
					if(BlueSaga.logoutTimeItr > 0){
						b.setCanUse(false);
					}else{
						b.setCanUse(true);
					}
				}
			}
		}
	}
	
	public void draw(Graphics g, int MouseX, int MouseY){
		if(isVisible()){
			g.setFont(Font.size10);

			for(int i = 0; i < 8; i++){
				Boxes[i].draw(g, MouseX, MouseY,0,0);
				if(SelectedAction == i){
					g.setColor(new Color(255,255,255,100));
					g.fillRect(X + i*50, Y, 50, 50);
				}
				/*
				if(Boxes[i].getItem() != null){
					g.setColor(BP_CLIENT.COLORS.WHITE);
					g.drawString(""+BP_CLIENT.playerCharacter.countItems(Boxes[i].getItem().getId()), X+i*50 + 40, Y);
				}
				 */
				g.setColor(BlueSagaColors.BLACK);
				g.drawString((i+1)+"", X+i*50 + 23, Y + 35);
				g.setColor(BlueSagaColors.WHITE);
				g.drawString((i+1)+"", X+i*50 + 24, Y + 34);
			}
		}
	}


	public int keyLogic(Input INPUT){

		if(INPUT.isKeyPressed(Input.KEY_1)){
			SelectedAction = 0;	
		}
		if(INPUT.isKeyPressed(Input.KEY_2)){
			SelectedAction = 1;	
		}
		if(INPUT.isKeyPressed(Input.KEY_3)){
			SelectedAction = 2;	
		}
		if(INPUT.isKeyPressed(Input.KEY_4)){
			SelectedAction = 3;	
		}
		if(INPUT.isKeyPressed(Input.KEY_5)){
			SelectedAction = 4;	
		}
		if(INPUT.isKeyPressed(Input.KEY_6)){
			SelectedAction = 5;	
		}
		if(INPUT.isKeyPressed(Input.KEY_7)){
			SelectedAction = 6;	
		}
		if(INPUT.isKeyPressed(Input.KEY_8)){
			SelectedAction = 7;	
		}

		if(SelectedAction < 8){
			DropBox b = Boxes[SelectedAction];

			if(b.getAbility() != null){
				if(b.getCanUse()){
					// SELECT ABILITY
					if(b.getAbility().isTargetSelf() || b.getAbility().isInstant()){
						BlueSaga.client.sendMessage("use_ability", BlueSaga.playerCharacter.getX()+","+BlueSaga.playerCharacter.getY()+","+b.getAbility().getAbilityId());
						SelectedAction = 10;
					}else if(!Gui.USE_ABILITY){
						Gui.USE_ABILITY = true;
						// Show AoE
						Gui.Mouse.setAoE(b.getAbility().getAoE());
						Gui.Mouse.setType("Ability");
					}
				}
			}else if(b.getItem() != null){
				Gui.cancelUseAbility();
				if(!BlueSaga.actionServerWait && b.isReady()){
					BlueSaga.client.sendMessage("useitem", "actionbar;"+b.getItem().getId());
					BlueSaga.actionServerWait = true;
					SelectedAction = 10;
				}
			}else{
				Gui.cancelUseAbility();
				SelectedAction = 10;
			}
		}
		
		return SelectedAction;
	}

	
	public boolean isClicked(int mouseX, int mouseY){
		boolean clicked = false;
		if(mouseX > X && mouseX < X + 50*8 && mouseY > Y && mouseY < Y + 50){
			clicked = true;
		}
		return clicked;
	}
	
	public boolean leftMouseClick(int mouseX, int mouseY){

		boolean clickedWindow = false;

		if(Visible){
			int i = 0;

			if(!BlueSaga.actionServerWait){
				if(Gui.MouseItem.isEmpty()){
					// IF MOUSE HAS ITEM
					
					// CHECK WHICH BOX IS CLICKED
					for(DropBox b: Boxes){
						if(b.isSelected(mouseX, mouseY, 0, 0)){
							clickedWindow = true;

							if(b.getAbility() != null){
								BlueSaga.actionServerWait = true;

								Gui.MouseItem.clear();
								Gui.MouseItem.setAbility(b.getAbility());
								BlueSaga.client.sendMessage("remove_actionbar", ""+i);
							}else if(b.getItem() != null){
								BlueSaga.actionServerWait = true;
								Gui.MouseItem.clear();
								BlueSaga.client.sendMessage("remove_actionbar", ""+i);
							}
							break;
						}
						i++;
					}
				}else{
					// ADD ITEM TO ACTIONBAR
					// CHECK WHICH BOX IS CLICKED
					for(DropBox b: Boxes){
						if(b.isSelected(mouseX, mouseY, 0, 0)){
							clickedWindow = true;
							if(Gui.MouseItem.getAbility() != null){
								// ADD ABILTY TO ACTIONBAR

								BlueSaga.actionServerWait = true;

								// ActionType; ActionId; PosX
								BlueSaga.client.sendMessage("add_actionbar", "Ability;"+Gui.MouseItem.getAbility().getAbilityId()+";"+i);

							}else if(Gui.MouseItem.getItem() != null){
								BlueSaga.actionServerWait = true;
								// ActionType; ActionId; PosX
								BlueSaga.client.sendMessage("add_actionbar", "Item;"+Gui.MouseItem.getItem().getId()+";"+i);
							}
							break;
						}
						i++;
					}
				}
			}	
		}
		return clickedWindow;
	}

	public boolean rightMouseClick(int mouseX, int mouseY){
		boolean clickedWindow = false;

		int i = 0;

		// CHECK WHICH BOX IS CLICKED
		for(DropBox b: Boxes){
			if(b.isSelected(mouseX, mouseY, 0, 0)){
				clickedWindow = true;

				if(Gui.MouseItem.isEmpty()){
					if(b.getAbility() != null){
						if(AbilityHandler.readyToUseAbility()){
							if(b.getCanUse()){
								// SELECT ABILITY
								if(b.getAbility().isTargetSelf() || b.getAbility().isInstant()){
									BlueSaga.client.sendMessage("use_ability", BlueSaga.playerCharacter.getX()+","+BlueSaga.playerCharacter.getY()+","+b.getAbility().getAbilityId());
								}else{
									Gui.USE_ABILITY = true;
									Gui.Mouse.setType("Ability");
									SelectedAction = i;
								}
							}
						}
					}else if(b.getItem() != null && !BlueSaga.actionServerWait && b.isReady()){
						BlueSaga.client.sendMessage("useitem", "actionbar;"+b.getItem().getId());
						BlueSaga.actionServerWait = true;
					}
				}
			}
			i++;
		}

		if(!clickedWindow && Gui.MouseItem.getAbility() != null){
			Gui.MouseItem.clear();
		}

		return clickedWindow;
	}

	public void startUseTimer(String type){
		for(DropBox b: Boxes){
			if(b.getItem() != null){
				if(type.equals("HEALTH")){
					if(b.getItem().getId() == 3 || b.getItem().getId() == 4){
						b.startUseTimer();
					}
				}else if(type.equals("MANA")){
					if(b.getItem().getId() == 5 || b.getItem().getId() == 6){
						b.startUseTimer();
					}
				}
			}

		}
	}

	public boolean isVisible() {
		return Visible;
	}

	public void setVisible(boolean visible) {
		Visible = visible;
	}


	public DropBox getBox(int index){
		return Boxes[index];
	}

	public Ability getSelectedAbility(){
		if(SelectedAction < 9){
			return Boxes[SelectedAction].getAbility();
		}
		return null;
	}

	public void cancelSelection(){
		SelectedAction = 10;
	}
}
