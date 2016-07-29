package utils;

import game.ServerSettings;

public class ServerMessage {
	
	public static void printMessage(String message, boolean debugOnly){
		if(!debugOnly || ServerSettings.DEV_MODE){
			System.out.println(message);
		}
	}
}