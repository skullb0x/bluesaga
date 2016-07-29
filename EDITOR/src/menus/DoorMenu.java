package menus;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.gui.TextField;

import game.BP_EDITOR;

public class DoorMenu {

	private boolean Active = false;
	
	private int ActiveDoorId;
	private int X;
	private int Y;

	private TextField gotoXField;
	private TextField gotoYField;
	private TextField gotoZField;
	
	public DoorMenu(int x, int y, GameContainer app){
		
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
	
	public int createDoor(int tileX, int tileY, int tileZ){
		int doorToReturn = 0;
		if(!Active){
			Active = true;
			BP_EDITOR.mapDB.updateDB("insert into door (GotoX, GotoY, GotoZ, Locked) values (0,0,0,0)");
			
			ResultSet rs = BP_EDITOR.mapDB.askDB("select Id from door order by Id desc limit 1");
			
			try {
				if(rs.next()){
					ActiveDoorId = rs.getInt("Id");
					BP_EDITOR.mapDB.updateDB("update area_tile set DoorId = "+ActiveDoorId+" where X = "+tileX+" and Y = "+tileY+" and Z = "+ tileZ);
				}
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			doorToReturn = ActiveDoorId;
			Active = true;
			
			gotoXField.setText(tileX+"");
			gotoYField.setText(tileY+"");
			gotoZField.setText(tileZ+"");
		}
		return doorToReturn;
	}
	
	
	public void keyLogic(Input INPUT){
		
		// DELETE CREATED DOOR
		if(INPUT.isKeyPressed(Input.KEY_ESCAPE)){
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
				BP_EDITOR.mapDB.updateDB("update door set GotoX = "+gotoXField.getText()+", GotoY = "+gotoYField.getText()+", GotoZ = "+gotoZField.getText()+" where Id = "+ActiveDoorId);
				
				Active = false;
				BP_EDITOR.PLACE_DOOR = false;
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
		
		gotoXField.setFocus(true);
	}
	
}
