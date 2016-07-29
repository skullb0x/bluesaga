package data_handlers;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import abilitysystem.StatusEffect;
import sound.Sfx;
import components.Crew;
import components.Item;
import components.Ship;
import creature.Creature;
import creature.Npc;
import creature.PlayerCharacter;
import creature.Creature.CreatureType;
import game.BlueSaga;
import gui.Gui;
import map.ScreenObject;
import map.Tile;
import map.TileObject;
import screens.ScreenHandler;

/* IS NOT IN USE YET */

public class MapHandler extends Handler {

	public static boolean FADED_SCREEN = false;
	private static Timer FadeTimer = new Timer();
	
	
	
	public MapHandler() {
		super();
		// TODO Auto-generated constructor stub
	}

	public static void handleData(String serverData){
		if(serverData.startsWith("<fade>")){
			
			// FADE SCREEN
			ScreenHandler.FADE_ALPHA = 0;
			ScreenHandler.FADE_SCREEN = true;
			
		}else if(serverData.startsWith("<screen>")){
			
			String screenData = serverData.substring(8);

			ScreenHandler.SCREEN_OBJECTS_WITH_ID.clear();
			ScreenHandler.SCREEN_OBJECTS_DRAW.clear();
			
			updateScreenTiles(screenData);
			
			ScreenObject newObject = new ScreenObject();
			newObject.setObjectCreature(BlueSaga.playerCharacter);
			
			ScreenHandler.SCREEN_OBJECTS_WITH_ID.put("Player"+BlueSaga.playerCharacter.getDBId(), newObject);
			
    		FADED_SCREEN = true;
    		
    		FadeTimer.schedule( new TimerTask(){
		        @Override
				public void run() {
					WalkHandler.walkPath.clear();
		        	BlueSaga.playerCharacter.MyWalkHandler.setWalking(false);
		        	FADED_SCREEN = false;
		    	}
			}, 1000);
        	
			
			if(!ScreenHandler.LoadingStatus.equals("Loading complete")){
				// STARTING GAME!
				ScreenHandler.LoadingStatus = "Loading complete";
				ScreenHandler.ready();
				BlueSaga.playerCharacter.appear();

				// ADD PLAYER TO MAP CREATURES
				ConnectHandler.loadDone();
							
				BlueSaga.BG_MUSIC.stop();
				BlueSaga.client.sendMessage("ready","ready");
			}
		
			updateScreenObjects();
			
			ScreenHandler.FADE_ALPHA = 255;
			ScreenHandler.FADE_SCREEN = false;
			
		}else if(serverData.startsWith("<cinfo>")){
			String cInfo = serverData.substring(7);
			updateCreatureOnScreen(cInfo);
		}
		
		if(serverData.startsWith("<soul>")){
			String soulCoord[] = serverData.substring(6).split(",");
			
			int soulX = Integer.parseInt(soulCoord[0]);
			int soulY = Integer.parseInt(soulCoord[1]);
			int soulZ = Integer.parseInt(soulCoord[2]);
			
			if(ScreenHandler.SCREEN_TILES.get(soulX+","+soulY+","+soulZ) != null){
				ScreenHandler.SCREEN_TILES.get(soulX+","+soulY+","+soulZ).setSoul(true);
			}
		}
		if(serverData.startsWith("<removesoul>")){
			String soulCoord[] = serverData.substring(12).split(",");
			
			int soulX = Integer.parseInt(soulCoord[0]);
			int soulY = Integer.parseInt(soulCoord[1]);
			int soulZ = Integer.parseInt(soulCoord[2]);
			
			if(ScreenHandler.SCREEN_TILES.get(soulX+","+soulY+","+soulZ) != null){
				ScreenHandler.SCREEN_TILES.get(soulX+","+soulY+","+soulZ).setSoul(false);
			}
		}
		
		if(serverData.startsWith("<object_move>")){
			String moveInfo[] = serverData.substring(13).split(";");
			String movedObjectName = moveInfo[0];
			String fromXYZ = moveInfo[1];
			String toXYZ = moveInfo[2];
			
			String newObjectCoord[] = toXYZ.split(",");
			int toX = Integer.parseInt(newObjectCoord[0]);
			int toY = Integer.parseInt(newObjectCoord[1]);
			int toZ = Integer.parseInt(newObjectCoord[2]);
			
			//BlueSaga.SCREEN_OBJECTS_WITH_ID.get(fromXYZ).clear();
			ScreenHandler.SCREEN_OBJECTS_WITH_ID.remove(fromXYZ);
			ScreenObject newSC = new ScreenObject();
			
			/*
			if(BlueSaga.SCREEN_TILES.get(fromXYZ) != null){
				BlueSaga.SCREEN_TILES.get(fromXYZ).setPassable(true);
			}
			*/
			
			newSC.setObjectCreature(new TileObject(movedObjectName,toX,toY,toZ));
			ScreenHandler.SCREEN_OBJECTS_WITH_ID.put(toXYZ, newSC);
			
			/*
			if(BlueSaga.SCREEN_TILES.get(toXYZ) != null){
				BlueSaga.SCREEN_TILES.get(toXYZ).setPassable(false);
			}
			*/
			
			MapHandler.updateScreenObjects();
			if(movedObjectName.contains("ball")){
				Sfx.playRandomPitch("items/kick_ball");
			}else{
				Sfx.playRandomPitch("items/slide_moveable");
			}
		}
		
		if(serverData.startsWith("<unlock_door>")){
			String doorCoord = serverData.substring(13);
			/*
			int doorX = Integer.parseInt(doorInfo[0]);
			int doorY = Integer.parseInt(doorInfo[1]);
			int doorZ = Integer.parseInt(doorInfo[2]);
			*/
			if(ScreenHandler.SCREEN_TILES.get(doorCoord) != null){
				ScreenHandler.SCREEN_TILES.get(doorCoord).setMonsterLocked(false);
			}
			
			Sfx.play("notifications/monster_unlock");
		}
		
		if(serverData.startsWith("<lock_door>")){
			String doorCoord = serverData.substring(11);
				
			if(ScreenHandler.SCREEN_TILES.get(doorCoord) != null){
				ScreenHandler.SCREEN_TILES.get(doorCoord).setMonsterLocked(true);
			}
			
			//BlueSaga.SFX.play("monster_unlock");
		}
		
		if(serverData.startsWith("<reset_moveable>")){
			String resetInfo[] = serverData.substring(16).split(";");
			String resetObjectName = resetInfo[0];
			String fromXYZ = resetInfo[1];
			String toXYZ = resetInfo[2];
			
			String newObjectCoord[] = toXYZ.split(",");
			int toX = Integer.parseInt(newObjectCoord[0]);
			int toY = Integer.parseInt(newObjectCoord[1]);
			int toZ = Integer.parseInt(newObjectCoord[2]);
			
			ScreenHandler.SCREEN_OBJECTS_WITH_ID.remove(fromXYZ);
			ScreenObject newSC = new ScreenObject();
			newSC.setObjectCreature(new TileObject(resetObjectName,toX,toY,toZ));
			ScreenHandler.SCREEN_OBJECTS_WITH_ID.put(toXYZ, newSC);
			MapHandler.updateScreenObjects();
		}
		
	}

	public static void updateScreenTiles(String tilesData){
		String tilesInfo[] = tilesData.split(";");

		// ADD NEW TILES TO SCREEN
		for(String tileData : tilesInfo){

			if(tileData.contains(":")){
				String tileInfo[] = tileData.split(":");

				addTileToScreen(tileInfo[0]);

				//OCCUPANT
				if(!tileInfo[1].equals("0")){
					addCreatureToScreen(tileInfo[1]);
				}
				
				
				
			}else{
				addTileToScreen(tileData);
			}
		}
		updateScreenObjects();
	}
	
	private static void addTileToScreen(String tileData){
		if(tileData.contains(",")){
			String tileInfo[] = tileData.split(",");
			int x = Integer.parseInt(tileInfo[0]);
			int y = Integer.parseInt(tileInfo[1]);
			int z = Integer.parseInt(tileInfo[2]);
			String type = tileInfo[3];
			String name = tileInfo[4];
			int passable = Integer.parseInt(tileInfo[5]);
			Integer.parseInt(tileInfo[6]);
			String objectId = tileInfo[7];
			String statusEffects = tileInfo[8];
			
			boolean isNewTile = true;
			
			Tile newTile = new Tile(x,y,z);
			
			if(ScreenHandler.SCREEN_TILES.containsKey(x+","+y+","+z)){
				if(ScreenHandler.SCREEN_TILES.get(x+","+y+","+z).getType().equals(type) && ScreenHandler.SCREEN_TILES.get(x+","+y+","+z).getName().equals(name)){
					newTile = ScreenHandler.SCREEN_TILES.get(x+","+y+","+z);
					isNewTile = false;
				}
			}
			
			if(isNewTile){
				newTile.setType(type, name);
			}
			
			
			if(passable == 1){
				newTile.setPassable(true);
			}else{
				newTile.setPassable(false);
			}
			
			
			
			//Status effects
			if(!statusEffects.equals("0")){
				String statusEffectsInfo[] = statusEffects.split("-");
				for(String se: statusEffectsInfo){
					String seInfo[] = se.split("/");
					int seId = Integer.parseInt(seInfo[0]);
					int seGraphics = Integer.parseInt(seInfo[1]);
					
					newTile.addStatusEffect(new StatusEffect(seId,seGraphics));
				}
			}
			
			// UPDATE MINIMAP
			boolean hasTileObject = false;
			
			// Add object
			if(!objectId.equals("0")){
				addObjectToScreen(x,y,z, objectId);
				hasTileObject = Gui.MapWindow.updateTileObject(x, y, z, objectId);
				
			}
			
			if(!hasTileObject){
				Gui.MapWindow.updateTile(x,y,z,type,name);
			}
			
			
			
			ScreenHandler.SCREEN_TILES.put(x+","+y+","+z,newTile);
		}
	}
	
	public static void addObjectToScreen(int x, int y, int z, String objectId){
		
		ScreenObject newScreenObject = new ScreenObject();
		TileObject newTileObject = new TileObject(objectId, x, y, z);
		newScreenObject.setObjectCreature(newTileObject);
		ScreenHandler.SCREEN_OBJECTS_WITH_ID.put(x+","+y+","+z, newScreenObject);
		
		Gui.MapWindow.updateTileObject(x,y,z,objectId);
	}
	
	public static Creature addCreatureToScreen(String creatureData){
		
		String creatureInfo[] = creatureData.split(",");
		
		CreatureType cType = CreatureType.valueOf(creatureInfo[0]);
		int cDbId = Integer.parseInt(creatureInfo[1]);
		int cId = Integer.parseInt(creatureInfo[2]);
		int cX = Integer.parseInt(creatureInfo[3]);
		int cY = Integer.parseInt(creatureInfo[4]);
		int cZ = Integer.parseInt(creatureInfo[5]);
		float cRotation = Float.parseFloat(creatureInfo[6]);
		
		if(ScreenHandler.SCREEN_OBJECTS_WITH_ID.containsKey(cType.toString()+cDbId)){
			ScreenHandler.SCREEN_OBJECTS_WITH_ID.get(cType.toString()+cDbId).getCreature().setRotationNow(cRotation);
			
			return ScreenHandler.SCREEN_OBJECTS_WITH_ID.get(cType.toString()+cDbId).getCreature();
		}else{
			Creature newCreature = new Creature(cX,cY,cZ);
			
			if(cType == CreatureType.Player){
				newCreature = new PlayerCharacter(cId,cX,cY,cZ);
			}else if(cType == CreatureType.Monster){
				newCreature = new Npc(cId,cX,cY,cZ);
			}else{
				newCreature.setType(cId);
			}
			
			newCreature.setCreatureType(cType);
			newCreature.setDBId(cDbId);
			newCreature.setSizeWidth(MonsterHandler.creatureDefinitions.get(cId).getSizeWidth());
			newCreature.setSizeHeight(MonsterHandler.creatureDefinitions.get(cId).getSizeHeight());
			newCreature.setRotationNow(cRotation);
		
			ScreenObject newObject = new ScreenObject();
			newObject.setObjectCreature(newCreature);
		
			ScreenHandler.SCREEN_OBJECTS_WITH_ID.put(cType.toString()+cDbId, newObject);
			BlueSaga.client.sendMessage("cinfo",cType.toString()+","+cDbId);
			
			return newCreature;
		}
	}	
	
	public static void updateCreatureOnScreen(String creatureData){
		String creatureInfo[] = creatureData.split(",");
		
		CreatureType creatureType = CreatureType.valueOf(creatureInfo[0]);
		int cDbId = Integer.parseInt(creatureInfo[1]);

		if(ScreenHandler.SCREEN_OBJECTS_WITH_ID.containsKey(creatureType.toString()+cDbId)){
			Creature newCreature = ScreenHandler.SCREEN_OBJECTS_WITH_ID.get(creatureType.toString()+cDbId).getCreature();
			
			// Common creature attributes
			String cName = creatureInfo[7];
			int cHeadId = Integer.parseInt(creatureInfo[8]);
			int cHeadSkinId = Integer.parseInt(creatureInfo[9]);
			int cWeaponId = Integer.parseInt(creatureInfo[10]);
			int cWeaponSkinId = Integer.parseInt(creatureInfo[11]);
			int cOffHandId = Integer.parseInt(creatureInfo[12]);
			int cOffHandSkinId = Integer.parseInt(creatureInfo[13]);
			int cAmuletId = Integer.parseInt(creatureInfo[14]);
			int cAmuletSkinId = Integer.parseInt(creatureInfo[15]);
			int cArtifactId = Integer.parseInt(creatureInfo[16]);
			int cArtifactSkinId = Integer.parseInt(creatureInfo[17]);
			int cHealthStatus = Integer.parseInt(creatureInfo[18]);
			int cSpeed = Integer.parseInt(creatureInfo[19]);
			
			int cMouthFeatureId = Integer.parseInt(creatureInfo[20]); 
			int cAccessoriesId = Integer.parseInt(creatureInfo[21]); 
			int cSkinFeatureId = Integer.parseInt(creatureInfo[22]);
			
			int cIsResting = Integer.parseInt(creatureInfo[23]);
			
			newCreature.setCreatureType(creatureType);
			newCreature.setName(cName);
			newCreature.setHealthStatus(cHealthStatus);
			
			newCreature.MyEquipHandler.updateItemCoordinates();

			Item HeadItem = null;
			if(cHeadSkinId > 0){
				HeadItem = new Item(cHeadSkinId);
			}else if(cHeadId > 0){
				HeadItem = new Item(cHeadId);
			}
			if(HeadItem != null){
				HeadItem.setType("Head");
				newCreature.MyEquipHandler.equipItem(HeadItem);
			}
			
			Item WeaponItem = null;
			if(cWeaponSkinId > 0){
				WeaponItem = new Item(cWeaponSkinId);
			}else if(cWeaponId > 0){
				WeaponItem = new Item(cWeaponId);
			}
			if(WeaponItem != null){
				WeaponItem.setType("Weapon");
				newCreature.MyEquipHandler.equipItem(WeaponItem);
			}
			
			Item OffHandItem = null;
			if(cOffHandSkinId > 0){
				OffHandItem = new Item(cOffHandSkinId);
			}else if(cOffHandId > 0){
				OffHandItem = new Item(cOffHandId);
			}
			if(OffHandItem != null){
				OffHandItem.setType("OffHand");
				newCreature.MyEquipHandler.equipItem(OffHandItem);
			}
			
			Item AmuletItem = null;
			if(cAmuletSkinId > 0){
				AmuletItem = new Item(cAmuletSkinId);
			}else if(cAmuletId > 0){
				AmuletItem = new Item(cAmuletId);
			}
			if(AmuletItem != null){
				AmuletItem.setType("Amulet");
				newCreature.MyEquipHandler.equipItem(AmuletItem);
			}
			
			Item ArtifactItem = null;
			if(cArtifactSkinId > 0){
				ArtifactItem = new Item(cArtifactSkinId);
			}else if(cArtifactId > 0){
				ArtifactItem = new Item(cArtifactId);
			}
			if(ArtifactItem != null){
				ArtifactItem.setType("Artifact");
				newCreature.MyEquipHandler.equipItem(ArtifactItem);
			}
			
			newCreature.setStat("SPEED", cSpeed);
		
		
			newCreature.getCustomization().setMouthFeatureId(cMouthFeatureId);
			newCreature.getCustomization().setAccessoriesId(cAccessoriesId);
			newCreature.getCustomization().setSkinFeatureId(cSkinFeatureId);
			
			
			if(cIsResting == 1){
				newCreature.setResting(true);
			}else{
				newCreature.setResting(false);
			}
			
			if(creatureType == CreatureType.Monster){
				// Npc attributes
				int cAggroType = Integer.parseInt(creatureInfo[24]);
				int cSpecialType = Integer.parseInt(creatureInfo[25]);
				
				Npc npcCreature = (Npc) newCreature;
				
				npcCreature.setAggroType(cAggroType);
				npcCreature.setSpecialType(cSpecialType);
				
				if(cName.contains(" Titan")){
					npcCreature.setEpic(true);
					
					int newSizeWidth = newCreature.getSizeWidth() * 4;
					int newSizeHeight = newCreature.getSizeHeight() * 4;
					
					npcCreature.setSizeWidth(newSizeWidth);
					npcCreature.setSizeHeight(newSizeHeight);
				}
				
			}else {
				// Player attributes
				int cShipId = Integer.parseInt(creatureInfo[24]);
				int cCrewId = Integer.parseInt(creatureInfo[25]);
				String cCrewName = creatureInfo[26];
				String cCrewMemberState = creatureInfo[27];
				int cPkMarker = Integer.parseInt(creatureInfo[28]);
				int cAdminLevel = Integer.parseInt(creatureInfo[29]);
				int cBountyRank = Integer.parseInt(creatureInfo[30]);
				
				PlayerCharacter newPlayer = (PlayerCharacter) newCreature;
				
				Crew newCrew = new Crew(cCrewId);
				newCrew.setName(cCrewName);
				newCrew.setMemberState(cCrewMemberState);
				
				newPlayer.setCrew(newCrew);
				
				if(cShipId > 0){
					Ship userShip = new Ship(cShipId);
					userShip.setShow(true);
					newPlayer.setShip(userShip);
				}else{
					if(newPlayer.getShip() != null){
						newPlayer.getShip().setShow(false);
					}
				}
				
				newPlayer.setPkMarker(cPkMarker);
				newPlayer.setAdminLevel(cAdminLevel);
				newPlayer.setBountyRank(cBountyRank);
			}
			
			
			if(cName.contains("!")){
				BlueSaga.BG_MUSIC.changeSong("event","None");
			}
		
		}
	}
	
	

	/* CREATURE SORTER */
	
	public static class ScreenObjectComparator implements Comparator<ScreenObject> {
	    @Override
	    public int compare(ScreenObject o1, ScreenObject o2) {
	    	int o1y = o1.getY();
	    	int o2y = o2.getY();
	    	
	    	if(o1.getType().equals("Creature")){
    			if(o1.getCreature().isDead()){
    				o1y-=3;
    			}
	    	}else{
	    		o1y--;
	    	}
    		if(o2.getType().equals("Creature")){
    			if(o2.getCreature().isDead()){
	    			o2y-=3;
    			}
	    	}else{
	    		o2y--;
	    	}
    	
    		if(o1y == o2y){
    			if(o1.getType().equals("Creature") && !o2.getType().equals("Creature")){
    				o2y++;
    			}
    			if(o2.getType().equals("Creature") && !o1.getType().equals("Creature")){
    				o1y++;
    			}
    		}
    		
	    	return o1y - o2y;
	    }
	}
	
	public static class ScreenTileComparator implements Comparator<Tile> {
	    @Override
	    public int compare(Tile o1, Tile o2) {
	    	int o1z = o1.getZ();
	    	int o2z = o2.getZ();
	    	
	    	return o1z - o2z;
	    }
	}	
	
	public static void updateScreenObjects(){
		
	    HashMap<String, Tile> NEW_SCREEN_TILES = new HashMap<String, Tile>();
		
		for(Tile t: ScreenHandler.SCREEN_TILES.values()){
			t.setOccupant(CreatureType.None, 0);
			if(isVisibleForPlayer(BlueSaga.playerCharacter,t.getX(),t.getY(), t.getZ())){
				NEW_SCREEN_TILES.put(t.getX()+","+t.getY()+","+t.getZ(), t);
			}
		}
		
		ScreenHandler.SCREEN_TILES = NEW_SCREEN_TILES; 
		
		HashMap<String, ScreenObject> newScreenObjectsId = new HashMap<String, ScreenObject>();
		Vector<ScreenObject> newScreenObjectsDraw = new Vector<ScreenObject>();
		
		for(Iterator<Entry<String, ScreenObject>> iter = ScreenHandler.SCREEN_OBJECTS_WITH_ID.entrySet().iterator();iter.hasNext();){ 
		    Entry<String, ScreenObject> entry = iter.next();
		    
	        if(isVisibleForPlayer(BlueSaga.playerCharacter,entry.getValue().getX(),entry.getValue().getY(), entry.getValue().getZ())){
		    	newScreenObjectsId.put(entry.getKey(), entry.getValue());
		    	newScreenObjectsDraw.add(entry.getValue());
		    	if(!entry.getValue().getType().equals("Object")){
		    		if(!entry.getValue().getCreature().isDead() && !entry.getValue().getCreature().isRemoved()){
			    		if(!entry.getKey().equals("Player"+BlueSaga.playerCharacter.getDBId())){
				    		if(ScreenHandler.SCREEN_TILES.get(entry.getValue().getX()+","+entry.getValue().getY()+","+entry.getValue().getZ()) != null){
				    			ScreenHandler.SCREEN_TILES.get(entry.getValue().getX()+","+entry.getValue().getY()+","+entry.getValue().getZ()).setOccupant(entry.getValue().getCreature().getCreatureType(), entry.getValue().getCreature().getDBId());
				    		}
			    		}
		    		}
		    	}
			}
		    
		}	
		
		ScreenHandler.SCREEN_OBJECTS_WITH_ID = newScreenObjectsId;
		ScreenHandler.SCREEN_OBJECTS_DRAW = newScreenObjectsDraw;
		
		Collections.sort(ScreenHandler.SCREEN_OBJECTS_DRAW, new ScreenObjectComparator());
		
	}

}
