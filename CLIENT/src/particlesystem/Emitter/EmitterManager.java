package particlesystem.Emitter;

import particlesystem.Particle.ParticleType;
import screens.Camera;
import screens.ScreenHandler;

import java.util.Vector;

import org.newdawn.slick.Graphics;

public class EmitterManager {

	private Vector<Emitter> myEmitters;

	public EmitterManager() {
		myEmitters = new Vector<Emitter>();
	}

	public void Update(float aElapsedTime) {

		for (Emitter emitter : myEmitters) {
			emitter.Update(aElapsedTime);
		}

		// Remove emitters
		for (int index = 0; index < myEmitters.size(); ++index) {
			if (myEmitters.elementAt(index).ShouldBeRemoved()) {
				myEmitters.remove(index);
			}
		}

	}

	public void Draw(Graphics g, Camera aCamera) {

		for (Emitter emitter : myEmitters) {
			emitter.Render(g, aCamera);
		}

	}

	public Emitter SpawnEmitter(int aXPos, int aYPos, String aEmitterName) {

		EmitterType emitterType = ScreenHandler.myEmitterContainer.GetEmitterByName(aEmitterName);
		
		int particleID = emitterType.myParticleID;
		
		ParticleType particleType = ScreenHandler.myParticleContainer.GetParticleByID(particleID);

		Emitter newEmitter = new Emitter(aXPos, aYPos, emitterType, particleType);
		myEmitters.add(newEmitter);

		return newEmitter;
	}

	public Emitter SpawnEmitter(float aXPos, float aYPos, EmitterType aEmitterType) {
		
		int particleID = aEmitterType.myParticleID;
		
		ParticleType particleType = ScreenHandler.myParticleContainer.GetParticleByID(particleID);

		Emitter newEmitter = new Emitter(aXPos, aYPos, aEmitterType, particleType);
		myEmitters.add(newEmitter);

		return newEmitter;
	}

	public void RemoveAllEmitters() {
		myEmitters.removeAllElements();
	}
	
}
