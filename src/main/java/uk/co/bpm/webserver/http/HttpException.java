package uk.co.bpm.webserver.http;

public class HttpException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public HttpException(String message) {
		super(message);
	}
	
	public HttpException(String message, Exception e) {
		super(message, e);
	}
}
