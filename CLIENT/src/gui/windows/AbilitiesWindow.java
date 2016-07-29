package gui.windows;

import game.BlueSaga;
import graphics.ImageResource;
import gui.Gui;

import java.util.Vector;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;

import abilitysystem.Ability;

public class AbilitiesWindow extends Window {

	private Vector<AbilityButton> Buttons = new Vector<AbilityButton>();
	private AbilityButton selectedAbilityButton = null;
	private ItemInfoBox SelectedInfoBox;
	
	public AbilitiesWindow(int x, int y, int width, int height) {
		super("AbiltiesW", x, y, width, height, true);
		// TODO Auto-generated constructor stub
		
		SelectedInfoBox = new ItemInfoBox(0,0,1,1);
		selectedAbilityButton = null;
		
	}
	

	public void load(){
		Buttons.clear();
		
		int x = 20;
		int y = 70;
		
		for(Ability a: BlueSaga.playerCharacter.getAbilities()){
			AbilityButton b = new AbilityButton("", x, y, 50, 50, this);
			b.setAbility(a);
			Buttons.add(b);
			y += 60;
			if(y > 350){
				y = 70;
				x += 60;
			}
		}
	}
	
	
	@Override
	public void draw(GameContainer app, Graphics g, int mouseX, int mouseY){
		if(isVisible()){
			super.draw(app, g, mouseX, mouseY);
			
			if(isFullyOpened()){
				boolean showInfo = false;
				ImageResource.getSprite("gui/menu/abilities_label").draw(X + 8 + moveX, Y + 14 + moveY);
				
				boolean abilitySelected = false;
				for(AbilityButton b: Buttons){
					b.draw(g, mouseX, mouseY);
					if(b.isClicked(mouseX, mouseY)){
						abilitySelected = true;
						if(selectedAbilityButton != null){
							if(selectedAbilityButton.getAbility().getAbilityId() != b.getAbility().getAbilityId()){
								selectedAbilityButton = b;
								showInfo = true;
							}
						}else {
							selectedAbilityButton = b;
							showInfo = true;
						}
					}
				}
				
				if(!abilitySelected){
					showInfo = false;
					SelectedInfoBox.close();
					selectedAbilityButton = null;
				}
				
				
				if(showInfo && selectedAbilityButton != null){
					int infoBoxX = selectedAbilityButton.getTotalX(); 
					if(infoBoxX > 800){
						infoBoxX -= 200;
					}
					
					SelectedInfoBox = new ItemInfoBox(infoBoxX+50, selectedAbilityButton.getTotalY(),1,1);
					BlueSaga.client.sendMessage("ability_info", "abilityW;"+selectedAbilityButton.getAbility().getAbilityId());
				}
				
				if(SelectedInfoBox != null){
					SelectedInfoBox.draw(app, g, 0, 0);
				}
				
			}
		}
	}
	
	public void showInfoBox(String info){
		SelectedInfoBox.load(info);
	}
	
	@Override
	public void keyLogic(Input INPUT){
		super.keyLogic(INPUT);
		if(INPUT.isKeyPressed(Input.KEY_B)){
			toggle();
			
			if(isOpen()){
				load();
			}
		}
		
	}
	
	@Override
	public void leftMouseClick(Input INPUT){
		
		int mouseX = INPUT.getAbsoluteMouseX();
		int mouseY = INPUT.getAbsoluteMouseY();
	
		boolean clickedAbility = false;
		if(isVisible()){
			if(!BlueSaga.actionServerWait){
				for(AbilityButton b: Buttons){
					if(b.isClicked(mouseX, mouseY)){
						Gui.MouseItem.setAbility(b.getAbility());
						clickedAbility = true;
						break;
					}
				}
			}
		}
		
		if(!clickedAbility){
			super.leftMouseClick(INPUT);
		}
	}
	
	@Override
	public void rightMouseClick(Input INPUT){
		super.rightMouseClick(INPUT);
		
		int mouseX = INPUT.getAbsoluteMouseX();
		int mouseY = INPUT.getAbsoluteMouseY();
		
		if(isVisible()){
			if(!BlueSaga.actionServerWait){
				for(AbilityButton b: Buttons){
					if(b.isClicked(mouseX, mouseY)){
						BlueSaga.GUI.MouseItem.setAbility(b.getAbility());
						break;
					}
				}
			}
		}
	}
	
	@Override
	public void toggle(){
		if(!isOpen()){
			load();
		}
		super.toggle();
	}
	
}
