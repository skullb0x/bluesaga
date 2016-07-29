package data_handlers.item_handler;

import java.util.HashMap;

public class Container {
	private String Type;
	
	private int SizeW;
	private int SizeH;
	
	private String Name = "Container";
	HashMap<String,Item> items;
	private int respawnTimer = 0;
	
	public Container(String type) {
		setType(type);
		
		int sizeW = 3;
		int sizeH = 3;
		
		respawnTimer = 2 * 60 * 6; // 2 timmar respawn

		if(type.contains("barrel")){
			sizeW = 3;
			sizeH = 5;
		}else if(type.contains("chest")){
			sizeW = 4;
			sizeH = 3;
		}else if(type.contains("bag")){
			sizeW = 3;
			sizeH = 3;
		}else{
			sizeW = 3;
			sizeH = 3;
		}
		
		if(type.contains("harvest")){
			respawnTimer = 12;
		}
		
		setSizeW(sizeW);
		setSizeH(sizeH);
		
		items = new HashMap<String,Item>();
		
	}

	public int getSizeH() {
		return SizeH;
	}

	public void setSizeH(int sizeH) {
		SizeH = sizeH;
	}

	public int getSizeW() {
		return SizeW;
	}

	public void setSizeW(int sizeW) {
		SizeW = sizeW;
	}
	
	public void addItemAtPos(int x, int y, Item newItem){
		items.put(x+","+y, newItem);
	}

	public void addItem(Item newItem){
		boolean placeItem = false;
		for(int j = 0; j < SizeH; j++){
			for(int i = 0; i < SizeW; i++){
				if(items.get(i+","+j) == null){
					items.put(i+","+j, newItem);
					placeItem = true;
					break;
				}
			}
			if(placeItem){
				break;
			}
		}
		if(!placeItem && SizeW < 4){
			SizeW = 4;
			SizeH = 6;
			for(int j = 0; j < SizeH; j++){
				for(int i = 0; i < SizeW; i++){
					if(items.get(i+","+j) == null){
						items.put(i+","+j, newItem);
						placeItem = true;
						break;
					}
				}
				if(placeItem){
					break;
				}
			}
		}
	}

	public void removeItem(int x, int y){
		items.remove(x+","+y);
	}
	
	public HashMap<String,Item> getItems(){
		return items;
	}

	public String getName(){
		if(Type.contains("smallbag")){
			Name = "Small Bag";
		}else if(Type.contains("bigbag")){
			Name = "Big Bag";
		}else if(Type.contains("barrel")){
			Name = "Barrel";
		}else if(Type.contains("chest")){
			Name = "Chest";
		}
		
		return Name;
	}
	
	public void setName(String newName){
		Name = newName;
	}
	
	public String getType() {
		return Type;
	}

	public void setType(String type) {
		Type = type;
	}
	
	public boolean checkRespawn(){
		if(respawnTimer == 0){
			return false;
		}else{
			respawnTimer--;
			if(respawnTimer == 0){
				return true;
			}
		}
		return false;
	}
	
	public boolean isFull(){
		for(int j = 0; j < SizeH; j++){
			for(int i = 0; i < SizeW; i++){
				if(items.get(i+","+j) != null){
					return false;
				}
			}
		}
		
		return true;
	}
}
