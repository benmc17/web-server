package uk.co.bpm.webserver.logging;

public interface Logger {

	public void info(String message);
	
	public void error(String message);
	
	public void error(String message, Exception ex);
	
	public void trace(String message);
	
	public void debug(String message);
	
	public void out(String message);
}
