package components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class Monster extends Creature {

  private Timer respawnTimer;
  private boolean showHealth;

  private int OtherPlayerId;
  private String OtherPlayerName;
  private int OtherPlayerBounty;
  private int OtherPlayerLightIndex;
  private boolean logout;

  private ArrayList<Item> Loot;

  private HashMap<Integer, Integer> attackersDamage;

  private Timer LevelUpTimer;

  public Monster(int creatureId, int newX, int newY, String specialType) {
    super(creatureId, newX, newY);

    SpecialType = specialType;

    attackersDamage = new HashMap<Integer, Integer>();

    //ServerMessage.printMessage("ATTACK: "+getStat("ATTACK"));
    //ServerMessage.printMessage("SPEED: "+getStat("SPEED"));
    //ServerMessage.printMessage("MAGIC: "+getStat("MAGIC"));

    respawnTimer = new Timer();

    //ServerMessage.printMessage("monster stats - str:"+Stats.get("STRENGTH")+" dex:"+Stats.get("DEXTERITY")+" mag:"+Stats.get("MAGIC"));
  }

  public void generateStats() {

    // HEALTH AND MANA

    if (SpecialType.equals("Furious")) {
      Stats.put("ATTACK", Stats.get("ATTACK") + MobLevel);
      Stats.put("MAX_HEALTH", Stats.get("MAX_HEALTH") + MobLevel * 5);
    } else if (SpecialType.equals("Cursed")) {
      Stats.put("MAGIC", Stats.get("MAGIC") + MobLevel);
      Stats.put("MAX_MANA", Stats.get("MAX_MANA") + MobLevel * 5);
      Stats.put("MAX_HEALTH", Stats.get("MAX_HEALTH") + MobLevel * 5);
    } else if (SpecialType.equals("Lightning")) {
      Stats.put("MAGIC", Stats.get("SPEED") + 2);
      Stats.put("MAX_HEALTH", Stats.get("MAX_HEALTH") + MobLevel * 5);
    } else {

    }

    Stats.put("HEALTH", Stats.get("MAX_HEALTH"));
    Stats.put("MANA", Stats.get("MAX_MANA"));
  }

  public Ability useRandomAbility() {
    /*
    ArrayList<AbilityOrb> ActiveAbilities = new ArrayList<AbilityOrb>();

    for(int i = 0; i < nrAbilities; i++){
    	if(!Abilities.get(i).isPassive()){
    		ActiveAbilities.add(Abilities.get(i));
    	}
    }

    if(ActiveAbilities.size() > 0){
    	int random = randomGenerator.nextInt() % ActiveAbilities.size();
    	return ActiveAbilities.get(random);
    }
    */
    return null;
  }

  public ArrayList<Item> getLoot() {
    return Loot;
  }

  public int getMobLevel() {
    return MobLevel;
  }

  public void respawn(int SpawnX, int SpawnY) {
    X = SpawnX;
    Y = SpawnY;
    hidden = false;
    ATTACK_MODE = false;
    ATTACKED = false;
    Dead = false;

    hidden = false;
    hideEquipment = false;

    useAbilityAnimate = false;
    animationDamage = false;
    IsCriticalHit = false;

    STATUS = "IDLE";
    animation = deathAnimation;
    respawnTimer.schedule(
        new TimerTask() {
          public void run() {
            animation = walkAnimation;
          }
        },
        1000);
  }

  public void login() {
    hideEquipment = true;
    animation = deathAnimation;
    logout = false;
    Dead = false;

    respawnTimer.schedule(
        new TimerTask() {
          public void run() {
            hideEquipment = false;
            animation = walkAnimation;
          }
        },
        1000);
  }

  public void logout() {
    hideEquipment = true;
    animation = deathAnimation;

    respawnTimer.schedule(
        new TimerTask() {
          public void run() {
            logout = true;
            Dead = true;
          }
        },
        1000);
  }

  public boolean getLogout() {
    return logout;
  }

  public void setOtherPlayerId(int newId) {
    OtherPlayerId = newId;
  }

  public int getOtherPlayerId() {
    return OtherPlayerId;
  }

  public void setOtherPlayerName(String newName) {
    OtherPlayerName = newName;
  }

  public String getOtherPlayerName() {
    return OtherPlayerName;
  }

  public void setOtherPlayerBounty(int bounty) {
    OtherPlayerBounty = bounty;
  }

  public int getOtherPlayerBounty() {
    return OtherPlayerBounty;
  }

  public void setOtherPlayerLightIndex(int newIndex) {
    OtherPlayerLightIndex = newIndex;
  }

  public int getOtherPlayerLightIndex() {
    return OtherPlayerLightIndex;
  }

  public void setAreaId(int newAreaId) {}

  public void setShowHealth(boolean newShowHealthStatus) {
    showHealth = newShowHealthStatus;
  }

  public boolean getShowHealth() {
    return showHealth;
  }

  public void showLevelUp() {

    LevelUpTimer = new Timer();

    animationUseAbility.restart();
    animationUseAbility.start();

    ShowLevelUp = true;

    LevelUpTimer.schedule(
        new TimerTask() {
          public void run() {
            ShowLevelUp = false;
            animationUseAbility.stop();
          }
        },
        2000);
  }
}
