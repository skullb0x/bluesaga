package screens;

import game.BlueSaga;
import game.Database;
import game.ClientSettings;
import graphics.BlueSagaColors;
import graphics.Font;
import graphics.ImageResource;
import gui.Gui;

import java.util.*;

import map.AreaEffect;
import map.NightEffect;
import map.ScreenObject;
import map.Tile;
import map.TileObject;
import movies.AbstractMovie;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;

import components.TargetingSystem;
import abilitysystem.Ability;
import particlesystem.EmitterManager;
import particlesystem.Emitter.EmitterContainer;
import particlesystem.Particle.ParticleContainer;
import particlesystem.Streak.StreakContainer;
import projectile.ProjectileManager;
import utils.LanguageUtils;
import utils.MathUtils;
import data_handlers.FishingHandler;
import data_handlers.MapHandler;
import data_handlers.WalkHandler;

public class ScreenHandler {

	private static ScreenType activeScreen = ScreenType.LOADING;

	public static Camera myCamera;

	// LOADING SCREEN
	private static Image LoadingLogo;
	public static String LoadingStatus;

	// MOUSE
	private static boolean clickedOnce = false;

	// WORLD VIEW
	public static boolean FADE_SCREEN;
	public static int FADE_ALPHA;

	// AREA EFFECT
	public static AreaEffect AREA_EFFECT;
	public static NightEffect NIGHT_EFFECT;

	// SCREEN DATA
	public static HashMap<String, Tile> SCREEN_TILES = new HashMap<String, Tile>();
	public static List<Tile> SCREEN_TILES_B = new ArrayList<Tile>();

	public static HashMap<String, ScreenObject> SCREEN_OBJECTS_WITH_ID = new HashMap<String, ScreenObject>();
	public static List<ScreenObject> SCREEN_OBJECTS_DRAW = new ArrayList<ScreenObject>();

	// PROJECTILES
	public static ProjectileManager ProjectileManager;

	// PARTICLE SYSTEM
	public static StreakContainer myStreakContainer;
	public static ParticleContainer myParticleContainer;
	public static EmitterContainer myEmitterContainer;
	public static EmitterManager myEmitterManager;

	// MOVIE
	public static AbstractMovie cutScene;

	public enum ScreenType{
		LOADING,
		ERROR,
		LOGIN,
		REGISTER,
		CHARACTER_CREATE,
		CHARACTER_SELECT,
		CHARACTER_DELETE,
		CHARACTER_OPTIONS,
		CHANGE_PASSWORD,
		CUT_SCENE,
		WORLD,
	}
	
	public static void init(GameContainer app){
		myCamera = new Camera(ClientSettings.SCREEN_WIDTH, ClientSettings.SCREEN_HEIGHT);

		AREA_EFFECT = new AreaEffect();

		LoadingLogo = ImageResource.getSprite("gamelogo").getImage();
		LoadingStatus = LanguageUtils.getString("ui.status.connecting");

		InitiateContainers(BlueSaga.gameDB);

		LoginScreen.init(app, 315, 300);
		CharacterSelection.init(app);
		CharacterCreate.init(app);

		NIGHT_EFFECT = new NightEffect();
		NIGHT_EFFECT.start();
	}

	public static void ready(){
		clickedOnce = false;
	}

	public static void draw(Graphics g, GameContainer app){


		int mouseX = app.getInput().getAbsoluteMouseX();
		int mouseY = app.getInput().getAbsoluteMouseY();


		if(getActiveScreen() == ScreenType.LOGIN 
				|| getActiveScreen() == ScreenType.CHARACTER_SELECT
				|| getActiveScreen() == ScreenType.CHARACTER_CREATE
				){
			StartScreen.draw();
		}

		if(getActiveScreen() == ScreenType.LOADING || getActiveScreen() == ScreenType.ERROR){
			g.setColor(new Color(0,0,0,255));
			g.fillRect(0, 0, ClientSettings.SCREEN_WIDTH, ClientSettings.SCREEN_HEIGHT);
			LoadingLogo.draw(322,120);
			g.setFont(Font.size20);
			g.setColor(new Color(255,255,255,255));

			g.drawString(LoadingStatus, 512-Font.size20.getWidth(LoadingStatus)/2, 350);
			g.drawString("v 0."+ClientSettings.VERSION_NR, 900, 600);
		}else if(getActiveScreen() == ScreenType.LOGIN){
			LoginScreen.draw(g, app);
			LoadingLogo.draw(322,120);
		}else if(getActiveScreen() == ScreenType.CHARACTER_SELECT){
			CharacterSelection.draw(g, app);
		}else if(getActiveScreen() == ScreenType.CHARACTER_CREATE){
			CharacterCreate.draw(g,app);
		}else if(getActiveScreen() == ScreenType.CUT_SCENE){
			if(cutScene.isActive()){
				cutScene.draw(g);
			}
		}else if(BlueSaga.playerCharacter != null) {

			// Draw background
			if(BlueSaga.playerCharacter.getShip() != null && BlueSaga.playerCharacter.getZ() == 0){
				ImageResource.getSprite("effects/void").draw(0, 0);
			}else if(BlueSaga.playerCharacter.getZ() < 10){
				g.setColor(new Color(0,0,0));
				g.fillRect(0,0,ClientSettings.SCREEN_WIDTH,ClientSettings.SCREEN_HEIGHT);
			}else if(BlueSaga.playerCharacter.getZ() >= 10){
				ImageResource.getSprite("effects/clouds").draw(0, 0);
				ImageResource.getSprite("effects/clouds").draw(0, 0, ScreenHandler.AREA_EFFECT.getTintColor());
			}

			// Draw tiles
			Tile TILE;
			int z = BlueSaga.playerCharacter.getZ();

			for(int x = BlueSaga.playerCharacter.getX() - ClientSettings.TILE_HALF_W; x < BlueSaga.playerCharacter.getX() + ClientSettings.TILE_HALF_W + 1; x++){
				for(int y = BlueSaga.playerCharacter.getY() - ClientSettings.TILE_HALF_H; y < BlueSaga.playerCharacter.getY() + ClientSettings.TILE_HALF_H + 1; y++){
					TILE = SCREEN_TILES.get(x+","+y+","+z);
					if(TILE != null){
						int cameraX = myCamera.getX();
						int cameraY = myCamera.getY();


						int renderPosX = TILE.getX()*ClientSettings.TILE_SIZE + cameraX - 25;
						int renderPosY = TILE.getY()*ClientSettings.TILE_SIZE + cameraY - 25;

						TILE.draw(renderPosX,renderPosY,g);
					}
				}	
			}

			// Draw AoE
			if(Gui.USE_ABILITY){
				if(Gui.getActionBar().getSelectedAbility() != null){
					Ability activeAbility = Gui.getActionBar().getSelectedAbility();

					// Show AoE
					String AoE = activeAbility.getAoE();
					if(!AoE.equals("None")){
						String mousePos[] = BlueSaga.WORLD_MAP.getClickedTile(mouseX,mouseY, myCamera.getX(), myCamera.getY()).split(";");

						int mouseTileX = Integer.parseInt(mousePos[0]);
						int mouseTileY = Integer.parseInt(mousePos[1]);

						String AoE_info[] = AoE.split(";");

						g.setColor(new Color(activeAbility.getColor().getRed(),activeAbility.getColor().getGreen(),activeAbility.getColor().getBlue(),100));
						for(String dXY_info: AoE_info){
							String dXY[] = dXY_info.split(",");
							int dX = Integer.parseInt(dXY[0]);
							int dY = Integer.parseInt(dXY[1]);

							int cameraX = myCamera.getX();
							int cameraY = myCamera.getY();

							int renderPosX = (mouseTileX + dX)*ClientSettings.TILE_SIZE + cameraX - 25;
							int renderPosY = (mouseTileY + dY)*ClientSettings.TILE_SIZE + cameraY - 25;

							g.fillRect(renderPosX, renderPosY, ClientSettings.TILE_SIZE, ClientSettings.TILE_SIZE);
						}
					}
				}
			}

			// Draw particles
			myEmitterManager.Draw(g, myCamera);

			// Draw screen objects such as trees and creatures
			for(ScreenObject c: SCREEN_OBJECTS_DRAW){
				if(c != null){
					int cameraX = myCamera.getX();
					int cameraY = myCamera.getY();

					int renderPosX = c.getX()*ClientSettings.TILE_SIZE + cameraX - 25;
					int renderPosY = c.getY()*ClientSettings.TILE_SIZE + cameraY - 25;

					if(c.getType().equals("Creature")){
						renderPosX += c.getCreature().MyWalkHandler.getMoveX() + 25;
						renderPosY += c.getCreature().MyWalkHandler.getMoveY() + 15;
					}
					c.draw(g, renderPosX, renderPosY);
				}
			}

			// Fades screen
			if(FADE_SCREEN){
				if(FADE_ALPHA < 255){
					FADE_ALPHA += 4;
				}
				g.setColor(new Color(0,0,0,FADE_ALPHA));
				g.fillRect(0, 0, ClientSettings.SCREEN_WIDTH, ClientSettings.SCREEN_HEIGHT);
			}else{
				if(FADE_ALPHA > 0){
					FADE_ALPHA -= 4;

					g.setColor(new Color(0,0,0,FADE_ALPHA));
					g.fillRect(0, 0, ClientSettings.SCREEN_WIDTH, ClientSettings.SCREEN_HEIGHT);
				}
			}

			// Draw projectiles
			ProjectileManager.draw();

			// Draw area effect
			AREA_EFFECT.draw(g);

			// Draws game over screen
			if(BlueSaga.playerCharacter.isDead()){
				g.setColor(new Color(255,0,0,150));
				g.fillRect(0, 0, 1024, 640);
			}

			// Draws resting fade
			BlueSaga.playerCharacter.drawRestingFade();

			//GatheringHandler.draw(g,myCamera.getX(),myCamera.getY(),mouseX,mouseY);

			// DEBUG
			if(ClientSettings.DEV_MODE){
				g.setFont(Font.size10);
				g.setColor(BlueSagaColors.WHITE);
				g.drawString("X,Y: "+BlueSaga.playerCharacter.getX()+", "+BlueSaga.playerCharacter.getY()+", "+BlueSaga.playerCharacter.getZ(),500,10);

				String mousePos[] = BlueSaga.WORLD_MAP.getClickedTile(mouseX,mouseY, myCamera.getX(), myCamera.getY()).split(";");

				g.drawString("Mouse: "+mousePos[0]+","+mousePos[1], 500, 30);
			}
		}
	}

	private static void InitiateContainers(Database aDatabase) {
		myStreakContainer = new StreakContainer(BlueSaga.gameDB);
		myParticleContainer = new ParticleContainer(BlueSaga.gameDB);
		myEmitterContainer = new EmitterContainer(BlueSaga.gameDB);
	}


	public static void update(float elapsedTime){

		if(myEmitterManager != null){
			myEmitterManager.Update(elapsedTime);
		}

		if(getActiveScreen() == ScreenType.WORLD){
			ScreenHandler.updateCamera(elapsedTime);
		}
	}



	public static void keyLogic(Input INPUT){

		if(getActiveScreen() == ScreenType.LOGIN){

			LoginScreen.keyLogic(INPUT);

		}else if(getActiveScreen() == ScreenType.CHARACTER_SELECT){

			CharacterSelection.keyLogic(INPUT);

		}else if(getActiveScreen() == ScreenType.CHARACTER_CREATE){

			CharacterCreate.keyLogic(INPUT);

		}else if(getActiveScreen() == ScreenType.ERROR){

			if(INPUT.isKeyPressed(Input.KEY_ESCAPE)){
				BlueSaga.close();
			}

		}else if(getActiveScreen() == ScreenType.CUT_SCENE){
			if(INPUT.isKeyPressed(Input.KEY_ESCAPE)){
				cutScene.skipMovie();
			}
		}else if(getActiveScreen() != ScreenType.LOADING && BlueSaga.playerCharacter != null){
			if(!BlueSaga.playerCharacter.isDead()){
				boolean clickedGUI = false;

				if(INPUT.isMousePressed(0)){
					clickedGUI = Gui.leftMouseClick(INPUT, true);
					if(clickedGUI){
						clickedOnce = true;
					}
				}

				if(!clickedGUI && !clickedOnce && INPUT.isMouseButtonDown(0)){
					clickedGUI = Gui.leftMouseClick(INPUT,false);

					if(!clickedGUI){
						String mousePos[] = BlueSaga.WORLD_MAP.getClickedTile(INPUT.getAbsoluteMouseX(),INPUT.getAbsoluteMouseY(),myCamera.getX(),myCamera.getY()).split(";");

						int tileX = Integer.parseInt(mousePos[0]);
						int tileY = Integer.parseInt(mousePos[1]);
						int tileZ = BlueSaga.playerCharacter.getZ();

						if(SCREEN_TILES.containsKey(tileX+","+tileY+","+tileZ)){
							BlueSaga.playerCharacter.setGoToTarget(false);
							WalkHandler.findPath(tileX,tileY,tileZ, "PlayerWalk");
						}
					}
				}


				if(!INPUT.isMouseButtonDown(0)){
					clickedOnce = false;
					if(Gui.isMovingWindows()){
						Gui.stopMoveWindows();
					}
				}

				/*
				 * 
				 * 		RIGHT MOUSE CLICK
				 * 			 	  
				 */

				if(INPUT.isMousePressed(1)){

					clickedGUI = Gui.rightMouseClick(INPUT);

					int mouseX = INPUT.getAbsoluteMouseX();
					int mouseY = INPUT.getAbsoluteMouseY();

					if(Gui.getActionBar().rightMouseClick(mouseX, mouseY)){
						clickedGUI = true;
					}

					if(!clickedGUI){
						WalkHandler.OPEN_CONTAINER = false;

						String mousePos[] = BlueSaga.WORLD_MAP.getClickedTile(INPUT.getAbsoluteMouseX(),INPUT.getAbsoluteMouseY(), myCamera.getX(), myCamera.getY()).split(";");

						int tileX = Integer.parseInt(mousePos[0]);
						int tileY = Integer.parseInt(mousePos[1]);

						// Check containers
						ScreenObject so = SCREEN_OBJECTS_WITH_ID.get(tileX+","+tileY+","+BlueSaga.playerCharacter.getZ());
						if(so != null){
							TileObject Container = so.getObject();

							// WALK UP TO CONTAINER
							if(Container != null){
								if(Container.getName().contains("crafting") || Container.getName().contains("gathering") || Container.getName().contains("container")){
									if(Gui.ContainerWindow.isOpen()){
										Gui.ContainerWindow.close();
									}
									WalkHandler.OPEN_CONTAINER = true;
									WalkHandler.OPEN_CONTAINER_ID = Container.getName()+","+tileX+","+tileY+","+BlueSaga.playerCharacter.getZ();

									if(BlueSaga.playerCharacter.isResting()){
										BlueSaga.playerCharacter.setResting(false);
										BlueSaga.client.sendMessage("rest","stop");
									}
									
									WalkHandler.findPath(tileX,tileY,BlueSaga.playerCharacter.getZ(),"PlayerAttack");
									if(WalkHandler.walkPath.size() > 0){
										WalkHandler.walkPath.remove(WalkHandler.walkPath.size()-1);
									}
								}
								// WANTED POSTER
								if(Container.getName().contains("wanted")){
									BlueSaga.client.sendMessage("getmostwanted", "info");
								}
							}
						}

						BlueSaga.client.sendMessage("settarget",tileX+";"+tileY);

						FishingHandler.catchFish(tileX,tileY);

					}

				}

				Gui.keyLogic(INPUT);


				if(!Gui.isInputActive()){

					if(getActiveScreen() == ScreenType.WORLD){

						// Target Monster
						if(INPUT.isKeyPressed(Input.KEY_T)){
							TargetingSystem.findClosestTarget(false);
						}

						// Target Player
						if(INPUT.isKeyPressed(Input.KEY_F)){
							TargetingSystem.findClosestTarget(true);
						}
						
						if(!MapHandler.FADED_SCREEN){

							int dirX = 0;
							int dirY = 0;

							if((INPUT.isKeyDown(Input.KEY_S) && INPUT.isKeyDown(Input.KEY_A)) || (INPUT.isKeyDown(Input.KEY_DOWN) && INPUT.isKeyDown(Input.KEY_LEFT))) {
								dirX = -1;
								dirY = 1;
							}else if((INPUT.isKeyDown(Input.KEY_S) && INPUT.isKeyDown(Input.KEY_D)) || (INPUT.isKeyDown(Input.KEY_DOWN) && INPUT.isKeyDown(Input.KEY_RIGHT))) {
								dirX = 1;
								dirY = 1;
							}else if((INPUT.isKeyDown(Input.KEY_W) && INPUT.isKeyDown(Input.KEY_A)) || (INPUT.isKeyDown(Input.KEY_UP) && INPUT.isKeyDown(Input.KEY_LEFT))) {
								dirX = -1;
								dirY = -1;
							}else if((INPUT.isKeyDown(Input.KEY_W) && INPUT.isKeyDown(Input.KEY_D)) || (INPUT.isKeyDown(Input.KEY_UP) && INPUT.isKeyDown(Input.KEY_RIGHT))) {
								dirX = 1;
								dirY = -1;
							}else if (INPUT.isKeyDown(Input.KEY_A) || INPUT.isKeyDown(Input.KEY_LEFT)) {
								dirX = -1;
								dirY = 0;
							}else if(INPUT.isKeyDown(Input.KEY_D) || INPUT.isKeyDown(Input.KEY_RIGHT)) {
								dirX = 1;
								dirY = 0;
							}else if(INPUT.isKeyDown(Input.KEY_W) || INPUT.isKeyDown(Input.KEY_UP)) {
								dirX = 0;
								dirY = -1;
							}else if(INPUT.isKeyDown(Input.KEY_S) || INPUT.isKeyDown(Input.KEY_DOWN)) {
								dirX = 0;
								dirY = 1;
							}

							if(dirX != 0 || dirY != 0){
								WalkHandler.walkPath.clear();

								BlueSaga.playerCharacter.setGoToTarget(false);

								if(!BlueSaga.playerCharacter.MyWalkHandler.isWalking()){
									if(WalkHandler.walkPath.size() > 0){
										WalkHandler.walkPath.clear();
									}
									int gotoX = BlueSaga.playerCharacter.getX()+dirX;
									int gotoY = BlueSaga.playerCharacter.getY()+dirY;

									if(BlueSaga.playerCharacter.hasStatusEffect(27)){
										gotoX = BlueSaga.playerCharacter.getX() - dirX;
										gotoY = BlueSaga.playerCharacter.getY() - dirY;
									}

									float newAngle = MathUtils.angleBetween(-dirX, -dirY);

									if(INPUT.isKeyDown(Input.KEY_LSHIFT)){
										// Only change direction if shift is pressed
										if(BlueSaga.playerCharacter.getGotoRotation() != newAngle){
											BlueSaga.client.sendMessage("changedir", ""+newAngle);
										}
									}else{
										if(WalkHandler.canWalk(gotoX, gotoY)){
											BlueSaga.client.sendMessage("canwalk",dirX+":"+dirY);
										}else{
											if(BlueSaga.playerCharacter.getGotoRotation() != newAngle){
												BlueSaga.client.sendMessage("changedir", ""+newAngle);
											}
										}
									}
								}
							}
						}

						if(INPUT.isKeyPressed(Input.KEY_ENTER)){
							Gui.Chat_Window.setActive(true);
						}
					}
				}else {
					Gui.Chat_Window.keyLogic(INPUT,BlueSaga.client);
				}
			}else{
				if(INPUT.isKeyPressed(Input.KEY_SPACE)){
					BlueSaga.client.sendMessage("player_respawn","true");
					BlueSaga.restartLogoutTimer(0);
					
					ScreenHandler.setActiveScreen(ScreenType.LOADING);
					ScreenHandler.setLoadingStatus(LanguageUtils.getString("ui.status.loading"));
				}
			}
		}
		INPUT.clearKeyPressedRecord();
	}

	public static void updateCamera(float elapsedTime){
		float cameraX = -BlueSaga.playerCharacter.getX()*ClientSettings.TILE_SIZE - BlueSaga.playerCharacter.MyWalkHandler.getMoveX();
		float cameraY = -BlueSaga.playerCharacter.getY()*ClientSettings.TILE_SIZE - BlueSaga.playerCharacter.MyWalkHandler.getMoveY();

		myCamera.SetCenterPosition(cameraX, cameraY);
		myCamera.Update(elapsedTime);
	}

	public static ScreenType getActiveScreen() {
		return activeScreen;
	}

	public static void setActiveScreen(ScreenType newActiveScreen) {
		activeScreen = newActiveScreen;
	}


	public static String getLoadingStatus() {
		return LoadingStatus;
	}

	public static void setLoadingStatus(String loadingStatus) {
		LoadingStatus = loadingStatus;
	}
}