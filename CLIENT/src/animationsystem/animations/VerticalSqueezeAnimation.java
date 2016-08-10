package animationsystem.animations;

import animationsystem.AnimationChannel;
import animationsystem.CreatureAnimation;
import animationsystem.ScaleYAnimation;

public class VerticalSqueezeAnimation extends CreatureAnimation {

  public VerticalSqueezeAnimation(float speed) {
    super(speed);

    animationChannels.add(new AnimationChannel());

    // startValue, valueChange, duration, easeType
    animationChannels.get(0).add(new ScaleYAnimation(this, 1.0f, -0.4f, 8 * speed, "Quad"));
    animationChannels.get(0).add(new ScaleYAnimation(this, 0.6f, 0.4f, 5 * speed, "Quad"));

    setAnimationItrEnd(Math.round(20 * speed));
  }
}
