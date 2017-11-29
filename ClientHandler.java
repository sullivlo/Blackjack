import java.net.*;
import java.util.*;
import java.io.*;
public class ClientHandler extends Thread{
	
	/** This handles the stream from the command-line of client */
    private Scanner inFromClient;
    
    /** This allows for identification of specific users in streams */
    private String remoteIP;
    
    /** This socket takes commands from client */
    private Socket controlListen;
    
    /** This handles the output stream by command-line to client */
    private PrintWriter outToClient;
    
    /** This is used to grab bytes over the data-line */
    private String recvMsg;
    
    /** This is for thread control */
    private boolean endThread = false;
    
    /** This takes the user information from the host     */
    private String userInformation;
    private String UserName;
    private String UserHostName;
    private String UserSpeed;
    private String UserFTPWelcomePort;
    private String tmpFileName, tmpKeyWords;
      
    private HostedDescriptions allHostedDescriptions;  
      
	/*******************************************************************
    *
    * Beginning of thread.
    * This constructor marks the beginning of a thread on the server.
    * Things here happen once, exclusively with THIS connected client.
    *
    ******************************************************************/
   public ClientHandler(Socket controlListen, HostedDescriptions totalHostedDescriptions) {
       
       /* Make this all... point to the total... */
       allHostedDescriptions = totalHostedDescriptions;
       
       try {
           /* Setting up a threaded input control-stream */
           inFromClient = new Scanner (
               controlListen.getInputStream());
           /* Setting up a threaded output control-stream */
           outToClient = 
               new PrintWriter(controlListen.getOutputStream());
           /* For error handling */
           /* Get IP from the control socket for future connections */
           remoteIP = controlListen.getInetAddress().getHostAddress();
           
           /* For debugging */
           // System.out.println("  DEBUG: A new thread was successfully setup.");
           // System.out.println("");
           
       } catch(IOException ioEx) {
           ioEx.printStackTrace();
           System.out.println("  ERROR: Could not set up a " +
                       "threaded client input and output stream.");
       }
   }
   
   /******************************************************************
   *
   * Beginning of main thread code.
   * This method marks the threaded area that this client receives
   * commands and handles. When this receives "QUIT" from the client,
   * the thread closes.
   * 
   ******************************************************************/
    public void run(){   
      	
    	/* Keeps tracks of when to close the thread */
        boolean stayAlive = true;
        /** Gets user information from the host      */
        StringTokenizer userTokens;
        
        try {
            /* This grabs the string of data with filenames and keys */
        	userInformation = inFromClient.nextLine();
        	
        	/* For Debugging. This shows what is read from control-line. */ 
        	// System.out.println("  DEBUG: Read-In: " + userInformation);
        	
        	/* Make the string parseable by tokens */         	 
        	userTokens = new StringTokenizer(userInformation);
        	
        	/* Initialize and display the new user's shown username */
            UserName = userTokens.nextToken();
            UserName = UserName.replaceAll("@@", " ");
            
            /*
        	 We can error handle multiple users on the same IP (two people 
        	 from the same house) by checking THIS username against the 
        	 stored ones. On User-SUCCESS, of choosing a unique one, 
        	 continue as planned. On User-ERROR, of choosing a taken one,
        	 never show the main table of info to the user, and break the 
        	 current thread.
        	 A failure to the user will look as nothing happened, while
        	 success will look as a table generated.
        	*/
            
            /* If username taken, throw error, return a message, and stop thread. */
        	if (allHostedDescriptions.isUsernameTaken(UserName)) {
                System.out.println("  ERROR-02: \""+ UserName + "\" [" + remoteIP + "] must" +
                                   " pick another username to connect!");
                                   
                /* Send notification to client to rechoose a username */
                outToClient.println("BAD-USERNAME");
                outToClient.flush();
                
                endThread = true;
                throw new EmptyStackException();
        	}
        	else {
        	    /* Send confirmation of username choice */
        	    outToClient.println("GOOD-USERNAME");
                outToClient.flush();
        	}
            
            System.out.println("Username: " + UserName);
                     
            /* 
             TODO - Change UserHostName
             Brendon - NOV 11, 2017
             The User's IP is grabbed directly from the stream by our
             methods. Right now, the variable remoteIP equals the user's 
             actual IP regardless of what the user typed in. I propose we 
             change UserHostName to be strictly the User's 
             typed-computer-name, to have our program paste the ACTUAL IP 
             to the end. Currently, a User could type in a false IP to 
             later have another user ping the wrong address.
            */

            /* Initialize and display the new user's hostname and IP */
            UserHostName = userTokens.nextToken();
            UserHostName = UserHostName.replaceAll("@@", " ");
            System.out.println("UserHostname: " + UserHostName);

            /* Initialize and display the new user's speed */
            UserSpeed = userTokens.nextToken();
            System.out.println("UserSpeed: " + UserSpeed); 

            /* Display new user's IP */
            System.out.println("IP-Address: " + remoteIP);
            
            /* Display new user's FTP Welcome Port */
            UserFTPWelcomePort = userTokens.nextToken();
            System.out.println("FTP-Welcome-Port: " + UserFTPWelcomePort);
        	 
            /* Variables used in parsing the crazy string */
            String tmp_a_Token, tmpFilename, tmp_c_Token;
            try {
                /* First "Filename" */
                tmp_a_Token = userTokens.nextToken();
                /* First "Image.jpg" */
                tmpFileName = userTokens.nextToken();
            }
            catch (Exception e) {
                /* There seems to be no uploads from this user */
                System.out.println("  DEBUG-06: No uploads from user.");
            }
            
            String tmpKeywordList = "";
            int numberOfFilesDescriptions = 0;
            while (true) {
                /* If there are still tokens left */
                try {
                    /* Should mostly be keys */
                    tmp_c_Token = userTokens.nextToken();
                } catch (Exception e) {
                    /* The last submit, as if "Filename" found again */

                    /* Add file description to the main collection */
                    allHostedDescriptions.addValues(UserSpeed, UserHostName, tmpFileName, tmpKeywordList, UserName, remoteIP, UserFTPWelcomePort);

                    /* For debugging */
                    // System.out.println("Submit: " + tmpFileName + tmpKeywordList);
                    
                    /* To show number of uploads at user entry */
                    numberOfFilesDescriptions++;
                    
                    break;
                }
                if (tmp_c_Token.equals("FILENAME")) {

                    /* Add file description to the main collection */
                    allHostedDescriptions.addValues(UserSpeed, UserHostName, tmpFileName, tmpKeywordList, UserName, remoteIP, UserFTPWelcomePort);

                    /* For debugging */
                    // System.out.println("Submit: " + tmpFileName + tmpKeywordList);
                    
                    /* To show number of uploads at user entry */
                    numberOfFilesDescriptions++;
                    
                    tmpKeywordList = "";
                    /* "Image.jpg" */
                    tmpFileName = userTokens.nextToken();
                } 
                else {
                    tmpKeywordList = tmpKeywordList + " " + tmp_c_Token; 
                }
            }
            
            /* 
             Once the file-descriptions are parsed, display a success 
             report of number of uploads to output on the server.
             */
            System.out.println("Number-of-Uploaded-Files: " + numberOfFilesDescriptions);
             
        } catch (Exception e) {
            /* Host did not supply user information  */
            System.out.println("  ERROR-04: Host did not supply valid information");
            endThread = true;
        }
        
        /* End the thread */
        if (endThread == true) {
            System.out.println("  ERROR-03: Ending user's thread");
            
            /* Remove all rows with this username */
            allHostedDescriptions.remove(UserName);
            
            /* Show the server data on user-leave */
            System.out.println(" ");
            allHostedDescriptions.showData();
            System.out.println(" ");
            
            return;
        }
        
        /* For show. To separate user-logins. */
        System.out.println(" ");
        
        /* For debugging */
        // System.out.println("  DEBUG-01: User's thread running");
        
        /* For show. View the server's main data points on each new entrance! */
        allHostedDescriptions.showData();
        
        /* For show. To separate information. */
        System.out.println(" ");
        
        /* The controlling loop that keeps the user alive */
        while (stayAlive) {
        	
        	/* For debugging */
            // System.out.println("  DEBUG-02: User's thread running");
        	
            /* This reads the command from the client */
            try {
                recvMsg = inFromClient.nextLine();
                
            } catch (Exception e) {
                /* Client left early, or otherwise */
                System.out.println("Client " + UserName + " [" + remoteIP + "] left early!");
                
                /* Remove all rows with this username */
                allHostedDescriptions.remove(UserName);
                
                /* Show the server data on user-leave */
                System.out.println(" ");
                allHostedDescriptions.showData();
                System.out.println(" ");
                
                break;
            }
            
            /* For token handling */
            String returnedString = "";
            String currentToken;
            StringTokenizer tokens = new StringTokenizer(recvMsg);

            /* Client command, "UPDATE" or another. */
            String commandFromClient = tokens.nextToken();
            
            /* For variable handling */
            String key, fileName, keyWords;  
            String totalKeys = "";
             
            /* While checks for more user input in the token */
            while (tokens.hasMoreTokens()) {
            	 
                /* Client opened dataport, "1079," for the server to
                   connect to */
                /* NOTE - This "clientDataPort" may be changed later. It was added to
                 * have continuity with the FTP-server that we developed. Oct 25.
                 */
                
                try {
                    
                    /* This commands initiates the keyword-search */
                    if(commandFromClient.equals("KEYWORD")){
                        /*Passes the last arguement into the key */
                        key = tokens.nextToken();
                        
                        /* For debugging */
                        // System.out.println("Key " + key);
                        totalKeys = totalKeys + " " + key;
                    }
                    /* This command updates the files the host have in their root directory */ 
                    else if(commandFromClient.equals("UPDATE")){

                        /* Passes the last arguement into the key */
                        fileName = tokens.nextToken();
                        System.out.println("Filename: " + fileName);

                        keyWords = tokens.nextToken();
                        System.out.println("keyWords " + keyWords);
                    }
                }
                catch (Exception e) {
                /* Was there a token error? */
                
                    /* For debugging */
                    System.out.println(" DEBUG-07: End of Parsing Tokens");
                }
	        
	        /* End of hasMoreTokens loop */    
            }
            
            /* For server log printing */
            System.out.println("Client " + UserName + " [" + remoteIP + "]: ");
            
            /* For server log printing */
            if (commandFromClient.equals("QUIT")) {
                System.out.println("Client " + UserName + " [" + remoteIP + "] disconnected!");
                
                /* Remove all rows with this username */
                allHostedDescriptions.remove(UserName);
                
                stayAlive = false;
            }
            else if (commandFromClient.equals("KEYWORD")) {
                System.out.println("Ran Command: KEYWORD:" + totalKeys);
                
                /* 
                 HERE, do the methods for figuring which things to send 
                 back to the client.
                */ 
                String toSendToClient = allHostedDescriptions.getKeywordData(totalKeys);

                /* For debugging */
                // String tempstring = "This works!";
                
                /* Send the query to the server! */
                outToClient.println(toSendToClient);
		outToClient.flush();
                
                System.out.println("  DEBUG: Succesfully Sent: " + toSendToClient);
            }
            else if (commandFromClient.equals("UPDATE")) {
                System.out.println("Ran Command: UPDATE");
            }
            else if (commandFromClient.equals("DISCONNECT")) {
                System.out.println("Ran Command: DISCONNECT");
                
                /* Client left early, or otherwise */
                System.out.println("Client " + UserName + " [" + remoteIP + "] left!");
                
                /* Remove all rows with this username */
                allHostedDescriptions.remove(UserName);
                
                /* Show the server data on user-leave */
                System.out.println(" ");
                allHostedDescriptions.showData();
                System.out.println(" ");
                
                break;
            }
            
            
            /* For server log printing */
            System.out.println(" ");
            
                
        /* End of the other while loop */
        }
    
    /* End of threading run() */  
    }
    
/* End of the thread class */  
}
	
