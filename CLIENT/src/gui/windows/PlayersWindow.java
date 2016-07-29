package gui.windows;

import game.BlueSaga;
import graphics.BlueSagaColors;
import graphics.Font;
import gui.Button;
import gui.Gui;
import gui.TextField;
import gui.list.GuiList;

import java.util.Timer;
import java.util.TimerTask;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;

import sound.Sfx;
import utils.LanguageUtils;

public class PlayersWindow extends Window {


	private GuiList PlayerButtons1;
	private GuiList PlayerButtons2;


	private Timer privateMessageTimer;

	private String selectedPlayerName;

	private int TOTAL_ONLINE = 0;

	private Button AddFriendButton;
	private Button CancelButton;

	
	private Button BackButton;
	
	private Button PartyButton;
	private Button AddToPartyButton;

	private String ACTIVE_VIEW = "LIST";

	private TextField new_friend_text;
	
	public PlayersWindow(GameContainer app, int x, int y, int width, int height) {
		super("PlayersW", x, y, width, height, true);
		// TODO Auto-generated constructor stub
		PlayerButtons1 = new GuiList(x+20,y+60,120, height - 80);
		PlayerButtons2 = new GuiList(x+140,y+60,120, height - 80);

		privateMessageTimer = new Timer();

		AddFriendButton = new Button(LanguageUtils.getString("ui.players_window.add_friend").toUpperCase(),20,height - 58,this);
		CancelButton = new Button(LanguageUtils.getString("ui.buttons.cancel").toUpperCase(),170,125,80,38,this);

		BackButton = new Button(LanguageUtils.getString("ui.buttons.back").toUpperCase(),170,height - 58,80,38,this);
		PartyButton = new Button(LanguageUtils.getString("ui.players_window.party").toUpperCase(),170,height - 58,80,38,this);
		AddToPartyButton = new Button(LanguageUtils.getString("ui.players_window.add_player").toUpperCase(),20,height - 58,140,38,this);

		new_friend_text = new TextField(app, Font.size12, X+30, Y+90, 160, 30);
		new_friend_text.setBackgroundColor(new Color(0,0,0,0));
		new_friend_text.setBorderColor(new Color(0,0,0,0));
		new_friend_text.setTextColor(new Color(255,255,255,255));
		new_friend_text.setFocus(false);
		new_friend_text.setMaxLength(15);
		new_friend_text.setAcceptingInput(false);
	}


	public void loadPlayers(String playerInfo){
		PlayerButtons1.reset();
		PlayerButtons2.reset();
		String online_info[] = playerInfo.split("/");

		int nrfriends = 0;

		if(!online_info[0].equals("")){
			TOTAL_ONLINE = Integer.parseInt(online_info[0]);
			if(online_info.length > 1){
				String friends_info[] = online_info[1].split(";");
				for(String player: friends_info){
					if(nrfriends > 8){
						PlayerButtons2.addButton(player);
					}else{
						PlayerButtons1.addButton(player);
					}
					nrfriends++;
				}
			}
		}
		new_friend_text.setAcceptingInput(false);
	}

	public void loadPartyMembers(String playerInfo){
		PlayerButtons1.reset();
		PlayerButtons2.reset();
		String online_info[] = playerInfo.split("/");

		
		int nrfriends = Integer.parseInt(online_info[0]);
		
		
		if(nrfriends > 0){
			if(online_info.length > 1){
				String friends_info[] = online_info[1].split(",");
				nrfriends = 0;
				for(String player: friends_info){
					if(nrfriends > 8){
						PlayerButtons2.addButton(player);
					}else{
						PlayerButtons1.addButton(player);
					}
					nrfriends++;
				}
			}
		}
		new_friend_text.setAcceptingInput(false);
	}
	

	@Override
	public void draw(GameContainer app, Graphics g, int mouseX, int mouseY){

		if(isVisible()){
			super.draw(app, g, mouseX, mouseY);

			if(isFullyOpened()){
				if(ACTIVE_VIEW.equals("LIST")){
					g.setColor(BlueSagaColors.YELLOW);
					g.setFont(Font.size16);
					g.drawString(LanguageUtils.getString("ui.players_window.players_online").toUpperCase()+": ", X+20+moveX, Y+20+moveY);

					g.setColor(BlueSagaColors.WHITE);
					g.drawString(""+TOTAL_ONLINE,X+200+moveX, Y+20+moveY);

					PlayerButtons1.draw(g, mouseX, mouseY, moveX, moveY);
					PlayerButtons2.draw(g, mouseX, mouseY, moveX, moveY);

					if(PlayerButtons1.getNrListItems() == 0){
						g.drawString(LanguageUtils.getString("ui.players_window.no_friends"), X+20+moveX, Y+60+moveY);
					}
					AddFriendButton.draw(g, mouseX, mouseY);
					PartyButton.draw(g, mouseX, mouseY);
				}else if(ACTIVE_VIEW.equals("ADD FRIEND")){
					g.setColor(BlueSagaColors.YELLOW);
					g.setFont(Font.size16);
					g.drawString(LanguageUtils.getString("ui.players_window.add_friend").toUpperCase(), X+20+moveX, Y+20+moveY);

					g.setColor(BlueSagaColors.WHITE);
					g.setFont(Font.size12);
					g.drawString(LanguageUtils.getString("ui.players_window.enter_player_name"),X+20+moveX, Y+60+moveY);

					g.setColor(new Color(255,255,255,100));
					g.fillRoundRect(X+20+moveX, Y+80+moveY, 200, 40, 10);
					g.setColor(new Color(0,0,0,150));
					g.fillRoundRect(X+25+moveX, Y+85+moveY, 190, 30, 8);
					g.setColor(new Color(255,255,255,255));

					new_friend_text.setLocation(X+30 + moveX, Y+90 + moveY);

					new_friend_text.render(app, g);


					AddFriendButton.draw(g, mouseX, mouseY);
					CancelButton.draw(g, mouseX, mouseY);
				}else if(ACTIVE_VIEW.equals("PARTY")){
					g.setColor(BlueSagaColors.YELLOW);
					g.setFont(Font.size16);
					
					if(PlayerButtons1.getNrListItems() == 0){
						g.drawString(LanguageUtils.getString("ui.players_window.no_party_active"), X+20+moveX, Y+20+moveY);
					}else{
						g.drawString(LanguageUtils.getString("ui.players_window.party_members"), X+20+moveX, Y+20+moveY);
					}

					PlayerButtons1.draw(g, mouseX, mouseY, moveX, moveY);
					PlayerButtons2.draw(g, mouseX, mouseY, moveX, moveY);

					
					BackButton.draw(g,mouseX,mouseY);
					AddToPartyButton.draw(g, mouseX, mouseY);
				}else if(ACTIVE_VIEW.equals("ADD TO PARTY")){
					g.setColor(BlueSagaColors.YELLOW);
					g.setFont(Font.size16);
					g.drawString(LanguageUtils.getString("ui.players_window.add_player_to_party").toUpperCase(), X+20+moveX, Y+20+moveY);
					
					g.setColor(BlueSagaColors.WHITE);
					g.setFont(Font.size12);
					g.drawString(LanguageUtils.getString("ui.players_window.enter_player_name"),X+20+moveX, Y+60+moveY);

					g.setColor(new Color(255,255,255,100));
					g.fillRoundRect(X+20+moveX, Y+80+moveY, 200, 40, 10);
					g.setColor(new Color(0,0,0,150));
					g.fillRoundRect(X+25+moveX, Y+85+moveY, 190, 30, 8);
					g.setColor(new Color(255,255,255,255));

					new_friend_text.setLocation(X+30 + moveX, Y+90 + moveY);

					new_friend_text.render(app, g);
					
					AddToPartyButton.draw(g, mouseX, mouseY);
					CancelButton.draw(g, mouseX, mouseY);
				}
			}
		}
	}

	@Override
	public void keyLogic(Input INPUT){
		if(!ACTIVE_VIEW.equals("ADD FRIEND") && !ACTIVE_VIEW.equals("ADD TO PARTY") && INPUT.isKeyPressed(Input.KEY_P) && !Gui.Chat_Window.isActive()){
			toggle();
		}
	}

	@Override
	public void leftMouseClick(Input INPUT){
		super.leftMouseClick(INPUT);

		int mouseX = INPUT.getAbsoluteMouseX();
		int mouseY = INPUT.getAbsoluteMouseY();

		if(isFullyOpened()){
			
			
			if(ACTIVE_VIEW.equals("LIST") || ACTIVE_VIEW.equals("PARTY")){
				int listIndex1 = PlayerButtons1.select(mouseX, mouseY, moveX, moveY);
				int listIndex2 = PlayerButtons2.select(mouseX, mouseY, moveX, moveY);

				if(listIndex1 < 9999 || listIndex2 < 9999){
					String playerName = "";

					if(listIndex1 < 9999){
						playerName = PlayerButtons1.getListItem(listIndex1).getLabel();
					}else if(listIndex2 < 9999){
						playerName = PlayerButtons2.getListItem(listIndex2).getLabel();
					}

					selectedPlayerName = playerName;
					close();
					Gui.Chat_Window.clearInputLine();
					Gui.Chat_Window.addChatChannel("@"+selectedPlayerName.toLowerCase());
					Gui.Chat_Window.setActiveChatChannel("@"+selectedPlayerName.toLowerCase());
				
					privateMessageTimer.schedule( new TimerTask(){
						@Override
						public void run() {
							Gui.Chat_Window.setActive(true);
						}
					}, 100);
				}
			}
			
			// PLAYERS ONLINE
			if(ACTIVE_VIEW.equals("LIST")){
								
				if(PartyButton.isClicked(mouseX, mouseY)){
					ACTIVE_VIEW = "PARTY";
					AddToPartyButton.setY(Height - 58);
					PlayerButtons1.reset();
					PlayerButtons2.reset();
					BlueSaga.client.sendMessage("party_members", "info");
				}
				
				if(AddFriendButton.isClicked(mouseX, mouseY)){
					Sfx.play("gui/menu_select");
					ACTIVE_VIEW = "ADD FRIEND";
					AddFriendButton.setY(125);
					new_friend_text.setFocus(true);
					new_friend_text.setAcceptingInput(true);
					new_friend_text.setText("");
				}
				
			}else if(ACTIVE_VIEW.equals("ADD FRIEND")){
				if(AddFriendButton.isClicked(mouseX, mouseY)){
					if(!new_friend_text.getText().equals("")){
						BlueSaga.client.sendMessage("add_friend", new_friend_text.getText());
						toggle();
					}
				}

				if(CancelButton.isClicked(mouseX, mouseY)){
					Sfx.play("gui/menu_back");
					ACTIVE_VIEW = "LIST";
					BlueSaga.client.sendMessage("playersonline", "info");
						
					AddFriendButton.setY(Height - 58);
					new_friend_text.setFocus(false);
					new_friend_text.setAcceptingInput(false);
				}
			}else if(ACTIVE_VIEW.equals("PARTY")){
				if(AddToPartyButton.isClicked(mouseX, mouseY)){
					ACTIVE_VIEW = "ADD TO PARTY";
					AddToPartyButton.setY(125);
					new_friend_text.setFocus(true);
					new_friend_text.setAcceptingInput(true);
					new_friend_text.setText("");
				}else if(BackButton.isClicked(mouseX, mouseY)){
					BlueSaga.client.sendMessage("playersonline", "info");
					ACTIVE_VIEW = "LIST";
					new_friend_text.setFocus(false);
					AddFriendButton.setY(Height - 58);
					new_friend_text.setAcceptingInput(false);
				}
			}else if(ACTIVE_VIEW.equals("ADD TO PARTY")){
				if(AddToPartyButton.isClicked(mouseX, mouseY)){
					if(!new_friend_text.getText().equals("")){
						BlueSaga.client.sendMessage("add_to_party", new_friend_text.getText());
						toggle();
					}
				}
				
				if(CancelButton.isClicked(mouseX, mouseY)){
					Sfx.play("gui/menu_back");
					AddToPartyButton.setY(Height - 58);
					ACTIVE_VIEW = "PARTY";
					new_friend_text.setFocus(false);
					new_friend_text.setAcceptingInput(false);
				}
			}
			
			
		}
	}

	@Override
	public void toggle(){
		if(!isOpen()){
			BlueSaga.client.sendMessage("playersonline", "info");
			ACTIVE_VIEW = "LIST";
			new_friend_text.setFocus(false);
			AddFriendButton.setY(Height - 58);
			new_friend_text.setAcceptingInput(false);
		}else{
			new_friend_text.setFocus(false);
			ACTIVE_VIEW = "LIST";
			new_friend_text.setAcceptingInput(false);
		}
		super.toggle();
	}

	@Override
	public void open(){
		Gui.closeInputWindows();
		super.open();
	}

	@Override
	public void close(){
		ACTIVE_VIEW = "LIST";
		super.close();
	}



	@Override
	public void stopMove(){

		if(PlayerButtons1 != null){
			PlayerButtons1.updatePos(moveX,moveY);
		}
		if(PlayerButtons2 != null){
			PlayerButtons2.updatePos(moveX,moveY);
		}
		
		new_friend_text.setLocation(X+30 + moveX, Y+90 + moveY);

		super.stopMove();

	}

	public String getActiveView(){
		return ACTIVE_VIEW;
	}

	public boolean hasActiveTextField(){
		if(ACTIVE_VIEW.equals("ADD FRIEND")){
			return true;
		}
		return false;
	}
}
