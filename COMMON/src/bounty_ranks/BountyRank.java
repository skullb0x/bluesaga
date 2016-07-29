package bounty_ranks;

public class BountyRank {
	public int id;
	public String name;
	public int minBounty;
	
	public BountyRank(int id, String name, int minBounty){
		this.id = id;
		this.name = name;
		this.minBounty = minBounty;
	}
}