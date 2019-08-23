package es.jovenesadventistas.arnion.process.binders.Publishers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import es.jovenesadventistas.arnion.process.binders.Subscribers.ASubscriber;
import es.jovenesadventistas.arnion.process.binders.Transfers.StringTransfer;

public class SocketListenerPublisher implements Publisher<StringTransfer>, APublisher, Runnable {
	private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger();
	private ConcurrentLinkedQueue<ASubscriber<? super StringTransfer>> subscribers;
	private Socket socket;
	private InputStream in;
	@Id
	private ObjectId id = new ObjectId();
	
	public SocketListenerPublisher(Socket socket) throws IOException {
		this.socket = socket;
		this.in = socket.getInputStream();
		this.subscribers = new ConcurrentLinkedQueue<ASubscriber<? super StringTransfer>>();
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
		if(subscriber instanceof ASubscriber) {
			this.subscribers.add((ASubscriber<? super StringTransfer>) subscriber);
		} else {
			logger.error("Cannot subscribe using an unknown subscriber type.");
		}
	}

	@Override
	public String toString() {
		return "SocketListenerPublisher [socket=" + socket + ", in=" + in + "]";
	}

	@Override
	public ObjectId getId() {
		return this.id;
	}
	
	@Override
	public void setId(ObjectId id) {
		if (id != null)
			this.id = id;		
	}
}
