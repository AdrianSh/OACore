package es.jovenesadventistas.arnion.process.binders.Publishers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

import es.jovenesadventistas.arnion.process.binders.Transfers.StringTransfer;

public class SocketListenerPublisher implements Publisher<StringTransfer>, Subscription, Runnable {
	private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger();
	private ConcurrentLinkedQueue<Subscriber<? super StringTransfer>> subscribers;
	private Socket socket;
	private InputStream in;
	
	public SocketListenerPublisher(Socket socket) throws IOException {
		this.socket = socket;
		this.in = socket.getInputStream();
		this.subscribers = new ConcurrentLinkedQueue<Subscriber<? super StringTransfer>>();
	}
	
	public Socket getSocket() {
		return this.socket;
	}
	
	public void close() throws IOException {
		this.socket.close();
	}
	
	@Override
	public void run() {
		try {
			if (in != null) {
				final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				String line = null;
				while ((line = reader.readLine()) != null) {
					final String t = line;
					logger.debug("Line: {}, received from the Socket", line);
					this.subscribers.forEach(c -> {
						c.onNext(new StringTransfer(t));
					});
				}
				reader.close();
			} else {
				logger.info("Cannot read from a null InputStream {}.", in);
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
		
		// socket.close();
	}
	
	@Override
	public void request(long n) {
		logger.debug("Cannot request {} to a Socket.", n);
	}

	@Override
	public void cancel() {
		logger.warn("Cancel means complete each subscriber.");
		this.subscribers.forEach(c -> {
			c.onComplete();
		});
	}

	@Override
	public void subscribe(Subscriber<? super StringTransfer> subscriber) {
		subscriber.onSubscribe(this);
		this.subscribers.add(subscriber);
	}

	@Override
	public String toString() {
		return "SocketListenerPublisher [socket=" + socket + ", in=" + in + "]";
	}
}
