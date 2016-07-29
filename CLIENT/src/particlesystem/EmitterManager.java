package particlesystem;

import particlesystem.Emitter.Emitter;
import particlesystem.Emitter.EmitterContainer;
import particlesystem.Emitter.EmitterType;
import particlesystem.Particle.ParticleContainer;
import particlesystem.Particle.ParticleType;
import screens.Camera;
import game.Database;

import java.util.Random;
import java.util.Vector;

import org.newdawn.slick.Graphics;

public class EmitterManager {

	private Vector<Emitter> myEmitters;

	private EmitterContainer myEmitterContainer;
	private ParticleContainer myParticleContainer;

	public EmitterManager(Database aGameDB) {

		myEmitters = new Vector<Emitter>();
		myEmitterContainer = new EmitterContainer(aGameDB);
		myParticleContainer = new ParticleContainer(aGameDB);

	}

	public void Update(float aElapsedTime) {
		
		for (int index = 0; index < myEmitters.size(); ++index) {
			if(myEmitters.get(index) != null){
				myEmitters.get(index).Update(aElapsedTime);
			}
		}

		// Remove emitters
		for (int index = 0; index < myEmitters.size(); ++index) {
			if (myEmitters.get(index).ShouldBeRemoved()) {
				myEmitters.remove(index);
			}
		}

	}

	public void Draw(Graphics g, Camera aCamera) {
		for (int index = 0; index < myEmitters.size(); ++index) {
			if(myEmitters.get(index) != null){
				myEmitters.get(index).Render(g, aCamera);
			}
		}
	}

	public Emitter SpawnEmitter(int aXPos, int aYPos, String aEmitterName) {

		EmitterType emitterType = myEmitterContainer.GetEmitterByName(aEmitterName);
		
		if(emitterType != null){
		
			int particleID = emitterType.myParticleID;
			
			ParticleType particleType = myParticleContainer.GetParticleByID(particleID);
	
			Emitter newEmitter = new Emitter(aXPos, aYPos, emitterType, particleType);
			myEmitters.add(newEmitter);
			return newEmitter;
		}
		return null;
	}

	public Emitter SpawnEmitter(int aXPos, int aYPos, EmitterType aEmitterType) {
		
		int particleID = aEmitterType.myParticleID;
		
		ParticleType particleType = myParticleContainer.GetParticleByID(particleID);

		Emitter newEmitter = new Emitter(aXPos, aYPos, aEmitterType, particleType);
		myEmitters.add(newEmitter);

		return newEmitter;
	}

	public Emitter SpawnEmitter(int aXPos, int aYPos, EmitterType aEmitterType, ParticleType aParticleType) {

		Emitter newEmitter = new Emitter(aXPos, aYPos, aEmitterType, aParticleType);
		myEmitters.add(newEmitter);

		return newEmitter;
	}

	public void RemoveAllEmitters() {
		myEmitters.removeAllElements();
	}
	
	public EmitterContainer GetEmitterContainer() {
		return myEmitterContainer;
	}
	
	public ParticleContainer GetParticleContainer() {
		return myParticleContainer;
	}
	
	public static float randomFloat(float aMinValue, float aMaxValue,
			Random aRandom) {
		int maxRand = 30000;
		int rand = aRandom.nextInt(maxRand);
		float randomFloat = (float) (rand) / (float) (maxRand);
		float dif = aMaxValue - aMinValue;
		float result = aMinValue + (randomFloat * dif);
		return result;
	}
}
