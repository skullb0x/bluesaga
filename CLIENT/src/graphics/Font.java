package graphics;


import org.newdawn.slick.SlickException;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.font.effects.ColorEffect;

public class Font {

	public static UnicodeFont size6;
	public static UnicodeFont size8;
	public static UnicodeFont size9;
	public static UnicodeFont size10;
	public static UnicodeFont size12;
	public static UnicodeFont size16;
	public static UnicodeFont size18;
	public static UnicodeFont size20;
	public static UnicodeFont size30;
	public static UnicodeFont size12bold;
	
	@SuppressWarnings("unchecked")
	
	public static void load(){
		// LOAD FONTS
		try {
			
			size30 = new UnicodeFont("fonts/regularFont.ttf", 25, false, false);
			size30.getEffects().add(new ColorEffect(java.awt.Color.white));
			
			size20 = new UnicodeFont("fonts/regularFont.ttf", 20, false, false);
			size20.getEffects().add(new ColorEffect(java.awt.Color.white));
			
			size18 = new UnicodeFont("fonts/regularFont.ttf", 18, false, false);
			size18.getEffects().add(new ColorEffect(java.awt.Color.white));

			size16 = new UnicodeFont("fonts/regularFont.ttf", 16, false, false);
			size16.getEffects().add(new ColorEffect(java.awt.Color.white));
			
			size12bold = new UnicodeFont("fonts/regularFont.ttf", 12, false, false);
			size12bold.getEffects().add(new ColorEffect(java.awt.Color.white));
			
			size12 = new UnicodeFont("fonts/regularFont.ttf", 12, false, false);
			size12.getEffects().add(new ColorEffect(java.awt.Color.white));
	
			size12.addAsciiGlyphs();
			size12.addNeheGlyphs();
			size12.loadGlyphs();
			
			size10 = new UnicodeFont("fonts/regularFont.ttf", 10, false, false);
			size10.getEffects().add(new ColorEffect(java.awt.Color.white));
	
			size9 = new UnicodeFont("fonts/regularFont.ttf", 9, false, false);
			size9.getEffects().add(new ColorEffect(java.awt.Color.white));
		
			size8 = new UnicodeFont("fonts/regularFont.ttf", 8, false, false);
			size8.getEffects().add(new ColorEffect(java.awt.Color.white));
			
			size6 = new UnicodeFont("fonts/regularFont.ttf", 6, false, false);
			size6.getEffects().add(new ColorEffect(java.awt.Color.white));
			
			
		} catch (SlickException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void loadGlyphs(){
		try {
			size8.loadGlyphs();
			size9.loadGlyphs();
			size10.loadGlyphs();
			size12.loadGlyphs();
			size16.loadGlyphs();
			size18.loadGlyphs(); 
			size20.loadGlyphs(); 
			size30.loadGlyphs();
			size12bold.loadGlyphs();
		} catch (SlickException e) {
			e.printStackTrace();
		}
	}
	
}
