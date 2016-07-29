package network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Timer;
import java.util.TimerTask;

import utils.Obfuscator;
import utils.ServerMessage;
import utils.TimeUtils;
import creature.PlayerCharacter;
import data_handlers.ConnectHandler;
import data_handlers.DataHandlers;
import data_handlers.Handler;
import data_handlers.Message;
import data_handlers.battle_handler.BattleHandler;
import data_handlers.battle_handler.PvpHandler;
import data_handlers.party_handler.PartyHandler;

public class Client implements Runnable {

	private Socket csocket;

	private int index;

	public String IP;
	
	public boolean sendingData = false;
	public ObjectOutputStream out;
	private ObjectInputStream in;

	public int inputPacketId = 10000;

	private String message;
	private Timer logoutTimer = new Timer();

	public int UserId;
	public String UserMail;
	public int loginSessionId = 0;
	
	public int chestSize;
	
	public PlayerCharacter playerCharacter;
	
	public String Muted = "No";

	public boolean ConfirmAccount = false;

	public boolean FirstTime = false;

	public int lostConnectionNr;

	public boolean Ready = false;
	public boolean RemoveMe = false;
	public boolean HasQuit = false;
	
	public boolean OpenContainer = false;

	public Client(Socket csocket){
		this.csocket = csocket;
		this.UserId = 0;
		this.playerCharacter = null;
		this.lostConnectionNr = 0;
	}


	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			this.out = new ObjectOutputStream(this.csocket.getOutputStream());
			this.out.flush();
			this.in = new ObjectInputStream(this.csocket.getInputStream());
			do{
				try{
					try{
						if(this.lostConnectionNr > 0){
							ServerMessage.printMessage("Client "+this.UserMail+" lost connection: "+this.lostConnectionNr,false);
						}
						this.lostConnectionNr = 0;
	
						message = new String((byte[]) in.readObject());
						
						if(!Server.SERVER_RESTARTING){
							int messageIndex = message.indexOf("<");
							
							try {
								String messageIdobf = message.substring(0,messageIndex);
								
								int messageId = Obfuscator.illuminate(messageIdobf);
								
								
								String messageInfo[] = message.substring(messageIndex).split(">");
								String messageType = messageInfo[0].substring(1);
								String messageText = messageInfo[1];
		
								
								
								if(inputPacketId == messageId){
									Message newMessage = new Message(this,messageType,messageText);
									
									DataHandlers.addIncomingMessage(newMessage);
									
									inputPacketId++;
									if(inputPacketId > 65000){
										inputPacketId = 10000;
									}
								}else {
									ServerMessage.printMessage("WRONG PACKET ID!",false);
									// Disconnect client
									ConnectHandler.removeClient(this);
								}
							}catch(NumberFormatException e){
								if(playerCharacter != null){
									ServerMessage.printMessage("Wrong packet sent by: "+playerCharacter.getName(),false);
								}
							}							
							
							
						}
					}catch(SocketTimeoutException te){
						ServerMessage.printMessage("client timed out... removed it",false);
						RemoveMe = true;
					}
				}
				catch(ClassNotFoundException classnot){
					System.err.println("Data received in unknown format");
					Handler.addOutGoingMessage(this,"error","fail");
				}
			}while(message != null && !message.contains("<connection>disconnect"));

			// KILLING THREAD, REMOVING PLAYER
			this.in.close();
			this.out.close();

			ServerMessage.printMessage("client closed properly",false);

			ConnectHandler.removeClient(this);
		}catch (IOException e) {
			this.lostConnectionNr++;

			// NOT SURE ABOUT THIS ONE, JUST TRYING!!!
			this.Ready = false;
			// CAN CAUSE ERRORS!!!
	
			//e.printStackTrace();	

			// RESPAWN AT CHECKPOINT IF DEAD
			if(this.playerCharacter != null){
				if(this.playerCharacter.isDead()){
					this.playerCharacter.revive();
					BattleHandler.respawnPlayer(this.playerCharacter);
				}
			}

			PartyHandler.leaveParty(this);
			
			if(!this.RemoveMe){
				if(this.lostConnectionNr > 20){
					this.Ready = false;
					ServerMessage.printMessage(TimeUtils.now()+": " + "! - Client crashed, connection lost.",false);

					this.startLogoutTimer();
				}else{
					run();
				}
			}
		}
	}

	public void startLogoutTimer(){
		ServerMessage.printMessage("client closed improperly, started logout timer...",false);
		
		// Default logout time 20 sec
		int logoutTime = BattleHandler.playerHitTime;
		
		
		// Playerkiller logout time 2 hours
		if(playerCharacter != null){
			if(playerCharacter.getPkMarker() > 0){
				ServerMessage.printMessage("client is player killer, longer logout time",false);
				
				if(playerCharacter.getPkMarker() == 1){
					logoutTime = BattleHandler.playerHitTime;
				}else if(playerCharacter.getPkMarker() == 2){
					logoutTime = PvpHandler.playerKillerTime;
				}
			}
		}
		
		logoutTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				if(playerCharacter != null){
					if(playerCharacter.getPkMarker() > 0){
						Server.userDB.updateDB("update user_character set PlayerKiller = 0 where Id = "+playerCharacter.getDBId());
					}
				}
				quit();
			}
		}, logoutTime * 1000); 
	}

	public void quit(){
		ConnectHandler.removeClient(this);
	}
	
	public void closeSocket(){
		try {
			this.csocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Socket getSocket(){
		return this.csocket;
	}


	public int getIndex() {
		return index;
	}


	public void setIndex(int index) {
		this.index = index;
	}


}
