package com.example.ratemycourses.service;

public class ServerUnavailableException extends Exception {
	
	/**
	 * 
	 * @param message
	 */
	public ServerUnavailableException(String message) {
		super(message);
	}

}
