package components;

import java.util.Vector;

import data_handlers.item_handler.Item;
import utils.ServerGameInfo;

public class TreasureMap extends Item {
	private int treasureX;
	private int treasureY;
	private int treasureZ;
	
	private Vector<Item> loot;
	
	private int level;
	
	private int monsterId = 0;
	
	public TreasureMap(){
		super(ServerGameInfo.itemDef.get(230));
	}
	
	
	/**
	 * Getters and setters
	 * @return
	 */
	
	public int getTreasureX() {
		return treasureX;
	}

	public void setTreasureX(int treasureX) {
		this.treasureX = treasureX;
	}

	public int getTreasureY() {
		return treasureY;
	}

	public void setTreasureY(int treasureY) {
		this.treasureY = treasureY;
	}

	public int getTreasureZ() {
		return treasureZ;
	}

	public void setTreasureZ(int treasureZ) {
		this.treasureZ = treasureZ;
	}

	public Vector<Item> getLoot() {
		return loot;
	}

	public void setLoot(Vector<Item> loot) {
		this.loot = loot;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getMonsterId() {
		return monsterId;
	}

	public void setMonsterId(int monsterId) {
		this.monsterId = monsterId;
	}

	
}
