package movies;

import java.util.Vector;

import game.BlueSaga;
import game.ClientSettings;
import graphics.Font;
import graphics.ImageResource;
import screens.ScreenHandler;
import screens.ScreenHandler.ScreenType;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import creature.Creature;
import utils.LanguageUtils;

public class CardMovie extends AbstractMovie {

	private Creature player;
	
	private float playerX = 512 - 25;
	private float playerY = 320.0f;
	
	private float cardStartY = 340;
	
	private float fade = 0.0f;
	
	private Vector<Integer> cards = new Vector<Integer>();
	
	public CardMovie(Creature newPlayer){
		player = newPlayer;
	
		for(int cardId = 315; cardId < 335; cardId++){
			cards.add(cardId);
		}
		
		canSkip = false;
		
		setDuration(1100);
	}
	
	public void play(){
		super.play();
		BlueSaga.BG_MUSIC.stop();
	}

	public void draw(Graphics g){
		update();

		if(isActive()){
			super.draw(g);
			g.setWorldClip(getX(),getY(),getWidth(),getHeight());
			
			// Move player upwards
			if(getTimeItr() < 100){
				playerY += 1.0f;
			}
			
			// Draw player
			player.draw(g, (int) playerX, (int) playerY, new Color(255,255,255,fade));
			
			// Fade in
			if(getTimeItr() < 50){
				fade = getTimeItr() / 50.0f;
			}else {
				// Animate cards
				
				int cardNr = 0;
				for(Integer card: cards){
					int cardX = (int) (playerX - 25 + 50.0f * (-2.5f + (cardNr % 5))); 
					int cardY = (int) (cardStartY - 200.0f + 50.0f * (float) (Math.floor(cardNr /5))); 
					
					float cardFade = (getTimeItr()*4 - (cardNr+1)*50) / 50.0f;
					ImageResource.getSprite("items/item"+card).draw(cardX, cardY, new Color(255,255,255,cardFade));
					
					cardNr++;
				}
				
			}
			
			g.clearWorldClip();

			int textY = 470;
			
			// Text
			if(getTimeItr() > 50 && getTimeItr() < 1100){
				g.setColor(new Color(255,255,255));
				g.setFont(Font.size30);
				
				String text = player.getName()+" "+LanguageUtils.getString("movies.cards.collect_cards");
				int textX = 512 - Font.size30.getWidth(text)/2;
				
				g.drawString(text,textX,textY);
		
				textY += 64;
				text = LanguageUtils.getString("movies.cards.congratulations");
				textX = 512 - Font.size30.getWidth(text)/2;
				g.drawString(text,textX,textY);
				
				textY += 32;
				text = LanguageUtils.getString("movies.cards.world_change");
				textX = 512 - Font.size30.getWidth(text)/2;
				g.drawString(text,textX,textY);
		
			}
			
			if(fadeAlpha > 0){
				if(skipped){
					fadeAlpha += 4;
				}else{
					fadeAlpha++;
				}
				g.setColor(new Color(0,0,0,fadeAlpha));
				g.fillRect(0, 0, ClientSettings.SCREEN_WIDTH, ClientSettings.SCREEN_HEIGHT);
			}
			
		}else{
			ScreenHandler.setActiveScreen(ScreenType.WORLD);
		}
	}
	
	public void update(){
		super.update();
		if(!isActive()){
			player.setAnimationColor(new Color(255,255,255));
			BlueSaga.BG_MUSIC.stop();
		}
	}
	
}
