package animationsystem.animations;

import animationsystem.AnimationChannel;
import animationsystem.CreatureAnimation;
import animationsystem.ScaleXAnimation;

public class HorizontalPumpingAnimation extends CreatureAnimation {
	
	public HorizontalPumpingAnimation(float speed) {
		super(speed);
		
		animationChannels.add(new AnimationChannel());
		
		// startValue, valueChange, duration, easeType
		animationChannels.get(0).add(new ScaleXAnimation(this,1.0f,0.2f,16*speed,"Quad"));
		animationChannels.get(0).add(new ScaleXAnimation(this,1.2f,-0.6f,10*speed,"Quad"));
		animationChannels.get(0).add(new ScaleXAnimation(this,0.6f,0.4f,10*speed,"Quad"));
		animationChannels.get(0).add(new ScaleXAnimation(this,1.0f,-0.4f,10*speed,"Quad"));
		animationChannels.get(0).add(new ScaleXAnimation(this,0.6f,0.4f,10*speed,"Quad"));
		animationChannels.get(0).add(new ScaleXAnimation(this,1.0f,-0.4f,10*speed,"Quad"));
		animationChannels.get(0).add(new ScaleXAnimation(this,0.6f,0.4f,10*speed,"Quad"));
	
		setAnimationItrEnd(Math.round(76*speed));
	}
	
}
