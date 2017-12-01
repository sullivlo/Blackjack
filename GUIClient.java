
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.border.LineBorder;
import java.awt.Color;
import javax.swing.JButton;
import java.awt.Font;
import javax.swing.JTextField;
import javax.swing.JList;
import javax.swing.AbstractListModel;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import java.awt.Scrollbar;
import java.awt.TextArea;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.net.*;
import java.util.*;
import java.io.*;

/* For tokens */
import java.util.*;
import javax.swing.JTextArea;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.UIManager;

public class GUIClient {

	private JFrame frmPlaya;

	private String commandHistory = "";
	private String ipAddress = "127.0.0.1";
	private String portNum = "1235";
	private Card card;
	public int numberOf11s = 0;

	public ArrayList<Card> deck = Card.setDeck();
	public Card[] hand = new Card[6];
	public int handSize = 0;
	public int handValue = 0;

	public int wins = 0;
	public int losses = 0;
	
	public boolean turnEnded = false;

	private String dealerHandString = "";
	private String toSend = "";

	public int dataPort = 1240;

	private boolean isConnected = false;
	public boolean gameActive = false;

	private Host host = new Host();
	private DealerServer hostServer;
	private Socket controlSocket;
	private boolean isConnectedToOtherHost = false;

	/* This handles the control-line out stream */
	PrintWriter outToHost = null;

	/* This handles the control-line in stream */
	Scanner inFromHost = null;

	/* This is used as a helper in initial connection to Central-Server */
	private String hostFTPWelcomeport;
	/* Holds the condition of whether Host-as-Server is setup */
	private boolean alreadySetupFTPServer = false;
	/* Holds the condition of whether connected to the Central-Server */
	private boolean isConnectedToCentralServer = false;
	private JTextField textField;
	private JTextField tfWins;
	private JTextField tfLosses;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUIClient window = new GUIClient();
					window.frmPlaya.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public GUIClient() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {

		frmPlaya = new JFrame();
		frmPlaya.setTitle("Playa");
		frmPlaya.setBounds(100, 100, 451, 628);
		frmPlaya.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmPlaya.getContentPane().setLayout(null);

		JLabel lblYourePlaying = new JLabel("You are playing:");
		lblYourePlaying.setBounds(12, 34, 121, 15);
		frmPlaya.getContentPane().add(lblYourePlaying);
		frmPlaya.getContentPane().setBackground(UIManager.getColor("CheckBoxMenuItem.acceleratorForeground"));

		JLabel lbBlackJackTitle = new JLabel("Black Jack");
		lbBlackJackTitle.setBounds(12, 12, 196, 15);
		frmPlaya.getContentPane().add(lbBlackJackTitle);

		textField = new JTextField();
		textField.setEditable(false);
		textField.setBounds(129, 32, 114, 19);
		frmPlaya.getContentPane().add(textField);
		textField.setColumns(10);

		JPanel panelGUIGameOpponents = new JPanel();
		panelGUIGameOpponents.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		panelGUIGameOpponents.setBounds(12, 73, 415, 197);
		frmPlaya.getContentPane().add(panelGUIGameOpponents);
		panelGUIGameOpponents.setLayout(null);

		TextArea textAreaOpponentsCards = new TextArea();
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
		JButton btHit = new JButton("Hit");
		btHit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				/**
				 * Request new card from the host dealer
				 */

				byte[] byteBuffer = new byte[32768];
				/**
				 * establish data connection from hostA to hostB
				 */
				ServerSocket dataListen;
				String ipAddress = "";
				/** The port number to send files across */
				int dataPort = 1240;
				int recvMsgSize;

				String toSend = "retr " + dataPort;

				outToHost.println(toSend);
				outToHost.flush();

				Socket dataConnection = null;

				try {
					/* Connect to server and establish variables */
					//dataListen = new ServerSocket(dataPort);

					//dataConnection = dataListen.accept();

					//InputStream inFromServer_Data = dataConnection.getInputStream();

					//while ((recvMsgSize = inFromServer_Data.read(byteBuffer)) != -1) {
						try {
							/* On listening port */
							Card card = new Card();
							String requestedCard = "";

							Scanner scanner = new Scanner(requestedCard);
							String newCard = scanner.nextLine();

							StringTokenizer tokens = new StringTokenizer(newCard);
							card.name = tokens.nextToken();
							card.suit = tokens.nextToken();
							card.value = Integer.parseInt(tokens.nextToken());

							hand[handSize] = card;
							handSize++;
							if (handSize > 6) {
								// Update game state to win for player
								wins++;
								tfWins.setText("" + wins);
								/*
								 * Send message to the other guy that he lost and to increment his loss counter.
								 */
								toSend = "loss" + dataPort;

								outToHost.println(toSend);
								outToHost.flush();
							}

							/* Ace check */
							if (card.suit == "Ace") {
								if (handValue >= 11) {
									card.value = 1;
								} else {
									card.value = 11;
								}
							}

							handValue = handValue + card.value;
							if (handValue > 21) {
								// Update gameState to lose for player
								losses++;
								tfLosses.setText("" + losses);
								/*
								 * Send message to the other guy that he won and to increment his win counter.
								 */
								toSend = "win" + dataPort;

								outToHost.println(toSend);
								outToHost.flush();
							}
						} catch (Exception f) {
							System.out.println("Error trying to get card.");
						}
					//}

				} catch (Exception f) {
					System.out.println("Error trying to " + "retrieve card.");
				}
			}
		});
		btHit.setBounds(10, 204, 100, 20);
		panelGUIgameYours.add(btHit);

		JButton btStay = new JButton("Stay");
		btStay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Disables hit/stay buttons
				btStay.setEnabled(false);
				btHit.setEnabled(false);

				// Sets turnEnded to 1

				// Sends message to host for them to take their turn

			}
		});
		btStay.setBounds(122, 204, 100, 20);
		panelGUIgameYours.add(btStay);

		TextArea textAreaYourCards = new TextArea();
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
		
		JButton btConnect = new JButton("Connect");
		btConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				/**
				 * Connect from playa to dealer
				 */

						
				/* Connect to other user's HostServer */
				try {
					connect(ipAddress, portNum);
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
					
				}
				catch (Exception q) {
					System.out.println("ERROR: Failed to " 
					+ "connect to server!");
				}
				
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
	private void connect(String ipAddress, String portNum){
	
		 /* Connect to server's welcome socket */
                try { 
                    controlSocket = new Socket("127.0.0.1",   //using hardcoded ipAdress 
                                         Integer.parseInt("1235"));  //using hardcoded portNum
                    boolean controlSocketOpen = true;
                }catch(Exception p){
                    System.out.println("ERROR: Did not find socket!");
                }
                                    
                // Set-up the control-stream,
                // if there's an error, report the non-connection.
                try {
                    inFromHost = 
                       new Scanner(controlSocket.getInputStream());
                    outToHost = 
                       new PrintWriter(controlSocket.getOutputStream());
                    isConnectedToOtherHost = true;
                    System.out.println("Connected to client!");
                    System.out.println(" ");
                }
                catch (Exception e) {
                    System.out.println("ERROR: Did not connect to " +
                        "client!");
                    isConnectedToOtherHost = false;
		}
	}

	
}
