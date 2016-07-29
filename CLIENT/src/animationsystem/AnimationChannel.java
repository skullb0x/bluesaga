package animationsystem;

import java.util.Vector;

public class AnimationChannel {

	private Vector<PartAnimation> animations;
	private float waitItr = 0.0f;
	
	public AnimationChannel(){
		animations = new Vector<PartAnimation>();
	}
	
	/**
	 * Constructor with time parameter
	 * @param wait frames to wait till start
	 */
	public AnimationChannel(float waitItr){
		animations = new Vector<PartAnimation>();
		setWaitItr(waitItr);
	}
	
	public void add(PartAnimation animation){
		animations.add(animation);
	}
	
	public PartAnimation get(int position){
		return animations.get(position);
	}
	
	public void remove(int position){
		animations.remove(position);
	}
	
	public int size(){
		return animations.size();
	}

	public boolean isActive() {
		if(waitItr > 0){
			waitItr--;
			return false;
		}
		return true;
	}

	public void setWaitItr(float waitItr) {
		this.waitItr = waitItr;
	}

	
}
