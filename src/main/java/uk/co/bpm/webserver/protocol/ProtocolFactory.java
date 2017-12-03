package uk.co.bpm.webserver.protocol;

import uk.co.bpm.webserver.WebServerException;
import uk.co.bpm.webserver.http.HttpMessageParser;
import uk.co.bpm.webserver.http.HttpProtocol;
import uk.co.bpm.webserver.https.HttpsProtocol;

public class ProtocolFactory {

	public ProtocolFactory() {}
	
	public Protocol createProtocol(ProtocolType type, HttpMessageParser requestParser) throws WebServerException {
		switch(type) {
			case HTTP: 
				return new HttpProtocol(requestParser);
			case HTTPS:
				return new HttpsProtocol();
			default:
				throw new WebServerException("Unrecognised Protocol: ");
		}
	}
	
}
