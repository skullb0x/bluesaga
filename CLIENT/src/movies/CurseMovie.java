package movies;

import java.util.Vector;

import game.BlueSaga;
import game.ClientSettings;
import graphics.Font;
import graphics.ImageResource;
import graphics.Sprite;
import screens.ScreenHandler;
import screens.ScreenHandler.ScreenType;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;

import creature.PlayerCharacter;
import sound.Sfx;
import utils.LanguageUtils;

public class CurseMovie extends AbstractMovie {

  private PlayerCharacter player;
  private float playerY;

  // Boss
  private Image boss;
  Vector<Image> bossPieces;

  private int splitX = 0;

  // Curse
  private Sprite curseGlow;

  // Nimbul
  private Sprite nimbul;
  private float nimbulY;

  // Fog
  private Sprite fog1;
  private Sprite fog2;
  private float fog1X;
  private float fog2X;
  private int fogY;

  public CurseMovie(PlayerCharacter newPlayer) {
    player = newPlayer;

    boss = ImageResource.getSprite("creatures/m8").getAnimation().getImage(0);
    bossPieces = new Vector<Image>();

    for (int i = 0; i < 10; i++) {
      Image bossPiece = boss.getSubImage(0, i * 10, 100, 10);
      bossPieces.add(bossPiece);
    }

    curseGlow = ImageResource.getSprite("effects/fx_special_glow");
    nimbul = ImageResource.getSprite("story/nimbul");

    fog1 = ImageResource.getSprite("effects/fog1");
    fog2 = ImageResource.getSprite("effects/fog2");

    setDuration(2400);
  }

  public void play() {
    super.play();
    BlueSaga.BG_MUSIC.stop();
    splitX = 0;
    playerY = 320;

    fog1X = 700;
    fog2X = 0;
    fogY = 370;
    nimbulY = getY() + 50;
  }

  public void draw(Graphics g) {
    update();

    if (isActive()) {
      super.draw(g);
      g.setWorldClip(getX(), getY(), getWidth(), getHeight());

      // Draw red background
      g.setColor(new Color(255, 95, 95));
      g.fillRect(getX(), getY(), getWidth(), getHeight());

      // Nimbul and fog
      if (getTimeItr() > 640) {
        if (getTimeItr() == 641) {
          Sfx.play("story/evil_laugh");
        }

        // Nimbul face
        int fade = (getTimeItr() - 640);
        if (fade > 255) {
          fade = 255;
        }
        nimbul.draw(getX(), Math.round(nimbulY), new Color(255, 255, 255, fade));
        if (nimbulY > getY()) {
          nimbulY -= 0.5f;
        }

        fog1X -= 0.25;
        fog2X -= 0.5;
        if (fog1X < -500) {
          fog1X = 900;
        }
        if (fog2X < -500) {
          fog2X = 1000;
        }
        fog1.draw(Math.round(fog1X), fogY, new Color(204, 108, 236, fade - 100));
        fog2.draw(Math.round(fog2X), fogY, new Color(204, 108, 236, fade - 100));
      }

      // Boss death
      if (getTimeItr() < 200) {

        // Draw boss split
        int pieceY = 250;
        int splitDir = 0;
        int pieceFade = 255 - splitX * 4;
        for (Image bossPiece : bossPieces) {
          if (splitDir % 2 == 0) {
            bossPiece.draw(462 + splitX, pieceY, new Color(255, 255, 255, pieceFade));
          } else {
            bossPiece.draw(462 - splitX, pieceY, new Color(255, 255, 255, pieceFade));
          }

          pieceY += 10;
          splitDir++;
        }

        if (getTimeItr() == 1) {
          Sfx.play("story/ghoulrat_strike");
        }

        if (getTimeItr() == 100) {
          Sfx.play("story/ghoulrat_spread");
        }
        // Draw white flash
        if (getTimeItr() < 25) {
          g.setColor(new Color(255, 255, 255, getTimeItr() * 10));
          g.fillRect(getX(), getY(), getWidth(), getHeight());
        } else if (getTimeItr() < 50) {
          g.setColor(new Color(255, 255, 255, 510 - getTimeItr() * 10));
          g.fillRect(getX(), getY(), getWidth(), getHeight());
        } else if (getTimeItr() > 100 && getTimeItr() < 200) {
          splitX++;
        }
        if (getTimeItr() == 140) {
          BlueSaga.BG_MUSIC.changeSong("ghosts", "dungeon");
        }
      } else {

        // Player curse
        player.draw(g, 512 + 5, Math.round(playerY));

        if (getTimeItr() < 300) {
          // Fade in
          int playerFade = 455 - (getTimeItr() - 200) * 4;
          if (playerFade > 0) {
            g.setColor(new Color(255, 95, 95, playerFade));
            g.fillRect(getX(), getY(), getWidth(), getHeight());
          }
        } else if (getTimeItr() == 460) {
          // Shake
          player.addCreatureAnimation(2, 40);
          Sfx.play("story/shipcrash");
        } else if (getTimeItr() < 560) {
          // Curse
          int curseFade = 0;

          if (getTimeItr() < 510) {
            curseFade = (getTimeItr() - 460) * 4;
          } else {
            curseFade = 200 - (getTimeItr() - 510) * 4;
          }

          if (getTimeItr() == 510) {
            Sfx.play("story/curse");
            player.setAnimationColor(new Color(204, 108, 236));
          }

          if (curseFade > 0) {
            curseGlow.draw(
                512 - 48, Math.round(playerY - 50), 100, 100, new Color(204, 108, 236, curseFade));
          }
        } else if (getTimeItr() < 860) {
          playerY += 0.3;
        }
      }

      g.clearWorldClip();

      int textY = 500;

      // Text
      if (getTimeItr() > 700 && getTimeItr() < 1100) {
        g.setColor(new Color(255, 255, 255));
        g.setFont(Font.size30);

        String text = LanguageUtils.getString("movies.movie2.text1");
        int textX = 512 - Font.size30.getWidth(text) / 2;

        g.drawString(text, textX, textY);
      } else if (getTimeItr() >= 1180 && getTimeItr() < 1560) {
        g.setColor(new Color(255, 255, 255));
        g.setFont(Font.size30);

        String text = LanguageUtils.getString("movies.movie2.text2");
        int textX = 512 - Font.size30.getWidth(text) / 2;

        g.drawString(text, textX, textY);
      } else if (getTimeItr() >= 1600) {
        g.setColor(new Color(255, 255, 255));
        g.setFont(Font.size30);

        String text = LanguageUtils.getString("movies.movie2.text3");
        int textX = 512 - Font.size30.getWidth(text) / 2;

        g.drawString(text, textX, textY);

        if (getTimeItr() == 1960) {
          endMovie();
        }
      }
      if (fadeAlpha > 0) {
        if (skipped) {
          fadeAlpha += 4;
        } else {
          fadeAlpha++;
        }
        g.setColor(new Color(0, 0, 0, fadeAlpha));
        g.fillRect(0, 0, ClientSettings.SCREEN_WIDTH, ClientSettings.SCREEN_HEIGHT);
      }

    } else {
      ScreenHandler.setActiveScreen(ScreenType.WORLD);
    }
  }

  public void update() {
    super.update();
    if (!isActive()) {
      player.setAnimationColor(new Color(255, 255, 255));
      BlueSaga.BG_MUSIC.stop();
    }
  }
}
