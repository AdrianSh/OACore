package es.jovenesadventistas.arnion.process.binders.Subscribers;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

import es.jovenesadventistas.arnion.process.binders.Transfers.Transfer;

public class ConcurrentLinkedQueueSubscriber<T extends Transfer> implements Subscriber<T> {
	private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger();

	private Subscription subscription;
	private ConcurrentLinkedQueue<T> data;
	private boolean subscribed;
	private boolean complete;

	public ConcurrentLinkedQueueSubscriber() {
		this.data = new ConcurrentLinkedQueue<T>();
		this.subscribed = false;
	}

	@Override
	public void onSubscribe(Subscription subscription) {
		this.subscription = subscription;
		this.subscribed = true;
		// this.subscription.request(1);
	}

	@Override
	public void onNext(T item) {
		logger.info("Got {}", item);
		this.data.add(item);
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
		logger.debug("No more data from the subscription: {}", this.subscription);
	}
}
