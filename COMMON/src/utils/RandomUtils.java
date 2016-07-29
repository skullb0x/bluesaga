package utils;

import java.util.Random;

public class RandomUtils {
	private static Random random = new Random();

	public static int getInt(int min, int max) {
		return random.nextInt(max - min + 1) + min;
	}
	
	public static float getFloat(float minf, float maxf){
		return random.nextFloat()*(maxf-minf) + minf;
	}
	
	public static double getGaussian(){
		return random.nextGaussian();
	}

}