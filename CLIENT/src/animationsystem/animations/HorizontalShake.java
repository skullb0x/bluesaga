package animationsystem.animations;

import animationsystem.AnimationChannel;
import animationsystem.CreatureAnimation;
import animationsystem.TranslateXAnimation;

public class HorizontalShake extends CreatureAnimation {

  public HorizontalShake(float speed) {
    super(speed);

    animationChannels.add(new AnimationChannel());

    // startValue, valueChange, duration, easeType
    animationChannels.get(0).add(new TranslateXAnimation(this, 0.0f, -5.0f, 5 * speed, "Quad"));
    animationChannels.get(0).add(new TranslateXAnimation(this, -5.0f, 10.0f, 10 * speed, "Quad"));
    animationChannels.get(0).add(new TranslateXAnimation(this, 5.0f, -10.0f, 10 * speed, "Quad"));
    animationChannels.get(0).add(new TranslateXAnimation(this, -5.0f, 5.0f, 5 * speed, "Quad"));

    setAnimationItrEnd(Math.round(30 * speed));
  }
}
