package components;

public class DebugOutput {
  private boolean ON;

  public DebugOutput(boolean on) {
    setON(on);
  }

  public boolean isON() {
    return ON;
  }

  public void setON(boolean oN) {
    ON = oN;
  }

  public void print(String msg) {
    if (isON()) {
      System.out.println(msg);
    }
  }
}
