package animationsystem;

public class ScaleYAnimation extends PartAnimation {

	private CreatureAnimation parentAnimation;
	
	
	public ScaleYAnimation(CreatureAnimation parentAnimation, float startValue, float valueChange, float duration, String easeType){
		super(startValue, valueChange, duration, easeType);
		this.parentAnimation = parentAnimation;
	}
	
	public float update(){
		float updatedValue = super.update();
		this.parentAnimation.setScaleY(updatedValue);
		return updatedValue;
	}
}
