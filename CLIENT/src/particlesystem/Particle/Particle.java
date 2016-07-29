package particlesystem.Particle;

import graphics.ImageResource;
import particlesystem.Streak.Streak;
import screens.Camera;
import utils.RandomUtils;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Vector2f;

public class Particle {

	private ParticleType myType;
	private Image myImage;
	Vector2f myPosition = new Vector2f();
	Vector2f myDirection = new Vector2f();
	private float myRotationSpeed;
	private float myRotation;
	private float myScale;
	private float myCurrentLifetime;
	private boolean myShouldBeRemoved = false;
	private Streak myStreak;

	public Particle(Vector2f aPosition, float aInitialRotation, ParticleType aParticleType) {
		myPosition.x = aPosition.x;
		myPosition.y = aPosition.y;
		myType = aParticleType;
		myImage = ImageResource.getSprite("effects/"+myType.myImageString).getImage();
		myDirection.x = RandomUtils.getFloat(myType.myMinDir.x, myType.myMaxDir.x);
		myDirection.y = RandomUtils.getFloat(myType.myMinDir.y, myType.myMaxDir.y);
		myDirection.add(aInitialRotation);
		myRotationSpeed = RandomUtils.getFloat(myType.myMinAxisRotSpeed, myType.myMaxAxisRotSpeed);
		myScale = RandomUtils.getFloat(myType.myMinScale, myType.myMaxScale);
		myCurrentLifetime = 0.0f;
		
		if(myType.myStreakType != null) {
			myStreak = new Streak(myPosition, myType.myStreakType);
		}
	}

	public void Update(float aElapsedTime) {
		myDirection.add(myType.myRotationSpeed * aElapsedTime);
		
		myDirection.x += myType.myHorizontalGravity * aElapsedTime;
		myDirection.y += myType.myVerticalGravity * aElapsedTime;
		
		myPosition.x += (myDirection.x * aElapsedTime);
		myPosition.y += (myDirection.y * aElapsedTime);
		
		//myRotation += myRotationSpeed * aElapsedTime;
		myCurrentLifetime += aElapsedTime;
		
		if(myStreak != null) {
			myStreak.Update(aElapsedTime);
		}
		
		if (myCurrentLifetime >= myType.myLifetime) {
			myShouldBeRemoved = true;
		}
	}

	public void Render(Graphics g, Camera aCamera) {
		myImage.setRotation(myRotation);
		
		float newAlpha = CalculateNewAlpha();
		
		Color newColor = new Color(0, 0, 0, newAlpha);
		CalculateNewColor(newColor);

		if(myStreak != null) {
			myStreak.Render(g, aCamera);
		}
		
		if(myType.myShouldShow){
			float renderPosX = myPosition.x + aCamera.getfX() - 25;
			float renderPosY = myPosition.y + aCamera.getfY() - 25;
			myImage.draw(renderPosX, renderPosY, myScale, newColor);
		}
	}

	public boolean ShouldBeRemoved() {
		return myShouldBeRemoved;
	}
	
	private void CalculateNewColor(Color aNewColor) {
		float percDone = myCurrentLifetime / myType.myLifetime;
		float diffR = myType.myEndColor.r - myType.myStartColor.r;
		aNewColor.r = myType.myStartColor.r + (diffR * percDone);
		float diffG = myType.myEndColor.g - myType.myStartColor.g;
		aNewColor.g = myType.myStartColor.g + (diffG * percDone);
		float diffB = myType.myEndColor.b - myType.myStartColor.b;
		aNewColor.b = myType.myStartColor.b + (diffB * percDone);
	}
	
	private float CalculateNewAlpha() {
		float newAlpha = 1.0f;
//		float percDone = myCurrentLifetime / myType.myLifetime;
//		if(percDone >= myType.myFadeSpeed){
//			newAlpha = myCurrentLifetime / myType.myLifetime;
//		}
		
		newAlpha = 1.0f - (myCurrentLifetime / myType.myLifetime);
		return newAlpha;
	}

}
