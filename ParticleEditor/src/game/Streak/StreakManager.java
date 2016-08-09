package game.Streak;

import game.ParticleEditor;
import game.Camera;

import java.util.Vector;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Vector2f;

public class StreakManager {

  private Vector<Streak> myStreaks;

  public StreakManager() {
    myStreaks = new Vector<Streak>();
  }

  public void Update(float aElapsedTime) {

    for (Streak Streak : myStreaks) {
      Streak.Update(aElapsedTime);
    }

    // Remove Streaks
    for (int index = 0; index < myStreaks.size(); ++index) {
      if (myStreaks.elementAt(index).ShouldBeRemoved()) {
        myStreaks.remove(index);
      }
    }
  }

  public void Draw(Graphics g, Camera aCamera) {

    for (Streak Streak : myStreaks) {
      Streak.Render(g, aCamera);
    }
  }

  public Streak SpawnStreak(Vector2f aPosition, int aStreakId) {

    StreakType streakType = ParticleEditor.myStreakContainer.GetStreakByID(aStreakId);

    Streak newStreak = new Streak(aPosition, streakType);
    myStreaks.add(newStreak);

    return newStreak;
  }

  public Streak SpawnStreak(Vector2f aPosition, StreakType aStreakType) {

    Streak newStreak = new Streak(aPosition, aStreakType);
    myStreaks.add(newStreak);

    return newStreak;
  }

  public void RemoveAllStreaks() {
    myStreaks.removeAllElements();
  }
}
