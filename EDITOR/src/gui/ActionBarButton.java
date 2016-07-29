package gui;


import java.util.Vector;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

import components.Ability;
import components.Item;

public class ActionBarButton {

	private Image IconImage;
	
	private String ActionType = "None"; // Item or Ability
	private Ability Ability;
	private Vector<Item> Items = new Vector<Item>();
	
	private boolean Selected = false;
	
	private int ActionBarIndex;
	
	public ActionBarButton(String actionType, int actionId, int x, int y, int actionBarIndex){
		ActionBarIndex = actionBarIndex;
		
		ActionType = actionType;
		
		
	}
	
	public void setAP(int ap, int apNext){
	}
	
	public void setAbility(Ability newAbility){
		ActionType = "Ability";
		Ability = newAbility;
		
		try {
			IconImage = new Image("images/gui/actionbar/icon_ability_"+Ability.getId()+".png");
		} catch (SlickException e) {
			e.printStackTrace();
		}
	}
	
	
	public void draw(Graphics g, int MouseX, int MouseY){
	}
	
	public void setSelected(boolean newState){
		Selected = newState;
	}
	
	public boolean getSelected(){
		return Selected;
	}
	
	public String getActionType(){
		return ActionType;
	}
	
	public void setActionType(String newType){
		ActionType = newType;
	}
	
	public int getActionId(){
		if(ActionType.equals("Ability")){
			return Ability.getId();
		}else if(ActionType.equals("Item")){
			return Items.get(0).getUserItemId();
		}else{
			return 0;
		}
	}
	
	public Ability getAbility(){
		return Ability;
	}
	
	public void setItems(Vector<Item> items, String Type){
		
		Items.clear();
		for(Item p: items){
			if(p.getSubType().equals(Type)){
				Items.add(p);
			}
		}
		
		if(Items.size() > 0){
			ActionType = "Item";
			
			try {
				IconImage = new Image("images/gui/actionbar/icon_item_"+Items.get(0).getId()+".png");
			} catch (SlickException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void updateButton(){
		if(ActionType.equals("Item")){
			// HEALTH
			if(ActionBarIndex == 8){
				int nrHealthPotions = 0;
				
				for(Item itemToCheck: Items){
					if(itemToCheck.getType().equals("Potion") && itemToCheck.getSubType().equals("HEALTH")){
						nrHealthPotions++;
					}
				}
				if(nrHealthPotions > 0){
					try {
						IconImage = new Image("images/gui/actionbar/icon_item_"+Items.get(0).getId()+".png");
					} catch (SlickException e) {
						e.printStackTrace();
					}
				}else{
					ActionType = "None";
				}
			}else if(ActionBarIndex == 9){
				// MANA
				int nrManaPotions = 0;
				
				for(Item itemToCheck: Items){
					if(itemToCheck.getType().equals("Potion") && itemToCheck.getSubType().equals("MANA")){
						nrManaPotions++;
					}
				}
				if(nrManaPotions > 0){
					try {
						IconImage = new Image("images/gui/actionbar/icon_item_"+Items.get(0).getId()+".png");
					} catch (SlickException e) {
						e.printStackTrace();
					}
				}else{
					ActionType = "None";
				}
			}
			
			
		}
	}
	
	public Item getItem(){
		if(Items.size() > 0){
			return Items.get(0);
		}else {
			return null;
		}
	}
	
	public void flashReady(){
	}
}
