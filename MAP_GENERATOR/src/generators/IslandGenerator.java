package generators;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

public class IslandGenerator {
  private static int[][] map;
  private static int mapWidth = 0;
  private static int mapHeight = 0;

  public static void generate() {
    MidpointDisplacement md = new MidpointDisplacement(false);
    map = md.getMap();
    mapWidth = map.length;
    mapHeight = map[0].length;
  }

  public static void printmap() {
    for (int xi = 0; xi < mapWidth; xi++) {
      for (int yi = 0; yi < mapHeight; yi++) {
        System.out.print(map[yi][xi]);
      }
      System.out.println();
    }
  }

  public static void draw(Graphics g) {
    for (int x = 0; x < mapWidth; x++) {
      for (int y = 0; y < mapHeight; y++) {
        int tileType = map[x][y];

        if (tileType == 0) {
          // Void
          g.setColor(new Color(0, 0, 0));
        } else if (tileType == 1) {
          // Deep water
          g.setColor(new Color(24, 100, 165));
        } else if (tileType == 2) {
          // Shallow water
          g.setColor(new Color(90, 161, 222));
        } else if (tileType == 3) {
          // Beach
          g.setColor(new Color(242, 255, 153));
        } else if (tileType == 4) {
          // Grass
          g.setColor(new Color(206, 255, 153));
        } else if (tileType == 5) {
          // Height 1
          g.setColor(new Color(168, 223, 108));
        } else if (tileType == 6) {
          // Height 2
          g.setColor(new Color(112, 159, 62));
        } else if (tileType == 7) {
          // Height 3
          g.setColor(new Color(175, 175, 175));
        } else if (tileType == 8) {
          // Height 4
          g.setColor(new Color(255, 255, 255));
        }

        if (tileType > 0) {
          g.fillRect(x, y, 1, 1);
        }
      }
    }
  }
}
