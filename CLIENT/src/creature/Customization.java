package creature;

import org.newdawn.slick.Image;

import graphics.ImageResource;

public class Customization {

  private int CreatureId;

  private int MouthFeatureX = 0;
  private int MouthFeatureY = 0;
  private int AccessoriesX = 0;
  private int AccessoriesY = 0;
  private int SkinFeatureX = 0;
  private int SkinFeatureY = 0;

  private int MouthFeatureId = 0;
  private int AccessoriesId = 0;
  private int SkinFeatureId = 0;

  private int HeadSkinId = 0;
  private int WeaponSkinId = 0;
  private int OffHandSkinId = 0;
  private int AmuletSkinId = 0;
  private int ArtifactSkinId = 0;

  public void setSkin(String skinType, int skinId) {
    if (skinType.equals("Head")) {
      HeadSkinId = skinId;
    } else if (skinType.equals("Weapon")) {
      WeaponSkinId = skinId;
    } else if (skinType.equals("OffHand")) {
      OffHandSkinId = skinId;
    } else if (skinType.equals("Amulet")) {
      AmuletSkinId = skinId;
    } else if (skinType.equals("Artifact")) {
      ArtifactSkinId = skinId;
    }
  }

  public void clearSkin() {
    HeadSkinId = 0;
    WeaponSkinId = 0;
    OffHandSkinId = 0;
    AmuletSkinId = 0;
    ArtifactSkinId = 0;
  }

  public int getMouthFeatureX() {
    return MouthFeatureX;
  }

  public void setMouthFeatureX(int mouthFeatureX) {
    MouthFeatureX = mouthFeatureX;
  }

  public int getMouthFeatureY() {
    return MouthFeatureY;
  }

  public void setMouthFeatureY(int mouthFeatureY) {
    MouthFeatureY = mouthFeatureY;
  }

  public int getAccessoriesX() {
    return AccessoriesX;
  }

  public void setAccessoriesX(int accessoriesX) {
    AccessoriesX = accessoriesX;
  }

  public int getAccessoriesY() {
    return AccessoriesY;
  }

  public void setAccessoriesY(int accessoriesY) {
    AccessoriesY = accessoriesY;
  }

  public int getSkinFeatureX() {
    return SkinFeatureX;
  }

  public void setSkinFeatureX(int skinFeatureX) {
    SkinFeatureX = skinFeatureX;
  }

  public int getSkinFeatureY() {
    return SkinFeatureY;
  }

  public void setSkinFeatureY(int skinFeatureY) {
    SkinFeatureY = skinFeatureY;
  }

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

  public Customization(int creatureId) {
    setCreatureId(creatureId);
  }

  public void draw(int x, int y, boolean flipped, float aniRotation, float scaleX, float scaleY) {
    int equipDir = 1;
    if (flipped) {
      equipDir = -1;
    }
    if (getSkinFeatureId() > 0) {
      Image skinGfx =
          ImageResource.getSprite("items/item" + getSkinFeatureId())
              .getImage()
              .getFlippedCopy(flipped, false);
      skinGfx.setCenterOfRotation(50 - SkinFeatureX * equipDir, 50 - SkinFeatureY);
      skinGfx.setRotation(aniRotation);
      skinGfx.draw(
          x + getSkinFeatureX() * equipDir * scaleX - 25 * scaleX,
          y + getSkinFeatureY() * scaleY - 25 * scaleY,
          scaleX * 100,
          scaleY * 100);
    }
    if (getMouthFeatureId() > 0) {
      Image mouthGfx =
          ImageResource.getSprite("items/item" + getMouthFeatureId())
              .getImage()
              .getFlippedCopy(flipped, false);
      mouthGfx.setCenterOfRotation(50 - MouthFeatureX * equipDir, 50 - MouthFeatureY);
      mouthGfx.setRotation(aniRotation);
      mouthGfx.draw(
          x + getMouthFeatureX() * equipDir * scaleX - 25 * scaleX,
          y + getMouthFeatureY() * scaleY - 25 * scaleY,
          scaleX * 100,
          scaleY * 100);
    }
    if (getAccessoriesId() > 0) {
      Image accessoriesGfx =
          ImageResource.getSprite("items/item" + getAccessoriesId())
              .getImage()
              .getFlippedCopy(flipped, false);
      accessoriesGfx.setCenterOfRotation(50 - AccessoriesX * equipDir, 50 - AccessoriesY);
      accessoriesGfx.setRotation(aniRotation);
      accessoriesGfx.draw(
          x + getAccessoriesX() * equipDir * scaleX - 25 * scaleX,
          y + getAccessoriesY() * scaleY - 25 * scaleY,
          scaleX * 100,
          scaleY * 100);
    }
  }

  public int getCreatureId() {
    return CreatureId;
  }

  public void setCreatureId(int creatureId) {
    CreatureId = creatureId;
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
