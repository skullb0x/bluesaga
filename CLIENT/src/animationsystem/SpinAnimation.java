package animationsystem;

public class SpinAnimation extends PartAnimation {

  private CreatureAnimation parentAnimation;

  public SpinAnimation(
      CreatureAnimation parentAnimation,
      float startValue,
      float valueChange,
      float duration,
      String easeType) {
    super(startValue, valueChange, duration, easeType);
    this.parentAnimation = parentAnimation;
  }

  public float update() {
    float updatedValue = super.update();
    this.parentAnimation.setSpin(updatedValue);
    return updatedValue;
  }
}
