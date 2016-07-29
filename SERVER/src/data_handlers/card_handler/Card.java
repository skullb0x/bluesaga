package data_handlers.card_handler;

import java.sql.ResultSet;
import java.sql.SQLException;

import network.Server;

public class Card {
	public int id;
	public String name;
	public int rarity;
	public int itemId;
	public int graphicsId;
	public int copiesLeft;
	
	public Card(int id){
		this.id = id;
		ResultSet cardInfo = Server.gameDB.askDB("SELECT * FROM card WHERE Id = "+id);
		try {
			if(cardInfo.next()){
				this.name = cardInfo.getString("Name");
				this.rarity = cardInfo.getInt("Rarity");
				this.itemId = cardInfo.getInt("ItemId");
				this.graphicsId = cardInfo.getInt("GraphicsId");
				this.copiesLeft = this.rarity;
			}
			cardInfo.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}