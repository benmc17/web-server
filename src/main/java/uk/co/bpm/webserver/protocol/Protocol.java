package uk.co.bpm.webserver.protocol;

public interface Protocol {

	byte[] processEvent(byte[] data) throws ProtocolException;
	
	
}
