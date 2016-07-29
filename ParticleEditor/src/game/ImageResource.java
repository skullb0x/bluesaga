package game;

import java.util.HashMap;

public class ImageResource {

	private HashMap<String,Sprite> gfx;
	
	public ImageResource(){
		gfx = new HashMap<String,Sprite>();
	}
	
	public void load(){		

		gfx.put("fx_particle_circle", new Sprite("images/effects/fx_particle_circle"));
		gfx.put("fx_particle_square", new Sprite("images/effects/fx_particle_square"));
		gfx.put("border", new Sprite("images/effects/border"));
		
	}
	
	public Sprite getSprite(String spriteName){
		return gfx.get(spriteName);
	}
	
}
