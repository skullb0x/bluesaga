package menus;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.gui.TextField;

import game.BP_EDITOR;

public class GotoMenu {

	private boolean Active = false;
	
	private int X;
	private int Y;

	private TextField gotoXField;
	private TextField gotoYField;
	private TextField gotoZField;
	
	public GotoMenu(int x, int y, GameContainer app){
		
		Active = false;
		X = x;
		Y = y;

		gotoXField = new TextField(app, BP_EDITOR.FONTS.size12, X+18, Y+45, 150, 20);
		gotoXField.setBackgroundColor(new Color(0,0,0,80));
		gotoXField.setBorderColor(new Color(0,0,0,0));
		gotoXField.setFocus(false);
		gotoXField.setCursorVisible(true);
		
		gotoYField = new TextField(app, BP_EDITOR.FONTS.size12, X+18, Y+95, 150, 20);
		gotoYField.setBackgroundColor(new Color(0,0,0,80));
		gotoYField.setBorderColor(new Color(0,0,0,0));
		gotoYField.setFocus(false);
		gotoYField.setCursorVisible(true);
		
		gotoZField = new TextField(app, BP_EDITOR.FONTS.size12, X+18, Y+145, 150, 20);
		gotoZField.setBackgroundColor(new Color(0,0,0,80));
		gotoZField.setBorderColor(new Color(0,0,0,0));
		gotoZField.setFocus(false);
		gotoZField.setCursorVisible(true);
		
	}
	
	
	public void draw(Graphics g, GameContainer app){
		if(Active){
			g.setColor(new Color(238,82,65,255));
			g.fillRect(X, Y, 213, 255);
			
			g.setColor(new Color(255,255,255,255));
			
			g.drawString("Goto X,Y,Z:", X+20, Y+20);
			gotoXField.render(app, g);
			gotoYField.render(app, g);
			gotoZField.render(app, g);
			
				
		}
	}
	
	public boolean isActive(){
		return Active;
	}
	
	
	public void keyLogic(Input INPUT){
		if(!isActive()){
			if(INPUT.isKeyPressed(Input.KEY_G)){
				BP_EDITOR.closeMenus();
				open();
			}
		}else{
			if(INPUT.isKeyPressed(Input.KEY_G)){
				toggle();
			}else if(INPUT.isKeyPressed(Input.KEY_ESCAPE)){
				Active = false;
			}
			
			if(INPUT.isKeyPressed(Input.KEY_TAB)){
				if(gotoXField.hasFocus()){
					gotoXField.setFocus(false);
					gotoYField.setFocus(true);
					gotoZField.setFocus(false);
				}else if(gotoYField.hasFocus()){
					gotoXField.setFocus(false);
					gotoYField.setFocus(false);
					gotoZField.setFocus(true);
				}else{
					gotoXField.setFocus(true);
				}
			}
			
			// SAVE COORDINATES OF DOOR GOAL
			if(INPUT.isKeyPressed(Input.KEY_ENTER)){
				if(!gotoXField.getText().equals("") && !gotoYField.getText().equals("") && !gotoZField.getText().equals("")){
					BP_EDITOR.PLAYER_X = Integer.parseInt(gotoXField.getText());
					BP_EDITOR.PLAYER_Y = Integer.parseInt(gotoYField.getText());
					BP_EDITOR.PLAYER_Z = Integer.parseInt(gotoZField.getText());
					
					BP_EDITOR.loadScreen();
					
					Active = false;
				}
			}
			
		}
		
	}
	
	public void toggle(){
		if(Active){
			Active = false;
		}else{
			Active = true;
		
		}
	}
	
	public void open(){
		Active = true;
		gotoXField.setText(""+BP_EDITOR.PLAYER_X);
		gotoYField.setText(""+BP_EDITOR.PLAYER_Y);
		gotoZField.setText(""+BP_EDITOR.PLAYER_Z);
		gotoXField.setFocus(true);
		gotoYField.setFocus(false);
		gotoZField.setFocus(false);
	}
	
}
