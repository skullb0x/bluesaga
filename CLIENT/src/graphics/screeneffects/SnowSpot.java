package graphics.screeneffects;

import org.newdawn.slick.Color;
import org.newdawn.slick.Image;

import utils.RandomUtils;
import game.ClientSettings;
import graphics.ImageResource;
import graphics.Sprite;

public class SnowSpot {

	private float X;
	private float Y;
	
	private int Size;
	private int Alpha;
	
	private float Speed;

	private int itr = 0;
	
	private Sprite snowGFX;
	
	public SnowSpot() {
		X = RandomUtils.getInt(-300, 924);
		Y = RandomUtils.getInt(-300, 540);
		
		Size = RandomUtils.getInt(200, 1000);
		Speed = RandomUtils.getFloat(0.2f,1.2f);
		Alpha = 0;
		
		//Rotation = BlueSaga.randomGenerator.nextFloat();
		
		//inkGFX = new Sprite("images/effects/screenfx_ink");
		
		//inkGFX.getImage().setRotation(Rotation);
		snowGFX = ImageResource.getSprite("effects/screenfx_snow");
		
		snowGFX.getImage().setFilter(Image.FILTER_NEAREST);
	}
	
	public void draw(){
		Y += Speed;
		
		itr++;
		
		if(itr < 1000 && Alpha < 255){
			Alpha += 2;
		}else if(itr >= 200){
			Alpha -= 2;
		}
		
		if(Y - Size < ClientSettings.SCREEN_HEIGHT){
			snowGFX.draw((int) X, (int) Y, Size, Size, new Color(255,255,255,Alpha));
		}
		
		
	}

}
