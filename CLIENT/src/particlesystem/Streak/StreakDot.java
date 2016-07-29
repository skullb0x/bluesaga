package particlesystem.Streak;

import org.newdawn.slick.geom.Vector2f;

public class StreakDot {

	public Vector2f myPosition;
	public float myCurrentLifetime;
	
	public StreakDot(Vector2f aPosition){
		myPosition = aPosition;
		myCurrentLifetime = 0.0f;
	}
	
	public void Update(float aElapsedTime){
		myCurrentLifetime += aElapsedTime;
	}
	
}
