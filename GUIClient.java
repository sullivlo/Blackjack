
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.border.LineBorder;
import java.awt.Color;
import javax.swing.JButton;
import javax.swing.JTextField;
import java.awt.TextArea;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.net.*;
import java.util.*;
import java.io.*;
import javax.swing.UIManager;

public class GUIClient {

	private static JFrame frmPlaya;

	private String ipAddress = "127.0.0.1";
	public static int numberOf11s = 0;

	public static ArrayList<Card> deck = null;
	public static Card[] hand = new Card[6];
	public static int handSize = 0;
	public static int handValue = 0;

	public static int wins = 0;
	public static int losses = 0;
	
	public static int staticWins = 0;
	public static int staticLosses = 0;

	public static TextArea textAreaYourCards;
	public static JButton btStay;
	public static JButton btHit;
	public static TextArea textAreaOpponentsCards;

	public static boolean dealerFinish = false;

	public boolean turnEnded = false;

	private static String opponentHandString = "";
	private static String dealerHandString = "";
	private String toSend = "";
	private String ClientNameSTR = "";

	public String ClientPortNum = "";

	public static boolean gameActive = false;
	
	private Socket controlSocket;
	private boolean isConnectedToOtherHost = false;

	/* This handles the control-line out stream */
	PrintWriter outToHost = null;

	/* This handles the control-line in stream */
	Scanner inFromHost = null;

	public static JTextField ClientName;
	public static JTextField tfWins;
	public static JTextField tfLosses;
	private static JLabel lblYourePlaying;
	private static JButton btConnect;

	/**
	 * Launch the application.
	 */
	public void start(String ClientName, String clientPortnNum) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUIClient window = new GUIClient(ClientName,clientPortnNum);
					GUIClient.frmPlaya.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public GUIClient(String ClientName, String clientPortNum) {
		ClientNameSTR = ClientName;
		ClientPortNum = clientPortNum;
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	public void initialize() {

		frmPlaya = new JFrame();
		frmPlaya.setTitle("Player");
		frmPlaya.setBounds(100, 100, 451, 628);
		frmPlaya.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmPlaya.getContentPane().setLayout(null);

		lblYourePlaying = new JLabel("You are playing:");
		lblYourePlaying.setBounds(12, 34, 121, 15);
		frmPlaya.getContentPane().add(lblYourePlaying);
		frmPlaya.getContentPane().setBackground(UIManager.getColor("CheckBoxMenuItem.acceleratorForeground"));

		JLabel lbBlackJackTitle = new JLabel("Black Jack");
		lbBlackJackTitle.setBounds(12, 12, 196, 15);
		frmPlaya.getContentPane().add(lbBlackJackTitle);

		ClientName = new JTextField(ClientPortNum);
		ClientName.setEditable(false);
		ClientName.setBounds(129, 32, 114, 19);
		frmPlaya.getContentPane().add(ClientName);
		ClientName.setColumns(10);

		tfWins = new JTextField();
		tfWins.setEditable(false);
		tfWins.setColumns(10);
		tfWins.setBounds(94, 555, 114, 19);
		frmPlaya.getContentPane().add(tfWins);
		tfWins.setText("" + wins);

		tfLosses = new JTextField();
		tfLosses.setEditable(false);
		tfLosses.setColumns(10);
		tfLosses.setBounds(94, 530, 114, 19);
		frmPlaya.getContentPane().add(tfLosses);
		tfLosses.setText("" + losses);
		
		JButton btNewHand = new JButton("New Hand");
		
		JPanel panelGUIGameOpponents = new JPanel();
		panelGUIGameOpponents.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		panelGUIGameOpponents.setBounds(12, 73, 415, 197);
		frmPlaya.getContentPane().add(panelGUIGameOpponents);
		panelGUIGameOpponents.setLayout(null);

		textAreaOpponentsCards = new TextArea();
		textAreaOpponentsCards.setEditable(false);
		textAreaOpponentsCards.setBounds(10, 33, 395, 157);
		panelGUIGameOpponents.add(textAreaOpponentsCards);

		JLabel lblOpponentsCards = new JLabel("Opponents Cards");
		lblOpponentsCards.setBounds(12, 12, 237, 15);
		panelGUIGameOpponents.add(lblOpponentsCards);

		JPanel panelGUIgameYours = new JPanel();
		panelGUIgameYours.setLayout(null);
		panelGUIgameYours.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		panelGUIgameYours.setBounds(12, 291, 415, 229);
		frmPlaya.getContentPane().add(panelGUIgameYours);

		/**
		 * retrieve card from host dealer
		 */
		btHit = new JButton("Hit");
		btHit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				/**
				 * Request new card from the host dealer
				 */
				/** The port number to send files across */
				try {
					try {
						/* On listening port */
						Card card = new Card();
						card = deck.get(0);
						deck.remove(0);
						hand[handSize] = card;
						handSize++;

						dealerHandString = dealerHandString + "[" + card.name + " of " + card.suit + "]\n";
						System.out.println(dealerHandString);

						if (handSize > 6) {
							// Update game state to win for player
							wins++;
							tfWins.setText("" + wins);
							/*
							 * Send message to the other guy that he lost and to increment his loss counter.
							 */
							toSend = "lossdealer " + ClientPortNum;

							outToHost.println(toSend);
							outToHost.flush();
							gameActive = false;
							numberOf11s = 0;
						}

						/* Ace check */
						if (card.name.equals("Ace")) {
							if (handValue >= 11) {
								card.value = 1;
							} else {
								card.value = 11;
								numberOf11s = numberOf11s + 1;
							}
						}
						
						handValue = handValue + card.value;
						if (handValue > 21) {
							if (numberOf11s != 0) {
								handValue = handValue - 10;
								numberOf11s--;
							} else {
								// Update gameState to lose for player
								losses++;
								tfLosses.setText("" + losses);
								/*
								 * Send message to the other guy that he won and to increment his win counter.
								 */
								toSend = "windealer " + ClientPortNum;
								
								outToHost.println(toSend);
								outToHost.flush();
								gameActive = false;
								numberOf11s = 0;
							}
						} else if (handValue == 21) {
							// Update game state to win for player
							wins++;
							tfWins.setText("" + wins);
							/*
							 * Send message to the other guy that he lost and to increment his loss counter.
							 */
							toSend = "lossdealer " + ClientPortNum;

							outToHost.println(toSend);
							outToHost.flush();
							gameActive = false;
							numberOf11s = 0;
						}
						
						textAreaYourCards.setText(dealerHandString);

						outToHost.println("DEALERRECEIVE " + card.name + " " + card.suit);
						outToHost.flush();

						if (gameActive == false) {
							dealerHandString = "";
							btHit.setEnabled(false);
							btStay.setEnabled(false);
							btNewHand.setEnabled(true);
						}
					} catch (Exception f) {
						System.out.println("Error trying to get card.");
					}
					// }

				} catch (Exception f) {
					System.out.println("Error trying to retrieve card.");
				}
				
				System.out.println(handValue);
			}
		});
		btHit.setBounds(10, 204, 100, 20);
		panelGUIgameYours.add(btHit);

		btStay = new JButton("Stay");
		btStay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Disables hit/stay buttons
				btStay.setEnabled(false);
				btHit.setEnabled(false);
				btNewHand.setEnabled(false);

				outToHost.println("stayclient " + handValue + " " + wins + " " + losses);
				outToHost.flush();
			}
		});
		btStay.setBounds(122, 204, 100, 20);
		panelGUIgameYours.add(btStay);

		btStay.setEnabled(false);
		btHit.setEnabled(false);
		
		btNewHand.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				outToHost.println("resetfromclient");
				outToHost.flush();
				
				dealerFinish = false;

				btStay.setEnabled(false);
				btHit.setEnabled(false);
				btNewHand.setEnabled(false);
				gameActive = false;
			}
		});
		btNewHand.setBounds(284, 204, 121, 20);
		panelGUIgameYours.add(btNewHand);
		btNewHand.setEnabled(false);

		textAreaYourCards = new TextArea();
		textAreaYourCards.setEditable(false);
		textAreaYourCards.setBounds(10, 33, 395, 157);
		panelGUIgameYours.add(textAreaYourCards);

		JLabel lbYourCards = new JLabel("Your Cards:");
		lbYourCards.setBounds(12, 12, 121, 15);
		panelGUIgameYours.add(lbYourCards);

		JLabel lblLosses = new JLabel("Losses");
		lblLosses.setBounds(12, 532, 64, 15);
		frmPlaya.getContentPane().add(lblLosses);

		JLabel lblWins = new JLabel("Wins");
		lblWins.setBounds(12, 559, 55, 15);
		frmPlaya.getContentPane().add(lblWins);

		btConnect = new JButton("Connect");
		btConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				/**
				 * Connect from playa to dealer
				 */

				/* Connect to other user's HostServer */
				try {
					
					connect(ipAddress, ClientPortNum);
					btConnect.setEnabled(false);

					do {
						/*
						 * Create a thread to handle communication with this client and pass the
						 * constructor for this thread a reference to the relevant socket and user IP.
						 */
						FTPClientHandler handler = new FTPClientHandler(controlSocket);

						/* Start a new thread for this client */
						handler.start();
					} while (isConnectedToOtherHost == false);

				} catch (Exception q) {
					System.out.println("ERROR: Failed to connect to server!");
				}
				
				outToHost.println("usernameclient " + ClientNameSTR);
				outToHost.flush();
			}
		});
		btConnect.setBounds(269, 29, 127, 34);
		frmPlaya.getContentPane().add(btConnect);

		JButton btDisconnect = new JButton("Disconnect");
		btDisconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				/* For debugging */
				// System.out.println(" DEBUG: Inside quit function.");

				if (isConnectedToOtherHost != false) {
					disconnect();
					btConnect.setEnabled(true);
				} else {
					System.out.println("Not connected to host.");
					btConnect.setEnabled(true);
				}
			}
		});
		btDisconnect.setBounds(285, 540, 127, 34);
		frmPlaya.getContentPane().add(btDisconnect);
	}

	private void disconnect() {

		/* Disconnect from server's welcome socket */
		outToHost.println("quit");
		outToHost.flush();

		inFromHost.close();
		outToHost.close();
		isConnectedToOtherHost = false;

		frmPlaya.dispose();
	}

	/*********************************************************************
	 * Connect is intended to set up a connection between host A and host B.
	 **********************************************************************/
	private void connect(String ipAddress, String portNum) {

		/* Connect to server's welcome socket */
		try {
			controlSocket = new Socket(ipAddress, Integer.parseInt(ClientPortNum));
		} catch (Exception p) {
			System.out.println("ERROR: Did not find socket!");
		}

		// Set-up the control-stream,
		// if there's an error, report the non-connection.
		try {
			inFromHost = new Scanner(controlSocket.getInputStream());
			outToHost = new PrintWriter(controlSocket.getOutputStream());
			isConnectedToOtherHost = true;
			System.out.println("Connected to client!");
			System.out.println(" ");
		} catch (Exception e) {
			System.out.println("ERROR: Did not connect to " + "client!");
			isConnectedToOtherHost = false;
		}

	}

	public static void stayChange() {
		dealerFinish = true;

		btStay.setEnabled(true);
		btHit.setEnabled(true);
		gameActive = true;
		System.out.println(handValue + "");
	}

	public static void updateHand(String name, String suit, int value) {
		Card card = new Card();
		card.name = name;
		card.suit = suit;
		card.value = value;
		hand[handSize] = card;
		handSize++;

		dealerHandString = dealerHandString + "[" + card.name + " of " + card.suit + "]\n";
		
		/* Ace check */
		if (card.name.equals("Ace")) {
			if (handValue >= 11) {
				card.value = 1;
			} else {
				card.value = 11;
				numberOf11s = numberOf11s + 1;
			}
		}
		
		handValue = handValue + card.value;
		
		System.out.println("\n" + dealerHandString);
		textAreaYourCards.setText(dealerHandString);
	}

	public static void updateDealer(String name, String suit) {
		opponentHandString = opponentHandString + "[" + name + " of " + suit + "]\n";
		textAreaOpponentsCards.setText(opponentHandString);
	}
	
	public static void reset() {
		opponentHandString = "";
		textAreaOpponentsCards.setText("");
		
		deck = null;
		hand = new Card[6];
		handSize = 0;
		handValue = 0;
		numberOf11s = 0;
		dealerHandString = "";
		textAreaYourCards.setText(dealerHandString);
	}
	
	public static void incrementWins() {
		wins = wins + 1;
		System.out.println(wins + "");
		tfWins.setText("" + wins);
	}
	
	public static void incrementLosses() {
		losses = losses + 1;
		System.out.println(losses + "");
		tfLosses.setText("" + losses);
	}
	
	public static void receiveDeck(ArrayList<Card> gotDeck) {
		deck = gotDeck;
	}
	
	public static int getValue() {
		return handValue;
	}
	
	public static void incrementWins(String w) {
		int winInt = Integer.parseInt(w);
		wins = winInt  + 1;
		
		System.out.println("wins: " +  wins);
	}
	
	public void clickConnect() {
		btConnect.doClick();
		btConnect.setVisible(false);
	}
	
	public static void incrementLosses(String l) {
		int lossInt = Integer.parseInt(l);
		losses = lossInt  + 1;
		
		System.out.println("Losses: " +  losses);
	}
	
	public static void whoAmIFacing(String name) {
		ClientName.setText(name);
	}
}
