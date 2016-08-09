package animationsystem;

import animationsystem.easing.Bounce;
import animationsystem.easing.Linear;
import animationsystem.easing.Quad;

public class PartAnimation {
  private float iterator = 0;
  private float duration = 0;
  private float startValue = 0.0f;
  private float valueChange = 0.0f;

  private boolean active = true;

  private String easeType;

  public PartAnimation(float startValue, float valueChange, float duration, String easeType) {
    setActive(true);
    this.iterator = 0;
    this.duration = Math.round(duration);
    this.startValue = startValue;
    this.valueChange = valueChange;

    this.easeType = easeType;
  }

  public float update() {
    if (isActive()) {
      iterator++;
      if (iterator >= duration) {
        setActive(false);
      }
    }

    // tick, start, total change, duration
    if (easeType.equals("Linear")) {
      return Linear.easeInOut(iterator, startValue, valueChange, duration);
    } else if (easeType.equals("Quad")) {
      return Quad.easeInOut(iterator, startValue, valueChange, duration);
    } else if (easeType.equals("Bounce")) {
      return Bounce.easeOut(iterator, startValue, valueChange, duration);
    } else if (easeType.equals("None")) {
      return Linear.easeNone(iterator, startValue, valueChange, duration);
    }

    return Linear.easeNone(iterator, startValue, valueChange, duration);
  }

  /**
   * Setters and getters
   * @return
   */
  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public float getDuration() {
    return duration;
  }

  public void setDuration(float duration) {
    this.duration = duration;
  }
}
