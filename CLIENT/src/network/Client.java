package network;

/************************************
 * 									*
 *			CLIENT / CLIENT			*
 *									*
 ************************************/


import game.BlueSaga;
import game.ClientSettings;

import java.io.*;
import java.net.*;

import utils.Obfuscator;


public class Client{
	
	private String USER_MAIL = "";
	private int USER_ID = 0;
	
	private int keepAliveSec = 0;
	
	public boolean connected = false;
	
	private int outputPacketId = 10000;
	
	private Socket requestSocket;
	public ObjectOutputStream out;
 	public ObjectInputStream in_answer;
 	ObjectInputStream in_new;
 	
 	String message;
 	public Client(){}
 	
 	public String init()
	{
		resetPacketId();
 		try {
 			// REGULAR SOCKET
			requestSocket = new Socket(ClientSettings.SERVER_IP, ClientSettings.PORT);
	 		
			requestSocket.setSoTimeout(0);
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			out.flush();
	 		
			in_answer = new ObjectInputStream(requestSocket.getInputStream());
	 		
			connected = true;
		} catch (UnknownHostException e) {
			BlueSaga.DEBUG.print("Connection to server failed to reconnect");
			return "error";
		} catch (IOException e) {
			BlueSaga.DEBUG.print("Connection to server failed to reconnect");
			return "error";
		}
		return "";
	}
	
	
	public void sendMessage(String type, String msg){
		keepAliveSec = 0;
		try{
			String outputPacketIdObfuscated = Obfuscator.obfuscate(outputPacketId);
			
			byte[] byteMsg = (outputPacketIdObfuscated+"<"+type+">"+msg).getBytes();
			out.writeObject(byteMsg);
			out.flush();
			
			outputPacketId++;
			if(outputPacketId > 65000){
				outputPacketId = 10000;
			}
		}
		catch(IOException ioException){
			//ioException.printStackTrace();
		}
	}
	
	
	public void setUserMail(String newMail){
		USER_MAIL = newMail;
	}
	
	public String getUserMail(){
		return USER_MAIL;
	}
	
	public void setUserId(int newUserId){
		USER_ID = newUserId;
	}
	
	public int getUserId(){
		return USER_ID;
	}
	
	
	public void closeConnection() {
		sendMessage("connection","disconnect");
		
		//4: Closing connection
		try{
			in_answer.close();
			out.close();
			requestSocket.close();
	    	
	    }catch(IOException ioException){
		//	ioException.printStackTrace();
		}
	}

	public int getKeepAliveSec() {
		return keepAliveSec;
	}

	public void sendKeepAlive() {
		if(!BlueSaga.HAS_QUIT){
			keepAliveSec++;
			if(this.keepAliveSec >= 4 && !BlueSaga.reciever.lostConnection ){
				sendMessage("keepalive","true");
			}
		}
	}
	
	public void resetPacketId(){
		outputPacketId = 10000;
	}
	
}