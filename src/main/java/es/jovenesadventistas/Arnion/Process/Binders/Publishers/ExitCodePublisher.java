package es.jovenesadventistas.Arnion.Process.Binders.Publishers;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;

import es.jovenesadventistas.Arnion.Process.Binders.Transfers.IntegerTransfer;

public class ExitCodePublisher implements Publisher<IntegerTransfer> {
	private ConcurrentLinkedQueue<Subscriber<? super IntegerTransfer>> subscribers;
	
	public ExitCodePublisher() {
		this.subscribers = new ConcurrentLinkedQueue<Subscriber<? super IntegerTransfer>>();
	}

	@Override
	public void subscribe(Subscriber<? super IntegerTransfer> subscriber) {
		this.subscribers.add(subscriber);
	}
	
	public void submit(IntegerTransfer data) {
		this.subscribers.forEach(c -> {
			c.onNext(data);
		});
	}
	
	public void close() {
		this.subscribers.forEach(c -> {
			c.onComplete();
		});
		this.subscribers.clear();
	}

}
