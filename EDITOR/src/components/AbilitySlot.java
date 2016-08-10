package components;

public class AbilitySlot {

  private boolean Occupied;
  private boolean extraSpace; // if used as extra space for ability
  private int OrbId;

  public AbilitySlot() {
    Occupied = false;
    extraSpace = false;
  }

  public boolean addOrb(int orbId) {
    if (!Occupied) {
      OrbId = orbId;
      Occupied = true;
      return true;
    }
    return false;
  }

  public boolean isOccupied() {
    return Occupied;
  }

  public int getOrbId() {
    return OrbId;
  }

  public boolean isExtraSpace() {
    return extraSpace;
  }
}
