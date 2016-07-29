package gui.windows;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;

import utils.LanguageUtils;
import game.BlueSaga;
import graphics.BlueSagaColors;
import graphics.Font;
import graphics.ImageResource;
import gui.Button;
import gui.TextField;

public class BountyWindow extends Window {
	
	private TextField bounty_field;
	private TextField target_name;
	
	private Button ConfirmButton;
	
	private String status_text;
	
	public BountyWindow(GameContainer app, int x, int y, int width, int height) {
		super("BountyW", x, y, width, height,true);
	
		bounty_field = new TextField(app, Font.size12, 525, 260, 80, 30);
		bounty_field.setBackgroundColor(new Color(0,0,0,0));
		bounty_field.setBorderColor(new Color(0,0,0,0));
		bounty_field.setTextColor(new Color(255,255,255,255));
		bounty_field.setMaxLength(6);
		
		target_name = new TextField(app, Font.size12, 525, 300, 80, 30);
		target_name.setBackgroundColor(new Color(0,0,0,0));
		target_name.setBorderColor(new Color(0,0,0,0));
		target_name.setTextColor(new Color(255,255,255,255));
		target_name.setMaxLength(15);
	
		ConfirmButton =  new Button(LanguageUtils.getString("ui.bounty.place_bounty").toUpperCase(), 55, 130,150, 35, this);		
	
		status_text = "";
	}
	

	@Override
	public void draw(GameContainer app, Graphics g, int mouseX, int mouseY){
		if(isVisible()){
			super.draw(app, g, mouseX, mouseY);
			
			g.setFont(Font.size12);
			g.setColor(BlueSagaColors.WHITE);
			g.drawString(LanguageUtils.getString("ui.bounty.sum"), realX+20, realY+60);
			g.drawString(LanguageUtils.getString("ui.bounty.player_name"), realX+20, realY+100);
	
			
			g.setColor(BlueSagaColors.RED.darker());
			g.fillRect(realX+120, realY+55,100,25);
			g.fillRect(realX+120, realY+95,100,25);
			
			g.setColor(BlueSagaColors.WHITE);
			
			if(!status_text.equals("Sending request...")){
				ConfirmButton.draw(g, mouseX, mouseY);
			}
			g.setColor(BlueSagaColors.WHITE);
			
			int textWidth = Font.size12.getWidth(status_text);
			g.drawString(status_text, realX + 125 - textWidth/2, realY + 180);
			
			
			bounty_field.setLocation(realX + 125, realY+60);
			target_name.setLocation(realX + 125, realY+100);
			
			bounty_field.render(app, g);
			target_name.render(app, g);
			
			ImageResource.getSprite("gui/menu/bounty_label").draw(realX+20,realY+15);
		}
	}
	
	@Override
	public void keyLogic(Input INPUT){
		if(isOpen()){
			super.keyLogic(INPUT);
			
			if(INPUT.isKeyPressed(Input.KEY_TAB)){
				changeField();
			}
		}
	}
	
	@Override
	public void leftMouseClick(Input INPUT){
		super.leftMouseClick(INPUT);
		
		int mouseX = INPUT.getAbsoluteMouseX();
		int mouseY = INPUT.getAbsoluteMouseY();
		
	
		if(ConfirmButton.isClicked(mouseX, mouseY)){
			// CHECK FIELDS
			if(!target_name.getText().equals("")){
				if(isIntNumber(bounty_field.getText())){
					BlueSaga.client.sendMessage("placebounty",target_name.getText().toLowerCase()+";"+bounty_field.getText());
					status_text = "Sending request...";
				}else{
					status_text = "Not a correct sum";
				}
			}else{
				status_text = "Enter player name";
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
	
	public void changeField() {
		if(bounty_field.hasFocus()){
			bounty_field.setFocus(false);
			target_name.setFocus(true);
		}else{
			bounty_field.setFocus(true);
			target_name.setFocus(false);
		}
	}
	
	@Override
	public void open(){
		super.open();
		
		bounty_field.setText("");
		target_name.setText("");
		
		bounty_field.setCursorPos(1);
		target_name.setCursorPos(1);
		
		bounty_field.setFocus(true);
		
		bounty_field.setCursorVisible(true);
		
		status_text = "";
	}
	
	public void setStatusText(String newText){
		status_text = newText;
	}
	
}
