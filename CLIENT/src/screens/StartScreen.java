package screens;

import org.newdawn.slick.Color;

import utils.RandomUtils;
import graphics.ImageResource;
import screens.ScreenHandler.ScreenType;

public class StartScreen {

  private static int posY = 0;
  private static float cloud1_x = RandomUtils.getInt(0, 1500);
  private static float cloud2_x = RandomUtils.getInt(0, 1500);

  private static int cloud1_y = RandomUtils.getInt(50, 250);
  private static int cloud2_y = RandomUtils.getInt(50, 250);

  private static float cloud1_speed = RandomUtils.getFloat(0.2f, 1.2f);
  private static float cloud2_speed = RandomUtils.getFloat(0.2f, 1.2f);

  private static float bgClouds1X = -512.0f;
  private static float bgClouds2X = -512.0f;

  public static void draw() {
    ImageResource.getSprite("startscreen/bg").draw(0, posY - 100);

    ImageResource.getSprite("startscreen/bg_clouds2").draw(Math.round(bgClouds2X), posY + 390);
    ImageResource.getSprite("startscreen/bg_clouds2")
        .getImage()
        .getFlippedCopy(true, false)
        .draw(Math.round(bgClouds2X + 512), posY + 390);
    ImageResource.getSprite("startscreen/bg_clouds2")
        .draw(Math.round(bgClouds2X + 1024), posY + 390);
    ImageResource.getSprite("startscreen/bg_clouds2")
        .getImage()
        .getFlippedCopy(true, false)
        .draw(Math.round(bgClouds2X + 1536), posY + 390);

    ImageResource.getSprite("startscreen/bg_clouds1").draw(Math.round(bgClouds1X), posY + 405);
    ImageResource.getSprite("startscreen/bg_clouds1")
        .getImage()
        .getFlippedCopy(true, false)
        .draw(Math.round(bgClouds1X + 512), posY + 405);
    ImageResource.getSprite("startscreen/bg_clouds1")
        .draw(Math.round(bgClouds1X + 1024), posY + 405);
    ImageResource.getSprite("startscreen/bg_clouds1")
        .getImage()
        .getFlippedCopy(true, false)
        .draw(Math.round(bgClouds1X + 1536), posY + 405);

    // mirror clouds
    ImageResource.getSprite("startscreen/bg_clouds1")
        .getImage()
        .getFlippedCopy(false, true)
        .draw(bgClouds1X, posY + 450, new Color(255, 255, 255, 50));
    ImageResource.getSprite("startscreen/bg_clouds1")
        .getImage()
        .getFlippedCopy(true, true)
        .draw(bgClouds1X + 512, posY + 450, new Color(255, 255, 255, 50));
    ImageResource.getSprite("startscreen/bg_clouds1")
        .getImage()
        .getFlippedCopy(false, true)
        .draw(bgClouds1X + 1024, posY + 450, new Color(255, 255, 255, 50));
    ImageResource.getSprite("startscreen/bg_clouds1")
        .getImage()
        .getFlippedCopy(true, true)
        .draw(bgClouds1X + 1536, posY + 450, new Color(255, 255, 255, 50));

    ImageResource.getSprite("startscreen/island1").draw(460, posY + 405);

    if (ScreenHandler.getActiveScreen() == ScreenType.CHARACTER_SELECT) {
      if (posY < 20) {
        posY++;
      } else if (posY > 20) {
        posY--;
      }
    } else if (ScreenHandler.getActiveScreen() == ScreenType.CHARACTER_CREATE) {
      if (posY < 80) {
        posY++;
      }
    } else if (ScreenHandler.getActiveScreen() == ScreenType.LOGIN && posY > 0) {
      posY--;
    }

    if (bgClouds1X < 0) {
      bgClouds1X += 0.3f;
    } else {
      bgClouds1X = -1024.0f;
    }

    if (bgClouds2X < 0) {
      bgClouds2X += 0.2f;
    } else {
      bgClouds2X = -1024.0f;
    }

    if (cloud1_x > -300) {
      cloud1_x -= cloud1_speed;
    } else {
      resetCloud1();
    }

    if (cloud2_x > -300) {
      cloud2_x -= cloud2_speed;
    } else {
      resetCloud2();
    }

    int horizonY = 590 + posY;
    //
    ImageResource.getSprite("startscreen/cloud1").draw((int) cloud1_x, cloud1_y + posY);
    float cloud1_mirror_y = horizonY + (100 - (((float) cloud1_y - 80) / 100) * 50);
    ImageResource.getSprite("startscreen/cloud1")
        .getImage()
        .draw((int) cloud1_x, (int) cloud1_mirror_y, 140, -70, new Color(255, 255, 255, 20));

    ImageResource.getSprite("startscreen/cloud2").draw((int) cloud2_x, cloud2_y + posY);
    float cloud2_mirror_y = horizonY + (50 - (((float) cloud2_y - 80) / 100) * 50);
    ImageResource.getSprite("startscreen/cloud2")
        .getImage()
        .draw((int) cloud2_x, (int) cloud2_mirror_y, 155, -60, new Color(255, 255, 255, 20));
  }

  private static void resetCloud1() {
    cloud1_x = RandomUtils.getInt(1050, 2050);
    cloud1_y = RandomUtils.getInt(80, 180);
    cloud1_speed = RandomUtils.getFloat(0.2f, 1.2f);
  }

  private static void resetCloud2() {
    cloud2_x = RandomUtils.getInt(1050, 2050);
    cloud2_y = RandomUtils.getInt(220, 320);
    cloud2_speed = RandomUtils.getFloat(0.2f, 1.2f);
  }
}
