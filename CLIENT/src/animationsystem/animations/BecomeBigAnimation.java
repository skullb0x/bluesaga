package animationsystem.animations;

import animationsystem.AnimationChannel;
import animationsystem.CreatureAnimation;
import animationsystem.ScaleXAnimation;
import animationsystem.ScaleYAnimation;

public class BecomeBigAnimation extends CreatureAnimation {

  public BecomeBigAnimation(float speed) {
    super(speed);

    animationChannels.add(new AnimationChannel());

    animationChannels.get(0).add(new ScaleXAnimation(this, 1.0f, -0.4f, 10 * speed, "Quad"));
    animationChannels.get(0).add(new ScaleXAnimation(this, 0.6f, 1.6f, 10 * speed, "Quad"));
    animationChannels.get(0).add(new ScaleXAnimation(this, 2.2f, -0.2f, 10 * speed, "Quad"));

    animationChannels.get(0).add(new ScaleXAnimation(this, 2.0f, 0.0f, 5 * 60 * speed, "None"));

    animationChannels.get(0).add(new ScaleXAnimation(this, 2.0f, 0.2f, 10 * speed, "Quad"));
    animationChannels.get(0).add(new ScaleXAnimation(this, 2.2f, -1.6f, 10 * speed, "Quad"));
    animationChannels.get(0).add(new ScaleXAnimation(this, 0.6f, 0.4f, 10 * speed, "Quad"));

    animationChannels.add(new AnimationChannel());

    animationChannels.get(1).add(new ScaleYAnimation(this, 1.0f, -0.4f, 10 * speed, "Quad"));
    animationChannels.get(1).add(new ScaleYAnimation(this, 0.6f, 1.6f, 10 * speed, "Quad"));
    animationChannels.get(1).add(new ScaleYAnimation(this, 2.2f, -0.2f, 10 * speed, "Quad"));

    animationChannels.get(1).add(new ScaleYAnimation(this, 2.0f, 0.0f, 5 * 60 * speed, "None"));

    animationChannels.get(1).add(new ScaleYAnimation(this, 2.0f, 0.2f, 10 * speed, "Quad"));
    animationChannels.get(1).add(new ScaleYAnimation(this, 2.2f, -1.6f, 10 * speed, "Quad"));
    animationChannels.get(1).add(new ScaleYAnimation(this, 0.6f, 0.4f, 10 * speed, "Quad"));

    setAnimationItrEnd(Math.round(60 * 6 * speed));
  }
}
