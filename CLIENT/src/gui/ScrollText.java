package gui;

import graphics.Font;

import java.util.Timer;
import java.util.TimerTask;

import org.newdawn.slick.Graphics;

public class ScrollText {

	private int x;
	private int y;
	
	private boolean scrolling;
	private float scrollY;
	private char scrollDir;
	
	private String text;
	
	private int width;
	private int height;
	
	private int textHeight;
	
	private int waitTime = 8000; // in milliseconds
	
	private Timer waitTimer;
	
	public ScrollText(String newText, int newX, int newY, int newWidth, int newHeight){
		x = newX;
		y = newY;
		
		text = newText;
		width = newWidth;
		height = newHeight;
		
		scrollY = 0;
		scrollDir = 'U';
		
		scrolling = false;
		waitTimer = new Timer();
		
		textHeight = Font.size12.getHeight(text);
		
		waitTimer.schedule( new TimerTask(){
			@Override
			public void run() {
				scrolling = true;
			}
	      }, waitTime);
	}
	
	
	public void draw(Graphics g){
	
		if(scrolling && textHeight > height){
			if(scrollDir == 'U'){
				scrollY -= 0.1f;
				if(height > textHeight + Math.round(scrollY)){
					scrollDir = 'D';
					scrolling = false;
					waitTimer.schedule( new TimerTask(){
						@Override
						public void run() {
							scrolling = true;
						}
				      }, waitTime);
				}
			}else if(scrollDir == 'D'){
				scrollY += 0.1f;
				if(Math.round(scrollY) == 0){
					scrollDir = 'U';
					scrolling = false;
					waitTimer.schedule( new TimerTask(){
						@Override
						public void run() {
							scrolling = true;
						}
				      }, waitTime);
				}
			}
		}
		
		g.setWorldClip(x,y,width,height);
		
		g.drawString(text,x,y+Math.round(scrollY));
	
		g.clearWorldClip();
		
	}
	
	
	
	
	
}
