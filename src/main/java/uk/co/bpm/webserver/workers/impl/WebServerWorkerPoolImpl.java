package uk.co.bpm.webserver.workers.impl;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import uk.co.bpm.webserver.WebServerException;
import uk.co.bpm.webserver.WebServerProcess;
import uk.co.bpm.webserver.logging.Logger;
import uk.co.bpm.webserver.protocol.Protocol;
import uk.co.bpm.webserver.workers.WebServerWorkerPool;

public class WebServerWorkerPoolImpl implements WebServerWorkerPool {

	private final Logger logger;
	
	private final Queue<WebServerEvent> queue = new LinkedList<WebServerEvent>();
	
	private final List<WebServerWorker> workers = new ArrayList<WebServerWorker>();
	
	private final Protocol protocol;
	
	private boolean running = false;
	
	public WebServerWorkerPoolImpl(Protocol protocol, Logger logger) {
		this.logger = logger;
		this.protocol = protocol;
	}
	
	@Override
	public void startWorkers(WebServerProcess process, int workerCount) throws WebServerException {
		logger.out("Starting Worker Pool");
		
		for(int i = 0; i < workerCount; i++) {
			try {
				WebServerWorker worker = new WebServerWorker(process, this.queue, this.protocol, this.logger);
				workers.add(worker);
				
				new Thread(worker).start();
			} catch(Exception e) {
				throw new WebServerException("Failed to start worker processes", e);
			}
		}
		running = true;
		logger.out("Started Worker Pool");
	}

	@Override
	public synchronized void stopWorkers() {
		logger.out("Stopping Worker Pool");
		
		for(WebServerWorker worker : workers) {
			worker.stop();
		}
		running = false;
		logger.out("Stopped Worker Pool");
	}

	@Override
	public void handOffToWorker(SocketChannel socket, byte[] data, int size) throws WebServerException {
		if(!running) {
			throw new WebServerException("Failed to hand off data as workers are not running");
		}
		
		synchronized(queue) {
			queue.add(new WebServerEvent(socket, data, size));
			queue.notify();
		}
	}
	
	@Override
	public boolean isRunning() {
		return this.running;
	}
}
