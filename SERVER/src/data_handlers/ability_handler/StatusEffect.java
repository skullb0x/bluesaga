package data_handlers.ability_handler;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.newdawn.slick.Color;

import components.Stats;

import creature.Creature;
import network.Server;

public class StatusEffect {

	private int Id;
	private String Name;
	private Stats StatsModif;
	private int Duration; // Duration in seconds
	private int RepeatDamage;
	private String RepeatDamageType;
	
	private int GraphicsNr;
	private int AnimationId;
	
	private String Sfx;
	
	private Ability ability;
	
	private Creature Caster;
	
	private boolean Active;
	private Color SEColor;
	private int classId;
	
	private int ActiveTimeItr = 0;
	private int ActiveTimeEnd;
	
	public StatusEffect(int newId){
		setId(newId);
		
		ability = null;
		
		Caster = null;
		
		ResultSet rs = Server.gameDB.askDB("select * from ability_statuseffect where Id = "+newId);
		
		try {
			if(rs.next()){
				setName(rs.getString("Name"));
				StatsModif = new Stats();
				StatsModif.reset();
				
				if(!rs.getString("StatsModif").equals("None")){
					String allStatsModifInfo[] = rs.getString("StatsModif").split(";");
					for(String statsModifInfo: allStatsModifInfo){
							String statsModifSplit[] = statsModifInfo.split(":");
							String statsType = statsModifSplit[0];
							int statsEffect = Integer.parseInt(statsModifSplit[1]);
							StatsModif.setValue(statsType, statsEffect);
					}
				}
				
				setGraphicsNr(rs.getInt("GraphicsNr"));
				setAnimationId(rs.getInt("AnimationId"));
				
				setDuration(rs.getInt("Duration"));
				setRepeatDamage(rs.getInt("RepeatDamage"));
				setRepeatDamageType(rs.getString("RepeatDamageType"));
				String colorInfo[] = rs.getString("Color").split(",");
				SEColor = new Color(Integer.parseInt(colorInfo[0]),Integer.parseInt(colorInfo[1]),Integer.parseInt(colorInfo[2]));
				setClassId(rs.getInt("ClassId"));
				setSfx(rs.getString("Sfx"));
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		ActiveTimeEnd = getDuration(); 
		Active = true;
	}

	public void start(){
		ActiveTimeItr = 0;
		setActive(true);
	}
		
	
	public String getName() {
		return Name;
	}

	public void setName(String name) {
		Name = name;
	}

	public int getDuration() {
		return Duration;
	}

	public void setDuration(int duration) {
		Duration = duration;
	}

	public int getRepeatDamage() {
		return RepeatDamage;
	}

	public void setRepeatDamage(int repeatDamage) {
		RepeatDamage = repeatDamage;
	}

	public String getRepeatDamageType() {
		return RepeatDamageType;
	}

	public void setRepeatDamageType(String repeatDamageType) {
		RepeatDamageType = repeatDamageType;
	}
	
	public Stats getStatsModif(){
		return StatsModif;
	}

	public int getId() {
		return Id;
	}

	public void setId(int id) {
		Id = id;
	}

	public boolean isActive() {
		ActiveTimeItr++;
		
		if(ActiveTimeItr >= ActiveTimeEnd){
			Active = false;
		}
		return Active;
	}

	public void setActive(boolean active) {
		Active = active;
	}

	public Creature getCaster() {
		return Caster;
	}

	public void setCaster(Creature caster) {
		Caster = caster;
	}
	
	public Color getColor(){
		return SEColor;
	}

	public int getSkillId() {
		return classId;
	}

	public void setClassId(int classId) {
		this.classId = classId;
	}

	public int getGraphicsNr() {
		return GraphicsNr;
	}

	public void setGraphicsNr(int graphicsNr) {
		GraphicsNr = graphicsNr;
	}

	public int getAnimationId() {
		return AnimationId;
	}

	public void setAnimationId(int animationId) {
		AnimationId = animationId;
	}

	public String getSfx() {
		return Sfx;
	}

	public void setSfx(String sfx) {
		Sfx = sfx;
	}

	public Ability getAbility() {
		return ability;
	}

	public void setAbility(Ability ability) {
		this.ability = ability;
	}
}