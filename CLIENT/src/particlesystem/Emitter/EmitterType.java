package particlesystem.Emitter;

import org.newdawn.slick.geom.Vector2f;

public final class EmitterType {

	public int myID;
	public String myName;
	public float myLifetime;
	public boolean myIsUnlimited;
	public float myEmittionRate;
	public float myRotationSpeed;
	public Vector2f myMinPos;
	public Vector2f myMaxPos;

	public boolean myShouldShowParticles;
	public int myParticleID;
	public boolean myShouldShowStreaks;
	public int myStreakID;

}
