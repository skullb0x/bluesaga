package map;

import game.BP_EDITOR;
import gui.Font;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import components.Creature;

public class Tile {
	private int X;
	private int Y;
	private int Z;
	
	private String Type = "none";
	private String Name = "none";
	private boolean Passable;
	
	private boolean Animated;
	
	private Creature Occupant;
	
	private int AreaEffectId = 0; 
	
	// DOOR
	
	private int doorId;
	private int graphicsNr;
	private int areaId;
	private int entranceX;
	private int entranceY;
	
	private boolean MENU = false;
	
	// CONTAINER
	private boolean LootBag = false;	
	
	private int TriggerId = 0;
	
	
	public Tile(int x, int y, int z){
		X = x;
		Y = y;
		setZ(z);
	}
	
	
	public boolean setType(String type, String newName, boolean passable){
		boolean changed = true;
		
		if(newName.contains("_")){
			String fileName[] = newName.split("_");
			newName = fileName[0];
		}
		TriggerId = 0;
		
		AreaEffectId = 0;
		
		if(!type.equals("none") && BP_EDITOR.GFX.getSprite("textures/"+type+"/"+newName) == null){
			changed = false;
		}else{
			if(Type.equals(type) && Name.equals(newName)){
				changed = false;
			}else{
				Type = type;
				Name = newName;
				
				Occupant = null;
				
				
				//isDoor = false;
				
				Passable = passable;
				
				if(Type.equals("none")){
					Animated = false;
				}else{
					if(BP_EDITOR.GFX.getSprite("textures/"+Type+"/"+Name).isAnimated()){
						Animated = true;
					}else{
						Animated = false;
					}
				}
			}
		}
		
		setMENU(false);
		return changed;
	}
	
	public void restartAnimation() {
		if(BP_EDITOR.GFX.getSprite("textures/"+Type+"/"+Name).isAnimated()){
			BP_EDITOR.GFX.getSprite("textures/"+Type+"/"+Name).getAnimation().restart();
		}
	}

	
	public void updateAnimation(){
		if(BP_EDITOR.GFX.getSprite("textures/"+Type+"/"+Name).isAnimated()){
			BP_EDITOR.GFX.getSprite("textures/"+Type+"/"+Name).getAnimation().updateNoDraw();
		}
	}
	
	
	
	public void draw(Graphics g, int x, int y){
		int alpha = 255;
		
		if(!Type.equals("none") && !MENU){
			alpha = 255 - Math.abs((Z-BP_EDITOR.PLAYER_Z)*245);
		}
		
		if(Type.equals("none")){
			//BP_EDITOR.GFX.getSprite("textures/cave/cave0").draw(x, y);
		}else{
			BP_EDITOR.GFX.getSprite("textures/"+Type+"/"+Name).draw(x, y,new Color(255,255,255,alpha));
			if(doorId > 0 && Z == BP_EDITOR.PLAYER_Z){
				g.setColor(new Color(0,0,255,100));
				g.fillRect(x, y, 50, 50);
				g.setColor(new Color(0,0,255));
				g.setFont(Font.size12);
				int textWidth = Font.size12.getWidth(""+doorId);
				g.drawString(""+doorId, x-textWidth/2 + 25, y+20);
			}
		}
		
		if(Occupant != null && Z == BP_EDITOR.PLAYER_Z){
			Occupant.draw(g, x, y);
			
		}
		
		
		if(BP_EDITOR.SHOW_PASSABLE && !Passable && !Type.equals("none") && !MENU){
			g.setColor(new Color(255,0,0,alpha - 200));
			g.fillRect(x, y, 50, 50);
		}
		
		if(BP_EDITOR.PLAYER_Z == Z){
			if(getAreaEffectId() > 0){
				g.setColor(new Color(255,104,235));
				g.drawRect(x, y, 49, 49);
				g.setFont(Font.size12bold);
				g.drawString(""+getAreaEffectId(), x + 20, y + 20);
			}
		}
		
		if(getTriggerId() > 0){
			g.setColor(new Color(0,255,0));
			g.drawRect(x, y, 49, 49);
			g.setFont(Font.size12bold);
			g.drawString(""+getTriggerId(), x + 20, y + 20);
		}
		
		
	}
	
	

		
	/****************************************
     *                                      *
     *         CONTAINER		            *
     *                                      *
     *                                      *
     ****************************************/
	
	
	public void setLootBag(boolean state){
		LootBag = state;
	}
	
	public boolean getLootBag(){
		return LootBag;
	}
	
	/****************************************
     *                                      *
     *         DOOR DATA		            *
     *                                      *
     *                                      *
     ****************************************/
	
	
	

	public int getGraphicsNr() {
		return graphicsNr;
	}
	
	public int getAreaId() {
		return areaId;
	}
	
	public int getEntranceX() {
		return entranceX;
	}
	
	public int getEntranceY() {
		return entranceY;
	}

	/****************************************
     *                                      *
     *         GETTER / SETTER				*
     *                                      *
     *                                      *
     ****************************************/
	
	public boolean isAnimated() {
		return Animated;
	}
	
	
	public void setOccupant(Creature m) {
		Occupant = m;
	}
	
	public String getName() {
		return Name;
	}
	
	public void setName(String newName) {
		Name = newName;
	}
	
	public String getType() {
		return Type;
	}
	
	public int getDoorId() {
		return doorId;
	}
	
	public boolean isPassable(){
		return Passable;
	}
	
	public void setPassable(boolean newPassable){
		Passable = newPassable;
	}
	
	public int getX(){
		return X;
	}
	
	public int getY(){
		return Y;
	}
	
	/*
	public boolean isDoor(){
		return isDoor;
	}
	
	public void setDoor(boolean newStatus){
		isDoor = newStatus;
	}
	*/
	
	public void setDoorId(int newDoorId){
		doorId = newDoorId;
	}


	public int getZ() {
		return Z;
	}


	public void setZ(int z) {
		Z = z;
	}


	public boolean isMENU() {
		return MENU;
	}


	public void setMENU(boolean mENU) {
		MENU = mENU;
	}


	public int getAreaEffectId() {
		return AreaEffectId;
	}


	public void setAreaEffectId(int areaEffectId) {
		AreaEffectId = areaEffectId;
	}


	public int getTriggerId() {
		return TriggerId;
	}


	public void setTriggerId(int triggerId) {
		TriggerId = triggerId;
	}


	
}

