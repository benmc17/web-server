package uk.co.bpm.webserver.http;

import java.util.Map;

public abstract class HttpMessage {

	private final Map<String, String> headers;
	
	private final long contentLength;
	
	private final String messageBody;
	
	public HttpMessage(Map<String, String> headers) {
		this(headers, -1, null);
	}
	
	public HttpMessage(Map<String, String> headers, long contentLength, String messageBody) {
		this.headers = headers;
		this.contentLength = contentLength;
		this.messageBody = messageBody;
	}

	public String getMessageBody() {
		return messageBody;
	}

	public long getContentLength() {
		return contentLength;
	}
	
	public String getHeaderValue(String headerKey) {
		return headers.get(headerKey);
	}
}
