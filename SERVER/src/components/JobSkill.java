package components;

import java.sql.ResultSet;
import java.sql.SQLException;
import utils.XPTables;

public class JobSkill {

  private int Id;
  private String Name;

  private String Type;

  private int Level;
  private int SP;

  public JobSkill() {}

  public JobSkill(JobSkill copy) {
    setId(copy.getId());
    SP = 0;
    Level = 1;
    setName(copy.getName());
    setType(copy.getType());
  }

  public void load(ResultSet rs) {
    if (rs != null) {
      try {
        Id = rs.getInt("Id");
        SP = 0;
        Level = 1;
        Name = rs.getString("Name");
        Type = rs.getString("Type");
      } catch (SQLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  public boolean addSP(int addedSP) {
    SP += addedSP;
    boolean levelUp = false;

    while (SP >= XPTables.nextLevelSP.get(Level + 1)) {
      SP = SP - XPTables.nextLevelSP.get(Level + 1);
      Level++;
      levelUp = true;
    }

    return levelUp;
  }

  public void setId(int id) {
    Id = id;
  }

  public int getId() {
    return Id;
  }

  public String getName() {
    return Name;
  }

  public void setName(String name) {
    Name = name;
  }

  public int getLevel() {
    return Level;
  }

  public void setLevel(int level) {
    Level = level;
  }

  public int getSP() {
    return SP;
  }

  public void setSP(int sp) {
    SP = sp;
  }

  public String getType() {
    return Type;
  }

  public void setType(String type) {
    Type = type;
  }
}
