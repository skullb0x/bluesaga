package movies;

import game.BlueSaga;
import graphics.ImageResource;
import screens.ScreenHandler;
import screens.ScreenHandler.ScreenType;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import components.Item;
import creature.Creature;
import creature.PlayerCharacter;
import sound.Sfx;

public class IntroMovie extends AbstractMovie {

	private int shipX;
	private int shipY;

	private int monsterX;
	private int monsterY;

	private int cloud1X;
	private int cloud1Y;

	private int cloud2X;
	private int cloud2Y;

	private PlayerCharacter yourChar;

	private Creature crewMember1;
	private Creature crewMember2;
	private Creature crewMember3;


	public IntroMovie(int charId){

		yourChar = new PlayerCharacter(charId, 0,0,0);
		yourChar.MyEquipHandler.updateItemCoordinates();
		yourChar.setLookRight(true);

		crewMember1 = new Creature(0,0,0);
		crewMember1.setType(2);
		crewMember1.MyEquipHandler.updateItemCoordinates();
		crewMember1.setLookRight(true);

		Item headItem1 = new Item(15);
		headItem1.setType("Head");

		Item weaponItem1 = new Item(120);
		weaponItem1.setType("Weapon");

		crewMember1.MyEquipHandler.equipItem(headItem1);
		crewMember1.MyEquipHandler.equipItem(weaponItem1);

		// SVAMP
		crewMember2 = new Creature(0,0,0);
		crewMember2.setType(21);
		crewMember2.MyEquipHandler.updateItemCoordinates();
		crewMember2.setLookRight(true);

		Item headItem2 = new Item(81);
		headItem2.setType("Head");

		Item weaponItem2 = new Item(10);
		weaponItem2.setType("Weapon");

		Item offHandItem2 = new Item(85);
		offHandItem2.setType("OffHand");

		crewMember2.MyEquipHandler.equipItem(headItem2);
		crewMember2.MyEquipHandler.equipItem(weaponItem2);
		crewMember2.MyEquipHandler.equipItem(offHandItem2);


		crewMember3 = new Creature(0,0,0);
		crewMember3.setType(79);
		crewMember3.MyEquipHandler.updateItemCoordinates();
		crewMember3.setLookRight(true);

		Item headItem3 = new Item(124);
		headItem3.setType("Head");

		crewMember3.MyEquipHandler.equipItem(headItem3);


		Item headItem = new Item(153);
		headItem.setType("Head");

		yourChar.MyEquipHandler.equipItem(headItem);

		if(yourChar.getCreatureId() == 19 || yourChar.getCreatureId() == 33){
			// Mage
			Item weaponItem = new Item(74);
			weaponItem.setType("Weapon");

			Item artifactItem = new Item(45);
			artifactItem.setType("Artifact");

			yourChar.MyEquipHandler.equipItem(weaponItem);
			yourChar.MyEquipHandler.equipItem(artifactItem);

		}else if(yourChar.getCreatureId() == 12 || yourChar.getCreatureId() == 31){
			// Warrior
			Item weaponItem = new Item(118);
			weaponItem.setType("Weapon");

			Item offHandItem = new Item(33);
			offHandItem.setType("OffHand");

			yourChar.MyEquipHandler.equipItem(weaponItem);
			yourChar.MyEquipHandler.equipItem(offHandItem);
		}else if(yourChar.getCreatureId() == 3 || yourChar.getCreatureId() == 61 || yourChar.getCreatureId() == 27){
			// Hunter
			Item weaponItem = new Item(65);
			weaponItem.setType("Weapon");

			Item artifactItem = new Item(46);
			artifactItem.setType("Artifact");

			yourChar.MyEquipHandler.equipItem(weaponItem);
			yourChar.MyEquipHandler.equipItem(artifactItem);
		}

		setDuration(2900);
		shipX = getX() - 300;
		shipY = getY() + 155;



		monsterX = getX() + 375;
		monsterY = 450;

		cloud1X = getX() + getWidth() + 100;
		cloud1Y = getY() + 50;

		cloud2X = getX() + getWidth() + 600;
		cloud2Y = getY() + 100;
	}

	public void play(){
		super.play();
		BlueSaga.BG_MUSIC.playSong("intro");
	}

	public void draw(Graphics g){
		update();

		if(isActive()){
			super.draw(g);
			g.setWorldClip(getX(),getY(),getWidth(),getHeight());

			g.setColor(new Color(109,200,237));
			g.fillRect(getX(),getY(),getWidth(),getHeight());

			if(getTimeItr() % 2 == 0){
				cloud1X--;
				cloud2X--;
			}

			if(getTimeItr() < 1400){
				// Move ship forward
				if(getTimeItr() % 4  == 0){
					shipX++;
				}
				if(getTimeItr() % 64 == 0){
					shipY += 4;
				}else if(getTimeItr() % 32 == 0){
					shipY -= 4;
				}
			}else if(getTimeItr() < 1420){
				// Ship collision
				if(getTimeItr() == 1400){
					Sfx.play("story/shipcrash");
				}

				if(getTimeItr() % 4  == 0){
					shipX -= 5;
				}else if(getTimeItr() % 2 == 0){
					shipX += 5;
				}
			}else if(getTimeItr() < 1650){
				// Monster shows up
				if(getTimeItr() == 1420){
					Sfx.play("story/seamonster_growl");
				}

				monsterY--;

			}else if(getTimeItr() > 1700 && getTimeItr() < 2500){
				if(getTimeItr() == 1701){
					Sfx.play("story/sinking_ship");
				}
				
				if(getTimeItr() % 26 == 0){
					crewMember1.setLookRight(false);
				}else if(getTimeItr() % 13 == 0){
					crewMember1.setLookRight(true);
				}
				
				if(getTimeItr() % 28 == 0){
					crewMember2.setLookRight(true);
				}else if(getTimeItr() % 14 == 0){
					crewMember2.setLookRight(false);
				}
				
				if(getTimeItr() % 24 == 0){
					crewMember3.setLookRight(false);
				}else if(getTimeItr() % 12 == 0){
					crewMember3.setLookRight(true);
				}
				
				if(getTimeItr() % 30 == 0){
					yourChar.setLookRight(true);
				}else if(getTimeItr() % 15 == 0){
					yourChar.setLookRight(false);
				}

				// Ship is sinking
				if(getTimeItr() % 4  == 0){
					shipY++;
				}
				if(getTimeItr() % 32 == 0){
					shipY += 4;
				}else if(getTimeItr() % 16 == 0){
					shipY -= 4;
				}
			}

			if(getTimeItr() == 2200){
				endMovie();
			}


			ImageResource.getSprite("startscreen/cloud1").draw(cloud1X,cloud1Y);
			ImageResource.getSprite("startscreen/cloud2").draw(cloud2X,cloud2Y);

			ImageResource.getSprite("story/sea1").draw(getX(), getY()+235);

			// SHIP SPAR TOP
			ImageResource.getSprite("story/spar_top").draw(shipX+120, shipY-100);

			// KOALA
			int crewMember3X = shipX + 120;
			int crewMember3Y = shipY - 85;

			crewMember3.draw(g, crewMember3X, crewMember3Y,null);

			// SHIP SPAR
			ImageResource.getSprite("story/spar").draw(shipX+80, shipY-70);

			// MUSHROOM
			int crewMember2X = shipX + 150;
			int crewMember2Y = shipY + 34;

			crewMember2.draw(g, crewMember2X, crewMember2Y,null);

			// SHIP
			ImageResource.getSprite("story/ship").draw(shipX, shipY);


			// YOUR CHARACTER
			int playerX = shipX + 250;
			int playerY = shipY + 12;
			yourChar.draw(g, playerX, playerY,null);

			// WINGED BLOB
			int crewMember1X = shipX + 30;
			int crewMember1Y = shipY - 25;

			crewMember1.draw(g, crewMember1X, crewMember1Y,null);

			ImageResource.getSprite("story/monster").draw(monsterX, monsterY);

			ImageResource.getSprite("story/sea").draw(getX(), getY()+246);

			if(fadeAlpha > 0){
				if(skipped){
					fadeAlpha += 4;
				}else{
					fadeAlpha++;
				}
				g.setColor(new Color(0,0,0,fadeAlpha));
				g.fillRect(getX(), getY(), getWidth(), getHeight());
			}

			g.clearWorldClip();
		}else{
			BlueSaga.BG_MUSIC.stop();
			ScreenHandler.setActiveScreen(ScreenType.WORLD);
		}
	}

	public void update(){
		super.update();
		if(!isActive()){
			BlueSaga.BG_MUSIC.stop();
		}
	}


}
