package components;

import java.security.*;
import java.math.*;

public class md5 {

  public md5() {}

  public String encode(String text) {
    String encodedText = "";
    try {
      MessageDigest m;

      m = MessageDigest.getInstance("MD5");

      m.update(text.getBytes(), 0, text.length());

      encodedText = new BigInteger(1, m.digest()).toString(16);
    } catch (NoSuchAlgorithmException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return encodedText;
  }
}
