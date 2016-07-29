package utils;

import java.util.HashMap;

public class XPTables {
	// LEVEL AND SKILL INFO
	public static HashMap<Integer,Integer> nextLevelXP = new HashMap<Integer,Integer>(); 
	public static HashMap<Integer,Integer> totalLevelXP = new HashMap<Integer,Integer>(); 

	public static HashMap<Integer,Integer> nextLevelSP = new HashMap<Integer,Integer>(); 
	public static HashMap<Integer,Integer> totalLevelSP = new HashMap<Integer,Integer>(); 

	public static void init(){
		// LEVEL UP XP INFO
		nextLevelXP.clear();
		totalLevelXP.clear();

		nextLevelSP.clear();
		totalLevelSP.clear();

		int oldxp = 0;
		int totalxp = 0;

		int maxlevel = 400; // last level to display

		totalLevelXP.put(1, totalxp);

		for (int lvl = 1; lvl < maxlevel; lvl++)
		{
			if(lvl == 2){
				totalxp += 150;
				nextLevelXP.put(lvl+1, 150);
			}else{
				totalxp += (int) (50*Math.pow(lvl,2) - 150*lvl + 200);
				nextLevelXP.put(lvl+1, totalxp-oldxp);
			}
			totalLevelXP.put(lvl+1, totalxp);
			oldxp = totalxp;
		}


		int oldsp = 0;
		int totalsp = 0;

		totalLevelSP.put(1, totalsp);

		for (int lvl = 1; lvl < maxlevel; lvl++)
		{
			if(lvl == 2){
				totalsp += 150*4;
				nextLevelSP.put(lvl+1, 150*4);
			}else{
				totalsp += (int) (50*Math.pow(lvl,2) - 150*lvl + 200)*4;
				nextLevelSP.put(lvl+1, totalsp-oldsp);
			}
			totalLevelSP.put(lvl+1, totalsp);

			oldsp = totalsp;
		}

	
	}
	
	public static int getLevelBySP(int SP){
		for (int lvl = 2; lvl < 100; lvl++)
		{
			if(totalLevelSP.get(lvl) > SP){
				return lvl - 1;
			}
		}
		return 1;
	}

	public static int getLevelByXP(int XP){
		for (int lvl = 2; lvl < 100; lvl++)
		{
			if(totalLevelXP.get(lvl) > XP){
				return lvl - 1;
			}
		}
		return 1;
	}
	

}
