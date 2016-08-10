package creature;

import org.newdawn.slick.Image;

import abilitysystem.Ability;
import graphics.ImageResource;
import components.Item;
import data_handlers.ItemHandler;
import data_handlers.MonsterHandler;

public class CequipHandler {

  private Creature myCreature;

  private boolean hideEquipment = false;

  private int HeadX;
  private int HeadY;
  private int WeaponX;
  private int WeaponY;
  private int OffHandX;
  private int OffHandY;
  private int AmuletX;
  private int AmuletY;
  private int ArtifactX;
  private int ArtifactY;

  private Item HeadItem = null;
  private Item WeaponItem = null;
  private Item OffHandItem = null;
  private Item AmuletItem = null;
  private Item ArtifactItem = null;

  private String WeaponAttackType;
  private int WeaponSpeed = 0;

  public CequipHandler(Creature aCreature) {
    myCreature = aCreature;
  }

  public void drawBack(
      int x, int y, boolean flipped, float aniRotation, float scaleX, float scaleY) {
    int equipDir = 1;
    if (flipped) {
      equipDir = -1;
    }

    Image artifactGfx = null;

    if (myCreature.getCustomization().getArtifactSkinId() > 0) {
      artifactGfx =
          ImageResource.getSprite("items/item" + myCreature.getCustomization().getArtifactSkinId())
              .getImage()
              .getFlippedCopy(flipped, false);
    } else if (ArtifactItem != null && ArtifactItem.getId() > 0) {
      artifactGfx =
          ImageResource.getSprite("items/item" + ArtifactItem.getId())
              .getImage()
              .getFlippedCopy(flipped, false);
    }

    if (artifactGfx != null) {
      artifactGfx.setCenterOfRotation(
          25 + myCreature.getSizeWidth() * 25 - (ArtifactX * equipDir),
          25 + myCreature.getSizeHeight() * 25 - ArtifactY);
      artifactGfx.setRotation(aniRotation);
      artifactGfx.draw(
          x + ArtifactX * equipDir * scaleX - 25 * scaleX,
          y + ArtifactY * scaleY - 25 * scaleY,
          scaleX * 100,
          scaleY * 100);
    }

    if (!ItemHandler.GlovesIds.contains(myCreature.getCustomization().getWeaponSkinId())) {
      drawWeaponItem(x, y, flipped, aniRotation, scaleX, scaleY);
    }
  }

  public void drawFront(
      int x, int y, boolean flipped, float aniRotation, float scaleX, float scaleY) {
    int equipDir = 1;
    if (flipped) {
      equipDir = -1;
    }

    drawHeadItem(x, y, flipped, aniRotation, scaleX, scaleY);

    Image offHandGfx = null;

    if (myCreature.getCustomization().getOffHandSkinId() > 0) {
      offHandGfx =
          ImageResource.getSprite("items/item" + myCreature.getCustomization().getOffHandSkinId())
              .getImage()
              .getFlippedCopy(flipped, false);
    } else if (OffHandItem != null && OffHandItem.getId() > 0) {
      offHandGfx =
          ImageResource.getSprite("items/item" + OffHandItem.getId())
              .getImage()
              .getFlippedCopy(flipped, false);
    }

    if (offHandGfx != null) {
      offHandGfx.setCenterOfRotation(
          25 + myCreature.getSizeWidth() * 25 - (OffHandX * equipDir),
          25 + myCreature.getSizeHeight() * 25 - OffHandY);
      offHandGfx.setRotation(aniRotation);
      offHandGfx.draw(
          x + OffHandX * equipDir * scaleX + 25 * (myCreature.getSizeWidth() - 1 - scaleX),
          y + OffHandY * scaleY - 25 * scaleY,
          scaleX * 100,
          scaleY * 100);
    }

    Image amuletGfx = null;

    if (myCreature.getCustomization().getAmuletSkinId() > 0) {
      amuletGfx =
          ImageResource.getSprite("items/item" + myCreature.getCustomization().getAmuletSkinId())
              .getImage()
              .getFlippedCopy(flipped, false);
    } else if (AmuletItem != null && AmuletItem.getId() > 0) {
      amuletGfx =
          ImageResource.getSprite("items/item" + AmuletItem.getId())
              .getImage()
              .getFlippedCopy(flipped, false);
    }

    if (amuletGfx != null) {
      amuletGfx.setCenterOfRotation(
          25 + myCreature.getSizeWidth() * 25 - AmuletX * equipDir,
          25 + myCreature.getSizeHeight() * 25 - AmuletY);
      amuletGfx.setRotation(aniRotation);
      amuletGfx.draw(
          x + AmuletX * equipDir * scaleX - 25 * scaleX,
          y + AmuletY * scaleY - 25 * scaleY,
          scaleX * 100,
          scaleY * 100);
    }

    if (ItemHandler.GlovesIds.contains(myCreature.getCustomization().getWeaponSkinId())) {
      drawWeaponItem(x, y, flipped, aniRotation, scaleX, scaleY);
    }
  }

  public void drawWeaponItem(
      int x, int y, boolean flipped, float aniRotation, float scaleX, float scaleY) {
    int equipDir = 1;
    if (flipped) {
      equipDir = -1;
    }

    Image weaponGfx = null;

    if (myCreature.getCustomization().getWeaponSkinId() > 0) {
      weaponGfx =
          ImageResource.getSprite("items/item" + myCreature.getCustomization().getWeaponSkinId())
              .getImage()
              .getFlippedCopy(flipped, false);
    } else if (WeaponItem != null && WeaponItem.getId() > 0) {
      weaponGfx =
          ImageResource.getSprite("items/item" + WeaponItem.getId())
              .getImage()
              .getFlippedCopy(flipped, false);
    }

    if (weaponGfx != null) {
      weaponGfx.setCenterOfRotation(
          25 + myCreature.getSizeWidth() * 25 - WeaponX * equipDir,
          25 + myCreature.getSizeHeight() * 25 - WeaponY);
      weaponGfx.setRotation(aniRotation);
      weaponGfx.draw(
          x + WeaponX * equipDir * scaleX + 25 * (myCreature.getSizeWidth() - 1 - scaleX),
          y + WeaponY * scaleY - 25 * scaleY,
          scaleX * 100,
          scaleY * 100);
    }
  }

  public void drawHeadItem(
      int x, int y, boolean flipped, float aniRotation, float scaleX, float scaleY) {
    int equipDir = 1;
    if (flipped) {
      equipDir = -1;
    }

    Image headGfx = null;

    if (myCreature.getCustomization().getHeadSkinId() > 0) {
      headGfx =
          ImageResource.getSprite("items/item" + myCreature.getCustomization().getHeadSkinId())
              .getImage()
              .getFlippedCopy(flipped, false);
    } else if (HeadItem != null && HeadItem.getId() > 0) {

      headGfx =
          ImageResource.getSprite("items/item" + HeadItem.getId())
              .getImage()
              .getFlippedCopy(flipped, false);
    }

    if (headGfx != null) {
      headGfx.setCenterOfRotation(
          25 + myCreature.getSizeWidth() * 25 - HeadX * equipDir,
          25 + myCreature.getSizeHeight() * 25 - HeadY + 4);
      headGfx.setRotation(aniRotation);
      headGfx.draw(
          x + HeadX * equipDir * scaleX - 25 * scaleX,
          y + HeadY * scaleY - 29 * scaleY,
          scaleX * 100,
          scaleY * 100);
    }
  }

  public void clearEquipment() {
    WeaponItem = null;
    HeadItem = null;
    OffHandItem = null;
    AmuletItem = null;
    ArtifactItem = null;
  }

  public void equipItem(Item newItem) {
    if (newItem.getType().equals("Weapon")) {
      WeaponItem = newItem;
      WeaponAttackType = newItem.getAttackType();
    } else if (newItem.getType().equals("Head")) {
      HeadItem = newItem;
    } else if (newItem.getType().equals("OffHand")) {
      OffHandItem = newItem;
    } else if (newItem.getType().equals("Amulet")) {
      AmuletItem = newItem;
    } else if (newItem.getType().equals("Artifact")) {
      ArtifactItem = newItem;
    }

    //updateBonusStats();
  }

  public void unEquipItem(String equipType) {
    if (equipType.equals("Weapon")) {
      WeaponItem = null;
      WeaponAttackType = "";
    } else if (equipType.equals("Head")) {
      HeadItem = null;
    } else if (equipType.equals("OffHand")) {
      OffHandItem = null;
    } else if (equipType.equals("Amulet")) {
      AmuletItem = null;
    } else if (equipType.equals("Artifact")) {
      ArtifactItem = null;
    }
    //updateBonusStats();
  }

  public void unEquipAllItems() {
    WeaponItem = null;
    WeaponAttackType = "";
    HeadItem = null;
  }

  public Item getEquipment(String equipType) {
    if (equipType.equals("Weapon")) {
      return WeaponItem;
    } else if (equipType.equals("Head")) {
      return HeadItem;
    } else if (equipType.equals("OffHand")) {
      return OffHandItem;
    } else if (equipType.equals("Amulet")) {
      return AmuletItem;
    } else if (equipType.equals("Artifact")) {
      return ArtifactItem;
    }

    return null;
  }

  public boolean replenish(Ability ABILITY, int Amount) {

    boolean useSuccess = false;

    if (ABILITY.getName().equals("Heal")
        && myCreature.Health < myCreature.getTotalStat("MAX_HEALTH")) {
      myCreature.Health += Amount;

      if (myCreature.Health > myCreature.getTotalStat("MAX_HEALTH")) {
        myCreature.Health = myCreature.getTotalStat("MAX_HEALTH");
      }

      useSuccess = true;
    } else if (ABILITY.getName().equals("Mana")
        && myCreature.Mana < myCreature.getTotalStat("MAX_MANA")) {
      myCreature.Mana += Amount;
      if (myCreature.Mana > myCreature.getTotalStat("MAX_MANA")) {
        myCreature.Mana = myCreature.getTotalStat("MAX_MANA");
      }

      useSuccess = true;
    }

    return useSuccess;
  }

  public int getWeaponSpeed() {
    return WeaponSpeed;
  }

  public void setWeaponSpeed(int weaponSpeed) {
    WeaponSpeed = weaponSpeed;
  }

  public int getHeadX() {
    return HeadX;
  }

  public void setHeadX(int headX) {
    HeadX = headX;
  }

  public int getHeadY() {
    return HeadY;
  }

  public void setHeadY(int headY) {
    HeadY = headY;
  }

  public int getWeaponX() {
    return WeaponX;
  }

  public void setWeaponX(int weaponX) {
    WeaponX = weaponX;
  }

  public int getWeaponY() {
    return WeaponY;
  }

  public void setWeaponY(int weaponY) {
    WeaponY = weaponY;
  }

  public int getOffHandX() {
    return OffHandX;
  }

  public void setOffHandX(int offHandX) {
    OffHandX = offHandX;
  }

  public int getOffHandY() {
    return OffHandY;
  }

  public void setOffHandY(int offHandY) {
    OffHandY = offHandY;
  }

  public int getAmuletX() {
    return AmuletX;
  }

  public void setAmuletX(int amuletX) {
    AmuletX = amuletX;
  }

  public int getAmuletY() {
    return AmuletY;
  }

  public void setAmuletY(int amuletY) {
    AmuletY = amuletY;
  }

  public int getArtifactX() {
    return ArtifactX;
  }

  public void setArtifactX(int artifactX) {
    ArtifactX = artifactX;
  }

  public int getArtifactY() {
    return ArtifactY;
  }

  public void setArtifactY(int artifactY) {
    ArtifactY = artifactY;
  }

  public boolean isHideEquipment() {
    return hideEquipment;
  }

  public void setHideEquipment(boolean hideEquipment) {
    this.hideEquipment = hideEquipment;
  }

  public void setWeaponAttackType(String newWeaponAttack) {
    WeaponAttackType = newWeaponAttack;
  }

  public String getWeaponAttackType() {
    return WeaponAttackType;
  }

  public void updateItemCoordinates() {
    setHeadX(
        MonsterHandler.creatureDefinitions
            .get(myCreature.getCreatureId())
            .MyEquipHandler
            .getHeadX());
    setHeadY(
        MonsterHandler.creatureDefinitions
            .get(myCreature.getCreatureId())
            .MyEquipHandler
            .getHeadY());

    setWeaponX(
        MonsterHandler.creatureDefinitions
            .get(myCreature.getCreatureId())
            .MyEquipHandler
            .getWeaponX());
    setWeaponY(
        MonsterHandler.creatureDefinitions
            .get(myCreature.getCreatureId())
            .MyEquipHandler
            .getWeaponY());

    setArtifactX(
        MonsterHandler.creatureDefinitions
            .get(myCreature.getCreatureId())
            .MyEquipHandler
            .getArtifactX());
    setArtifactY(
        MonsterHandler.creatureDefinitions
            .get(myCreature.getCreatureId())
            .MyEquipHandler
            .getArtifactY());

    setAmuletX(
        MonsterHandler.creatureDefinitions
            .get(myCreature.getCreatureId())
            .MyEquipHandler
            .getAmuletX());
    setAmuletY(
        MonsterHandler.creatureDefinitions
            .get(myCreature.getCreatureId())
            .MyEquipHandler
            .getAmuletY());

    setOffHandX(
        MonsterHandler.creatureDefinitions
            .get(myCreature.getCreatureId())
            .MyEquipHandler
            .getOffHandX());
    setOffHandY(
        MonsterHandler.creatureDefinitions
            .get(myCreature.getCreatureId())
            .MyEquipHandler
            .getOffHandY());
  }
}
