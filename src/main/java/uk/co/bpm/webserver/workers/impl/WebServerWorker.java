package uk.co.bpm.webserver.workers.impl;

import java.util.Queue;
import java.util.UUID;

import uk.co.bpm.webserver.WebServerProcess;
import uk.co.bpm.webserver.logging.Logger;
import uk.co.bpm.webserver.protocol.Protocol;

public class WebServerWorker implements Runnable {

	private final UUID id;
	
	private final Protocol protocol;
	
	private final Logger logger;
	
	private final Queue<WebServerEvent> queue;
	
	private final WebServerProcess webServerProcess;
	
	private boolean running = true;
	
	public WebServerWorker(WebServerProcess webServerProcess, Queue<WebServerEvent> queue, Protocol protocol, Logger logger) {
		this.id = UUID.randomUUID();
		this.protocol = protocol;
		this.logger = logger;
		this.queue = queue;
		this.webServerProcess = webServerProcess;
	}
	
	@Override
	public void run() {
		WebServerEvent event = null;
		int attempts = 0;
		logger.debug("Starting worker " + id.toString());
		
		while(running) {
			try {
				synchronized(queue) {
					
					while(queue.isEmpty()) {
						try {
							queue.wait();
						} catch(InterruptedException e) {}
					}
					event = queue.remove();
				}
				processEvent(event);
			} catch(Exception e) {
				if(attempts == 5) {
					logger.error(String.format("Worker %s could not be restarted", this.id), e);
					return;
				}
				logger.error(String.format("Worker %s crashed, attempting to resolve, attemp %d", this.id, attempts), e);
				attempts++;
			}
		}
	}

	public synchronized void stop() {
		running = false;
	}

	public UUID getId() {
		return this.id;
	}
	
	private void processEvent(WebServerEvent event) {
		try {
			byte[] responseData = this.protocol.processEvent(event.getData());
			
			if(responseData == null || responseData.length == 0) {
				logger.error("Unable to process request, response is empty");
				return;
			}
			this.webServerProcess.send(event.getSocket(), responseData);
		} catch(Exception e) {
			logger.error("An error occurred processing request", e);
			return;
		}
	}
}
