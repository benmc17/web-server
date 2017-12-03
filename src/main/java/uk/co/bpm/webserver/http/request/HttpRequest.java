package uk.co.bpm.webserver.http.request;

import java.net.URI;
import java.util.Map;

import uk.co.bpm.webserver.http.HttpGeneralHeader;
import uk.co.bpm.webserver.http.HttpMessage;
import uk.co.bpm.webserver.http.HttpMethod;

public class HttpRequest extends HttpMessage {

	private final HttpMethod method;
	
	private final URI requestURI;
	
	private final String plainRequestURI;
	
	private final String httpVersion;
	
	private final HttpGeneralHeader generalHeader;
	
	public HttpRequest(Map<String, String> headers, long contentLength, String messageBody, HttpMethod method, URI requestURI, String plainRequestURI, String httpVersion, HttpGeneralHeader generalHeader) {
		super(headers, contentLength, messageBody);
		
		this.method = method;
		this.requestURI = requestURI;
		this.plainRequestURI = plainRequestURI;
		this.httpVersion = httpVersion;
		this.generalHeader = generalHeader;
	}
	
	public HttpMethod getMethod() {
		return this.method;
	}
	
	public URI getRequestURI() {
		return this.requestURI;
	}

	public String getPlainRequestURI() {
		return this.plainRequestURI;
	}

	public String getHttpVersion() {
		return httpVersion;
	}

	public HttpGeneralHeader getGeneralHeader() {
		return generalHeader;
	}
}
