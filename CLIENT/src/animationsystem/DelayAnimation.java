package animationsystem;

public class DelayAnimation extends PartAnimation {

  public DelayAnimation(
      CreatureAnimation parentAnimation,
      float startValue,
      float valueChange,
      float duration,
      String easeType) {
    super(startValue, valueChange, duration, easeType);
  }

  public float update() {
    float updatedValue = super.update();
    return updatedValue;
  }
}
