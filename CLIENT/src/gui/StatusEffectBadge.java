package gui;

import graphics.ImageResource;

import org.newdawn.slick.Graphics;

import abilitysystem.StatusEffect;

public class StatusEffectBadge {

	public void draw(Graphics g, int x, int y, int mouseX, int mouseY){
		ImageResource.getSprite("gui/world/statuseffect_badge").draw(x, y);
	}
	
}
