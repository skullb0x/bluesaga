package network;

public class Timers implements TimersMBean {

	private long t_init = 0L;
	private long t_begin = 0L;
	
	private double t_incoming = 0.0;
	private double t_update = 0.0;
	private double t_outgoing = 0.0;
	private double t_cleanup = 0.0;
	
	private int actualTPS = 0;

	public void updateTicksPerSecond(int val) { actualTPS = val; }
	public void setInitBeginTime(long val) { t_init = val; }
	public void setRunningBeginTime(long val) { t_begin = val; }
	
	public void updateLoopTime(long now, long incoming, long update,long outgoing, long cleanup) {
		double denom = 1.0 * (now - t_begin);
		t_incoming  = incoming / denom;
		t_update    = update / denom;
		t_outgoing  = outgoing / denom;
		t_cleanup   = cleanup / denom;
	}
	
	public int getActualTPS() { return actualTPS; }
	public long getInitDurationMs() { return (t_begin - t_init); }

	public double getPercentIncoming() { return t_incoming; }
	public double getPercentUpdate()   { return t_update; }
	public double getPercentOutgoing() { return t_outgoing ;}
	public double getPercentCleanup()  { return t_cleanup; }
}
