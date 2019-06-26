package es.jovenesadventistas.Arnion.Process.Binders.Subscribers;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

import es.jovenesadventistas.Arnion.Process.Binders.Transfers.IntegerTransfer;

public class ExitCodeSubscriber implements Subscriber<IntegerTransfer> {
	private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger();

	private Subscription subscription;
	private ConcurrentLinkedQueue<IntegerTransfer> data;
	private boolean open;
	private boolean complete;

	public ExitCodeSubscriber() {
		this.data = new ConcurrentLinkedQueue<IntegerTransfer>();
		this.open = false;
	}

	@Override
	public void onSubscribe(Subscription subscription) {
		this.subscription = subscription;
		this.open = true;
		// this.subscription.request(1);
	}

	@Override
	public void onNext(IntegerTransfer item) {
		logger.info("Got {}", item);
		this.data.add(item);
		// subscription.request(1);
	}

	public void request(Long n) throws IOException {
		if (!this.complete)
			throw new IOException("Subscription completed.");
		if (!this.open)
			throw new IOException("Subscription not yet started.");
		this.subscription.request(n);
	}

	public void requestOne() throws IOException {
		this.request(1L);
	}

	public IntegerTransfer getData() {
		return this.data.poll();
	}
	
	public ConcurrentLinkedQueue<IntegerTransfer> getAllData() {
		return this.data;
	}

	@Override
	public void onError(Throwable throwable) {
		logger.error("An error ocurred in the exit-code subscriber", throwable);
		this.open = false;
	}

	@Override
	public void onComplete() {
		// this.data.clear();
		this.open = false;
		this.complete = true;
		logger.debug("No more data from the subscription: {}", this.subscription);
	}
}
