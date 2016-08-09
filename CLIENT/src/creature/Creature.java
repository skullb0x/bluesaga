package creature;
/************************************
 * 									*
 *		CLIENT / CREATURE			*
 *									*
 ************************************/
import game.BlueSaga;
import game.ClientSettings;
import graphics.BlueSagaColors;
import graphics.Font;
import graphics.ImageResource;
import graphics.Sprite;
import graphics.screeneffects.StatusScreenEffect;
import gui.DamageLabel;
import screens.ScreenHandler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.newdawn.slick.Animation;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;

import abilitysystem.Ability;
import abilitysystem.Skill;
import abilitysystem.StatusEffect;
import animationsystem.CreatureAnimation;
import animationsystem.animations.AttackAnimation;
import animationsystem.animations.BecomeBigAnimation;
import animationsystem.animations.ChargeBackAttackAnimation;
import animationsystem.animations.DashAnimation;
import animationsystem.animations.HorizontalPumpingAnimation;
import animationsystem.animations.HorizontalShake;
import animationsystem.animations.VerticalSqueezeAnimation;
import animationsystem.animations.JumpSpinAnimation;
import animationsystem.animations.RollAttackAnimation;
import animationsystem.animations.RoundAttackAnimation;
import animationsystem.animations.ThrowAnimation;
import animationsystem.animations.VerticalPumpingAnimation;
import sound.Sfx;
import components.Stats;
import creature.Creature.CreatureType;
import data_handlers.MonsterHandler;

public class Creature {

  protected int dbId = 0; // Id in area_creature table or in user_character
  private int CreatureId;
  protected String Name = "";
  protected String Family;

  public enum CreatureType {
    None,
    Monster,
    Player
  }

  private CreatureType creatureType = CreatureType.Monster;

  // POSITION
  protected int X;
  protected int Y;
  protected int Z;

  protected float rotation = 180.0f;
  private float gotoRotation = 180.0f;

  public boolean lookRight = false;
  public int equipDir = 1;

  private int screenPixelX = 0;
  private int screenPixelY = 0;

  private int HealthStatus = 4; // 4 = healthy, 3 = wounded, 2 = heavily wounded, 1 = nearly dead

  // RESTING
  private boolean Resting = false;
  protected int RestingFadeAlpha = 0;

  // ANIMATION SYSTEM

  // ACTION HANDLERS
  public CwalkHandler MyWalkHandler;
  public CbattleHandler MyBattleHandler;
  public CequipHandler MyEquipHandler;
  public CemoticonHandler MyEmoticonHandler;

  // GFX
  protected Sprite Shadow;
  protected Animation deathAnimation;
  protected Animation deadAnimation;
  protected Animation frontAnimation;
  protected Animation backAnimation;
  protected Animation animation;

  protected Animation targetIcon;

  protected Animation animationHitEffect;

  protected Animation animationUseAbility;

  protected Image toDraw;

  // Ability animations
  protected boolean useAbilityAnimate;
  private Color abilityColor;
  private int useAbilityGraphicsNr;

  protected Vector<CreatureAnimation> creatureAnimations = new Vector<CreatureAnimation>();

  // NPC
  protected Sprite talkBubble;

  protected int aniEffectItr;

  protected int RespawnTime;

  static Timer timerEffect = new Timer();
  static Timer timerDie = new Timer();
  private Timer LevelUpTimer;

  // BATTLE
  static Timer timerTurn = new Timer();

  private CreatureType AttackTargetType = CreatureType.None;
  private int AttackTargetId = 0;

  private int AttackRange;

  private Animation CriticalHitAnimation;
  protected boolean IsCriticalHit;
  protected boolean MissedHit;
  protected boolean EvadedHit;

  protected boolean ATTACK_MODE;
  protected boolean ATTACKED; // If showing target icon

  protected String AttackType;
  private String hitType;
  private Sprite hitImage;

  private int mostHitDamage;

  private int AnimationHitItr;
  private int AnimationDamageItr;

  private float animationDeathY = 0;

  private boolean animationHit = false;
  protected boolean animationDamage = false;

  private ConcurrentHashMap<Integer, DamageLabel> damageLabels;
  private int damageLabelId = 0;

  protected Sprite directionRing;

  // ABILTIY VARIABLES
  protected Vector<Ability> Abilities = new Vector<Ability>();
  private HashMap<String, Skill> Skills = new HashMap<String, Skill>();

  private Color hitColor2;
  private Color hitColor1;

  static Timer timerAbility = new Timer();

  protected boolean Dead = false;

  // LEVEL UP
  protected boolean ShowLevelUp;
  protected boolean ShowLevelDown;
  protected int giveXP;

  private Customization Customization;

  protected boolean hidden = false;
  protected boolean removed = false;

  // STATS

  protected int Health;
  protected int Mana;

  protected Stats Stats = new Stats();
  protected Stats BonusStats = new Stats();

  private Vector<StatusEffect> StatusEffects = new Vector<StatusEffect>();

  protected String STATUS;

  protected int SizeWidth;
  protected int SizeHeight;
  protected float sizeWidthF;
  protected float sizeHeightF;

  private Color animationColor;

  public Creature(int newX, int newY, int newZ) {
    X = newX;
    Y = newY;
    Z = newZ;

    MyWalkHandler = new CwalkHandler(this);
    MyEquipHandler = new CequipHandler(this);
    MyEmoticonHandler = new CemoticonHandler(this);
    Skills.clear();

    damageLabels = new ConcurrentHashMap<Integer, DamageLabel>();

    directionRing = new Sprite("images/creatures/direction_ring_big");
  }

  public void setType(int creatureId) {

    setCustomization(new Customization(creatureId));

    setCreatureId(creatureId);

    mostHitDamage = 0;
    IsCriticalHit = false;
    STATUS = "IDLE";
    hitType = "";

    SizeWidth = 1;
    SizeHeight = 1;

    MyEquipHandler.clearEquipment();

    ATTACK_MODE = false;
    ATTACKED = false;

    ShowLevelUp = false;
    ShowLevelDown = false;

    animationColor = new Color(255, 255, 255, 0);
    frontAnimation = ImageResource.getSprite("creatures/m" + CreatureId).getAnimation();

    if (ImageResource.getSprite("creatures/m" + CreatureId + "b") != null) {
      backAnimation = ImageResource.getSprite("creatures/m" + CreatureId + "b").getAnimation();
    } else {
      backAnimation = frontAnimation;
    }

    deathAnimation = ImageResource.getSprite("effects/fx_spawn").getAnimation();

    Shadow = ImageResource.getSprite("creatures/shadow1");

    frontAnimation.restart();
    Shadow.getAnimation().restart();

    animation = frontAnimation;

    useAbilityAnimate = false;

    animationUseAbility = ImageResource.getSprite("effects/fx_use_ability").getAnimation();

    CriticalHitAnimation = ImageResource.getSprite("gui/battle/critical_hit").getAnimation();

    targetIcon = ImageResource.getSprite("gui/world/targetIcon").getAnimation();

    talkBubble = ImageResource.getSprite("gui/emoticons/talk");
  }

  public int compareTo(Creature OtherC) {
    return getY() - OtherC.getY();
  }

  public Stats getBonusStats() {
    return BonusStats;
  }

  public void useAbilityAnimate(int graphicsNr, Color useColor) {
    abilityColor = useColor;
    useAbilityAnimate = true;
    useAbilityGraphicsNr = graphicsNr;
    animationUseAbility.restart();

    timerAbility.schedule(
        new TimerTask() {
          @Override
          public void run() {
            useAbilityAnimate = false;
          }
        },
        1000);
  }

  public void useAbility(Ability ability) {
    useAbilityAnimate(ability.getGraphicsNr(), ability.getColor());
  }

  public void updateRotation() {
    float rotationSpeed = 18.0f;

    float diff = rotation - gotoRotation;

    if (creatureType == CreatureType.Player) {
      rotation = gotoRotation;
    } else if (Math.abs(diff) > rotationSpeed) {
      if (diff > 0) {
        rotationSpeed *= -1.0f;
      }
      if (Math.abs(diff) > 180.0f) {
        rotationSpeed *= -1.0f;
      }

      rotation += rotationSpeed;

      if (rotation < 0.0f) {
        rotation = 360.0f + rotation;
      }

      if (rotation >= 360.0f) {
        rotation = rotation - 360.0f;
      }
    } else {
      rotation = gotoRotation;
    }

    if (diff != 0) {
      if (rotation > 270.0f || rotation < 90.0f) {
        animation = backAnimation;
      } else {
        animation = frontAnimation;
      }
    }

    directionRing.getImage().setRotation(rotation);
  }

  public void draw(Graphics g, int centerX, int centerY, Color tintColor) {
    // size correction for placement

    // Animation variables
    float scaleX = 1.0f;
    float scaleY = 1.0f;
    float aniRotation = 0.0f;
    float aniSpin = 0.0f;

    boolean showBack = false;

    // WALK JUMP UP AND DOWN ANIMATION
    int walkY = 0;

    // Up and down idle animation for equipment
    int idleY = 0;

    int framenr = animation.getFrame() % 2;
    if (framenr == 1) {
      idleY = 2;
    }

    if (!hidden) {
      if (MyWalkHandler.getWalkItr() % 25 >= 11) {
        walkY = -5;
      }
    }

    centerY += walkY;

    sizeWidthF = SizeWidth;
    sizeHeightF = SizeHeight;

    // Add ability animation coordinates

    Iterator<CreatureAnimation> it = creatureAnimations.iterator();

    while (it.hasNext()) {
      CreatureAnimation cAni = it.next();

      if (cAni.isActive()) {
        cAni.update();

        centerX += cAni.getAnimationX();
        centerY += cAni.getAnimationY();

        aniRotation = cAni.getRotation();

        if (aniRotation != 0) {
          aniRotation = (aniRotation + rotation) % 360.0f;

          if (aniRotation < 0.0f) {
            aniRotation += 360.0f;
          }

          if (aniRotation > 270.0f || aniRotation < 90.0f) {
            animation = backAnimation;
          } else {
            animation = frontAnimation;
          }

          directionRing.getImage().setRotation(aniRotation);
        }
        aniSpin = cAni.getSpin();

        scaleX = cAni.getScaleX();
        scaleY = cAni.getScaleY();

        sizeWidthF *= scaleX;
        sizeHeightF *= scaleY;
      } else {
        it.remove();
      }
    }

    float cXf = centerX - (sizeWidthF) * 25.0f;
    float cYf = centerY - (sizeHeightF) * 25.0f;

    int cornerX = Math.round(cXf);
    int cornerY = Math.round(cYf);

    if (rotation > 0.0f && rotation < 180.0f) {
      equipDir = -1;
      lookRight = true;
    } else if (rotation < 360.0f && rotation > 180.0f) {
      equipDir = 1;
      lookRight = false;
    }

    if (rotation > 270.0f || rotation < 90.0f) {
      showBack = true;
    }

    if (Dead) {
      int deadnr = getDBId() % 3 + 1;
      ImageResource.getSprite("creatures/m_dead" + deadnr).draw(cornerX, cornerY + 5);
    }

    // CLOAK ABILITIES
    boolean cloaked = false;

    if (hasStatusEffect(13) || hasStatusEffect(40) || hasStatusEffect(42)) {
      cloaked = true;
    }

    if (!hidden) {
      animation.updateNoDraw();

      hitAnimation();

      if (Dead) {
        animationDeathY += 0.5;
        if (animationDeathY > 120) {
          hidden = true;
        }
      }

      if (ShowLevelUp || ShowLevelDown) {
        if (animationUseAbility != null) {
          animationUseAbility.draw(
              cornerX,
              cornerY - sizeHeightF * 25.0f - (sizeWidthF - 1) * 50.0f - 5,
              new Color(255, 255, 135, 200));
        }
      } else if (useAbilityAnimate) {
        animationUseAbility.updateNoDraw();
        Image useAniImage =
            animationUseAbility
                .getCurrentFrame()
                .getScaledCopy((int) (50 * sizeWidthF), (int) (100 * sizeHeightF));

        useAniImage.draw(
            cornerX, cornerY - sizeHeightF * 25.0f - (sizeWidthF - 1) * 50.0f - 5, abilityColor);
      }

      if (ShowLevelUp) {
        ImageResource.getSprite("gui/world/level_up").drawCentered(cornerX - 10, cornerY - 45);
      } else if (ShowLevelDown) {
        ImageResource.getSprite("gui/world/level_down").drawCentered(cornerX - 10, cornerY - 45);
      }

      if (!Dead && !hasStatusEffect(13) && !hasStatusEffect(40)) {

        int shadowY = centerY - 15 - walkY;
        Shadow.getAnimation()
            .getImage(framenr)
            .draw(cornerX, shadowY, sizeWidthF * 50, 50, new Color(255, 255, 255, 255));
      }

      if (!Dead && ATTACKED) {
        targetIcon.getCurrentFrame().drawCentered(centerX, centerY);
      }

      if (Dead) {
        animation
            .getImage(0)
            .getFlippedCopy(lookRight, false)
            .drawFlash(
                cornerX,
                cornerY - Math.round(animationDeathY),
                SizeWidth * 50,
                SizeHeight * 50,
                new Color(255, 255, 255, 200 - Math.round(animationDeathY) * 3));
      } else {
        Color cColor = ScreenHandler.AREA_EFFECT.getTintColor();

        cColor = new Color(cColor.getRed(), cColor.getGreen(), cColor.getBlue(), cColor.getAlpha());

        if (tintColor != null) {
          cColor = tintColor;
        }

        // Half transparent if using fade ability
        if (hasStatusEffect(12)) {
          cColor = new Color(255, 255, 255, 50);
        }

        int transformFixY =
            Math.round(-(sizeHeightF - 1) * 25 + sizeHeightF * 25 - (25 * sizeHeightF));

        // Draw equipment behind player
        if (!cloaked) {
          if (!showBack) {
            MyEquipHandler.drawBack(
                cornerX, cornerY + idleY + transformFixY, lookRight, aniSpin, scaleX, scaleY);
          } else {
            getCustomization()
                .draw(cornerX, cornerY + idleY + transformFixY, lookRight, aniSpin, scaleX, scaleY);
            MyEquipHandler.drawFront(
                cornerX, cornerY + idleY + transformFixY, lookRight, aniSpin, scaleX, scaleY);
          }
        }

        // Draw creature
        if (!cloaked) {

          Image creatureSprite = animation.getCurrentFrame().getFlippedCopy(lookRight, false);

          Image transformedSprite =
              creatureSprite.getScaledCopy((int) (50 * sizeWidthF), (int) (50 * sizeHeightF));

          transformedSprite.rotate(aniSpin);

          transformedSprite.draw(Math.round(cornerX), Math.round(cornerY) + transformFixY);

          // NIGHT SPRITE
          transformedSprite.draw(Math.round(cornerX), Math.round(cornerY) + transformFixY, cColor);

          // ABILITY SPRITE
          transformedSprite.draw(
              Math.round(cornerX), Math.round(cornerY) + transformFixY, animationColor);
        } else if (hasStatusEffect(42)) {
          ImageResource.getSprite("effects/flashstep")
              .draw(Math.round(cornerX), Math.round(cornerY) + 10 + transformFixY);
        } else if (hasStatusEffect(40)) {
          ImageResource.getSprite("objects/gathering/oranga_open")
              .draw(Math.round(cornerX), Math.round(cornerY) + 10 + transformFixY);
        }

        setScreenPixelX(cornerX);
        setScreenPixelY(cornerY);

        // Draw equipment in front of player
        if (!cloaked) {
          if (showBack) {
            MyEquipHandler.drawHeadItem(
                cornerX, cornerY + idleY + transformFixY, lookRight, aniSpin, scaleX, scaleY);
            MyEquipHandler.drawBack(
                cornerX, cornerY + idleY + transformFixY, lookRight, aniSpin, scaleX, scaleY);
          } else {
            getCustomization()
                .draw(cornerX, cornerY + idleY + transformFixY, lookRight, aniSpin, scaleX, scaleY);
            MyEquipHandler.drawFront(
                cornerX, cornerY + idleY + transformFixY, lookRight, aniSpin, scaleX, scaleY);
          }
        }
      }

      MyEmoticonHandler.draw(centerX, cornerY - 40);

      // Draw blink animation when hit
      if (animationDamage) {
        if (IsCriticalHit) {
          CriticalHitAnimation.getCurrentFrame().drawCentered(centerX, cornerY - 30);
        }
        g.setFont(Font.size18);

        if (animationHit && hitImage != null && AnimationHitItr < 15) {
          hitImage.getAnimation().updateNoDraw();
          hitImage.getAnimation().getCurrentFrame().drawCentered(centerX, centerY);
        }

        AnimationDamageItr++;
        if (AnimationDamageItr > 100) {
          animationDamage = false;
          IsCriticalHit = false;
        }
      }

      // Draw ability icon when using ability
      if (useAbilityAnimate) {
        if (useAbilityGraphicsNr > 0) {
          ImageResource.getSprite("abilities/ability_icon" + useAbilityGraphicsNr)
              .drawCentered(centerX, cornerY);
        }
      }
      for (int i = 0; i < StatusEffects.size(); i++) {
        StatusEffect SE = StatusEffects.get(i);
        if (SE.isActive()) {
          SE.draw(centerX, centerY);
        } else {
          StatusEffects.remove(i);
        }
      }

      // RESTING
      if (isResting()) {
        ImageResource.getSprite("gui/emoticons/rest").drawCentered(centerX - 25, cornerY - 25);
      }

      // SHOW DAMAGE TAKEN
      Iterator<DamageLabel> iter = damageLabels.values().iterator();

      while (iter.hasNext()) {
        DamageLabel label = iter.next();
        label.draw(g, centerX, cornerY);

        if (label.getAlpha() <= 0) {
          iter.remove();
          ;
        }
      }
    }
  }

  /****************************************
   *                                      *
   *             SKILLS		            *
   *                                      *
   *                                      *
   ****************************************/
  public void addSkill(Skill newSkill) {
    Skills.put(newSkill.getName(), newSkill);
  }

  public HashMap<String, Skill> getSkills() {
    return Skills;
  }

  public Skill getSkill(String skillName) {
    return Skills.get(skillName);
  }

  public Skill getSkillById(int skillId) {
    for (Skill mySkill : Skills.values()) {
      if (mySkill.getId() == skillId) {
        return mySkill;
      }
    }
    return null;
  }

  /****************************************
   *                                      *
   *             USE ITEMS	            *
   *                                      *
   *                                      *
   ****************************************/
  public void useItem(Color useColor) {
    useAbilityAnimate(0, useColor);
  }

  /****************************************
   *                                      *
   *         ABILITIES 					*
   *                                      *
   *                                      *
   ****************************************/
  public void clearAbilities() {
    Abilities.clear();
  }

  public int hasAbility(String abilityName) {
    int abilityId = 9999;
    for (int i = 0; i < Abilities.size(); i++) {
      if (Abilities.get(i).getName().equals(abilityName)) {
        abilityId = i;
        return abilityId;
      }
    }
    return abilityId;
  }

  public void addAbility(Ability newAbility) {
    Abilities.add(newAbility);
  }

  public void addStatusEffect(StatusEffect newSE) {
    newSE.setActive(true);
    boolean exist = false;
    for (StatusEffect checkSE : StatusEffects) {
      if (checkSE.getId() == newSE.getId()) {
        exist = true;
        break;
      }
    }

    if (!exist) {
      StatusEffects.add(newSE);
    }

    if (newSE.getAnimationId() > 0) {
      addCreatureAnimation(newSE.getAnimationId(), 100);
    }

    updateBonusStats();
  }

  public void addDashAnimation(int moveTilesX, int moveTilesY) {
    creatureAnimations.add(new DashAnimation(moveTilesX, moveTilesY));
  }

  public void addCreatureAnimation(int animationId, int attackSpeed) {
    int dX = 0;
    if (getRotation() > 0 && getRotation() < 180) {
      dX = 1;
    } else if (getRotation() > 180 && getRotation() < 360) {
      dX = -1;
    }

    int dY = 0;
    if (getRotation() > 270 || getRotation() < 90) {
      dY = -1;
    } else if (getRotation() < 270 && getRotation() > 90) {
      dY = 1;
    }

    float animationSpeed = 100.0f / ((attackSpeed) + 150.0f);

    if (animationId == 1) {
      creatureAnimations.add(new VerticalPumpingAnimation(animationSpeed));
    } else if (animationId == 2) {
      creatureAnimations.add(new HorizontalShake(animationSpeed));
    } else if (animationId == 3) {
      creatureAnimations.add(new JumpSpinAnimation(dX, animationSpeed));
    } else if (animationId == 4) {
      creatureAnimations.add(new ThrowAnimation(dX, animationSpeed));
    } else if (animationId == 5) {
      creatureAnimations.add(new HorizontalPumpingAnimation(animationSpeed));
    } else if (animationId == 6) {
      creatureAnimations.add(new RoundAttackAnimation(360.0f, animationSpeed * 0.14f));
    } else if (animationId == 7) {
      creatureAnimations.add(new RollAttackAnimation(dX, dY, animationSpeed));
    } else if (animationId == 8) {
      creatureAnimations.add(new ChargeBackAttackAnimation(dX, dY, animationSpeed));
    } else if (animationId == 9) {
      creatureAnimations.add(new BecomeBigAnimation(animationSpeed));
    } else if (animationId == 10) {
      creatureAnimations.add(new VerticalSqueezeAnimation(animationSpeed));
    }
    Sfx.playRandomPitch("animations/" + animationId);
  }

  public void updateBonusStats() {}

  public synchronized StatusEffect getStatusEffect(int sId) {
    for (StatusEffect s : StatusEffects) {
      if (s.getId() == sId) {
        return s;
      }
    }
    return null;
  }

  public boolean hasManaShield() {
    if (getStatusEffect(25) == null) {
      return false;
    }
    return true;
  }

  public synchronized Vector<StatusEffect> getStatusEffects() {
    return StatusEffects;
  }

  public void removeStatusEffect(int seId) {

    for (int i = 0; i < StatusEffects.size(); i++) {
      if (StatusEffects.get(i).getId() == seId) {
        StatusEffects.get(i).setActive(false);
        StatusEffects.remove(i);
        break;
      }
    }
  }

  public boolean hasStatusEffect(int j) {
    boolean hasStat = false;
    for (int i = 0; i < StatusEffects.size(); i++) {
      if (StatusEffects.get(i).getId() == j) {
        hasStat = true;
        break;
      }
    }
    return hasStat;
  }

  public synchronized Vector<Ability> getAbilities() {
    return Abilities;
  }

  public int getNrAbilities() {
    return Abilities.size();
  }

  public Ability getAbility(int index) {
    return Abilities.get(index);
  }

  public Ability getAbilityById(int abilityId) {
    for (int i = 0; i < Abilities.size(); i++) {
      if (Abilities.get(i).getAbilityId() == abilityId) {
        return Abilities.get(i);
      }
    }
    return null;
  }

  public void clearDamageLabels() {
    damageLabels.clear();
  }

  public void addDamageLabel(String labelText, Color newColor, int graphicsNr) {
    damageLabels.put(
        damageLabelId,
        new DamageLabel(
            labelText,
            newColor,
            X * ClientSettings.TILE_SIZE + 25,
            Y * ClientSettings.TILE_SIZE + (SizeHeight - 1) * 25,
            graphicsNr));
    damageLabelId++;
    if (damageLabelId > 50) {
      damageLabelId = 0;
    }
  }

  /****************************************
   *                                      *
   *             ATTACK/ANIMATION			*
   *                                      *
   *                                      *
   ****************************************/
  public void attackTarget(int dX, int dY, float attackSpeed) {
    float animationSpeed = 100.0f / attackSpeed;
    creatureAnimations.add(new AttackAnimation(dX, dY, animationSpeed));
  }

  public void hitByAttack(int damage, String criticalOrMiss, String attackType, Color hitColor) {

    MissedHit = false;
    EvadedHit = false;

    if (criticalOrMiss.equals("true")) {
      IsCriticalHit = true;
      Sfx.playRandomPitch("battle/critical_atk");
    } else {
      if (criticalOrMiss.equals("miss")) {
        addDamageLabel("MISS", BlueSagaColors.RED, 0);
        MissedHit = true;
        Sfx.playRandomPitch("battle/miss_atk");
      } else if (criticalOrMiss.equals("evade")) {
        addDamageLabel("EVADED", BlueSagaColors.RED, 0);
        EvadedHit = true;
        Sfx.playRandomPitch("battle/evade_atk");
      }
      IsCriticalHit = false;
    }

    if (damage > mostHitDamage) {
      mostHitDamage = damage;
    }

    animationDamage = true;
    AnimationDamageItr = 0;

    if (!MissedHit && !EvadedHit) {
      animationHit = true;
      Sfx.playRandomPitch("battle/" + attackType + "_atk");

      int showDamage = damage;

      hitType = attackType;

      hitImage = ImageResource.getSprite("effects/attack" + hitType);

      // Update Health Bar

      hitColor1 = new Color(hitColor.getRed(), hitColor.getGreen(), hitColor.getBlue(), 0);
      hitColor2 = new Color(hitColor.getRed(), hitColor.getGreen(), hitColor.getBlue(), 200);

      if (showDamage < 0) {
        showDamage *= -1;
        addDamageLabel("+" + showDamage, hitColor, 0);
      } else {
        addDamageLabel(showDamage + "", hitColor, 0);
      }
      AnimationHitItr = 0;
    }

    // SCREEN EFFECT OF HIT
    if (creatureType == CreatureType.Player && getDBId() == BlueSaga.playerCharacter.getDBId()) {
      StatusScreenEffect screenFX = new StatusScreenEffect("Hit");
      float dmgPerc = damage * 10.0f / getStat("MAX_HEALTH");
      screenFX.setAlpha(dmgPerc);
      BlueSaga.GUI.addScreenEffect(screenFX);
    }
  }

  public void hitAnimation() {
    // create a new timeline for the given object
    if (animationHit) {
      if (AnimationHitItr % 6 == 0) {
        if (animationColor.getAlpha() == hitColor1.getAlpha()) {
          animationColor = hitColor2;
        } else {
          animationColor = hitColor1;
        }
      }

      AnimationHitItr++;

      if (AnimationHitItr >= 36) {
        animationColor = new Color(255, 255, 255, 0);
        animationHit = false;
      }
    }
  }

  // SHOW DAMAGE

  public boolean isHit() {
    return animationHit;
  }

  public void die() {
    StatusEffects.clear();
    Dead = true;
    ATTACKED = false;

    animationDeathY = 0;
    MyEquipHandler.setHideEquipment(true);

    timerDie.schedule(
        new TimerTask() {
          @Override
          public void run() {
            damageLabels.clear();
            dieHide();
          }
        },
        2500);
  }

  public void dieHide() {
    if (Dead) {
      hidden = true;
      animationDeathY = 0;
    } else {
      animation = frontAnimation;
      hidden = false;
      MyEquipHandler.setHideEquipment(false);
    }
  }

  public boolean isDead() {
    return Dead;
  }

  public void setDead(boolean deadStatus) {
    Dead = deadStatus;
  }

  public void revive() {
    StatusEffects.clear();
    BonusStats.clear();
    Health = getTotalStat("MAX_HEALTH");
    Mana = getTotalStat("MAX_MANA");
    HealthStatus = 4;
    Dead = false;
    hidden = false;
    MyEquipHandler.setHideEquipment(false);
    setRotation(181.0f);
    setGotoRotation(181.0f);
    animation = frontAnimation;
  }

  public void setHidden(boolean hiddenStatus) {
    hidden = hiddenStatus;
  }

  public void reviveAfterBattle() {
    StatusEffects.clear();

    Health = 1;
    Dead = false;
    hidden = false;
    MyEquipHandler.setHideEquipment(false);
    animation = frontAnimation;
  }

  public void changeHealth(int changeH) {
    Health += changeH;
    if (Health < 0) {
      Health = 0;
      // die
    }

    if (Health > getTotalStat("MAX_HEALTH")) {
      Health = getTotalStat("MAX_HEALTH");
    }
  }

  public void changeMana(int changeM) {
    Mana += changeM;
    if (Mana < 0) {
      Mana = 0;
    }
    if (Mana > getTotalStat("MAX_MANA")) {
      Mana = getTotalStat("MAX_MANA");
    }
  }

  /****************************************
   *                                      *
   *          APPEAR/DISSAPPEAR        	*
   *                                      *
   *                                      *
   ****************************************/
  public void dissappear() {
    hidden = false;
    MyEquipHandler.setHideEquipment(true);

    //BP_CLIENT.myEmitterManager.SpawnEmitter(aXPos, aYPos, aEmitterType)

    animation = deathAnimation;

    timerDie.schedule(
        new TimerTask() {
          @Override
          public void run() {
            hidden = true;
          }
        },
        500);

    Sfx.playRandomPitch("abilities/appear");
  }

  public void appear() {
    hidden = false;
    MyEquipHandler.setHideEquipment(true);

    animation = deathAnimation;
    Dead = false;
    setRemoved(false);

    timerDie.schedule(
        new TimerTask() {
          @Override
          public void run() {
            hidden = false;
            if (gotoRotation > 270.0f || gotoRotation < 90.0f) {
              animation = backAnimation;
            } else {
              animation = frontAnimation;
            }

            MyEquipHandler.setHideEquipment(false);
          }
        },
        500);
    Sfx.playRandomPitch("abilities/appear");
  }

  public void showLevelUp() {
    LevelUpTimer = new Timer();

    animationUseAbility.restart();
    animationUseAbility.start();

    ShowLevelUp = true;
    ShowLevelDown = false;

    LevelUpTimer.schedule(
        new TimerTask() {
          @Override
          public void run() {
            ShowLevelUp = false;
            animationUseAbility.stop();
          }
        },
        1000);
  }

  /**
   * Setters and Getters
   * @return
   */
  public int getHealth() {
    return Health;
  }

  public void setHealth(int newHealth) {
    Health = newHealth;
  }

  public int getMana() {
    return Mana;
  }

  public void setMana(int newMana) {
    Mana = newMana;
  }

  public int getSizeWidth() {
    return SizeWidth;
  }

  public int getSizeHeight() {
    return SizeHeight;
  }

  public void setSizeWidth(int newSizeW) {
    SizeWidth = newSizeW;
  }

  public void setSizeHeight(int newSizeH) {
    SizeHeight = newSizeH;
  }

  public int getMostHitDamage() {
    return mostHitDamage;
  }

  public void setAttackTarget(CreatureType attackTargetType, int attackTargetId) {
    AttackTargetType = attackTargetType;
    AttackTargetId = attackTargetId;
  }

  public int getAttackTargetId() {
    return AttackTargetId;
  }

  public CreatureType getAttackTargetType() {
    return AttackTargetType;
  }

  public String getAttackType() {
    if (MyEquipHandler.getWeaponAttackType().equals("")) {
      return AttackType;
    }
    return MyEquipHandler.getWeaponAttackType();
  }

  public void setCreatureId(int creatureId) {
    CreatureId = creatureId;
    frontAnimation = ImageResource.getSprite("creatures/m" + CreatureId).getAnimation();

    if (ImageResource.getSprite("creatures/m" + CreatureId + "b") != null) {
      backAnimation = ImageResource.getSprite("creatures/m" + CreatureId + "b").getAnimation();
    } else {
      backAnimation = frontAnimation;
    }

    if (getRotation() > 90 && getRotation() < 270) {
      animation = frontAnimation;
    } else {
      animation = backAnimation;
    }

    updateItemCoordinates();
  }

  public int getCreatureId() {
    return CreatureId;
  }

  public void setName(String newName) {
    Name = newName;
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

  public int getPixelX() {
    return X * ClientSettings.TILE_SIZE;
  }

  public int getPixelY() {
    return Y * ClientSettings.TILE_SIZE;
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
    return Health + " / " + getTotalStat("MAX_HEALTH");
  }

  public int getHealthBarWidth(int Max) {
    float fHealth = Health;
    float fMaxHealth = getTotalStat("MAX_HEALTH");
    float healthBarWidth = (fHealth / fMaxHealth) * Max;
    return Math.round(healthBarWidth);
  }

  public String getManaAsString() {
    return Mana + " / " + getTotalStat("MAX_MANA");
  }

  public int getManaBarWidth(int Max) {
    float fMana = Mana;
    float fMaxMana = getTotalStat("MAX_MANA");
    float manaBarWidth = (fMana / fMaxMana) * Max;
    return Math.round(manaBarWidth);
  }

  public int getHealthRegainBarWidth(int Max) {
    float fHealth = getTotalStat("HEALTH_REGAIN");
    float fMaxHealth = getTotalStat("MAX_HEALTH");
    float healthBarWidth = (fHealth / fMaxHealth) * Max;
    return Math.round(healthBarWidth);
  }

  public int getManaRegainBarWidth(int Max) {
    float fHealth = getTotalStat("MANA_REGAIN");
    float fMaxHealth = getTotalStat("MAX_MANA");
    float healthBarWidth = (fHealth / fMaxHealth) * Max;
    return Math.round(healthBarWidth);
  }

  public int getStat(String StatType) {
    return Stats.getValue(StatType);
  }

  public void setStat(String StatType, int value) {
    Stats.setValue(StatType, value);
  }

  public void clearBonusStats() {
    BonusStats.clear();
    BonusStats.reset();
  }

  public int getBonusStat(String StatType) {
    return BonusStats.getValue(StatType);
  }

  public void setBonusStat(String StatType, int value) {
    BonusStats.setValue(StatType, value);
  }

  public int getTotalStat(String StatType) {
    int totalStat = Stats.getValue(StatType);
    totalStat += BonusStats.getValue(StatType);
    return totalStat;
  }

  public HashMap<String, Integer> getStats() {
    return Stats.getHashMap();
  }

  public boolean getAttacked() {
    return ATTACKED;
  }

  public void setAttacked(boolean attackedStatus) {
    ATTACKED = attackedStatus;
  }

  public void setX(int newX) {
    X = newX;
    MyWalkHandler.setOldX(newX);
  }

  public void setY(int newY) {
    Y = newY;
    MyWalkHandler.setOldY(newY);
  }

  public int getDBId() {
    return dbId;
  }

  public void setDBId(int newDBId) {
    dbId = newDBId;
  }

  public int getScreenPixelX() {
    return screenPixelX;
  }

  public void setScreenPixelX(int screenPixelX) {
    this.screenPixelX = screenPixelX;
  }

  public int getScreenPixelY() {
    return screenPixelY;
  }

  public void setScreenPixelY(int screenPixelY) {
    this.screenPixelY = screenPixelY;
  }

  public int getHealthStatus() {
    return HealthStatus;
  }

  public void setHealthStatus(int healthStatus) {
    HealthStatus = healthStatus;
  }

  public void setZ(int newZ) {
    Z = newZ;
  }

  public int getZ() {
    return Z;
  }

  public CreatureType getCreatureType() {
    return creatureType;
  }

  public void setCreatureType(CreatureType creatureType) {
    this.creatureType = creatureType;
  }

  public int getAttackRange() {
    return AttackRange;
  }

  public void setAttackRange(int attackRange) {
    AttackRange = attackRange;
  }

  public boolean isResting() {
    return Resting;
  }

  public void setResting(boolean resting) {
    Resting = resting;
  }

  public Customization getCustomization() {
    return Customization;
  }

  public void setCustomization(Customization customization) {
    Customization = customization;
  }

  public void updateItemCoordinates() {
    if (MonsterHandler.creatureDefinitions.get(CreatureId) != null) {

      MyEquipHandler.updateItemCoordinates();

      getCustomization()
          .setMouthFeatureX(
              MonsterHandler.creatureDefinitions
                  .get(CreatureId)
                  .getCustomization()
                  .getMouthFeatureX());
      getCustomization()
          .setMouthFeatureY(
              MonsterHandler.creatureDefinitions
                  .get(CreatureId)
                  .getCustomization()
                  .getMouthFeatureY());

      getCustomization()
          .setAccessoriesX(
              MonsterHandler.creatureDefinitions
                  .get(CreatureId)
                  .getCustomization()
                  .getAccessoriesX());
      getCustomization()
          .setAccessoriesY(
              MonsterHandler.creatureDefinitions
                  .get(CreatureId)
                  .getCustomization()
                  .getAccessoriesY());

      getCustomization()
          .setSkinFeatureX(
              MonsterHandler.creatureDefinitions
                  .get(CreatureId)
                  .getCustomization()
                  .getSkinFeatureX());
      getCustomization()
          .setSkinFeatureY(
              MonsterHandler.creatureDefinitions
                  .get(CreatureId)
                  .getCustomization()
                  .getSkinFeatureY());
    }
  }

  public boolean isHidden() {
    return hidden;
  }

  public boolean isRemoved() {
    return removed;
  }

  public void setRemoved(boolean newRemoved) {
    removed = newRemoved;
  }

  public float getRotation() {
    return rotation;
  }

  public void setRotation(float rotation) {
    this.rotation = rotation;
    directionRing.getImage().setRotation(rotation);
  }

  public float getGotoRotation() {
    return gotoRotation;
  }

  public void setGotoRotation(float gotoRotation) {
    this.gotoRotation = gotoRotation;
  }

  public void setRotationNow(float rotation) {
    this.rotation = rotation;
    this.gotoRotation = rotation;

    if (rotation > 270.0f || rotation < 90.0f) {
      animation = backAnimation;
    } else {
      animation = frontAnimation;
    }

    directionRing.getImage().setRotation(rotation);
  }

  public Vector<CreatureAnimation> getAnimations() {
    return creatureAnimations;
  }

  public boolean isLookRight() {
    return lookRight;
  }

  public void setLookRight(boolean lookRight) {
    this.lookRight = lookRight;
  }

  public float getSizeWidthF() {
    return sizeWidthF;
  }

  public void setSizeWidthF(float sizeWidthF) {
    this.sizeWidthF = sizeWidthF;
  }

  public float getSizeHeightF() {
    return sizeHeightF;
  }

  public void setSizeHeightF(float sizeHeightF) {
    this.sizeHeightF = sizeHeightF;
  }

  public Color getAnimationColor() {
    return animationColor;
  }

  public void setAnimationColor(Color animationColor) {
    this.animationColor = animationColor;
  }
}
