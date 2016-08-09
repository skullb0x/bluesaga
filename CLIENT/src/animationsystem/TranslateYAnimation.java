package animationsystem;

public class TranslateYAnimation extends PartAnimation {

  private CreatureAnimation parentAnimation;

  public TranslateYAnimation(
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
    this.parentAnimation.setAnimationY(Math.round(updatedValue));
    return updatedValue;
  }
}
