package data_handlers.item_handler;

import java.util.HashMap;

public class CoinConverter {
	HashMap<String, Integer> coins = new HashMap<String, Integer>();
	
	public CoinConverter(int copper){
		coins.clear();
		
		double copperd = copper;
		double goldd = Math.floor(copperd/10000);
		double silverd = Math.floor((copperd - goldd*10000) / 100);
		copperd = copperd - goldd*10000 - silverd*100;
		
		coins.put("Copper", (int) copperd);
		coins.put("Silver", (int) silverd);
		coins.put("Gold", (int) goldd);
	}

	public HashMap<String,Integer> getCoinValues(){
		return coins;
	}
	
	public int getGold(){
		return coins.get("Gold");
	}
	
	public int getSilver(){
		return coins.get("Silver");
	}
	
	public int getCopper(){
		return coins.get("Copper");
	}
}
