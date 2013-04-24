package bu.cs683.ratemycourses.service;

public class ServerUnavailableException extends Exception {
	
	/**
	 * 
	 * @param message
	 */
	public ServerUnavailableException(String message) {
		super(message);
	}

}
