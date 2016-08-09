package map;

import org.newdawn.slick.Color;

import utils.RandomUtils;
import graphics.ImageResource;

public class FogCloud {

  int Id;
  float X;
  float Y;
  float Speed;
  int opacity;
  int gotoOpacity;

  int blue;
  int cameraStartX;
  int cameraStartY;

  public FogCloud(int newId, int cameraX, int cameraY) {
    Id = newId;

    cameraStartX = cameraX;
    cameraStartY = cameraY;

    X = RandomUtils.getInt(-400, 1024);
    Y = RandomUtils.getInt(-200, 640);
    Speed = RandomUtils.getFloat(0.5f, 2.5f);
    opacity = 0;
    gotoOpacity = RandomUtils.getInt(50, 150);
  }

  public void draw(int cameraX, int cameraY, Color aColor) {
    X -= (cameraStartX - cameraX) + Speed;
    Y -= (cameraStartY - cameraY);
    cameraStartX = cameraX;
    cameraStartY = cameraY;

    if (opacity < gotoOpacity) {
      opacity++;
    } else if (opacity > gotoOpacity) {
      opacity--;
    }

    if (X < -1024 || Y < -1000 || Y > 1000 || X > 2048) {
      if (gotoOpacity > 0) {
        X = RandomUtils.getInt(1024, 1824);
        Y = RandomUtils.getInt(-200, 640);
        Speed = RandomUtils.getFloat(0.5f, 2.5f);
        opacity = RandomUtils.getInt(50, 150);
        gotoOpacity = opacity;
      }
    }

    ImageResource.getSprite("effects/fog" + Id)
        .draw(
            Math.round(X),
            Math.round(Y),
            new Color(aColor.getRed(), aColor.getGreen(), aColor.getBlue(), opacity));
  }

  public void dissappear() {
    gotoOpacity = 0;
  }

  public void appear() {
    gotoOpacity = RandomUtils.getInt(50, 150);
  }
}
