package game;

import org.newdawn.slick.SlickException;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.font.effects.ColorEffect;

public class Font {

	public static UnicodeFont size8;
	public static UnicodeFont size12;
	public static UnicodeFont size18;
	public static UnicodeFont size20;
	public static UnicodeFont size30;
	public static UnicodeFont size12bold;
	
	public Font(){
		// LOAD FONTS
		try {
			
			size30 = new UnicodeFont("fonts/nokiaFont.ttf", 30, true, true);
			size30.getEffects().add(new ColorEffect(java.awt.Color.white));
			
			size20 = new UnicodeFont("fonts/nokiaFont.ttf", 20, true, true);
			size20.getEffects().add(new ColorEffect(java.awt.Color.white));
			
			size18 = new UnicodeFont("fonts/nokiaFont.ttf", 18, true, true);
			size18.getEffects().add(new ColorEffect(java.awt.Color.white));
			
			size12bold = new UnicodeFont("fonts/nokiaFont.ttf", 12, true, true);
			size12bold.getEffects().add(new ColorEffect(java.awt.Color.white));
			
			size12 = new UnicodeFont("fonts/nokiaFont.ttf", 12, true, true);
			size12.getEffects().add(new ColorEffect(java.awt.Color.white));
	
			size8 = new UnicodeFont("fonts/nokiaFont.ttf", 8, true, true);
			size8.getEffects().add(new ColorEffect(java.awt.Color.white));
			
		} catch (SlickException e) {
			e.printStackTrace();
		}
		
	}
	
	public void loadGlyphs(){
		try {
			size12.loadGlyphs();
			size18.loadGlyphs(); 
			size20.loadGlyphs(); 
			size30.loadGlyphs();
			size12bold.loadGlyphs();
			size8.loadGlyphs();
		} catch (SlickException e) {
			e.printStackTrace();
		}
	}
	
}
