package map;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

public class NightEffect {
	private boolean Tint = false;
	private Color TintColor = new Color(255,255,255);
	private Color TintObjectColor = new Color(255,255,255);
	
	private int TintAlpha = 0;
	
	
	public NightEffect() {

	}
	
	
	public void draw(Graphics g) {
		if(Tint){
			if(TintAlpha < 255){
				TintAlpha++;
				TintColor = new Color(TintColor.getRed(),TintColor.getGreen(),TintColor.getBlue(), TintAlpha);
				TintObjectColor = new Color(TintColor.getRed(),TintColor.getGreen(),TintColor.getBlue(), TintAlpha-200);
			}
		}else{
			if(TintAlpha > 0){
				TintAlpha--;
				TintColor = new Color(TintColor.getRed(),TintColor.getGreen(),TintColor.getBlue(), TintAlpha);
				TintObjectColor = new Color(TintColor.getRed(),TintColor.getGreen(),TintColor.getBlue(), TintAlpha-200);
			}
		}
		
		// NIGHT EFFECT
		//g.setColor(new Color(100,50,150,TintAlpha));
		//g.fillRect(0, 0, BlueSaga.SCREEN_WIDTH, BlueSaga.SCREEN_HEIGHT);
		
	}
	
	
	public boolean getTint() {
		return Tint;
	}
	
	public void start(){
		setTintColor(new Color(100,50,150));
		TintObjectColor = new Color(100,50,150);
		Tint = true;
	}
	
	
	public Color getTintColor() {
		return TintColor;
	}

	public Color getTintObjectColor(){
		return TintObjectColor;
	}
	
	public void setTintColor(Color tintColor) {
		TintColor = tintColor;
	}

	public int getTintAlpha(){
		return TintAlpha;
	}
}
