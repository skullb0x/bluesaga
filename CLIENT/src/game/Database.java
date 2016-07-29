package game;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class Database {

	private Connection conn;

	public Database() throws ClassNotFoundException {
		Class.forName("org.sqlite.JDBC");

		try {
			if(!ClientSettings.DEV_MODE){
				conn = DriverManager.getConnection("jdbc:sqlite:libs/gameDB.db");
			}else{
				conn = DriverManager.getConnection("jdbc:sqlite:gameDB.db");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}




	public void updateDB(String sqlStatement){
		try {
			Statement stat = conn.createStatement();

			stat.execute(sqlStatement);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public ResultSet askDB(String sqlStatement){
		try {
			Statement stat = conn.createStatement();

			if(stat.execute(sqlStatement)){
				return stat.getResultSet();
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}	
		return null;
	}

	public void loadOptions(){
		ResultSet optionRS = askDB("select GameValue from option where GameOption = 'Music'");
		try {
			if(optionRS.next()){
				int music_on = optionRS.getInt("GameValue");

				if(music_on == 1){
					ClientSettings.MUSIC_ON = true;
				}else{
					ClientSettings.MUSIC_ON = false;
				}
			}
			optionRS.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		optionRS = askDB("select GameValue from option where GameOption = 'Sfx'");
		try {
			if(optionRS.next()){
				int sfx_on = optionRS.getInt("GameValue");

				if(sfx_on == 1){
					ClientSettings.SFX_ON = true;
				}else{
					ClientSettings.SFX_ON = false;
				}
			}
			optionRS.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		optionRS = askDB("select GameValue from option where GameOption = 'MusicVol'");
		try {
			if(optionRS.next()){
				float music_vol = optionRS.getFloat("GameValue");
				ClientSettings.musicVolume = music_vol;
			}
			optionRS.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		optionRS = askDB("select GameValue from option where GameOption = 'SfxVol'");
		try {
			if(optionRS.next()){
				float sfx_vol = optionRS.getFloat("GameValue");
				ClientSettings.soundVolume = sfx_vol;
			}
			optionRS.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		optionRS = askDB("select GameValue from option where GameOption = 'Fullscreen'");
		try {
			if(optionRS.next()){
				int fullscreen = optionRS.getInt("GameValue");

				if(fullscreen == 1){
					ClientSettings.FULL_SCREEN = true;
				}else{
					ClientSettings.FULL_SCREEN = false;
				}
			}
			optionRS.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
/*
	public void saveMiniMap(HashMap<String,MiniMapTile> NewTiles){

		try {
			Statement tilesInsert = conn.createStatement();
			Statement tilesUpdate = conn.createStatement();
			
			//PreparedStatement newTiles = conn.prepareStatement("insert into mini_map (CharacterId, X, Y, Z, Color) values (?,?,?,?,?)");
			//PreparedStatement updateTiles = conn.prepareStatement("update mini_map set Color = ? where x = ? and y = ? and z = ?");

			int totalTiles = NewTiles.size();
			int savedTiles = 0;
			Iterator<Entry<String, MiniMapTile>> it = NewTiles.entrySet().iterator();
			
			final int batchSize = 50;
			int batchCount = 0;
			
			while (it.hasNext()) {

				Entry<String,MiniMapTile> pairs = it.next();

				if(((MiniMapTile) pairs.getValue()).isNew()){
					// Save new tile
					String[] coord = pairs.getKey().toString().split(",");
					int x = Integer.parseInt(coord[0]);
					int y = Integer.parseInt(coord[1]);
					int z = Integer.parseInt(coord[2]);
					Color c = ((MiniMapTile) pairs.getValue()).getColor();
					String color = c.getRed()+","+c.getGreen()+","+c.getBlue();

					tilesInsert.addBatch("insert into mini_map (CharacterId, X, Y, Z, Color) values ("+BlueSaga.playerCharacter.getDBId()+","+x+","+y+","+z+",'"+color+"')");
				
				}else if(((MiniMapTile) pairs.getValue()).isUpdate()){
					// Update tile
					String[] coord = pairs.getKey().toString().split(",");
					int x = Integer.parseInt(coord[0]);
					int y = Integer.parseInt(coord[1]);
					int z = Integer.parseInt(coord[2]);

					Color c = ((MiniMapTile) pairs.getValue()).getColor();
					String color = c.getRed()+","+c.getGreen()+","+c.getBlue();
					tilesUpdate.addBatch("update mini_map set Color = '"+color+"' where x = "+x+" and y = "+y+" and z = "+z);
				}
				savedTiles++;
				batchCount++;
				
				if(batchCount % batchSize == 0){
					tilesUpdate.executeBatch();
					tilesInsert.executeBatch();
					tilesUpdate.clearBatch();
					tilesInsert.clearBatch();
				}
				int percent = (int) Math.ceil(((float) savedTiles / (float) totalTiles) * 100.0f);
				ViewHandler.LoadingStatus = "Saving minimap "+percent+"%";
			}
			tilesUpdate.executeBatch();
			tilesInsert.executeBatch();
			tilesUpdate.close();
			tilesInsert.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
*/

	public void closeDB(){
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		conn = null;

	}
}
