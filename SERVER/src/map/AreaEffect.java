package map;

public class AreaEffect {

  private int id = 0;

  private String areaName = "";

  private int guardedLevel = 0;

  private String particles = "None";

  private int tint = 0;
  private String tintColor = "0";

  private int fog = 0;
  private String fogColor = "0";

  private String song = "None";
  private String ambient = "None";

  private String areaItems = "None";
  private int areaCopper = 0;

  public AreaEffect(int id) {
    this.id = id;
  }

  public String getInfo() {
    String effectData =
        id + "," + areaName + "," + tint + "," + tintColor + "," + fog + "," + fogColor + "," + song
            + "," + ambient + "," + particles;
    return effectData;
  }

  /**
   * Getters and setters
   * @return
   */
  public int getId() {
    return id;
  }

  public String getTintColor() {
    return tintColor;
  }

  public void setTintColor(String tintColor) {
    this.tintColor = tintColor;
  }

  public int getFog() {
    return fog;
  }

  public void setFog(int fog) {
    this.fog = fog;
  }

  public String getFogColor() {
    return fogColor;
  }

  public void setFogColor(String fogColor) {
    this.fogColor = fogColor;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getAreaName() {
    return areaName;
  }

  public void setAreaName(String areaName) {
    this.areaName = areaName;
  }

  public int getGuardedLevel() {
    return guardedLevel;
  }

  public void setGuardedLevel(int guardedLevel) {
    this.guardedLevel = guardedLevel;
  }

  public String getParticles() {
    return particles;
  }

  public void setParticles(String particles) {
    this.particles = particles;
  }

  public int getTint() {
    return tint;
  }

  public void setTint(int tint) {
    this.tint = tint;
  }

  public String getAreaItems() {
    return areaItems;
  }

  public void setAreaItems(String areaItems) {
    this.areaItems = areaItems;
  }

  public int getAreaCopper() {
    return areaCopper;
  }

  public void setAreaCopper(int areaCopper) {
    this.areaCopper = areaCopper;
  }

  public String getSong() {
    return song;
  }

  public void setSong(String song) {
    this.song = song;
  }

  public String getAmbient() {
    return ambient;
  }

  public void setAmbient(String ambient) {
    this.ambient = ambient;
  }
}
