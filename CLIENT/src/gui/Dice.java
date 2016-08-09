package gui;

import graphics.BlueSagaColors;
import graphics.Font;
import graphics.ImageResource;

import java.util.Collections;
import java.util.Vector;

import org.newdawn.slick.Graphics;

public class Dice {
  private Vector<Integer> faces;
  private int aniItr = 0;
  private int aniDuration = 10;
  private int faceNr = 0;
  private int facesTotal = 6;
  private int nrSpins = 0;
  private int totalSpins = 0;
  private int result;

  private boolean doneSpinning;
  private boolean active;

  public Dice() {
    active = false;
    faces = new Vector<Integer>();
    faces.add(1);
    faces.add(2);
    faces.add(3);
    faces.add(4);
    faces.add(5);
    faces.add(6);
  }

  public void roll(int result) {
    doneSpinning = false;
    this.result = result;
    totalSpins = 2;
    nrSpins = 0;
    aniItr = 0;
    faceNr = 0;
    Collections.shuffle(faces);
    active = true;
  }

  public void draw(Graphics g, int x, int y) {
    if (active && !doneSpinning) {
      aniItr++;
      if (aniItr > aniDuration) {
        aniItr = 0;
        faceNr++;
        if (faceNr >= facesTotal) {
          faceNr = 0;
        }
        if ((faceNr + 1) == result) {
          nrSpins++;
          if (nrSpins > totalSpins) {
            doneSpinning = true;
            aniItr = 0;
          }
        }
      }
      ImageResource.getSprite("gui/dice/dice" + faces.get(faceNr)).draw(x - 40, y);
      g.setFont(Font.size18);
      g.setColor(BlueSagaColors.BLACK);
      g.drawString("ROLL!", x - 10, y + 3);
      g.setColor(BlueSagaColors.WHITE);
      g.drawString("ROLL!", x - 9, y + 2);

    } else if (active) {
      aniItr++;
      ImageResource.getSprite("gui/dice/dice" + result).draw(x - 20, y);
      g.setFont(Font.size18);
      g.setColor(BlueSagaColors.BLACK);
      g.drawString(result + "!", x + 10, y + 3);
      g.setColor(BlueSagaColors.WHITE);
      g.drawString(result + "!", x + 9, y + 2);
      if (aniItr > 120) {
        active = false;
      }
    }
  }
}
