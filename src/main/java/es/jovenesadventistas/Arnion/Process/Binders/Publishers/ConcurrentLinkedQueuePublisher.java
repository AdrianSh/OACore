package es.jovenesadventistas.Arnion.Process.Binders.Publishers;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

import es.jovenesadventistas.Arnion.Process.Binders.Transfers.Transfer;

public class ConcurrentLinkedQueuePublisher<T extends Transfer> extends SubmissionPublisher<T> implements Subscription {
	private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger();
	private ConcurrentLinkedQueue<Subscriber<? super T>> subscribers;
	
	public ConcurrentLinkedQueuePublisher() {
		this.subscribers = new ConcurrentLinkedQueue<Subscriber<? super T>>();
	}

	public void subscribe(Subscriber<? super T> subscriber) {
		subscriber.onSubscribe(this);
		this.subscribers.add(subscriber);
	}
	
	public int submit(T data) {
		this.subscribers.forEach(c -> {
			c.onNext(data);
		});
		return 1;
	}
	
	public void close() {
		this.subscribers.forEach(c -> {
			c.onComplete();
		});
		this.subscribers.clear();
	}

	@Override
	public void request(long n) {
		logger.debug("{} requested.", n);
	}

	@Override
	public void cancel() {
		logger.debug("Cancel request.");
	}

}
