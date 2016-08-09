package game.Streak;

import game.Camera;

import java.util.Vector;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Vector2f;

public class Streak {

  private Vector2f myPosition;
  private Vector<StreakDot> myStreakDots;
  private boolean myShouldBeRemoved;

  private StreakType myType;

  public Streak(Vector2f aPosition, StreakType aStreakType) {
    myPosition = aPosition;
    myStreakDots = new Vector<StreakDot>();
    myType = aStreakType;
    myShouldBeRemoved = false;
  }

  public void Update(float aElapsedTime) {

    if (myStreakDots.size() <= 0) {
      AddStreakDot();
    } else {

      Vector2f diffDist = new Vector2f();
      diffDist.x = myPosition.x - myStreakDots.get(myStreakDots.size() - 1).myPosition.x;
      diffDist.y = myPosition.y - myStreakDots.get(myStreakDots.size() - 1).myPosition.y;
      float dist = Math.abs(diffDist.length());

      if (dist >= myType.myQuality) {
        AddStreakDot();
      }
    }

    UpdateStreakDots(aElapsedTime);
  }

  private void AddStreakDot() {
    Vector2f newPos = new Vector2f(myPosition);
    StreakDot newDot = new StreakDot(newPos);
    myStreakDots.add(newDot);
  }

  public void Render(Graphics g, Camera aCamera) {

    for (int index = 0; index < myStreakDots.size(); ++index) {

      Vector2f firstPos = myStreakDots.get(index).myPosition;
      Vector2f secondPos;

      if (index + 1 < myStreakDots.size()) {
        secondPos = myStreakDots.get(index + 1).myPosition;
      } else {
        break;
      }

      Color firstColor = new Color(255, 255, 255, 255);
      Color secondColor = new Color(255, 255, 255, 255);
      CalculateLineColor(firstColor, secondColor, index);
      g.setLineWidth(myType.myWidth);

      float firstRenderPosX = firstPos.x + aCamera.x;
      float firstRenderPosY = firstPos.y + aCamera.y;
      float secondRenderPosX = secondPos.x + aCamera.x;
      float secondRenderPosY = secondPos.y + aCamera.y;
      g.drawGradientLine(
          firstRenderPosX,
          firstRenderPosY,
          firstColor,
          secondRenderPosX,
          secondRenderPosY,
          secondColor);
    }
  }

  public void SetPosition(Vector2f aPosition) {
    myPosition = aPosition;
  }

  public void SetPosition(float aX, float aY) {
    myPosition.x = aX;
    myPosition.y = aY;
  }

  private void UpdateStreakDots(float aElapsedTime) {

    // Update dots
    for (int index = 0; index < myStreakDots.size(); ++index) {
      myStreakDots.get(index).Update(aElapsedTime);
    }

    // Remove dots
    for (int index = 0; index < myStreakDots.size(); ++index) {
      StreakDot streakDot = myStreakDots.get(index);
      if (streakDot.myCurrentLifetime > myType.myLifetime) {
        myStreakDots.remove(streakDot);
      }
    }
  }

  private void CalculateLineColor(Color aFirstColor, Color aSecondColor, float aDotNr) {
    float percentageColor = aDotNr / myStreakDots.size();
    CalculateDotColor(aFirstColor, percentageColor);
    percentageColor = (aDotNr + 1) / myStreakDots.size();
    CalculateDotColor(aSecondColor, percentageColor);
  }

  private void CalculateDotColor(Color aColor, float aPercentage) {
    float diff = myType.myEndColor.r - myType.myStartColor.r;
    aColor.r = myType.myStartColor.r + (diff * aPercentage);
    diff = myType.myEndColor.g - myType.myStartColor.g;
    aColor.g = myType.myStartColor.g + (diff * aPercentage);
    diff = myType.myEndColor.b - myType.myStartColor.b;
    aColor.b = myType.myStartColor.b + (diff * aPercentage);
    diff = myType.myEndColor.a - myType.myStartColor.a;
    aColor.a = myType.myStartColor.a + (diff * aPercentage);
  }

  public boolean ShouldBeRemoved() {
    return myShouldBeRemoved;
  }
}
