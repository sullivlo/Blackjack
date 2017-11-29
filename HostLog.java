import java.net.*;
import java.util.*;
import java.io.*;
import java.awt.*;

/**
 * 
 * @author Louis Sullivan
 * @see HostLog creates a object with String Filename,
 * String UserIP, 
 * String UserSpeed,String Description
 * 
 */
public class HostLog {
	private String Filename, UserIP, 
	UserSpeed, Description;
	
	public HostLog(String Filename,String UserIP, 
			String UserSpeed,String Description) {
		
		this.Description = Description;
		this.Filename = Filename;
		this.UserIP = UserIP;
		this.UserSpeed = UserSpeed;
	}
	
	public String getFilename() {
		return Filename;
	}
	
	public String getUserIP() {
		return UserIP;
	}
	
	public String getUserSpeed() {
		return UserSpeed;
	}
	
	public String getDescription() {
		return Description;
	}
	
	
	
}
