package data_handlers;

import org.newdawn.slick.Color;

import abilitysystem.Ability;
import abilitysystem.StatusEffect;
import projectile.Projectile;
import screens.ScreenHandler;
import sound.Sfx;
import map.Tile;
import creature.Creature;
import creature.Creature.CreatureType;
import game.BlueSaga;
import graphics.screeneffects.StatusScreenEffect;
import gui.Gui;

public class AbilityHandler extends Handler {

  private static int abilityCooldown = 4;
  private static int abilityCooldownItr = 0;

  public AbilityHandler() {
    super();
  }

  public static boolean readyToUseAbility() {
    if (abilityCooldownItr == 0) {
      return true;
    }
    return false;
  }

  public static void resetUseAbilityCooldown() {
    abilityCooldownItr = abilityCooldown;
  }

  public static void updateAbilityCooldown() {
    if (abilityCooldownItr > 0) {
      abilityCooldownItr--;
    }
  }

  public static void handleData(String serverData) {

    if (serverData.startsWith("<abilitydata>")) {
      String abilityInfo[] = serverData.substring(13).split("/");

      int load_more = Integer.parseInt(abilityInfo[0]);

      // nrAbilities; Id,Name,Red,Green,Blue,ManaCost,Cooldown,CooldownLeft,Range,Price,TargetSelf,Instant,EquipReq

      BlueSaga.playerCharacter.clearAbilities();

      int nrAbilities = Integer.parseInt(abilityInfo[1]);

      for (int i = 0; i < nrAbilities; i++) {

        String info[] = abilityInfo[i + 2].split("=");
        int AbilityId = Integer.parseInt(info[0]);

        Ability newAB = new Ability(AbilityId);
        newAB.load(abilityInfo[i + 2]);

        BlueSaga.playerCharacter.addAbility(newAB);
      }

      if (Gui.AbilitiesWindow != null) {
        if (Gui.AbilitiesWindow.isOpen()) {
          Gui.AbilitiesWindow.load();
        }
      }

      if (load_more == 1) {
        ScreenHandler.LoadingStatus = "Loading actionbar data...";
        BlueSaga.client.sendMessage("actionbar", "info");
      }
    }

    // INFO ABOUT ONE ABILITY IN INFOBOX
    if (serverData.startsWith("<ability_info>")) {
      String info[] = serverData.substring(14).split("#");

      if (info[0].equals("equip")) {
        Gui.StatusWindow.showInfoBox(info[1]);
      } else if (info[0].equals("shop")) {
        Gui.ShopWindow.showInfoBox(info[1]);
      } else if (info[0].equals("abilityW")) {
        Gui.AbilitiesWindow.showInfoBox(info[1]);
      } else {
        Gui.InventoryWindow.showInfoBox(info[1]);
      }
    }

    if (serverData.startsWith("<statuseffect_hit>")) {
      // VictimType; VictimId; StatusEffectId; Damage
      String creatureSEinfo[] = serverData.substring(18).split(";");

      Creature TARGET = MapHandler.addCreatureToScreen(creatureSEinfo[0]);

      String seInfo[] = creatureSEinfo[1].split(",");

      int healthStatus = Integer.parseInt(seInfo[0]);

      int seId = Integer.parseInt(seInfo[1]);
      int graphicsNr = Integer.parseInt(seInfo[2]);

      String damageType = seInfo[3];

      Sfx.playRandomPitch(damageType + "_atk");

      int colorR = Integer.parseInt(seInfo[5]);
      int colorG = Integer.parseInt(seInfo[6]);
      int colorB = Integer.parseInt(seInfo[7]);

      TARGET.setHealthStatus(healthStatus);

      StatusEffect SE = new StatusEffect(seId, graphicsNr);
      SE.setColor(new Color(colorR, colorG, colorB));
      SE.setRepeatDamageType(damageType);

      // Add hit screen effect
      if (TARGET.getCreatureType() == CreatureType.Player
          && TARGET.getDBId() == BlueSaga.playerCharacter.getDBId()) {
        StatusScreenEffect screenFX = new StatusScreenEffect("Hit");
        screenFX.setEffectColor(SE.getColor());

        BlueSaga.GUI.addScreenEffect(screenFX);
      }
    }

    if (serverData.startsWith("<use_ability>")) {
      // CasterType; CasterId; AbilityId
      String useInfo[] = serverData.substring(13).split(";");

      Creature CASTER = MapHandler.addCreatureToScreen(useInfo[0]);

      if (CASTER != null) {
        String AbilityInfo[] = useInfo[1].split(",");
        int AbilityId = Integer.parseInt(AbilityInfo[0]);
        int colorR = Integer.parseInt(AbilityInfo[1]);
        int colorG = Integer.parseInt(AbilityInfo[2]);
        int colorB = Integer.parseInt(AbilityInfo[3]);
        int GraphicsNr = Integer.parseInt(AbilityInfo[4]);
        int animationId = Integer.parseInt(AbilityInfo[5]);
        int attackSpeed = Integer.parseInt(AbilityInfo[6]);

        Ability ABILITY = null;
        if (CASTER.getCreatureType() == CreatureType.Player
            && CASTER.getDBId() == BlueSaga.playerCharacter.getDBId()) {
          Gui.cancelUseAbility();
          ABILITY = BlueSaga.playerCharacter.getAbilityById(AbilityId);
          resetUseAbilityCooldown();
        } else {
          ABILITY = new Ability(AbilityId);
        }

        if (ABILITY != null) {
          ABILITY.setColor(new Color(colorR, colorG, colorB));
          ABILITY.setGraphicsNr(GraphicsNr);
          ABILITY.used();

          CASTER.addCreatureAnimation(animationId, attackSpeed);

          if (animationId == 0) {
            CASTER.useAbility(ABILITY);
            Sfx.playRandomPitch("battle/ability_use");
          }
        }
      }
    }

    if (serverData.startsWith("<projectile>")) {
      // CasterType, CasterId / GoalX, GoalY, GfxName, ParticlesId, Delay

      String projectileInfo[] = serverData.substring(12).split(",");

      int projectileId = Integer.parseInt(projectileInfo[0]);

      int startX = Integer.parseInt(projectileInfo[1]);
      int startY = Integer.parseInt(projectileInfo[2]);
      int startZ = Integer.parseInt(projectileInfo[3]);

      int goalX = Integer.parseInt(projectileInfo[4]);
      int goalY = Integer.parseInt(projectileInfo[5]);

      int delay = Integer.parseInt(projectileInfo[6]);
      float speed = Float.parseFloat(projectileInfo[7]);

      int effectId = Integer.parseInt(projectileInfo[8]);

      Projectile newProjectile =
          new Projectile(
              projectileId, startX, startY, startZ, goalX, goalY, delay, speed, effectId);

      ScreenHandler.ProjectileManager.addProjectile(newProjectile);
    }

    if (serverData.startsWith("<tile_effect>")) {
      String effectInfo[] = serverData.substring(13).split("/");
      String effectData[] = effectInfo[0].split(";");

      //int AbilityId = Integer.parseInt(effectData[0]);

      //Ability ABILITY = new Ability(AbilityId);

      if (!effectInfo[1].equals("None")) {
        String seData[] = effectInfo[1].split(";");

        for (int i = 1; i < effectData.length; i++) {
          String tileInfo[] = effectData[i].split(",");
          int tileX = Integer.parseInt(tileInfo[0]);
          int tileY = Integer.parseInt(tileInfo[1]);
          int tileZ = Integer.parseInt(tileInfo[2]);
          //BlueSaga.WORLD_MAP.getTile(tileX, tileY).hitByAbility();

          Tile seTile = ScreenHandler.SCREEN_TILES.get(tileX + "," + tileY + "," + tileZ);

          if (seTile != null) {
            for (String se : seData) {
              String seIdGfx[] = se.split(",");
              int seId = Integer.parseInt(seIdGfx[0]);
              int seGfx = Integer.parseInt(seIdGfx[1]);
              String seSfx = seIdGfx[2];

              seTile.addStatusEffect(new StatusEffect(seId, seGfx));
              if (!seSfx.equals("None")) {
                Sfx.playRandomPitch(seSfx);
              }
            }
          }
        }
      }
    }

    if (serverData.startsWith("<ability_ready>")) {
      String abilityInfo = serverData.substring(15);

      int abilityId = Integer.parseInt(abilityInfo);

      Ability ABILITY = BlueSaga.playerCharacter.getAbilityById(abilityId);

      ABILITY.setReady(true);
    }

    if (serverData.startsWith("<statuseffect_add>")) {
      String addInfo[] = serverData.substring(18).split("/");

      // targetType, targetId; sId,sId2,sId3,sId4
      Creature TARGET = MapHandler.addCreatureToScreen(addInfo[0]);

      if (TARGET != null) {
        String seInfo[] = addInfo[1].split(";");

        for (String se : seInfo) {
          String seIdGfx[] = se.split(",");
          int seId = Integer.parseInt(seIdGfx[0]);
          int seGfx = Integer.parseInt(seIdGfx[1]);
          int seDuration = Integer.parseInt(seIdGfx[2]);
          String seName = seIdGfx[3];
          int seRed = Integer.parseInt(seIdGfx[4]);
          int seGreen = Integer.parseInt(seIdGfx[5]);
          int seBlue = Integer.parseInt(seIdGfx[6]);
          int seAnimationId = Integer.parseInt(seIdGfx[7]);
          String sfx = seIdGfx[8];

          StatusEffect SE = new StatusEffect(seId, seGfx);
          SE.setName(seName);
          SE.setDuration(seDuration);
          SE.setColor(new Color(seRed, seGreen, seBlue));
          SE.setAnimationId(seAnimationId);

          if (!sfx.equals("None")) {
            Sfx.playRandomPitch("statuseffects/" + sfx);
          }

          TARGET.addStatusEffect(SE);

          // Add eventual screen effect
          if (TARGET.getCreatureType() == CreatureType.Player
              && TARGET.getDBId() == BlueSaga.playerCharacter.getDBId()) {
            StatusScreenEffect screenFX = new StatusScreenEffect("Hit");
            screenFX.setEffectColor(SE.getColor());

            BlueSaga.GUI.addScreenEffect(screenFX);

            if (seGfx == 13) {
              // INK SPIT
              StatusScreenEffect inkFX = new StatusScreenEffect("Ink");
              BlueSaga.GUI.addScreenEffect(inkFX);
            } else if (seGfx == 19) {
              // SNOW
              StatusScreenEffect snowFX = new StatusScreenEffect("Snow");
              BlueSaga.GUI.addScreenEffect(snowFX);
            }
          }
        }
      }
    }

    if (serverData.startsWith("<statuseffect_remove>")) {
      // VictimType; VictimId; StatusEffectId
      String seInfo[] = serverData.substring(21).split(";");

      Creature TARGET = MapHandler.addCreatureToScreen(seInfo[0]);

      int seId = Integer.parseInt(seInfo[1]);

      TARGET.removeStatusEffect(seId);
    }

    if (serverData.startsWith("<update_tiles>")) {
      String tileData[] = serverData.substring(14).split("/");

      for (String tileInfo : tileData) {
        String tSE[] = tileInfo.split(";");
        String tCoord[] = tSE[0].split(",");
        int tileX = Integer.parseInt(tCoord[0]);
        int tileY = Integer.parseInt(tCoord[1]);
        int tileZ = Integer.parseInt(tCoord[2]);

        //Tile t = BlueSaga.SCREEN_TILES.get(tileX+","+tileY+","+tileZ);

        String seToRemove[] = tSE[1].split(",");
        for (String seInfo : seToRemove) {
          int seId = Integer.parseInt(seInfo);
          if (ScreenHandler.SCREEN_TILES.get(tileX + "," + tileY + "," + tileZ) != null) {
            ScreenHandler.SCREEN_TILES
                .get(tileX + "," + tileY + "," + tileZ)
                .removeStatusEffect(seId);
          }
        }
      }
    }

    if (serverData.startsWith("<update_bonusstats>")) {
      String bonusStats[] = serverData.substring(19).split(",");
      BlueSaga.playerCharacter.clearBonusStats();

      BlueSaga.playerCharacter.setBonusStat("STRENGTH", Integer.parseInt(bonusStats[0]));
      BlueSaga.playerCharacter.setBonusStat("INTELLIGENCE", Integer.parseInt(bonusStats[1]));
      BlueSaga.playerCharacter.setBonusStat("AGILITY", Integer.parseInt(bonusStats[2]));
      BlueSaga.playerCharacter.setBonusStat("SPEED", Integer.parseInt(bonusStats[3]));

      BlueSaga.playerCharacter.setBonusStat("CRITICAL_HIT", Integer.parseInt(bonusStats[4]));
      BlueSaga.playerCharacter.setBonusStat("EVASION", Integer.parseInt(bonusStats[5]));
      BlueSaga.playerCharacter.setBonusStat("ACCURACY", Integer.parseInt(bonusStats[6]));

      BlueSaga.playerCharacter.setBonusStat("MAX_HEALTH", Integer.parseInt(bonusStats[7]));
      BlueSaga.playerCharacter.setBonusStat("MAX_MANA", Integer.parseInt(bonusStats[8]));

      BlueSaga.playerCharacter.setBonusStat("FIRE_DEF", Integer.parseInt(bonusStats[9]));
      BlueSaga.playerCharacter.setBonusStat("COLD_DEF", Integer.parseInt(bonusStats[10]));
      BlueSaga.playerCharacter.setBonusStat("SHOCK_DEF", Integer.parseInt(bonusStats[11]));
      BlueSaga.playerCharacter.setBonusStat("CHEMS_DEF", Integer.parseInt(bonusStats[12]));
      BlueSaga.playerCharacter.setBonusStat("MIND_DEF", Integer.parseInt(bonusStats[13]));
      BlueSaga.playerCharacter.setBonusStat("ARMOR", Integer.parseInt(bonusStats[14]));

      BlueSaga.playerCharacter.MyEquipHandler.setWeaponSpeed(Integer.parseInt(bonusStats[15]));

      BlueSaga.playerCharacter.setBonusStat("HEALTH_REGAIN", Integer.parseInt(bonusStats[16]));
      BlueSaga.playerCharacter.setBonusStat("MANA_REGAIN", Integer.parseInt(bonusStats[17]));
    }
  }
}
