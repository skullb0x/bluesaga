package abilitysystem;
import graphics.Font;
import graphics.ImageResource;

import java.util.Vector;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import utils.GameInfo;


public class Ability {
	
	int AbilityId;
	int dbId; // Id in character_ability table
	
	private String Name;
	private int classId;
	
	private Color bgColor;
	private Color abilityColor;
	private int ManaCost;
	
	private int GraphicsNr;
	
	private int Cooldown;
	private int CooldownLeft;
	
	//private Timer timerCooldown = new Timer();
	
	private boolean Ready;
	private boolean ReadySent;

	private int readyFlashAlpha = 0;
	
	private String AoE = "None";
	private int Damage;
	private int Range;
	private boolean Instant;
	private int Price;
	private boolean TargetSelf;
	private Vector<StatusEffect> StatusEffects;
	
	
	// INFO FOR THE OWNER OF THE ABILITY
	private int AP;
	private int APnext;

	//protected Animation abilityAnimation;

	private String EquipReq = "None";
	
	
	public Ability(int newId) {
		

		AbilityId = newId;

		AP = 0;
		APnext = 50;
		
		CooldownLeft = 0;
		
		StatusEffects = new Vector<StatusEffect>();
		
		
		
		//abilityAnimation = BlueSaga.GFX.getSprite("abilities/ability"+AbilityId).getAnimation();
	}
	
	
	public void load(String abilityInfo){

		String info[] = abilityInfo.split("=");
		
		Name = info[1];
		
		classId = Integer.parseInt(info[2]);
		
		bgColor = new Color(0,0,0);

		if(classId > 0){
			String bgColorInfo[] = GameInfo.classDef.get(classId).bgColor.split(",");
			bgColor = new Color(Integer.parseInt(bgColorInfo[0]),Integer.parseInt(bgColorInfo[1]),Integer.parseInt(bgColorInfo[2]));
		}
		
		abilityColor = new Color(Integer.parseInt(info[3]),Integer.parseInt(info[4]),Integer.parseInt(info[5]));
		
		
		ManaCost = Integer.parseInt(info[6]);
		
		Cooldown = Integer.parseInt(info[7]);
		CooldownLeft = Integer.parseInt(info[8]);
		
		if(CooldownLeft != 0){
			Ready = false;
			setReadySent(false);
			cooldown();
		}else{
			Ready = true;
			setReadySent(true);
		}
				
		Range = Integer.parseInt(info[9]);
		
		Price = Integer.parseInt(info[10]);
		
		TargetSelf = Boolean.parseBoolean(info[11]);
		
		Instant = Boolean.parseBoolean(info[12]);
		
		EquipReq = info[13];
		
		GraphicsNr = Integer.parseInt(info[14]);
		
		AoE = info[15];
	}
	
	
	public void drawIcon(Graphics g, int x, int y){
		g.setColor(bgColor);
		g.fillRect(x,y,50,50);
		
		if(GraphicsNr > 0){
			ImageResource.getSprite("abilities/ability_icon"+GraphicsNr).draw(x, y);
		}
		
		// draw cooldown
		g.setColor(new Color(255,255,255,150));
		int cooldownHeight = getCooldownHeight(50);
		g.fillRect(x, y+ 50 - cooldownHeight, 50, cooldownHeight);
		
		g.setColor(new Color(255,255,255,readyFlashAlpha));
		g.fillRect(x, y, 50, 50);
		
		if(readyFlashAlpha > 0){
			readyFlashAlpha -= 4;
		}
		
		if(getCooldownLeft() > 0){
			g.setFont(Font.size8);
			g.setColor(new Color(0,0,0));
	
			int secondsLeft = (int) Math.ceil(getCooldownLeft()/5.0f);
	
			int textX = x+25 - Math.round(Font.size8.getWidth(secondsLeft+"s")/2.0f);
			
			g.drawString(secondsLeft+"s", textX, y+6);
		}
		
		ImageResource.getSprite("abilities/ability_button_frame").draw(x, y);
	}
	
	/*
	 * 
	 * 	AP and LEVEL UP
	 * 
	 */
	
	public void setAP(int newAP){
		AP = newAP;
	}
	
	public int getAP(){
		return AP;
	}
	
	public void gainAP(int plus){
		AP += plus;
		if(AP > APnext){
			AP = APnext;
		}
	}
	
	
	public int getAPnext(){
		return APnext;
	}
	
	
	public void setAPnext(int newAPnext){
		APnext = newAPnext;
	}
	
	
	/*
	 * 
	 * 	GETTER and SETTER
	 * 
	 * 
	 */
	
	/*
	public Animation getAnimation() {
		return abilityAnimation;
	}
	*/
	
	
	
	public String getName() {
		return Name;
	}
	
	
	public boolean isInstant(){
		return Instant;
	}
	
	public Color getColor() {
		return abilityColor;
	}
	
	public void setBgColor(Color newColor){
		bgColor = newColor;
	}
	
	public Color getColorAlpha(int alpha){
		Color alphaColor = new Color(abilityColor.getRed(),abilityColor.getGreen(),abilityColor.getBlue(),alpha);
		return alphaColor;
	}

	
	public int getManaCost(){
		return ManaCost;
	}
	
	public int getRange(){
		return Range;
	}
	
	public boolean isReady(){
		return Ready;
	}
	
	
	public void setCooldown(int newCooldown){
		Cooldown = newCooldown;
	}
	
	public void setCooldownLeft(int newCooldownLeft){
		CooldownLeft = newCooldownLeft*5;
		if(CooldownLeft > 0){
			cooldown();
		}
	}
	
	public int getCooldownLeft(){
		return CooldownLeft;
	}
	
	public int getCooldownHeight(int max){
		int cooldownHeight = max;
		if(Cooldown > 0){
			cooldownHeight = Math.round(((float) (Cooldown*5 - CooldownLeft)/ (float) (Cooldown*5)) * max);
		}
		return max - cooldownHeight;
	}
	
	public void used(){
		CooldownLeft = Cooldown*5;
		Ready = false;
		cooldown();
	}
	public void cooldown(){
		if(CooldownLeft > 0){
			CooldownLeft--;
		}
	}
	
	public void setReady(boolean readyStatus){
		Ready = readyStatus;
		if(Ready){
			readyFlashAlpha = 255;
		}
	}

	public int getPrice() {
		return Price;
	}


	public int getDamage() {
		return Damage;
	}


	public void setDamage(int damage) {
		Damage = damage;
	}


	public String getAoE() {
		return AoE;
	}


	public void setAoE(String aoE) {
		AoE = aoE;
	}


	public Vector<StatusEffect> getStatusEffects(){
		return StatusEffects;
	}
	
	public boolean isReadySent() {
		return ReadySent;
	}


	public void setReadySent(boolean readySent) {
		ReadySent = readySent;
	}
	
	public int getAbilityId(){
		return AbilityId;
	}
	
	public void setColor(Color newColor){
		abilityColor = newColor;
	}


	public boolean isTargetSelf() {
		return TargetSelf;
	}


	public void setTargetSelf(boolean targetSelf) {
		TargetSelf = targetSelf;
	}

	public String getEquipReq(){
		return EquipReq;
	}
	
	public void setGraphicsNr(int newNr){
		GraphicsNr = newNr;
	}
	
	public int getGraphicsNr(){
		return GraphicsNr;
	}
	
}
