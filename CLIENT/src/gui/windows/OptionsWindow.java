package gui.windows;

import game.BlueSaga;
import game.ClientSettings;
import graphics.BlueSagaColors;
import graphics.ImageResource;
import gui.Button;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;

import utils.LanguageUtils;

public class OptionsWindow extends Window {

  Button SoundUpButton;
  Button SoundDownButton;

  Button MusicUpButton;
  Button MusicDownButton;

  Button FullScreenButton;

  public OptionsWindow(int x, int y, int width, int height) {
    super("OptionsW", x, y, width, height, true);
    // TODO Auto-generated constructor stub

    SoundDownButton = new Button("-", 100, 60, 35, 35, this);
    SoundUpButton = new Button("+", 200, 60, 35, 35, this);

    MusicDownButton = new Button("-", 100, 100, 35, 35, this);
    MusicUpButton = new Button("+", 200, 100, 35, 35, this);

    FullScreenButton = new Button("TOGGLE FULLSCREEN", 25, 160, 200, 35, this);
    setMovable(false);
  }

  @Override
  public void draw(GameContainer app, Graphics g, int mouseX, int mouseY) {
    if (isVisible()) {
      super.draw(app, g, mouseX, mouseY);
      if (isFullyOpened()) {
        g.setColor(BlueSagaColors.WHITE);
        g.drawString(LanguageUtils.getString("ui.options.sfx"), X + 20, Y + 70);
        g.setColor(BlueSagaColors.WHITE);
        g.drawString(LanguageUtils.getString("ui.options.music"), X + 20, Y + 110);

        int sV = (int) Math.ceil(ClientSettings.soundVolume * 10);
        if (sV == 0) {
          g.drawString(LanguageUtils.getString("ui.options.off"), X + 155, Y + 70);
        } else {
          g.drawString("" + sV, X + 160, Y + 70);
        }

        int mV = (int) Math.ceil(ClientSettings.musicVolume * 10);
        if (mV == 0) {
          g.drawString(LanguageUtils.getString("ui.options.off"), X + 155, Y + 110);
        } else {
          g.drawString("" + mV, X + 160, Y + 110);
        }

        SoundUpButton.draw(g, mouseX, mouseY);
        SoundDownButton.draw(g, mouseX, mouseY);
        MusicUpButton.draw(g, mouseX, mouseY);
        MusicDownButton.draw(g, mouseX, mouseY);

        //FullScreenButton.draw(g, mouseX, mouseY);

        ImageResource.getSprite("gui/menu/options_label").draw(X + 20, Y + 20);
      }
    }
  }

  @Override
  public void leftMouseClick(Input INPUT) {
    super.leftMouseClick(INPUT);

    int mouseX = INPUT.getAbsoluteMouseX();
    int mouseY = INPUT.getAbsoluteMouseY();

    if (isVisible()) {
      boolean updatedSound = false;
      boolean updatedMusic = false;

      if (SoundUpButton.isClicked(mouseX, mouseY)) {
        if (ClientSettings.soundVolume < 1.0f) {
          ClientSettings.soundVolume += 0.1f;
          updatedSound = true;
        }
      }
      if (SoundDownButton.isClicked(mouseX, mouseY)) {
        if (ClientSettings.soundVolume > 0.0f) {
          ClientSettings.soundVolume -= 0.1f;
          updatedSound = true;
        }
      }
      if (MusicUpButton.isClicked(mouseX, mouseY)) {
        if (ClientSettings.musicVolume < 1.0f) {
          ClientSettings.musicVolume += 0.1f;
          updatedMusic = true;
        }
      }
      if (MusicDownButton.isClicked(mouseX, mouseY)) {
        if (ClientSettings.musicVolume > 0.0f) {
          ClientSettings.musicVolume -= 0.1f;
          updatedMusic = true;
        }
      }

      /*
      if(FullScreenButton.isClicked(mouseX, mouseY)){
      	BlueSaga.toggleFullScreen();
      }
      */

      if (updatedSound) {
        int saveSFX_ON = 0;

        if (ClientSettings.soundVolume <= 0.0f) {
          ClientSettings.SFX_ON = false;
        } else {
          ClientSettings.SFX_ON = true;
          saveSFX_ON = 1;
        }
        BlueSaga.gameDB.updateDB(
            "update option set GameValue = "
                + ClientSettings.soundVolume
                + " where GameOption = 'SfxVol'");
        BlueSaga.gameDB.updateDB(
            "update option set GameValue = " + saveSFX_ON + " where GameOption = 'Sfx'");
      }

      if (updatedMusic) {
        int saveMUSIC_ON = 0;

        if (ClientSettings.musicVolume <= 0.0f) {
          ClientSettings.MUSIC_ON = false;
        } else {
          ClientSettings.MUSIC_ON = true;
          saveMUSIC_ON = 1;
        }

        BlueSaga.gameDB.updateDB(
            "update option set GameValue = "
                + ClientSettings.musicVolume
                + " where GameOption = 'MusicVol'");
        BlueSaga.gameDB.updateDB(
            "update option set GameValue = " + saveMUSIC_ON + " where GameOption = 'Music'");

        BlueSaga.BG_MUSIC.updateVolume();
      }
      /*
      if(Settings.FULL_SCREEN){
      	int fullscreen = 0;
      	fullscreen = 1;
      	BlueSaga.gameDB.updateDB("update option set GameValue = "+fullscreen+" where GameOption = 'Fullscreen'");
      }
      */
    }
  }
}
