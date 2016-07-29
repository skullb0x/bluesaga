package animationsystem;

public class TranslateXAnimation extends PartAnimation {

	private CreatureAnimation parentAnimation;
	
	
	public TranslateXAnimation(CreatureAnimation parentAnimation, float startValue, float valueChange, float duration, String easeType){
		super(startValue, valueChange, duration, easeType);
		this.parentAnimation = parentAnimation;
	}
	
	public float update(){
		float updatedValue = super.update();
		this.parentAnimation.setAnimationX(Math.round(updatedValue));
		return updatedValue;
	}
}
