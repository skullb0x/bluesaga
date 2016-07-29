package minigames;

import java.util.Vector;

import utils.RandomUtils;
import map.Tile;
import network.Client;
import network.Server;

public class DefenseGame {
	private int evadeX = 0;
	private int evadeY = 0;
	
	private boolean Active = false;
	private boolean CheckedResult = false;
	
	public DefenseGame() {
		
	}
	
	public void start(Client client){
		Vector<Tile> randomTiles = new Vector<Tile>();
		
		for(int i = -1; i < 2; i++){
			for(int j = -1; j < 2; j++){
				if(i == 0 && j == 0){
					
				}else{
					if(Server.WORLD_MAP.getTile(client.playerCharacter.getX()+i, client.playerCharacter.getY()+j, client.playerCharacter.getZ()) != null){
						if(Server.WORLD_MAP.getTile(client.playerCharacter.getX()+i, client.playerCharacter.getY()+j, client.playerCharacter.getZ()).isPassable()){
							randomTiles.add(Server.WORLD_MAP.getTile(client.playerCharacter.getX()+i, client.playerCharacter.getY()+j, client.playerCharacter.getZ()));
						}
					}
				}
			}
		}
		
		if(randomTiles.size() > 0){
			int randomIndex = RandomUtils.getInt(0,randomTiles.size()-1);
			
			evadeX = randomTiles.get(randomIndex).getX();
			evadeY = randomTiles.get(randomIndex).getY();

			setActive(true);
			setCheckedResult(false);
		}else{
			setActive(false);
		}
	}
	
	public void end(){
		setActive(false);
	}
	
	public boolean succeeded(int targetX, int targetY){
		if(!isCheckedResult()){
			setCheckedResult(true);
			if(targetX == evadeX && targetY == evadeY){
				return true;
			}
		}
		return false;
	}
	
	public int getEvadeX(){
		return evadeX;
	}
	
	public int getEvadeY(){
		return evadeY;
	}

	public boolean isActive() {
		return Active;
	}

	public void setActive(boolean state) {
		this.Active = state;
	}

	public boolean isCheckedResult() {
		return CheckedResult;
	}

	public void setCheckedResult(boolean checkedResult) {
		CheckedResult = checkedResult;
	}
}