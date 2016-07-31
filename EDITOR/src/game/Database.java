package game;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import components.Monster;

import map.Container;
import map.Tile;


public class Database {

	private Connection conn;
	public Database(String name) throws ClassNotFoundException {
		Class.forName("org.sqlite.JDBC");
		
		try {
			if(name.equals("mapDB")){
				conn = DriverManager.getConnection("jdbc:sqlite:"+name+".db");
			}else{
				conn = DriverManager.getConnection("jdbc:sqlite:../SERVER/"+name+".db");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void saveTile(int AreaId, Tile newTile, int x,int y){
		updateDB("update area_tile set Type = '"+newTile.getType()+"', Number = "+newTile.getName()+" where X = "+x+" and Y = "+y+" and AreaId = "+AreaId);
	}
	
	
	public void saveTiles(int AreaId, Tile newTile, int x,int y, int brushSize){
	
		updateDB("update area_tile set Type = '"+newTile.getType()+"', Number = "+newTile.getName()+" where X >= "+x+" and X < "+(x+brushSize)+" and Y <= "+y+" and Y > "+(y-brushSize)+" and AreaId = "+AreaId);
	}
	
	public int addDoor(int areaId, int x, int y){
		int doorId = 0;
		updateDB("insert into door (AreaId, X, Y, GotoX, GotoY, GoToAreaId, Locked) values ("+areaId+","+x+","+y+",0,0,0,0)");
		ResultSet rs = askDB("select Id from door order by Id desc limit 1");
		try {
			if(rs.next()){
				doorId = rs.getInt("Id");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return doorId;
	}
	
	public int checkIfDoor(int areaId, int x, int y){
		ResultSet rs = askDB("select Id from door where AreaId = "+areaId+" and X = "+x+" and Y = "+y);
		try {
			if(rs.next()){
				return rs.getInt("Id");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public void deleteMap(int AreaId){
		updateDB("delete from area_tile where AreaId = "+AreaId);
		updateDB("delete from area_creature where AreaId = "+AreaId);
		updateDB("delete from area_container where AreaId = "+AreaId);
	}
	
	
	public void saveTileObject(int AreaId, Container newContainer, int x,int y){
		
		updateDB("update container set Type = '"+newContainer.getType()+"', X = "+x+", Y = '"+y+"' where X = "+x+" and Y = "+y+" and AreaId = "+AreaId);
	}
	
	public void addMonster(int AreaId, Monster newMonster, int x, int y){
		updateDB("insert into area_creature (AreaId,CreatureId,MobLevel, SpawnX,SpawnY,Special,AggroType, NpcName) values ("+AreaId+","+newMonster.getId()+",1,"+x+","+y+",'"+newMonster.getSpecialType()+"',2,'None')");
	}
	
	public void removeMonster(int AreaId, int x, int y){
		updateDB("delete from area_creature where AreaId = "+AreaId+" and SpawnX = "+x+" and SpawnY = "+y);
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

	
}
