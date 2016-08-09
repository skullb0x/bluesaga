package animationsystem;

import java.util.Vector;

public abstract class CreatureAnimation {

  private int animationItr = 0;
  private int animationItrEnd = 0;

  /**
   * Position
   */
  private int animationX = 0;
  private int animationY = 0;

  /**
   * Transparency
   */
  private int animationFade = 0;

  /**
   * Change view direction
   */
  private float rotation = 0.0f;
  private float startRotation = 0.0f;
  /**
   * Spinning the sprite
   */
  private float spin = 0.0f;

  /**
   * Sprite scaling
   */
  private float scaleX = 1.0f;
  private float scaleY = 1.0f;

  protected Vector<AnimationChannel> animationChannels;

  private boolean active = true;

  public CreatureAnimation(float speed) {
    animationChannels = new Vector<AnimationChannel>();
  }

  public void update() {
    if (isActive()) {

      for (AnimationChannel channel : animationChannels) {
        if (channel.size() > 0) {
          // Check if animation channel is ready or waiting
          if (channel.isActive()) {
            if (channel.get(0).isActive()) {
              // tick, start, total change, duration
              channel.get(0).update();
            }

            if (!channel.get(0).isActive()) {
              // Remove partAnimation
              channel.remove(0);
            }
          }
        }
      }

      animationItr++;
      if (animationItr >= animationItrEnd) {
        setAnimationX(0);
        setAnimationY(0);
        setScaleX(1.0f);
        setScaleY(1.0f);
        setRotation(0);
        setSpin(0.0f);
        setActive(false);
      }
    }
  }

  /**
   * Getters and setters
   */
  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public int getAnimationItr() {
    return animationItr;
  }

  public void setAnimationItr(int animationItr) {
    this.animationItr = animationItr;
  }

  public int getAnimationItrEnd() {
    return animationItrEnd;
  }

  public void setAnimationItrEnd(int animationItrEnd) {
    this.animationItrEnd = animationItrEnd;
  }

  public int getAnimationX() {
    return animationX;
  }

  public void setAnimationX(int animationX) {
    this.animationX = animationX;
  }

  public int getAnimationY() {
    return animationY;
  }

  public void setAnimationY(int animationY) {
    this.animationY = animationY;
  }

  public int getAnimationFade() {
    return animationFade;
  }

  public void setAnimationFade(int animationFade) {
    this.animationFade = animationFade;
  }

  public float getRotation() {
    return rotation;
  }

  public void setRotation(float rotation) {
    this.rotation = rotation;
  }

  public float getScaleX() {
    return scaleX;
  }

  public void setScaleX(float scaleX) {
    this.scaleX = scaleX;
  }

  public float getScaleY() {
    return scaleY;
  }

  public void setScaleY(float scaleY) {
    this.scaleY = scaleY;
  }

  public float getSpin() {
    return spin;
  }

  public void setSpin(float spin) {
    this.spin = spin;
  }

  public float getStartRotation() {
    return startRotation;
  }

  public void setStartRotation(float startRotation) {
    this.startRotation = startRotation;
  }
}
