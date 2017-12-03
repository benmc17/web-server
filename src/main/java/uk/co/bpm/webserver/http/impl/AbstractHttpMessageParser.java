package uk.co.bpm.webserver.http.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import uk.co.bpm.webserver.http.HttpException;
import uk.co.bpm.webserver.http.HttpMessageParser;

public abstract class AbstractHttpMessageParser implements HttpMessageParser {

	protected Map<String, String> parseHeaders(BufferedReader in) throws HttpException {
		try {
			boolean endOfHeader = false;
			StringBuilder builder = new StringBuilder();
			Map<String, String> headers = new HashMap<String, String>();
			
			String key = null;

			while(!endOfHeader) {
				char c = (char) in.read();
				
				if(c == ':' && key == null) {
					key = builder.toString();
					builder = new StringBuilder();
					in.skip(1);
					continue;
				} else if(c == 13) {
					in.skip(1);
					c = (char) in.read();
					
					if(c == 13) {
						endOfHeader = true;
						continue;
					} else if(c != 32) {
						headers.put(key, builder.toString());
						key = null;
						builder = new StringBuilder();
						builder.append(c);
					}
				} else {
					builder.append(c);
				}
			}
			return headers;
		} catch(IOException ex) {
			
		}
		return null;
	}

}
