package screens;

import game.BlueSaga;
import game.ClientSettings;
import graphics.BlueSagaColors;
import graphics.Font;
import graphics.ImageResource;
import gui.Button;
import gui.ColorPicker;
import gui.Gui;
import gui.TextField;
import screens.ScreenHandler.ScreenType;

import java.util.ArrayList;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;

import sound.Sfx;
import utils.Encryption;
import utils.LanguageUtils;
import components.Item;
import creature.Creature;

public class CharacterSelection {

  private static ArrayList<Creature> Characters;
  private static ArrayList<Integer> CharacterIds;
  private static ArrayList<Integer> Levels;
  private static ArrayList<String> Names;
  private static int[] Deleted;
  private static ArrayList<Button> Buttons;

  private static Button ChangePasswordButton;
  private static Button DeleteCharacterButton;

  private static int startX = 225;
  private static int startY = 70;

  private static int buttonWidth = 120;
  private static int buttonSpace = 150;

  private static int CharacterSelectedId = 0;
  private static int CharacterSelectedIndex = 0;

  private static ColorPicker ColorPicker;

  // CHANGE PASSWORD
  private static TextField old_password;
  private static TextField new_password;
  private static TextField new_password_repeat;
  private static String change_status;

  public static void init(GameContainer app) {
    DeleteCharacterButton =
        new Button(
            LanguageUtils.getString("ui.character_selection.delete").toUpperCase(), 410, 520, null);
    ChangePasswordButton =
        new Button(
            LanguageUtils.getString("ui.character_selection.change_password").toUpperCase(),
            410,
            570,
            null);

    ScreenHandler.setActiveScreen(ScreenType.CHARACTER_SELECT);

    change_status = "";

    int X = 400;
    int Y = 250;

    old_password = new TextField(app, Font.size12, X + 80, Y, 180, 30);
    old_password.setBackgroundColor(new Color(0, 0, 0, 0));
    old_password.setBorderColor(new Color(0, 0, 0, 0));
    old_password.setTextColor(new Color(255, 255, 255, 255));
    old_password.setFocus(false);
    old_password.setMaskCharacter('*');
    old_password.setMaskEnabled(true);

    new_password = new TextField(app, Font.size12, X + 80, Y + 60, 180, 30);
    new_password.setBackgroundColor(new Color(0, 0, 0, 0));
    new_password.setBorderColor(new Color(0, 0, 0, 0));
    new_password.setTextColor(new Color(255, 255, 255, 255));
    new_password.setMaxLength(20);
    new_password.setFocus(false);
    new_password.setMaskCharacter('*');
    new_password.setMaskEnabled(true);

    new_password_repeat = new TextField(app, Font.size12, X + 80, Y + 120, 180, 30);
    new_password_repeat.setBackgroundColor(new Color(0, 0, 0, 0));
    new_password_repeat.setBorderColor(new Color(0, 0, 0, 0));
    new_password_repeat.setTextColor(new Color(255, 255, 255, 255));
    new_password.setMaxLength(20);
    new_password_repeat.setFocus(false);
    new_password_repeat.setMaskCharacter('*');
    new_password_repeat.setMaskEnabled(true);

    ColorPicker = new ColorPicker(347, 340);

    ColorPicker.addColor(new Color(71, 178, 218)); // BLUE
    ColorPicker.addColor(new Color(225, 91, 91)); // RED
    ColorPicker.addColor(new Color(255, 176, 23)); // ORANGE
    ColorPicker.addColor(new Color(246, 255, 101)); // YELLOW
    ColorPicker.addColor(new Color(127, 191, 146)); // GREEN
    ColorPicker.addColor(new Color(255, 173, 238)); // PINK
    ColorPicker.addColor(new Color(75, 80, 82)); // DARK GRAY
    ColorPicker.addColor(new Color(219, 128, 255)); // PURPLE
  }

  public static void load(String characterInfo) {
    Characters = new ArrayList<Creature>();
    CharacterIds = new ArrayList<Integer>();
    Levels = new ArrayList<Integer>();
    Names = new ArrayList<String>();
    Buttons = new ArrayList<Button>();
    Deleted = new int[8];

    int i = 0;

    if (!characterInfo.equals("None")) {
      String characters_info[] = characterInfo.split("/");

      Creature tempCreature;
      for (String char_info : characters_info) {
        String ch_inf[] = char_info.split(";");

        // CreatureId, Name, Level, AreaId, HeadId, WeaponId, OffHandId, AmuletId, ArtifactId
        // HeadX, HeadY, WeaponX, WeaponY, OffHandX, OffHandY, AmuletX, AmuletY, ArtifactX, ArtifactY

        int characterId = Integer.parseInt(ch_inf[0]);
        int creatureId = Integer.parseInt(ch_inf[1]);
        String name = ch_inf[2];
        int level = Integer.parseInt(ch_inf[3]);

        int mouthFeatureId = Integer.parseInt(ch_inf[4]);
        int accessoriesId = Integer.parseInt(ch_inf[5]);
        int skinFeatureId = Integer.parseInt(ch_inf[6]);

        int headId = Integer.parseInt(ch_inf[7]);
        int weaponId = Integer.parseInt(ch_inf[8]);
        int offhandId = Integer.parseInt(ch_inf[9]);
        int amuletId = Integer.parseInt(ch_inf[10]);
        int artifactId = Integer.parseInt(ch_inf[11]);

        int headX = Integer.parseInt(ch_inf[12]);
        int headY = Integer.parseInt(ch_inf[13]);
        int weaponX = Integer.parseInt(ch_inf[14]);
        int weaponY = Integer.parseInt(ch_inf[15]);
        int offhandX = Integer.parseInt(ch_inf[16]);
        int offhandY = Integer.parseInt(ch_inf[17]);
        int amuletX = Integer.parseInt(ch_inf[18]);
        int amuletY = Integer.parseInt(ch_inf[19]);
        int artifactX = Integer.parseInt(ch_inf[20]);
        int artifactY = Integer.parseInt(ch_inf[21]);

        int mouthFeatureX = Integer.parseInt(ch_inf[22]);
        int mouthFeatureY = Integer.parseInt(ch_inf[23]);
        int accessoriesX = Integer.parseInt(ch_inf[24]);
        int accessoriesY = Integer.parseInt(ch_inf[25]);
        int skinFeatureX = Integer.parseInt(ch_inf[26]);
        int skinFeatureY = Integer.parseInt(ch_inf[27]);

        int deleted = Integer.parseInt(ch_inf[28]);
        // String customColor = ch_inf[20];

        tempCreature = new Creature(0, 0, 0);
        tempCreature.setType(creatureId);

        tempCreature.MyEquipHandler.setHeadX(headX);
        tempCreature.MyEquipHandler.setHeadY(headY);
        tempCreature.MyEquipHandler.setWeaponX(weaponX);
        tempCreature.MyEquipHandler.setWeaponY(weaponY);
        tempCreature.MyEquipHandler.setOffHandX(offhandX);
        tempCreature.MyEquipHandler.setOffHandY(offhandY);
        tempCreature.MyEquipHandler.setAmuletX(amuletX);
        tempCreature.MyEquipHandler.setAmuletY(amuletY);
        tempCreature.MyEquipHandler.setArtifactX(artifactX);
        tempCreature.MyEquipHandler.setArtifactY(artifactY);

        tempCreature.getCustomization().setMouthFeatureId(mouthFeatureId);
        tempCreature.getCustomization().setMouthFeatureX(mouthFeatureX);
        tempCreature.getCustomization().setMouthFeatureY(mouthFeatureY);

        tempCreature.getCustomization().setAccessoriesId(accessoriesId);
        tempCreature.getCustomization().setAccessoriesX(accessoriesX);
        tempCreature.getCustomization().setAccessoriesY(accessoriesY);

        tempCreature.getCustomization().setSkinFeatureId(skinFeatureId);
        tempCreature.getCustomization().setSkinFeatureX(skinFeatureX);
        tempCreature.getCustomization().setSkinFeatureY(skinFeatureY);

        if (headId > 0) {
          Item newEquip = new Item(headId);
          newEquip.setType("Head");
          tempCreature.MyEquipHandler.equipItem(newEquip);
        }
        if (weaponId > 0) {
          Item newEquip = new Item(weaponId);
          newEquip.setType("Weapon");
          tempCreature.MyEquipHandler.equipItem(newEquip);
        }
        if (offhandId > 0) {
          Item newEquip = new Item(offhandId);
          newEquip.setType("OffHand");
          tempCreature.MyEquipHandler.equipItem(newEquip);
        }
        if (amuletId > 0) {
          Item newEquip = new Item(amuletId);
          newEquip.setType("Amulet");
          tempCreature.MyEquipHandler.equipItem(newEquip);
        }
        if (artifactId > 0) {
          Item newEquip = new Item(artifactId);
          newEquip.setType("Artifact");
          tempCreature.MyEquipHandler.equipItem(newEquip);
        }

        CharacterIds.add(characterId);
        Characters.add(tempCreature);
        Names.add(name);
        Levels.add(level);
        Deleted[i] = deleted;
        Buttons.add(
            new Button(
                "",
                startX + i % 4 * buttonSpace,
                (int) (startY + buttonSpace * Math.floor(i / 4)) + 70,
                buttonWidth,
                buttonWidth,
                null));
        i++;
      }
    }
    while (i < 8) {
      Buttons.add(
          new Button(
              "",
              startX + i % 4 * buttonSpace,
              (int) (startY + buttonSpace * Math.floor(i / 4)) + 70,
              buttonWidth,
              buttonWidth,
              null));
      Deleted[i] = -1;
      i++;
    }

    if (!Gui.MapWindow.isSaving()) {
      show();
    } else {
      ScreenHandler.setActiveScreen(ScreenType.LOADING);
    }
  }

  public static void show() {
    ScreenHandler.setActiveScreen(ScreenType.CHARACTER_SELECT);
  }

  public static void draw(Graphics g, GameContainer app) {

    int mouseX = app.getInput().getAbsoluteMouseX();
    int mouseY = app.getInput().getAbsoluteMouseY();

    g.setFont(Font.size12);

    if (ScreenHandler.getActiveScreen() == ScreenType.CHARACTER_SELECT
        || ScreenHandler.getActiveScreen() == ScreenType.CHARACTER_DELETE) {
      ImageResource.getSprite("startscreen/character_label").draw(startX + 115, startY);

      for (int i = 0; i < 8; i++) {
        boolean drawChar = true;

        if (drawChar) {
          Buttons.get(i).draw(g, mouseX, mouseY);
          if (i < Characters.size()) {

            if (Buttons.get(i).isClicked(mouseX, mouseY)) {
              g.setColor(BlueSagaColors.BLACK);
            } else {
              g.setColor(BlueSagaColors.YELLOW);
            }
            g.drawString(
                Names.get(i),
                startX + i % 4 * buttonSpace + 60 - Font.size12.getWidth(Names.get(i)) / 2,
                (int) (startY + buttonSpace * Math.floor(i / 4)) + 85);
            g.setColor(BlueSagaColors.WHITE);
            g.drawString(
                "LVL " + Levels.get(i),
                startX
                    + i % 4 * buttonSpace
                    + 60
                    - Font.size12.getWidth("LVL " + Levels.get(i)) / 2,
                (int) (startY + buttonSpace * Math.floor(i / 4)) + 165);

            if (Deleted[i] > -1) {
              Characters.get(i)
                  .draw(
                      g,
                      startX + i % 4 * buttonSpace + 60,
                      (int) (startY + buttonSpace * Math.floor(i / 4)) + 130,
                      null);
              if (Buttons.get(i).isClicked(mouseX, mouseY)) {
                g.setColor(
                    new Color(
                        BlueSagaColors.YELLOW.getRed(),
                        BlueSagaColors.YELLOW.getGreen(),
                        BlueSagaColors.YELLOW.getBlue(),
                        200));
              } else {
                g.setColor(
                    new Color(
                        BlueSagaColors.RED.getRed(),
                        BlueSagaColors.RED.getGreen(),
                        BlueSagaColors.RED.getBlue(),
                        200));
              }
              g.fillRect(Buttons.get(i).getX() + 10, Buttons.get(i).getY() + 10, 100, 100);

            } else {
              Characters.get(i)
                  .draw(
                      g,
                      startX + i % 4 * buttonSpace + 60,
                      (int) (startY + buttonSpace * Math.floor(i / 4)) + 130,
                      null);
            }

            if (ScreenHandler.getActiveScreen() == ScreenType.CHARACTER_DELETE) {
              if (Deleted[i] == -1) {
                if (Buttons.get(i).isClicked(mouseX, mouseY)) {
                  ImageResource.getSprite("gui/menu/delete_character")
                      .draw(
                          startX + i % 4 * buttonSpace + 13,
                          (int) (startY + buttonSpace * Math.floor(i / 4)) + 80,
                          new Color(255, 255, 255, 200));
                } else {
                  ImageResource.getSprite("gui/menu/delete_character")
                      .draw(
                          startX + i % 4 * buttonSpace + 13,
                          (int) (startY + buttonSpace * Math.floor(i / 4)) + 80,
                          new Color(255, 255, 255, 100));
                }
              } else {
                if (Buttons.get(i).isClicked(mouseX, mouseY)) {
                  g.setColor(BlueSagaColors.BLACK);
                } else {
                  g.setColor(BlueSagaColors.WHITE);
                }
                g.drawString(
                    LanguageUtils.getString("ui.character_selection.recover").toUpperCase(),
                    startX + i % 4 * buttonSpace + 30,
                    (int) (startY + buttonSpace * Math.floor(i / 4)) + 125);
              }
            } else {
              if (Deleted[i] > -1) {
                if (Buttons.get(i).isClicked(mouseX, mouseY)) {
                  g.setColor(BlueSagaColors.BLACK);
                } else {
                  g.setColor(BlueSagaColors.WHITE);
                }
                g.drawString(
                    LanguageUtils.getString("ui.character_selection.deleted_in").toUpperCase(),
                    startX + i % 4 * buttonSpace + 20,
                    (int) (startY + buttonSpace * Math.floor(i / 4)) + 117);

                int daysLeft = 3 - Deleted[i];
                g.drawString(
                    daysLeft
                        + " "
                        + LanguageUtils.getString("ui.character_selection.days").toUpperCase(),
                    startX + i % 4 * buttonSpace + 35,
                    (int) (startY + buttonSpace * Math.floor(i / 4)) + 132);
              }
            }

          } else {
            if (Buttons.get(i).isClicked(mouseX, mouseY)) {
              g.setColor(BlueSagaColors.BLACK);
            } else {
              g.setColor(BlueSagaColors.WHITE);
            }
            g.drawString(
                LanguageUtils.getString("ui.character_selection.create").toUpperCase(),
                startX + i % 4 * buttonSpace + 35,
                (int) (startY + buttonSpace * Math.floor(i / 4)) + 122);
          }
        }
      }

      ChangePasswordButton.draw(g, mouseX, mouseY);
      DeleteCharacterButton.draw(g, mouseX, mouseY);

    } else if (ScreenHandler.getActiveScreen() == ScreenType.CHARACTER_OPTIONS) {
      ImageResource.getSprite("startscreen/character_label").draw(startX + 115, startY);

      g.setFont(Font.size12);

      int charX = 470;
      int charY = 200;

      Characters.get(CharacterSelectedIndex).draw(g, charX, charY + 20, null);

      g.setColor(new Color(0, 0, 0, 100));
      g.drawString(
          Names.get(CharacterSelectedIndex),
          charX + 27 - Font.size12.getWidth(Names.get(CharacterSelectedIndex)) / 2,
          charY - 18);
      g.setColor(BlueSagaColors.YELLOW);
      g.drawString(
          Names.get(CharacterSelectedIndex),
          charX + 25 - Font.size12.getWidth(Names.get(CharacterSelectedIndex)) / 2,
          charY - 20);

      g.setColor(new Color(0, 0, 0, 100));
      g.drawString(
          "LVL " + Levels.get(CharacterSelectedIndex),
          charX + 27 - Font.size12.getWidth("LVL " + Levels.get(CharacterSelectedIndex)) / 2,
          charY + 72);
      g.setColor(BlueSagaColors.WHITE);
      g.drawString(
          "LVL " + Levels.get(CharacterSelectedIndex),
          charX + 25 - Font.size12.getWidth("LVL " + Levels.get(CharacterSelectedIndex)) / 2,
          charY + 70);

      g.setColor(new Color(0, 0, 0, 200));
      g.drawString("Customize character: ", 350, 320);
      ColorPicker.draw(g, mouseX, mouseY);

    } else {

      int X = 300;
      int Y = 200;

      g.setFont(Font.size12);

      g.setColor(new Color(255, 255, 255, 100));
      g.fillRoundRect(X + 170, Y + 38, 200, 40, 10);
      g.setColor(new Color(0, 0, 0, 150));
      g.fillRoundRect(X + 170 + 5, Y + 38 + 5, 190, 30, 8);
      g.setColor(BlueSagaColors.BLACK);
      g.drawString(LanguageUtils.getString("ui.character_selection.old_password"), X, Y + 50);
      g.setColor(BlueSagaColors.WHITE);
      old_password.render(app, g);

      Y += 60;

      g.setColor(new Color(255, 255, 255, 100));
      g.fillRoundRect(X + 170, Y + 38, 200, 40, 10);
      g.setColor(new Color(0, 0, 0, 150));
      g.fillRoundRect(X + 170 + 5, Y + 38 + 5, 190, 30, 8);
      g.setColor(BlueSagaColors.BLACK);
      g.drawString(LanguageUtils.getString("ui.character_selection.new_password"), X, Y + 50);
      g.setColor(BlueSagaColors.WHITE);
      new_password.render(app, g);

      Y += 60;

      g.setColor(new Color(255, 255, 255, 100));
      g.fillRoundRect(X + 170, Y + 38, 200, 40, 10);
      g.setColor(new Color(0, 0, 0, 150));
      g.fillRoundRect(X + 170 + 5, Y + 38 + 5, 190, 30, 8);
      g.setColor(BlueSagaColors.BLACK);
      g.drawString(LanguageUtils.getString("ui.character_selection.repeat"), X, Y + 50);
      g.setColor(BlueSagaColors.WHITE);
      new_password_repeat.render(app, g);

      Y += 100;

      g.setColor(BlueSagaColors.BLACK);
      g.drawString(change_status, 512 - Font.size12.getWidth(change_status) / 2, Y);

      g.setColor(BlueSagaColors.RED);
      g.drawString(LanguageUtils.getString("ui.character_selection.beta_warning"), 200, 150);

      ChangePasswordButton.draw(g, mouseX, mouseY);
    }

    g.setColor(new Color(255, 255, 255, 255));
    g.drawString("v 0." + ClientSettings.VERSION_NR, 950, 610);
  }

  public static int selectCharacter(int mouseX, int mouseY) {
    int clickedId = -1;

    for (int i = 0; i < 8; i++) {
      if (Buttons.get(i).isClicked(mouseX, mouseY)) {
        if (i < Characters.size()) {
          clickedId = CharacterIds.get(i);
          CharacterSelectedIndex = i;
          break;
        } else {
          clickedId = 0;
        }
      }
    }

    if (clickedId > -1) {
      Sfx.play("gui/menu_confirm2");
    } else {
      Sfx.play("gui/menu_no");
    }
    return clickedId;
  }

  public static void keyLogic(Input INPUT) {
    if (INPUT.isKeyPressed(Input.KEY_ESCAPE)) {
      if (goBack()) {
        LoginScreen.setStatusMessage(LanguageUtils.getString("ui.login.instructions"));
        ScreenHandler.setActiveScreen(ScreenType.LOGIN);
        LoginScreen.focusLoginField();
      }
    } else {
      if (INPUT.isMousePressed(0)) {
        if (ScreenHandler.getActiveScreen() == ScreenType.CHARACTER_SELECT
            || ScreenHandler.getActiveScreen() == ScreenType.CHARACTER_DELETE) {
          int clickedId = selectCharacter(INPUT.getAbsoluteMouseX(), INPUT.getAbsoluteMouseY());
          if (clickedId == 0) {
            // CREATE NEW CHARACTER

            BlueSaga.client.sendMessage("newchar", "info");
          } else if (clickedId > -1) {
            if (ScreenHandler.getActiveScreen() == ScreenType.CHARACTER_SELECT) {

              CharacterSelectedId = clickedId;
              ScreenHandler.LoadingStatus =
                  LanguageUtils.getString("ui.character_selection.request_data");
              ScreenHandler.setActiveScreen(ScreenType.LOADING);

              // SEND CHARACTER INFO REQUEST
              BlueSaga.client.sendMessage("playerinfo", "" + CharacterSelectedId);

            } else if (ScreenHandler.getActiveScreen() == ScreenType.CHARACTER_DELETE) {
              BlueSaga.client.sendMessage("deletechar", "" + clickedId);
              ScreenHandler.setActiveScreen(ScreenType.CHARACTER_SELECT);
              DeleteCharacterButton.setLabel(
                  LanguageUtils.getString("ui.character_selection.delete"));
            }
          }

          if (DeleteCharacterButton.isClicked(
              INPUT.getAbsoluteMouseX(), INPUT.getAbsoluteMouseY())) {
            if (ScreenHandler.getActiveScreen() == ScreenType.CHARACTER_DELETE) {
              ScreenHandler.setActiveScreen(ScreenType.CHARACTER_SELECT);
              DeleteCharacterButton.setLabel(
                  LanguageUtils.getString("ui.character_selection.delete"));
            } else {
              ScreenHandler.setActiveScreen(ScreenType.CHARACTER_DELETE);
              DeleteCharacterButton.setLabel(
                  LanguageUtils.getString("ui.character_selection.cancel"));
            }
          } else if (ChangePasswordButton.isClicked(
              INPUT.getAbsoluteMouseX(), INPUT.getAbsoluteMouseY())) {
            ScreenHandler.setActiveScreen(ScreenType.CHANGE_PASSWORD);
            old_password.setText("");
            new_password.setText("");
            new_password_repeat.setText("");
            old_password.setFocus(true);
            change_status = "";
          }
        } else if (ScreenHandler.getActiveScreen() == ScreenType.CHARACTER_OPTIONS) {

        } else if (ScreenHandler.getActiveScreen() == ScreenType.CHANGE_PASSWORD) {
          if (!change_status.equals(LanguageUtils.getString("ui.character_selection.send"))
              && ChangePasswordButton.isClicked(
                  INPUT.getAbsoluteMouseX(), INPUT.getAbsoluteMouseY())) {
            // CHECK IF NEW PASSWORD IS REPEATED CORRECTLY
            if (!new_password.getText().equals(new_password_repeat.getText())) {
              change_status = LanguageUtils.getString("ui.character_selection.new_password_wrong");
            } else {
              change_status = LanguageUtils.getString("ui.character_selection.send");

              // ENCODE PASSWORD

              String old_password_enc =
                  Encryption.encryptPassword(BlueSaga.client.getUserMail(), old_password.getText());
              String new_password_enc =
                  Encryption.encryptPassword(BlueSaga.client.getUserMail(), new_password.getText());

              BlueSaga.client.sendMessage(
                  "changepassword", old_password_enc + ";" + new_password_enc);
              LoginScreen.restartCountdown();
            }
          }
        }
      }

      if (INPUT.isKeyPressed(Input.KEY_TAB)
          && ScreenHandler.getActiveScreen() == ScreenType.CHANGE_PASSWORD) {
        if (old_password.hasFocus()) {
          new_password.setFocus(true);
        } else if (new_password.hasFocus()) {
          new_password_repeat.setFocus(true);
        } else if (new_password_repeat.hasFocus()) {
          old_password.setFocus(true);
        }
      }
    }
  }

  public static void changeCharStatus(int charId, int delStatus) {
    int charIndex = 0;
    for (int i = 0; i < 8; i++) {
      if (CharacterIds.get(i) == charId) {
        charIndex = i;
        break;
      }
    }
    Deleted[charIndex] = delStatus;
  }

  public static void setStatus(String newStatus) {
    change_status = newStatus;
  }

  public static boolean goBack() {
    if (ScreenHandler.getActiveScreen() != ScreenType.CHARACTER_SELECT) {
      ScreenHandler.setActiveScreen(ScreenType.CHARACTER_SELECT);
      DeleteCharacterButton.setLabel(LanguageUtils.getString("ui.character_selection.delete"));
    } else {
      BlueSaga.HAS_QUIT = true;
      BlueSaga.client.closeConnection();
      return true;
    }
    return false;
  }
}
