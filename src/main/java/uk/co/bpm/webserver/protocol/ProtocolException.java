package uk.co.bpm.webserver.protocol;

public class ProtocolException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	public ProtocolException(String message) {
		super(message);
	}
	
	public ProtocolException(String message, Exception e) {
		super(message, e);
	}
}
