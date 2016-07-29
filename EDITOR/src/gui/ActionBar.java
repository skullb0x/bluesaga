package gui;

import java.util.Vector;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import components.Ability;
import components.Item;

public class ActionBar {
	
	private Vector<ActionBarButton> Buttons;
	
	
	private int X;
	private int Y;
	
	private int SelectedAction = 10;
	
	public ActionBar(int x, int y){
		
		X = x;
		Y = y;
		
		
		Buttons = new Vector<ActionBarButton>();
		for(int i = 0; i < 10; i++){
			Buttons.add(new ActionBarButton("None",0,X + i*52 + 22, Y+30, i));
		}
	}
	
	public void loadAbilities(Vector<Ability> Abilities){
		for(int i = 0; i < Abilities.size(); i++){
			Buttons.get(i).setAbility(Abilities.get(i));
			Abilities.get(i).setActionBarIndex(i);
		}
	}
	
	public void loadPotions(Vector<Item> Potions){
		Buttons.get(8).setItems(Potions,"HEALTH");
		Buttons.get(9).setItems(Potions,"MANA");
	}
	
	public void draw(Graphics g, int MouseX, int MouseY){
	}

	public void unselectAction(){
		Buttons.get(SelectedAction).setSelected(false);
		
	}
	
	public int keyLogic(Input INPUT){
	
		int keyNr = 10;
		
		if(INPUT.isKeyPressed(INPUT.KEY_1)){
			keyNr = 0;
		}else if(INPUT.isKeyPressed(INPUT.KEY_2)){
			keyNr = 1;
		}else if(INPUT.isKeyPressed(INPUT.KEY_3)){
			keyNr = 2;
		}else if(INPUT.isKeyPressed(INPUT.KEY_4)){
			keyNr = 3;
		}else if(INPUT.isKeyPressed(INPUT.KEY_5)){
			keyNr = 4;
		}else if(INPUT.isKeyPressed(INPUT.KEY_6)){
			keyNr = 5;
		}else if(INPUT.isKeyPressed(INPUT.KEY_7)){
			keyNr = 6;
		}else if(INPUT.isKeyPressed(INPUT.KEY_8)){
			keyNr = 7;
		}else if(INPUT.isKeyPressed(INPUT.KEY_9)){
			keyNr = 8;
		}else if(INPUT.isKeyPressed(INPUT.KEY_0)){
			keyNr = 9;
		}
		
		if(keyNr < 10){
			if(!Buttons.get(keyNr).getActionType().equals("None")){
			
				if(SelectedAction < 10){
					Buttons.get(SelectedAction).setSelected(false);
				}
				
				
				// CHECK IF ABILITY IS READY
				if(Buttons.get(keyNr).getActionType().equals("Ability")
				   && Buttons.get(keyNr).getAbility().isReady()){
		
					Buttons.get(keyNr).setSelected(true);
					SelectedAction = keyNr;
				}else if(Buttons.get(keyNr).getActionType().equals("Item")){
					if(Buttons.get(keyNr).getItem() != null){
						Buttons.get(keyNr).flashReady();
						SelectedAction = keyNr;
					}else {
						Buttons.get(keyNr).setActionType("None");
						keyNr = 10;
					}
				}
			
				
				
				
				
			}else{
				keyNr = 10;
			}
		}
		
		
		return keyNr;
	}
	
	
	public int getSelectedActionId(){
		return Buttons.get(SelectedAction).getActionId();
	}
	
	public String getSelectedActionType(){
		return Buttons.get(SelectedAction).getActionType();
	}
	
	public ActionBarButton getButton(int index){
		return Buttons.get(index);
	}
	
	public Vector<ActionBarButton> getButtons(){
		return Buttons;
	}
	
}
