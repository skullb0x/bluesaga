package animationsystem.animations;

import animationsystem.AnimationChannel;
import animationsystem.CreatureAnimation;
import animationsystem.SpinAnimation;


public class ThrowAnimation extends CreatureAnimation {
	
	public ThrowAnimation(int dX, float speed) {
		super(speed);
		
		// startValue, valueChange, duration, easeType
		
		// Rotation
		animationChannels.add(new AnimationChannel());
		
		if(dX < 0){
			animationChannels.get(0).add(new SpinAnimation(this,0.0f,45.0f,10*speed,"Quad"));
			animationChannels.get(0).add(new SpinAnimation(this,45.0f,-90.0f,20*speed,"Quad"));
			animationChannels.get(0).add(new SpinAnimation(this,-45.0f,45.0f,10*speed,"Quad"));
		}else{
			animationChannels.get(0).add(new SpinAnimation(this,0.0f,-45.0f,10*speed,"Quad"));
			animationChannels.get(0).add(new SpinAnimation(this,-45.0f,90.0f,20*speed,"Quad"));
			animationChannels.get(0).add(new SpinAnimation(this,45.0f,-45.0f,10*speed,"Quad"));
		}
		
		setAnimationItrEnd(Math.round(40*speed));
	}
}
