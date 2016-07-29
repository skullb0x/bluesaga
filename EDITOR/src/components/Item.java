package components;

import game.Database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;



public class Item {
	
	private int Id;			// Id in item table
	private String Name;
	private String Type;
	private String SubType;
	private String Material;
	private String Family;
	private String Color;
	
	private int dbId; 			// Id in user_item table
	private int CrewMemberId;		// CrewMemberId in user_item table
	
	private boolean equipable;
	
	private int nrSlots;
	
	private int Value;
	
	private HashMap<Integer,AbilitySlot> AbilitySlots = new HashMap<Integer, AbilitySlot>();
	
	private String attackType;
	
	private HashMap<String, Integer> Stats = new HashMap<String, Integer>(); 
	private HashMap<String, Integer> Requirements = new HashMap<String, Integer>(); 

	
	public Item(int newId, Database gameDB) {
		
		Id = newId;
	
		equipable = false;
		
		try {
			ResultSet rs = gameDB.askDB("select * from item where Id = "+newId);
			
			while (rs.next()) {
				Name = rs.getString("Name");
				Type = rs.getString("Type");
				SubType = rs.getString("SubType");
				Material = rs.getString("Material");
				
				attackType = rs.getString("AttackType");
				
				// PRIMARY STATS
				Stats.put("ATTACK",rs.getInt("ATTACK"));
			    Stats.put("MAGIC",rs.getInt("MAGIC"));
			    Stats.put("ATTACK_DEF",rs.getInt("ATTACK_DEF"));
				Stats.put("MAGIC_DEF",rs.getInt("MAGIC_DEF"));
			    Stats.put("SPEED",rs.getInt("SPEED"));
			   
			    // SECONDARY STATS
			    Stats.put("CRITICAL_HIT",rs.getInt("CRITICAL_HIT"));
			    Stats.put("EVASION",rs.getInt("EVASION"));
			    Stats.put("ACCURACY",rs.getInt("ACCURACY"));
			    
		    	// HEALTH AND MANA
			    Stats.put("MAX_HEALTH",rs.getInt("MAX_HEALTH"));
				Stats.put("MAX_MANA",rs.getInt("MAX_MANA"));
				
				Stats.put("HEALTH", rs.getInt("HEALTH"));
				Stats.put("MANA", rs.getInt("MANA"));
				
			    // MAGIC STATS
			    Stats.put("FIRE_DEF",rs.getInt("FIRE_DEF"));
			    Stats.put("FIRE_ATK",rs.getInt("FIRE_ATK"));
			    Stats.put("COLD_DEF",rs.getInt("COLD_DEF"));
			    Stats.put("COLD_ATK",rs.getInt("COLD_ATK"));
			    Stats.put("SHOCK_DEF",rs.getInt("SHOCK_DEF"));
			    Stats.put("SHOCK_ATK",rs.getInt("SHOCK_ATK"));
			    Stats.put("POISON_DEF",rs.getInt("POISON_DEF"));
			    Stats.put("POISON_ATK",rs.getInt("POISON_ATK"));
			    
			    nrSlots = rs.getInt("NrSlots");
			    Value = rs.getInt("Value");
			    
			    Requirements.put("ReqLevel", rs.getInt("ReqLevel"));
			    Requirements.put("ReqAttack", rs.getInt("ReqAttack"));
			    Requirements.put("ReqMagic", rs.getInt("ReqMagic"));
			    Requirements.put("ReqSpeed", rs.getInt("ReqSpeed"));
				
			    if(Type.equals("Head") || Type.equals("Weapon") 
					    || Type.equals("Chest") || Type.equals("Legs") 
					    || Type.equals("Feet") || Type.equals("Misc")){
			    	equipable = true;
				}
			    
			    for(int i = 0; i < nrSlots; i++){
			    	AbilitySlots.put(i,new AbilitySlot());
			    }
			}
			rs.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		
    }
	
	
	
	public String getImageSrc(){
		String filename = "images/items/";
		
		if(Type.equals("Weapon")){
			filename += "weapons/"+Material+"_"+SubType;
		}else if(Type.equals("Head")){
			filename += "helmets/"+Material+"_"+SubType;
		}else {
			filename = "none";
		}
		
		if(filename != "none"){
			filename += ".png";
		}
		return filename;
		
	}
	
	
	public void equip(int creatureId) {
		CrewMemberId = creatureId;
	}
	
	public void unEquip() {
		CrewMemberId = 0;
	}
	
	
	public AbilitySlot getAbilitySlot(int index){
		return AbilitySlots.get(index);
	}
	
	
	
	 /****************************************
	 *                                      *
	 *             GETTER/SETTER            *
	 *                                      *
	 *                                      *
	 ****************************************/	

	public HashMap<String, Integer> getStats() {
		return Stats;
	}
	
	public int getStatEffect(String StatType){
		return Stats.get(StatType).intValue();
	}
	
	
	public int getUserItemId() {
		return dbId;
	}
	
	public void setUserItemId(int newUserItemId) {
		dbId = newUserItemId;
	}
	
	public int getCrewMemberId(){
		return CrewMemberId;
	}
	
	
	public String getName() {
		return Name;
	}

	public void setName(String name) {
		Name = name;
	}

	public String getType() {
		return Type;
	}

	public void setType(String type) {
		Type = type;
	}

	public String getSubType() {
		return SubType;
	}

	public void setSubType(String subType) {
		SubType = subType;
	}

	public String getMaterial() {
		return Material;
	}

	public void setMaterial(String material) {
		Material = material;
	}

	public String getFamily() {
		return Family;
	}

	public void setFamily(String family) {
		Family = family;
	}

	public String getColor() {
		return Color;
	}

	public void setColor(String color) {
		Color = color;
	}
	
	
	public int getNrSlots(){
		return nrSlots;
	}
	
	public int getId() {
		return Id;
	}
	
	public int getValue() {
		return Value;
	}
	
	public String getAttackType() {
		return attackType;
	}
	
	public boolean isEquipable(){
		return equipable;
	}	
	
	public int getRequirement(String Type){
		return Requirements.get(Type);
	}
	
	public HashMap<String, Integer> getRequirements(){
		return Requirements;
	}
}
