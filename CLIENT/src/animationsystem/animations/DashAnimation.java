package animationsystem.animations;

import game.ClientSettings;
import animationsystem.AnimationChannel;
import animationsystem.CreatureAnimation;
import animationsystem.TranslateXAnimation;
import animationsystem.TranslateYAnimation;

public class DashAnimation extends CreatureAnimation {

  public DashAnimation(int dX, int dY) {
    super(100);

    animationChannels.add(new AnimationChannel());

    // startValue, valueChange, duration, easeType
    animationChannels
        .get(0)
        .add(new TranslateXAnimation(this, 0.0f, ClientSettings.TILE_SIZE * dX, 10, "None"));

    animationChannels.add(new AnimationChannel());

    animationChannels
        .get(1)
        .add(new TranslateYAnimation(this, 0.0f, ClientSettings.TILE_SIZE * dY, 10, "None"));

    setAnimationItrEnd(Math.round(25));
  }
}
