package uk.co.bpm.webserver.http.request.impl;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Map;

import uk.co.bpm.webserver.http.HttpConstants;
import uk.co.bpm.webserver.http.HttpException;
import uk.co.bpm.webserver.http.HttpGeneralHeader;
import uk.co.bpm.webserver.http.HttpMethod;
import uk.co.bpm.webserver.http.impl.AbstractHttpMessageParser;
import uk.co.bpm.webserver.http.request.HttpRequest;
import uk.co.bpm.webserver.logging.Logger;

public class HttpRequestParserImpl extends AbstractHttpMessageParser {

	private final Logger logger;
	
	public HttpRequestParserImpl(Logger logger) {
		this.logger = logger;
	}

	@Override
	public HttpRequest parse(byte[] httpPacket) throws HttpException {
		
		// Section 5 - Request
		try(ByteArrayInputStream bais = new ByteArrayInputStream(httpPacket);
			BufferedReader in = new BufferedReader(new InputStreamReader(bais))) {
			
			// Section 5.1 - Parse Request Line
			HttpMethod method = getMethod(parseSection(in));

			String requestURIStr = parseSection(in);
			URI requestURI = null;
			
			if(!requestURIStr.equals("*")) {
				requestURI = getRequestURI(requestURIStr);
			}
			
			String httpVersion = parseSection(in);
			
			// skip last LF (Line Feed)
			in.skip(1);

			// Section 4.5, Section 5.3, Section 7.1
			Map<String, String> headers = this.parseHeaders(in);
			
			// CRLF - CRLF
			in.skip(4);
			
			String contentLength = headers.get(HttpConstants.HTTP_HEADER_CONTENT_LENGTH);
			String transferEncoding = headers.get(HttpConstants.HTTP_HEADER_TRANSFER_ENCODING);
			String content = null;
			long contentBytes = -1;
			
			if(transferEncoding != null && !transferEncoding.equals("identity")) {
				//TODO: ignore content length
			} else if(contentLength != null) {
				// Section 4.3 - Parse Message Body
				contentBytes = Long.parseLong(contentLength);
				content = parseMessageBody(in, contentBytes);
			}
			
			HttpGeneralHeader generalHeader = new HttpGeneralHeader();
			return new HttpRequest(headers, contentBytes, content, method, requestURI, requestURIStr, httpVersion, generalHeader);
		} catch(IOException e) {
			throw new HttpException("Error parsing HTTP request, it could be an invalid message.", e);
		}
	}

	private HttpMethod getMethod(String method) throws HttpException {
		try {
			return Enum.valueOf(HttpMethod.class, method);
		} catch(Exception e) {
			throw new HttpException("Invalid HTTP 1.1 Method Specified", e);
		}
	}
	
	private URI getRequestURI(String requestURI) throws HttpException {
		try {
			return URI.create(requestURI);
		} catch(Exception e) {
			throw new HttpException("Invalid HTTP 1.1 Request URI Specified", e);
		}
	}
	
	private String parseSection(BufferedReader in) throws IOException {
		char c;
		StringBuilder builder = new StringBuilder();
		
		while((c = (char) in.read()) != -1) {
			if(c == 32 || c == 13) break;
			
			builder.append(c);
		}
		return builder.toString();
	}
	
	private String parseMessageBody(BufferedReader in, long contentLength) throws IOException {
		char c;
		StringBuilder builder = new StringBuilder();
		
		for(int i = 0; i < contentLength; i++) {
			builder.append((char) in.read());
		}
		return builder.toString();
	}
}
