package uk.co.bpm.webserver.workers;

import java.nio.channels.SocketChannel;

import uk.co.bpm.webserver.WebServerException;
import uk.co.bpm.webserver.WebServerProcess;
import uk.co.bpm.webserver.impl.WebServerProcessImpl;

public interface WebServerWorkerPool {

	void startWorkers(WebServerProcess process, int workerCount) throws WebServerException;
	
	void stopWorkers();
	
	void handOffToWorker(SocketChannel socket, byte[] data, int size) throws WebServerException;
	
	boolean isRunning();
	
}
