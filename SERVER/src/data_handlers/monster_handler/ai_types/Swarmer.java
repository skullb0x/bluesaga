package data_handlers.monster_handler.ai_types;

import java.util.Vector;

import creature.Creature.CreatureType;
import creature.Npc;
import data_handlers.monster_handler.MonsterHandler;
import map.Tile;
import network.Server;

public class Swarmer extends Melee {

  public Swarmer(Npc monster) {
    super(monster);
  }

  public void becomeAggro() {
    int rangeTiles = 2;

    for (int tileX = me.getX() - rangeTiles; tileX < me.getX() + rangeTiles; tileX++) {
      for (int tileY = me.getY() - rangeTiles; tileY < me.getY() + rangeTiles; tileY++) {
        Tile t = Server.WORLD_MAP.getTile(tileX, tileY, me.getZ());

        if (t != null
            && t.getOccupant() != null
            && t.getOccupant().getCreatureType() == CreatureType.Monster
            && t.getOccupant().getCreatureId() == me.getCreatureId()
            && me.getOriginalAggroType() != 3) {
          Npc ally = (Npc) t.getOccupant();

          if (!ally.isAggro() && !ally.isTitan()) {
            ally.setAggro(me.getAggroTarget());
          }
        }
      }
    }
  }
}
