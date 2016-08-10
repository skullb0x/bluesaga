package instances;

import java.util.Vector;

public class MonsterGroup {
  private Vector<Integer> monsters;

  public MonsterGroup() {
    monsters = new Vector<Integer>();
  }

  public void addMonsterId(int monsterId) {
    monsters.add(monsterId);
  }

  public Vector<Integer> getMonsterIds() {
    return monsters;
  }
}
