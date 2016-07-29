package animationsystem;

public class ScaleXAnimation extends PartAnimation {

	private CreatureAnimation parentAnimation;
	
	
	public ScaleXAnimation(CreatureAnimation parentAnimation, float startValue, float valueChange, float duration, String easeType){
		super(startValue, valueChange, duration, easeType);
		this.parentAnimation = parentAnimation;
	}
	
	public float update(){
		float updatedValue = super.update();
		this.parentAnimation.setScaleX(updatedValue);
		return updatedValue;
	}
}
