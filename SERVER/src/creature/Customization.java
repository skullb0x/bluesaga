package creature;

import java.util.HashMap;

public class Customization {

  public HashMap<String, Integer> coords = new HashMap<String, Integer>();

  private int MouthFeatureId = 0;
  private int AccessoriesId = 0;
  private int SkinFeatureId = 0;

  private int HeadSkinId = 0;
  private int WeaponSkinId = 0;
  private int OffHandSkinId = 0;
  private int AmuletSkinId = 0;
  private int ArtifactSkinId = 0;

  public Customization() {
    coords.put("MouthFeatureX", 0);
    coords.put("MouthFeatureY", 0);
    coords.put("AccessoriesX", 0);
    coords.put("AccessoriesY", 0);
    coords.put("SkinFeatureX", 0);
    coords.put("SkinFeatureY", 0);
  }

  /**
   * Setters and getters
   * @return
   */
  public int getMouthFeatureX() {
    return coords.get("MouthFeatureX");
  }

  public void setMouthFeatureX(int mouthFeatureX) {
    coords.put("MouthFeatureX", mouthFeatureX);
  }

  public int getMouthFeatureY() {
    return coords.get("MouthFeatureY");
  }

  public void setMouthFeatureY(int mouthFeatureY) {
    coords.put("MouthFeatureY", mouthFeatureY);
  }

  public int getAccessoriesX() {
    return coords.get("AccessoriesX");
  }

  public void setAccessoriesX(int accessoriesX) {
    coords.put("AccessoriesX", accessoriesX);
  }

  public int getAccessoriesY() {
    return coords.get("AccessoriesY");
  }

  public void setAccessoriesY(int accessoriesY) {
    coords.put("AccessoriesY", accessoriesY);
  }

  public int getSkinFeatureX() {
    return coords.get("SkinFeatureX");
  }

  public void setSkinFeatureX(int skinFeatureX) {
    coords.put("SkinFeatureX", skinFeatureX);
  }

  public int getSkinFeatureY() {
    return coords.get("SkinFeatureY");
  }

  public void setSkinFeatureY(int skinFeatureY) {
    coords.put("SkinFeatureY", skinFeatureY);
  }

  /**
   * Customization Ids
   * @return
   */
  public int getMouthFeatureId() {
    return MouthFeatureId;
  }

  public void setMouthFeatureId(int mouthFeatureId) {
    MouthFeatureId = mouthFeatureId;
  }

  public int getAccessoriesId() {
    return AccessoriesId;
  }

  public void setAccessoriesId(int accessoriesId) {
    AccessoriesId = accessoriesId;
  }

  public int getSkinFeatureId() {
    return SkinFeatureId;
  }

  public void setSkinFeatureId(int skinFeatureId) {
    SkinFeatureId = skinFeatureId;
  }

  public int getHeadSkinId() {
    return HeadSkinId;
  }

  public void setHeadSkinId(int headSkinId) {
    HeadSkinId = headSkinId;
  }

  public int getWeaponSkinId() {
    return WeaponSkinId;
  }

  public void setWeaponSkinId(int weaponSkinId) {
    WeaponSkinId = weaponSkinId;
  }

  public int getOffHandSkinId() {
    return OffHandSkinId;
  }

  public void setOffHandSkinId(int offHandSkinId) {
    OffHandSkinId = offHandSkinId;
  }

  public int getAmuletSkinId() {
    return AmuletSkinId;
  }

  public void setAmuletSkinId(int amuletSkinId) {
    AmuletSkinId = amuletSkinId;
  }

  public int getArtifactSkinId() {
    return ArtifactSkinId;
  }

  public void setArtifactSkinId(int artifactSkinId) {
    ArtifactSkinId = artifactSkinId;
  }
}
