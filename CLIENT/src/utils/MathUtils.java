package utils;

public class MathUtils {

  public static float angleBetween(int dX, int dY) {
    // Update direction of monster

    float angle = (float) Math.toDegrees(Math.atan2(-(dX), dY));

    if (angle < 0.0f) {
      angle += 360.0f;
    }

    return angle;
  }
}
