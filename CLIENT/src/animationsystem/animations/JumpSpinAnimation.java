package animationsystem.animations;

import animationsystem.AnimationChannel;
import animationsystem.CreatureAnimation;
import animationsystem.SpinAnimation;
import animationsystem.TranslateYAnimation;

public class JumpSpinAnimation extends CreatureAnimation {

  public JumpSpinAnimation(int dX, float speed) {
    super(speed);

    // startValue, valueChange, duration, easeType

    // Rotation
    animationChannels.add(new AnimationChannel());

    if (dX < 0) {
      animationChannels.get(0).add(new SpinAnimation(this, 0.0f, -360.0f, 76 * speed, "None"));
    } else {
      animationChannels.get(0).add(new SpinAnimation(this, 0.0f, 360.0f, 76 * speed, "None"));
    }

    // Y
    animationChannels.add(new AnimationChannel());

    animationChannels.get(1).add(new TranslateYAnimation(this, 0.0f, -20.0f, 38 * speed, "None"));
    animationChannels.get(1).add(new TranslateYAnimation(this, -20.0f, 20.0f, 38 * speed, "Quad"));

    setAnimationItrEnd(Math.round(76 * speed));
  }
}
