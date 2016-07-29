package game;

import org.newdawn.slick.Animation;
import org.newdawn.slick.Color;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

public class Sprite {

	private Image image;
	private Animation animation;
	private boolean Animated;

	public Sprite(String filename, int nrFrames) {
		Animated = true;
		Image[] graphics;

		if(nrFrames <= 2){
			graphics = new Image[nrFrames];
		}else{
			graphics = new Image[nrFrames*2 - 2];
		}
		
		try {
			int k = 0;
			for (int i = 0; i < nrFrames; i++) {
				graphics[k] = new Image(filename + "_" + i + ".png");
				k++;
			}
			
			if(nrFrames > 2){
				for(int i = nrFrames-2; i > 0; i--){
					graphics[k] = new Image(filename + "_" + i + ".png");
					k++;
				}
			}
		} catch (SlickException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int duration = 1000 / nrFrames;
		
		if(nrFrames > 2){
			duration = 1000 / ((nrFrames*2)-2);
		}
		animation = new Animation(graphics, duration, true);
	}

	public Sprite(String filename) {
		Animated = false;
		try {
			image = new Image(filename+".png");
		} catch (SlickException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Animation getAnimation(){
		return animation;
	}
	
	public Image getImage(){
		return image;
	}
	

	public void draw(int x, int y) {
		if (Animated) {
			animation.draw(x, y);
		} else {
			image.draw(x, y);
		}
	}
	
	public void draw(int x, int y, Color color) {
		if (Animated) {
			animation.draw(x, y, color);
		} else {
			image.draw(x, y, color);
		}
	}

	public void draw(int x, int y, int width, int height, Color color) {
		if (Animated) {
			animation.draw(x, y, width, height, color);
		} else {
			image.draw(x, y, width, height, color);
		}
	}

	
}
