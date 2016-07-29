package screens;


import game.BlueSaga;
import game.ClientSettings;
import graphics.BlueSagaColors;
import graphics.Font;
import graphics.ImageResource;
import gui.Button;
import gui.TextField;
import screens.ScreenHandler.ScreenType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;

import sound.Sfx;
import utils.Encryption;
import utils.LanguageUtils;
import utils.WebUtils;

public class LoginScreen {
	private static TextField mail_field;
	private static TextField password_field;

	private static String statusMessage;
	
	private static int X;
	private static int Y;
	
	private static Button RegisterButton;
	private static Button LoginButton;
	private static Button ForgotButton;
	
	
	static Timer focusTimer = new Timer();
    
	private static int retryCountdown = 0;
	private static Timer retryCountdownTimer = new Timer();
	
	public static boolean clickedLogin = false;
	
	public static void init(GameContainer app, int x, int y) {
		X = x;
		Y = y;
				
		statusMessage = LanguageUtils.getString("ui.login.instructions");
		
		mail_field = new TextField(app, Font.size12, X+5, Y, 180, 30);
		mail_field.setBackgroundColor(new Color(0,0,0,0));
		mail_field.setBorderColor(new Color(0,0,0,0));
		mail_field.setTextColor(new Color(255,255,255,255));
		mail_field.setFocus(true);
		
		password_field = new TextField(app, Font.size12, X+215, Y, 180, 30);
		password_field.setBackgroundColor(new Color(0,0,0,0));
		password_field.setBorderColor(new Color(0,0,0,0));
		password_field.setTextColor(new Color(255,255,255,255));
		password_field.setFocus(false);
		password_field.setMaskCharacter('*');
		password_field.setMaskEnabled(true);
		
		// SHOW LAST MAIL IN LOGIN FIELD
		ResultSet loginMail = BlueSaga.gameDB.askDB("select GameValue from option where GameOption = 'Mail'");
		try {
			if(loginMail.next()){
				if(loginMail.getString("GameValue") != null && !loginMail.getString("GameValue").equals("")){
					mail_field.setText(loginMail.getString("GameValue"));
					mail_field.setCursorPos(loginMail.getString("GameValue").length());
					password_field.setFocus(true);
				}
			}
			loginMail.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		LoginButton = new Button(LanguageUtils.getString("ui.login.enter").toUpperCase(), 410, 360,Font.size12.getWidth(LanguageUtils.getString("ui.login.enter")) + 60, 35, null);
		RegisterButton = new Button(LanguageUtils.getString("ui.login.create").toUpperCase(), 398, 500,Font.size12.getWidth(LanguageUtils.getString("ui.login.create")) + 60, 35, null);
		ForgotButton = new Button(LanguageUtils.getString("ui.login.forgot"), 418, 550,Font.size12.getWidth(LanguageUtils.getString("ui.login.forgot")) + 60, 35, null);
	}
	
	public static void setStatusMessage(String newMessage){
		statusMessage = newMessage;
	}
	
	public static String getMail() {
		return mail_field.getText();
	}
	
	public String getPassword() {
		return password_field.getText();
	}
	
	public static void changeField() {
		if(mail_field.hasFocus()){
			mail_field.setFocus(false);
			password_field.setFocus(true);
		}else{
			mail_field.setFocus(true);
			password_field.setFocus(false);
		}
	}
	
	public static boolean hasEmptyFields() {
		if(mail_field.getText().isEmpty() || password_field.getText().isEmpty()){
			return true;
		}
	
		return false;
	}
	
	
	
	public static boolean goBack(){
		return true;
	}
	
	
	public static void keyLogic(Input INPUT){
		if(INPUT.isKeyPressed(Input.KEY_TAB)){
 			changeField();
 		}else if(INPUT.isKeyPressed(Input.KEY_ENTER)){
			if(hasEmptyFields()){
 				setStatusMessage(LanguageUtils.getString("ui.login.fill_both"));
 			}else{
 				if(!clickedLogin){
 					ScreenHandler.setLoadingStatus(LanguageUtils.getString("ui.status.connecting"));
 					
 					BlueSaga.chooseServer("AWS");
 		 		}
 			}
		}else if(INPUT.isKeyPressed(Input.KEY_ESCAPE)){
			if(goBack()){
    			System.exit(0);
			}
		}
		
		if(INPUT.isMousePressed(0)){
			int mouseX = INPUT.getAbsoluteMouseX();
			int mouseY = INPUT.getAbsoluteMouseY();
			
			if(ScreenHandler.getActiveScreen() == ScreenType.LOGIN){
				if(RegisterButton.isClicked(mouseX,mouseY)){
					
				}else if(LoginButton.isClicked(mouseX,mouseY)){
	 				if(!clickedLogin){
	 					ScreenHandler.setLoadingStatus(LanguageUtils.getString("ui.status.connecting"));
	 					
	 					BlueSaga.chooseServer("AWS");
	 	 			}
				}else if(ForgotButton.isClicked(mouseX,mouseY)){
					WebUtils.openWebpage("http://www.bluesaga.org/forgot.php");
				}
			}
		}
	}
	
	
	public static void draw(Graphics g, GameContainer app){
	
		int mouseX = app.getInput().getAbsoluteMouseX();
		int mouseY = app.getInput().getAbsoluteMouseY();
		
		
		if(ScreenHandler.getActiveScreen() == ScreenType.LOGIN){
			Font.size12.addGlyphs(mail_field.getText());
			
			g.setFont(Font.size12);
			
			// LOGIN FIELDS
			g.setColor(new Color(255,255,255,100));
			g.fillRoundRect(X-10, Y-12, 200, 40, 10);
			g.setColor(new Color(0,0,0,150));
			g.fillRoundRect(X-5, Y-7, 190, 30, 8);
			g.setColor(new Color(255,255,255,255));
			g.drawString(LanguageUtils.getString("ui.login.mail"), X, Y-30);
			mail_field.render(app, g);
			
			g.setColor(new Color(255,255,255,100));
			g.fillRoundRect(X+200, Y-12, 200, 40, 10);
			g.setColor(new Color(0,0,0,150));
			g.fillRoundRect(X+205, Y-7, 190, 30, 8);
			g.setColor(new Color(255,255,255,255));
			g.drawString(LanguageUtils.getString("ui.login.password"), X+210, Y-30);
			password_field.render(app, g);
			
			g.setColor(new Color(0,0,0,240));
			
			if(retryCountdown > 0){
				g.drawString(statusMessage+", "+LanguageUtils.getString("ui.login.retry")+" "+retryCountdown+"s", X + 190 -Font.size12.getWidth(statusMessage+", "+LanguageUtils.getString("ui.login.retry")+" "+retryCountdown+"s")/2, Y+40);
			}else{
				g.drawString(statusMessage, X + 190 -Font.size12.getWidth(statusMessage)/2, Y+40);
			}
			
		
			LoginButton.draw(g, mouseX, mouseY);
			RegisterButton.draw(g, mouseX, mouseY);
			ForgotButton.draw(g, mouseX, mouseY);
		}
	
		g.setColor(new Color(255,255,255,255));
		g.drawString("v 0."+ClientSettings.VERSION_NR, 930, 610);
	
	}
	
	public static void login(){
		// SAVE LOGIN MAIL
		if(!ClientSettings.DEV_MODE){
			BlueSaga.gameDB.updateDB("update option set GameValue = '"+mail_field.getText()+"' where GameOption like 'Mail'");
		}
		
		// ENCODE PASSWORD
		String encryptedPassword = Encryption.encryptPassword(mail_field.getText(),password_field.getText());
	    BlueSaga.client.sendMessage("login", getMail()+";"+encryptedPassword);
     	setStatusMessage(LanguageUtils.getString("ui.login.check"));
    	
     	Sfx.play("gui/menu_confirm2");
	}
	
	public static void restartCountdown(){
		if(retryCountdown > 0){
			retryCountdownTimer.cancel();
		}
		retryCountdown = 9;
		
		retryCountdownTimer = new Timer();
		retryCountdownTimer.schedule( new TimerTask(){
	        @Override
			public void run() {
	        	countdown();
			}
	      }, 1000);
	}
	
	public static void countdown(){
		if(retryCountdown > 0){
    		retryCountdown--;
    		if(retryCountdown > 0){
	    		retryCountdownTimer.schedule( new TimerTask(){
	    	        @Override
	    			public void run() {
	    	        	countdown();
	    			}
	    	      }, 1000);
    		}else{
    			clickedLogin = false;
    		}
    	}else{
    		clickedLogin = true;
    	}
		
	}
	
	public boolean checkMail(String mail){
		Pattern p = Pattern.compile(".+@.+\\.[a-z]+");
		Matcher m = p.matcher(mail);
		boolean matchFound = m.matches();

		if(matchFound){
			return true;
		}
		return false;
	}
	
	
	
	public static void unfocusFields(){
		mail_field.setFocus(false);
		password_field.setFocus(false);
		
		mail_field.setAcceptingInput(false);
		password_field.setAcceptingInput(false);
	}
	
	public static void focusLoginField(){
		mail_field.setAcceptingInput(true);
		password_field.setAcceptingInput(true);
		password_field.setFocus(true);
	}
	
	
}
