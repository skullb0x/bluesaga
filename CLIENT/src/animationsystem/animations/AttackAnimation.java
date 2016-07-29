package animationsystem.animations;

import animationsystem.AnimationChannel;
import animationsystem.CreatureAnimation;
import animationsystem.TranslateXAnimation;
import animationsystem.TranslateYAnimation;


public class AttackAnimation extends CreatureAnimation {
	
	public AttackAnimation(int dX, int dY, float speed) {
		super(speed);
		
		animationChannels.add(new AnimationChannel());
		
		// startValue, valueChange, duration, easeType
		animationChannels.get(0).add(new TranslateXAnimation(this,0.0f,40.0f*dX,5*speed,"None"));
		animationChannels.get(0).add(new TranslateXAnimation(this,40.0f*dX,-40.0f*dX,8*speed,"Quad"));

		animationChannels.add(new AnimationChannel());
		
		animationChannels.get(1).add(new TranslateYAnimation(this,0.0f,40.0f*dY,5*speed,"None"));
		animationChannels.get(1).add(new TranslateYAnimation(this,40.0f*dY,-40.0f*dY,8*speed,"Quad"));

		setAnimationItrEnd(Math.round(13*speed));
	}
	
}
