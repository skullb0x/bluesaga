package data_handlers.ability_handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;

import org.newdawn.slick.Color;

import utils.ServerGameInfo;
import creature.Creature;
import creature.Creature.CreatureType;

public class Ability {
	
	int AbilityId;
	int dbId; // Id in character_ability table
	private String Name;
	private Color abilityColor;
	private int ManaCost;
	
	private int animationId = 0;
	
	private int GraphicsNr = 0;
	
	private int Cooldown = 0;
	private int CooldownLeft = 0;

	private int CastingSpeed = 100;
	
	private boolean Ready;
	private boolean ReadySent;

	private String Description;
	
	private int ClassId;
	private int classLevel;
	
	private int jobSkillId;
	
	private int FamilyId;
	
	private String AoE;
	private int Damage;
	private String DamageType;
	private int Range;
	private boolean Instant;
	private int Price;
	private String TargetType;
	private boolean TargetSelf;
	private Vector<StatusEffect> StatusEffects;
	private float WeaponDamageFactor;
	
	private int Delay;
	
	private String EquipReq;
	
	private String SpawnIds;
	
	private int ProjectileId;
	private int projectileEffectId;
	
	private boolean buffOrNot;
	
	
	// INFO ABOUT THE CASTER OF THE ABILITY
	private Creature Caster;
	
	public Ability() {
		
	}
	
	public Ability(Ability copy){
		AbilityId = copy.getAbilityId();

		Name = copy.getName();
		
		abilityColor = copy.getColor();
			
		animationId = copy.getAnimationId();
		
		ManaCost = copy.getManaCost();
		
		Cooldown = copy.getCooldown();
		CooldownLeft = 0;
		Ready = true;
		ReadySent = true;
				
		AoE = copy.getAoE();
		Damage = copy.getDamage();
		DamageType = copy.getDamageType();
		Range = copy.getRange();
		
		setDelay(copy.getDelay());
		
		WeaponDamageFactor = copy.getWeaponDamageFactor();

		setProjectileId(copy.getProjectileId());
		
		setInstant(copy.isInstant());
		
		setClassId(copy.getClassId());
		setFamilyId(copy.getFamilyId());
		
		jobSkillId = copy.getJobSkillId();
		
		Price = copy.getPrice();
		TargetType = copy.getTargetType();
		
		TargetSelf = copy.isTargetSelf();
		
		StatusEffects = new Vector<StatusEffect>();
		
		StatusEffects = copy.getStatusEffects();
		
		EquipReq = copy.getEquipReq();
		
		setGraphicsNr(copy.getGraphicsNr());
		
		setSpawnIds(copy.getSpawnIds());
		
		setDescription(copy.getDescription());
		
		setCastingSpeed(copy.getCastingSpeed());
		
		setClassLevel(copy.getClassLevel());
		
		setProjectileEffectId(copy.getProjectileEffectId());
		
		setBuffOrNot(copy.isBuffOrNot());
	}
	
	public void load(ResultSet rs){
		if(rs != null){
			try {
				AbilityId = rs.getInt("Id");
				
				Name = rs.getString("Name");
				
				ManaCost = rs.getInt("ManaCost");
				
				animationId = rs.getInt("AnimationId");
				
				Cooldown = rs.getInt("Cooldown");
				CooldownLeft = 0;
				Ready = true;
				ReadySent = true;
						
				AoE = rs.getString("AoE");
				Damage = rs.getInt("Damage");
				setDamageType(rs.getString("DamageType"));
				Range = rs.getInt("Range");
	
				setDelay(rs.getInt("Delay"));
				
				WeaponDamageFactor = rs.getFloat("WeaponDamageFactor");
	
				setProjectileId(rs.getInt("ProjectileId"));
				
				if(rs.getInt("Instant") == 1){
					Instant = true;
				}else{
					Instant = false;
				}
				
				setClassId(rs.getInt("ClassId"));
				setClassLevel(rs.getInt("ClassLevel"));
				
				jobSkillId = rs.getInt("JobSkillId");
				
				setFamilyId(rs.getInt("FamilyId"));
				
				String[] colorRGB = rs.getString("Color").split(",");
				
				if(rs.getString("Color").equals("0,0,0") && ServerGameInfo.classDef.get(getClassId()) != null){
					colorRGB = ServerGameInfo.classDef.get(getClassId()).bgColor.split(",");
				}
				abilityColor = new Color(Integer.parseInt(colorRGB[0]),Integer.parseInt(colorRGB[1]),Integer.parseInt(colorRGB[2]),255);
				
				Price = rs.getInt("Price");
				TargetType = rs.getString("TargetType");
				
				if(rs.getInt("TargetSelf") == 0){
					TargetSelf = false;
				}else{
					TargetSelf = true;
				}
				
				StatusEffects = new Vector<StatusEffect>();
				
				if(!rs.getString("StatusEffects").equals("None")){
					String StatusEffectsId[] = rs.getString("StatusEffects").split(";");
					
					for(String statusEffectInfo: StatusEffectsId){
						int statusEffectId = Integer.parseInt(statusEffectInfo);
						StatusEffect newSE = new StatusEffect(statusEffectId);
						StatusEffects.add(newSE);
					}
				}
				
				EquipReq = rs.getString("EquipReq");
				
				setGraphicsNr(rs.getInt("GraphicsNr"));
				
				setSpawnIds(rs.getString("SpawnIds"));
				
				setDescription(rs.getString("Description"));
				
				setCastingSpeed(rs.getInt("CastingSpeed"));
				
				setProjectileEffectId(rs.getInt("ProjectileEffectId"));
				
				if(rs.getInt("BuffOrNot") == 1){
					setBuffOrNot(true);
				}else{
					setBuffOrNot(false);
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}
	
	
	/*
	 * 
	 * 
	 *   COOLDOWN
	 * 
	 * 
	 */

	public void used(){
		CooldownLeft = Cooldown*5;
		Ready = false;
		ReadySent = false;
	}
	
	
	public int getCooldown(){
		return Cooldown;
	}


	public boolean isReady(){
		return Ready;
	}
	
	public boolean checkReady(){
		if(CooldownLeft > 0){
			CooldownLeft--;
		}else if(!Ready){
			CooldownLeft = 0;
			Ready = true;
		}
		if(Ready && !ReadySent){
			ReadySent = true;
			return true;
		}
		return false;
	}
	
	
	/*
	 * 
	 * 
	 * 	GETTERS AND SETTERS
	 * 
	 * 
	 */
	
	
	public void setDbId(int newDbId){
		dbId = newDbId;
	}
	
	
	public int getDbId(){
		return dbId;
	}
	
	
	public int getDamage() {
		return Damage;
	}
	
	
	public void setCaster(CreatureType newCasterType, Creature newCaster) {
		Caster = newCaster;
		
		for(Iterator<StatusEffect> iter = StatusEffects.iterator();iter.hasNext();)  
        {  
			StatusEffect s = iter.next();
			s.setCaster(Caster);
		}
	}
	
	public String getName() {
		return Name;
	}
	
	
	public Color getColor() {
		return abilityColor;
	}
	
	public Color getColorAlpha(int alpha){
		Color alphaColor = new Color(abilityColor.getRed(),abilityColor.getGreen(),abilityColor.getBlue(),alpha);
		return alphaColor;
	}

	public void setInstant(boolean instant){
		Instant = instant;
	}
	
	public boolean isInstant(){
		return Instant;
	}
	
	public int getAbilityId() {
		return AbilityId;
	}
	
	public String getAoE(){
		return AoE;
	}
	
	public int getManaCost(){
		return ManaCost;
	}
	
	public void setManaCost(int manaCost){
		this.ManaCost = manaCost;
	}
	
	public Vector<StatusEffect> getStatusEffects(){
		return StatusEffects;
	}
	
	public int getRange(){
		return Range;
	}
	
	public void setCooldownLeft(int newCooldownLeft){
		CooldownLeft = newCooldownLeft;
		if(CooldownLeft > 0){
			Ready = false;
			ReadySent = false;
		}
	}
	
	public int getCooldownLeft(){
		return CooldownLeft;
	}
	
	public String getTargetType(){
		return TargetType;
	}
	
	public boolean isTargetSelf(){
		return TargetSelf;
	}

	public Creature getCaster() {
		return Caster;
	}

	public void setCaster(Creature caster) {
		Caster = caster;
		for(Iterator<StatusEffect> iter = StatusEffects.iterator();iter.hasNext();)  
        {  
			StatusEffect SE = iter.next();
			SE.setCaster(Caster);
		}
	}

	public String getDamageType() {
		return DamageType;
	}

	public void setDamageType(String damageType) {
		DamageType = damageType;
	}

	public int getPrice() {
		return Price;
	}

	public float getWeaponDamageFactor() {
		return WeaponDamageFactor;
	}

	public void setWeaponDamageFactor(float weaponDamageFactor) {
		WeaponDamageFactor = weaponDamageFactor;
	}

	public String getEquipReq(){
		return EquipReq;
	}

	public int getProjectileId() {
		return ProjectileId;
	}

	public void setProjectileId(int projectileId) {
		ProjectileId = projectileId;
	}

	public int getGraphicsNr() {
		return GraphicsNr;
	}

	public void setGraphicsNr(int graphicsNr) {
		GraphicsNr = graphicsNr;
	}

	public String getSpawnIds() {
		return SpawnIds;
	}

	public void setSpawnIds(String spawnIds) {
		SpawnIds = spawnIds;
	}

	public int getDelay() {
		return Delay;
	}

	public void setDelay(int delay) {
		Delay = delay;
	}

	public int getClassId() {
		return ClassId;
	}

	public void setClassId(int classId) {
		ClassId = classId;
	}

	public int getFamilyId() {
		return FamilyId;
	}

	public void setFamilyId(int familyId) {
		FamilyId = familyId;
	}


	public int getClassLevel() {
		return classLevel;
	}

	public void setClassLevel(int classLevel) {
		this.classLevel = classLevel;
	}

	public String getDescription() {
		return Description;
	}

	public void setDescription(String description) {
		Description = description;
	}

	public int getAnimationId() {
		return animationId;
	}

	public void setAnimationId(int animationId) {
		this.animationId = animationId;
	}
	
	public int getCastingSpeed() {
		return CastingSpeed;
	}

	public void setCastingSpeed(int castingSpeed) {
		CastingSpeed = castingSpeed;
	}

	public int getJobSkillId() {
		return jobSkillId;
	}
	
	public int getProjectileEffectId() {
		return projectileEffectId;
	}

	public void setProjectileEffectId(int projectileEffectId) {
		this.projectileEffectId = projectileEffectId;
	}

	public boolean isBuffOrNot() {
		return buffOrNot;
	}

	public void setBuffOrNot(boolean buffOrNot) {
		this.buffOrNot = buffOrNot;
	}
}