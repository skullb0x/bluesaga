package data_handlers;

import network.Client;

public class Message {
	private Client client;
	private String type;
	private String message;
	
	public Message(Client reciever, String type, String message){
		this.client = reciever;
		this.type = type;
		this.message = message;
	}
	
	public Client getClient() {
		return client;
	}
	public void setClient(Client client) {
		this.client = client;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
}
