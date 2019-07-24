package es.jovenesadventistas.Arnion.Process.Binders.Subscribers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

import es.jovenesadventistas.Arnion.Process.Binders.Transfers.Transfer;

public class SocketSubscriber<T extends Transfer> implements Subscriber<T> {
	private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger();

	private Subscription subscription;
	private ConcurrentLinkedQueue<T> data;
	private boolean subscribed;
	private boolean complete;
	private Socket s;
	private BufferedReader br;
	private InputStreamReader isr;

	public SocketSubscriber(Socket s) throws IOException {
		this.data = new ConcurrentLinkedQueue<T>();
		this.subscribed = false;
		this.s = s;
		this.isr = new InputStreamReader(s.getInputStream());
		this.br = new BufferedReader(isr);
	}

	@Override
	public void onSubscribe(Subscription subscription) {
		this.subscription = subscription;
		this.subscribed = true;
		// this.subscription.request(1);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onNext(T item) {
		logger.info("Got {} but ignored as it its a socket subscriber.", item);
		try {
			this.data.add((T) item.parse(br.readLine()));
		} catch (IOException e) {
			logger.error("An error ocurred when reading a line from the connection.", e);
		}
		// subscription.request(1);
	}

	public void request(Long n) throws IOException {
		if (!this.complete)
			throw new IOException("Subscription not completed.");
		if (!this.subscribed)
			throw new IOException("Subscription not yet started.");
		this.subscription.request(n);
	}

	public void requestOne() throws IOException {
		this.request(1L);
	}

	public T getData() {
		return this.data.poll();
	}
	
	public ConcurrentLinkedQueue<T> getAllData() {
		return this.data;
	}
	
	public boolean isSubscribed() {
		return this.subscribed;
	}

	@Override
	public void onError(Throwable throwable) {
		logger.error("An error ocurred in the exit-code subscriber", throwable);
		this.subscribed = false;
	}

	@Override
	public void onComplete() {
		// this.data.clear();
		this.subscribed = false;
		this.complete = true;
		
		try {
			this.br.close();
			this.s.close();
		} catch (IOException e) {
			logger.error("An error ocurred when closing BufferedReader and Socket.", e);
		}
		logger.debug("No more data from the subscription: {}", this.subscription);
	}
}
