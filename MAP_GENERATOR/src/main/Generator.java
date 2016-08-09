package main;

import generators.CaveGenerator;
import generators.ArchipelagoGenerator;
import generators.IslandGenerator;
import generators.PirateIslandGenerator;

import org.newdawn.slick.BasicGame;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.AppGameContainer;

import utils.RandomUtils;

public class Generator extends BasicGame {

  public static String type = "Pirate Island";

  public static AppGameContainer app;

  public static boolean generating = false;

  public static CaveGenerator cg;
  public static IslandGenerator ig;
  public static ArchipelagoGenerator ag;
  public static PirateIslandGenerator pg;

  public Generator() {
    super("Map Generator");
  }

  @Override
  public void init(GameContainer container) throws SlickException {

    //generateMap();

    if (type.equals("Cave")) {
      cg = new CaveGenerator();
    } else if (type.equals("Island")) {
      ig = new IslandGenerator();
    } else if (type.equals("Archipelago")) {
      ag = new ArchipelagoGenerator();
    } else if (type.equals("Pirate Island")) {
      pg = new PirateIslandGenerator();
    }

    generateMap();
  }

  @Override
  public void update(GameContainer container, int delta) throws SlickException {

    Input input = container.getInput();

    if (input.isKeyPressed(Input.KEY_SPACE) && !generating) {
      generateMap();
    } else if (input.isKeyPressed(Input.KEY_ESCAPE)) {
      System.exit(0);
    }
  }

  @Override
  public void render(GameContainer container, Graphics g) throws SlickException {
    //MoAGenerator.draw(g);

    if (type.equals("Cave")) {
      cg.draw(g);
    } else if (type.equals("Island")) {
      ig.draw(g);
    } else if (type.equals("Archipelago")) {
      ag.draw(g);
    } else if (type.equals("Pirate Island")) {
      pg.draw(g);
    }
  }

  public static void main(String[] args) {

    try {
      app = new AppGameContainer(new Generator());

      app.setDisplayMode(1024, 640, false);
      app.setTargetFrameRate(60);
      app.setShowFPS(true);
      app.setAlwaysRender(true);
      app.setVSync(false);
      app.start();
    } catch (SlickException e) {
      e.printStackTrace();
    }
  }

  public static void generateMap() {
    generating = true;

    if (type.equals("Cave")) {
      int caveSize = RandomUtils.getInt(128, 128);
      cg.generate(caveSize, caveSize);
      generating = false;
      cg.printmap();
    } else if (type.equals("Island")) {
      ig.generate();
      generating = false;
      ig.printmap();
    } else if (type.equals("Archipelago")) {
      int nrIslands = RandomUtils.getInt(100, 200);
      ag.generate(nrIslands);
      generating = false;
      ag.printmap();
    } else if (type.equals("Pirate Island")) {
      pg.generate();
      generating = false;
      //pg.printmap();
    }
  }
}
