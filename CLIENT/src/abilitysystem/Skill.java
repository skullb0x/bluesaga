package abilitysystem;

public class Skill {

	private int Id;
	private String Name;

	private int Level;
	private int SP;
	private int SPnext;
	
	
	public Skill(int newId){
		setId(newId);
	}


	public void addSP(int addedSP){
		SP += addedSP;
		if(SP > SPnext){
			SP = SPnext;
		}
	}
	
	

	public String getName() {
		return Name;
	}


	public void setName(String name) {
		Name = name;
	}


	public int getLevel() {
		return Level;
	}


	public void setLevel(int level) {
		Level = level;
	}


	public int getSP() {
		return SP;
	}
	
	public void setSP(int sp){
		SP = sp;
	}

	public void setSPnext(int nextSP){
		SPnext = nextSP;
	}
	
	public int getSPBarWidth(int Max) {
		float fSP = SP;
		float fMaxSP = SPnext;
		float spBarWidth = (fSP / fMaxSP) * Max;
		return Math.round(spBarWidth);
	}

	public int getId() {
		return Id;
	}


	public void setId(int id) {
		Id = id;
	}
}