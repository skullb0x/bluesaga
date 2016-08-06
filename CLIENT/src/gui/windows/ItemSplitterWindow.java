package gui.windows;

import game.BlueSaga;
import graphics.BlueSagaColors;
import graphics.Font;
import gui.TextField;
import network.Client;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;

import utils.LanguageUtils;

public class ItemSplitterWindow extends Window {
	private TextField number_field;
	private String InventoryPos;
	
	public ItemSplitterWindow(GameContainer app, int x, int y, int width, int height) {
		super("ItemSplitterW", x, y, width, height, true);
		// TODO Auto-generated constructor stub
		
		number_field = new TextField(app, Font.size12, x+20, y+55, 80, 30);
		number_field.setBackgroundColor(new Color(0,0,0,0));
		number_field.setBorderColor(new Color(0,0,0,0));
		number_field.setTextColor(new Color(255,255,255,255));
		number_field.setMaxLength(6);
	}
	
	@Override
	public void draw(GameContainer app, Graphics g, int mouseX, int mouseY){
		if(isVisible()){
			super.draw(app, g, mouseX, mouseY);
			
			if(isFullyOpened()){
			
				g.setColor(BlueSagaColors.RED.darker());
				g.fillRect(X+moveX+20, Y+moveY+50,80,25);
				
				g.setFont(Font.size12);
				
				g.setColor(BlueSagaColors.WHITE);
				number_field.render(app, g);
				
				number_field.setLocation(X+20+moveX, Y+55+moveY);
				
				g.drawString(LanguageUtils.getString("ui.split.enter_amount"), X+moveX+20, Y+moveY+20);
			}
		}
	}

	@Override
	public void leftMouseClick(Input INPUT){
		super.leftMouseClick(INPUT);
	}

	public void keyLogic(Input INPUT, Client client){
		if(isOpen()){
			if(INPUT.isKeyPressed(Input.KEY_RETURN) || INPUT.isKeyPressed(Input.KEY_NUMPADENTER)){
				if(!number_field.getText().equals("")){
					if(isIntNumber(number_field.getText())){
						BlueSaga.client.sendMessage("splitmouse",getInventoryPos()+";"+number_field.getText());
						close();
					}
				}
			}
		}
	}
	
	public boolean isIntNumber(String num){
	    try{
	        Integer.parseInt(num);
	        if(Integer.parseInt(num) == 0){
	        	return false;
	        }
	    } catch(NumberFormatException nfe) {
	        return false;
	    }
	    return true;
	}
	
	
	@Override
	public boolean clickedOn(int mouseX, int mouseY){
		boolean clicked = false;
		
		clicked = super.clickedOn(mouseX, mouseY);
		
		if(clicked){
			number_field.setLocation(X+moveX+20, Y+moveY+55);
		}
		
		return clicked;		
	}

	@Override
	public void open(){
		BlueSaga.GUI.closeInputWindows();
		super.open();
		number_field.setText("");
		
		number_field.setCursorPos(1);
		
		number_field.setFocus(true);
		
		number_field.setCursorVisible(true);
		
	}

	public String getInventoryPos() {
		return InventoryPos;
	}

	public void setInventoryPos(String inventoryPos) {
		InventoryPos = inventoryPos;
	}
	
}
