package utils;

import java.util.Locale;

public class Obfuscator {

  //adjust to suit:
  final static int feistelRounds = 4;
  final static int randRounds = 4;
  final static int seed = 12345;

  // modulus for half a string:
  final static int mod = 60466176; //36^5

  private static int f(int x) {
    // http://en.wikipedia.org/wiki/Linear_congruential_generator
    final int a = 12 + 1;
    final int c = 1361423303;
    x = (x + seed) % mod;
    int r = randRounds;
    while (r-- != 0) {
      x = (a * x + c) % mod;
    }
    return x;
  }

  public static String obfuscate(int i) {
    int a = i / mod;
    int b = i % mod;
    int r = feistelRounds;
    while (r-- != 0) {
      a = (a + f(b)) % mod;
      b = (b + f(a)) % mod;
    }
    return pad5(Integer.toString(a, 36)) + pad5(Integer.toString(b, 36));
  }

  public static int illuminate(String s) {
    int a = Integer.valueOf(s.substring(0, 5), 36);
    int b = Integer.valueOf(s.substring(5, 10), 36);
    int r = feistelRounds;
    while (r-- != 0) {
      b = (b - f(a)) % mod;
      a = (a - f(b)) % mod;
    }
    // make the modulus positive:
    a = (a + mod) % mod;
    b = (b + mod) % mod;

    return a * mod + b;
  }

  public static String pad5(String s) {
    return String.format("%5s", s).replace(' ', '0').toUpperCase(Locale.ENGLISH);
  }

  public static String pad10(String s) {
    return String.format("%10s", s).replace(' ', '0').toUpperCase(Locale.ENGLISH);
  }
}
