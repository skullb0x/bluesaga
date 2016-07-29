package utils;

import java.util.HashMap;

import bounty_ranks.BountyRank;
import player_classes.*;

public class GameInfo {


	public static HashMap<Integer,BaseClass> classDef;
	public static HashMap<Integer,Integer> classNextXP;

	public static HashMap<Integer,BountyRank> bountyRanks;
	
	public static void load(){
	
		// LOAD CLASSES INFO
		classDef = new HashMap<Integer,BaseClass>();

		classDef.put(1, new WarriorClass());
		classDef.put(2, new MageClass());
		classDef.put(3, new HunterClass());
		classDef.put(4, new KnightClass());
		classDef.put(5, new FireMageClass());
		classDef.put(6, new ArcherClass());
		classDef.put(7, new TankClass());
		classDef.put(8, new IceMageClass());
		classDef.put(9, new DruidClass());
		classDef.put(10, new BerserkerClass());
		classDef.put(11, new ShockMageClass());
		classDef.put(12, new AssassinClass());
		classDef.put(14, new WhiteMageClass());
		
		// LOAD CLASS XP TABLE
		classNextXP = new HashMap<Integer, Integer>();
		int totalXP = 0;
		int oldXP = 0;
		
		classNextXP.put(1, totalXP);

		for (int lvl = 1; lvl < 400; lvl++)
		{
			if(lvl == 2){
				totalXP += 150;
				classNextXP.put(lvl+1, 150);
			}else{
				totalXP += (int) (50*Math.pow(lvl,2) - 150*lvl + 200);
				classNextXP.put(lvl+1, totalXP-oldXP);
			}
			oldXP = totalXP;
		}
		
		bountyRanks = new HashMap<Integer,BountyRank>();
		bountyRanks.put(1, new BountyRank(1,"Landlubber",1));
		bountyRanks.put(2, new BountyRank(2,"Deck Scrubber",100));
		bountyRanks.put(3, new BountyRank(3,"Scurvy",250));
		bountyRanks.put(4, new BountyRank(4,"Sailor",500));
		bountyRanks.put(5, new BountyRank(5,"Boatswain",800));
		bountyRanks.put(6, new BountyRank(6,"Pirate",1200));
		bountyRanks.put(7, new BountyRank(7,"Swashbuckler",1700));
		bountyRanks.put(8, new BountyRank(8,"Corsair",2300));
		bountyRanks.put(9, new BountyRank(9,"Lieutenant",3000));
		bountyRanks.put(10, new BountyRank(10,"Lieutenant Commander",3800));
		bountyRanks.put(11, new BountyRank(11,"Commander",4700));
		bountyRanks.put(12, new BountyRank(12,"Third Mate",5700));
		bountyRanks.put(13, new BountyRank(13,"Second Mate",6800));
		bountyRanks.put(14, new BountyRank(14,"First Mate",8000));
		bountyRanks.put(15, new BountyRank(15,"Captain",10000));
		bountyRanks.put(16, new BountyRank(16,"Commodore",14000));
		bountyRanks.put(17, new BountyRank(17,"Cutthroat",20000));
		bountyRanks.put(18, new BountyRank(18,"Vice Admiral",28000));
		bountyRanks.put(19, new BountyRank(19,"Admiral",38000));
		bountyRanks.put(20, new BountyRank(20,"Fleet Admiral",50000));
	}
	
	public static int getBountyRankId(int bounty){
		int rankId = 0;
		for(BountyRank rank: bountyRanks.values()){
			if(bounty >= rank.minBounty){
				rankId = rank.id;
			}else{
				return rankId;
			}
		}
		return rankId;
	}
	
}
