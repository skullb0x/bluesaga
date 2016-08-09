package game.Particle;

import game.Streak.StreakType;

import org.newdawn.slick.geom.Vector2f;

public final class ParticleType {

  public int myID;
  public String myName;

  public Vector2f myMinDir;
  public Vector2f myMaxDir;
  public float myMinAxisRotSpeed;
  public float myMaxAxisRotSpeed;
  public float myRotationSpeed;
  public float myMinScale;
  public float myMaxScale;
  public float myLifetime;
  public String myImageString;
  public float myVerticalGravity;
  public float myHorizontalGravity;
  public org.newdawn.slick.Color myStartColor;
  public org.newdawn.slick.Color myEndColor;
  public float myFadeSpeed;
  public StreakType myStreakType;
  public boolean myShouldShow;
}
