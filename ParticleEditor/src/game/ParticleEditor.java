package game;

import game.Emitter.Emitter;
import game.Emitter.EmitterContainer;
import game.Emitter.EmitterManager;
import game.Emitter.EmitterType;
import game.Particle.ParticleContainer;
import game.Streak.Streak;
import game.Streak.StreakContainer;
import game.Streak.StreakType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.geom.Vector2f;

public class ParticleEditor extends BasicGame {

	// INIT AND RESOLUTION
	static private int SCREEN_WIDTH = 1024;
	static private int SCREEN_HEIGHT = 700;
	static private boolean FULL_SCREEN = false;
	static private final int FRAME_RATE = 60;

	private static Database myGameDB;
	private static Input INPUT;
	public static ImageResource GFX;
	private Camera myCamera;
	private EmitterManager myEmitterManager;
	private Emitter myEmitter;
	private String myEmitterName;
	private TextField myEmitterTextField;
	private Image myImage;
	private int myLeftBorderWidth;
	private WarningTextManager myWarningTextManager;
	
	public static EmitterContainer myEmitterContainer;
	public static ParticleContainer myParticleContainer;
	public static StreakContainer myStreakContainer;
	
	public ParticleEditor() {
		super("Particle Editor");
	}

	public void init(GameContainer container) throws SlickException {

		try {
			myGameDB = new Database("../BPClientMouse/gameDB.db");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		GFX = new ImageResource();
		GFX.load();
		
		myCamera = new Camera();

		InitiateContainers(myGameDB);
		myEmitterManager = new EmitterManager();
		
		ResultSet emitterResult = myGameDB.askDB("select * from Emitter order by id desc limit 1");

		try {
			myEmitterName = emitterResult.getString("Name");
			emitterResult.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		myLeftBorderWidth = 200;
		myEmitterTextField = new TextField("Emitter name", 10, 40,myLeftBorderWidth - 30, myEmitterName, container);
		myImage = GFX.getSprite("border").getImage();

		myWarningTextManager = new WarningTextManager(10, 80, container);
		
		Vector2f pos = new Vector2f(SCREEN_WIDTH/2,SCREEN_HEIGHT/2);
		StreakType streakType = new StreakType();
		streakType.myQuality = 1.0f;
		streakType.myLifetime = 1.5f;
		streakType.myStartColor = new Color(255, 0, 0, 0);
		streakType.myEndColor = new Color(100, 0, 150, 150);
		streakType.myWidth = 5.0f;
	}

	public void update(GameContainer container, int delta)
			throws SlickException {
		float elapsedTime = delta / 1000.0f;

		myCamera.Update(elapsedTime);
		myEmitterManager.Update(elapsedTime);
		myWarningTextManager.Update(elapsedTime);
		
		keyLogic(container, elapsedTime);

	}

	public void render(GameContainer container, Graphics g)
			throws SlickException {

		myEmitterManager.Draw(g, myCamera);
		myImage.draw(0, 0, myLeftBorderWidth, SCREEN_HEIGHT);
		myEmitterTextField.Render(container, g);
		myWarningTextManager.Render(container, g);
		
		g.drawString("Left Mouse Button:\nCreate emitter", 10, 100);
		g.drawString("Right Mouse Button:\nMove emitter", 10, 150);
		g.drawString("R Button:\nRemove all emitters", 10, 200);

	}

	public static void main(String[] args) {
		try {

			AppGameContainer app = new AppGameContainer(new ParticleEditor());
			app.setDisplayMode(SCREEN_WIDTH, SCREEN_HEIGHT, FULL_SCREEN);
			app.setTargetFrameRate(FRAME_RATE);
			app.setShowFPS(true);
			app.setAlwaysRender(true);
			app.setVSync(true);

			app.start();
		} catch (SlickException e) {
			e.printStackTrace();
		}
	}

	private void keyLogic(GameContainer GC, float aElapsedTime) {

		INPUT = GC.getInput();
		
		float mouseX = INPUT.getAbsoluteMouseX() - myCamera.x;
		float mouseY = INPUT.getAbsoluteMouseY() - myCamera.y;
		
		myCamera.HandleInput(INPUT, aElapsedTime);

		//if (mouseX > myLeftBorderWidth) {
			
			
			if (INPUT.isMousePressed(Input.MOUSE_LEFT_BUTTON)) {
				try {
					getTextFieldValue();
					createNewEmitter(mouseX, mouseY);
				} catch (SQLException e) {
					myWarningTextManager.ShowText("Wrong name");
					e.printStackTrace();
				}
			}

			if(myEmitter != null) {
				if (INPUT.isMouseButtonDown(Input.MOUSE_RIGHT_BUTTON)) {
					myEmitter.SetPosition(mouseX, mouseY);
				}
			}
			
		//}

		if (INPUT.isKeyPressed(Input.KEY_ENTER)) {
			getTextFieldValue();
		}

		if (INPUT.isKeyPressed(Input.KEY_ESCAPE)) {
			GC.exit();
		}

		if (INPUT.isKeyPressed(Input.KEY_R)) {
			reset();
		}

		INPUT.clearKeyPressedRecord();
	}

	private void createNewEmitter(float aX, float aY) throws SQLException {

		EmitterType emitterType = new EmitterType();
		ResultSet emitterResult = myGameDB.askDB("select * from Emitter where name = '"+myEmitterName+"'");
		myEmitterContainer.GetEmitterTypeFromDB(emitterType, emitterResult);
		emitterResult.close();
		myEmitter = myEmitterManager.SpawnEmitter(aX, aY, emitterType);

	}

	private void reset() {
		myEmitterManager.RemoveAllEmitters();
	}

	private void getTextFieldValue() {
		String newEmitterName = myEmitterTextField.GetText();
		myEmitterName = newEmitterName;
	}

	public static float randomFloat(float aMinValue, float aMaxValue,
			Random aRandom) {
		int maxRand = 30000;
		int rand = aRandom.nextInt(maxRand);
		float randomFloat = (float) (rand) / (float) (maxRand);
		float dif = aMaxValue - aMinValue;
		float result = aMinValue + (randomFloat * dif);
		return result;
	}
	
	private void InitiateContainers(Database aDatabase) {
		myParticleContainer = new ParticleContainer(myGameDB);
		myStreakContainer = new StreakContainer(myGameDB);
		myEmitterContainer = new EmitterContainer(myGameDB);
	}

}