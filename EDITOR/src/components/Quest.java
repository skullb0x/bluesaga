package components;

import game.Database;

public class Quest {

  private int Id;
  private int Completed;

  private String Name;
  private String QuestMessage;
  private String RewardMessage;

  private int Level;
  private String Type;
  private int ActiveNumber;
  private int TargetNumber;
  private String TargetType;
  private int TargetId;

  private int NrQuestLines;
  private int NrRewardLines;

  private int NpcId;

  public Quest(int newId) {
    Id = newId;
    ActiveNumber = 0;
  }

  public String justifyLeft(int width, String st) {
    StringBuffer buf = new StringBuffer(st);
    int lastspace = -1;
    int linestart = 0;
    int i = 0;

    while (i < buf.length()) {
      if (buf.charAt(i) == ' ') lastspace = i;
      if (buf.charAt(i) == '\n') {
        lastspace = -1;
        linestart = i + 1;
      }
      if (i > linestart + width - 1) {
        if (lastspace != -1) {
          buf.setCharAt(lastspace, '\n');
          linestart = lastspace + 1;
          lastspace = -1;
        } else {
          buf.insert(i, '\n');
          linestart = i + 1;
        }
      }
      i++;
    }

    return buf.toString();
  }

  public void loadQuest(Database gameDB) {}

  /****************************************
   *                                      *
   *             GETTER/SETTER            *
   *                                      *
   *                                      *
   ****************************************/
  public String getName() {
    return Name;
  }

  public int getId() {
    return Id;
  }

  public int getCompleted() {
    return Completed;
  }

  public int getLevel() {
    return Level;
  }

  public String getType() {
    return Type;
  }

  public int getTargetNumber() {
    return TargetNumber;
  }

  public String getTargetType() {
    return TargetType;
  }

  public int getTargetId() {
    return TargetId;
  }

  public int getNrQuestLines() {
    return NrQuestLines;
  }

  public int getNrRewardLines() {
    return NrRewardLines;
  }

  public void setActiveNumber(int newNumber) {
    ActiveNumber = newNumber;
  }

  public int getActiveNumber() {
    return ActiveNumber;
  }

  public String getQuestMessage() {
    return QuestMessage;
  }

  public String getRewardMessage() {
    return RewardMessage;
  }

  public void setTargetNumber(int newNr) {
    TargetNumber = newNr;
  }

  public void setCompleted(int newStatus) {
    Completed = newStatus;
  }

  public int getNpcId() {
    return NpcId;
  }
}
