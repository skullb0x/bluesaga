package particlesystem.Emitter;

import game.Database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.newdawn.slick.geom.Vector2f;

public class EmitterContainer {

  HashMap<String, EmitterType> myEmitterTypes;

  public EmitterContainer(Database aGameDB) {
    CreateEmittersFromDB(aGameDB);
  }

  private void CreateEmittersFromDB(Database aGameDB) {

    myEmitterTypes = new HashMap<String, EmitterType>();

    ResultSet result = aGameDB.askDB("select * from Emitter");

    try {

      while (result.next()) {

        EmitterType newEmitterType = new EmitterType();
        GetEmitterTypeFromDB(newEmitterType, result);
        myEmitterTypes.put(newEmitterType.myName, newEmitterType);
      }
      result.close();

    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public EmitterType GetEmitterByName(String aEmitterName) {
    return myEmitterTypes.get(aEmitterName);
  }

  public void GetEmitterTypeFromDB(EmitterType aEmitterType, ResultSet aEmitterResult)
      throws SQLException {

    aEmitterType.myID = aEmitterResult.getInt("Id");
    aEmitterType.myName = aEmitterResult.getString("Name");
    aEmitterType.myLifetime = aEmitterResult.getFloat("Lifetime");
    aEmitterType.myIsUnlimited = false;

    if (aEmitterType.myLifetime == 0.0f) {
      aEmitterType.myIsUnlimited = true;
    }
    aEmitterType.myEmittionRate = aEmitterResult.getFloat("EmittionRate");
    aEmitterType.myRotationSpeed = aEmitterResult.getFloat("RotationSpeed");

    String[] minPos = aEmitterResult.getString("MinPos").split(",");
    aEmitterType.myMinPos = new Vector2f(Float.parseFloat(minPos[0]), Float.parseFloat(minPos[1]));
    String[] maxPos = aEmitterResult.getString("MaxPos").split(",");
    aEmitterType.myMaxPos = new Vector2f(Float.parseFloat(maxPos[0]), Float.parseFloat(maxPos[1]));

    aEmitterType.myShouldShowParticles = aEmitterResult.getBoolean("ShowParticle");
    aEmitterType.myParticleID = aEmitterResult.getInt("ParticleId");
    aEmitterType.myShouldShowStreaks = aEmitterResult.getBoolean("ShowStreak");
    aEmitterType.myStreakID = aEmitterResult.getInt("StreakId");
  }
}
