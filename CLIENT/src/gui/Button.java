package gui;

import graphics.BlueSagaColors;
import graphics.Font;
import graphics.ImageResource;
import gui.windows.Window;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;

import sound.Sfx;

public class Button {

  private int Width;
  private int Height;
  private String Label;
  protected int X;
  protected int Y;
  private boolean hover;
  private boolean selected = false;
  private Image imageButton;
  private Image imageButtonActive;
  private boolean isImage;
  protected Window ParentWindow;

  private Color idleColor = BlueSagaColors.RED;
  private Color hoverColor = BlueSagaColors.YELLOW;

  private boolean Visible;

  private ToolTip ToolTip;

  public Button(String label, int x, int y, int width, int height, Window parent) {
    ParentWindow = parent;
    Label = label;
    X = x;
    Y = y;
    Width = width;
    Height = height;
    hover = false;
    isImage = false;

    setVisible(true);
    setToolTip(new ToolTip());
  }

  public Button(String label, int x, int y, Window parent) {
    ParentWindow = parent;
    Label = label;
    X = x;
    Y = y;
    Width = Font.size12.getWidth(Label) + 30;
    Height = 35;
    hover = false;
    isImage = false;

    setVisible(true);
    setToolTip(new ToolTip());
  }

  public void setImage(String path) {
    imageButton = ImageResource.getSprite(path).getImage();

    if (ImageResource.getSprite(path + "_active") != null) {
      imageButtonActive = ImageResource.getSprite(path + "_active").getImage();
    }
    isImage = true;
  }

  public void draw(Graphics g, int mouseX, int mouseY) {
    int moveX = 0;
    int moveY = 0;

    if (isVisible()) {

      if (ParentWindow != null) {
        moveX = ParentWindow.X + ParentWindow.getMoveX();
        moveY = ParentWindow.Y + ParentWindow.getMoveY();
      }

      if (isImage) {
        if ((isClicked(mouseX, mouseY) || selected) && imageButtonActive != null) {
          imageButtonActive.draw(X + moveX, Y + moveY);
        } else {
          imageButton.draw(X + moveX, Y + moveY);
        }
      } else {

        if (isClicked(mouseX, mouseY) || selected) {
          g.setColor(hoverColor.darker(0.2f));
        } else {
          g.setColor(idleColor.darker(0.2f));
        }

        g.fillRoundRect(X + moveX, Y + moveY, Width, Height, 10);

        if (isClicked(mouseX, mouseY) || selected) {
          g.setColor(hoverColor);
        } else {
          g.setColor(idleColor);
        }
        g.fillRoundRect(X + 5 + moveX, Y + 5 + moveY, Width - 10, Height - 10, 8);

        if (!Label.equals("")) {
          g.setFont(Font.size12);

          if (isClicked(mouseX, mouseY) || selected) {
            g.setColor(new Color(0, 0, 0, 255));
          } else {
            g.setColor(new Color(255, 255, 255, 255));
          }

          int labelWidth = Font.size12.getWidth(Label);

          g.drawString(Label, X + Width / 2 - labelWidth / 2 + moveX, Y + 10 + moveY);
        }

        // Shine
        /*
        g.setWorldClip(X+moveX, Y+moveY, Width, Height/2);
        g.setColor(new Color(255,255,255,60));
        g.fillRoundRect(X+moveX, Y+moveY, Width, Height,10);
        g.clearWorldClip();
        */

      }
    }

    if (getToolTip().getText() != "") {
      if (isClicked(mouseX, mouseY)) {
        if (!getToolTip().isActive()) {
          getToolTip().setActive(true);
        }
        getToolTip().draw(g, X + moveX, Y + Height + 5 + moveY);
      } else if (getToolTip().isActive()) {
        getToolTip().setActive(false);
      }
    }
  }

  public boolean isClicked(int mouseX, int mouseY) {
    int moveX = 0;
    int moveY = 0;

    if (ParentWindow != null) {
      moveX = ParentWindow.X + ParentWindow.getMoveX();
      moveY = ParentWindow.Y + ParentWindow.getMoveY();
    }

    if (mouseX > X + moveX
        && mouseX < X + Width + moveX
        && mouseY > Y + moveY
        && mouseY < Y + Height + moveY) {
      if (!hover) {
        Sfx.play("gui/menu_select");
        hover = true;
      }
      return true;
    }
    hover = false;
    return false;
  }

  public void toggle() {
    if (selected) {
      selected = false;
    } else {
      selected = true;
    }
  }

  public void click() {
    Sfx.play("gui/menu_confirm2");
  }

  public void select() {
    selected = true;
  }

  public void deselect() {
    selected = false;
  }

  public void setSelected(boolean newState) {
    selected = newState;
  }

  public boolean isSelected() {
    return selected;
  }

  public int getY() {
    return Y;
  }

  public int getX() {
    return X;
  }

  public int getTotalX() {
    return ParentWindow.X + ParentWindow.getMoveX() + X;
  }

  public int getTotalY() {
    return ParentWindow.Y + ParentWindow.getMoveY() + Y;
  }

  public void setLabel(String newLabel) {
    Label = newLabel;
  }

  public boolean isVisible() {
    return Visible;
  }

  public void setVisible(boolean visible) {
    Visible = visible;
  }

  public ToolTip getToolTip() {
    return ToolTip;
  }

  public void setToolTip(ToolTip toolTip) {
    ToolTip = toolTip;
  }

  public void setY(int newY) {
    Y = newY;
  }

  public void setX(int newX) {
    X = newX;
  }

  public Color getIdleColor() {
    return idleColor;
  }

  public void setIdleColor(Color idleColor) {
    this.idleColor = idleColor;
  }

  public Color getHoverColor() {
    return hoverColor;
  }

  public void setHoverColor(Color hoverColor) {
    this.hoverColor = hoverColor;
  }
}
