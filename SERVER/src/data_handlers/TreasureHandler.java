package data_handlers;

import components.TreasureMap;
import utils.RandomUtils;

public class TreasureHandler extends Handler {
	
	public TreasureHandler(){
		super();
	}
	
	
	public TreasureMap generateTreasure(){
		TreasureMap treasureMap = new TreasureMap();
		
		RandomUtils.getInt(1, 200);
		
		return treasureMap;
	}
}
