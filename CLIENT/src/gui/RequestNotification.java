package gui;

import game.BlueSaga;
import graphics.BlueSagaColors;

import org.newdawn.slick.Graphics;

public class RequestNotification extends Notification {

	private Button acceptButton;
	private Button declineButton;
	
	private String requestType;
	private int requestId;
	
	public RequestNotification(String newText, String requestType, int requestId, int newY) {
		super(newText, newY, BlueSagaColors.RED);
		
		this.requestType = requestType;
		this.requestId = requestId;
		
		startedTimer = true;
		showTimer.cancel();
		acceptButton = new Button("Accept", 920, y+10, 70, 32, null);
		declineButton = new Button("Decline", 920, y+45, 70, 32, null);
		
		justifiedText = justifyLeft(20,newText);
		
		request = true;
	}
	
	public void draw(Graphics g){
		super.draw(g);
		
		int mouseX = BlueSaga.INPUT.getAbsoluteMouseX();
		int mouseY = BlueSaga.INPUT.getAbsoluteMouseY();
		
		if(!fade){
			acceptButton.draw(g, mouseX, mouseY);
			declineButton.draw(g, mouseX, mouseY);
		}
			
	}

	public boolean clickedAccept(int mouseX, int mouseY){
		if(acceptButton.isClicked(mouseX, mouseY)){
			// Send accept
			
			if(!fade){
				if(requestType.equals("join_party")){
					BlueSaga.client.sendMessage("join_party",""+requestId);
				}	
			}
			
			fade = true;
			return true;
		}
		return false;
	}
	
	public boolean clickedDenied(int mouseX, int mouseY){
		if(declineButton.isClicked(mouseX, mouseY)){
			fade = true;
			return true;
		}
		return false;
	}
	
}