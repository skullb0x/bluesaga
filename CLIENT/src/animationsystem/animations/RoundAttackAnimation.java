package animationsystem.animations;

import animationsystem.AnimationChannel;
import animationsystem.CreatureAnimation;
import animationsystem.RotateAnimation;
import animationsystem.TranslateXAnimation;
import animationsystem.TranslateYAnimation;

public class RoundAttackAnimation extends CreatureAnimation {

  /**
   *
   * @param rotation - total value change in rotation
   * @param animationSpeed - lower value, faster speed
   */
  public RoundAttackAnimation(float rotation, float speed) {
    super(speed);

    // Active angle
    float activeAngle = 0.0f;

    // Distance to go forward
    float distance = 40.0f;

    // X
    animationChannels.add(new AnimationChannel());
    // Y
    animationChannels.add(new AnimationChannel());
    // Rotation
    animationChannels.add(new AnimationChannel());

    float oldX = 0.0f;
    float oldY = 0.0f;

    float newX = 0.0f;
    float newY = 0.0f;

    float changeX = 0.0f;
    float changeY = 0.0f;

    int partDuration = 0;
    int totalDuration = 0;

    animationChannels.get(2).add(new RotateAnimation(this, rotation, totalDuration, "Linear"));

    while (rotation >= 0.0f) {
      // cos(angle) = dX/distance;
      // sin(angle) = dY/distance;

      newX = (float) (Math.sin(Math.toRadians(activeAngle)) * distance);
      newY = -(float) (Math.cos(Math.toRadians(activeAngle)) * distance);

      changeX = newX - oldX;
      changeY = newY - oldY;

      activeAngle = (activeAngle + 15.0f) % 360.0f;
      rotation -= 15.0f;

      partDuration = Math.round((Math.abs(changeX) + Math.abs(changeY)) * speed);

      animationChannels
          .get(0)
          .add(new TranslateXAnimation(this, oldX, changeX, partDuration, "Linear"));
      animationChannels
          .get(1)
          .add(new TranslateYAnimation(this, oldY, changeY, partDuration, "Linear"));

      totalDuration += partDuration;

      oldX = newX;
      oldY = newY;
    }

    partDuration = Math.round((Math.abs(oldX) + Math.abs(oldY)) * speed);

    animationChannels
        .get(0)
        .add(new TranslateXAnimation(this, oldX, -oldX, partDuration, "Linear"));
    animationChannels
        .get(1)
        .add(new TranslateYAnimation(this, oldY, -oldY, partDuration, "Linear"));
    totalDuration += partDuration;

    animationChannels.get(2).get(0).setDuration(totalDuration);

    setAnimationItrEnd(totalDuration);
  }
}
