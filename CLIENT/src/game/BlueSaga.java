package game;

import graphics.Font;
import graphics.ImageResource;
import gui.Gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import map.ScreenObject;
import map.WorldMap;
import network.Client;
import network.ClientReceiverThread;
import screens.LoginScreen;
import screens.ScreenHandler;
import screens.ScreenHandler.ScreenType;
import data_handlers.*;

import org.newdawn.slick.BasicGame; 
import org.newdawn.slick.GameContainer; 
import org.newdawn.slick.Graphics; 
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException; 
import org.newdawn.slick.AppGameContainer; 

import abilitysystem.Ability;
import abilitysystem.StatusEffect;
import sound.BgMusic;
import sound.Sfx;
import utils.GameInfo;
import utils.LanguageUtils;
import utils.json.JSONObject;
import components.DebugOutput;
import creature.PlayerCharacter;

public class BlueSaga extends BasicGame {


	public static DebugOutput DEBUG = new DebugOutput(ClientSettings.DEV_MODE);

	// GAME STATES
	public static boolean HAS_QUIT = false;

	public static long updateTimeItr = 0;
	public static int logoutTime = 20;
	public static int playerKillerLogoutTime = 60*5;
	public static int logoutTimeItr = 0;

	public static Database gameDB;

	// CONTROL
	public static Input INPUT;

	// SCREEN DATA
	public static WorldMap WORLD_MAP; 
	
	// PLAYER
	public static PlayerCharacter playerCharacter;
	public static int lastPlayedDbId = 0;
	
	// GUI
	public static Gui GUI;
	public static boolean actionServerWait = false;
	public static boolean LoginSuccess = false;

	// NETWORK
	public static Client client;
	private String serverData;
	public static ClientReceiverThread reciever;
	public static boolean ServerCheck = false;

	// MUSIC & SFX
	public static BgMusic BG_MUSIC; 

	public static AppGameContainer app;
	private static Timer closeTimer;
	
	public BlueSaga() { 
		super("Blue Saga v0."+ClientSettings.VERSION_NR); 
	}
	
	@Override
	public void init(GameContainer container) throws SlickException {
		
		INPUT = container.getInput();
				
		// Load translation
		File jarFile = new File(BlueSaga.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		String translation_path = jarFile.getAbsolutePath() + "/../assets/languages/game_text.txt";
		JSONObject translationJSON = ClientSettings.loadTranslationLanguageFile(translation_path);
		if(translationJSON != null){
			LanguageUtils.loadJson(translationJSON,false);
		}
		
		// Load original english text
		JSONObject originalJSON = ClientSettings.loadOriginalLanguageFile();
		LanguageUtils.loadJson(originalJSON,true);
		
		loading(container);
	}

	public void loading(GameContainer container) throws SlickException {
		// Connect to DB
		try {
			gameDB = new Database();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		Font.load();

		GameInfo.load();
		
		ImageResource.load();
		
		DataHandlers.init();
		
		ScreenHandler.init(container);
		
		// LOAD SOUNDS
		BG_MUSIC = new BgMusic();
		Sfx.load(gameDB);

		// NETWORK INIT
		client = new Client();

		// LOAD OPTIONS
		gameDB.loadOptions();

		ServerCheck = false;

		// GUI INIT
		GUI = new Gui();
		GUI.init(container);
		
		WORLD_MAP = new WorldMap();

		container.setMouseCursor("images/gui/cursors/cursor_hidden.png", 5, 5);

		if(ClientSettings.DEV_MODE){
			ClientSettings.MUSIC_ON = false;
			ClientSettings.SFX_ON = false;
		}
		
		BG_MUSIC.changeSong("title","title");
		ScreenHandler.setActiveScreen(ScreenType.LOGIN);
		
	}

	@SuppressWarnings("deprecation")
	public static void stopClient(){
		HAS_QUIT = true;
		reciever.stop();
		ServerCheck = false;
	}

	public static void chooseServer(String ServerName){
		LoginScreen.clickedLogin = true;
		
		
		if(client.init().equals("error")){
			ScreenHandler.setActiveScreen(ScreenType.ERROR);
			ScreenHandler.setLoadingStatus("Can't connect to server!");
			LoginScreen.clickedLogin = false;
		}else{
			BlueSaga.HAS_QUIT = false;

			reciever = new ClientReceiverThread(client.in_answer);
			reciever.start();

			ServerCheck = true;
			client.resetPacketId();
			client.sendMessage("connection","hello");
		}
	}


	public static void reconnect() {

		if(client.init().equals("error")){
			reciever.lostConnection = true;
			ScreenHandler.LoadingStatus = "Failed to reconnect with server!";
			reciever.startReconnectCountdown();
		}else{
			reciever = new ClientReceiverThread(client.in_answer);
			reciever.start();

			ServerCheck = true;

			client.sendMessage("connection","hello");
		}
	}

	public void serverCheck() {
		if(ServerCheck){
			serverData = reciever.getInfo();

			DataHandlers.handleData(serverData);
		}
	}


	@Override
	public void update(GameContainer container, int delta) throws SlickException {
		
		float elapsedTime = (delta) / 1000.0f;

		updateTimeItr++;

		if(updateTimeItr % 60 == 0){
			// Every 1000 ms

			if(client.connected){
				// Send keep alive packet
				client.sendKeepAlive();
			}
			// UPDATE LOGOUT TIMER
			if(logoutTimeItr > 0){
				logoutTimeItr--;
				if(logoutTimeItr == 0){
					Gui.getActionBar().updateSoulstone();
				}
			}

			// UPDATE STATUSEFFECTS
			if(playerCharacter != null){
				for(Iterator<StatusEffect> iter = playerCharacter.getStatusEffects().iterator();iter.hasNext();){  
					StatusEffect s = iter.next();
					if(!s.increaseDurationItr(1)){
						iter.remove();
						playerCharacter.updateBonusStats();
					}
				}
			}
		}

		if(updateTimeItr % 12 == 0){
			// Every 200 ms

			if(playerCharacter != null){
				// UPDATE ABILITY COOLDOWN

				for(Iterator<Ability> iter = playerCharacter.getAbilities().iterator();iter.hasNext();){  
					Ability a = iter.next();
					if(a != null){
						a.cooldown();
					}
				}
			}

		}

		if(updateTimeItr % 6 == 0){
			// Every 100 ms
			
			AbilityHandler.updateAbilityCooldown();
			
			// update monster rotation
			for(ScreenObject c: ScreenHandler.SCREEN_OBJECTS_DRAW){
				if(c != null){
					if(c.getType().equals("Creature")){
						c.getCreature().updateRotation();
					}
				}
			}
		}

		// CHECK IF NEW INFO FROM SERVER HAS COME
		serverCheck();

		ScreenHandler.update(elapsedTime);

		if(ScreenHandler.getActiveScreen() == ScreenType.WORLD){
			WalkHandler.updatePlayerWalk(playerCharacter);
		}

		keyLogic(container, elapsedTime);
	}


	@Override
	public void render(GameContainer container, Graphics g) throws SlickException { 
		
		Font.loadGlyphs();

		ScreenHandler.draw(g,container);
		GUI.draw(g, container);
		
	}


	public static void main(String[] args) { 

		if(args.length>0 && "-dev".equals(args[0])) {
		  ClientSettings.DEV_MODE = true;
		}
		
		if(!ClientSettings.DEV_MODE){
			// CRASH REPORTS
			Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
				@Override
				public void uncaughtException(Thread t, Throwable e) {
					Calendar cal = Calendar.getInstance();
					SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");

					String filename = "libs/crashlogs/crashlog_"+sdf.format(cal.getTime())+".txt";

					PrintStream writer;
					try {
						writer = new PrintStream(filename, "UTF-8");
						writer.println(e.getClass() + ": " + e.getMessage());
						for (int i = 0; i < e.getStackTrace().length; i++) {
							writer.println(e.getStackTrace()[i].toString());
						}
					} catch (FileNotFoundException e1) {
						e1.printStackTrace();
					} catch (UnsupportedEncodingException e1) {
						e1.printStackTrace();
					}
				}
			});
		}
		if(!ClientSettings.DEV_MODE){
			System.setProperty("org.lwjgl.librarypath", new File("libs/").getAbsolutePath());
		}

		try { 

			app = new AppGameContainer(new BlueSaga()); 

			app.setDisplayMode(ClientSettings.SCREEN_WIDTH,ClientSettings.SCREEN_HEIGHT,ClientSettings.FULL_SCREEN);
			app.setTargetFrameRate(ClientSettings.FRAME_RATE);
			app.setShowFPS(ClientSettings.DEV_MODE);
			app.setAlwaysRender(true);
			app.setVSync(false);
			
			app.setIcons(new String[] {
			          "images/icons/16x16.png",
			          "images/icons/24x24.png",
			          "images/icons/32x32.png",
			          "images/icons/64x64.png",
			          "images/icons/128x128.png"
			         });
			
			app.start();
		} catch (SlickException e) { 
			e.printStackTrace(); 
		} 
	}

	/*
	 * 
	 * 	KEYBOARD & MOUSE
	 * 
	 */

	private void keyLogic(GameContainer GC, float aElapsedTime) {
		INPUT = GC.getInput();
		
		if(INPUT.isKeyPressed(Input.KEY_F1)){
			toggleFullscreen(app);
		}
		
		ScreenHandler.keyLogic(INPUT);
	}



	public static void restartLogoutTimer(int logoutTime){
		if(BlueSaga.playerCharacter.isResting()){
			BlueSaga.playerCharacter.setResting(false);
			BlueSaga.client.sendMessage("rest","stop");
		}
		
		logoutTimeItr = logoutTime;
		
		Gui.getActionBar().updateSoulstone();
	}

	public static void logoutTimerOk(){
		logoutTimeItr = 0;
	}



	public static void toggleFullscreen(AppGameContainer app){
		ClientSettings.toggleFullScreen();
		try {
			app.setDisplayMode(ClientSettings.SCREEN_WIDTH, ClientSettings.SCREEN_HEIGHT, ClientSettings.FULL_SCREEN);
		} catch (SlickException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static void close(){
		BlueSaga.client.closeConnection();
		gameDB.closeDB();
		
		closeTimer = new Timer();
		
		closeTimer.schedule( new TimerTask(){
			@Override
			public void run() {
				System.exit(0);
			}
		}, 1000);		
	}
}