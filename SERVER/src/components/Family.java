package components;

public class Family {
	private int id;
	private String name;
	private int enemyId;
	
	public Family(int id, String name, int enemyId){
		setId(id);
		setName(name);
		setEnemyId(enemyId);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getEnemyId() {
		return enemyId;
	}

	public void setEnemyId(int enemyId) {
		this.enemyId = enemyId;
	}
}
