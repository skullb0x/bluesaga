package creature;

import graphics.BlueSagaColors;
import graphics.Font;
import graphics.ImageResource;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import abilitysystem.Ability;
import components.Item;

public class Npc extends Creature {

  private Timer respawnTimer;

  private boolean showHealth;

  private int OtherPlayerId;
  private String OtherPlayerName;
  private int OtherPlayerBounty;
  private int OtherPlayerLightIndex;
  private boolean OtherPlayerKiller;

  private int
      AggroType; // 0 = not moving and no aggro, 1 = moving and no aggro, 2 = moving and aggro, 3 = npc quest/shop
  private boolean Aggro;
  private int aggroBubbleItr = 0; // used to show aggro bubble for a time

  protected int SpecialType = 0;
  private Color SpecialColor;

  private boolean logout;

  private ArrayList<Item> Loot;

  private boolean Boss;
  private boolean epic = false;

  public Npc(int creatureId, int newX, int newY, int newZ) {
    super(newX, newY, newZ);

    setType(creatureId);

    showHealth = false;
    OtherPlayerId = 0; // 0 = no player
    OtherPlayerBounty = 0;
    OtherPlayerLightIndex = 0;
    OtherPlayerKiller = false;
    logout = false;

    respawnTimer = new Timer();
  }

  public void draw(Graphics g, int centerX, int centerY) {
    sizeWidthF = SizeWidth;
    sizeHeightF = SizeHeight;

    float cXf = centerX - (sizeWidthF) * 25.0f;
    float cYf = centerY - (sizeHeightF) * 25.0f;

    int cornerX = Math.round(cXf);
    int cornerY = Math.round(cYf);

    Color specialC = null;

    // Gloomy outline
    if (getSpecialType() > 0 && !isEpic() && !Dead) {
      specialC =
          new Color(
              getSpecialColor().getRed(),
              getSpecialColor().getGreen(),
              getSpecialColor().getBlue(),
              150);
      ImageResource.getSprite("effects/fx_special_glow")
          .draw(
              cornerX - 12 - ((SizeWidth - 1) * 20),
              cornerY - 12 - ((SizeHeight - 1) * 35),
              Math.round(sizeWidthF * 75),
              Math.round(sizeHeightF * 75),
              specialC);
    }

    super.draw(g, centerX, centerY, specialC);

    // Draw name & crewname
    if (!Dead && !hasStatusEffect(13) && isAggro()) {

      if (aggroBubbleItr > 0) {
        aggroBubbleItr--;
        ImageResource.getSprite("gui/battle/turn_aggro").drawCentered(centerX, cornerY - 26);
      } else {
        g.setFont(Font.size10);

        int nameWidth = Font.size10.getWidth(Name);
        int nameX = centerX - nameWidth / 2;

        g.setColor(new Color(0, 0, 0, 150));

        g.drawString(Name, nameX, cornerY - 25);

        if (getHealthStatus() == 4) {
          g.setColor(BlueSagaColors.WHITE);
        } else if (getHealthStatus() == 3) {
          g.setColor(new Color(255, 249, 75));
        } else if (getHealthStatus() == 2) {
          g.setColor(BlueSagaColors.ORANGE);
        } else {
          g.setColor(BlueSagaColors.RED);
        }
        g.drawString(Name, nameX, cornerY - 26);
      }
    }

    if (!Dead) {
      if (getAggroType() == 3 && !isAggro()) {
        talkBubble.drawCentered(centerX - 20, cornerY - 26);
      }
    }
  }

  public void generateStats(String newSpecialType) {

    // HEALTH AND MANA
    /*
    if(SpecialType.equals("Furious")){
    	Stats.setValue("STRENGTH",getTotalStat("STRENGTH")+MobLevel);
    	Stats.setValue("MAX_HEALTH",getTotalStat("MAX_HEALTH")+MobLevel*5);
    }else if(SpecialType.equals("Cursed")){
    	Stats.setValue("INTELLIGENCE",getTotalStat("INTELLIGENCE")+MobLevel);
    	Stats.setValue("MAX_MANA",getTotalStat("MAX_MANA")+MobLevel*5);
    	Stats.setValue("MAX_HEALTH",getTotalStat("MAX_HEALTH")+MobLevel*5);
    }else if(SpecialType.equals("Lightning")){
    	Stats.setValue("SPEED",1);
    	Stats.setValue("MAX_HEALTH",getTotalStat("MAX_HEALTH")+MobLevel*5);
    }else{

    }
     */
    Health = getTotalStat("MAX_HEALTH");
    Mana = getTotalStat("MAX_MANA");
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

  public void respawn(int SpawnX, int SpawnY) {
    X = SpawnX;
    Y = SpawnY;
    hidden = false;
    ATTACK_MODE = false;
    ATTACKED = false;
    Dead = false;

    hidden = false;
    MyEquipHandler.setHideEquipment(false);

    useAbilityAnimate = false;
    animationDamage = false;
    IsCriticalHit = false;

    Health = Stats.getValue("MAX_HEALTH");
    Mana = Stats.getValue("MAX_MANA");

    STATUS = "IDLE";
    animation = deathAnimation;
    respawnTimer.schedule(
        new TimerTask() {
          @Override
          public void run() {
            animation = frontAnimation;
          }
        },
        1000);
  }

  public void login() {
    MyEquipHandler.setHideEquipment(true);
    animation = deathAnimation;
    logout = false;
    Dead = false;

    respawnTimer.schedule(
        new TimerTask() {
          @Override
          public void run() {
            MyEquipHandler.setHideEquipment(false);
            animation = frontAnimation;
          }
        },
        1000);
  }

  public void logout() {
    MyEquipHandler.setHideEquipment(true);
    animation = deathAnimation;

    respawnTimer.schedule(
        new TimerTask() {
          @Override
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

  public void setOtherPlayerKiller(boolean killer) {
    OtherPlayerKiller = killer;
  }

  public boolean getOtherPlayerKiller() {
    return OtherPlayerKiller;
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

  public void setShowHealth(boolean newShowHealthStatus) {
    showHealth = newShowHealthStatus;
  }

  public boolean getShowHealth() {
    return showHealth;
  }

  public int getAggroType() {
    return AggroType;
  }

  public void setAggroType(int newType) {
    AggroType = newType;
  }

  public boolean isAggro() {
    return Aggro;
  }

  public void setAggro(boolean aggroState) {
    Aggro = aggroState;

    // If Aggro then show aggro bubble
    if (Aggro) {
      aggroBubbleItr = 60;
    }
  }

  public void setSpecialType(int newType) {
    SpecialType = newType;
    if (SpecialType == 1) {
      // SCORCHING
      SpecialColor = new Color(242, 35, 35);
    } else if (SpecialType == 2) {
      // FROSTED
      SpecialColor = new Color(0, 150, 255);
    } else if (SpecialType == 3) {
      // ELECTRO
      SpecialColor = new Color(233, 255, 43);
    } else if (SpecialType == 4) {
      // MIGHTY
      SpecialColor = new Color(255, 138, 0);
    } else if (SpecialType == 5) {
      // TOXIC
      SpecialColor = new Color(247, 36, 250);
    } else if (SpecialType == 6) {
      // FLASH
      SpecialColor = new Color(186, 255, 96);
    }
  }

  public void die() {
    super.die();
    Aggro = false;
  }

  public int getSpecialType() {
    return SpecialType;
  }

  public Color getSpecialColor() {
    return SpecialColor;
  }

  public boolean isBoss() {
    return Boss;
  }

  public boolean isEpic() {
    return epic;
  }

  public void setEpic(boolean epic) {
    this.epic = epic;
  }
}
