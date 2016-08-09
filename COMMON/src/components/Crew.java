package components;

public class Crew {

  private int Id = 0;
  private String Name = "0";
  private String MemberState = "0";

  public Crew(int newId) {
    setId(newId);
  }

  public String getName() {
    return Name;
  }

  public void setName(String name) {
    Name = name;
  }

  public int getId() {
    return Id;
  }

  public void setId(int id) {
    Id = id;
  }

  public String getMemberState() {
    return MemberState;
  }

  public void setMemberState(String memberState) {
    MemberState = memberState;
  }
}
