package map;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Trigger {

  private int Id;

  private int X;
  private int Y;
  private int Z;

  private int TrapId;
  private int DoorId;

  private int ActiveTime;
  private int ActiveTimeItr;

  private boolean Triggered = false;

  public Trigger() {}

  public void load(ResultSet info) {
    try {
      setId(info.getInt("Id"));
      setX(info.getInt("X"));
      setY(info.getInt("Y"));
      setZ(info.getInt("Z"));

      setTrapId(info.getInt("TrapId"));
      setDoorId(info.getInt("DoorId"));
      setActiveTime(info.getInt("ActiveTime"));

    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public int getId() {
    return Id;
  }

  public void setId(int id) {
    Id = id;
  }

  public int getX() {
    return X;
  }

  public void setX(int x) {
    X = x;
  }

  public int getY() {
    return Y;
  }

  public void setY(int y) {
    Y = y;
  }

  public int getZ() {
    return Z;
  }

  public void setZ(int z) {
    Z = z;
  }

  public int getTrapId() {
    return TrapId;
  }

  public void setTrapId(int trapId) {
    TrapId = trapId;
  }

  public int getDoorId() {
    return DoorId;
  }

  public void setDoorId(int doorId) {
    DoorId = doorId;
  }

  public int getActiveTime() {
    return ActiveTime;
  }

  public void setActiveTime(int activeTime) {
    ActiveTime = activeTime;
  }

  public boolean isTriggered() {
    if (Triggered) {
      ActiveTimeItr--;
      if (ActiveTimeItr <= 0) {
        Triggered = false;
      }
    }
    return Triggered;
  }

  public void setTriggered(boolean triggered) {
    if (triggered) {
      ActiveTimeItr = ActiveTime;
    }
    Triggered = triggered;
  }
}
