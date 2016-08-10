package gui;

import graphics.BlueSagaColors;
import graphics.Font;

import java.util.Vector;

import org.newdawn.slick.Graphics;

import creature.Creature;

public class CreatureSelector {

  private Vector<Creature> creatures;
  private int selected = 0;

  public CreatureSelector(Vector<Creature> newCreatures) {
    creatures = newCreatures;
  }

  public void draw(Graphics g, int x, int y) {
    g.setColor(BlueSagaColors.RED.darker(0.2f));
    g.fillRoundRect(x, y, 120, 120, 10);

    g.setColor(BlueSagaColors.RED);
    g.fillRoundRect(x + 5, y + 5, 110, 110, 8);

    g.setColor(BlueSagaColors.WHITE);
    g.setFont(Font.size12bold);

    String family = creatures.get(selected).getFamily();
    g.drawString(family, x + 120 - Font.size12bold.getWidth(family), y + 100);
  }
}
