package uk.co.bpm.webserver;

import java.nio.channels.SocketChannel;

public interface WebServerProcess extends Runnable {
	
	public void stop();
	
	public void send(SocketChannel socket, byte[] data);
}
