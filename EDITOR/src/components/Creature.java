package components;
/************************************
 * 									*
 *		CLIENT / CREATURE			*
 *									*
 ************************************/
import game.BP_EDITOR;
import graphics.Sprite;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.newdawn.slick.Animation;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.font.effects.ColorEffect;

public class Creature {

  protected int CreatureId;
  protected String Name;
  protected String Family;
  protected int MobLevel;

  protected String SpecialType = "no";

  private boolean Captain = false;

  protected Sprite Shadow;

  private Animation special_glow;

  protected Animation deathAnimation;
  protected Animation deadAnimation;
  protected Animation walkAnimation;
  protected Animation animation;

  protected Animation targetIcon;
  protected Animation animationHitEffect;
  protected Animation animationUseAbility;
  protected boolean useAbilityAnimate;
  private Color abilityColor;

  private Animation CriticalHitAnimation;
  protected boolean IsCriticalHit; // IF HIT BY CRITICAL ATTACK
  protected boolean MissedHit; // IF HIT BY CRITICAL ATTACK

  protected int aniEffectItr;

  // MOVEMENT
  protected int X;
  protected int Y;
  protected int oldX;
  protected int oldY;
  private int moveX;
  private int moveY;
  private int walkItr;

  protected int RespawnTime;

  private int TILE_SIZE = 50;

  static Timer timerHit = new Timer();
  static Timer timerAttack = new Timer();
  static Timer timerMove = new Timer();
  static Timer timerTurn = new Timer();
  static Timer timerEffect = new Timer();
  static Timer timerAbility = new Timer();
  static Timer timerDie = new Timer();

  // BATTLE

  protected boolean ATTACK_MODE;
  protected boolean ATTACKED; // If attacked true, if attacker false

  private String BattleStance;

  protected String AttackType;
  private String WeaponAttackType;
  private String hitType;
  private Image hitImage;

  private int turnCounter = 0; // 0 - turnReady = ready!
  protected int turnReady = 2000; // Speed changes this

  private int AnimationHitItr;
  private int AnimationAttackItr;
  private int AnimationDamageItr;
  private float AnimationDeathY = 0;

  private boolean animationHit = false;
  protected boolean animationDamage = false;

  // ABILTIY VARIABLES
  private boolean resurrected = false;

  private UnicodeFont damageFont;

  private Color hitColor2;
  private Color hitColor1;

  protected boolean Dead = false;

  private boolean TURN_READY = false;

  private static int MOVE_MONSTER_DELAY = 16;

  protected int giveXP;

  private int lastHitDamage;
  private int mostHitDamage;

  private int AttackTargetId;

  private String AttackDir;

  private boolean Aggro;
  private int AggroRange;
  private int
      AggroType; // 0 = not moving and no aggro, 1 = moving and no aggro, 2 = moving and aggro

  // EQUIPMENT AND STATS

  private Image WeaponImage;
  protected Image HeadImage;

  protected Item WeaponItem = null;
  protected Item HeadItem = null;
  protected Item ChestItem = null;
  protected Item LegsItem = null;
  protected Item FeetItem = null;
  protected Item MiscItem = null;

  private int HeadX;
  private int HeadY;
  private int WeaponX;
  private int WeaponY;

  private int SizeWidth;
  private int SizeHeight;
  private boolean Boss;

  protected boolean hidden = false;
  protected boolean hideEquipment;

  protected HashMap<Integer, Ability> Abilities = new HashMap<Integer, Ability>();

  protected HashMap<String, Integer> Stats = new HashMap<String, Integer>();
  private HashMap<String, Integer> BonusStats = new HashMap<String, Integer>();
  private ArrayList<Ability> StatusEffects = new ArrayList<Ability>();

  protected int nrAbilities;
  private HashMap<String, Integer> itemStats = new HashMap<String, Integer>();

  protected String STATUS;

  private String FaceDir;

  private Color animationColor;

  private Color StatColor;

  // LEVEL UP

  protected boolean ShowLevelUp;

  public Creature(int creatureId, int newX, int newY) {

    try {
      damageFont = new UnicodeFont("fonts/nokiaFont.ttf", 18, true, true);
      damageFont.getEffects().add(new ColorEffect(java.awt.Color.white));

    } catch (SlickException e1) {
      e1.printStackTrace();
    }

    mostHitDamage = 0;
    IsCriticalHit = false;
    CreatureId = creatureId;
    FaceDir = "LEFT";
    STATUS = "IDLE";
    hitType = "";

    X = newX;
    Y = newY;

    oldX = X;
    oldY = Y;

    moveX = 0;
    moveY = 0;

    WeaponItem = null;
    HeadItem = null;

    HeadX = 0;
    HeadY = 0;

    WeaponX = -50;
    WeaponY = -50;

    hideEquipment = false;

    StatColor = new Color(255, 255, 255, 0);

    ATTACK_MODE = false;
    ATTACKED = false;
    BattleStance = "Aggressive";

    ShowLevelUp = false;

    SizeWidth = 1;
    SizeHeight = 1;

    Aggro = false;

    try {
      ResultSet rs;

      rs = BP_EDITOR.gameDB.askDB("select * from creature where Id = " + CreatureId);

      while (rs.next()) {

        Name = rs.getString("Name");
        WeaponAttackType = "";
        Boss = rs.getBoolean("Boss");

        MobLevel = rs.getInt("Level");

        RespawnTime = rs.getInt("RespawnTime");

        SizeWidth = rs.getInt("SizeW");
        SizeHeight = rs.getInt("SizeH");

        Aggro = false;

        AttackType = rs.getString("AttackType");
        AggroRange = rs.getInt("AggroRange");
      }

      rs.close();

    } catch (SQLException e1) {
      e1.printStackTrace();
    }

    animationColor = new Color(Color.white);

    animationColor = new Color(Color.white);

    walkAnimation = BP_EDITOR.GFX.getSprite("creatures/m" + CreatureId).getAnimation();
    deathAnimation = BP_EDITOR.GFX.getSprite("effects/fx_spawn").getAnimation();

    animation = walkAnimation;

    useAbilityAnimate = false;

    animationUseAbility = BP_EDITOR.GFX.getSprite("effects/fx_use_ability").getAnimation();

    CriticalHitAnimation = BP_EDITOR.GFX.getSprite("gui/battle/critical_hit").getAnimation();

    Shadow = BP_EDITOR.GFX.getSprite("creatures/shadow1");

    targetIcon = BP_EDITOR.GFX.getSprite("gui/world/targetIcon").getAnimation();

    special_glow = BP_EDITOR.GFX.getSprite("effects/fx_special_glow").getAnimation();
  }

  public void resetBonusStats() {
    // PRIMARY BonusStats
    BonusStats.put("ATTACK", 0);
    BonusStats.put("MAGIC", 0);
    BonusStats.put("ATTACK_DEF", 0);
    BonusStats.put("MAGIC_DEF", 0);
    BonusStats.put("SPEED", 0);

    // SECONDARY BonusStats
    BonusStats.put("CRITICAL_HIT", 0);
    BonusStats.put("EVASION", 0);
    BonusStats.put("ACCURACY", 0);

    // HEALTH AND MANA
    BonusStats.put("MAX_HEALTH", 0);
    BonusStats.put("MAX_MANA", 0);

    BonusStats.put("HEALTH", 0);
    BonusStats.put("MANA", 0);

    // MAGIC BonusStats
    BonusStats.put("FIRE_DEF", 0);
    BonusStats.put("FIRE_ATK", 0);
    BonusStats.put("COLD_DEF", 0);
    BonusStats.put("COLD_ATK", 0);
    BonusStats.put("SHOCK_DEF", 0);
    BonusStats.put("SHOCK_ATK", 0);
    BonusStats.put("POISON_DEF", 0);
    BonusStats.put("POISON_ATK", 0);
  }

  public void useAbilityAnimate(Color useColor) {
    abilityColor = useColor;
    useAbilityAnimate = true;
    animationUseAbility.restart();

    timerAbility.schedule(
        new TimerTask() {
          public void run() {
            useAbilityAnimate = false;
          }
        },
        500);
  }

  public void draw(Graphics g, int x, int y) {
    if (!hidden) {

      /*
      if(!Dead && ATTACKED){
      	targetIcon.draw(x,y+15);
      }


      if(!SpecialType.equals("no") && !Dead){
      	Color specialColor = new Color(255,255,255,255);

      	if(SpecialType.equals("Furious")){
      		specialColor = new Color(255,66,52,120);
      	}else if(SpecialType.equals("Cursed")){
      		specialColor = new Color(188,0,255,120);
      	}else if(SpecialType.equals("Rampaging")){
      		specialColor = new Color(95,255,60,120);
      	}
      	special_glow.draw(x-13 - (SizeWidth-1)*20, y-12 - (SizeHeight-1)*25, SizeWidth*75, SizeHeight*75, specialColor);
      }


      if(Dead){
      	AnimationDeathY += 0.5;
      	if(AnimationDeathY > 120){
      		hidden = true;
      	}
      }

      for(int i = 0; i < StatusEffects.size(); i++){
      	if(StatusEffects.get(i).isActive()){
      		StatusEffects.get(i).getAnimation().draw(x-50,y-SizeHeight*50);
      	}else{
      		StatusEffects.remove(i);
      	}
      }

      if(ShowLevelUp){
      	if(animationUseAbility == null){
      		ServerMessage.printMessage("ANIMATION LEVEL UP IS NULL!");
      	}else{
      		animationUseAbility.draw(x, y-40,new Color(255,255,135,200));
      	}
      }else if(useAbilityAnimate){
      	animationUseAbility.draw(x, y-40, abilityColor);
      }

      if(ShowLevelUp){
      	levelUpLabel.draw(x-20,y-(SizeHeight-1)*50 - 60);
      }
      */
      if (!Dead) {
        Shadow.draw(
            x - (SizeWidth - 1) * 25, y + 5, SizeWidth * 50, 50, new Color(255, 255, 255, 255));
      }

      int dY = 0;

      if (walkItr % 20 >= 11) {
        dY = -5;
      }

      if (FaceDir.equals("LEFT")) {
        if (Dead) {
          animation
              .getImage(0)
              .drawFlash(
                  x,
                  y + dY - 5 - Math.round(AnimationDeathY) - 50 * (SizeHeight - 1),
                  50 * SizeWidth,
                  50 * SizeHeight,
                  new Color(255, 255, 255, 100));
        } else {
          animation.draw(
              x,
              y + dY - 5 - 50 * (SizeHeight - 1),
              50 * SizeWidth,
              50 * SizeHeight,
              animationColor);
        }

        if (WeaponItem != null && !hideEquipment) {
          WeaponImage.draw(x - 25 - WeaponX, y - 50 - WeaponY + dY, 100, 100, animationColor);
        }
        if (HeadItem != null && !hideEquipment) {
          HeadImage.draw(x - 25 - HeadX, y - 50 - HeadY + dY, 50, 50, animationColor);
        }
      } else {
        if (Dead) {
          animation
              .getImage(0)
              .drawFlash(
                  x + 50 * SizeWidth,
                  y + dY - 5 - Math.round(AnimationDeathY) - 50 * (SizeHeight - 1),
                  -50 * SizeWidth,
                  50 * SizeHeight,
                  new Color(255, 255, 255, 100));
        } else {
          animation.draw(
              x + 50 * SizeWidth,
              y + dY - 5 - 50 * (SizeHeight - 1),
              -50 * SizeWidth,
              50 * SizeHeight,
              animationColor);
        }

        if (WeaponItem != null && !hideEquipment) {
          WeaponImage.draw(x + 75 + WeaponX, y - 50 - WeaponY + dY, -100, 100, animationColor);
        }
        if (HeadItem != null && !hideEquipment) {
          HeadImage.draw(x + 75 + HeadX, y - 50 - HeadY + dY, -50, 50, animationColor);
        }
      }
      /*
      if(animationDamage){
      	if(IsCriticalHit){
      		CriticalHitAnimation.draw(x-30,y-20);
      	}
      	try {
      		damageFont.loadGlyphs();
      	} catch (SlickException e) {
      		e.printStackTrace();
      	}
      	g.setFont(damageFont);

      	if(!MissedHit){
      		g.setColor(new Color(255,255,255));
      		g.drawString(Integer.toString(lastHitDamage), x+20, y-AnimationDamageItr);
      	}else {
      		g.setColor(new Color(210,75,75));
      		g.drawString("MISS", x, y-AnimationDamageItr);
      	}

      	if(hitType != "" && lastHitDamage > 0 && AnimationDamageItr < 40){
      		hitImage.draw(x-15,y-10);
      	}

      	AnimationDamageItr++;
      	if(AnimationDamageItr > 100){
      		animationDamage = false;
      		IsCriticalHit = false;
      	}

      }


      if(Aggro){
      	//aggroBubble.draw(x,y-(Size-1)*50 - 20);
      	g.setColor(new Color(255, 85, 85, 255));
      	g.fillRect(x, y, getHealthBarWidth(50), 5);
      	g.setColor(new Color(255, 255, 255, 255));
      	g.drawRect(x, y, 50, 5);
      }
      */
    }
  }

  // DRAW WITH LIGHTING
  public void draw(Graphics g, int x, int y, float[][][] lightValue) {
    if (!hidden) {
      if (!Dead && ATTACKED) {
        targetIcon.draw(x, y + 15);
      }

      if (Dead) {
        AnimationDeathY += 0.5;
        if (AnimationDeathY > 120) {
          hidden = true;
        }
      }

      if (!Dead) {
        //Shadow.getAnimation().updateNoDraw();
        Shadow.draw(x, y + 5);
      }

      if (!SpecialType.equals("no") && !Dead) {
        Color specialColor = new Color(255, 255, 255, 255);

        if (SpecialType.equals("Furious")) {
          specialColor = new Color(255, 66, 52, 120);
        } else if (SpecialType.equals("Cursed")) {
          specialColor = new Color(188, 0, 255, 120);
        } else if (SpecialType.equals("Rampaging")) {
          specialColor = new Color(95, 255, 60, 120);
        }
        special_glow.draw(
            x - 13 - (SizeWidth - 1) * 20,
            y - 12 - (SizeHeight - 1) * 25,
            SizeWidth * 75,
            SizeHeight * 75,
            specialColor);
      }

      int dY = 0;

      if (walkItr % 20 >= 10) {
        dY = -5;
      }

      animation.updateNoDraw();

      Image image;

      if (FaceDir.equals("RIGHT")) {
        image = animation.getCurrentFrame().getFlippedCopy(true, false);
      } else {
        image = animation.getCurrentFrame().copy();
      }

      // if lighting is on apply the lighting values we've
      // calculated for each vertex to the image. We can apply
      // colour components here as well as just a single value.
      image.setColor(
          Image.TOP_LEFT, lightValue[X][Y][0], lightValue[X][Y][1], lightValue[X][Y][2], 1);
      image.setColor(
          Image.TOP_RIGHT,
          lightValue[X + 1][Y][0],
          lightValue[X + 1][Y][1],
          lightValue[X + 1][Y][2],
          1);
      image.setColor(
          Image.BOTTOM_RIGHT,
          lightValue[X + 1][Y - 1][0],
          lightValue[X + 1][Y - 1][1],
          lightValue[X + 1][Y - 1][2],
          1);
      image.setColor(
          Image.BOTTOM_LEFT,
          lightValue[X][Y - 1][0],
          lightValue[X][Y - 1][1],
          lightValue[X][Y - 1][2],
          1);

      if (!Dead) {
        image.draw(x, y + dY - 5, 50 * SizeWidth, 50 * SizeHeight, animationColor);
      } else {
        image.drawFlash(
            x,
            y + dY - 5 - Math.round(AnimationDeathY) - 50 * (SizeWidth - 1),
            50 * SizeHeight,
            50 * SizeWidth,
            new Color(255, 255, 255, 100));
      }

      // draw the image with it's newly declared vertex colours
      // to the display

      if (WeaponItem != null && !hideEquipment) {

        image = WeaponImage.copy();

        // if lighting is on apply the lighting values we've
        // calculated for each vertex to the image. We can apply
        // colour components here as well as just a single value.
        image.setColor(
            Image.TOP_LEFT, lightValue[X][Y][0], lightValue[X][Y][1], lightValue[X][Y][2], 1);
        image.setColor(
            Image.TOP_RIGHT,
            lightValue[X + 1][Y][0],
            lightValue[X + 1][Y][1],
            lightValue[X + 1][Y][2],
            1);
        image.setColor(
            Image.BOTTOM_RIGHT,
            lightValue[X + 1][Y - 1][0],
            lightValue[X + 1][Y - 1][1],
            lightValue[X + 1][Y - 1][2],
            1);
        image.setColor(
            Image.BOTTOM_LEFT,
            lightValue[X][Y - 1][0],
            lightValue[X][Y - 1][1],
            lightValue[X][Y - 1][2],
            1);

        if (FaceDir.equals("RIGHT")) {
          image.draw(x + 75 + WeaponX, y - 50 - WeaponY + dY, -100, 100, animationColor);
        } else {
          image.draw(x - 25 - WeaponX, y - 50 - WeaponY + dY, 100, 100, animationColor);
        }
      }

      if (HeadItem != null && !hideEquipment) {
        image = HeadImage.copy();

        // if lighting is on apply the lighting values we've
        // calculated for each vertex to the image. We can apply
        // colour components here as well as just a single value.
        image.setColor(
            Image.TOP_LEFT, lightValue[X][Y][0], lightValue[X][Y][1], lightValue[X][Y][2], 1);
        image.setColor(
            Image.TOP_RIGHT,
            lightValue[X + 1][Y][0],
            lightValue[X + 1][Y][1],
            lightValue[X + 1][Y][2],
            1);
        image.setColor(
            Image.BOTTOM_RIGHT,
            lightValue[X + 1][Y - 1][0],
            lightValue[X + 1][Y - 1][1],
            lightValue[X + 1][Y - 1][2],
            1);
        image.setColor(
            Image.BOTTOM_LEFT,
            lightValue[X][Y - 1][0],
            lightValue[X][Y - 1][1],
            lightValue[X][Y - 1][2],
            1);

        if (FaceDir.equals("RIGHT")) {
          image.draw(x + 75 + HeadX, y - 50 - HeadY + dY, -50, 50, animationColor);
        } else {
          image.draw(x - 25 - HeadX, y - 50 - HeadY + dY, 50, 50, animationColor);
        }
      }

      if (animationDamage) {
        if (IsCriticalHit) {
          CriticalHitAnimation.draw(x - 30, y - 20);
        }

        try {
          damageFont.loadGlyphs();
        } catch (SlickException e) {
          e.printStackTrace();
        }
        g.setFont(damageFont);

        if (!MissedHit) {
          g.setColor(new Color(255, 255, 255));
          g.drawString(Integer.toString(lastHitDamage), x + 20, y - AnimationDamageItr);
        } else {
          g.setColor(new Color(210, 75, 75));
          g.drawString("MISS", x, y - AnimationDamageItr);
        }

        if (hitType != "" && lastHitDamage > 0) {
          hitImage.draw(x - 15, y - 10);
        }

        AnimationDamageItr++;
        if (AnimationDamageItr > 100) {
          animationDamage = false;
          IsCriticalHit = false;
        }
      }

      if (Aggro) {
        //aggroBubble.draw(x,y-(Size-1)*50 - 20);
        g.setColor(new Color(255, 85, 85, 255));
        g.fillRect(x, y, getHealthBarWidth(50), 5);
        g.setColor(new Color(255, 255, 255, 255));
        g.drawRect(x, y, 50, 5);
      }
    }
  }

  /*
   *
   * 			USE ITEM
   *
   *
   */

  public void useItem(Item usedItem) {
    if (usedItem.getType().equals("HEALTH")) {}
  }

  /****************************************
   *                                      *
   *         ABILITIES / SLOTS            *
   *                                      *
   *                                      *
   ****************************************/
  public void useAbility(Ability ability) {
    TURN_READY = false;
    turnCounter = 0;
    setStat("MANA", getStat("MANA") - ability.getManaCost());
    useAbilityAnimate(ability.getColor());
  }

  public int hasAbility(String abilityName) {
    int abilityId = 9999;
    for (int i = 0; i < nrAbilities; i++) {
      if (Abilities.get(i).getName().equals(abilityName)) {
        abilityId = i;
        // Can only use resurrect once in battle
        if (abilityName == "Resurrect" && resurrected) {
          abilityId = 9999;
        }
      }
    }
    return abilityId;
  }

  public void addStatusEffect(Ability newAbility) {
    newAbility.setActive(true);
    StatusEffects.add(newAbility);
  }

  public void removeStatusEffect(int abilityId) {
    int removeIndex = 0;
    boolean hasStat = false;

    for (int i = 0; i < StatusEffects.size(); i++) {
      if (StatusEffects.get(i).getId() == abilityId) {
        hasStat = true;
        removeIndex = i;
        break;
      }
    }

    if (hasStat) {
      StatusEffects.remove(removeIndex);
    }
  }

  public boolean hasStatusEffect(String abilityName) {
    boolean hasStat = false;
    for (int i = 0; i < StatusEffects.size(); i++) {
      if (StatusEffects.get(i).getName().equals(abilityName)) {
        hasStat = true;
        break;
      }
    }
    return hasStat;
  }

  public void hitByAbility(Ability ability, int damage) {

    if (damage > 0) {
      lastHitDamage = damage;

      if (damage > mostHitDamage) {
        mostHitDamage = damage;
      }

      animationDamage = true;
      AnimationDamageItr = 0;

      Stats.put("HEALTH", getStat("HEALTH") - damage);

      if (getStat("HEALTH") <= 0) {
        Stats.put("HEALTH", 0);
        //	die();
      }
    }

    abilityColor = ability.getColor();

    hitColor1 =
        new Color(abilityColor.getRed(), abilityColor.getGreen(), abilityColor.getBlue(), 100);
    hitColor2 = abilityColor;

    animationHit = true;

    hitColor1 = new Color(255, 255, 255, 100);
    hitColor2 = new Color(255, 255, 255, 255);

    AnimationHitItr = 0;
    hitAnimation();
  }

  public HashMap<Integer, Ability> getAbilities() {
    return Abilities;
  }

  public int getNrAbilities() {
    return Abilities.size();
  }

  public Ability getAbility(int nr) {
    return Abilities.get(nr);
  }

  public Ability getAbilityById(int abilityId) {
    for (int i = 0; i < Abilities.size(); i++) {
      if (Abilities.get(i).getId() == abilityId) {
        return Abilities.get(i);
      }
    }
    return null;
  }

  /****************************************
   *                                      *
   *             MOVEMENT  	            *
   *                                      *
   *                                      *
   ****************************************/
  public void move() {
    walkItr++;

    if (moveX > 0) {
      moveX -= 2;
    } else if (moveX < 0) {
      moveX += 2;
    } else if (moveY > 0) {
      moveY -= 2;
    } else if (moveY < 0) {
      moveY += 2;
    }
    if (moveX == 0 && moveY == 0) {

    } else {
      timerMove.schedule(
          new TimerTask() {
            public void run() {
              move();
            }
          },
          MOVE_MONSTER_DELAY);
    }
  }

  public void walk(String Dir) {
    walkItr = 0;
    if (Dir.equals("LEFT") || Dir.equals("RIGHT")) {
      FaceDir = Dir;
    }

    if (Dir.equals("LEFT")) {
      X--;
      moveX = TILE_SIZE;
    } else if (Dir.equals("RIGHT")) {
      X++;
      moveX = -TILE_SIZE;
    } else if (Dir.equals("UP")) {
      Y++;
      moveY = TILE_SIZE;
    } else if (Dir.equals("DOWN")) {
      Y--;
      moveY = -TILE_SIZE;
    }

    oldX = X;
    oldY = Y;

    move();
    //ServerMessage.printMessage("Monster X,Y: "+X+","+Y);

  }

  public void walkTo(int newX, int newY) {
    int oldX = X;
    int oldY = Y;

    X = newX;
    Y = newY;

    walkItr = 0;

    if (oldX < X) {
      FaceDir = "RIGHT";
      moveX = -TILE_SIZE;
    } else if (oldX > X) {
      FaceDir = "LEFT";
      moveX = TILE_SIZE;
    } else if (oldY < Y) {
      moveY = TILE_SIZE;
    } else if (oldY > Y) {
      moveY = -TILE_SIZE;
    }
    move();
  }

  /****************************************
   *                                      *
   *             EQUIPMENT  	            *
   *                                      *
   *                                      *
   ****************************************/
  public void equipItem(Item newItem) {
    if (newItem.getType().equals("Weapon")) {
      WeaponItem = newItem;
      WeaponAttackType = newItem.getAttackType();
      try {
        WeaponImage = new Image(WeaponItem.getImageSrc());
      } catch (SlickException e) {
        e.printStackTrace();
      }
    } else if (newItem.getType().equals("Head")) {
      HeadItem = newItem;

      try {
        HeadImage = new Image(HeadItem.getImageSrc());
      } catch (SlickException e) {
        e.printStackTrace();
      }
    }
  }

  public void unEquipItem(String equipType) {
    if (equipType.equals("Weapon")) {
      WeaponItem = null;
      WeaponAttackType = "";
    } else if (equipType.equals("Head")) {
      HeadItem = null;
    } else if (equipType.equals("Weapon")) {
      ChestItem = null;
    } else if (equipType.equals("Weapon")) {
      LegsItem = null;
    } else if (equipType.equals("Weapon")) {
      FeetItem = null;
    } else if (equipType.equals("Weapon")) {
      FeetItem = null;
    }
  }

  public Item getEquipment(String equipType) {
    if (equipType.equals("Weapon")) {
      return WeaponItem;
    } else if (equipType.equals("Head")) {
      return HeadItem;
    } else if (equipType.equals("Weapon")) {
      return WeaponItem;
    } else if (equipType.equals("Weapon")) {
      return WeaponItem;
    } else if (equipType.equals("Weapon")) {
      return WeaponItem;
    } else if (equipType.equals("Weapon")) {
      return WeaponItem;
    }
    return null;
  }

  public Image getWeaponImage() {
    return WeaponImage;
  }

  public Image getHeadImage() {
    return HeadImage;
  }

  public boolean replenish(Ability ABILITY, int Amount) {

    boolean useSuccess = false;

    if (ABILITY.getName().equals("Heal") && getStat("HEALTH") < getStat("MAX_HEALTH")) {
      StatusEffects.add(ABILITY);

      int hp = getStat("HEALTH");
      hp += Amount;
      if (hp > getStat("MAX_HEALTH")) {
        hp = getStat("MAX_HEALTH");
      }
      Stats.put("HEALTH", hp);

      useSuccess = true;
    } else if (ABILITY.getName().equals("MANA") && getStat("MANA") < getStat("MAX_MANA")) {
      StatusEffects.add(ABILITY);

      int mana = getStat("MANA");
      mana += Amount;
      if (mana > getStat("MAX_MANA")) {
        mana = getStat("MAX_MANA");
      }
      Stats.put("MANA", mana);

      useSuccess = true;
    }

    return useSuccess;
  }

  /****************************************
   *                                      *
   *             ATTACK!                  *
   *                                      *
   *                                      *
   ****************************************/
  public void initBattleState(int TopSpeed) {
    turnReady = 20 + (TopSpeed / getStat("SPEED")) * 10;
    StatusEffects.clear();
    TURN_READY = false;
    resurrected = false;
  }

  // TIMER
  public void updateTimer() {
    if (turnCounter < turnReady) {
      turnCounter++;
    }
  }

  public void timerReset() {
    TURN_READY = false;
    turnCounter = 0;
  }

  public int getTimerWidth() {
    float turnWidth = ((float) turnCounter / (float) turnReady) * 100;

    return Math.round(turnWidth);
  }

  public boolean isReady() {
    return TURN_READY;
  }

  public void setReady(boolean readyState) {
    TURN_READY = readyState;
    if (TURN_READY) {
      turnCounter = turnReady;
    }
  }

  public void setStartTimer(int startTime) {
    turnCounter = startTime;
  }

  public void toggleStance() {
    if (BattleStance == "Aggressive") {
      BattleStance = "Defensive";
    } else {
      BattleStance = "Aggressive";
    }
  }

  public void setAttackMode(boolean newStatus) {
    ATTACK_MODE = newStatus;
  }

  public boolean getAttackMode() {
    return ATTACK_MODE;
  }

  /****************************************
   *                                      *
   *             ATTACK/ANIMATION			*
   *                                      *
   *                                      *
   ****************************************/
  public int attackTarget(String dir) {
    turnCounter = turnReady;

    TURN_READY = false;

    if (dir.equals("LEFT") || dir.equals("RIGHT")) {
      FaceDir = dir;
    }

    AttackDir = dir;

    moveX = 0;
    moveY = 0;

    if (dir.equals("LEFT")) {
      moveX = -2;
    } else if (dir.equals("RIGHT")) {
      moveX = 2;
    } else if (dir.equals("UP")) {
      moveY = 2;
    } else if (dir.equals("DOWN")) {
      moveY = -2;
    }

    AnimationAttackItr = 0;

    attackAnimation();

    return 0;
  }

  public void attackAnimation() {
    if (AttackDir.equals("LEFT")) {
      moveX -= 2;
    } else if (AttackDir.equals("RIGHT")) {
      moveX += 2;
    } else if (AttackDir.equals("DOWN")) {
      moveY += 2;
    } else if (AttackDir.equals("UP")) {
      moveY -= 2;
    }
    AnimationAttackItr++;

    if (AnimationAttackItr == 20) {
      AnimationAttackItr = 0;
      attackAnimationBack();
    } else {
      timerAttack.schedule(
          new TimerTask() {
            public void run() {
              attackAnimation();
            }
          },
          10);
    }
  }

  public void attackAnimationBack() {
    if (AttackDir.equals("LEFT")) {
      moveX += 2;
    } else if (AttackDir.equals("RIGHT")) {
      moveX -= 2;
    } else if (AttackDir.equals("DOWN")) {
      moveY -= 2;
    } else if (AttackDir.equals("UP")) {
      moveY += 2;
    }
    AnimationAttackItr++;

    if (AnimationAttackItr == 20) {
      AnimationAttackItr = 0;
      turnCounter = 0;
    } else {
      timerAttack.schedule(
          new TimerTask() {
            public void run() {
              attackAnimationBack();
            }
          },
          10);
    }
  }

  public void hitByAttack(int damage, String criticalOrMiss, String attackType) {
    lastHitDamage = damage;

    MissedHit = false;

    if (criticalOrMiss.equals("true")) {
      IsCriticalHit = true;
    } else {
      if (criticalOrMiss.equals("miss")) {
        MissedHit = true;
      }
      IsCriticalHit = false;
    }

    if (damage > mostHitDamage) {
      mostHitDamage = damage;
    }

    animationDamage = true;
    AnimationDamageItr = 0;

    if (!MissedHit) {
      animationHit = true;

      Stats.put("HEALTH", getStat("HEALTH") - damage);

      hitType = attackType;

      try {
        hitImage = new Image("images/effects/attack" + hitType + ".png");
      } catch (SlickException e) {
        e.printStackTrace();
      }

      if (getStat("HEALTH") <= 0) {
        Stats.put("HEALTH", 0);
      }

      // Update Health Bar

      hitColor1 = new Color(255, 255, 255, 100);
      hitColor2 = new Color(255, 255, 255, 255);

      AnimationHitItr = 0;
      hitAnimation();
    }
  }

  public void hitAnimation() {
    // create a new timeline for the given object

    if (AnimationHitItr % 2 == 0) {
      animationColor = hitColor1;
    } else {
      animationColor = hitColor2;
    }

    AnimationHitItr++;

    if (AnimationHitItr < 6) {
      timerHit.schedule(
          new TimerTask() {
            public void run() {
              hitAnimation();
            }
          },
          100);
    } else {
      animationColor = new Color(255, 255, 255, 255);
      animationHit = false;
    }
  }

  // SHOW DAMAGE

  public int getHitDamage() {
    return lastHitDamage;
  }

  public boolean isHit() {
    return animationHit;
  }

  public void die() {
    StatusEffects.clear();
    Aggro = false;
    Dead = true;

    AnimationDeathY = 0;
    hideEquipment = true;

    timerDie.schedule(
        new TimerTask() {
          public void run() {
            dieHide();
          }
        },
        2000);
  }

  public void dieHide() {
    if (Dead) {
      hidden = true;
      AnimationDeathY = 0;
    } else {
      animation = walkAnimation;
      hidden = false;
      hideEquipment = false;
    }
  }

  public boolean isDead() {
    return Dead;
  }

  public void revive() {
    StatusEffects.clear();
    resurrected = true;

    Stats.put("HEALTH", Stats.get("MAX_HEALTH"));
    Stats.put("MANA", Stats.get("MAX_MANA"));
    Dead = false;
    hidden = false;
    hideEquipment = false;
    animation = walkAnimation;
  }

  public void reviveAfterBattle() {
    StatusEffects.clear();

    Stats.put("HEALTH", 1);
    Dead = false;
    hidden = false;
    hideEquipment = false;
    animation = walkAnimation;
  }

  public void changeHealth(int changeH) {
    Stats.put("HEALTH", Stats.get("HEALTH") + changeH);
    if (Stats.get("HEALTH") < 0) {
      Stats.put("HEALTH", 0);
      // die
    }

    if (Stats.get("HEALTH") > Stats.get("MAX_HEALTH")) {
      Stats.put("HEALTH", Stats.get("MAX_HEALTH"));
    }
  }

  public void changeMana(int changeM) {
    Stats.put("MANA", Stats.get("MANA") + changeM);
    if (Stats.get("MANA") < 0) {
      Stats.put("MANA", 0);
    }
    if (Stats.get("MANA") > Stats.get("MAX_MANA")) {
      Stats.put("MANA", Stats.get("MAX_MANA"));
    }
  }

  /****************************************
   *                                      *
   *          APPEAR/DISSAPPEAR        	*
   *                                      *
   *                                      *
   ****************************************/
  public void dissappear() {
    hideEquipment = true;
    animation = deathAnimation;

    timerDie.schedule(
        new TimerTask() {
          public void run() {
            hidden = true;
          }
        },
        1000);
  }

  public void appear() {
    hidden = false;
    hideEquipment = true;
    animation = deathAnimation;

    timerDie.schedule(
        new TimerTask() {
          public void run() {
            animation = walkAnimation;
            hideEquipment = false;
          }
        },
        1000);
  }

  /****************************************
   *                                      *
   *             GETTER/SETTER            *
   *                                      *
   *                                      *
   ****************************************/
  public boolean isBoss() {
    return Boss;
  }

  public int getSizeWidth() {
    return SizeWidth;
  }

  public void setCaptainStatus(boolean newStatus) {
    Captain = newStatus;
  }

  public boolean isCaptain() {
    return Captain;
  }

  public void setFaceDir(String newDir) {
    FaceDir = newDir;
  }

  public int getMostHitDamage() {
    return mostHitDamage;
  }

  public void setAttackTarget(String attackTargetType, int attackTargetId) {
    AttackTargetId = attackTargetId;
  }

  public int getAttackTargetId() {
    return AttackTargetId;
  }

  public void setWeaponAttackType(String newWeaponAttack) {
    WeaponAttackType = newWeaponAttack;
  }

  public String getAttackType() {
    if (WeaponAttackType.equals("")) {
      return AttackType;
    }
    return WeaponAttackType;
  }

  public int getId() {
    return CreatureId;
  }

  public String getName() {
    return Name;
  }

  public String getFamily() {
    return Family;
  }

  public int getX() {
    return X;
  }

  public int getY() {
    return Y;
  }

  public void setMoveX(int newMoveX) {
    moveX = newMoveX;
  }

  public int getMoveX() {
    return moveX;
  }

  public void setMoveY(int newMoveY) {
    moveY = newMoveY;
  }

  public int getMoveY() {
    return moveY;
  }

  public Image getImage() {
    return animation.getImage(0);
  }

  public Animation getAnimation() {
    return animation;
  }

  public String getStatus() {
    return STATUS;
  }

  public void setStatus(String newStatus) {
    STATUS = newStatus;
  }

  public int getGiveXp() {
    return giveXP;
  }

  public String getHealthAsString() {
    return getStat("HEALTH") + " / " + getStat("MAX_HEALTH");
  }

  public int getHealthBarWidth(int Max) {
    float fHealth = getStat("HEALTH");
    float fMaxHealth = getStat("MAX_HEALTH");
    float healthBarWidth = (fHealth / fMaxHealth) * Max;
    return Math.round(healthBarWidth);
  }

  public String getManaAsString() {
    return getStat("MANA") + " / " + getStat("MAX_MANA");
  }

  public int getManaBarWidth(int Max) {
    float fMana = getStat("MANA");
    float fMaxMana = getStat("MAX_MANA");
    float manaBarWidth = (fMana / fMaxMana) * Max;
    return Math.round(manaBarWidth);
  }

  public void setStat(String StatType, int value) {
    Stats.put(StatType, value);
  }

  public int getStat(String StatType) {
    return Stats.get(StatType).intValue();
  }

  public String getBattleStance() {
    return BattleStance;
  }

  public void setBattleState(String newState) {
    BattleStance = newState;
  }

  public boolean isAggro() {
    return Aggro;
  }

  public int getAggroType() {
    return AggroType;
  }

  public void setAggro(boolean aggroState) {
    Aggro = aggroState;
  }

  public boolean getAttacked() {
    return ATTACKED;
  }

  public void setAttacked(boolean attackedStatus) {
    ATTACKED = attackedStatus;
  }

  public String getFaceDir() {
    return FaceDir;
  }

  public String getSpecialType() {
    return SpecialType;
  }
}
