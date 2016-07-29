package map;

public class Moveable {
	private String Name;
	
	private int SpawnX;
	private int SpawnY;
	private int SpawnZ;
	
	private int X;
	private int Y;
	private int Z;
	
	private int respawnItr = 0;
	private int respawnItrEnd = 3;
	
	public Moveable(String newName, int x, int y, int z){
		setName(newName);
		setX(x);
		setY(y);
		setZ(z);
		setSpawnX(x);
		setSpawnY(y);
		setSpawnZ(z);
	}

	public int getSpawnX() {
		return SpawnX;
	}

	public void setSpawnX(int spawnX) {
		SpawnX = spawnX;
	}

	public int getSpawnY() {
		return SpawnY;
	}

	public void setSpawnY(int spawnY) {
		SpawnY = spawnY;
	}

	public int getSpawnZ() {
		return SpawnZ;
	}

	public void setSpawnZ(int spawnZ) {
		SpawnZ = spawnZ;
	}

	public int getX() {
		return X;
	}

	public void setX(int x) {
		X = x;
	}

	public int getY() {
		return Y;
	}

	public void setY(int y) {
		Y = y;
	}

	public int getZ() {
		return Z;
	}

	public void setZ(int z) {
		Z = z;
	}
	
	public boolean checkRespawn(){
		boolean respawn = false;
		if(getX() == getSpawnX() && getY() == getSpawnY() && getZ() == getSpawnZ()){
			
		}else{
			respawnItr++;
			if(respawnItr >= respawnItrEnd){
				respawn();
				respawn = true;
			}
		}
		return respawn;
	}
	
	public void respawn(){
		setX(getSpawnX());
		setY(getSpawnY());
		setZ(getSpawnZ());
		respawnItr = 0;
		
	}

	public void resetRespawnTimer(){
		respawnItr = 0;
	}
	
	public String getName() {
		return Name;
	}

	public void setName(String name) {
		Name = name;
	}
}
