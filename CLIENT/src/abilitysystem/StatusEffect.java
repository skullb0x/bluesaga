package abilitysystem;

import org.newdawn.slick.Color;

import components.Stats;
import particlesystem.Emitter.Emitter;
import creature.Creature;
import graphics.ImageResource;
import graphics.Sprite;

public class StatusEffect {

  private int Id;
  private String Name = "";
  private Stats StatsModif;
  private int DurationItr;
  private int Duration; // Duration in seconds
  private int RepeatDamage;
  private String RepeatDamageType;

  private String CasterType;
  private Creature Caster;

  private boolean Active;

  private Sprite Gfx;
  private float scale = 0.0f;

  private int GraphicsNr;
  private int AnimationId;

  private Color Color;

  public StatusEffect(int newId, int graphicsNr) {
    setId(newId);
    setGraphicsNr(graphicsNr);

    setCasterType("None");
    Gfx = ImageResource.getSprite("statuseffects/" + getGraphicsNr());
    /*
    int randomFrame = RandomUtils.getInt(0,Gfx.getFrameCount()-1);
    Gfx.getAnimation().setCurrentFrame(randomFrame);
    */
    setDurationItr(0);

    Active = true;
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

  public Stats getStatsModif() {
    return StatsModif;
  }

  public int getId() {
    return Id;
  }

  public void setId(int id) {
    Id = id;
  }

  public boolean isActive() {
    return Active;
  }

  public void setActive(boolean active) {
    Active = active;
  }

  public String getCasterType() {
    return CasterType;
  }

  public void setCasterType(String casterType) {
    CasterType = casterType;
  }

  public Creature getCaster() {
    return Caster;
  }

  public void setCaster(Creature caster) {
    Caster = caster;
  }

  public Sprite getSprite() {
    return Gfx;
  }

  public void draw(int x, int y) {
    if (scale < 1.0f) {
      scale += 0.1f;
    }
    Gfx.getAnimation().updateNoDraw();
    Gfx.getAnimation().getCurrentFrame().getScaledCopy(scale).drawCentered(x, y);

    //MyEmitter.SetPosition(x,y);
  }

  public Color getColor() {
    return Color;
  }

  public void setColor(Color newColor) {
    Color = newColor;
  }

  public int getGraphicsNr() {
    return GraphicsNr;
  }

  public void setGraphicsNr(int graphicsNr) {
    GraphicsNr = graphicsNr;
  }

  public boolean increaseDurationItr(int value) {
    DurationItr += value;
    if (DurationItr > getDuration()) {
      DurationItr = getDuration();
      setActive(false);
    }
    return isActive();
  }

  public int getDurationItr() {
    return DurationItr;
  }

  public void setDurationItr(int durationItr) {
    DurationItr = durationItr;
  }

  public int getAnimationId() {
    return AnimationId;
  }

  public void setAnimationId(int animationId) {
    AnimationId = animationId;
  }
}
