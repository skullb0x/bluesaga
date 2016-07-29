package animationsystem.animations;

import animationsystem.AnimationChannel;
import animationsystem.CreatureAnimation;
import animationsystem.ScaleXAnimation;
import animationsystem.SpinAnimation;
import animationsystem.TranslateXAnimation;
import animationsystem.TranslateYAnimation;


public class RollAttackAnimation extends CreatureAnimation {
	
	public RollAttackAnimation(int dX, int dY, float speed) {
		super(speed);

		// Contract Width
		animationChannels.add(new AnimationChannel());
		
		animationChannels.get(0).add(new ScaleXAnimation(this,1.0f,0.05f,20.0f*speed,"None"));
		animationChannels.get(0).add(new ScaleXAnimation(this,1.2f,-0.6f,20.0f*speed,"None"));

		// Y
		animationChannels.add(new AnimationChannel());

		animationChannels.get(1).add(new TranslateYAnimation(this,0.0f,-20.0f,20.0f*speed,"None"));
		animationChannels.get(1).add(new TranslateYAnimation(this,-20.0f,20.0f,20.0f*speed,"None"));

		// X
		animationChannels.get(0).add(new TranslateXAnimation(this,0.0f,40.0f*dX,20.0f*speed,"None"));
		animationChannels.get(0).add(new TranslateXAnimation(this,40.0f*dX,-40.0f*dX,30.0f*speed,"Quad"));

		// Y
		animationChannels.get(1).add(new TranslateYAnimation(this,0.0f,40.0f*dY,20.0f*speed,"None"));
		animationChannels.get(1).add(new TranslateYAnimation(this,40.0f*dY,-40.0f*dY,30.0f*speed,"Quad"));
		
		
		// Spin
		animationChannels.add(new AnimationChannel(40.0f*speed));

		if(dX < 0){
			animationChannels.get(2).add(new SpinAnimation(this,0.0f,-360.0f,20.0f*speed,"None"));
			animationChannels.get(2).add(new SpinAnimation(this,-360.0f,360.0f,30.0f*speed,"Quad"));
		}else{
			animationChannels.get(2).add(new SpinAnimation(this,0.0f,360.0f,20.0f*speed,"None"));
			animationChannels.get(2).add(new SpinAnimation(this,360.0f,-360.0f,30.0f*speed,"Quad"));
		}

		// Expand width
		animationChannels.add(new AnimationChannel(Math.round(90.0f*speed)));
		
		animationChannels.get(3).add(new ScaleXAnimation(this,0.6f,0.4f,20.0f*speed,"Quad"));

		setAnimationItrEnd(Math.round(110.0f*speed));
	}
	
}
