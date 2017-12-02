import java.net.*; 
import java.util.*;
import java.io.*; 
import java.awt.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;


public class Host {
	
	/* To handle connecting to Central-Server */
	static Socket server;
	
	/* Central-Server's IP */
	private String serverHostname;
	/* Central-Server's Port */
	private String port;
	/* User's username */
	private String username;
	/* User's name of computer... and IP */
	private String hostname;
	/* User's submitted internet speed */
	private String speed;
	/* User's FTP port for others to connect to for downloading */
	private String hostFTPWelcomePort;
	
    /* This handles the control-line out stream */
    PrintWriter outToServer_Control = null;
    /* This handles the control-line input stream */
    Scanner inFromServer_Control = null;
	
	/* Checks for the user being already connected */
    private boolean isConnected = false;

	@SuppressWarnings("resource")
    public static void main(String[] args) throws Exception {}
	
	public void disconnectFromServer() {
	
	    outToServer_Control.println("DISCONNECT");
        outToServer_Control.flush();
	
	    isConnected = false;
	}
	
	/**
	 * This method, intiated by the GUI, handles connecting to the 
	 * Central-Server. 
	 *
	 * @param serverHostname
	 * @param port
	 * @param username
	 * @param hostname
	 */
	public int connectToServer(String serverHostname, String port, 
	                            String username, String hostname, 
	                            String speed, String hostFTPWelcomePort) {
	                            
		/* Taking the parameters from the GUI */
		this.serverHostname = serverHostname;
		this.port = port;
		this.username = username;
		this.hostname = hostname;
		this.speed = speed;
		this.hostFTPWelcomePort = hostFTPWelcomePort;
	    
	    /* This socket handles communicating main commands to server */
	    Socket controlSocket = null;	    

	    /* To catch if there was errors. Limits sending. */
	    boolean hadConnectErrors = false;
	    
	    /* Only DO connect if NOT already connected */
	    if (isConnected == false){
	    	
	    	/* Establish a TCP connection with the server */
		    try {
		        controlSocket = new Socket(serverHostname, 
		                             Integer.parseInt(port));
		        boolean controlSocketOpen = true;
		    } catch (Exception p) {
		        System.out.println("  ERROR: Did not find socket!");
		        hadConnectErrors = true;
		    }
		                        
		    /* Set-up the control-stream. Handle errors. */
		    try {
		        inFromServer_Control = 
		           new Scanner(controlSocket.getInputStream());
		        outToServer_Control = 
		           new PrintWriter(controlSocket.getOutputStream());
		        isConnected = true;
		        
		        /* For debugging */
		        // System.out.println("  DEBUG: Connection stream intiated!");
		        // System.out.println(" ");
		    }
		    catch (Exception e) {
		        System.out.println("  ERROR: Did not connect to " +
		            "server!");
		        hadConnectErrors = true;
		        isConnected = false;
		    } 
		    		     
		    /* 
		     The following prepares the string "totalDescription" to be 
		     sent over the wire. This string contains all the keywords and
		     filenames of the files included within the client's XML 
		     document that should be within their source folder.
		     
		     TODO: This CAN validate that each of those files exist in 
		     the directory before sending any list to the Central-Server.
		     There could be a case that the XML document has misspellings
		     and causes issue with retrieval of files by another host.
		     
		     NOTE: The following website was a great help to this function
		     of parsing from XML. 
		     https://www.mkyong.com/java/how-to-read-xml-file-in-java-dom-parser/
		    */
			String allFilenamesAndKeys = "";
		    boolean errorReadingXML = false;
		    try {
		    	/* Prepare the document for XML extraction */
			    File fXmlFile = new File("HostedFiles.xml");
			    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			    Document doc = dBuilder.parse(fXmlFile);
			    
				//optional, but recommended
			    //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			    doc.getDocumentElement().normalize();
			   
			    NodeList nList = doc.getElementsByTagName("File");
			    
			    /* For each "object," or, each file-description iterate */
			    /* Each iteration would send a string to the central-server */
			    /* TO-DO: (1) Look over this. (2) Implement the server listening for these strings. */
			  
			    
                /* Loop through each "<File id='0001'>"... */ 
                for (int temp = 0; temp < nList.getLength(); temp++) {
					
			        Node nNode = nList.item(temp);
					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					
		                /* The target element. Ex: "<File id='0001'>" */
						Element eElement = (Element) nNode;
		
						/* 
						 NOTE - If later we want to include the 
						 display-name of the file... 
						 
						 String DisplayName = 
						  eElement.getElementsByTagName("DisplayName").item(0);
						*/
						
						/* Grab the filename */
						String FileName = eElement.getElementsByTagName("FileName").item(0).getTextContent();
						FileName = FileName.replaceAll(" ", "");	
						String KeyWords = "";
						
						/* How many keys are there? */
						int tmpLength = eElement.getElementsByTagName("Key").getLength();
						
						/* For debugging */
						// System.out.println("  DEBUG: Filename: " + FileName);
					    // System.out.println("  DEBUG: Number of keys: " + tmpLength);
						
						/* Wrap all the keys to one string */
						String tempKey;
						String totalKeysForCurrentFile = "";
						for(int i = 0; i < tmpLength; i++) {
						    tempKey = eElement.getElementsByTagName("Key").item(i).getTextContent();
						    totalKeysForCurrentFile = totalKeysForCurrentFile + " " + tempKey;
						    /* For debugging */
						    // System.out.println("  DEBUG: tempKey: " + tempKey);
						}
						
						/* For debugging */
						// System.out.println("  DEBUG: File's Keys: " + totalKeysForCurrentFile);
                        // System.out.println(" ");
                        
                        /* Attach those keys to the whole sending-string */
                        allFilenamesAndKeys = allFilenamesAndKeys + "FILENAME " + FileName + " " + totalKeysForCurrentFile + " ";
					}
				}
				
		    } catch (Exception e) {
		        System.out.println("  ERROR: Error in reading XML " + 
		                           "document!");
		        hadConnectErrors = true;
		        errorReadingXML = true;
		        return (-6);
		    } 
		    
		    /* 
		     If there are no errors to this point and the client is 
		     connected to the Central-Server, then send data. 
		     */
		    if (isConnected == true && hadConnectErrors == false) {
		        /* 
		         Send userName, HostName, speed, and each filename and 
		         its associated keys to the Central-Server 
		         */
		        String newUserInformation = username + " " + hostname + " "
		                                    + speed + " " + hostFTPWelcomePort + " " 
		                                    + allFilenamesAndKeys;
		                                    
		        /* For debugging. View the sent filenames and keys. */
		        // System.out.print("  DEBUG: Sent String To Connect: " 
		        //                  + newUserInformation);
		                         
		        outToServer_Control.println(newUserInformation);
		        outToServer_Control.flush();
		        
		        String recvMsg;
		        try {
		            /* Wait for response from server */
		            recvMsg = inFromServer_Control.nextLine();
		            
		            if (recvMsg.equals("GOOD-USERNAME")) {
		                /* For debugging */
                        System.out.println("  DEBUG: Good username!");
                        isConnected = true;
		                return (1);
		            }
		            else if (recvMsg.equals("BAD-USERNAME")) {
		                /* For debugging */
                        System.out.println("  DEBUG: Bad username!");
                        isConnected = false;
		                return (-2);
		            }
		            else {
		                /* For debugging */
                        System.out.println("  DEBUG: Corrupted response from server!");
                        isConnected = false;
		                return (-3);
		            }
                } 
                catch (Exception e) {
                    /* For debugging */
                    System.out.println("  DEBUG: Failed response from server!");
                    isConnected = false;
                    return (-4);
                }
 
		        /* TODO - 
		         Wait for response from server to validate that THAT 
		         username is valid. Username may be the best unique 
		         identifiers for end-users. It may also help in backend.
		        */
		        
		    }
		    else if (isConnected == true && hadConnectErrors == true && errorReadingXML == true) {
		        /* Send command to end thread because of client issues. */
		        
		        /* Which error message to send */
		        String sendErrorMessage = "XML-READ-ERROR";
		                                    
		        /* For debugging. View the sent filenames and keys. */
		        System.out.print("  DEBUG: Sent String: " 
		                         + sendErrorMessage);
		                         
		        /* Send the error message */
		        outToServer_Control.println(sendErrorMessage);
		        outToServer_Control.flush();
		        
		        /* Reset the connected status to later reconnect */
		        isConnected = false;
		    } 
		    else {
		        /* 
		         Return to user that there was an error in connecting. 
		         This could be by the GUI. It could be several options of
		         issue: (1) an already taken username, (2) no XML document, 
		         (3) a missing file that was called by the XML document,
		         (4) no connection to available to the called IP of
		         Central-Server, (5) refused connection by the "CS" at
		         application-level.
		        */
		        
		        /* For debugging */
		        System.out.println("  DEBUG: Error in connecting...");
		        return (-5);
		    }
		    
		/* Ends of if(isConnected == false) */   
	    }
	    else {
	        /* For debugging */
		    System.out.println("  DEBUG-04: Already connected to Central" +
		    "-Server!");
		    return (-7);
	    }
	    
	    return (0);
	/* End of connectToServer() */
	}

    /* This allows the GUI to send a keyword search to the Central-Server */
    public String queryKeywords(String keySearch) {
    
        /* Prepare the command for the central-server */
        String toSend = "KEYWORD" + " " + keySearch;
        
        /* Send the query to the server! */
        outToServer_Control.println(toSend);
	outToServer_Control.flush();
		
	/* Listen for Server Response */
	/* This would have all the files that have the key involved */
	try {
		String recvMsg;
		recvMsg = inFromServer_Control.nextLine();
		System.out.println("  DEBUG: Received message: " + recvMsg);
		    
		return (recvMsg);
		    
	}catch (Exception e) {
		System.out.println("  DEBUG: Connection broke with server while waiting for response.");
		return ("ERROR");
	}
		
		
	/* For debugging */
        // System.out.println("  DEBUG: Sending: " + toSend);
        // System.out.println("  DEBUG: End of queryKeywords()");

    /* End of queryKeywords() */
    }


/* End of FTP client */
}
