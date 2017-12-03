package uk.co.bpm.webserver.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import uk.co.bpm.webserver.WebServerException;
import uk.co.bpm.webserver.WebServerProcess;
import uk.co.bpm.webserver.logging.Logger;
import uk.co.bpm.webserver.workers.WebServerWorkerPool;

public class WebServerProcessImpl implements WebServerProcess {
	
	private final InetAddress hostAddress;
	
	private final int port;
	
	private final Logger logger;
	
	private final Selector selector;
	
	private final ServerSocketChannel serverChannel;
	
	private final ByteBuffer readBuffer;
	
	private final WebServerWorkerPool workerPool;
	
	private final Queue<WebServerChangeRequest> changeRequests = new LinkedList<WebServerChangeRequest>();
	
	private final Map<SocketChannel, Queue<ByteBuffer>> pendingData = new HashMap<SocketChannel, Queue<ByteBuffer>>();
	
	private boolean running = true;
	
	public WebServerProcessImpl(WebServerWorkerPool workerPool, int port, Logger logger) throws WebServerException {
		this(workerPool, null, port, logger, -1);
	}
	
	public WebServerProcessImpl(WebServerWorkerPool workerPool, InetAddress hostAddress, int port, Logger logger) throws WebServerException {
		this(workerPool, hostAddress, port, logger, -1);
	}
	
	public WebServerProcessImpl(WebServerWorkerPool workerPool, InetAddress hostAddress, int port, Logger logger, int readBufferSize) throws WebServerException {
		this.hostAddress = hostAddress;
		this.port = port;
		this.logger = logger;
		this.workerPool = workerPool;
		
		if(readBufferSize <= 0) {
			readBufferSize = 8192;
		}
		this.readBuffer = ByteBuffer.allocate(readBufferSize);
		
		try {
			this.serverChannel = ServerSocketChannel.open();
			this.selector = createSelector();
		} catch(IOException e) {
			throw new WebServerException("Error creating WebServer", e);
		}
	}

	@Override
	public void run() {
		logger.out("Starting server..");
		
		if(!this.workerPool.isRunning()) {
			logger.error("Could not start server, worker pool has not been started.");
			return;
		}
		
		while(running) {
			try {
				logger.trace("Waiting...");
				
				synchronized(this.changeRequests) {
					Iterator<WebServerChangeRequest> requests = this.changeRequests.iterator();
					
					while(requests.hasNext()) {
						WebServerChangeRequest change = requests.next();
						
						if(change.type == WebServerChangeRequest.CHANGEOPS) {
							SelectionKey key = change.socket.keyFor(this.selector);
							key.interestOps(change.ops);
						}
					}
					this.changeRequests.clear();
				}
				this.selector.select();
				logger.trace("Attempted Connection");
				Iterator<SelectionKey> selectedKeys = this.selector.selectedKeys().iterator();
				
				while(selectedKeys.hasNext()) {
					SelectionKey key = selectedKeys.next();
					selectedKeys.remove();
					
					if (!key.isValid()) {
						continue;
					}
					
					if(key.isAcceptable()) {
						this.accept(key);
					} else if(key.isReadable()) {
						this.read(key);
					} else if(key.isWritable()) {
						this.write(key);
					}
				}
			} catch(Exception e) {
				logger.error("An unexpected error has occurred.", e);
			}
		}
	}

	@Override
	public void send(SocketChannel socket, byte[] data) {
		logger.debug("Sending data...");
		
		synchronized(this.changeRequests) {
			
			this.changeRequests.add(new WebServerChangeRequest(socket, WebServerChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE));
			
			synchronized(this.pendingData) {
				Queue<ByteBuffer> dataQueue = this.pendingData.get(socket);
				
				if(dataQueue == null) {
					dataQueue = new LinkedList<ByteBuffer>();
					this.pendingData.put(socket, dataQueue);
				}
				dataQueue.add(ByteBuffer.wrap(data));
			}
		}
		this.selector.wakeup();
	}
	
	@Override
	public synchronized void stop() {
		running = false;
	}
	
	private void write(SelectionKey key) throws IOException {
		SocketChannel sc = (SocketChannel) key.channel();
		
		synchronized(this.pendingData) {
			Queue<ByteBuffer> dataQueue = this.pendingData.get(sc);
			
			while(!dataQueue.isEmpty()) {
				ByteBuffer buf = dataQueue.peek();
				sc.write(buf);
				
				if(buf.remaining() > 0) {
					break;
				}
				dataQueue.remove();
			}
			
			if(dataQueue.isEmpty()) {
				key.interestOps(SelectionKey.OP_READ);
			}
		}
	}
	
	private void accept(SelectionKey key) throws IOException {
		logger.debug("Accepting connection...");
		
		ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
		
		SocketChannel sc = ssc.accept();
		//Socket socket = socketChannel.socket();
		
		sc.configureBlocking(false);
		sc.register(this.selector, SelectionKey.OP_READ);
	}
	
	private void read(SelectionKey key) throws WebServerException {
		SocketChannel sc = (SocketChannel) key.channel();
		
		this.readBuffer.clear();
		//TODO: chunking packets of over buffer size!!
		int readCount;
		try {
			try {
				readCount = sc.read(this.readBuffer);
				logger.trace("Reading");
				
			} catch(IOException e) {
				key.cancel();
				sc.close();
				return;
			}
			
			if(readCount == -1) {
				key.channel().close();
				key.cancel();
				return;
			}
			this.workerPool.handOffToWorker(sc, this.readBuffer.array(), readCount);
			
		} catch(IOException e) {
			throw new WebServerException("An error has occurred processing OP_READ key", e);
		}
	}
	
	private Selector createSelector() throws IOException {
		Selector selector = SelectorProvider.provider().openSelector();
		
		this.serverChannel.configureBlocking(false);
		
		InetSocketAddress isa = null;
		
		if(this.hostAddress == null) {
			isa = new InetSocketAddress("localhost", this.port);
		} else {
			isa = new InetSocketAddress(this.hostAddress, this.port);
		}
		this.serverChannel.socket().bind(isa);
		this.serverChannel.register(selector, SelectionKey.OP_ACCEPT);
		return selector;
	}
}
