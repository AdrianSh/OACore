package es.jovenesadventistas.Arnion.Process.Binders.Publishers;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.Flow.Subscriber;

import es.jovenesadventistas.Arnion.Process.Binders.Transfers.Transfer;

public class ConcurrentLinkedQueuePublisher<T extends Transfer> extends SubmissionPublisher<T> {
	private ConcurrentLinkedQueue<Subscriber<? super T>> subscribers;
	
	public ConcurrentLinkedQueuePublisher() {
		this.subscribers = new ConcurrentLinkedQueue<Subscriber<? super T>>();
	}

	public void subscribe(Subscriber<? super T> subscriber) {
		this.subscribers.add(subscriber);
	}
	
	public int submit(T data) {
		this.subscribers.forEach(c -> {
			c.onNext(data);
		});
		return 0;
	}
	
	public void close() {
		this.subscribers.forEach(c -> {
			c.onComplete();
		});
		this.subscribers.clear();
	}

}
