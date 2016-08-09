package game.Particle;

import game.ParticleEditor;
import game.Camera;
import game.Streak.Streak;

import java.util.Random;

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
  private Random myRandom;
  private Streak myStreak;

  public Particle(Vector2f aPosition, float aInitialRotation, ParticleType aParticleType) {
    myPosition.x = aPosition.x;
    myPosition.y = aPosition.y;
    myType = aParticleType;
    myImage = ParticleEditor.GFX.getSprite(myType.myImageString).getImage();
    myRandom = new Random();
    myDirection.x = ParticleEditor.randomFloat(myType.myMinDir.x, myType.myMaxDir.x, myRandom);
    myDirection.y = ParticleEditor.randomFloat(myType.myMinDir.y, myType.myMaxDir.y, myRandom);
    myDirection.add(aInitialRotation);
    myRotationSpeed =
        ParticleEditor.randomFloat(myType.myMinAxisRotSpeed, myType.myMaxAxisRotSpeed, myRandom);
    myScale = ParticleEditor.randomFloat(myType.myMinScale, myType.myMaxScale, myRandom);
    myCurrentLifetime = 0.0f;

    if (myType.myStreakType != null) {
      myStreak = new Streak(myPosition, myType.myStreakType);
    }
  }

  public void Update(float aElapsedTime) {
    myDirection.add(myType.myRotationSpeed * aElapsedTime);

    myDirection.x += myType.myHorizontalGravity * aElapsedTime;
    myDirection.y += myType.myVerticalGravity * aElapsedTime;

    myPosition.x += (myDirection.x * aElapsedTime);
    myPosition.y += (myDirection.y * aElapsedTime);

    myRotation += myRotationSpeed * aElapsedTime;

    myCurrentLifetime += aElapsedTime;

    if (myStreak != null) {
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

    if (myStreak != null) {
      myStreak.Render(g, aCamera);
    }

    if (myType.myShouldShow) {
      float renderPosX = myPosition.x + aCamera.x;
      float renderPosY = myPosition.y + aCamera.y;
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
