
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.border.LineBorder;
import java.awt.Color;
import javax.swing.*;
import javax.swing.JTextField;
import javax.swing.UIManager;

import java.awt.TextArea;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.net.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.io.*;

public class GUIDealer {

	private JFrame frmDealer;

	private static Card card;
	public int numberOf11s = 0;
	public int oppNumberOf11s = 0;

	public ArrayList<Card> deck = Card.setDeck();
	public Card[] hand = new Card[6];
	public Card[] opponentHand = new Card[6];
	public int oppHandSize = 0;
	public int handSize = 0;
	public static int handValue = 0;
	public int oppHandValue = 0;

	public static int wins = 0;
	public static int losses = 0;

	public boolean turnEnded = false;
	private String DealerNameSTR = "";

	private static String dealerHandString = "";
	private static String playerHandString = "";
	private String toSend = "";

	public int dataPort = 1240;
	public String DealerPortNum;

	private ServerSocket welcomeSocket;
	
	private boolean isConnected = false;
	public boolean gameActive = false;
	public static boolean clientWin = false;
	public static boolean clientLoss = false;

	private boolean isConnectedToOtherClient = false;

	static /* This handles the control-line out stream */
	PrintWriter outToClient = null;

	/* This handles the control-line in stream */
	Scanner inFromClient = null;

	private static JTextField DealerName;
	private static JTextField tfWins;
	private static JTextField tfLosses;
	private static TextArea textAreaOpponentsCards;
	private static JButton btNewHand;
	private static TextArea textAreaYourCards;
	private static JButton btnReadyForPlayer;

	private ServerSocket ourWelcomeSocket;
	
	/**
	 * Launch the application.
	 */
	public void start(String name, String portNum, ServerSocket tmpBBBWelcomeSocket) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUIDealer window = new GUIDealer(name, portNum, tmpBBBWelcomeSocket);
					window.frmDealer.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public GUIDealer(String name, String portNum, ServerSocket ptrWelcomeSocket) {
		DealerNameSTR = name;
		DealerPortNum = portNum;
		ourWelcomeSocket = ptrWelcomeSocket;
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	public void initialize() {

		frmDealer = new JFrame();
		frmDealer.setTitle("Dealer");
		frmDealer.setBounds(100, 100, 451, 628);
		frmDealer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmDealer.getContentPane().setLayout(null);
		frmDealer.getContentPane().setBackground(UIManager.getColor("CheckBoxMenuItem.acceleratorForeground"));

		JLabel lblYourePlaying = new JLabel("You are playing:");
		lblYourePlaying.setBounds(12, 34, 121, 15);
		frmDealer.getContentPane().add(lblYourePlaying);

		JLabel lbBlackJackTitle = new JLabel("Black Jack");
		lbBlackJackTitle.setBounds(12, 12, 196, 15);
		frmDealer.getContentPane().add(lbBlackJackTitle);

		DealerName = new JTextField("" + DealerPortNum);
		DealerName.setEditable(false);
		DealerName.setBounds(129, 32, 114, 19);
		frmDealer.getContentPane().add(DealerName);
		DealerName.setColumns(10);

		JPanel panelGUIGameOpponents = new JPanel();
		panelGUIGameOpponents.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		panelGUIGameOpponents.setBounds(12, 73, 415, 197);
		frmDealer.getContentPane().add(panelGUIGameOpponents);
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
		frmDealer.getContentPane().add(panelGUIgameYours);

		textAreaYourCards = new TextArea();
		textAreaYourCards.setEditable(false);
		textAreaYourCards.setBounds(10, 33, 395, 157);
		panelGUIgameYours.add(textAreaYourCards);

		btNewHand = new JButton("New Hand");
		JButton btHit = new JButton("Hit");

		JButton btStay = new JButton("Stay");
		btStay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (isConnectedToOtherClient != false) {
					// Disables hit/stay buttons
					btStay.setEnabled(false);
					btHit.setEnabled(false);

					for (int i = 0; i < deck.size(); i++) {
						Card card = new Card();
						card = deck.get(0);
						deck.remove(0);
						
						outToClient.println("DECK " + card.name + " " + card.suit + " " + card.value);
						outToClient.flush();
					}
					
					outToClient.println("staydealer");
					outToClient.flush();
					
					outToClient.println("sendDeck");
					outToClient.flush();
				} else {
					System.out.println("Not connected to host.");
				}

			}
		});
		btStay.setBounds(122, 204, 100, 20);
		panelGUIgameYours.add(btStay);

		/**
		 * retrieve card from host dealer
		 */

		btHit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				/**
				 * Request new card from the host dealer
				 */
				
				/** The port number to send files across */
				dataPort = 1240;
				try {
					try {
						if (gameActive == true) {
							/* On listening port */
							Card card = new Card();

							card = deck.get(0);
							deck.remove(0);

							hand[handSize] = card;
							handSize++;

							dealerHandString = dealerHandString + "[" + card.name + " of " + card.suit + "]\n";

							if (handSize > 6) {
								// Update game state to win for player
								wins++;
								tfWins.setText("" + wins);
								/*
								 * Send message to the other guy that he lost and to increment his loss counter.
								 */
								toSend = "lossClient " + dataPort;

								outToClient.println(toSend);
								outToClient.flush();
								
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
									toSend = "winClient " + dataPort;

									outToClient.println(toSend);
									outToClient.flush();
									
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
								toSend = "lossClient " + dataPort;

								outToClient.println(toSend);
								outToClient.flush();
								gameActive = false;
								
								numberOf11s = 0;
							}

							// dealerHandString = dealerHandString + "\n\nNumber of cards: " + handSize +
							// "\nValue: " + handValue;
							textAreaYourCards.setText(dealerHandString);

							outToClient.println("DEALERSEND " + card.name + " " + card.suit);
							outToClient.flush();

							if (gameActive == false) {
								dealerHandString = "";
								btHit.setEnabled(false);
								btStay.setEnabled(false);
								btNewHand.setEnabled(true);
							}
						}
					} catch (Exception f) {
						System.out.println("Error trying to get card.");
					}
					// }

				} catch (Exception f) {
					System.out.println("Error trying to retrieve file.");
				}
				
				System.out.println(handValue);
			}
		});
		btHit.setBounds(10, 204, 100, 20);
		panelGUIgameYours.add(btHit);

		JLabel lbYourCards = new JLabel("Your Cards:");
		lbYourCards.setBounds(12, 12, 121, 15);
		panelGUIgameYours.add(lbYourCards);

		btNewHand.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btHit.setEnabled(true);
				btStay.setEnabled(true);

				boolean draw21 = false;
				numberOf11s = 0;

				outToClient.println("resetfromdealer");
				outToClient.flush();

				deck = Card.setDeck();
				hand = new Card[6];
				handSize = 0;
				handValue = 0;
				dealerHandString = "";

				opponentHand = new Card[6];
				oppHandSize = 0;
				oppHandValue = 0;
				
				for (int i = 0; i < 2; i++) {

					/* On listening port */
					card = new Card();
					card = deck.get(0);
					deck.remove(0);

					hand[handSize] = card;
					handSize++;
					dealerHandString = dealerHandString + "[" + card.name + " of " + card.suit + "]\n";

					/* Ace check */
					if (card.name.equals("Ace")) {
						if (handValue >= 11) {
							card.value = 1;
						} else {
							card.value = 11;
							numberOf11s++;
						}
					}

					handValue = handValue + card.value;

					if (handValue == 21) {
						// Update game state to win for player
						wins++;
						tfWins.setText("" + wins);
						/*
						 * Send message to the other guy that he lost and to increment his loss counter.
						 */
						toSend = "lossClient " + dataPort;

						try {
							TimeUnit.SECONDS.sleep(1);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
						outToClient.println(toSend);
						outToClient.flush();
						
						draw21 = true;
					}
					
					if (i == 0) {
						outToClient.println("DEALERSEND " + "????" + " " + "????");
						outToClient.flush();
					} else {
						outToClient.println("DEALERSEND " + card.name + " " + card.suit);
						outToClient.flush();
					}
					
					
				}
				playerHandString =  "";
				for (int j = 0; j < 2; j++) {

					/* On listening port */
					card = new Card();

					card = deck.get(0);
					deck.remove(0);

					opponentHand[oppHandSize] = card;
					oppHandSize++;

					if (j == 0) {
						playerHandString = playerHandString + "[" + "????" + " of " + "????" + "]\n";
						textAreaOpponentsCards.setText(playerHandString);
					}else {
						playerHandString = playerHandString + "[" + card.name + " of " + card.suit + "]\n";
						textAreaOpponentsCards.setText(playerHandString);
					}

					/* Ace check */
					if (card.name.equals("Ace")) {
						if (oppHandValue >= 11) {
							card.value = 1;
						} else {
							card.value = 11;
							oppNumberOf11s++;
						}
					}

					oppHandValue = oppHandValue + card.value;

					outToClient.println(card.name + " " + card.suit + " " + card.value);
					outToClient.flush();
				}
				if (oppHandValue == 21 && draw21 != true) {
					// Update game state to win for player
					losses++;
					tfLosses.setText("" + losses);
					/*
					 * Send message to the other guy that he lost and to increment his loss counter.
					 */
					toSend = "winClient " + dataPort;

					outToClient.println(toSend);
					outToClient.flush();
					
					gameActive = false;
					draw21 = true;
				}
				// dealerHandString = dealerHandString + "\nNumber of cards: " + handSize +
				// "\nValue: " + handValue;
				textAreaYourCards.setText(dealerHandString);
				if (draw21 == true) {
					dealerHandString = "";
					draw21 = false;
					numberOf11s = 0;
					btHit.setEnabled(false);
					btStay.setEnabled(false);
					btNewHand.setEnabled(true);
				} else {
					gameActive = true;
					btNewHand.setEnabled(false);
				}

			}
		});
		btNewHand.setBounds(284, 204, 121, 20);
		panelGUIgameYours.add(btNewHand);
		btNewHand.setEnabled(false);

		JButton btDisconnect = new JButton("Disconnect");
		btDisconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				/* For debugging */
				// System.out.println(" DEBUG: Inside quit function.");

				if (isConnectedToOtherClient != false) {
					disconnect();
				} else {
					System.out.println("Not connected to host.");
				}
			}
		});
		btDisconnect.setBounds(285, 540, 127, 34);
		frmDealer.getContentPane().add(btDisconnect);

		JLabel lblLosses = new JLabel("Losses");
		lblLosses.setBounds(12, 532, 196, 15);
		frmDealer.getContentPane().add(lblLosses);

		JLabel lblWins = new JLabel("Wins");
		lblWins.setBounds(12, 559, 55, 15);
		frmDealer.getContentPane().add(lblWins);

		tfWins = new JTextField();
		tfWins.setEditable(false);
		tfWins.setColumns(10);
		tfWins.setBounds(94, 555, 114, 19);
		frmDealer.getContentPane().add(tfWins);
		tfWins.setText("" + wins);

		tfLosses = new JTextField();
		tfLosses.setEditable(false);
		tfLosses.setColumns(10);
		tfLosses.setBounds(94, 530, 114, 19);
		frmDealer.getContentPane().add(tfLosses);
		tfLosses.setText("" + losses);

		btnReadyForPlayer = new JButton("Ready for player");
		btnReadyForPlayer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				/* Perform a loop to wait for new connections */

				try {

					do {
						/* Wait for client... */
						Socket connectionSocket = ourWelcomeSocket.accept();

						/* Display to terminal when new client connects */
						System.out.println("\nClient-As-FTP-Server: New Client Connected!");
						isConnected = true;

						/*
						 * Create a thread to handle communication with this client and pass the
						 * constructor for this thread a reference to the relevant socket and user IP.
						 */
						FTPClientHandler handler = new FTPClientHandler(connectionSocket);

						inFromClient = new Scanner(connectionSocket.getInputStream());
						outToClient = new PrintWriter(connectionSocket.getOutputStream());

						/**
						 * Game logic draw two cards for GUIClient
						 * 
						 * display second guiClients card on GuiDialers opponents textfield
						 * 
						 * Game logic draw two cards for GUIDealer display GUIdealers card in your cards
						 * textfeild
						 * 
						 * send GUIClients their two cards + faceup GuiDealers card
						 * 
						 * 
						 */

						/* Start a new thread for this client */
						handler.start();

					} while (isConnected == false);
					isConnectedToOtherClient = true;

					outToClient.println("usernamedealer " + DealerNameSTR);
					outToClient.flush();
					
					for (int i = 0; i < 2; i++) {

						/* On listening port */
						card = new Card();
						card = deck.get(0);
						deck.remove(0);

						hand[handSize] = card;
						handSize++;
						dealerHandString = dealerHandString + "[" + card.name + " of " + card.suit + "]\n";

						/* Ace check */
						if (card.name.equals("Ace")) {
							if (handValue >= 11) {
								card.value = 1;
							} else {
								card.value = 11;
								numberOf11s++;
							}
						}

						handValue = handValue + card.value;

						if (i == 0) {
							outToClient.println("DEALERSEND " + "????" + " " + "????");
							outToClient.flush();
						} else {
							outToClient.println("DEALERSEND " + card.name + " " + card.suit);
							outToClient.flush();
						}
					}
					// dealerHandString = dealerHandString + "\nNumber of cards: " + handSize +
					// "\nValue: " + handValue;
					textAreaYourCards.setText(dealerHandString);
					gameActive = true;

					if (handValue == 21) {
						// Update game state to win for player
						wins++;
						tfWins.setText("" + wins);
						/*
						 * Send message to the other guy that he lost and to increment his loss counter.
						 */
						toSend = "lossClient " + dataPort;

						outToClient.println(toSend);
						outToClient.flush();
						gameActive = false;
						
						dealerHandString = "";
					}

					for (int i = 0; i < 2; i++) {

						/* On listening port */
						card = new Card();

						card = deck.get(0);
						deck.remove(0);

						opponentHand[oppHandSize] = card;
						oppHandSize++;

						if (i == 0) {
							playerHandString = playerHandString + "[" + "????" + " of " + "????" + "]\n";
							textAreaOpponentsCards.setText(playerHandString);
						}else {
							playerHandString = playerHandString + "[" + card.name + " of " + card.suit + "]\n";
							textAreaOpponentsCards.setText(playerHandString);
						}

						/* Ace check */
						if (card.name.equals("Ace")) {
							if (oppHandValue >= 11) {
								card.value = 1;
							} else {
								card.value = 11;
								oppNumberOf11s++;
							}
						}

						oppHandValue = oppHandValue + card.value;

						outToClient.println(card.name + " " + card.suit + " " + card.value);
						outToClient.flush();
					}
					if (oppHandValue == 21 && gameActive == true) {
						// Update game state to win for player
						losses++;
						tfLosses.setText("" + losses);
						/*
						 * Send message to the other guy that he lost and to increment his loss counter.
						 */
						toSend = "winClient " + dataPort;

						outToClient.println(toSend);
						outToClient.flush();
						
						gameActive = false;
					}

					if (gameActive == false) {
						dealerHandString = "";
						btHit.setEnabled(false);
						btStay.setEnabled(false);
						btNewHand.setEnabled(true);
					}
				} catch (Exception f) {
					isConnected = false;
					System.out.println("ERROR: Failure in setting up a " + "new thread.");
				}
			}
		});
		btnReadyForPlayer.setBounds(255, 24, 165, 34);
		frmDealer.getContentPane().add(btnReadyForPlayer);
	}

	private void disconnect() {

		/* Disconnect from server's welcome socket */
		outToClient.println("quit");
		outToClient.flush();

		inFromClient.close();
		outToClient.close();
		isConnectedToOtherClient = false;

		frmDealer.dispose();
	}
	
	public static void incrementWins() {
		wins++;
		tfWins.setText("" + wins);
	}
	
	public void clickReady() {
		btnReadyForPlayer.doClick();
		btnReadyForPlayer.setVisible(false);
	}
	
	public static void incrementLosses() {
		losses++;
		tfLosses.setText("" + losses);
	}
	
	public static void updatePlayer(String name, String suit) {
		playerHandString = playerHandString + "[" + name + " of " + suit + "]\n";
		textAreaOpponentsCards.setText(playerHandString);
	}
	
	public static void resetPlayer() {
		btNewHand.setEnabled(true);
		btNewHand.doClick();
	}
	
	public static void enableNewGame(String result) {
		if (result.equals("win")) {
			clientWin = true;
			String toSend = "winclientlossdealer";

			outToClient.println(toSend);
			outToClient.flush();
		}else if (result.equals("loss")) {
			clientLoss = true;
			String toSend = "windealerlossclient";

			outToClient.println(toSend);
			outToClient.flush();
		}
		btNewHand.setEnabled(true);
	}
	
	public static int getValue() {
		return handValue;
	}
	
	public static void whoAmIFacing(String name) {
		DealerName.setText(name);
	}
}
