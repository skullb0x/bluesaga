package network;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import java.lang.management.ManagementFactory;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MalformedObjectNameException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import components.md5;
import map.WorldMap;
import utils.CrashLogger;
import utils.FileCopy;
import utils.GameInfo;
import utils.LanguageSupport;
import utils.ServerGameInfo;
import utils.PlayerFix;
import utils.ServerMessage;
import utils.TimeUtils;
import utils.WebHandler;
import utils.XPTables;
import data_handlers.ConnectHandler;
import data_handlers.DataHandlers;
import data_handlers.Handler;
import game.Database;
import game.ServerSettings;

public abstract class Server {

	
	// Databases
	public static Database gameDB;
	public static Database mapDB;
	public static Database userDB;

	// World map
	public static WorldMap WORLD_MAP;

	// Timers
	public static Timer closeTimer;
	public static Timer restartTimer;
	
	public static md5 MD5 = new md5();

	public static boolean SERVER_RESTARTING = false;

	// Ticks per second
	private float ticksPerSecond = 20;
	protected float targetTPS;
	protected int actualTPS;
	
	// Nanoseconds per tick
	private float nsPerTick;
	
	// Milliseconds per tick
	private float msPerTick;
	
	// Global tick count
	protected static long tick = 0;
	
	// Running state
	protected boolean running;
	
	// Connection Listener
	private ConnectionListener connectionListener;
	
	// Clients
	public static int clientIndex = 0;
	public static int newClientIndex = 0;
	public static ConcurrentHashMap<Integer, Client> clients;
	
  private Timers mb_timers;
	
	/**
	 * Constructor
	 * @param ticksPerSecond
	 * @param port
	 */
	public Server() {
		setTargetTPS(ticksPerSecond);	
		init();
	}
	
	
	/**
	 * Public Methods
	 */
	
	// Start the server
	public synchronized void start() {
		mb_timers = new Timers();  // Timers implements TimersMBean
		mb_timers.setInitBeginTime(System.currentTimeMillis());

		try {
			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
			ObjectName name = new ObjectName("BlueSaga:type=Server,name=Timers");
			mbs.registerMBean(mb_timers, name);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		
		clients = new ConcurrentHashMap<Integer, Client>();
		
		if(ServerSettings.DEV_MODE){
			ServerMessage.printMessage("DEV_MODE",false);
		}

		ServerMessage.printMessage("PVP: "+ServerSettings.PVP,false);
		
		ServerMessage.printMessage("Starting server v0."+ServerSettings.CLIENT_VERSION,false);

		
		// Initialize all databases
		try {
			gameDB = new Database("game");
			mapDB = new Database("map");
			userDB = new Database("users");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		// Generate language json-file
		LanguageSupport.loadLanguageFile("");
		
		XPTables.init();

		ServerGameInfo.load();
		GameInfo.load();
	
		PlayerFix.dbFix(false);
	
		// Prepare datahandlers
		DataHandlers.init();

		// Load world map
		WORLD_MAP = new WorldMap();
		WORLD_MAP.loadMap();
		
		ServerMessage.printMessage("Started server processes...",false);
		
		restartTimer = new Timer();

		restartTimer.schedule( new TimerTask(){
			@Override
			public void run() {
				for (Map.Entry<Integer, Client> entry : clients.entrySet()) {
					Client s = entry.getValue();
					if(s.Ready){
						Handler.addOutGoingMessage(s,"message","#messages.server.restart_fifteen");
					}
				}
				sendRestartWarning();
			}
		}, ServerSettings.restartTime);
		
			
		// Start server connection
		
		ServerMessage.printMessage("Initializing connection...",false);

		connectionListener = new ConnectionListener(this, ServerSettings.PORT);
		new Thread(connectionListener).start();
		
		ServerMessage.printMessage("Server is ready and waiting for clients!",false);

		running = true;
		
		// Begin the loop
		serverLoop();
	}
	
	// Stop the server
	public synchronized void stop() {
		// Stop the connection listener
		connectionListener.stop();
		
		// Update running state
		running = false;
	}
	
	
	/**
	 * Implementation-based Methods
	 */
	
	protected abstract void init();
	
	protected abstract void update(int delta);
	
	
	
	/**
	 * Accessory Methods
	 */
	
	public void setTargetTPS(float targetTPS) {
		this.targetTPS = targetTPS;
		nsPerTick = TimeUtils.NS_PER_SEC / targetTPS;
		msPerTick = nsPerTick / TimeUtils.NS_PER_MS;
	}
	
	public long getTick() {
		return tick;
	}	
	
	private void serverLoop() {

		// Initialize last tick/second time
		long lastTickTime = TimeUtils.nanos();
		long lastSecondTime = TimeUtils.nanos();

		// Initialize the live tick counter for TPS monitoring
		int ticksCount = 0;
		
		long t_incoming = 0L;
		long t_update = 0L;
		long t_outgoing = 0L;
		long t_cleanup = 0L;

		mb_timers.setRunningBeginTime(System.currentTimeMillis());
		while (running) {

			// Current time
			long nowTime = TimeUtils.nanos();

			// The delta time in ms since last tick
			int delta = (int)((nowTime - lastTickTime) / TimeUtils.NS_PER_MS);

			// Once the proper tick interval has passed, perform an update
			if (delta >= msPerTick) {
				lastTickTime = nowTime;
				ticksCount++;
				tick++;

				if(!SERVER_RESTARTING){
					long t0 = System.currentTimeMillis();
					
					// 1. Process queued requests
					DataHandlers.processIncomingData();
					long t1 = System.currentTimeMillis();
					t_incoming += t1 - t0;
					t0 = t1;
					
					// 2. Update the models
					DataHandlers.update(tick);
					t1 = System.currentTimeMillis();
					t_update += t1 - t0;
					t0 = t1;
					
					// 3. Notify clients of queued relevant changes
					DataHandlers.processOutgoingData();
					t1 = System.currentTimeMillis();
					t_outgoing += t1 - t0;
					t0 = t1;
					
					// 4. Remove deleted clients
					removeClients();
					t1 = System.currentTimeMillis();
					t_cleanup += t1 - t0;
				}
			} 

			// Update TPS counter each second
			if ((nowTime - lastSecondTime) / TimeUtils.NS_PER_MS >= TimeUtils.MS_PER_SEC) {
				lastSecondTime = nowTime;
				actualTPS = ticksCount;
				ticksCount = 0;
				mb_timers.updateLoopTime(System.currentTimeMillis(), t_incoming, t_update, t_outgoing, t_cleanup);
				mb_timers.updateTicksPerSecond(actualTPS);
			}
			
			try {
				Thread.currentThread().sleep(10L);
			} catch (InterruptedException ex) {
				return;
			}
		}
	}


	public void removeClients(){
		boolean removedClient = false;
		Iterator<Map.Entry<Integer, Client>> iterator = clients.entrySet().iterator();
		while(iterator.hasNext()){
		   Map.Entry<Integer, Client> client = iterator.next();
		   
		   if(client.getValue().RemoveMe){
			   ConnectHandler.removeClient(client.getValue());
			   iterator.remove();
			   removedClient = true;
		   }             
		}
		
		if(removedClient){
			ServerMessage.printMessage("Removed client, players online: "+clients.size(),false);
		}
	}


	public void sendRestartWarning(){
		restartTimer.schedule( new TimerTask(){
			@Override
			public void run() {
				// Send server restart warning to all clients
				for (Map.Entry<Integer, Client> entry : clients.entrySet()) {
					Client s = entry.getValue();
					if(s.Ready){
						Handler.addOutGoingMessage(s,"message","#messages.server.restart_one");
					}
				}
				sendLastRestartWarning();
			}
		}, 13 * 60 * 1000);
	}

	public void sendLastRestartWarning(){
		restartTimer.schedule( new TimerTask(){
			@Override
			public void run() {
				restartServer();
			}
		}, 60 * 1000);
	}

	public static void restartServer()
	{
		ServerMessage.printMessage("Restarting server...",false);

		SERVER_RESTARTING = true;
		ServerMessage.printMessage("Saving player data before restart...",false);

		for (Map.Entry<Integer, Client> entry : clients.entrySet()) {
			Client s = entry.getValue();
			ConnectHandler.removeClient(s);
		}

		gameDB.closeDB();
		userDB.closeDB();
		mapDB.closeDB();
		
		// BACKUP SERVER
		FileCopy.backupDB();	
		
		Runtime re = Runtime.getRuntime();
		try{ 
			re.exec("java -jar BPserver.jar"); 
		} catch (IOException ioe){
			CrashLogger.uncaughtException(ioe);
		}

		closeTimer = new Timer();
		closeTimer.schedule( new TimerTask(){
			@Override
			public void run() {
				ServerMessage.printMessage("RESTART SERVER!",false);
				System.exit(0);
			}
		}, 1000);
	}

	public static void stopServer(){

		SERVER_RESTARTING = true;

		ServerMessage.printMessage("Saving player data before stopping...",false);

		for (Map.Entry<Integer, Client> entry : clients.entrySet()) {
			Client s = entry.getValue();

			if(s.playerCharacter != null){
				s.playerCharacter.saveInfo();
			}
		}

		gameDB.closeDB();
		userDB.closeDB();
		mapDB.closeDB();
		
		// You need to create a folder called "db_backups" in the server folder
		// When you have done so, you can uncomment this line
		//FileCopy.backupDB();	
		
		closeTimer = new Timer();
		closeTimer.schedule( new TimerTask(){
			@Override
			public void run() {
				ServerMessage.printMessage("STOP SERVER!",false);
				System.exit(0);
			}
		}, 1000);
	}


	public void addClient(Client client){
		clients.put(clientIndex, client);
		clientIndex++;
	}
	
}
