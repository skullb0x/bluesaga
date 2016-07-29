package map;

/****************************************
*                                       *
*           WORLDMAP / EDITOR	        *
*                                       *
*                                       *
****************************************/


import game.BP_EDITOR;
import java.sql.ResultSet;
import java.sql.SQLException;


public class WorldMap  {
	private int MapSize;
	private int MapId;
	
	private String Name;

	
	/****************************************
	*                                       *
	*           INIT/LOAD INFO		        *
	*                                       *
	*                                       *
	****************************************/
	
	public WorldMap() {
		/* 
		try { 
			TILE_MAP = new TiledMap("maps/forest.tmx"); 
		} catch (SlickException e) { 
			e.printStackTrace(); 
		}
		*/
		
	}
	
	
	public void loadMap(int mapId) {
		ResultSet rs = BP_EDITOR.mapDB.askDB("select Id, Name from area where Id = " + mapId);

		try {
			if(rs.next()){
				MapId = rs.getInt("Id");
				Name = rs.getString("Name");
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	/****************************************
	*                                       *
	*           GETTER/SETTER		        *
	*                                       *
	*                                       *
	****************************************/
	
	public String getClickedTile(int mouseX, int mouseY, int PlayerX, int PlayerY){
		String mouseInfo = "0";
		
		int tileX = PlayerX + (int) Math.floor(mouseX/ 50) - 10;
		int tileY = PlayerY - (int) Math.floor(mouseY/ 50) + 6;
		
		mouseInfo = tileX + ";" + tileY;
		
		return mouseInfo;
	}
	
	
	
	public String getName() {
		return Name;
	}
	

	public int getMapId() {
		return MapId;
	}
	
	public void setMapId(int newId){
		MapId = newId;
	}

	public int getMapSize() {
		return MapSize;
	}
	
	public void setMapSize(int newSize){
		MapSize = newSize;
	}
	

}
