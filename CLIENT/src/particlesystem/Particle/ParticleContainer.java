package particlesystem.Particle;

import game.Database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.newdawn.slick.Color;
import org.newdawn.slick.geom.Vector2f;

public class ParticleContainer {

  private HashMap<Integer, ParticleType> myParticleTypes;

  public ParticleContainer(Database aGameDB) {
    CreateParticleFromDB(aGameDB);
  }

  private void CreateParticleFromDB(Database aGameDB) {

    myParticleTypes = new HashMap<Integer, ParticleType>();

    ResultSet result = aGameDB.askDB("select * from Particle");

    try {

      while (result.next()) {

        ParticleType newParticleType = new ParticleType();
        GetParticleTypeFromDB(newParticleType, result);
        myParticleTypes.put(newParticleType.myID, newParticleType);
      }
      result.close();

    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public ParticleType GetParticleByID(int aParticleID) {
    return myParticleTypes.get(aParticleID);
  }

  public void GetParticleTypeFromDB(ParticleType aParticleType, ResultSet aParticleResult)
      throws SQLException {

    aParticleType.myID = aParticleResult.getInt("Id");
    aParticleType.myName = aParticleResult.getString("Name");

    String[] minDir = aParticleResult.getString("MinDir").split(",");
    aParticleType.myMinDir = new Vector2f(Float.parseFloat(minDir[0]), Float.parseFloat(minDir[1]));
    String[] maxDir = aParticleResult.getString("MaxDir").split(",");
    aParticleType.myMaxDir = new Vector2f(Float.parseFloat(maxDir[0]), Float.parseFloat(maxDir[1]));

    aParticleType.myMinAxisRotSpeed = aParticleResult.getFloat("MinAxisRotSpeed");
    aParticleType.myMaxAxisRotSpeed = aParticleResult.getFloat("MaxAxisRotSpeed");
    aParticleType.myRotationSpeed = aParticleResult.getFloat("RotationSpeed");
    aParticleType.myMinScale = aParticleResult.getFloat("MinScale");
    aParticleType.myMaxScale = aParticleResult.getFloat("MaxScale");
    aParticleType.myLifetime = aParticleResult.getFloat("Lifetime");
    aParticleType.myImageString = aParticleResult.getString("ImageString");
    aParticleType.myVerticalGravity = aParticleResult.getFloat("VerticalGravity");
    aParticleType.myHorizontalGravity = aParticleResult.getFloat("HorizontalGravity");

    //GetColors
    String[] startColorRGB = aParticleResult.getString("StartColor").split(",");
    aParticleType.myStartColor =
        new Color(
            Integer.parseInt(startColorRGB[0]),
            Integer.parseInt(startColorRGB[1]),
            Integer.parseInt(startColorRGB[2]),
            255);
    String[] endColorRGB = aParticleResult.getString("EndColor").split(",");
    aParticleType.myEndColor =
        new Color(
            Integer.parseInt(endColorRGB[0]),
            Integer.parseInt(endColorRGB[1]),
            Integer.parseInt(endColorRGB[2]),
            255);

    aParticleType.myFadeSpeed = aParticleResult.getFloat("FadeSpeed");
  }
}
