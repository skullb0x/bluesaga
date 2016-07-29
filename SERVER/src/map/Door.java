package map;

public class Door {
	private int x;
	private int y;
	private int z;
	
	private int gotoX;
	private int gotoY;
	private int gotoZ;
	
	public Door(int x, int y, int z, int gotoX, int gotoY, int gotoZ){
		this.x = x;
		this.y = y;
		this.z = z;
		
		this.gotoX = gotoX;
		this.gotoY = gotoY;
		this.gotoZ = gotoZ;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getZ() {
		return z;
	}

	public void setZ(int z) {
		this.z = z;
	}

	public int getGotoX() {
		return gotoX;
	}

	public void setGotoX(int gotoX) {
		this.gotoX = gotoX;
	}

	public int getGotoY() {
		return gotoY;
	}

	public void setGotoY(int gotoY) {
		this.gotoY = gotoY;
	}

	public int getGotoZ() {
		return gotoZ;
	}

	public void setGotoZ(int gotoZ) {
		this.gotoZ = gotoZ;
	}
	
	
}
