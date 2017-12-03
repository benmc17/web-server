package uk.co.bpm.webserver.http;

public interface HttpMessageParser {

	public HttpMessage parse(byte[] httpPacket) throws HttpException;
	
}
