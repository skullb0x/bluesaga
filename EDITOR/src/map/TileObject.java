package map;

import game.BP_EDITOR;
import graphics.Sprite;
import gui.Font;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

public class TileObject {

  private String name;

  private Sprite graphics;
  private int width = 1;
  private int height = 1;

  private int Z;
  private boolean MENU = false;

  private int TrapId = 0;

  public TileObject(String newName) {
    name = newName;
    graphics = BP_EDITOR.GFX.getSprite("objects/" + name);

    if (graphics.isAnimated()) {
      width = graphics.getAnimation().getWidth() / 50;
      height = graphics.getAnimation().getHeight() / 50;
    } else {
      width = graphics.getImage().getWidth() / 50;
      height = graphics.getImage().getHeight() / 50;
    }

    //ServerMessage.printMessage("TILE OBJECT W,H: "+width+","+height);
  }

  public void draw(Graphics g, int x, int y) {
    int alpha = 255;

    /*
    if(!MENU){
    	int diffZ = Z-BP_EDITOR.PLAYER_Z;
    	if(diffZ < 0){
    		diffZ = 0;
    	}
    	alpha = 255 - diffZ*200;
    }
    */
    if (MENU || BP_EDITOR.PLAYER_Z == Z) {
      graphics.draw(x - (width - 1) * 25, y - ((height - 1) * 50), new Color(255, 255, 255, alpha));
    }

    if (getTrapId() > 0) {
      g.setColor(new Color(255, 0, 0));
      g.drawRect(x, y, 49, 49);
      g.setFont(Font.size12bold);
      g.drawString("" + getTrapId(), x + 20, y + 20);
    }
  }

  public Sprite getSprite() {
    return graphics;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public String getName() {
    return name;
  }

  public int getZ() {
    return Z;
  }

  public void setZ(int z) {
    Z = z;
  }

  public void setMENU(boolean newValue) {
    MENU = newValue;
  }

  public boolean isMENU() {
    return MENU;
  }

  public int getTrapId() {
    return TrapId;
  }

  public void setTrapId(int trapId) {
    TrapId = trapId;
  }
}
