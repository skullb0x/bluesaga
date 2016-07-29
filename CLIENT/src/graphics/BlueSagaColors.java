package graphics;

import org.newdawn.slick.Color;

public class BlueSagaColors {

	public static Color RED = new Color(222,67,67);
	public static Color WHITE = new Color(255,255,255,255);
	public static Color BLACK = new Color(21,21,21,255);
	public static Color YELLOW = new Color(255,234,116,255);
	public static Color GREEN = new Color(0, 180, 0, 255);
	public static Color BLUE = new Color(0,165,255,255);
	public static Color ORANGE = new Color(227,144,0,255);
	
	public static Color STANCE_DEF_COLOR = new Color(169,255,122);
	public static Color STANCE_ATK_COLOR = new Color(249,144,144);
	
	public static Color getColorFromString(String colorInfo){
		String color_info[] = colorInfo.split(",");
		Color newColor = new Color(Integer.parseInt(color_info[0]), Integer.parseInt(color_info[1]), Integer.parseInt(color_info[2]));
		return newColor;
	}

}
