package uk.co.bpm.webserver.workers.impl;

import java.nio.channels.SocketChannel;

public class WebServerEvent {
	
	private final SocketChannel socket;
	
	private final byte[] data;
	
	private final int size;

	public WebServerEvent(SocketChannel socket, byte[] data, int size) {
		this.socket = socket;
		this.data = data;
		this.size = size;
	}
	
	public int getSize() {
		return size;
	}
	
	public byte[] getData() {
		return data;
	}
	
	public SocketChannel getSocket() {
		return socket;
	}
}
