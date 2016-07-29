package particlesystem.Streak;


import game.Database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.newdawn.slick.Color;

public class StreakContainer {
	
	private HashMap<Integer, StreakType> myStreakTypes;

	public StreakContainer(Database aGameDB) {
		CreateStreakFromDB(aGameDB);
	}
	
	private void CreateStreakFromDB(Database aGameDB){
		
		myStreakTypes = new HashMap<Integer, StreakType>();
		
		ResultSet result = aGameDB.askDB("select * from Streak");
		
		try {
			
			while (result.next()) {

				StreakType newStreakType = new StreakType();
				GetStreakTypeFromDB(newStreakType, result);
				myStreakTypes.put(newStreakType.myID, newStreakType);
			   
			}
			result.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void GetStreakTypeFromDB( StreakType aStreakType, ResultSet aStreakResult) throws SQLException {

		aStreakType.myID 					= aStreakResult.getInt("Id");
		aStreakType.myName 					= aStreakResult.getString("Name");
		aStreakType.myQuality				= aStreakResult.getFloat("Quality");
		aStreakType.myLifetime				= aStreakResult.getFloat("Lifetime");
		aStreakType.myWidth					= aStreakResult.getFloat("Width");
		
		//GetColors
		String[] startColorRGBA = aStreakResult.getString("StartColor").split(",");		
		aStreakType.myStartColor = new Color(Integer.parseInt(startColorRGBA[0]),Integer.parseInt(startColorRGBA[1]),Integer.parseInt(startColorRGBA[2]),Integer.parseInt(startColorRGBA[3]));
		String[] endColorRGBA = aStreakResult.getString("EndColor").split(",");
		aStreakType.myEndColor = new Color(Integer.parseInt(endColorRGBA[0]),Integer.parseInt(endColorRGBA[1]),Integer.parseInt(endColorRGBA[2]),Integer.parseInt(endColorRGBA[3]));
	
	}

	public StreakType GetStreakByID(int aStreakID) {
		return myStreakTypes.get(aStreakID);
	}
	
}