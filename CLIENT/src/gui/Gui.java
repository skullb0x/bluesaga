package gui;

import game.BlueSaga;
import game.ClientSettings;
import graphics.BlueSagaColors;
import graphics.Font;
import graphics.ImageResource;
import graphics.screeneffects.StatusScreenEffect;
import gui.dragndrop.DragObject;
import gui.windows.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;

import abilitysystem.Ability;
import abilitysystem.StatusEffect;
import sound.Sfx;
import utils.LanguageUtils;
import creature.Creature;
import creature.Npc;
import creature.Creature.CreatureType;
import data_handlers.AbilityHandler;
import data_handlers.FishingHandler;
import data_handlers.WalkHandler;
import map.ScreenObject;
import screens.ScreenHandler;
import screens.ScreenHandler.ScreenType;

public class Gui {

	private static ActionBar ActionBar = new ActionBar(600,570); //600, 570

	// PLAYER INFO
	private Image xp_bg;
	private Image xp_meter;

	private Image meter_bg;
	private Image hp_meter;
	private Image mana_meter;

	private Image heart_big;
	private Image star_big;

	// WINDOWS
	private static ArrayList<Window> ALL_WINDOWS;

	public static MostWantedWindow mostWantedWindow;
	public static Chat Chat_Window;
	public static ShopWindow ShopWindow;
	public static InventoryWindow InventoryWindow;
	public static StatusWindow StatusWindow;
	public static AbilitiesWindow AbilitiesWindow;
	public static SkillWindow SkillWindow;
	public static MenuWindow MenuWindow;
	public static QuestWindow QuestWindow;
	public static OptionsWindow OptionsWindow;
	public static BountyWindow BountyWindow;
	public static PlayersWindow PlayersWindow;
	public static ItemSplitterWindow ItemSplitterWindow;
	public static NpcDialog QUEST_DIALOG;
	public static ContainerWindow ContainerWindow;
	public static EventWindow EventWindow;
	public static MapWindow MapWindow;
	public static BookWindow BookWindow;
	public static CraftingWindow CraftingWindow;
	public static CharacterSkinWindow CharacterSkinWindow;
	public static CardBook CardBook;
	
	public static TutorialDialog TutorialDialog;
	
	public static int MaxWindowsZ = 0;

	public static boolean MovingWindows = false;

	// NOTIFICATIONS
	private static ArrayList<Notification> Notifications;
	private static int NotificationsY;


	// PLAY MUSIC
	private static boolean PlayMusicMode = false;

	// MENU BUTTONS
	private static Button CharacterButton;
	private static Button AbilitiesButton;
	private static Button InventoryButton;
	private static Button SkillsButton;
	private static Button QuestButton;
	private static Button CommunityButton;
	private static Button MenuButton;
	private static Button CardButton;
	
	// GAME OVER 
	private Image Die_Label;

	// BOUNTY INDICATOR
	private Image bountyBg;
	private static boolean SHOW_BOUNTY;
	private static int oldBounty;
	private static int newBounty;
	private static int bountyChange;
	private static int bountyOpacity;
	private static Timer bountyTimer;
	private Image arrow_down;
	private Image arrow_up;
	private static int bountyAniCounter;

	private Vector<StatusScreenEffect> ScreenFX;

	// MOUSE
	public static MouseCursor Mouse;
	public static boolean MOUSE_DONE_ACTION = false;
	public static DragObject MouseItem;


	public static boolean USE_ABILITY = false;

	public static boolean USE_SCROLL = false;
	public static int USE_SCROLL_ID = 0;
	public static String USE_SCROLL_TYPE = "None"; // Ability or ...
	public static String USE_SCROLL_LOCATION = "Inventory"; // Inventory or Actionbar


	public Gui() {
		Notifications = new ArrayList<Notification>();
		Notifications.clear();
		NotificationsY = 80;
		ScreenFX = new Vector<StatusScreenEffect>();

		ALL_WINDOWS = new ArrayList<Window>();

		Mouse = new MouseCursor();
		MouseItem = new DragObject();
	}


	public void init(GameContainer container) {

		Die_Label = ImageResource.getSprite("gui/world/you_die_label").getImage();

		xp_bg = ImageResource.getSprite("gui/world/xp_bg").getImage();
		xp_meter = ImageResource.getSprite("gui/world/xp_meter").getImage();

		meter_bg = ImageResource.getSprite("gui/world/meter_bg").getImage();
		hp_meter = ImageResource.getSprite("gui/world/hp_meter").getImage();
		mana_meter = ImageResource.getSprite("gui/world/mana_meter").getImage();

		heart_big = ImageResource.getSprite("gui/world/heart_big").getImage();
		star_big = ImageResource.getSprite("gui/world/star_big").getImage();

		bountyBg = ImageResource.getSprite("gui/pvp/bounty_bg").getImage();
		arrow_down = ImageResource.getSprite("gui/pvp/arrow_down").getImage();
		arrow_up = ImageResource.getSprite("gui/pvp/arrow_up").getImage();

		int buttonX = 600;
		int buttonY = 20;
		int buttonSpace = 50;
		
		CharacterButton = new Button("",buttonX,buttonY,50,50,null);
		CharacterButton.setImage("gui/world/character_info_button");
		CharacterButton.getToolTip().setText(LanguageUtils.getString("ui.top_menu.character_info"));

		buttonX += buttonSpace;

		AbilitiesButton = new Button("",buttonX,buttonY,50,50,null);
		AbilitiesButton.setImage("gui/world/abilities_button");
		AbilitiesButton.getToolTip().setText(LanguageUtils.getString("ui.top_menu.abilities"));

		buttonX += buttonSpace;

		InventoryButton = new Button("",buttonX,buttonY,50,50,null);
		InventoryButton.setImage("gui/world/inventory_button");

		InventoryButton.getToolTip().setText(LanguageUtils.getString("ui.top_menu.inventory"));

		buttonX += buttonSpace;

		SkillsButton = new Button("",buttonX,buttonY,50,50,null);
		SkillsButton.setImage("gui/world/skills_button");

		SkillsButton.getToolTip().setText(LanguageUtils.getString("ui.top_menu.skills"));


		buttonX += buttonSpace;

		QuestButton = new Button("",buttonX,buttonY,50,50,null);
		QuestButton.setImage("gui/world/quest_button");
		QuestButton.getToolTip().setText(LanguageUtils.getString("ui.top_menu.quests"));

		buttonX += buttonSpace;

		CommunityButton = new Button("",buttonX,buttonY,50,50,null);
		CommunityButton.setImage("gui/world/community_button");
		CommunityButton.getToolTip().setText(LanguageUtils.getString("ui.top_menu.players"));

		buttonX += buttonSpace;

		CardButton = new Button("",buttonX, buttonY,50,50,null);
		CardButton.setImage("gui/world/card_button");
		CardButton.getToolTip().setText(LanguageUtils.getString("ui.top_menu.card_book"));
		
		buttonX += buttonSpace;
		
		MenuButton = new Button("",buttonX,buttonY,50,50,null);
		MenuButton.setImage("gui/world/menu_button");
		MenuButton.getToolTip().setText(LanguageUtils.getString("ui.top_menu.game_menu"));

		Chat_Window = new Chat(20,430,container);

		QUEST_DIALOG = new NpcDialog(512 - 250, 640 - 150 - 20, 500, 150);
		InventoryWindow = new InventoryWindow(600,200,250,250);
		StatusWindow = new StatusWindow(20,90,395,380);
		ShopWindow = new ShopWindow(20,90,320,350);
		AbilitiesWindow = new AbilitiesWindow(20,90,320,380);
		SkillWindow = new SkillWindow(20,90,320,400);
		MenuWindow = new MenuWindow(400,200,250,215);
		QuestWindow = new QuestWindow(680,100,320,400);
		OptionsWindow = new OptionsWindow(400,200,250,215);
		BountyWindow = new BountyWindow(container,400,200,250,220);
		PlayersWindow = new PlayersWindow(container, 680,100,320,400);
		ItemSplitterWindow = new ItemSplitterWindow(container,400,200,180,90);
		ContainerWindow = new ContainerWindow(200,200,0,0,true);
		EventWindow = new EventWindow();
		mostWantedWindow = new MostWantedWindow(305,100,440,440);
		MapWindow = new MapWindow(800,90,200,230,true);
		BookWindow = new BookWindow(600,150,300,400,true);
		CraftingWindow = new CraftingWindow(680,160,320,400);
		CharacterSkinWindow = new CharacterSkinWindow(680,160,320,400);
		CardBook = new CardBook(20,90);
		
		TutorialDialog = new TutorialDialog();
		
		
		ALL_WINDOWS.add(QUEST_DIALOG);
		ALL_WINDOWS.add(InventoryWindow);
		ALL_WINDOWS.add(StatusWindow);
		ALL_WINDOWS.add(ShopWindow);
		ALL_WINDOWS.add(AbilitiesWindow);
		ALL_WINDOWS.add(SkillWindow);
		ALL_WINDOWS.add(MenuWindow);
		ALL_WINDOWS.add(QuestWindow);
		ALL_WINDOWS.add(OptionsWindow);
		ALL_WINDOWS.add(BountyWindow);
		ALL_WINDOWS.add(PlayersWindow);
		ALL_WINDOWS.add(ItemSplitterWindow);
		ALL_WINDOWS.add(ContainerWindow);
		ALL_WINDOWS.add(EventWindow);
		ALL_WINDOWS.add(mostWantedWindow);
		ALL_WINDOWS.add(MapWindow);
		ALL_WINDOWS.add(BookWindow);
		ALL_WINDOWS.add(CraftingWindow);
		ALL_WINDOWS.add(TutorialDialog);
		ALL_WINDOWS.add(CharacterSkinWindow);
		ALL_WINDOWS.add(CardBook);
		
		closeAllWindows();

		MouseItem = new DragObject();

	}

	public static ActionBar getActionBar(){
		return ActionBar;
	}

	public static void loadActionbar(String data){
		ActionBar.clear();
		ActionBar.load(data);
	}

	public void draw(Graphics g, GameContainer app) {
		int mouseX = app.getInput().getAbsoluteMouseX();
		int mouseY = app.getInput().getAbsoluteMouseY();

		int mouseAlpha = 255;


		if(ScreenHandler.getActiveScreen() == ScreenType.WORLD){

			// Draw Screen effects
			for(Iterator<StatusScreenEffect> iter = ScreenFX.iterator();iter.hasNext();){ 
				StatusScreenEffect fx = iter.next();
				if(!fx.getRemoveMe()){
					fx.draw(g);
				}else{
					iter.remove();
				}
			}
			
			for(int i = 0; i < Notifications.size(); i++){
				Notification notif = Notifications.get(i);

				notif.draw(g);

				notif.update();

				if(!notif.isActive()){
					int changeY = 45 + notif.getNrTextLines()*15;
					NotificationsY -= changeY;
					for(int j = i; j < Notifications.size(); j++){
						Notifications.get(j).moveUp(changeY);
					}
				}
			}

			for(int i = 0; i < Notifications.size();i++){
				if(!Notifications.get(i).isActive()){
					Notifications.remove(i);
					i--;
				}
			}


			drawPlayerInfo(g, 20,20, mouseX, mouseY);

			if(!QUEST_DIALOG.isOpen() && !EventWindow.isOpen() && !BountyWindow.isOpen()){
				if(!PlayMusicMode){
					ActionBar.draw(g, mouseX, mouseY);
				}
			}

			drawBounty(g, 380, 25);

			CardButton.draw(g, mouseX, mouseY);
			CharacterButton.draw(g, mouseX, mouseY);
			AbilitiesButton.draw(g, mouseX, mouseY);
			InventoryButton.draw(g, mouseX, mouseY);
			SkillsButton.draw(g, mouseX, mouseY);
			QuestButton.draw(g, mouseX, mouseY);
			CommunityButton.draw(g, mouseX, mouseY);
			MenuButton.draw(g, mouseX, mouseY);

			if(!QUEST_DIALOG.isOpen() && !EventWindow.isOpen() && !BountyWindow.isOpen()){
				Chat_Window.draw(g,app,20,430);
			}

			if(MenuWindow.isVisible() || OptionsWindow.isVisible()){
				g.setColor(new Color(0,0,0,50));
				g.fillRect(0, 0, 1024, 768);
			}

			drawWindows(app, g, mouseX, mouseY);
			
			
			if(BlueSaga.playerCharacter != null){
				if(BlueSaga.playerCharacter.isDead()){
					drawGameOver(g);
				}
			}

			// CHANGE MOUSE CURSOR
			if(ScreenHandler.getActiveScreen() != ScreenType.LOGIN
					&& ScreenHandler.getActiveScreen() != ScreenType.CHARACTER_SELECT
					&& ScreenHandler.getActiveScreen() != ScreenType.CHARACTER_CREATE
					){

				String mousePos[] = BlueSaga.WORLD_MAP.getClickedTile(mouseX,mouseY,ScreenHandler.myCamera.getX(),ScreenHandler.myCamera.getY()).split(";");

				int mouseTileX = Integer.parseInt(mousePos[0]);
				int mouseTileY = Integer.parseInt(mousePos[1]);
				int mouseTileZ = BlueSaga.playerCharacter.getZ();

				if(mouseTileX > 0 && mouseTileY > 0){

					Creature attackTarget = null;

					boolean friendly = false;
					
					// CHECK IF MOUSE IS OVER CREATURE
					for(ScreenObject c: ScreenHandler.SCREEN_OBJECTS_DRAW){
						if(c.getType().equals("Creature")){
							// CHECK THAT CREATURE IS NOT PLAYER 
							if(!(c.getCreature().getX() == BlueSaga.playerCharacter.getX() && c.getY() == BlueSaga.playerCharacter.getY())){
								if(mouseX > c.getCreature().getScreenPixelX() - 10 && mouseX < c.getCreature().getScreenPixelX() + c.getCreature().getSizeWidthF()*ClientSettings.TILE_SIZE+10 
										&& mouseY > c.getCreature().getScreenPixelY() + 15 - 10 && mouseY < c.getCreature().getScreenPixelY() + c.getCreature().getSizeHeightF()*ClientSettings.TILE_SIZE + 15 + 10){
									
									if(c.getCreature().getCreatureType() == CreatureType.Monster){
										Npc npc = (Npc) c.getCreature();
										if((npc.getAggroType() < 4 || npc.isAggro()) && !npc.isDead()){
											if(npc.getAggroType() == 3 && !npc.isAggro()){
												friendly = true;
											}
											attackTarget = npc;
											mouseTileX = npc.getX();
											mouseTileY = npc.getY();
											break;
										}
									}else if(ScreenHandler.SCREEN_TILES.get(c.getCreature().getX()+","+c.getCreature().getY()+","+c.getCreature().getZ()) != null){
										if(ScreenHandler.SCREEN_TILES.get(c.getCreature().getX()+","+c.getCreature().getY()+","+c.getCreature().getZ()).getType().equals("arena") || c.getZ() <= -100){
											attackTarget = c.getCreature();
										}
									}
								}
							}
						}
					}


					if(!USE_ABILITY && !USE_SCROLL && attackTarget != null){
						if(friendly){
							Mouse.setType("Talk");
						}else{
							Mouse.setType("Attack");
						}
					}else if(USE_ABILITY){
						// ABILITY TO USE
						Ability ActiveAbility = ActionBar.getSelectedAbility();
						if(ActiveAbility != null){
							// CHECK RANGE
							if(Math.abs(mouseTileX-BlueSaga.playerCharacter.getX()) + Math.abs(mouseTileY-BlueSaga.playerCharacter.getY()) > ActiveAbility.getRange()){
								// Not in range
								mouseAlpha = 100;
							}
						}else{
							USE_ABILITY = false;
							Mouse.setType("Pointer");
						}
					}else if(!USE_SCROLL && ScreenHandler.SCREEN_OBJECTS_WITH_ID.get(mouseTileX+","+mouseTileY+","+mouseTileZ) != null){
						if(ScreenHandler.SCREEN_OBJECTS_WITH_ID.get(mouseTileX+","+mouseTileY+","+mouseTileZ).getObject() != null){
							if(!USE_ABILITY && ScreenHandler.SCREEN_OBJECTS_WITH_ID.get(mouseTileX+","+mouseTileY+","+mouseTileZ).getObject().getName().contains("container")
									|| ScreenHandler.SCREEN_OBJECTS_WITH_ID.get(mouseTileX+","+mouseTileY+","+mouseTileZ).getObject().getName().contains("gathering")
									|| ScreenHandler.SCREEN_OBJECTS_WITH_ID.get(mouseTileX+","+mouseTileY+","+mouseTileZ).getObject().getName().contains("crafting")){
								Mouse.setType("Pickup");
							}
						}
					}else if(!USE_SCROLL && !Mouse.getType().equals("Pointer")) {
						Mouse.setType("Pointer");
					}

					if(FishingHandler.drawFishingGame(g,app)){
						Mouse.setType("Pickup");
					}
				}
			}
		}

		if(ScreenHandler.getActiveScreen() != ScreenType.LOADING 
				&& ScreenHandler.getActiveScreen() != ScreenType.ERROR 
				&& ScreenHandler.getActiveScreen() != ScreenType.CUT_SCENE){
			if(BlueSaga.playerCharacter != null && BlueSaga.playerCharacter.isDead()){

			}else{
				MouseItem.draw(g, mouseX-10, mouseY-10);
				Mouse.draw(mouseX, mouseY, mouseAlpha);
			}
		}
	}

	private synchronized void drawWindows(GameContainer app, Graphics g, int mouseX, int mouseY){
		for(Window w: ALL_WINDOWS){
			w.draw(app, g,mouseX,mouseY);
		}
	}
	
	public void drawPlayerInfo(Graphics g, int x, int y, int mouseX, int mouseY){
		xp_bg.draw(x,y);

		g.setWorldClip(x+5,y+5+40-BlueSaga.playerCharacter.getXPBarWidth(40),40,BlueSaga.playerCharacter.getXPBarWidth(40));
		xp_meter.draw(x+5,y+5);
		g.clearWorldClip();

		int levelWidth = 0;
		levelWidth = Font.size20.getWidth(""+BlueSaga.playerCharacter.getLevel());

		g.setFont(Font.size20);
		g.setColor(new Color(0,0,0,150));
		g.drawString(""+BlueSaga.playerCharacter.getLevel(), x+27 - levelWidth/2, y+14);
		g.setColor(new Color(255,255,255,255));
		g.drawString(""+BlueSaga.playerCharacter.getLevel(), x+25 - levelWidth/2, y+12);

		meter_bg.draw(x+70,y+4);
		meter_bg.draw(x+70,y+29);

		// HEALTH
		int healthWidth = BlueSaga.playerCharacter.getHealthBarWidth(107);

		g.setWorldClip(x+75,y+8,healthWidth,20);
		hp_meter.draw(x+75,y+8);
		g.clearWorldClip();

		// HEALTH REGAIN
		int healthRegainWidth = healthWidth + BlueSaga.playerCharacter.getHealthRegainBarWidth(107);

		g.setWorldClip(x+75,y+8,healthRegainWidth,20);
		hp_meter.draw(x+75,y+8,new Color(255,255,255,100));
		g.clearWorldClip();

		// MANA
		int manaWidth = BlueSaga.playerCharacter.getManaBarWidth(107);
		g.setWorldClip(x+75,y+33,manaWidth,20);
		mana_meter.draw(x+75,y+33);
		g.clearWorldClip();

		// MANA REGAIN
		int manaRegainWidth = manaWidth + BlueSaga.playerCharacter.getManaRegainBarWidth(107);

		g.setWorldClip(x+75,y+33,manaRegainWidth,20);
		mana_meter.draw(x+75,y+33,new Color(255,255,255,100));
		g.clearWorldClip();

		g.setColor(new Color(255,255,255,200));
		g.setFont(Font.size8);

		int textX = x + 75 + Math.round(54 - Font.size8.getWidth(BlueSaga.playerCharacter.getHealthAsString()) / 2.0f);
		g.drawString(BlueSaga.playerCharacter.getHealthAsString(),textX,y+8);

		textX = x + 75 + Math.round(54 - Font.size8.getWidth(BlueSaga.playerCharacter.getManaAsString()) / 2.0f);
		g.drawString(BlueSaga.playerCharacter.getManaAsString(),textX,y+33);


		heart_big.draw(x+60,y+1);
		star_big.draw(x+60,y+24);

		int i = 0;
		for(StatusEffect SE: BlueSaga.playerCharacter.getStatusEffects()){
			int seX = x + i*40;
			int seY = y+60;


			g.setColor(new Color(255,255,255,200));
			float angle = 360.0f * ((float) SE.getDurationItr() / (float) SE.getDuration());

			g.fillArc(seX-4, seY-4, 38, 38, angle - 90, 270); 
			//ImageResource.getSprite("gui/world/statuseffect_badge").draw(seX, seY);

			g.setColor(new Color(SE.getColor().getRed(),SE.getColor().getGreen(),SE.getColor().getBlue(),150));
			g.fillOval(seX, seY, 30, 30); 

			ImageResource.getSprite("statuseffects/"+SE.getGraphicsNr()).getAnimation().getCurrentFrame().draw(seX - 10, seY - 10,50,50);
			i++;

			// DRAW TOOLTIP
			if(mouseX > seX && mouseX < seX + 30 && mouseY > seY && mouseY < seY + 30){
				if(SE.getName() != ""){
					int TextWidth = Font.size10.getWidth(SE.getName()) + 20;
					int TextHeight = Font.size10.getHeight(SE.getName()) + 20;

					g.setColor(new Color(BlueSagaColors.RED.getRed(),BlueSagaColors.RED.getGreen(),BlueSagaColors.RED.getBlue(),150));
					g.fillRoundRect(seX, seY + 40, TextWidth, TextHeight, 10);
					g.setColor(new Color(255,255,255));
					g.setFont(Font.size10);
					g.drawString(SE.getName(), seX+10, seY + 50);
				}
			}
		}
	}

	public void drawGameOver(Graphics g){
		Die_Label.draw(415,300);
		g.setColor(new Color(255,255,255,255));
		g.setFont(Font.size12);
		String gameOverString = "Press SPACE to respawn at your last checkpoint";
		g.drawString(gameOverString,512 - Font.size12.getWidth(gameOverString)/2,360);
	}


	public static boolean hasMessage(String message){
		for(Notification notif: Notifications){
			if(notif.getText().equals(message)){
				return true;
			}
		}
		return false;
	}
	
	public static void addMessage(String newMessage, Color bgColor) {
		// CHECK IF NOTIFICATION ALLREADY EXIST
		boolean duplicate = false;

		if(newMessage.contains("#")){
			String translatedMessage = "";
			String message_parts[] = newMessage.split("#");
			for(String part: message_parts){
				if(part.contains(".")){
					translatedMessage += LanguageUtils.getString(part);
				}else{
					translatedMessage += part;
				}
			}
			newMessage = translatedMessage;
		}
		
		for(Notification notif: Notifications){
			if(notif.getText().equals(newMessage)){
				duplicate = true;
				notif.addDuplicate();
				break;
			}
		}

		if(!duplicate){
			Notification newNotif;
			newNotif = new Notification(newMessage, NotificationsY, bgColor);
			NotificationsY += 45 + newNotif.getNrTextLines()*15;
			Notifications.add(newNotif);
		}
		Sfx.play("notifications/quest_updated");
	}

	public static void addRequest(String requestType, int requestId, String newMessage){
		// CHECK IF NOTIFICATION ALLREADY EXIST
		boolean duplicate = false;

		for(Notification notif: Notifications){
			if(notif.getText().equals(newMessage)){
				duplicate = true;
				notif.addDuplicate();
				break;
			}
		}

		if(!duplicate){
			RequestNotification newNotif = new RequestNotification(newMessage, requestType, requestId, NotificationsY);
			NotificationsY += 45 + newNotif.getNrTextLines()*15;
			Notifications.add(newNotif);
		}
		Sfx.play("notifications/quest_updated");
	}
	
	
	public static void cancelUseAbility(){
		if(USE_ABILITY){
			USE_ABILITY = false;
			ActionBar.cancelSelection();
			Mouse.setType("Pointer");
		}
		if(USE_SCROLL){
			USE_SCROLL = false;
			USE_SCROLL_ID = 0;
			USE_SCROLL_TYPE = "None";
			Gui.Mouse.setType("Pointer");
		}
	}

	public static int keyLogic(Input INPUT){

		if(!isInputActive()){
			if(INPUT.isKeyPressed(Input.KEY_ESCAPE)){
				if(closeAllWindows()){
					MenuWindow.open();
				}
			}

			ALL_WINDOWS.iterator();
			
			/*
			while(windowIterator.hasNext()){
				Window w = windowIterator.next();
				w.keyLogic(INPUT);
			}
			*/
			
			QUEST_DIALOG.keyLogic(INPUT);
			InventoryWindow.keyLogic(INPUT);
			StatusWindow.keyLogic(INPUT);
			ShopWindow.keyLogic(INPUT);
			AbilitiesWindow.keyLogic(INPUT);
			SkillWindow.keyLogic(INPUT);
			MenuWindow.keyLogic(INPUT);
			QuestWindow.keyLogic(INPUT);
			OptionsWindow.keyLogic(INPUT);
			BountyWindow.keyLogic(INPUT);
			PlayersWindow.keyLogic(INPUT);
			ItemSplitterWindow.keyLogic(INPUT);
			ContainerWindow.keyLogic(INPUT);
			EventWindow.keyLogic(INPUT);
			mostWantedWindow.keyLogic(INPUT);
			MapWindow.keyLogic(INPUT);
			BookWindow.keyLogic(INPUT);
			CraftingWindow.keyLogic(INPUT);
			TutorialDialog.keyLogic(INPUT);
			CharacterSkinWindow.keyLogic(INPUT);
			CardBook.keyLogic(INPUT);
			
			Chat_Window.keyLogic(INPUT, BlueSaga.client);


			if(INPUT.isKeyPressed(Input.KEY_SPACE)){
			
				if(BlueSaga.playerCharacter.isResting()){
					BlueSaga.client.sendMessage("rest","stop");
				}else{
					if(ScreenHandler.SCREEN_TILES.get(BlueSaga.playerCharacter.getX()+","+BlueSaga.playerCharacter.getY()+","+BlueSaga.playerCharacter.getZ()) != null){
						if(!ScreenHandler.SCREEN_TILES.get(BlueSaga.playerCharacter.getX()+","+BlueSaga.playerCharacter.getY()+","+BlueSaga.playerCharacter.getZ()).getType().equals("indoors")){
							if(BlueSaga.playerCharacter.getStat("HEALTH_REGAIN") == 0 && BlueSaga.playerCharacter.getStat("MANA_REGAIN") == 0){
								Gui.addMessage(LanguageUtils.getString("messages.quest.need_eat_first"), BlueSagaColors.RED);
							}
						}
						BlueSaga.client.sendMessage("rest","start");
					}
				}
			}	

/*
			if(INPUT.isKeyPressed(Input.KEY_M)){
				//BlueSaga.MIDI_PLAYER.stopAllNotes();
				if(PlayMusicMode){
					PlayMusicMode = false;
				}else{
					PlayMusicMode = true;
				}
			}
*/
			
			
			
			return ActionBar.keyLogic(INPUT);

		}else if(ItemSplitterWindow.isOpen()){
			ItemSplitterWindow.keyLogic(INPUT, BlueSaga.client);
		}else if(BountyWindow.isOpen()){
			BountyWindow.keyLogic(INPUT);
		}

		if(INPUT.isKeyPressed(Input.KEY_ESCAPE)){
			closeAllWindows();
		}
		
		return 10;
	}





	/*
	 * 
	 * 	MENU
	 * 
	 * 
	 */



	public static boolean leftMouseClick(Input INPUT, boolean DoAction){
		boolean clickedGUI = true;

		int mouseX = INPUT.getAbsoluteMouseX();
		int mouseY = INPUT.getAbsoluteMouseY();

		// TOP MENU
		if(CardButton.isClicked(mouseX,mouseY)){
			if(DoAction){
				CardButton.toggle();
				CardBook.toggle();
				if(CardBook.isOpen()){
					BlueSaga.client.sendMessage("card_book", "info");
				}
			}
		}else if(CharacterButton.isClicked(mouseX, mouseY)){
			if(DoAction){
				CharacterButton.toggle();
				StatusWindow.toggle();
				if(StatusWindow.isOpen()){
					BlueSaga.client.sendMessage("statuswindow", "info");
				}
			}
		}else if(InventoryButton.isClicked(mouseX, mouseY)){
			if(DoAction){
				InventoryButton.toggle();
				InventoryWindow.toggle();
				if(InventoryWindow.isOpen()){
					BlueSaga.client.sendMessage("inventory", "info");
				}
			}
		}else if(AbilitiesButton.isClicked(mouseX, mouseY)){
			if(DoAction){
				AbilitiesButton.toggle();
				AbilitiesWindow.toggle();
			}
		}else if(SkillsButton.isClicked(mouseX, mouseY)){
			if(DoAction){
				SkillsButton.toggle();
				SkillWindow.toggle();
			}
		}else if(QuestButton.isClicked(mouseX, mouseY)){
			if(DoAction){
				if(!QuestWindow.isOpen()){
					BlueSaga.client.sendMessage("myquests", "info");
				}
				QuestWindow.toggle();
				QuestButton.toggle();
			}
		}else if(CommunityButton.isClicked(mouseX, mouseY)){
			if(DoAction){
				PlayersWindow.toggle();
				CommunityButton.toggle();
			}
		}else if(MenuButton.isClicked(mouseX, mouseY)){
			if(DoAction){
				MenuButton.toggle();
				MenuWindow.toggle();
			}
		}else{
			clickedGUI = false;
		
			// Check if clicked any request notifications
			for(Notification notif: Notifications){
				if(notif.isActive()){
					if(notif.isRequest()){
						RequestNotification requestNotif = (RequestNotification) notif;
						if(requestNotif.clickedAccept(mouseX,mouseY)){
							clickedGUI = true;
							break;
						}else if(requestNotif.clickedDenied(mouseX, mouseY)){
							clickedGUI = true;
							break;
						}
					}
				}
			}
		}

		
		// WINDOWS
		if(!clickedGUI){
			Collections.reverse(ALL_WINDOWS);

			for(Window w: ALL_WINDOWS){
				if(w.clickedOn(mouseX, mouseY)){
					clickedGUI = true;
					if(DoAction){
						w.leftMouseClick(INPUT);
					}
					break;
				}
			}

			sortWindows();
		}

		if(!clickedGUI){
			if(ActionBar.isClicked(mouseX, mouseY)){
				clickedGUI = true;
				if(DoAction){
					ActionBar.leftMouseClick(mouseX, mouseY);
				}
			}
		}	
		
		if(!clickedGUI && DoAction){
			clickedGUI = Chat_Window.leftMouseClick(mouseX, mouseY);
		}


		if(!clickedGUI){
			if(MouseItem.getAbility() != null){
				MouseItem.clear();
				clickedGUI = true;
			}
		}

		if(!clickedGUI && DoAction){

			WalkHandler.OPEN_CONTAINER = false;
			if(ContainerWindow.isOpen()){
				ContainerWindow.close();
				CharacterSkinWindow.close();
			}

			String mousePos[] = BlueSaga.WORLD_MAP.getClickedTile(mouseX,mouseY,ScreenHandler.myCamera.getX(),ScreenHandler.myCamera.getY()).split(";");

			int tileX = Integer.parseInt(mousePos[0]);
			int tileY = Integer.parseInt(mousePos[1]);
			//int tileZ = BlueSaga.playerCharacter.getZ();

			if(BlueSaga.playerCharacter.isResting()){
				BlueSaga.playerCharacter.setResting(false);
				BlueSaga.client.sendMessage("rest","stop");
			}
			
			if(USE_ABILITY){
				clickedGUI = true;
				// ABILITY TO USE
				Ability ActiveAbility = ActionBar.getSelectedAbility();
				// CHECK RANGE
				if(Math.abs(tileX-BlueSaga.playerCharacter.getX()) + Math.abs(tileY-BlueSaga.playerCharacter.getY()) <= ActiveAbility.getRange()){
					if(AbilityHandler.readyToUseAbility()){
						BlueSaga.client.sendMessage("use_ability", tileX+","+tileY+","+ActiveAbility.getAbilityId());
					}
				}
			}else if(USE_SCROLL){
				if(USE_SCROLL_TYPE.equals("Ability")){
					clickedGUI = true;
					BlueSaga.client.sendMessage("use_scroll", tileX+","+tileY+","+USE_SCROLL_ID+","+USE_SCROLL_LOCATION);
				}
			}else {
				if(MouseItem.getItem() != null){
					clickedGUI = true;
					BlueSaga.client.sendMessage("drop_item",""+MouseItem.getItem().getId());
					MouseItem.clear();
				}else if (DoAction){
					if(ScreenHandler.SCREEN_TILES.get(tileX+","+tileY+","+BlueSaga.playerCharacter.getZ()) != null){
						ScreenHandler.SCREEN_TILES.get(tileX+","+tileY+","+BlueSaga.playerCharacter.getZ()).showWalkTo();
					}
				}
			}
		}

		return clickedGUI;
	}


	public static boolean rightMouseClick(Input INPUT){
		boolean clickedGUI = false;

		int mouseX = INPUT.getAbsoluteMouseX();
		int mouseY = INPUT.getAbsoluteMouseY();


		if(USE_ABILITY || USE_SCROLL){
			cancelUseAbility();
			clickedGUI = true;
		}else{
			//Collections.reverse(ALL_WINDOWS);

			for(Window w: ALL_WINDOWS){
				if(w.clickedOn(mouseX, mouseY)){
					clickedGUI = true;
					w.rightMouseClick(INPUT);
					break;
				}
			}

			sortWindows();
		}
		return clickedGUI;
	}


	/*
	 * 
	 * 
	 * 
	 */

	public static boolean isInputActive(){
		if(BountyWindow.isOpen() || Chat_Window.isActive() || ItemSplitterWindow.isOpen() || (PlayersWindow.isOpen() && (PlayersWindow.getActiveView().equals("ADD FRIEND") || PlayersWindow.getActiveView().equals("ADD TO PARTY")))){
			return true;
		}
		return false;
	}

	public boolean isWindowOnTop(int winDepthZ){
		if(winDepthZ == MaxWindowsZ){
			return true;
		}
		return false;
	}

	public static boolean closeAllWindows(){
		boolean allClosed = true;

		for(Window w: ALL_WINDOWS){
			if(w.isOpen() && !w.getName().equals("Tutorial Dialog")){
				w.close();
				allClosed = false;
			}
		}
		
		if(Chat_Window.isActive()){
			Chat_Window.setActive(false);
			allClosed = false;
		}
		return allClosed;
	}

	public static void closeWalkWindows(){
		QUEST_DIALOG.close();
		mostWantedWindow.close();
		ContainerWindow.close();
		CraftingWindow.close();
		CharacterSkinWindow.close();
	}

	public static void closeInputWindows(){
		PlayersWindow.close();
		BountyWindow.close();
		ItemSplitterWindow.close();
	}

	public static boolean isMovingWindows(){
		return MovingWindows;
	}

	public static void stopMoveWindows(){
		for(Window w: ALL_WINDOWS){
			w.stopMove();
		}
		Gui.MovingWindows = false;
	}

	/*
	 * 
	 * 	SORT WINDOWS
	 * 
	 */

	public static class WindowsDepthComparator implements Comparator<Window> {
		@Override
		public int compare(Window w1, Window w2) {
			return w1.getDepthZ() - w2.getDepthZ();
		}
	}

	public static synchronized void sortWindows(){
		Collections.sort(ALL_WINDOWS, new WindowsDepthComparator());
	}

	/*
	 * 
	 * 	BOUNTY VIEWER
	 * 
	 * 
	 */
	public static void showBountyChange(int playerBounty, int change){
		Sfx.play("notifications/gold");
		
		SHOW_BOUNTY = true;

		bountyAniCounter = 0;

		oldBounty = playerBounty;
		newBounty = playerBounty;

		bountyChange = change;
		bountyOpacity = 0;
		BlueSaga.playerCharacter.setBounty(newBounty-bountyChange);
		bountyTimer = new Timer();
	}

	public void drawBounty(Graphics g, int x, int y){
		if(SHOW_BOUNTY){
			bountyAniCounter++;
			g.setFont(Font.size30);
			bountyBg.draw(x,y,new Color(255,255,255,bountyOpacity));


			if(bountyOpacity < 255){
				bountyOpacity += 8;
			}else{
				if(newBounty != oldBounty + bountyChange && oldBounty + bountyChange / Math.abs(bountyChange) > 0){
					newBounty += bountyChange / Math.abs(bountyChange);
				}else{
					bountyTimer.schedule( new TimerTask(){
						@Override
						public void run() {
							SHOW_BOUNTY = false;
						}
					}, 3000);
				}
			}

			if(bountyOpacity < 255){
				g.setColor(new Color(0,0,0,bountyOpacity));
			}else{
				if(bountyChange > 0){
					g.setColor(new Color(128, 215, 97, 255));
					if(bountyAniCounter % 20 < 10){
						arrow_up.draw(x+20,y+ 18 + 5);
						arrow_up.draw(x+236,y+ 18 + 5);
					}else{
						arrow_up.draw(x+20,y+ 18);
						arrow_up.draw(x+236,y+ 18);
					}
				}else if(bountyChange < 0){
					g.setColor(new Color(236, 86, 86, 255));
					if(bountyAniCounter % 20 < 10){
						arrow_down.draw(x+20,y+ 18 + 5);
						arrow_down.draw(x+236,y+ 18 + 5);
					}else{
						arrow_down.draw(x+20,y+ 18);
						arrow_down.draw(x+236,y+ 18);
					}
				}
			}

			int textX = Math.round(x + 145 - Font.size30.getWidth(newBounty+"   ") / 2);
			g.drawString(newBounty+"   ", textX, y + 30);

			ImageResource.getSprite("gui/skulls/silver").getImage().draw(textX + Font.size30.getWidth(newBounty+"   ") / 2 + 40,y + 22);
			
		}else if(bountyOpacity > 0){
			g.setFont(Font.size30);
			bountyBg.draw(x,y,new Color(255,255,255,bountyOpacity));
			g.setColor(new Color(0,0,0,bountyOpacity));

			int textX = Math.round(x + 145 - Font.size30.getWidth(newBounty+"   ") / 2);

			g.drawString(newBounty+"   ", textX, y + 30);
			ImageResource.getSprite("gui/skulls/silver").getImage().draw(textX + Font.size30.getWidth(newBounty+"   ") / 2 + 40,y + 22, 40, 40, new Color(255,255,255,bountyOpacity));

			bountyOpacity -= 8;
		}
	}


	public void addScreenEffect(StatusScreenEffect screenFX){
		ScreenFX.add(screenFX);
	}






	/*
	 * 
	 * 	ACTIONBAR
	 * 
	 * 
	 */



	public void hideActionBar(){
		ActionBar.setVisible(false);
	}

	public void showActionBar() {
		ActionBar.setVisible(true);
	}


	public boolean isPlayMusicMode() {
		return PlayMusicMode;
	}


	public void setPlayMusicMode(boolean playMusicMode) {
		PlayMusicMode = playMusicMode;
	}

}
