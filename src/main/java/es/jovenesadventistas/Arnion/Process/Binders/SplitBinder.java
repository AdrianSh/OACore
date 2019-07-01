package es.jovenesadventistas.Arnion.Process.Binders;

import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.atomic.AtomicBoolean;

import es.jovenesadventistas.Arnion.Process.Binders.Transfers.Transfer;

/**
 * This binder uses an Implementation of a Subscriber<T> and Publisher<S> of different types of deliverables.
 * @author Adrian E. Sanchez Hurtado
 *
 */
public abstract class SplitBinder<T extends Transfer, S extends Transfer> implements Binder<T, S> {
	protected Subscriber<T> subscriber;
	protected SubmissionPublisher<S> publisher;
	protected AtomicBoolean ready;
	protected AtomicBoolean join;
	
	public SplitBinder(Subscriber<T> inputSubscriber, SubmissionPublisher<S> outputPublisher) {
		this.subscriber = inputSubscriber;
		this.publisher = outputPublisher;
		this.ready = new AtomicBoolean(false);
		this.join = new AtomicBoolean(false);
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
		this.join.set(true);
		this.publisher.subscribe(subscriber);
	}

	@Override
	public boolean ready() {
		return this.ready.get();
	}

	@Override
	public void markAsReady() {
		this.ready.set(true);
	}

	@Override
	public boolean joined() {
		return this.join.get();
	}

	@Override
	public void submit(S i) {
		this.publisher.submit(i);
	}
}
