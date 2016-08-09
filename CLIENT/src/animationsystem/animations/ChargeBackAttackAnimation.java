package animationsystem.animations;

import animationsystem.AnimationChannel;
import animationsystem.CreatureAnimation;
import animationsystem.TranslateXAnimation;
import animationsystem.TranslateYAnimation;

public class ChargeBackAttackAnimation extends CreatureAnimation {

  public ChargeBackAttackAnimation(int dX, int dY, float speed) {
    super(speed);

    animationChannels.add(new AnimationChannel());

    // startValue, valueChange, duration, easeType
    animationChannels
        .get(0)
        .add(new TranslateXAnimation(this, 0.0f, 20.0f * -dX, 30 * speed, "Quad"));
    animationChannels
        .get(0)
        .add(new TranslateXAnimation(this, 20.0f * -dX, 60.0f * dX, 15 * speed, "None"));
    animationChannels
        .get(0)
        .add(new TranslateXAnimation(this, 40.0f * dX, -40.0f * dX, 15 * speed, "Quad"));

    animationChannels.add(new AnimationChannel());

    animationChannels
        .get(1)
        .add(new TranslateYAnimation(this, 0.0f, 20.0f * -dY, 30 * speed, "Quad"));
    animationChannels
        .get(1)
        .add(new TranslateYAnimation(this, 20.0f * -dY, 60.0f * dY, 15 * speed, "None"));
    animationChannels
        .get(1)
        .add(new TranslateYAnimation(this, 40.0f * dY, -40.0f * dY, 15 * speed, "Quad"));

    setAnimationItrEnd(Math.round(60 * speed));
  }
}
