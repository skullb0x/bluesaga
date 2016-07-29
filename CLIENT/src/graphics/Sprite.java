package graphics;

import game.BlueSaga;

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

		if(nrFrames <= 2 || filename.contains("largewindow")){
			graphics = new Image[nrFrames];
		}else{
			graphics = new Image[nrFrames*2 - 2];
		}
		
		try {
			int k = 0;
			for (int i = 0; i < nrFrames; i++) {
				graphics[k] = new Image(filename + "_" + i + ".png");
				graphics[k].setFilter(Image.FILTER_NEAREST);
				k++;
			}
			
			if(nrFrames > 2 && !filename.contains("largewindow")){
				for(int i = nrFrames-2; i > 0; i--){
					graphics[k] = new Image(filename + "_" + i + ".png");
					graphics[k].setFilter(Image.FILTER_NEAREST);
					k++;
				}
			}
		} catch (SlickException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int duration = 800 / nrFrames;
		
		if(nrFrames > 2){
			duration = 800 / ((nrFrames*2)-2);
		}
		animation = new Animation(graphics, duration, true);
	}

	
	public Sprite(String filename) {
		Animated = false;
		try {
			image = new Image(filename+".png");
			image.setFilter(Image.FILTER_NEAREST);
		} catch (SlickException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	public boolean isAnimated(){
		return Animated;
	}
	
	public Animation getAnimation(){
		return animation;
	}
	
	public Image getImage(){
		return image;
	}
	
	public void draw(int x, int y) {
		if (Animated) {
			if(animation.getFrameCount() == 2){
				if(BlueSaga.updateTimeItr % 60 < 30){
					animation.getImage(0).draw(x,y);
				}else{
					animation.getImage(1).draw(x,y);
				}
			}else{
				animation.draw(x, y);
			}
		} else {
			image.draw(x, y);
		}
	}
	
	public void drawCentered(int x, int y){
		if(Animated){
			animation.updateNoDraw();
			if(animation.getFrameCount() == 2){
				if(BlueSaga.updateTimeItr % 60 < 30){
					animation.getImage(0).draw(x,y);
				}else{
					animation.getImage(1).draw(x,y);
				}
			}else{
				animation.draw(x, y);
			}
		}else{
			image.drawCentered(x,y);
		}
	}
	
	public void draw(int x, int y, Color color) {
		if (Animated) {
			if(animation.getFrameCount() == 2){
				if(BlueSaga.updateTimeItr % 60 < 30){
					animation.getImage(0).draw(x,y, color);
				}else{
					animation.getImage(1).draw(x,y,color);
				}
			}else{
				animation.draw(x, y, color);
			}
		} else {
			image.draw(x, y, color);
		}
	}

	public void draw(int x, int y, int width, int height, Color color) {
		if (Animated) {
			if(animation.getFrameCount() == 2){
				if(BlueSaga.updateTimeItr % 60 < 30){
					animation.getImage(0).draw(x, y, width, height, color);
				}else{
					animation.getImage(1).draw(x, y, width, height, color);
				}
			}else{
				animation.draw(x, y, width, height, color);
			}
		} else {
			image.draw(x, y, width, height, color);
		}
	}	
	
	public void draw(int x, int y, float scale) {
		if (Animated) {
			if(animation.getFrameCount() == 2){
				if(BlueSaga.updateTimeItr % 60 < 30){
					animation.getImage(0).getScaledCopy(scale).draw(x, y);
				}else{
					animation.getImage(1).getScaledCopy(scale).draw(x, y);
				}
			}else{
				animation.getCurrentFrame().getScaledCopy(scale).draw(x, y);
			}
		} else {
			image.getScaledCopy(scale).draw(x, y);
		}
	}
}
