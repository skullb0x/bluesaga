package utils;

import java.util.concurrent.ThreadLocalRandom;

public class RandomUtils {
	public static int getInt(int min, int max) {
		return ThreadLocalRandom.current().nextInt(max - min + 1) + min;
	}
	
	public static float getFloat(float minf, float maxf){
		return ThreadLocalRandom.current().nextFloat()*(maxf-minf) + minf;
	}
	
	public static double getGaussian(){
		return ThreadLocalRandom.current().nextGaussian();
	}

}