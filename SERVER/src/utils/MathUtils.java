package utils;

public class MathUtils {

	
	public static float angleBetween(int dX, int dY){
		// Update direction of monster
		
		float angle = (float) Math.toDegrees(Math.atan2(-(dX), dY));

		
		return angle;
	}
	
	public static void rotate_point(float cx, float cy, float angle, Point p)
	{
		
		double newX =  Math.cos(angle) * p.x - Math.sin(angle) * p.y;
		double newY =  Math.sin(angle) * p.x + Math.cos(angle) * p.y;
		
		p.x = (float) newX;
		p.y = (float) newY;
		 
	}
	
}
