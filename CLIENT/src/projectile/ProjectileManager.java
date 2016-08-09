package projectile;

import game.BlueSaga;
import game.ClientSettings;
import screens.ScreenHandler;

import java.util.Vector;

import sound.Sfx;

public class ProjectileManager {

  private Vector<Projectile> Projectiles = new Vector<Projectile>();

  public ProjectileManager() {
    // TODO Auto-generated constructor stub
  }

  public void addProjectile(Projectile newProjectile) {
    Projectiles.add(newProjectile);
    Sfx.playRandomPitch("abilities/" + newProjectile.getSfx());
  }

  public void draw() {
    int playerZ = 0;

    if (BlueSaga.playerCharacter != null) {
      playerZ = BlueSaga.playerCharacter.getZ();
    }

    // DRAW PROJECTILES
    for (int i = 0; i < Projectiles.size(); i++) {

      if (!Projectiles.get(i).getActive()) {
        Projectiles.remove(i);
        i--;
      } else if (Projectiles.get(i).getOriginZ() == playerZ) {
        int projX = Projectiles.get(i).getOriginX();
        int projY = Projectiles.get(i).getOriginY();

        int projMoveX = Projectiles.get(i).getMoveX();
        int projMoveY = Projectiles.get(i).getMoveY();

        int renderProjX =
            Math.round(
                projX * ClientSettings.TILE_SIZE + ScreenHandler.myCamera.getX() + projMoveX - 25);
        int renderProjY =
            Math.round(
                projY * ClientSettings.TILE_SIZE + ScreenHandler.myCamera.getY() - projMoveY - 25);

        Projectiles.get(i).draw(renderProjX, renderProjY);
      }
    }
  }
}
