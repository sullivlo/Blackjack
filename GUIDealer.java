
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

public class GUIDealer {

	private JFrame frmDealer;

	private String commandHistory = "";
	private String ipAddress = "";
	private String portNum = "";

	public ArrayList<Card> deck = Card.setDeck();
	public Card[] hand = new Card[6];
	public int handSize = 2;
	public int handValue = 0;

	public int wins = 0;
	public int losses = 0;

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
					GUIDealer window = new GUIDealer();
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
	public GUIDealer() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {

		frmDealer = new JFrame();
		frmDealer.setTitle("Dealer");
		frmDealer.setBounds(100, 100, 451, 628);
		frmDealer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmDealer.getContentPane().setLayout(null);

		JLabel lblYourePlaying = new JLabel("You are playing:");
		lblYourePlaying.setBounds(12, 34, 121, 15);
		frmDealer.getContentPane().add(lblYourePlaying);

		JLabel lbBlackJackTitle = new JLabel("Black Jack");
		lbBlackJackTitle.setBounds(12, 12, 196, 15);
		frmDealer.getContentPane().add(lbBlackJackTitle);

		textField = new JTextField();
		textField.setEditable(false);
		textField.setBounds(129, 32, 114, 19);
		frmDealer.getContentPane().add(textField);
		textField.setColumns(10);

		JPanel panelGUIGameOpponents = new JPanel();
		panelGUIGameOpponents.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		panelGUIGameOpponents.setBounds(12, 73, 415, 197);
		frmDealer.getContentPane().add(panelGUIGameOpponents);
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
		frmDealer.getContentPane().add(panelGUIgameYours);

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

				String toSend = "retr" + dataPort;

				//outToHost.println(toSend);
				//outToHost.flush();

				Socket dataConnection = null;

				try {
					/* Connect to server and establish variables */
					dataListen = new ServerSocket(dataPort);

					dataConnection = dataListen.accept();

					InputStream inFromServer_Data = dataConnection.getInputStream();

					//while ((recvMsgSize = inFromServer_Data.read(byteBuffer)) != -1) {
						try {
							/* On listening port */
							Card card = new Card();
							
							card = deck.get(0);
							deck.remove(0);

							hand[handSize] = card;
							handSize++;
							tfLosses.setText("[" + card.name + ", " + card.suit);
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
					System.out.println("Error trying to " + "retrieve file.");
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

		JButton btDisconnect = new JButton("Disconnect");
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

		tfLosses = new JTextField();
		tfLosses.setEditable(false);
		tfLosses.setColumns(10);
		tfLosses.setBounds(94, 530, 114, 19);
		frmDealer.getContentPane().add(tfLosses);
	}

	

	private void disconnect() {
		/* Disconnect from server's welcome socket */
		outToHost.println("quit");
		outToHost.flush();

		boolean controlSocketOpen = false;
		inFromHost.close();
		outToHost.close();
		isConnectedToOtherHost = false;
	}
}
