import java.net.*;
import java.util.*;
import java.io.*;

/*************************
 * 
 * Host Server
 * 
 * This class is a part of the host in this project, being initiated by it. This
 * part of the host, keeps and open connection for other host to connect and
 * request files. The request host client will know
 * 
 * @author Javier Ramirez
 *
 *************************/
public class DealerServer extends Thread {

	// private static final int serverPort = 1234;
	// private static ServerSocket serverSocket;

	/* Brendon Version Nov 16, 2017 */
	private static int welcomePort = 1235;
	private static ServerSocket welcomeSocket;

	private boolean successfullySetFTPPort = false;

	public DealerServer() {
		/* Show server starting status */
		System.out.println(" ");
		System.out.println("Client-As-FTP-Server initialized. Waiting " + "for connections...");
		for (int i = 0; i < 5; i++) {
			/* Initialize the welcome socket */
			try {
				welcomeSocket = new ServerSocket(welcomePort);
				successfullySetFTPPort = true;

				/* For debugging */
				System.out.println("  DEBUG-05: FTP-Welcome Port: " + welcomePort);

				/* Stop the loop if found a good port */
				break;
			} catch (IOException ioEx) {
				/*
				 * Setting a port for another host to connect to. This is setup to handle the
				 * case of multiple users on one computer. Without this loop and catching, the
				 * second user immediately breaks.
				 */
				welcomePort = welcomePort + 1;

				/* For debugging */
				System.out.println("  DEBUG-06: Changing the port number.");
			}
		}

		/* If unable to setup a port for later FTP connections */
		if (successfullySetFTPPort == false) {
			/* Throw exception to the GUI */
			throw new EmptyStackException();
		}
	}

	/* This is the "threaded" part of this class */
	public void run() {
		/* Perform a loop to wait for new connections */
		try {
			do {
				/* Wait for client... */
				Socket connectionSocket = welcomeSocket.accept();

				System.out.println("\nClient-As-FTP-Server: New Client Connected!");

				/* For debugging */
				// System.out.println(" DEBUG: New Connection's IP: " +
				// connectionSocket.getInetAddress());

				/*
				 * Create a thread to handle communication with this client and pass the
				 * constructor for this thread a reference to the relevant socket and user IP.
				 */
				FTPClientHandler handler = new FTPClientHandler(connectionSocket);

				/* Start a new thread for this client */
				handler.start();
			} while (true);
		} catch (Exception e) {
			System.out.println("ERROR-01: Failure in setting up a thread.");
		}
	}

	/*
	 * Once the host-FTP-server is setup, we know it's welcomePort. THEN, this needs
	 * to be passed to the Central-Server to allow others to download files from
	 * this host. This function helps in this process.
	 */
	public String getFTPWelcomePort() {
		return Integer.toString(welcomePort);
	}
	/* End of Entire HostServer Class */
}

/***********************************************************************
 *
 * This class handles allows for threading and handling the various clients that
 * are connected to server simultaneously.
 *
 **********************************************************************/
class FTPClientHandler extends Thread {

	/** This handles the stream from the command-line of client */
	private Scanner inScanFromClient;

	/** This is used to grab bytes over the data-line */
	private String recvMsg;

	/** This acts as a deck for gameplay. */
	public ArrayList<Card> deck = Card.setDeck();
	/*******************************************************************
	 *
	 * Beginning of thread. This constructor marks the beginning of a thread on the
	 * server. Things here happen once, exclusively with THIS connected client.
	 *
	 ******************************************************************/
	public FTPClientHandler(Socket controlListen) {
		try {
			/* Setting up a threaded input control-stream */
			inScanFromClient = new Scanner(controlListen.getInputStream());
			/* Setting up a threaded output control-stream */
			/* For error handling */
			/* Get IP from the control socket for future connections */
			System.out.println("A new thread was successfully setup.");
			System.out.println("");
		} catch (IOException ioEx) {
			ioEx.printStackTrace();
			System.out.println("ERROR: Could not set up a " + "threaded client input and output stream.");
		}
	}

	/******************************************************************
	 *
	 * Beginning of main thread code. This method marks the threaded area that this
	 * client receives commands and handles. When this receives "QUIT" from the
	 * client, the thread closes.
	 * 
	 ******************************************************************/
	public void run() {
		boolean stayAlive = true;

		ArrayList<Card> deck = new ArrayList<Card>();
		
		while (stayAlive) {

			// Try to get input from client
			try {
				recvMsg = inScanFromClient.nextLine();
				System.out.println("RECEIVED MESSAGE: " + recvMsg);
			} catch (Exception e) {
				System.out.println("");
				System.out.println("NO INPUT FROM CLIENT");
				break;
			}

			StringTokenizer tokens = new StringTokenizer(recvMsg);
			String commandToken = tokens.nextToken();

			if (commandToken.toLowerCase().equals("quit")) {
				try {
					System.out.println("Disconnected!");

				} catch (Exception e) {
					System.out.println("  ERROR: Closing connection error");
				}
			} else if (commandToken.toLowerCase().equals("winclient")) {
				// Tells the client that they won.
				GUIClient.incrementWins();
			} else if (commandToken.toLowerCase().equals("lossclient")) {
				// Tells the client that they lost.
				GUIClient.incrementLosses();
			} else if (commandToken.equals("usernamedealer")) {
				// Tells the client that they won.
				GUIClient.whoAmIFacing(tokens.nextToken());
			} else if (commandToken.equals("usernameclient")) {
				// Tells the client that they lost.
				GUIDealer.whoAmIFacing(tokens.nextToken());
			} else if (commandToken.toLowerCase().equals("windealer")) {
				// Tells the dealer that they won.
				GUIDealer.incrementWins();
			} else if (commandToken.toLowerCase().equals("windealerlossclient")) {
				// Tells the dealer that they won.
				GUIClient.incrementLosses();
			} else if (commandToken.toLowerCase().equals("winclientlossdealer")) {
				// Tells the dealer that they won.
				GUIClient.incrementWins();
			} else if (commandToken.toLowerCase().equals("lossdealer")) {
				// Tells the dealer that they lost.
				GUIDealer.incrementLosses();
			} else if (commandToken.toLowerCase().equals("staydealer")) {
				GUIClient.stayChange();
			} else if (commandToken.toLowerCase().equals("stayclient")) {
				String clientScoreStr = tokens.nextToken();
				int clientScore = Integer.parseInt(clientScoreStr);
				int dealerScore = GUIDealer.getValue();
				
				System.out.println("Client: " + clientScore + "\nDealer: " + dealerScore);
				
				if (clientScore > dealerScore) {
					GUIDealer.incrementLosses();
					GUIDealer.enableNewGame("win");
				}else if (dealerScore >= clientScore) {
					GUIDealer.incrementWins();
					GUIDealer.enableNewGame("loss");
				}
			} else if (commandToken.toLowerCase().equals("resetfromdealer")) {
				GUIClient.reset();
			} else if (commandToken.toLowerCase().equals("resetfromclient")) {
				GUIDealer.resetPlayer();
			} else if (commandToken.toLowerCase().equals("deck")) {
				Card card = new Card();
				card.name = tokens.nextToken();
				card.suit = tokens.nextToken();
				card.value = Integer.parseInt(tokens.nextToken());
				deck.add(card);				
			} else if (commandToken.toLowerCase().equals("senddeck")) {
				GUIClient.receiveDeck(deck);
			} else if (commandToken.toLowerCase().equals("dealersend")) {
				String nameToSend = tokens.nextToken();
				String suitToSend = tokens.nextToken();
				System.out.println(nameToSend + " " + suitToSend);
				GUIClient.updateDealer(nameToSend, suitToSend);
			} else if (commandToken.toLowerCase().equals("dealerreceive")) {
				String nameToSend = tokens.nextToken();
				String suitToSend = tokens.nextToken();
				System.out.println(nameToSend + " " + suitToSend);
				GUIDealer.updatePlayer(nameToSend, suitToSend);
			} else if (commandToken.equals("Two") || commandToken.equals("Three") || commandToken.equals("Four") 
					|| commandToken.equals("Five") || commandToken.equals("Six") || commandToken.equals("Seven") 
					|| commandToken.equals("Eight") || commandToken.equals("Nine") || commandToken.equals("Ten") 
					|| commandToken.equals("Jack") || commandToken.equals("Queen") || commandToken.equals("King") 
					|| commandToken.equals("Ace")) {
				String nameToSend = commandToken;
				String suitToSend = tokens.nextToken();
				String valueToSend = tokens.nextToken();
				GUIClient.updateHand(nameToSend, suitToSend, Integer.parseInt(valueToSend));
			} else {
				System.out.println("HOST SERVER: WRONG INPUT");
			}
			/* End of the other while loop */
		}

		/* End of threading run() */
	}

	/* End of the thread class */
}
