package generators;


import java.awt.Point;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import brushes.GeneratorBrush;
import utils.RandomUtils;

public class MoAGenerator {
	private static HashMap<String,Integer> mapTiles;

	public static int edgeMinX;
	public static int edgeMaxX;
	public static int edgeMinY;
	public static int edgeMaxY;
	
	public static int stitchX;
	public static int stitchY;
	
	public static int exitX;
	public static int exitY;
	
	public static int width = 0;
	public static int height = 0;
	
	/**
	 * Generates a map of islands of different sizes
	 */

	public static void generate(int archipelagoZ){
		System.out.println("Generting archipelago...");
		mapTiles = new HashMap<String,Integer>();

		// Map borders
		int minX = 0;
		int maxX = 1024;
		int minY = 0;
		int maxY = 640;
		int timeSinceIsland = 0;
		
		// Edges of the generated map
		edgeMinX = maxX;
		edgeMaxX = minX;
		edgeMinY = maxY;
		edgeMaxY = minY;
		
		// Coordinates that connects the archipelago with the static world
		stitchX = 0;
		stitchY = 0;
		
		// Island generator variables
		int nrIslands = RandomUtils.getInt(100,200);

		// Cursor position
		int islandX = 512;
		int islandY = 320;
		
		int islandSize = 0;

		

		
		//islandX = RandomUtils.getInt(minX+padding*3, maxX-padding*3);
		//islandY = RandomUtils.getInt(minY+padding*3, maxY-padding*3);

	
		// Generator
		for(int n = 0; n < nrIslands; n++){


			int chanceOfIsland = RandomUtils.getInt(0,40);
			
			if(chanceOfIsland > 0){
				timeSinceIsland++;
			}else{
				timeSinceIsland = 0;
			}
			
			// If last island, be sure to place it
			if(n == nrIslands -1 || timeSinceIsland > 150){
				timeSinceIsland = 0;
				chanceOfIsland = 0;
			}

			
			
			// Get a random size for the island
			islandSize = RandomUtils.getInt(50, 200);
			if(chanceOfIsland < 5){
				islandSize = RandomUtils.getInt(60, 800);
			}
			
			
			// Places out tiles
			for(int s = 0; s < islandSize; s++){
				
				Iterator<Entry<Point, Integer>> it = null;
				
				if(chanceOfIsland < 5){
					// Island spot
					it = GeneratorBrush.getIslandBrush().entrySet().iterator();
				}else if(chanceOfIsland > 25){
					// Shallow spot
					it = GeneratorBrush.getShallowBrush().entrySet().iterator();
				}else{
					// Deep water
					it = GeneratorBrush.getWaterBrush().entrySet().iterator();
				}
				
				if(it != null){
					while (it.hasNext()) {
						Entry<Point, Integer> b = it.next();
						
						boolean placedTile = false;
						int brushX = islandX+b.getKey().x;
						int brushY = islandY+b.getKey().y;
						
						// Only place water on null tiles
						if(b.getValue() > 0){
							if(!mapTiles.containsKey(brushX+","+brushY+","+archipelagoZ)){
								placedTile = true;
								mapTiles.put(brushX+","+brushY+","+archipelagoZ, b.getValue());
							}else{
								if(b.getValue() > mapTiles.get(brushX+","+brushY+","+archipelagoZ)){
									placedTile = true;
									mapTiles.put(brushX+","+brushY+","+archipelagoZ, b.getValue());
								}
							}	
						}
						
						if(placedTile){
							// Save edges coord
							if(brushX > edgeMaxX){
								edgeMaxX = brushX;
							}
							if(brushX < edgeMinX){
								edgeMinX = brushX;
								
								stitchX = edgeMinX;
								stitchY = brushY;
							
							}
							if(brushY > edgeMaxY){
								edgeMaxY = brushY;
							}
							if(brushY < edgeMinY){
								edgeMinY = brushY;
							}
						}
						
					}
				}
				
				// Randomize the direction of the cross path
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

				// Check that the cross doesn't pass map borders
		
			}
		}
		
		width = edgeMaxX - edgeMinX;
		height = edgeMaxY - edgeMinY;
		
		
		// Set exit
		exitX = stitchX + 5;
		exitY = stitchY;
		
		/*
		Spiral findExitSpot = new Spiral(10,10);
		List<Point> l = findExitSpot.spiral();

		for(Point p: l){
			int checkX = (int) (stitchX + p.getX());
			int checkY = (int) (stitchY + p.getY());
			
			if(mapTiles.get(checkX+","+checkY+","+archipelagoZ) != null){
				int distance = checkX - stitchX;
				if(distance > 5){
					exitX = checkX;
					exitY = checkY;
					break;
				}
			}
		}
		*/
		
		mapTiles.put(exitX+","+exitY+","+archipelagoZ, 1);
		
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

			if(tileX == stitchX && tileY == stitchY){
	    		g.setColor(new Color(255,0,0));
	    	}else if(tileType == 0){
				// Void
				g.setColor(new Color(0,0,0));
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
	    	}else if(tileType == 100){
	    		// Debug
	    		g.setColor(new Color(255,0,0));
	    	}
	    	
			if(tileType > 0){
				g.fillRect(tileX, tileY, 1, 1);
			}

		}
	}

	public static HashMap<String, Integer> getTiles(){
		return mapTiles;
	}
	
	public static int getTile(int x, int y, int z){
		if(mapTiles.containsKey(x+","+y+","+z)){
			return mapTiles.get(x+","+y+","+z);
		}
		return 0;
	}
	
	public static void setTile(int x, int y, int z, int tileNr){
		if(mapTiles.containsKey(x+","+y+","+z)){
			mapTiles.put(x+","+y+","+z,tileNr);
		}
	}
	
	


}
