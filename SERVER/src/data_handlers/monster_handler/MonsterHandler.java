package data_handlers.monster_handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import map.Tile;
import map.Trigger;
import network.Client;
import network.Server;
import utils.ServerGameInfo;
import utils.MathUtils;
import utils.RandomUtils;
import creature.Creature;
import creature.Npc;
import creature.PlayerCharacter;
import creature.Creature.CreatureType;
import data_handlers.DataHandlers;
import data_handlers.Handler;
import data_handlers.MapHandler;
import data_handlers.Message;
import data_handlers.TrapHandler;
import data_handlers.ability_handler.Ability;
import data_handlers.ability_handler.AbilityHandler;
import data_handlers.ability_handler.StatusEffect;
import data_handlers.ability_handler.StatusEffectHandler;
import data_handlers.battle_handler.BattleHandler;
import data_handlers.card_handler.CardHandler;
import data_handlers.item_handler.CoinConverter;
import data_handlers.item_handler.ContainerHandler;
import data_handlers.item_handler.Item;
import data_handlers.item_handler.ItemHandler;
import data_handlers.monster_handler.ai_types.Melee;
import data_handlers.monster_handler.ai_types.Ranged;
import data_handlers.monster_handler.ai_types.Shy;

public class MonsterHandler extends Handler {

  //PATHFINDING

  public static Vector<Integer> movingMonsterTypes = new Vector<Integer>();

  public static boolean diagonalWalk = true;

  public static Vector<Npc> aggroMonsters = new Vector<Npc>();

  public static void init() {
    movingMonsterTypes.clear();
    movingMonsterTypes.add(1);
    movingMonsterTypes.add(2);
    movingMonsterTypes.add(4);

    DataHandlers.register("mobinfo", m -> handleMobInfo(m));
  }

  public static void changeMonsterSleepState(Creature c, boolean sleepOrNot) {
    c.setResting(sleepOrNot);
    for (Entry<Integer, Client> entry3 : Server.clients.entrySet()) {
      Client s = entry3.getValue();
      if (s.Ready) {
        if (isVisibleForPlayer(s.playerCharacter, c.getX(), c.getY(), c.getZ())) {
          addOutGoingMessage(s, "otherrest", c.getSmallData() + ";" + c.isResting());
        }
      }
    }
  }

  public static void update(long tick) {
    if (tick % 2 == 0) {
      // Every 100 ms
      updateMoveIterators();
      moveAggroMonsters();
    }
    if (tick % 4 == 0) {
      // Every 200ms
      moveNonAggroMonsters();
    }
    if (tick % 40 == 0) {
      // Every 2000ms
      checkMonsterRespawn();
    }
  }

  private static void updateMoveIterators() {
    for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
      Client s = entry.getValue();

      if (s.Ready) {
        s.playerCharacter.checkMoveTimer();
      }
    }

    for (int z : Server.WORLD_MAP.zLevels) {
      if (Server.WORLD_MAP.playersByZ.get(z).size() > 0) {
        for (Iterator<Npc> iter = Server.WORLD_MAP.monstersByZ.get(z).iterator();
            iter.hasNext();
            ) {
          Npc monster = iter.next();
          if (!monster.isDead()) {
            monster.checkMoveTimer();
          }
        }
      }
    }
  }

  public static void handleMobInfo(Message m) {
    Client client = m.client;
    StringBuilder mobinfo = new StringBuilder(5500);
    ResultSet mobdata =
        Server.gameDB.askDB(
            "select Id, Name, SizeW, SizeH, HeadX, HeadY, WeaponX, WeaponY, OffHandX, OffHandY, AmuletX, AmuletY, ArtifactX, ArtifactY, MouthFeatureX, MouthFeatureY, AccessoriesX, AccessoriesY, SkinFeatureX, SkinFeatureY from creature");

    try {
      while (mobdata.next()) {
        mobinfo
            .append(mobdata.getInt("Id"))
            .append(',')
            .append(mobdata.getString("Name"))
            .append(',')
            .append(mobdata.getInt("SizeW"))
            .append(',')
            .append(mobdata.getInt("SizeH"))
            .append(',')
            .append(mobdata.getInt("HeadX"))
            .append(',')
            .append(mobdata.getInt("HeadY"))
            .append(',')
            .append(mobdata.getInt("WeaponX"))
            .append(',')
            .append(mobdata.getInt("WeaponY"))
            .append(',')
            .append(mobdata.getInt("OffHandX"))
            .append(',')
            .append(mobdata.getInt("OffHandY"))
            .append(',')
            .append(mobdata.getInt("AmuletX"))
            .append(',')
            .append(mobdata.getInt("AmuletY"))
            .append(',')
            .append(mobdata.getInt("ArtifactX"))
            .append(',')
            .append(mobdata.getInt("ArtifactY"))
            .append(',')
            .append(mobdata.getInt("MouthFeatureX"))
            .append(',')
            .append(mobdata.getInt("MouthFeatureY"))
            .append(',')
            .append(mobdata.getInt("AccessoriesX"))
            .append(',')
            .append(mobdata.getInt("AccessoriesY"))
            .append(',')
            .append(mobdata.getInt("SkinFeatureX"))
            .append(',')
            .append(mobdata.getInt("SkinFeatureY"))
            .append(';');
      }
      mobdata.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    addOutGoingMessage(client, "mobinfo", mobinfo.toString());
  }

  public static void checkMonsterRespawn() {

    String monsterRespawnInfo = Server.WORLD_MAP.checkRespawns();

    if (!monsterRespawnInfo.equals("none")) {

      String monstersToRespawn[] = monsterRespawnInfo.split(",");

      for (String mId : monstersToRespawn) {
        int monsterId = Integer.parseInt(mId);
        Npc monster = Server.WORLD_MAP.getMonster(monsterId);

        MapHandler.checkIfDoorCloses(monsterId);

        for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
          Client s = entry.getValue();

          if (s.Ready) {
            if (isVisibleForPlayer(
                s.playerCharacter, monster.getX(), monster.getY(), monster.getZ())) {
              addOutGoingMessage(s, "respawnmonster", monster.getSmallData());
            }
          }
        }
      }
    }
  }

  public static void moveNonAggroMonsters() {

    // MOVE NON AGGRO MONSTERS
    Vector<Npc> monsterMoved = Server.WORLD_MAP.moveNonAggroMonsters();

    if (monsterMoved.size() > 0) {

      for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
        Client s = entry.getValue();

        if (s.Ready) {
          // CHECK IF PLAYERS IS IN MONSTER AGGRO RANGE
          if (s.playerCharacter.getAdminLevel() < 3) {
            alertNearMonsters(
                s.playerCharacter,
                s.playerCharacter.getX(),
                s.playerCharacter.getY(),
                s.playerCharacter.getZ(),
                false);
          }

          StringBuilder monsterData = new StringBuilder(1000);

          for (Iterator<Npc> iter2 = monsterMoved.iterator(); iter2.hasNext(); ) {
            Npc m = iter2.next();
            if (isVisibleForPlayer(s.playerCharacter, m.getX(), m.getY(), m.getZ())) {
              monsterData
                  .append("Monster,")
                  .append(m.getDBId())
                  .append(',')
                  .append(m.getCreatureId())
                  .append(',')
                  .append(m.getOldX())
                  .append(',')
                  .append(m.getOldY())
                  .append(',')
                  .append(m.getOldZ())
                  .append(',')
                  .append(m.getRotation())
                  .append('/')
                  .append(m.getX())
                  .append(',')
                  .append(m.getY())
                  .append(',')
                  .append(m.getZ())
                  .append(',')
                  .append(m.getStat("SPEED"))
                  .append(',')
                  .append(m.getGotoRotation())
                  .append(';');
            }
          }
          if (monsterData.length() > 0) {
            addOutGoingMessage(s, "creaturepos", monsterData.toString());
          }
        }
      }
    }
  }

  public static void moveAggroMonsters() {

    Vector<Npc> monsterMoved = new Vector<Npc>();
    Vector<Npc> monsterLostAggro = new Vector<Npc>();

    for (Iterator<Npc> iter = aggroMonsters.iterator(); iter.hasNext(); ) {
      Npc aggroMonster = iter.next();

      if (aggroMonster.isDead() || !aggroMonster.isAggro()) {
        monsterLostAggro.add(aggroMonster);
      } else {
        // Stop resting if resting
        if (aggroMonster.isResting()) {
          MonsterHandler.changeMonsterSleepState(aggroMonster, false);
        }

        // IF MONSTER ISN'T DEAD AND IS AGGRO
        if (aggroMonster.isReadyToMove()) {

          if (aggroMonster.getZ() == aggroMonster.getAggroTarget().getZ()) {
            aggroMonster.doAggroBehaviour(monsterMoved);
          } else {
            monsterLostAggro.add(aggroMonster);
          }
        }
      }
    }

    // Remove non-aggro monsters from aggroMonsters
    for (Npc m : monsterLostAggro) {
      m.setAggro(null);
    }

    // LOOP THROUGH THREADS AND SEND MONSTER DATA
    for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
      Client s = entry.getValue();

      if (s.Ready) {
        StringBuilder monsterInfoToSend = new StringBuilder(1000);
        StringBuilder monsterAggroToSend = new StringBuilder(1000);

        for (Npc m : monsterMoved) {
          if (isVisibleForPlayer(s.playerCharacter, m.getX(), m.getY(), m.getZ())) {
            monsterInfoToSend
                .append("Monster,")
                .append(m.getDBId())
                .append(',')
                .append(m.getCreatureId())
                .append(',')
                .append(m.getOldX())
                .append(',')
                .append(m.getOldY())
                .append(',')
                .append(m.getOldZ())
                .append(',')
                .append(m.getRotation())
                .append('/')
                .append(m.getX())
                .append(',')
                .append(m.getY())
                .append(',')
                .append(m.getZ())
                .append(',')
                .append(m.getStat("SPEED"))
                .append(',')
                .append(m.getGotoRotation())
                .append(';');
          }
        }
        for (Npc m : monsterLostAggro) {
          int aggroStatus = 0;

          monsterAggroToSend
              .append(m.getSmallData())
              .append('/')
              .append(aggroStatus)
              .append(',')
              .append(m.getAggroType())
              .append(',')
              .append(m.getHealthStatus())
              .append(';');
        }

        if (monsterInfoToSend.length() > 0) {
          addOutGoingMessage(s, "creaturepos", monsterInfoToSend.toString());
        }

        if (monsterAggroToSend.length() > 0) {
          addOutGoingMessage(s, "aggroinfo", monsterAggroToSend.toString());
        }
      }
    }
  }

  public static boolean monsterUseRandomAbility(Creature monster, Creature target) {
    boolean monsterUseAbility = false;

    // CHOOSE RANDOM ABILITY
    int nrAbilities = monster.getNrAbilities();

    if (nrAbilities > 0) {
      // CHECK IF CAN USE ABILITY

      int chosenAbility = RandomUtils.getInt(0, nrAbilities - 1);

      Ability ABILITY = monster.getAbility(chosenAbility);

      int chanceOfUsingAbility = RandomUtils.getInt(0, 100);

      // RANDOM CHANCE OF USING ABILITY
      if (chanceOfUsingAbility < 50) {

        // Monster MONSTER, int monsterIndex, Ability ABILITY, int goalX, int goalY
        if (target != null) {
          if (AbilityHandler.monsterUseAbility(monster, ABILITY, target)) {
            monster.startAttackTimer();
            monster.restartMoveTimer();
            monsterUseAbility = true;
          }
        }
      }
    }
    return monsterUseAbility;
  }

  public synchronized static void spawnMonster(Npc m, int posX, int posY, int posZ) {
    Server.WORLD_MAP.addMonsterSpawn(m, posX, posY, posZ);
  }

  public static void monsterDropLoot(Npc TARGET) {
    // CHECK IF MONSTER HAS DEATH ABILITY
    if (TARGET.getDeathAbilityId() > 0) {
      Ability deathAbility = new Ability(ServerGameInfo.abilityDef.get(TARGET.getDeathAbilityId()));
      TARGET.useAbility(deathAbility);
      AbilityHandler.abilityEffect(deathAbility, TARGET.getX(), TARGET.getY(), TARGET.getZ());
    }

    // MONSTER DROP LOOT
    Vector<Item> droppedLoot = ItemHandler.dropLoot(TARGET);
    int droppedMoney = ItemHandler.dropMoney(TARGET);

    Item dropCard = null;
    if (TARGET.getSpawnZ() <= -200) {
      dropCard = CardHandler.monsterDropCard(TARGET.getId());
    }

    if (droppedLoot.size() > 0 || droppedMoney > 0 || dropCard != null) {

      int tileX = TARGET.getX();
      int tileY = TARGET.getY();
      int tileZ = TARGET.getZ();

      for (Item loot : droppedLoot) {
        ContainerHandler.addItemToContainer(loot, tileX, tileY, tileZ);
      }

      if (dropCard != null) {
        ContainerHandler.addItemToContainer(dropCard, tileX, tileY, tileZ);
      }

      if (droppedMoney > 0) {
        CoinConverter cc = new CoinConverter(droppedMoney);
        if (cc.getGold() > 0) {
          Item GoldItem = new Item(ServerGameInfo.itemDef.get(34));
          GoldItem.setStacked(cc.getGold());
          ContainerHandler.addItemToContainer(GoldItem, tileX, tileY, tileZ);
        }
        if (cc.getSilver() > 0) {
          Item SilverItem = new Item(ServerGameInfo.itemDef.get(35));
          SilverItem.setStacked(cc.getSilver());
          ContainerHandler.addItemToContainer(SilverItem, tileX, tileY, tileZ);
        }
        if (cc.getCopper() > 0) {
          Item CopperItem = new Item(ServerGameInfo.itemDef.get(36));
          CopperItem.setStacked(cc.getCopper());
          ContainerHandler.addItemToContainer(CopperItem, tileX, tileY, tileZ);
        }
      }

      Server.WORLD_MAP.getTile(tileX, tileY, tileZ).setObjectId("container/smallbag");

      // SEND LOOT INFO TO ALL CLIENTS IN AREA
      for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
        Client s = entry.getValue();

        if (s.Ready && isVisibleForPlayer(s.playerCharacter, tileX, tileY, tileZ)) {
          addOutGoingMessage(
              s, "droploot", "container/smallbag," + tileX + ',' + tileY + ',' + tileZ);
        }
      }
    }

    MapHandler.checkIfDoorOpens(TARGET.getDBId());
  }

  public static void checkMonsterMoveConsequences(Npc aggroMonster) {
    Tile goalTile =
        Server.WORLD_MAP.getTile(aggroMonster.getX(), aggroMonster.getY(), aggroMonster.getZ());

    // CHECK IF TILE HAS STATUS EFFECT
    if (goalTile.getStatusEffects().size() > 0) {
      for (StatusEffect se : goalTile.getStatusEffects()) {
        boolean selfInflict = true;
        if (se.getCaster() != null) {
          if (se.getRepeatDamage() > 0) {
            if (se.getCaster().getCreatureType() == CreatureType.Monster
                && se.getCaster().getDBId() == aggroMonster.getDBId()) {
              selfInflict = false;
            }
          }
        }
        if (selfInflict) {
          StatusEffectHandler.addStatusEffect(aggroMonster, se);
        }
      }
    }

    // CHECK IF TILE HAS TRIGGER
    if (goalTile.getTrigger() != null) {
      Trigger T =
          Server.WORLD_MAP
              .getTile(aggroMonster.getX(), aggroMonster.getY(), aggroMonster.getZ())
              .getTrigger();

      T.setTriggered(true);

      // IF TRIGGERS TRAP, TRAP EFFECT
      if (T.getTrapId() > 0) {
        TrapHandler.triggerTrap(
            T.getTrapId(), aggroMonster.getX(), aggroMonster.getY(), aggroMonster.getZ());
      }
    }
  }

  public static int alertNearMonsters(
      Creature attacker, int hitX, int hitY, int hitZ, boolean changeTarget) {
    int AGGRO_RANGE = 3;

    Vector<Npc> aggroMonsters = new Vector<Npc>();

    int rangeTiles = 6;

    for (int tileX = hitX - rangeTiles; tileX < hitX + rangeTiles; tileX++) {
      for (int tileY = hitY - rangeTiles; tileY < hitY + rangeTiles; tileY++) {
        Tile t = Server.WORLD_MAP.getTile(tileX, tileY, hitZ);

        if (t != null) {
          if (t.getOccupant() != null) {
            if (t.getOccupant().getCreatureType() == CreatureType.Monster) {
              Npc m = (Npc) t.getOccupant();

              if (!m.isDead()) {

                // Guardian aggro
                if (m.getOriginalAggroType() == 5 && !m.isAggro()) {
                  // Check if attacker is a player with crusader status
                  if (attacker.getCreatureType() == CreatureType.Player) {
                    PlayerCharacter playerAttacker = (PlayerCharacter) attacker;
                    if (playerAttacker.getPkMarker() > 0) {

                      // CHECK IF WITHIN RANGE
                      double distToMob =
                          Math.sqrt(Math.pow(m.getX() - hitX, 2) + Math.pow(m.getY() - hitY, 2));

                      AGGRO_RANGE = 40;
                      if (distToMob <= AGGRO_RANGE) {
                        m.setAggro(attacker);
                        aggroMonsters.add(m);
                      }
                    }
                  }
                } else if (!m.isAggro() || changeTarget) {
                  /**
                   * To become aggro monster need to be of aggrotype:
                   * 2: moving aggressive monster
                   * 4: moving aggressive monster, no attackspeed
                   * Epic and hit: epic monster that is hit by attack
                   * Can't become aggro if a training target
                   */
                  boolean turnAggro = false;

                  if ((m.getAggroType() == 2 || m.getAggroType() == 4)
                      && !m.isTitan()
                      && m.getFamilyId() != 8) {
                    turnAggro = true;
                  } else if (m.isTitan()
                      && m.getX() == hitX
                      && m.getY() == hitY
                      && m.getZ() == hitZ) {
                    turnAggro = true;
                  }

                  if (turnAggro) {
                    // Check if monster can attack, if not do not chase
                    boolean chaseOk =
                        BattleHandler.checkAttackOk(m.getX(), m.getY(), m.getZ(), hitX, hitY, hitZ);

                    if (chaseOk) {
                      if (attacker.getCreatureType() == CreatureType.Monster
                          && attacker.getDBId() == m.getDBId()) {
                        // NOT SET ITSELF TO AGGRO TARGET
                      } else {
                        turnAggro = false;

                        // CHECK IF WITHIN RANGE
                        double distToMob =
                            Math.sqrt(Math.pow(m.getX() - hitX, 2) + Math.pow(m.getY() - hitY, 2));

                        AGGRO_RANGE = m.getAggroRange();

                        if (changeTarget) {
                          turnAggro = true;
                        } else if (distToMob <= AGGRO_RANGE) {

                          // Check immediate vicinity, hearing
                          if (distToMob < 2 || changeTarget) {
                            turnAggro = true;
                          } else {
                            // Check field of vision

                            // Calculate angle between monsters direction and player position
                            int dX = m.getX() - hitX;
                            int dY = m.getY() - hitY;

                            float dA = MathUtils.angleBetween(dX, dY);

                            // Half width of field of vision
                            float visionWidth = 60.0f;

                            if (Math.abs(m.getRotation() - dA) <= visionWidth) {
                              turnAggro = true;
                            }
                          }
                        }

                        if (turnAggro) {
                          m.setAggro(attacker);
                          aggroMonsters.add(m);
                        }
                      }
                    }
                  }
                } else if (m.isAggro()
                    && m.getAggroTarget().getCreatureType() == CreatureType.Monster) {
                  if (attacker.getCreatureType() == CreatureType.Player) {
                    m.setAggro(attacker);
                  }
                }
              }
            }
          }
        }
      }
    }

    if (aggroMonsters.size() > 0) {
      // SEND AGGRO INFO TO PLAYERS IN THE AREA

      for (Entry<Integer, Client> entry : Server.clients.entrySet()) {
        Client c = entry.getValue();

        if (c.Ready) {
          StringBuilder aggroData = new StringBuilder(1000);

          for (Iterator<Npc> iter3 = aggroMonsters.iterator(); iter3.hasNext(); ) {
            Npc m = iter3.next();
            if (Handler.isVisibleForPlayer(c.playerCharacter, m.getX(), m.getY(), m.getZ())) {
              int aggroStatus = 1;
              aggroData
                  .append(m.getSmallData())
                  .append('/')
                  .append(aggroStatus)
                  .append(',')
                  .append(m.getAggroType())
                  .append(',')
                  .append(m.getHealthStatus())
                  .append(';');
            }
          }
          if (aggroData.length() > 0) {
            Handler.addOutGoingMessage(c, "aggroinfo", aggroData.toString());
          }
        }
      }
    }

    return aggroMonsters.size();
  }
}
