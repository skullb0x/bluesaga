package screens;

import java.util.Vector;

import game.BlueSaga;
import graphics.BlueSagaColors;
import graphics.Font;
import graphics.ImageResource;
import gui.Button;
import gui.TextField;
import screens.ScreenHandler.ScreenType;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;

import sound.Sfx;
import utils.LanguageUtils;
import creature.Creature;

public class CharacterCreate {

  private static TextField new_name;

  private static Vector<Creature> Creatures = new Vector<Creature>();

  private static Vector<Button> FamilyButtons = new Vector<Button>();
  private static Vector<Button> CreatureButtons = new Vector<Button>();
  private static Vector<Button> ClassButtons = new Vector<Button>();

  private Vector<String> weaponTypes = new Vector<String>();
  private Vector<Integer> weaponIds = new Vector<Integer>();

  private static int selectedFamilyIndex = 0;
  private static int selectedCreatureIndex = 100;
  private static int selectedClassIndex = 100;

  private static int startX = 235;
  private static int startY = 100;

  int buttonWidth = 120;
  int buttonSpace = 150;

  private static String createState;

  private static Button createButton;

  private static String status;

  private static boolean checkingServer = false;

  public static void init(GameContainer app) {
    status = "";

    new_name = new TextField(app, Font.size12, startX + 185, startY + 110, 180, 30);
    new_name.setBackgroundColor(new Color(0, 0, 0, 0));
    new_name.setBorderColor(new Color(0, 0, 0, 0));
    new_name.setTextColor(new Color(255, 255, 255, 255));
    new_name.setMaxLength(15);

    checkingServer = false;

    createButton =
        new Button(
            LanguageUtils.getString("ui.character_create.create_character").toUpperCase(),
            410,
            560,
            null);
  }

  public static void load(String createData) {

    // FamilyId, FamilyName;  FamilyId2, FamilyName2

    String fcData[] = createData.split("/");

    String familyData[] = fcData[0].split(";");
    String creatureData[] = fcData[1].split(";");

    FamilyButtons.clear();

    // LOAD FAMILIES WITH CREATURES
    int i = 0;
    for (String fData : familyData) {
      String familyInfo[] = fData.split(",");
      Integer.parseInt(familyInfo[0]);
      FamilyButtons.add(new Button("", 480, 300, 50, 50, null));
      i++;
    }

    FamilyButtons.get(0).select();

    createState = "choose creature";

    // CreatureId, CreatureName; CreatureId2, CreatureName2

    // LOAD CREATURES
    CreatureButtons.clear();

    int creatureButtonX = 170;

    i = 0;

    for (String cData : creatureData) {
      String creatureInfo[] = cData.split(",");
      int creatureId = Integer.parseInt(creatureInfo[0]);
      String creatureName = creatureInfo[1];

      CreatureButtons.add(
          new Button("", creatureButtonX + i % 9 * 100, startY + 195, 75, 100, null));
      Creature tempCreature = new Creature(0, 0, 0);
      tempCreature.setType(creatureId);
      tempCreature.setName(creatureName);
      Creatures.add(tempCreature);
      i++;
    }

    ClassButtons.clear();
    ClassButtons.add(new Button("", 517 - 108, startY + 350, 50, 50, null));
    ClassButtons.add(new Button("", 517 - 35, startY + 350, 50, 50, null));
    ClassButtons.add(new Button("", 517 + 37, startY + 350, 50, 50, null));

    ScreenHandler.setActiveScreen(ScreenType.CHARACTER_CREATE);
    focusName();
  }

  public static void focusName() {
    new_name.setAcceptingInput(true);
    new_name.setFocus(true);
    new_name.setCursorVisible(true);
  }

  public static boolean selectFamily(int mouseX, int mouseY) {
    boolean clickOnButton = false;
    for (int i = 0; i < 8; i++) {
      if (i < FamilyButtons.size()) {
        if (FamilyButtons.get(i).isClicked(mouseX, mouseY)) {
          clickOnButton = true;
          if (selectedFamilyIndex < 100) {
            FamilyButtons.get(selectedFamilyIndex).deselect();
          }
          Sfx.play("gui/menu_confirm2");
          //selectedFamilyId = FamilyIds.get(i);
          selectedFamilyIndex = i;
          FamilyButtons.get(i).select();
          if (createState.equals("choose name")) {
            createState = "choose creature";
          }
        }
      }
    }
    return clickOnButton;
  }

  public static boolean selectCreature(int mouseX, int mouseY) {
    boolean clickOnButton = false;
    for (int i = 0; i < 8; i++) {
      if (i < CreatureButtons.size()) {
        if (CreatureButtons.get(i).isClicked(mouseX, mouseY)) {
          clickOnButton = true;
          if (selectedCreatureIndex < 100) {
            CreatureButtons.get(selectedCreatureIndex).deselect();
          }

          Sfx.play("gui/menu_confirm2");
          selectedCreatureIndex = i;
          CreatureButtons.get(i).select();
        }
      }
    }
    return clickOnButton;
  }

  public static boolean selectClass(int mouseX, int mouseY) {
    boolean clickOnButton = false;
    for (int i = 0; i < 3; i++) {
      if (i < ClassButtons.size()) {
        if (ClassButtons.get(i).isClicked(mouseX, mouseY)) {
          clickOnButton = true;
          if (selectedClassIndex < 100) {
            ClassButtons.get(selectedClassIndex).deselect();
          }

          Sfx.play("gui/menu_confirm2");
          selectedClassIndex = i;
          ClassButtons.get(i).select();
        }
      }
    }
    return clickOnButton;
  }

  public static boolean createChar(int mouseX, int mouseY) {
    if (createButton.isClicked(mouseX, mouseY) && !checkingServer) {
      if (new_name.getText().matches("^[a-zA-Z]+$") && new_name.getText().length() > 0) {
        //NAME APPROVED!
      } else {
        //NAME INCORRECT!
        status = LanguageUtils.getString("ui.character_create.incorrect_name");
        return false;
      }

      if (selectedFamilyIndex == 100) {
        //FAMILY NOT SELECTED!
        status = LanguageUtils.getString("ui.character_create.select_family");
        return false;
      }
      if (selectedCreatureIndex == 100) {
        //CREATURE NOT SELECTED!
        status = LanguageUtils.getString("ui.character_create.select_creature");
        return false;
      }

      if (selectedClassIndex == 100) {
        // CLASS NOT SELECTED!
        status = LanguageUtils.getString("ui.character_create.select_class");
        return false;
      }

      status = LanguageUtils.getString("ui.character_create.creating_character");
      checkingServer = true;

      return true;
    }
    return false;
  }

  public static void draw(Graphics g, GameContainer app) {
    int mouseX = app.getInput().getAbsoluteMouseX();
    int mouseY = app.getInput().getAbsoluteMouseY();

    int posY = startY;

    ImageResource.getSprite("startscreen/new_label").draw(startX + 90, posY);

    posY += 60;

    ImageResource.getSprite("startscreen/name_label").draw(startX + 200, posY);

    posY += 35;

    g.setColor(new Color(255, 255, 255, 100));
    g.fillRoundRect(startX + 170, posY, 200, 40, 10);
    g.setColor(new Color(0, 0, 0, 150));
    g.fillRoundRect(startX + 175, posY + 5, 190, 30, 8);
    g.setColor(new Color(255, 255, 255, 255));
    new_name.setLocation(
        startX + 265 - Font.size12bold.getWidth(new_name.getText()) / 2, posY + 12);
    new_name.render(app, g);

    /*
    BlueSaga.GFX.getSprite("startscreen/family_label").draw(startX+185, posY);

    for(int i = 0; i < FamilyButtons.size(); i++){
    	FamilyButtons.get(i).draw(g, mouseX, mouseY);

    	if(FamilyButtons.get(i).isClicked(mouseX, mouseY) || FamilyButtons.get(i).isSelected()){
    		g.setColor(BlueSagaColors.BLACK);
    	}else{
    		g.setColor(BlueSagaColors.YELLOW);
    	}

    	BlueSaga.GFX.getSprite("gui/menu/family_"+Families.get(i)+"_icon").draw(FamilyButtons.get(i).getX()+ 13, FamilyButtons.get(i).getY() + 12);
    }
    */

    posY += 65;

    if (createState.equals("choose creature")
        || createState.equals("save info")
        || createState.equals("saving")) {

      ImageResource.getSprite("startscreen/creature_label").draw(400, posY);

      g.setFont(Font.size10);

      for (int j = 0; j < CreatureButtons.size(); j++) {
        CreatureButtons.get(j).draw(g, mouseX, mouseY);
        Creatures.get(j)
            .draw(g, CreatureButtons.get(j).getX() + 40, CreatureButtons.get(j).getY() + 35, null);

        int textWidth = Font.size10.getWidth(Creatures.get(j).getName());

        if (CreatureButtons.get(j).isClicked(mouseX, mouseY) || selectedCreatureIndex == j) {
          g.setColor(BlueSagaColors.BLACK);
        } else {
          g.setColor(BlueSagaColors.WHITE);
        }
        g.drawString(
            Creatures.get(j).getName(),
            CreatureButtons.get(j).getX() + 38 - textWidth / 2,
            CreatureButtons.get(j).getY() + 70);

        //ImageResource.getSprite("gui/menu/icon_class"+Creatures.get(j).getClassId()).draw(CreatureButtons.get(j).getX()+ 28, CreatureButtons.get(j).getY() + 64);
      }

      posY += 155;

      ImageResource.getSprite("startscreen/class_label").draw(410, posY);

      for (int j = 0; j < ClassButtons.size(); j++) {
        ClassButtons.get(j).draw(g, mouseX, mouseY);
        int classId = j + 1;
        ImageResource.getSprite("gui/menu/icon_class" + classId)
            .draw(ClassButtons.get(j).getX() + 15, ClassButtons.get(j).getY() + 15);
      }
    }

    createButton.draw(g, mouseX, mouseY);

    if (!status.equals("")) {
      g.setColor(BlueSagaColors.WHITE);
      g.setFont(Font.size12bold);
      g.drawString(status, 512 - Font.size12bold.getWidth(status) / 2, 610);
    }
  }

  public void cancelFamilySelect() {
    FamilyButtons.get(selectedFamilyIndex).deselect();
  }

  public static String getCreateInfo() {
    int classId = selectedClassIndex + 1;
    return new_name.getText()
        + ";"
        + Creatures.get(selectedCreatureIndex).getCreatureId()
        + ";"
        + classId;
  }

  public static void setStatus(String newStatus) {
    status = newStatus;
  }

  public static void keyLogic(Input INPUT) {
    if (INPUT.isKeyPressed(Input.KEY_ESCAPE)) {
      ScreenHandler.setActiveScreen(ScreenType.CHARACTER_SELECT);
      disableInputFields();
    } else {
      if (INPUT.isMousePressed(0)) {
        if (selectFamily(INPUT.getAbsoluteMouseX(), INPUT.getAbsoluteMouseY())) {
          Sfx.play("gui/menu_confirm2");
        } else if (selectCreature(INPUT.getAbsoluteMouseX(), INPUT.getAbsoluteMouseY())) {
          Sfx.play("gui/menu_confirm2");
        } else if (createChar(INPUT.getAbsoluteMouseX(), INPUT.getAbsoluteMouseY())
            && selectedCreatureIndex != 100
            && selectedClassIndex != 100) {
          Sfx.play("gui/menu_confirm2");
          BlueSaga.client.sendMessage("createchar", getCreateInfo());
          createState = "saving";
        } else if (selectClass(INPUT.getAbsoluteMouseX(), INPUT.getAbsoluteMouseY())) {
          Sfx.play("gui/menu_confirm2");
        } else {
          Sfx.play("gui/menu_no");
        }
      } else {
        focusName();
      }
    }
  }

  public static void disableInputFields() {
    new_name.setAcceptingInput(false);
  }

  public boolean isCheckingServer() {
    return checkingServer;
  }

  public static void setCheckingServer(boolean state) {
    checkingServer = state;
  }
}
