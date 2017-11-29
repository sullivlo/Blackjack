import java.util.ArrayList;
/* For tokens */
import java.util.*;

public class HostedDescriptions {
	
	private ArrayList<String> speeds;
	private ArrayList<String> hostnames;
	private ArrayList<String> filenames;
	private ArrayList<String> keywords;
    private ArrayList<String> usernames;
    private ArrayList<String> userIPs;
    private ArrayList<String> userPorts;    

	public HostedDescriptions() {
	    userIPs = new ArrayList<String>();		
	    userPorts = new ArrayList<String>();		
	    usernames = new ArrayList<String>();		
		speeds = new ArrayList<String>();
		hostnames = new ArrayList<String>();
		filenames = new ArrayList<String>();
		keywords = new ArrayList<String>();
	}
	
	public void addValues (String addThisToSpeeds, String addThisToHostname, String addThisToFilename, String addThisToKeywords, String addThisToUsernames, String addThisToUserIPs, String addThisToUserPorts) {
		this.speeds.add(addThisToSpeeds);
		this.hostnames.add(addThisToHostname);
		this.filenames.add(addThisToFilename);
		this.keywords.add(addThisToKeywords);
		this.usernames.add(addThisToUsernames);
		this.userIPs.add(addThisToUserIPs);
		this.userPorts.add(addThisToUserPorts);
	}
	
	/* Removes all elements that belong to the client with a specific username */
	public void remove (String username) {
		for(int i = 0; i < usernames.size(); i++) {
			if(usernames.get(i).equals(username)) {
			    usernames.remove(i);
				hostnames.remove(i);
				speeds.remove(i);
				filenames.remove(i);
				userIPs.remove(i);
				keywords.remove(i);
				userPorts.remove(i);
				i--;
			}
		}
	}
	
	/* Returns true if the username is already taken */
	public boolean isUsernameTaken(String username) {
	    if (usernames.contains(username)) {
	        return true;
	    } 
	    else {
	        return false;
	    }	    
	}
	
	/* Displays some information about the current state of the main tables */
	public void showData() {
	    ArrayList<String> variousUsernames = new ArrayList<String>();
	    int numberOfUsers;
	    try {
            variousUsernames.add( usernames.get(0) );
            for(int i = 0; i < usernames.size(); i++) {
                if (!variousUsernames.contains( usernames.get(i) )) {
                    variousUsernames.add( usernames.get(i) );
                }
	        }
	        numberOfUsers = variousUsernames.size();
	    }
	    catch (Exception e) {
	        numberOfUsers = 0;
	    }
	    
	    System.out.println("Showing Current Server Data...");
	    System.out.print(" Total Current Users: " + numberOfUsers);
	    if (numberOfUsers > 1) {
	        System.out.print(" [");
	        for(int j = 0; j < variousUsernames.size() - 1; j++) {
	            System.out.print( variousUsernames.get(j) + ", ");
	        }
	        System.out.print( variousUsernames.get(numberOfUsers - 1) + "]");
	    }
	    System.out.print("\n");
	    
	    if (numberOfUsers != 0) {
	        System.out.println(" Total Files Represented: " + usernames.size() );	    
            System.out.print(" List of Files: \n");
            System.out.println(" [Username, IP-Address, FTP-Port-Num, Speed, Image]");
            for(int k = 0; k < filenames.size(); k++) {
                System.out.print(" [" + usernames.get(k) + ", " + userIPs.get(k) + ", " + userPorts.get(k) + ", " + speeds.get(k) + ", " + filenames.get(k) + "] \n");
            }
        }
        
    /* End of showData() */
	}
	
	public String getKeywordData(String keywordsList) {
		/* keywordsLIST = "image apple banana tom cruise"
	    
		/* Construct the thing to return */
		/* Ex: "FILE username userIP userPort filename speed */
		String stringToReturn = "";
		
		
		
		StringTokenizer tokens = new StringTokenizer(keywordsList);	
	    
		String grabbedFiles = "";
		
		while (tokens.hasMoreTokens()) {
			try {
				/* currentToken equals one key from the client's string */
				/* Ex: "apple" */
				String currentToken = tokens.nextToken();
				
				/* Loops through all the files in search for a keyword */
				for(int i= 0; i < keywords.size(); i++) {
				    
				    /* If a file has this keeyword inside */
				    if ( keywords.get(i).contains(currentToken) 
					    && !grabbedFiles.contains(filenames.get(i) + usernames.get(i)
					    ) ) {
						/* To build the string to send back */
						/*stringToReturn = stringToReturn + " FILE " + usernames.get(i) + " " +
						userIPs.get(i) + " " + userPorts.get(i) + " " + 
						filenames.get(i) + " " + speeds.get(i); */
						
						stringToReturn = stringToReturn + "FILE " + usernames.get(i) 
						+ " " + userIPs.get(i) 
						+ " " + userPorts.get(i) 
						+ " " + filenames.get(i) 
						+ " " + speeds.get(i);
						
						/* Allows for uniqueness of files with users */
						String identifier = filenames.get(i) + usernames.get(i);
						
						/* For checking if the file was already appended */
						grabbedFiles = grabbedFiles + " " + identifier;
					    
					    
					    
						/* For debugging */
						System.out.println("  DEBUG-09: File has "
						+ "key (" + currentToken + "): " 
						+ filenames.get(i) );
				    }
				}
			}catch (Exception e) {
			System.out.println("  ERROR-08: Caught nextToken error!");
			}
		}
		
		if (stringToReturn == "") {
			return ("NOFOUNDMATCHES");
		}
		return stringToReturn;
	}
	
	
/* End of class HostedDescriptions */
}
