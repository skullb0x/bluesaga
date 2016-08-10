package utils;

import java.util.concurrent.ThreadLocalRandom;
import java.util.List;

public class RandomUtils {
  public static int getInt(int min, int max) {
    return ThreadLocalRandom.current().nextInt(max - min + 1) + min;
  }

  public static float getFloat(float minf, float maxf) {
    return ThreadLocalRandom.current().nextFloat() * (maxf - minf) + minf;
  }

  public static double getGaussian() {
    return ThreadLocalRandom.current().nextGaussian();
  }

  public static <T> T getAny(List<T> things) {
    int pick = ThreadLocalRandom.current().nextInt(things.size());
    return things.get(pick);
  }
}
