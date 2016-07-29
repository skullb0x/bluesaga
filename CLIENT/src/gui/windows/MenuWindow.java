package gui.windows;

import game.BlueSaga;
import graphics.Font;
import gui.Button;
import gui.Gui;
import screens.ScreenHandler;
import screens.ScreenHandler.ScreenType;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;

import utils.LanguageUtils;


public class MenuWindow extends Window {

	Button CharacterButton;
	Button OptionsButton;
	Button QuitButton;
	
	private boolean CLICKED_QUIT = false;
	
	public MenuWindow(int x, int y, int width, int height) {
		super("MenuW", x, y, width, height, true);
		// TODO Auto-generated constructor stub
	
		OptionsButton =  new Button("OPTIONS", 25, 40,200, 35, this);
		CharacterButton =  new Button("LOGOUT CHARACTER", 25, 90,200, 35, this);
		QuitButton =  new Button("QUIT TO DESKTOP", 25, 140,200, 35, this);
		setMovable(false);
	}

	@Override
	public void draw(GameContainer app, Graphics g, int mouseX, int mouseY) {
		if(isVisible()){
			super.draw(app, g, mouseX, mouseY);
			if(isFullyOpened()){
				g.setFont(Font.size10);
				if(BlueSaga.logoutTimeItr > 0){
					g.setColor(new Color(255,255,255));
					String logoutText = "You can log out in "+BlueSaga.logoutTimeItr+" sec";
					int textWidth = Font.size12.getWidth(logoutText);
					g.drawString(logoutText, X + 140 - textWidth/2, Y + 17);
				}
				
				OptionsButton.draw(g,mouseX,mouseY);
				CharacterButton.draw(g,mouseX,mouseY);
				QuitButton.draw(g, mouseX, mouseY);
			}
		}
	}

	
	@Override
	public void leftMouseClick(Input INPUT){
		super.leftMouseClick(INPUT);
		
		int mouseX = INPUT.getAbsoluteMouseX();
		int mouseY = INPUT.getAbsoluteMouseY();
		
		if(isVisible()){
			
			if(OptionsButton.isClicked(mouseX, mouseY)){
				close();
				Gui.OptionsWindow.open();
			}
			
			if(BlueSaga.logoutTimeItr == 0){
				if(CharacterButton.isClicked(mouseX, mouseY)){
					Gui.MouseItem.clear();
					BlueSaga.client.sendMessage("quitchar", "quit");
					ScreenHandler.setActiveScreen(ScreenType.LOADING);
				 	
					ScreenHandler.LoadingStatus = LanguageUtils.getString("ui.status.saving_mini_map");
					
					Gui.MapWindow.saveMiniMap(2);
					BlueSaga.reciever.lostConnection = false;
					BlueSaga.lastPlayedDbId = 0; 
				}
				if(QuitButton.isClicked(mouseX, mouseY) && !CLICKED_QUIT){
					BlueSaga.HAS_QUIT = true;
					ScreenHandler.setActiveScreen(ScreenType.LOADING);
					ScreenHandler.LoadingStatus = LanguageUtils.getString("ui.status.saving_mini_map");
					CLICKED_QUIT = true;
					BlueSaga.client.sendMessage("connection", "disconnect");
					
					Gui.MapWindow.saveMiniMap(1);
				}
			}
			
		}
	}
	

}