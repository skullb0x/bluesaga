package components;
import game.Database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;


import org.newdawn.slick.Animation;
import org.newdawn.slick.Color;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;



public class Ability {
	
	int Id;
	private String Name;
	private String Type;
	private int UserId;
	
	private boolean Passive;
	private int damage;
	private String StatModifier;
	
	private int Duration;
	
	private int Cooldown;
	private int CooldownLeft;
	private boolean Ready;
	static Timer timerCooldown = new Timer();
	
	private boolean Active;
	
	private int ManaCost;
	
	private String AoE;
	
	private float AP;
	private float APnext;
	private int Level;
	
	private Color abilityColor;
	
	private int nrSlots;
	
	private Image [] graphics;
	
	protected Animation abilityAnimation;
	
	private int ActionBarIndex;
	
	
	public Ability(int newId, Database gameDB) {
		
		ActionBarIndex = 10; // 10 = not in actionbar
		
		UserId = 100;
		
		Id = newId;
		Ready = true;
		
		try {
			ResultSet rs = gameDB.askDB("select * from ability where Id = "+newId);
			
			while (rs.next()) {
				
				Name = rs.getString("Name");
				Type = rs.getString("Type");
				Passive = rs.getBoolean("Passive");
				
				AoE = rs.getString("AoE");
				
				Duration = rs.getInt("Duration");
				Cooldown = 5*rs.getInt("Cooldown");
				CooldownLeft = 0;
				
				//ServerMessage.printMessage("ABILITY: "+Name+", PASSIVE: "+Passive+", MULTITARGET: "+MultiTarget+", TARGET SELF: "+TargetSelf);
				
				Level = rs.getInt("Level");
				AP = 0;
				APnext = rs.getInt("APnext");
				
				ManaCost = rs.getInt("ManaCost");
				
				abilityColor = new Color(255,255,255,150);
				
				String[] colorRGB = rs.getString("Color").split(",");
				
				abilityColor = new Color(Integer.parseInt(colorRGB[0]),Integer.parseInt(colorRGB[1]),Integer.parseInt(colorRGB[2]),150);
				nrSlots = Level;
				
				Active = false;	
				
			}
		
			rs.close();
			
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		graphics = new Image[2];
		
		try {
			graphics[0] = new Image("images/ability/ability_"+Id+"_1.png");
			graphics[1] = new Image("images/ability/ability_"+Id+"_2.png");
			abilityAnimation = new Animation(graphics,500,true);
		
		} catch (SlickException e) {
			e.printStackTrace();
		}
		
	}
	
	
	public boolean addAP(int moreAP) {
		AP += moreAP;
		
		boolean levelUp = false;
		
		while(AP >= APnext){
			levelUp = true;
			Level++;
			AP = AP - APnext;
			APnext = Math.round(APnext * 1.2);
		}
		
		return levelUp;
	}
	
	public void setActive(boolean activeState) {
		Active = activeState;
	}
	
	public boolean isActive() {
		return Active;
	}
	
	public Animation getAnimation() {
		return abilityAnimation;
	}
	
	
	public int getDamage() {
		return damage;
	}
	
	public String getStatModifier() {
		return StatModifier;
	}
	
	public void setUserId(int newUser) {
		UserId = newUser;
	}
	
	public int getAPwidth(int Max){
		float APwidth = (AP / APnext) * Max;
		return Math.round(APwidth);
	}
	
	public String getName() {
		return Name;
	}
	
	public int getNrSlots() {
		return nrSlots;
	}
	
	public int getUserId() {
		return UserId;
	}

	public Color getColor() {
		return abilityColor;
	}
	
	public Color getColorAlpha(int alpha){
		Color alphaColor = new Color(abilityColor.getRed(),abilityColor.getGreen(),abilityColor.getBlue(),alpha);
		return alphaColor;
	}

	public boolean isPassive() {
		return Passive;
	}

	public String getType() {
		return Type;
	}
	
	public int getId() {
		return Id;
	}
	
	
	public int getDuration() {
		return Duration;
	}
	
	public int getManaCost(){
		return ManaCost;
	}
	
	public boolean isReady(){
		return Ready;
	}
	
	public void setAP(int newAP){
		AP = newAP;
	}
	
	public void setCooldownLeft(int newCooldownLeft){
		CooldownLeft = newCooldownLeft;
	}
	
	public int getCooldownLeft(){
		return CooldownLeft;
	}
	
	public int getCooldownHeight(int max){
		return Math.round((((float) Cooldown - (float) CooldownLeft)/ Cooldown) * max);
	}
	
	public void used(){
		CooldownLeft = Cooldown;
		Ready = false;
		cooldown();
	}
	
	public void cooldown(){
		timerCooldown.schedule( new TimerTask(){
	        public void run() {
	        	if(!Ready){
		        	CooldownLeft--;
		        	if(CooldownLeft > 0){
		        		cooldown();
		        	}
	        	}else{
	        		CooldownLeft = 0;
	        		Ready = true;
	        	}
	        }
	    }, 200);
	}
	
	public void setReady(boolean readyStatus){
		Ready = readyStatus;
	}

	public void setActionBarIndex(int newIndex){
		ActionBarIndex = newIndex;
	}
	
	public int getActionBarIndex(){
		return ActionBarIndex;
	}
	

}
