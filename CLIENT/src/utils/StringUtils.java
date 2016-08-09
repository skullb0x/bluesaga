package utils;

public class StringUtils {

  private final static String NON_THIN = "[^iIl1\\.,']";

  private static int textWidth(String str) {
    return str.length() - str.replaceAll(NON_THIN, "").length() / 2;
  }

  /**
   * Ellipsize text
   * @param text, text to be ellipsized
   * @param max, max nr of letters allowed
   * @return ellipsized text
   */
  public static String ellipsize(String text, int max) {

    if (textWidth(text) <= max) return text;

    // Start by chopping off at the word before max
    // This is an over-approximation due to thin-characters...
    int end = text.lastIndexOf(' ', max - 3);

    // Just one long word. Chop it off.
    if (end == -1) return text.substring(0, max - 3) + "...";

    // Step forward as long as textWidth allows.
    int newEnd = end;
    do {
      end = newEnd;
      newEnd = text.indexOf(' ', end + 1);

      // No more spaces.
      if (newEnd == -1) newEnd = text.length();

    } while (textWidth(text.substring(0, newEnd) + "...") < max);

    return text.substring(0, end) + "...";
  }
}
