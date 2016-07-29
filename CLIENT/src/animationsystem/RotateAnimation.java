package animationsystem;

public class RotateAnimation extends PartAnimation {

	private CreatureAnimation parentAnimation;
	
	
	public RotateAnimation(CreatureAnimation parentAnimation, float valueChange, float duration, String easeType){
		super(0, valueChange, duration, easeType);
		this.parentAnimation = parentAnimation;
	}
	
	public float update(){
		float updatedValue = super.update();
		this.parentAnimation.setRotation(updatedValue);
		return updatedValue;
	}
}
