package instances;

import java.awt.Point;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Vector;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import utils.RandomUtils;


public class ArchipelagoGenerator {
	private static HashMap<String,Integer> mapTiles;

	/**
	 * Generates a map of islands of different sizes
	 */

	// Map borders
	private static int minX = 0;
	private static int maxX = 1024;
	private static int minY = 0;
	private static int maxY = 640;

	public static int islandZ = 0;
	
	public static Vector<Point> mountains;
	public static Vector<Point> shallow_spots;
	
	public static void generate(int nrIslands){
		mapTiles = new HashMap<String,Integer>();

		mountains = new Vector<Point>();
		shallow_spots = new Vector<Point>();
		
		

		// Island generator variables
		
		int islandX = 0;
		int islandY = 0;
		
		int islandSize = 0;

		

		
		//islandX = RandomUtils.getInt(minX+padding*3, maxX-padding*3);
		//islandY = RandomUtils.getInt(minY+padding*3, maxY-padding*3);

		islandX = 512;
		islandY = 320;

	
		// Generator
		for(int n = 0; n < nrIslands; n++){


			int chanceOfIsland = RandomUtils.getInt(0,2);
			
			// If last island, be sure to place it
			if(n == nrIslands -1){
				chanceOfIsland = 0;
			}

			// Get a random size for the island
			islandSize = RandomUtils.getInt(100, 2000);
			if(chanceOfIsland == 1){
				islandSize = RandomUtils.getInt(20, 200);
			}
			
			
			int distanceToNextMountain = RandomUtils.getInt(300,600);

			// Places out tiles
			for(int s = 0; s < islandSize; s++){

				if(chanceOfIsland == 0){
					
					// Island
					Iterator<Entry<Point, Integer>> it = GeneratorBrush.getIslandBrush().entrySet().iterator();
	
					if(distanceToNextMountain > 0){
						distanceToNextMountain--;
					}else{
						int mountainChance = RandomUtils.getInt(0, 100);
						
						if(mountainChance == 0){
							mountains.add(new Point(islandX+10,islandY+10));
							distanceToNextMountain = RandomUtils.getInt(300,600);
						}
					}
					
					
					while (it.hasNext()) {
						Entry<Point, Integer> b = it.next();
						
						// Only place water on null tiles
						if(!mapTiles.containsKey((islandX+b.getKey().x)+","+(islandY+b.getKey().y)+","+islandZ)){
							mapTiles.put((islandX+b.getKey().x)+","+(islandY+b.getKey().y)+","+islandZ, b.getValue());
						}else{
							String coord = (islandX+b.getKey().x)+","+(islandY+b.getKey().y)+","+islandZ;
							if(b.getValue() > mapTiles.get(coord)){
								mapTiles.put(coord, b.getValue());
							}
						}
					}
				}else{
					// Deep water
					Iterator<Entry<Point, Integer>> it = GeneratorBrush.getWaterBrush().entrySet().iterator();
					
					int shallowChance = RandomUtils.getInt(0,150);
					
					if(shallowChance == 0){
						shallow_spots.add(new Point(islandX+10,islandY+10));
					}
					
					while (it.hasNext()) {
						Entry<Point, Integer> b = it.next();
						
						// Only place water on null tiles
						if(!mapTiles.containsKey((islandX+b.getKey().x)+","+(islandY+b.getKey().y)+","+islandZ)){
							mapTiles.put((islandX+b.getKey().x)+","+(islandY+b.getKey().y)+","+islandZ, b.getValue());
						}else{
							if(b.getValue() > mapTiles.get((islandX+b.getKey().x)+","+(islandY+b.getKey().y)+","+islandZ)){
								mapTiles.put((islandX+b.getKey().x)+","+(islandY+b.getKey().y)+","+islandZ, b.getValue());
							}
						}
						
					}
				}
				
				// Randomize the direction of the cross path
				int direction = RandomUtils.getInt(1, 4);
				if(direction == 1){
					islandY-=2;
				}else if(direction == 2){
					islandX+=2;
				}else if(direction == 3){
					islandY+=2;
				}else if(direction == 4){
					islandX-=2;
				}
				
				/*
				int direction = RandomUtils.getInt(1, 8);
				if(direction == 1){
					islandY--;
				}else if(direction == 2){
					islandY--;
					islandX++;
				}else if(direction == 3){
					islandX++;
				}else if(direction == 4){
					islandY++;
					islandX++;
				}else if(direction == 5){
					islandY++;
				}else if(direction == 6){
					islandY++;
					islandX--;
				}else if(direction == 7){
					islandX--;
				}else if(direction == 8){
					islandY--;
					islandX--;
				}
				 */
				/*
				// Check that the cross doesn't pass map borders
				if(islandX < minX+padding){
					islandX = minX+padding;
				}
				if(islandX > maxX-padding){
					islandX = maxX-padding;
				}
				if(islandY < minY+padding){
					islandY = minY+padding;
				}
				if(islandY > maxY-padding){
					islandY = maxY-padding;
				}
				*/
			}

		}
	}
	

	public static void printmap(){
		for(int y = minY; y < maxY; y++){
			for(int x = minX; x < maxX; x++){
				if(mapTiles.containsKey(x+","+y+","+islandZ)){
					System.out.print(mapTiles.get(x+","+y+","+islandZ));
				}else{
					System.out.print(" ");
				}
			}
			System.out.println("");
		}
	}
	

	public static void draw(Graphics g){
		Iterator<Entry<String, Integer>> it = mapTiles.entrySet().iterator();

		while (it.hasNext()) {
			Entry<String, Integer> t = it.next();

			String tCoord[] = t.getKey().split(",");

			int tileX = Integer.parseInt(tCoord[0]);
			int tileY = Integer.parseInt(tCoord[1]);
			Integer.parseInt(tCoord[2]);

			int tileType = t.getValue();

			if(tileType == 0){
				g.setColor(new Color(255,0,0));
			}else if(tileType == 1){
				// Deep water
				g.setColor(new Color(24,100,165));
			}else if(tileType == 2){
				// Shallow water
				g.setColor(new Color(90,161,222));
			}else if(tileType == 3){
				// Beach 
				g.setColor(new Color(242,255,153));
			}else if(tileType == 4){
				// Grass 
				g.setColor(new Color(206,255,153));
			}else if(tileType == 5){
				// Height 1
				g.setColor(new Color(168,223,108));
			}else if(tileType == 6){
				// Height 2
				g.setColor(new Color(112,159,62));
			}else if(tileType == 7){
				// Height 3
				g.setColor(new Color(175,175,175));
			}else if(tileType == 8){
				// Height 4
				g.setColor(new Color(255,255,255));
			}

			g.fillRect(tileX, tileY, 1, 1);

		}
	}

	public static HashMap<String, Integer> getTiles(){
		return mapTiles;
	}


}
