package graphics.screeneffects;

import java.util.Iterator;
import java.util.Vector;

import game.ClientSettings;
import graphics.ImageResource;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.fills.GradientFill;
import org.newdawn.slick.geom.Rectangle;

import utils.RandomUtils;


public class StatusScreenEffect {
	private String Name;
	private Color EffectColor;

	private int duration;
	private int duration_itr;
	
	private int gSize = 100;
	private float gAlpha = 1.0f;
	
	private boolean RemoveMe = false;
	
	private GradientFill topG;
	private GradientFill bottomG;
	private Rectangle topR;
	private Rectangle bottomR;
	private Vector<InkSpot> inkSpots;
	private Vector<SnowSpot> snowSpots;
	private float spiralAngle;
	
	public StatusScreenEffect(String effectName){
		Name = effectName;
		
		
		if(Name.equals("Hit")){
			duration = 60;
			setEffectColor(new Color(255,0,0));
		}else if(Name.equals("Ink")){
			inkSpots = new Vector<InkSpot>();
			int nrSpots = RandomUtils.getInt(3, 10);
			for(int i = 0; i < nrSpots; i++){
				inkSpots.add(new InkSpot());
			}
			duration = 20 * 60;
		}else if(Name.equals("Snow")){
			snowSpots = new Vector<SnowSpot>();
			int nrSpots = RandomUtils.getInt(3,10);
			for(int i = 0; i < nrSpots; i++){
				snowSpots.add(new SnowSpot());
			}
			duration = 20 * 60;
		}else if(Name.equals("Spiral")){
			spiralAngle = 0.0f;
		}
		duration_itr = 0;
	}
	
	public void draw(Graphics g){
		duration_itr++;
		if(duration_itr > duration){
			RemoveMe = true;
		}else{
			if(Name.equals("Hit")){
				float alpha = sineCurve(duration_itr, duration, gAlpha);
				
				EffectColor.a = alpha;
				
				topG.setStartColor(EffectColor);
				bottomG.setEndColor(EffectColor);
				
				g.fill(topR, topG);
				g.fill(bottomR, bottomG);
				
				/*
				// TOP
				BlueSaga.GFX.getSprite("effects/screenfx_hit").getImage().drawFlash(0,0-gSize/2,BlueSaga.SCREEN_WIDTH,gSize, new Color(EffectColor.getRed(),EffectColor.getGreen(),EffectColor.getBlue(), alpha));
				// RIGHT
				BlueSaga.GFX.getSprite("effects/screenfx_hit").getImage().setAlpha(alpha);BlueSaga.GFX.getSprite("effects/screenfx_hit").getImage().drawFlash(BlueSaga.SCREEN_WIDTH-gSize/2,0,gSize,BlueSaga.SCREEN_HEIGHT, new Color(EffectColor.getRed(),EffectColor.getGreen(),EffectColor.getBlue(), alpha));
				// BOTTOM
				BlueSaga.GFX.getSprite("effects/screenfx_hit").getImage().drawFlash(0,BlueSaga.SCREEN_HEIGHT-gSize/2,BlueSaga.SCREEN_WIDTH,gSize, new Color(EffectColor.getRed(),EffectColor.getGreen(),EffectColor.getBlue(), alpha));
				// LEFT
				BlueSaga.GFX.getSprite("effects/screenfx_hit").getImage().drawFlash(0-gSize/2,0,gSize,BlueSaga.SCREEN_HEIGHT, new Color(EffectColor.getRed(),EffectColor.getGreen(),EffectColor.getBlue(), alpha));
				 */
			}else if(Name.equals("Ink")){
				for(Iterator<InkSpot> iter = inkSpots.iterator();iter.hasNext();){ 
					InkSpot ink = iter.next();
					ink.draw();
				}
			}else if(Name.equals("Snow")){
				for(Iterator<SnowSpot> iter = snowSpots.iterator();iter.hasNext();){ 
					SnowSpot snow = iter.next();
					snow.draw();
				}
			}else if(Name.equals("Spiral")){
				ImageResource.getSprite("effects/screenfx_spiral").getImage().setRotation(spiralAngle);
				spiralAngle += 0.05f;
				ImageResource.getSprite("effects/screenfx_spiral").getImage().draw(0, 0,ClientSettings.SCREEN_WIDTH,ClientSettings.SCREEN_HEIGHT);
			}
		}
	}
	
	private float sineCurve(float time, float duration, float maxValue){
		return (float) (1-Math.cos((time/duration)*Math.PI*2))*maxValue/2;
//		return (float) (0.5 - Math.cos((time/duration)*2*Math.PI)*0.5) * maxValue;
	}
	
	public void setEffectColor(Color newColor){
		EffectColor = newColor;
		topG = new GradientFill(0,0,new Color(newColor.getRed(),newColor.getGreen(),newColor.getBlue(),255),0,gSize/2,new Color(newColor.getRed(),newColor.getGreen(),newColor.getBlue(),0));
		bottomG = new GradientFill(0,ClientSettings.SCREEN_HEIGHT-gSize/2,new Color(newColor.getRed(),newColor.getGreen(),newColor.getBlue(),0),0,ClientSettings.SCREEN_HEIGHT,new Color(newColor.getRed(),newColor.getGreen(),newColor.getBlue(),255));
		//leftG = new GradientFill(0,-75,new Color(newColor.getRed(),newColor.getGreen(),newColor.getBlue(),0),0,75,new Color(newColor.getRed(),newColor.getGreen(),newColor.getBlue(),255));
		//rightG = new GradientFill(0,-75,new Color(newColor.getRed(),newColor.getGreen(),newColor.getBlue(),0),0,75,new Color(newColor.getRed(),newColor.getGreen(),newColor.getBlue(),255));
		
		// TOP
		topR = new Rectangle(0,0-gSize/2,ClientSettings.SCREEN_WIDTH,gSize);
		// RIGHT
		//rightR = new Rectangle(BlueSaga.SCREEN_WIDTH-gSize/2,0,gSize,BlueSaga.SCREEN_HEIGHT);
		// BOTTOM
		bottomR = new Rectangle(0,ClientSettings.SCREEN_HEIGHT-gSize/2,ClientSettings.SCREEN_WIDTH,gSize);
		// LEFT
		//leftR = new Rectangle(0-gSize/2,0,gSize,BlueSaga.SCREEN_HEIGHT);
		
	}
	
	public void setAlpha(float newAlpha){
		gAlpha = newAlpha;
	}
	
	public boolean getRemoveMe(){
		return RemoveMe;
	}
	
}
