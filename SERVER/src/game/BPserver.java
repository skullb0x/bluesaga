package game;

import network.Server;
import utils.CrashLogger;

public class BPserver {

	/******************************
	 * 							*
	 *    GLOBAL SERVER METHODS	*
	 * 							*
	 ******************************/


	public static void main(String args[]) throws Exception {
		
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				// Log crashes
				CrashLogger.uncaughtException(e);
				
				// Restart server if uncaught exception occur
				Server.restartServer();
			}
		});
	
		Server s = new Server() {
			@Override
			protected void update(int delta) {}

			@Override
			protected void init() {}
		};
		
		s.start();
	}
}