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

public class GUI {

	private JFrame frame;
	private JTextField textFieldServerHostname;
	private JTextField textFieldUsername;
	private JTextField textFieldHostname;
	private JTextField textFieldPort;
	private JTextField ftpCommandField;
	private JComboBox comboBoxSpeed;
	
	private String commandHistory = "";
	private String ipAddress = "";
	private String portNum = "";
	
	private Host host = new Host();
	private PlayerServer hostServer;
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
	private JTable table;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI window = new GUI();
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
	public GUI() {
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
		
		JPanel panelConnection = new JPanel();
		panelConnection.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		panelConnection.setBounds(12, 28, 415, 120);
		frame.getContentPane().add(panelConnection);
		panelConnection.setLayout(null);
		
		JLabel lblServerHostname = new JLabel("Server Hostname:");
		lblServerHostname.setFont(new Font("Dialog", Font.PLAIN, 12));
		lblServerHostname.setBounds(12, 12, 133, 15);
		panelConnection.add(lblServerHostname);
		
		JLabel lblUsername = new JLabel("Username:");
		lblUsername.setFont(new Font("Dialog", Font.PLAIN, 12));
		lblUsername.setBounds(12, 39, 119, 15);
		panelConnection.add(lblUsername);
		
		JLabel lblPort = new JLabel("Port:");
		lblPort.setFont(new Font("Dialog", Font.PLAIN, 12));
		lblPort.setBounds(268, 12, 39, 15);
		panelConnection.add(lblPort);
		
		JLabel lblNewLabel = new JLabel("Hostname:");
		lblNewLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
		lblNewLabel.setBounds(12, 66, 68, 15);
		panelConnection.add(lblNewLabel);
		
		JLabel lblSpeed = new JLabel("Speed:");
		lblSpeed.setFont(new Font("Dialog", Font.PLAIN, 12));
		lblSpeed.setBounds(254, 39, 49, 15);
		panelConnection.add(lblSpeed);
		
		JButton btnConnect = new JButton("Connect");
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			
				String serverHostname = textFieldServerHostname.getText();
				
				String username = textFieldUsername.getText(); 
				username = username.replaceAll(" ", "@@");
				
				String port = textFieldPort.getText();
				
				String hostname = textFieldHostname.getText();
				hostname = hostname.replaceAll(" ", "@@");
				
				String speed = comboBoxSpeed.getSelectedItem().toString();
				
				if (alreadySetupFTPServer == true) {
				    /* For debugging */
				    System.out.println("  DEBUG-06: FTP-Server is already running.");
				    
				}
				
				/* TODO - For displaying to the user */
				// IF NO USERNAME
				// THEN DISPLAY "Please enter a USERNAME"
				
				/* Makes the host a FTP server */
				if (!username.equals("") && alreadySetupFTPServer == false) {
		            try {
		                /* 
		                 This starts a new thread for FTP-as-Server-Handling 
			             This allows for the GUI to act separately from the
			             actions of the FTP part of the client 
			            */
			            hostServer = new PlayerServer();
			            hostFTPWelcomeport = hostServer.getFTPWelcomePort();
			            
			            /* For debugging */
			            // System.out.println("  DEBUG-05: WelcomePort: " +
			            //    hostFTPWelcomeport);
			            
			            /* Initiate the thread */
				    hostServer.start();
				    
				    /* 
				    Next "Connect" click will now NOT try this
				    again. This important so that a multitude of 
				    actions aren't being opened on the same port.
				    */
				    alreadySetupFTPServer = true;
				    
				    /* For debugging */
		                // System.out.println("DEBUG-01: Initializing Host as FTP Server!");
		                
		            } catch(Exception f) {
			            System.out.println("  ERROR-03: Failure to setup" +
			             " client as a FTP-Server.");
		            }
		        }
		        else {
		            System.out.println("  DEBUG-03: Did not attempt FTP-Server setup.");
		        }
				
				/* Initialize connection to the Central-Server! */
				if (!username.equals("") && isConnectedToCentralServer == false) {
				    /* 
				     This internally handles the user clicking "connect"
				     multiple times.
				    */
				    try {
				        /* NOV17 - This may need to behave differently to validate usernames */
				        int report = host.connectToServer(serverHostname, port, username, hostname, speed, hostFTPWelcomeport);
				        
				        
				        if (report == -2 || report == -3 || report == -4) {
				            /* Taken Username and other errors */       
				            isConnectedToCentralServer = false;
				        }
				        else if(report == 1) {
				            isConnectedToCentralServer = true;
				        }
				        else {				            
				            /* For debugging */
				            System.out.println("  ERROR: General failure in connecting to Central-Server.");
				            
				            isConnectedToCentralServer = false;
				        }
				    }
				    catch (Exception g) {
				        System.out.println("  ERROR-03: Issue connecting to Central-Server!");
				        isConnectedToCentralServer = false;
				    }
				}
				else {
				    /* For debugging */
				    System.out.println("  DEBUG-07: Did not attempt CS-Connection.");
				}
				
				
		        
			}
		});
		btnConnect.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 12));
		btnConnect.setBounds(254, 66, 149, 19);
		panelConnection.add(btnConnect);
		
		textFieldServerHostname = new JTextField();
		textFieldServerHostname.setText("127.0.0.1");
		textFieldServerHostname.setBounds(130, 10, 114, 19);
		panelConnection.add(textFieldServerHostname);
		textFieldServerHostname.setColumns(10);
		
		textFieldUsername = new JTextField();
		textFieldUsername.setText("");
		textFieldUsername.setBounds(84, 39, 152, 19);
		panelConnection.add(textFieldUsername);
		textFieldUsername.setColumns(10);
		
		textFieldHostname = new JTextField();
		textFieldHostname.setText("DCCLIENT");
		textFieldHostname.setBounds(84, 66, 158, 19);
		panelConnection.add(textFieldHostname);
		textFieldHostname.setColumns(10);
		
		textFieldPort = new JTextField();
		textFieldPort.setText("1234");
		textFieldPort.setBounds(301, 10, 102, 19);
		panelConnection.add(textFieldPort);
		textFieldPort.setColumns(10);
		
		comboBoxSpeed = new JComboBox();
		comboBoxSpeed.setModel(new DefaultComboBoxModel(new String[] {"Ethernet", "Broadband", "T1", "T3"}));
		comboBoxSpeed.setFont(new Font("Dialog", Font.PLAIN, 12));
		comboBoxSpeed.setBounds(301, 37, 99, 19);
		panelConnection.add(comboBoxSpeed);
		
		JButton btnDisconnect = new JButton("Disconnect");
		btnDisconnect.setBounds(254, 91, 149, 19);
		panelConnection.add(btnDisconnect);
		btnDisconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			    /*
			     Actions to happen on click of "Disconnect"
			    */
			    try {
			        host.disconnectFromServer();
			        
			        /* Reset the boolean for connectedness */
			        isConnectedToCentralServer = false;
			    }
			    catch (Exception h) {
			        System.out.println("  ERROR: Failure in disconnect()...");
			
			    }
			}
		});
		
		JPanel panelSearch = new JPanel();
		panelSearch.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		panelSearch.setBounds(12, 212, 415, 229);
		frame.getContentPane().add(panelSearch);
		panelSearch.setLayout(null);
		
		JButton btnReady = new JButton("Ready to play");
		btnReady.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			/*
			 Actions to happen on click of "Search"
			*/
			    String keySearch = btnReady.getText();
			
			    if(!keySearch.equals("")) {
			    
			        if (alreadySetupFTPServer == true && isConnectedToCentralServer == true) {
			            /* This may hold values to paste to GUI boxes */
			            String results;
			        
			            /* 
			             TODO - have this function return the values so that
			             we can use them to generate GUI boxes. For now, let's
			             debug issues and get halfway there. 
			            */
			            
			            /* Query the server for the User's string of keywords */
			            String returnedFileData = host.queryKeywords(keySearch);
			            
				    //String messageToPrint = returnedFileData.replaceAll("*", "\n");

				   
				    
				    String stringForTextArea = "";
				    
				    /* This contains all the FILES as tokens to be parsed */
				    StringTokenizer tokens = new StringTokenizer(returnedFileData);	
		
				    /* 
				    If there was no files == "NOFOUNDMATCHES"
				    If there was files == "FILE"
				    If there was error == "ERROR"
				    */
				    String firstToken = "";
				    try {
					firstToken = tokens.nextToken();
				    }
				    catch (Exception g) {
					System.out.println("  DEBUG: First token threw error!");
				    }
				    
				    String currentToken;
				    if (firstToken.equals("ERROR")) {
					System.out.println("  DEBUG-10: Error in parsing the returned string!");              
					/* Print in GUI "There was an error, try again..." */
					
				    }
				    else if (firstToken.equals("NOFOUNDMATCHES")) {
					System.out.println("  DEBUG-11: No found matches!");
				    
					/* Print in GUI "no matches" */
					
				    }
				    else if (firstToken.equals("FILE")) {
					    /* 
					    For each filedescription, for each row, on the
					    GUI. On each iteration in the while(), these 
					    values change.
					    */
					    String tempUserPort = "";
					    String tempFileName = "";
					    String tempHostName = "";
					    String tempUserName = "";
					    String tempUserIP = "";
					    String tempSpeed = "";
					    String notUsed = "";
					
					    /* Could be a TRY CATCH problem */
					    try {
					        while (tokens.hasMoreTokens()) {
						    // "Alice"
						    tempUserName = tokens.nextToken();
					
						    // "127.1.1.2"
						    tempUserIP = tokens.nextToken();
				        
						    // "1235"
						    tempUserPort = tokens.nextToken();
					        
						    // "Apples.jpg"
						    tempFileName = tokens.nextToken();
						
						    // "Ethernet"
						    tempSpeed = tokens.nextToken();
						
						    stringForTextArea = stringForTextArea + "USERNAME: " 
						        + tempUserName + "\nIP ADDRESS: " + tempUserIP 
						        + "\nPORT NUMBER: " + tempUserPort 
						        + "\nFILE NAME: " + tempFileName 
						        + "\nSPEED: " + tempSpeed + "\n\n";
						
						    // FILE
						    //notUsed = tokens.nextToken();
					        }
					    }
					    catch (Exception h) {
					        System.out.println("  DEBUG: while() threw an error!");
					    }
					
					    stringForTextArea = stringForTextArea + "End of search list.";
				        }
			        }
			        else {
			            /* For debugging */
			            System.out.println("  DEBUG-04: User not connected to Central-Server!");
			        }
			    
			    }
			    else {
			        /* For debugging */
			        System.out.println("  DEBUG-08: No current keyword. Did not send request!");
			    }
			    btnReady.setText("");
			}
		});
		
		JButton btnHit = new JButton("Hit");
		btnHit.setBounds(73, 204, 100, 20);
		panelSearch.add(btnHit);
		
		JButton btnStay = new JButton("Stay");
		btnStay.setBounds(240, 204, 100, 20);
		panelSearch.add(btnStay);
		btnReady.setBounds(86, 12, 244, 19);
		btnReady.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 12));
		panelSearch.add(btnReady);
		
		table = new JTable();
		table.setBounds(12, 175, 285, -125);
		panelSearch.add(table);
		
		JPanel panelFTP = new JPanel();
		panelFTP.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		panelFTP.setBounds(12, 480, 415, 106);
		frame.getContentPane().add(panelFTP);
		panelFTP.setLayout(null);
		
		TextArea ftpTextArea = new TextArea();
		ftpTextArea.setBounds(10, 33, 395, 137);
		panelFTP.add(ftpTextArea);
		ftpTextArea.setEditable(false);
		
		JLabel lblEnterCommand = new JLabel("Enter Command:");
		lblEnterCommand.setFont(new Font("Dialog", Font.PLAIN, 12));
		lblEnterCommand.setBounds(12, 12, 125, 15);
		panelFTP.add(lblEnterCommand);
		
		ftpCommandField = new JTextField();
		ftpCommandField.setBounds(119, 10, 225, 19);
		panelFTP.add(ftpCommandField);
		ftpCommandField.setColumns(10);
		
		JButton btnGo = new JButton("Go");
		btnGo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				/*
				Actions to happen on click of "Search"
				*/
				String toAdd = "";
				
				if (alreadySetupFTPServer == true && isConnectedToCentralServer == true) {
					String commandLine = ftpCommandField.getText();
					commandHistory = commandHistory + ">>> " + commandLine + "\n";
					ftpTextArea.setText(commandHistory);
					
					StringTokenizer cmdTokens = new StringTokenizer(commandLine);
					String firstToken = cmdTokens.nextToken();
					firstToken = firstToken.toLowerCase();
					
					if(!firstToken.equals("")) {
						if (firstToken.equals("connect")){
							System.out.println("  DEBUG: Inside connect function.");
							
							try {
								ipAddress = cmdTokens.nextToken();
								portNum = cmdTokens.nextToken();
								
								toAdd = "Connecting to " + ipAddress +
								    " on port " + portNum + "\n";
							}catch (Exception q){
								System.out.println("ERROR: Improper or invalid " +
								    "arguments!");
								toAdd = "ERROR: Improper or invalid " +
								    "arguments!\n";
							}
							
							//Connect to other user's HostServer.
							try{
								connect(ipAddress, portNum);
								toAdd = "Connected to " + ipAddress 
								+ " on port " + portNum + "!\n";
								
							}catch (Exception q){
								System.out.println("ERROR: Failed to connect"
								+ " to server!");
								toAdd = "ERROR: Failed to connect"
								+ " to server!\n";
							}
							
						}
						else if (firstToken.equals("retr")){
							System.out.println("  DEBUG: Inside retr function.");
							
							try {
							
								String fileName = cmdTokens.nextToken();
								
			/*************************************************************/	
		                        
		                        /* This socket is the "Welcome socket" for the data-link */
                                ServerSocket dataListen; 
		                        /* This socket handles data-links with the server */
                                Socket dataConnection = null;
                                /* This value handles the file transfer */
                                int recvMsgSize;
		                        /* For sending and retrieving file */
                                byte[] byteBuffer = new byte[32768];
		                        
		                        int dataPort = 1240;
		                        
								try {
                                    /* Send the request over the control line */
                                    String toSend = "RETR" + " " + dataPort + " " + fileName;       
                                        
                                    System.out.println("  DEBUG: A");
                                    System.out.println("  DEBUG: Dataport: " + dataPort + " Filename: " + fileName);
                                    
                                        
                                    outToHost.println(toSend);
                                    outToHost.flush();
                                    
                                    System.out.println("  DEBUG: B");
                                    
                                    /* Connect to server and establish variables */
                                    dataListen = new ServerSocket(dataPort);
                                    
                                    System.out.println("  DEBUG: C");
                                    
                                    dataConnection = dataListen.accept();
                                    InputStream inFromServer_Data = 
                                        dataConnection.getInputStream();   


                                    System.out.println("  DEBUG: D");                               
                                    
                                    
                                    /* Below this handles sending the file itself */
                                    String fileRequestedString = 
                                        new String(fileName.getBytes());
                                    while ((recvMsgSize = 
                                            inFromServer_Data.read(byteBuffer)) != -1) {
                                        try {
                                            File fileRequested = 
                                                new File(fileRequestedString);
                                            FileOutputStream fileOutputStream = 
                                                new FileOutputStream(fileRequested);

                                            fileOutputStream.write(byteBuffer, 
                                                                        0, recvMsgSize);
                                            fileOutputStream.close();
                                            
                                            Scanner scanner = new Scanner(fileRequested);
                                            String errorCheck = scanner.nextLine();
                                            if(errorCheck.equals(
                                                              "File does not exist.")) { 
                                                System.out.println(
                                                  "File does not exist.");
                                                fileRequested.delete();
                                            } else {
                                                System.out.println("File retrieved!");
                                            }
                                        } catch (Exception f) {
                                            System.out.println("Error trying to " + 
                                             "retrieve file.");
                                        }
                                    }
                                    
                                    System.out.println("  DEBUG: E");
                                    
                                    /* Close the data connection */
                                    try {
                                        dataListen.close();
                                        dataConnection.close();
                                        toAdd = "File " + fileName + " retrieved!\n";
                                    } catch (Exception h) {
                                        System.out.println("ERROR: Could not close data" +
                                            " connection");
                                    }
                
                                } catch (Exception g) {
                                    System.out.println("ERROR: Failure in " + 
                                        "file retrieval.");
                                }
                                
                                System.out.println("  DEBUG: F");
								
						/*************************************************/		
								
								
								
							}
							catch (Exception q) {
								System.out.println("ERROR: Did not give " + 
								    "arguement to RETR.");
								toAdd = "ERROR: Did not give " + 
								    "arguement to RETR.\n";
							}
							
							// Attempt to retrieve file from selected user.
							
						}
						else if (firstToken.equals("quit")){
							System.out.println("  DEBUG: Inside quit function.");
							if (isConnectedToOtherHost != false){
								disconnect();
								toAdd = "Disconnected from " + ipAddress + ".\n";
								System.out.print(toAdd);
							}else {
								toAdd = "Not connected to a host.\n";
								System.out.println("Not connected to host.");
							}
						}else {
							toAdd = "Invalid command!\n";
						}
					}
				}else {
					String commandLine = ftpCommandField.getText();
					commandHistory = commandHistory + ">>> " + commandLine + "\n";
					ftpTextArea.setText(commandHistory);
					
					toAdd = "Not connected to server!\n";
				}
				commandHistory = commandHistory + toAdd;
				ftpTextArea.setText(commandHistory);
				ftpCommandField.setText("");
			}
		});
		btnGo.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 12));
		btnGo.setBounds(351, 10, 54, 17);
		panelFTP.add(btnGo);
		
		JLabel lblConnection = new JLabel("Connection");
		lblConnection.setBounds(12, 12, 80, 15);
		frame.getContentPane().add(lblConnection);
		
		JLabel lblYourCards = new JLabel("Your Cards");
		lblYourCards.setBounds(12, 193, 196, 15);
		frame.getContentPane().add(lblYourCards);
		
		JLabel lblFtp = new JLabel("FTP");
		lblFtp.setBounds(22, 453, 70, 15);
		frame.getContentPane().add(lblFtp);
		
		JPanel panel = new JPanel();
		panel.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel.setBounds(12, 154, 415, 34);
		frame.getContentPane().add(panel);
		panel.setLayout(null);
		
		JTextArea errorTextArea = new JTextArea();
		errorTextArea.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 12));
		errorTextArea.setBackground(Color.WHITE);
		errorTextArea.setBounds(12, 4, 400, 22);
		errorTextArea.setEditable(false);
		errorTextArea.setText("Error Messages will show here");
		panel.add(errorTextArea);
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
