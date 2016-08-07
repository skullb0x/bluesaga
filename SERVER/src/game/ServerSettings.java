package game;

public class ServerSettings {
	
	/**
	 * Register a new server at http://www.bluesaga.org/newserver
	 * 
	 * Copy the server id given on the site and paste it instead of the default value next to SERVER_ID
	 */
	public static final int SERVER_ID = 1;

	/**
	 * If you made updates to the client, change the client version number,
	 * be sure to set the same number in your server settings on the website
	 * http://www.bluesaga.org/myservers
	 */
	public static final String CLIENT_VERSION = "724";
	
	/**
	 * If you want to test the server locally while developing, use DEV_MODE = true
	 * Set DEV_MODE = false when running it live.
	 */
	public static boolean DEV_MODE = false;

	// Network settings
	public static int PORT = 26342;
	
	// Game settings
	public final static boolean PVP = true;
	public final static int LEVEL_CAP = 50;
	public final static int CLASS_LEVEL_CAP = 10;
	
	// Server restart time in milliseconds
	public static int restartTime = (4*60*60)*1000; // 4 hours (+15 min for warning)
	
	// CLIENT SCREEN SIZE
	public static int TILE_HALF_W = 18;
	public static int TILE_HALF_H = 10;
	public static int TILE_SIZE = 50;
}
