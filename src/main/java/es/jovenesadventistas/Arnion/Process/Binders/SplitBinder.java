package es.jovenesadventistas.Arnion.Process.Binders;

import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.atomic.AtomicBoolean;

import es.jovenesadventistas.Arnion.Process.Binders.Transfers.Transfer;

/**
 * This binder uses an Implementation of a Subscriber<T> and Publisher<S> of different types of deliverables.
 * @author Adrian E. Sanchez Hurtado
 *
 */
public class SplitBinder<T extends Transfer, S extends Transfer> implements Binder<T, S> {
	private Subscriber<T> subscriber;
	private Publisher<S> publisher;
	private AtomicBoolean ready;
	
	public SplitBinder(Subscriber<T> inputSubscriber, Publisher<S> outputPublisher) {
		this.subscriber = inputSubscriber;
		this.publisher = outputPublisher;
		this.ready = new AtomicBoolean(false);
	}
	
	@Override
	public void onSubscribe(Subscription subscription) {
		this.subscriber.onSubscribe(subscription);
	}

	@Override
	public void onNext(T item) {
		this.subscriber.onNext(item);
	}

	@Override
	public void onError(Throwable throwable) {
		this.subscriber.onError(throwable);
	}

	@Override
	public void onComplete() {
		this.subscriber.onComplete();
	}

	@Override
	public void subscribe(Subscriber<? super S> subscriber) {
		this.ready.set(true);
		this.publisher.subscribe(subscriber);
	}

	@Override
	public boolean ready() {
		return this.ready.get();
	}
}
