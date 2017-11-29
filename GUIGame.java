
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

public class GUIGame {

	private JFrame frame;
	
	private String commandHistory = "";
	private String ipAddress = "";
	private String portNum = "";
	
	private Host host = new Host();
	private HostServer hostServer;
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
	private JTextField tfWinds;
	private JTextField tfLosses;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUIGame window = new GUIGame();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public GUIGame() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 451, 628);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JLabel lblYourePlaying = new JLabel("You are playing:");
		lblYourePlaying.setBounds(12, 34, 121, 15);
		frame.getContentPane().add(lblYourePlaying);
		
		JLabel lbBlackJackTitle = new JLabel("Black Jack");
		lbBlackJackTitle.setBounds(12, 12, 196, 15);
		frame.getContentPane().add(lbBlackJackTitle);
		
		textField = new JTextField();
		textField.setEditable(false);
		textField.setBounds(129, 32, 114, 19);
		frame.getContentPane().add(textField);
		textField.setColumns(10);
		
		JPanel panelGUIGameOpponents = new JPanel();
		panelGUIGameOpponents.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		panelGUIGameOpponents.setBounds(12, 73, 415, 197);
		frame.getContentPane().add(panelGUIGameOpponents);
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
		frame.getContentPane().add(panelGUIgameYours);
		
		JButton btHit = new JButton("Hit");
		btHit.setBounds(10, 204, 100, 20);
		panelGUIgameYours.add(btHit);
		
		JButton btStay = new JButton("Stay");
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
		frame.getContentPane().add(btDisconnect);
		
		JLabel lblLosses = new JLabel("Losses");
		lblLosses.setBounds(12, 532, 196, 15);
		frame.getContentPane().add(lblLosses);
		
		JLabel lblWins = new JLabel("Wins");
		lblWins.setBounds(12, 559, 55, 15);
		frame.getContentPane().add(lblWins);
		
		tfWinds = new JTextField();
		tfWinds.setEditable(false);
		tfWinds.setColumns(10);
		tfWinds.setBounds(94, 555, 114, 19);
		frame.getContentPane().add(tfWinds);
		
		tfLosses = new JTextField();
		tfLosses.setEditable(false);
		tfLosses.setColumns(10);
		tfLosses.setBounds(94, 530, 114, 19);
		frame.getContentPane().add(tfLosses);
	}
	
	/*********************************************************************
	* Connect is intended to set up a connection between host A and host B.
	**********************************************************************/
	private void connect(String ipAddress, String portNum){
		 /* Connect to server's welcome socket */
                try {
                    controlSocket = new Socket(ipAddress, 
                                         Integer.parseInt(portNum));
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
	
	private void disconnect(){
		 /* Disconnect from server's welcome socket */
		outToHost.println("quit");
		outToHost.flush();
		
		boolean controlSocketOpen = false;
                inFromHost.close();
                outToHost.close();
		isConnectedToOtherHost = false;
	}
}
