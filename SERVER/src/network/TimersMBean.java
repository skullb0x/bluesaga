package network;

public interface TimersMBean {

	public int getActualTPS();
	public long getInitDurationMs();

	public double getPercentIncoming();
	public double getPercentUpdate();
	public double getPercentOutgoing();
	public double getPercentCleanup();
}
