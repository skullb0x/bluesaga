package gui.windows;

import graphics.ImageResource;
import gui.Gui;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;

public class Window {
	
	private String Name;
	public int X;
	public int Y;
	
	private int DepthZ;
	
	protected int moveX;
	protected int moveY;
	
	// Real position of window: X+moveX, Y+moveY
	public int realX;
	public int realY;
	
	
	protected int Width;
	protected int Height;
	protected float aniWidth;
	protected float aniHeight;
	
	private float dW;
	private float dH;
	
	private boolean Open;
	private boolean Visible;
	private boolean FullyOpened;
	
	private Image ulCorner;
	private Image dlCorner;
	private Image urCorner;
	private Image drCorner;
	
	private float OPENING_SPEED = 40.0f;
	
	private boolean Movable;
	protected boolean moveWithMouse;
	private int moveStartMouseX;
	private int moveStartMouseY;
	
	private Color BORDER_COLOR;
	private Color BG_COLOR;
	
	private Image closeButton;
	private boolean showCloseButton;
	
	private boolean HasTextInput = false;
	
	public Window(String newName, int x, int y, int width, int height, boolean ShowCloseButton){
		setName(newName);
		X = x;
		Y = y;
		setDepthZ(0);
		
		moveX = 0;
		moveY = 0;
		
		Width = width;
		Height = height;
		
		showCloseButton = ShowCloseButton;
		
		aniWidth = 0;
		aniHeight = 0;
		
		float openSpeed = OPENING_SPEED * ((float) Height/150);
		
		dW = ((float) Width / (float) Height) * openSpeed;
		dH = openSpeed;
		
		Open = false;
		setVisible(false);
		setFullyOpened(false);
		
		ulCorner = ImageResource.getSprite("gui/menu/window_ulcorner").getImage();
		urCorner = ImageResource.getSprite("gui/menu/window_urcorner").getImage();
		dlCorner = ImageResource.getSprite("gui/menu/window_dlcorner").getImage();
		drCorner = ImageResource.getSprite("gui/menu/window_drcorner").getImage();
		
		moveWithMouse = false;
		setMovable(true);
		
		BORDER_COLOR = new Color(164,42,42,255);
		BG_COLOR = new Color(236,86,86,255);
		closeButton = ImageResource.getSprite("gui/menu/close_button").getImage();
	}
	
	public void setPos(int newX, int newY){
		X = newX;
		Y = newY;
	}
	
	public void setSize(int newWidth, int newHeight){
		Width = newWidth;
		Height = newHeight;
		
		aniWidth = newWidth;
		aniHeight = newHeight;
		
		float openSpeed = OPENING_SPEED * ((float) Height/150);
		
		dW = ((float) Width / (float) Height) * openSpeed;
		dH = openSpeed;
		
		
		
	}
	
	public void draw(GameContainer app, Graphics g, int mouseX, int mouseY){
		if(isVisible()){
			if(isMovable() && moveWithMouse){
				moveX = mouseX - moveStartMouseX;
				moveY = mouseY - moveStartMouseY;
			}
			
			realX = X + moveX;
			realY = Y + moveY;
			
			
			if(aniWidth > 0 || aniHeight > 0){
				// DARK BORDER
				g.setColor(BORDER_COLOR);
				g.fillRect(Math.round((Width - aniWidth)/2 + realX), Math.round((Height - aniHeight)/2 + 8 + realY), Math.round(aniWidth), Math.round(aniHeight - 16));
				g.fillRect(Math.round((Width - aniWidth)/2+8 + realX), Math.round((Height - aniHeight)/2 + realY), Math.round(aniWidth-16), Math.round(aniHeight));
				
				// LIGHTER COLOR
				g.setColor(BG_COLOR);
				g.fillRoundRect(Math.round(realX + (Width - aniWidth)/2 + 4), Math.round(realY + (Height - aniHeight)/2 + 4), Math.round(aniWidth - 8), Math.round(aniHeight - 8), 5);
				
				ulCorner.draw(Math.round(realX + (Width - aniWidth)/2), Math.round(realY + (Height - aniHeight)/2), BORDER_COLOR);
				dlCorner.draw(Math.round(realX + (Width - aniWidth)/2), Math.round(realY + (Height + aniHeight)/2 - 12), BORDER_COLOR);
				
				urCorner.draw(Math.round(realX + Width - (Width - aniWidth)/2 - 12), Math.round(realY + (Height - aniHeight)/2), BORDER_COLOR);
				drCorner.draw(Math.round(realX + Width - (Width - aniWidth)/2 - 12), Math.round(realY + (Height + aniHeight)/2 - 12), BORDER_COLOR);
			}
			
			if(Open){
				if(aniWidth < Width){
					aniWidth += dW;
				}
				if(aniHeight < Height){
					aniHeight += dH;
				}
				if(aniWidth >= Width && aniHeight >= Height && !FullyOpened){
					aniWidth = Width;
					aniHeight = Height;
					FullyOpened = true;
				}
			}else{
				if(aniWidth > 0){
					aniWidth -= dW;
				}
				if(aniHeight > 0){
					aniHeight -= dH;
				}
				
				if(aniWidth <= 0 && aniHeight <= 0){
					aniWidth = 0;
					aniHeight = 0;
					Visible = false;
					stopMove();
				}
			}
			if(FullyOpened && showCloseButton){
				closeButton.draw(realX+ Width - 35,realY + 10);
			}
		}
	}
	
	public void open(){
		Visible = true;
		Open = true;
		Gui.MaxWindowsZ++;
		DepthZ = Gui.MaxWindowsZ;
		Gui.sortWindows();
	}
	
	public void close(){
		Open = false;
		FullyOpened = false;
	}
	
	public void toggle(){
		if(Open){
			close();
		}else{
			open();
		}
	}
	
	public void keyLogic(Input INPUT){
	
	}
	
	
	public void leftMouseClick(Input INPUT){
		if(isOpen()){
			if(showCloseButton && INPUT.getAbsoluteMouseX() > X + moveX + Width - 35 
				&& INPUT.getAbsoluteMouseX() <  X + moveX + Width 
				&& INPUT.getAbsoluteMouseY() > Y + moveY + 10 
				&& INPUT.getAbsoluteMouseY() < Y + moveY + 35){
				close();
			}else if (clickedOn(INPUT.getAbsoluteMouseX(), INPUT.getAbsoluteMouseY())) {
				Gui.MaxWindowsZ++;
				DepthZ = Gui.MaxWindowsZ;
			}
		}
	}
	
	public void rightMouseClick(Input INPUT){
		if(clickedOn(INPUT.getAbsoluteMouseX(),INPUT.getAbsoluteMouseY())){
			Gui.MaxWindowsZ++;
			DepthZ = Gui.MaxWindowsZ;
		}
	}
	
	public boolean isOpen(){
		return Open;
	}
	
	
	
	public int getMoveX(){
		return moveX;
	}
	
	public int getMoveY(){
		return moveY;
	}


	public boolean isVisible() {
		return Visible;
	}


	public void setVisible(boolean visible) {
		Visible = visible;
	}


	public boolean isFullyOpened() {
		return FullyOpened;
	}


	public void setFullyOpened(boolean fullyOpened) {
		FullyOpened = fullyOpened;
	}
	
	public void setWidth(int newWidth){
		Width = newWidth;
	}
	
	public void setHeight(int newHeight){
		Height = newHeight;
	}
	
	
	public boolean clickedOn(int mouseX, int mouseY){
		boolean clickedOn = false;
		if(isVisible()){
			if(mouseX > X + moveX && mouseX < X + Width + moveX && mouseY > Y + moveY && mouseY < Y + Height + moveY){
				if(isMovable() && !moveWithMouse){
					Gui.MovingWindows = true;
					moveWithMouse = true;
					moveStartMouseX = mouseX;
					moveStartMouseY = mouseY;
				}
				
				clickedOn = true;
			}
		}
		return clickedOn;
	}
	
	public void stopMove(){
		if(isMovable()){
			moveWithMouse = false;
			X += moveX;
			Y += moveY;
			moveX = 0;
			moveY = 0;
		}
	}
	
	
	public void setBorderColor(Color newColor){
		BORDER_COLOR = newColor;
	}
	
	public void setBgColor(Color newColor){
		BG_COLOR = newColor;
	}

	public int getDepthZ() {
		return DepthZ;
	}

	public void setDepthZ(int depthZ) {
		DepthZ = depthZ;
	}

	public boolean hasTextInput() {
		return HasTextInput;
	}

	public void setTextInput(boolean textInput) {
		HasTextInput = textInput;
	}

	public String getName() {
		return Name;
	}

	public void setName(String name) {
		Name = name;
	}

	public boolean isMovable() {
		return Movable;
	}

	public void setMovable(boolean movable) {
		Movable = movable;
	}
	
	
	public void setX(int newValue){
		X = newValue;
	}
	public void setY(int newValue){
		Y = newValue;
	}
	
	
	
}
