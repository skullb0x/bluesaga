package network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import utils.ServerMessage;

/**
 * @author Tyson Domitrovits
 * @date Feb 28, 2014
 */
public class ConnectionListener implements Runnable {

  /**
   * Fields
   */
  protected Server server;
  protected ServerSocket socket;
  protected int port;
  protected boolean listening;

  /**
   * Constructor
   * @param server
   * @param port
   */
  public ConnectionListener(Server server, int port) {
    this.server = server;
    this.port = port;
  }

  @Override
  public void run() {

    ServerMessage.printMessage("ConnectionListener starting to listen on port " + port, false);

    // Initialize the server socket
    try {
      socket = new ServerSocket(port);
    } catch (IOException e) {
      ServerMessage.printMessage("ServerListener failed to start - " + e.getMessage(), false);
      System.exit(0);
    }

    // Start listening
    listening = true;

    // Start the loop for accepting connections
    while (listening) {

      try {

        // Accept a client connection
        Socket clientSocket = socket.accept();
        clientSocket.setSoTimeout(15000);

        // Add the client to the server
        Client clientObject = new Client(clientSocket);

        clientObject.IP = clientSocket.getInetAddress().getHostAddress().toString();
        ServerMessage.printMessage(clientObject.IP + " connected.", false);

        server.addClient(clientObject);

        // Start a client listener thread
        new Thread(clientObject).start();

      } catch (IOException e) {
        ServerMessage.printMessage(
            "ServerListener failed to accept connection from client - " + e.getMessage(), false);
      }
    }

    /*
    ServerMessage.printMessage("ConnectionListener starting to listen on port " + port);

    // Initialize the server socket
    try {
    	SSLServerSocketFactory sslserversocketfactory =
                      (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
    	socketSSL = (SSLServerSocket) sslserversocketfactory.createServerSocket(port);

    } catch (IOException e) {
    	ServerMessage.printMessage("ServerListener failed to start - " + e.getMessage());
    	System.exit(0);
    }

    // Start listening
    listening = true;

    // Start the loop for accepting connections
    while (listening) {

    	try {

    		// Accept a client connection
    		Socket clientSocket = socketSSL.accept();
    		clientSocket.setSoTimeout(15000);

    		// Add the client to the server
    		Client clientObject = new Client(clientSocket);
    		server.addClient(clientObject);

    		// Start a client listener thread
    		new Thread(clientObject).start();

    	} catch (IOException e) {
    		ServerMessage.printMessage("ServerListener failed to accept connection from client - " + e.getMessage());
    	}
    }
    */
  }

  public synchronized void stop() {
    this.listening = false;
  }
}
