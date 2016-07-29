package network;

/************************************
 * 									*
 *			CLIENT / CLIENT			*
 *									*
 ************************************/


import java.io.*;
import java.net.*;


public class Client{
	
	private String PLAYER_NAME = "";
	private int USER_ID = 0;
	
	private Socket requestSocket;
	public ObjectOutputStream out;
 	public ObjectInputStream in_answer;
 	ObjectInputStream in_new;
 	
 	String message;
 	public Client(){}
 	
 	public String init(String ServerIp)
	{
		try {
			requestSocket = new Socket(ServerIp, 4444);
			
			requestSocket.setTcpNoDelay(true);
			
			//requestSocket = new Socket("83.249.171.93", 4444);
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			out.flush();
	
			in_answer = new ObjectInputStream(requestSocket.getInputStream());
			
		} catch (UnknownHostException e) {
			//e.printStackTrace();
			return "error";
		} catch (IOException e) {
			//e.printStackTrace();
			return "error";
		}
		return "";
	}
	
	
	public void run(Socket inSocket) {
		try {
			new ObjectInputStream(inSocket.getInputStream());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	public void sendMessage(String type, String msg){
		try{
			out.writeObject("<"+type+">"+msg);
			out.flush();
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
	
	public void login(String username, String password){
		try{
			out.writeObject("<login>"+username+"-"+password);
			out.flush();
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
	
	
	public void setPlayerName(String newName){
		PLAYER_NAME = newName;
	}
	
	public String getPlayerName(){
		return PLAYER_NAME;
	}
	
	public void setUserId(int newUserId){
		USER_ID = newUserId;
	}
	
	public int getUserId(){
		return USER_ID;
	}
	
	public void getData(String type){
		try{
			out.writeObject("<"+type+">");
			out.flush();
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
	
	
	public void canWalk(int playerX, int playerY){
		try{
			out.writeObject("<canwalk>"+playerX+":"+playerY);
			out.flush();
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
	
	public String getNewData(){
		String getMsg = "";
		try {
			getMsg = (String)in_answer.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		if(getMsg == null){
			getMsg = "";
		}
		return getMsg;
	}
	
	public void sendReady(){
		try{
			out.writeObject("<ready>ready");
			out.flush();
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
		
	}
	
	public void sendNotReady(){
		try{
			out.writeObject("<ready>no");
			out.flush();
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
		
	}
	
	
	public void closeConnection() {
		try{
			out.writeObject("<connection>disconnect");
			out.flush();
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
		
		//4: Closing connection
		try{
			in_answer.close();
			out.close();
			requestSocket.close();
	    	
	    	System.exit(0);    		
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
}