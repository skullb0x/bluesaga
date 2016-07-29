package animationsystem.animations;

import animationsystem.AnimationChannel;
import animationsystem.CreatureAnimation;
import animationsystem.ScaleYAnimation;

public class VerticalPumpingAnimation extends CreatureAnimation {
	
	public VerticalPumpingAnimation(float speed) {
		super(speed);
		
		animationChannels.add(new AnimationChannel());
		
		// startValue, valueChange, duration, easeType
		animationChannels.get(0).add(new ScaleYAnimation(this,1.0f,0.2f,16*speed,"Quad"));
		animationChannels.get(0).add(new ScaleYAnimation(this,1.2f,-0.6f,10*speed,"Quad"));
		animationChannels.get(0).add(new ScaleYAnimation(this,0.6f,0.4f,10*speed,"Quad"));
		animationChannels.get(0).add(new ScaleYAnimation(this,1.0f,-0.4f,10*speed,"Quad"));
		animationChannels.get(0).add(new ScaleYAnimation(this,0.6f,0.4f,10*speed,"Quad"));
		animationChannels.get(0).add(new ScaleYAnimation(this,1.0f,-0.4f,10*speed,"Quad"));
		animationChannels.get(0).add(new ScaleYAnimation(this,0.6f,0.4f,10*speed,"Quad"));
	
		setAnimationItrEnd(Math.round(76*speed));
	}
	
}
