package generators;

import java.util.concurrent.ThreadLocalRandom;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

/**
 * translated from C code found on
 * http://roguebasin.roguelikedevelopment.org/index.php/Cellular_Automata_Method_for_Generating_Random_Cave-Like_Levels
 *
 * all rights belong to original author, Jim Babcock
 */
public class CaveGenerator {
 
        private static final int TILE_FLOOR = 0;
        private static final int TILE_WALL = 1;
 
        private class GenerationParams {
                int r1_cutoff, r2_cutoff;
                int reps;
        }
 
        private int[][] grid, grid2;
 
        private int fillprob = 40; // 40
        private int r1_cutoff = 5; // 5
        private int r2_cutoff = 2; // 2
        public int size_x = 128, size_y = 128;
 
        private GenerationParams[] params_set;
        private int generations = 3; // 3
 
        public int[][] generate(int sizeX, int sizeY){
        	size_x = sizeX;
        	size_y = sizeY;
        	run();
        	return grid;
        }
        
        private int randpick() {
                if (ThreadLocalRandom.current().nextInt(100) < fillprob)
                        return TILE_WALL;
                else
                        return TILE_FLOOR;
        }
 
        private void initmap() {
                grid = new int[size_y][size_x];
                grid2 = new int[size_y][size_x];
 
                for (int yi = 1; yi < size_y - 1; yi++)
                        for (int xi = 1; xi < size_x - 1; xi++)
                                grid[yi][xi] = randpick();
 
                for (int yi = 0; yi < size_y; yi++)
                        for (int xi = 0; xi < size_x; xi++)
                                grid2[yi][xi] = TILE_WALL;
 
                for (int yi = 0; yi < size_y; yi++)
                        grid[yi][0] = grid[yi][size_x - 1] = TILE_WALL;
                for (int xi = 0; xi < size_x; xi++)
                        grid[0][xi] = grid[size_y - 1][xi] = TILE_WALL;
        }
 
        private void generation(GenerationParams params) {
                for (int yi = 1; yi < size_y - 1; yi++)
                        for (int xi = 1; xi < size_x - 1; xi++) {
                                int adjcount_r1 = 0, adjcount_r2 = 0;
 
                                for (int ii = -1; ii <= 1; ii++)
                                        for (int jj = -1; jj <= 1; jj++) {
                                                if (grid[yi + ii][xi + jj] != TILE_FLOOR)
                                                        adjcount_r1++;
                                        }
                                for (int ii = yi - 2; ii <= yi + 2; ii++)
                                        for (int jj = xi - 2; jj <= xi + 2; jj++) {
                                                if (Math.abs(ii - yi) == 2 && Math.abs(jj - xi) == 2)
                                                        continue;
                                                if (ii < 0 || jj < 0 || ii >= size_y || jj >= size_x)
                                                        continue;
                                                if (grid[ii][jj] != TILE_FLOOR)
                                                        adjcount_r2++;
                                        }
                                if (adjcount_r1 >= params.r1_cutoff
                                                || adjcount_r2 <= params.r2_cutoff)
                                        grid2[yi][xi] = TILE_WALL;
                                else
                                        grid2[yi][xi] = TILE_FLOOR;
                        }
                for (int yi = 1; yi < size_y - 1; yi++)
                        for (int xi = 1; xi < size_x - 1; xi++)
                                grid[yi][xi] = grid2[yi][xi];
        }
 
        public void draw(Graphics g){
        	for (int yi = 0; yi < size_y; yi++) {
                for (int xi = 0; xi < size_x; xi++) {
                        switch (grid[yi][xi]) {
                        case TILE_WALL:
                            g.setColor(new Color(50,50,50));    
                            break;
                        case TILE_FLOOR:
                        	g.setColor(new Color(150,150,150));    
                            break;
                        }
                        g.fillRect(xi*8, yi*8, 8, 8);
                }
            }
        }
        
        public void printmap() {
                for (int xi = 0; xi < size_x; xi++) {
                    	for (int yi = 0; yi < size_y; yi++) {
                                switch (grid[yi][xi]) {
                                case TILE_WALL:
                                        System.out.print('#');
                                        break;
                                case TILE_FLOOR:
                                        System.out.print('.');
                                        break;
                                }
                        }
                        System.out.println();
                }
        }
 
        public void run() {
                params_set = new GenerationParams[generations];
 
                for (int ii = 0; ii < generations; ii++) {
                        params_set[ii] = new GenerationParams();
                        params_set[ii].r1_cutoff = r1_cutoff;
                        params_set[ii].r2_cutoff = r2_cutoff;
                        params_set[ii].reps = 10;
                }
 
                initmap();
 
                for (int ii = 0; ii < generations; ii++) {
                        for (int jj = 0; jj < params_set[ii].reps; jj++)
                                generation(params_set[ii]);
                }
                //printfunc();
                printmap();
        }

}