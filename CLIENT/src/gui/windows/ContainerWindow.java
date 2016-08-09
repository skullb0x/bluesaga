package gui.windows;

import game.BlueSaga;
import graphics.Font;
import gui.Gui;
import gui.dragndrop.DropBox;

import java.util.HashMap;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;

import sound.Sfx;
import utils.WebUtils;
import components.Item;

public class ContainerWindow extends Window {

  private String Name;

  private String Id; // "Inventory" or "Container X,Y,Z" or "Shop Id"
  private int SizeW;
  private int SizeH;

  private int ContainerX = 0;
  private int ContainerY = 0;
  private int ContainerZ = 0;

  private HashMap<String, DropBox> itemBoxes = new HashMap<String, DropBox>();

  private ItemInfoBox SelectedInfoBox;
  private Item selectedItemForInfo;

  public ContainerWindow(int x, int y, int width, int height, boolean ShowCloseButton) {
    super("ContainerW", x, y, width, height, ShowCloseButton);
    // TODO Auto-generated constructor stub

    SelectedInfoBox = new ItemInfoBox(0, 0, 1, 1);
    selectedItemForInfo = null;
  }

  public void init(String name, String newId, int x, int y, int sizeW, int sizeH) {
    Name = name;
    Id = newId;
    super.setPos(x, y);
    SizeW = sizeW;
    SizeH = sizeH;

    String containerXYZ[] = Id.split(",");
    setContainerX(Integer.parseInt(containerXYZ[0]));
    setContainerY(Integer.parseInt(containerXYZ[1]));
    setContainerZ(Integer.parseInt(containerXYZ[2]));

    setSize(SizeW * 50 + 20, SizeH * 50 + 50);

    itemBoxes.clear();
    for (int i = 0; i < SizeW; i++) {
      for (int j = 0; j < SizeH; j++) {
        itemBoxes.put(i + "," + j, new DropBox(x + 10 + i * 50, y + 40 + j * 50));
      }
    }
  }

  public boolean addItem(Item newItem) {
    boolean placeOk = false;

    for (int j = 0; j < SizeH; j++) {
      for (int i = 0; i < SizeW; i++) {
        if (itemBoxes.get(i + "," + j).getItem() == null) {
          itemBoxes.get(i + "," + j).setItem(newItem);
          placeOk = true;
          break;
        }
      }
      if (placeOk) {
        break;
      }
    }
    return placeOk;
  }

  public boolean addItemAtPos(Item newItem, int x, int y) {
    boolean placeOk = true;
    if (itemBoxes.get(x + "," + y) == null) {
      placeOk = false;
    } else {
      if (itemBoxes.get(x + "," + y).getItem() != null) {
        placeOk = false;
      } else {
        itemBoxes.get(x + "," + y).setItem(newItem);
      }
    }

    if (placeOk) {
      Sfx.playRandomPitch("gui/move_item");
    }
    return placeOk;
  }

  @Override
  public void draw(GameContainer app, Graphics g, int mouseX, int mouseY) {
    if (isVisible()) {
      super.draw(app, g, mouseX, mouseY);

      if (isFullyOpened()) {

        g.setFont(Font.size12);
        g.setColor(new Color(255, 255, 255, 255));
        g.drawString(Name, X + 15 + moveX, Y + 15 + moveY);

        boolean mouseOnItem = false;

        for (int i = 0; i < SizeW; i++) {
          for (int j = 0; j < SizeH; j++) {
            itemBoxes.get(i + "," + j).draw(g, mouseX, mouseY, moveX, moveY);

            //if(Gui.isWindowOnTop(getDepthZ())){
            if (itemBoxes.get(i + "," + j).isSelected(mouseX, mouseY, moveX, moveY)) {

              if (itemBoxes.get(i + "," + j).getItem() != null) {
                mouseOnItem = true;

                boolean showInfo = false;
                if (selectedItemForInfo != null) {
                  if (selectedItemForInfo.getId() != itemBoxes.get(i + "," + j).getItem().getId()) {
                    showInfo = true;
                  }
                } else {
                  showInfo = true;
                }

                if (showInfo) {
                  selectedItemForInfo = itemBoxes.get(i + "," + j).getItem();
                  //int nrItems = itemBoxes.get(i+","+j).getNumber();

                  int infoBoxX = X + (i + 1) * 50 + moveX;
                  if (infoBoxX > 800) {
                    infoBoxX -= 200;
                  }

                  SelectedInfoBox = new ItemInfoBox(infoBoxX, Y + j * 50 + moveY, 1, 1);

                  if (getName().equals("Personal Chest")) {
                    BlueSaga.client.sendMessage(
                        "item_info", "personalchest;" + selectedItemForInfo.getUserItemId());
                  } else if (getName().equals("Closet")) {
                    BlueSaga.client.sendMessage(
                        "item_info", "closet;" + selectedItemForInfo.getId());
                  } else {
                    BlueSaga.client.sendMessage(
                        "item_info",
                        "container;"
                            + getContainerX()
                            + ","
                            + getContainerY()
                            + ","
                            + getContainerZ()
                            + ";"
                            + i
                            + ","
                            + j);
                  }
                }
              } else {
                selectedItemForInfo = null;
                SelectedInfoBox.close();
              }
            }
            //}
          }
        }

        if (!mouseOnItem) {
          selectedItemForInfo = null;
          SelectedInfoBox.close();
        }
        SelectedInfoBox.draw(app, g, 0, 0);
        g.setFont(Font.size12);
      }
    }
  }

  public void showInfoBox(String info) {
    SelectedInfoBox.load(info);
  }

  @Override
  public void leftMouseClick(Input INPUT) {
    super.leftMouseClick(INPUT);

    int mouseX = INPUT.getAbsoluteMouseX();
    int mouseY = INPUT.getAbsoluteMouseY();

    if (isVisible() && !BlueSaga.actionServerWait) {

      // CHECK WHICH BOX IS CLICKED
      for (int i = 0; i < SizeW; i++) {
        for (int j = 0; j < SizeH; j++) {

          // IF BOX IS CLICKED, CHECK IF PICKUP OR DROP
          if (itemBoxes.get(i + "," + j).isSelected(mouseX, mouseY, moveX, moveY)) {
            if (!BlueSaga.actionServerWait) {

              if (Id.startsWith("Shop") && Gui.MouseItem.getItem() != null) {
                // SELL ITEM
                //	BlueSaga.client.sendMessage("sell", "Mouse,"+Gui.MouseItem.getItem().getId());
                //	BlueSaga.actionServerWait = true;
              } else if (Name.equals("Closet")) {
                if (itemBoxes.get(i + "," + j).getItem() != null) {
                  if (itemBoxes.get(i + "," + j).getItem().getId() == 214) {
                    WebUtils.openWebpage("http://www.bluesaga.org/shop/");
                  }
                }
              } else if (Gui.MouseItem.getItem() != null) {
                // former moveitem
                // PLACE ITEM IN CONTAINER
                if (Name.equals("Personal Chest")) {
                  // ADD ITEM TO PERSONAL CHEST
                  // ItemId; posX; posY
                  BlueSaga.client.sendMessage(
                      "moveitem", Gui.MouseItem.getItem().getId() + ";" + i + ";" + j + ";1");
                  Gui.MouseItem.clear();
                  BlueSaga.actionServerWait = true;
                } else {
                  BlueSaga.client.sendMessage("container_placeitem", Id + ";" + i + "," + j);
                  Gui.MouseItem.clear();
                  BlueSaga.actionServerWait = true;
                }
              } else if (!Id.startsWith("Shop")
                  && Gui.MouseItem.isEmpty()
                  && !itemBoxes.get(i + "," + j).isEmpty()) {
                // PICK UP ITEM TO MOUSE
                if (Name.equals("Personal Chest")) {
                  if (itemBoxes.get(i + "," + j).getItem().getId() == 213) {
                    WebUtils.openWebpage("http://www.bluesaga.org/shop/tools.php");
                  } else {
                    // PICK UP ITEM FROM PERSONAL CHEST
                    itemBoxes.get(i + "," + j).clear();
                    BlueSaga.client.sendMessage("addmouseitem", i + ";" + j + ";1");
                    BlueSaga.actionServerWait = true;
                  }
                } else {
                  // PICK UP FROM OTHER CONTAINERS
                  BlueSaga.client.sendMessage("container_addmouseitem", Id + ";" + i + "," + j);
                  BlueSaga.actionServerWait = true;
                }
              }
            }
          }
        }
      }

      clickedOn(mouseX, mouseY);
    }
  }

  // USE ITEM/SELL ITEM IN INV, FAST PICKUP FROM CONTAINER, BUY ITEM FROM SHOP

  @Override
  public void rightMouseClick(Input INPUT) {
    super.rightMouseClick(INPUT);

    int mouseX = INPUT.getAbsoluteMouseX();
    int mouseY = INPUT.getAbsoluteMouseY();

    if (isVisible()) {
      boolean clickedBox = false;

      for (int i = 0; i < SizeW; i++) {
        for (int j = 0; j < SizeH; j++) {

          // IF BOX IS CLICKED, CHECK ACTIONS
          if (itemBoxes.get(i + "," + j).isSelected(mouseX, mouseY, moveX, moveY)) {
            clickedBox = true;
            if (Name.equals("Closet")) {
              if (!itemBoxes.get(i + "," + j).isEmpty()) {
                BlueSaga.client.sendMessage(
                    "set_skin", "" + itemBoxes.get(i + "," + j).getItem().getId());
              }
            } else {
              if (!itemBoxes.get(i + "," + j).isEmpty()) {
                BlueSaga.client.sendMessage(
                    "fastloot",
                    "" + ContainerX + "," + ContainerY + "," + ContainerZ + "," + i + "," + j);
              }
            }
          }
          if (clickedBox) {
            break;
          }
        }
        if (clickedBox) {
          break;
        }
      }
    }
  }

  @Override
  public void stopMove() {
    if (moveWithMouse) {
      for (int i = 0; i < SizeW; i++) {
        for (int j = 0; j < SizeH; j++) {
          itemBoxes.get(i + "," + j).updatePos(moveX, moveY);
        }
      }
    }
    super.stopMove();
  }

  public DropBox getBox(int x, int y) {
    return itemBoxes.get(x + "," + y);
  }

  public int getContainerX() {
    return ContainerX;
  }

  public void setContainerX(int containerX) {
    ContainerX = containerX;
  }

  public int getContainerY() {
    return ContainerY;
  }

  public void setContainerY(int containerY) {
    ContainerY = containerY;
  }

  public int getContainerZ() {
    return ContainerZ;
  }

  public void setContainerZ(int containerZ) {
    ContainerZ = containerZ;
  }

  @Override
  public String getName() {
    return Name;
  }

  @Override
  public void setName(String name) {
    Name = name;
  }
}
