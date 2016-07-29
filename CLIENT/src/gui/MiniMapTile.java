package gui;

import org.newdawn.slick.Color;

public class MiniMapTile {

	private Color Color;
	private boolean New = false;
	private boolean Update = false;
	
	public MiniMapTile(Color newColor){
		setColor(newColor);
	}

	public org.newdawn.slick.Color getColor() {
		return Color;
	}

	public void setColor(org.newdawn.slick.Color color) {
		Color = color;
	}

	public boolean isNew() {
		return New;
	}

	public void setNew(boolean new1) {
		New = new1;
	}

	public boolean isUpdate() {
		return Update;
	}

	public void setUpdate(boolean update) {
		Update = update;
	}
}
