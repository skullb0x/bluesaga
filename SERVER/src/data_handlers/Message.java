package data_handlers;

import network.Client;

public class Message {
	public final Client client;
	public final String type;
	public final String message;
	
	public Message(Client reciever, String type, String message){
		this.client = reciever;
		this.type = type;
		this.message = message;
	}
}
