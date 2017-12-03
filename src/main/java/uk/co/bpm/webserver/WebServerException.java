package uk.co.bpm.webserver;

public class WebServerException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public WebServerException(String message) {
		super(message);
	}
	
	public WebServerException(String message, Exception e) {
		super(message, e);
	}

}
