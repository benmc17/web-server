package uk.co.bpm.webserver.http;

import uk.co.bpm.webserver.http.request.HttpRequest;
import uk.co.bpm.webserver.protocol.Protocol;
import uk.co.bpm.webserver.protocol.ProtocolException;

public class HttpProtocol implements Protocol {

	private HttpMessageParser requestParser;
	
	public HttpProtocol(HttpMessageParser requestParser) {
		this.requestParser = requestParser;
	}

	@Override
	public byte[] processEvent(byte[] data) throws ProtocolException {
		try {
			HttpRequest request = (HttpRequest) requestParser.parse(data);
		
		
			HttpContext context = new HttpContext();
			
		} catch(HttpException e) {
			
		}
		
		
		return null;
	}

}
