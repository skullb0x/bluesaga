package graphics.screeneffects;

import org.newdawn.slick.Color;
import org.newdawn.slick.Image;

import utils.RandomUtils;
import game.ClientSettings;
import graphics.ImageResource;
import graphics.Sprite;

public class InkSpot {

  private float X;
  private float Y;

  private int Size;
  private int Alpha;

  private float Speed;

  private int itr = 0;

  private Sprite inkGFX;

  public InkSpot() {
    X = RandomUtils.getInt(-300, 923);
    Y = RandomUtils.getInt(-300, 540);

    Size = RandomUtils.getInt(200, 1000);
    Speed = RandomUtils.getFloat(0.2f, 1.2f);
    Alpha = 0;

    //Rotation = BlueSaga.randomGenerator.nextFloat();

    //inkGFX = new Sprite("images/effects/screenfx_ink");

    //inkGFX.getImage().setRotation(Rotation);
    inkGFX = ImageResource.getSprite("effects/screenfx_ink");

    inkGFX.getImage().setFilter(Image.FILTER_NEAREST);
  }

  public void draw() {
    Y += Speed;

    itr++;

    if (itr < 1000 && Alpha < 255) {
      Alpha += 2;
    } else if (itr >= 200) {
      Alpha -= 2;
    }

    if (Y - Size < ClientSettings.SCREEN_HEIGHT) {
      inkGFX.draw((int) X, (int) Y, Size, Size, new Color(0, 0, 0, Alpha));
    }
  }
}
