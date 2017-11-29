/**
 * 
 * This object holds all the variables and methods that represent one
 * hosted file on the Central Server. There could be multiple objects
 * for the same "FileName.jpg."
 * 
 * @author sullivlo
 *
 */
public class FileContainer {
	   
	
	
	String speed;
	String hostIP;
	String fileName;
	String keyString;
	
	public String getSpeed() {
		return speed;
	}
	public void setSpeed(String speed) {
		this.speed = speed;
	}
	public String getHostIP() {
		return hostIP;
	}
	public void setHostIP(String hostIP) {
		this.hostIP = hostIP;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getKeyString() {
		return keyString;
	}
	public void setKeyString(String keyString) {
		this.keyString = keyString;
	}
	
	/* Returns true if the string exists for this file */
	boolean doesKeyExist(String checkThis) {
		
		return false;
		
	}
	
}