package es.jovenesadventistas.arnion.process.binders;

import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.atomic.AtomicBoolean;

import es.jovenesadventistas.arnion.process.AProcess;
import es.jovenesadventistas.arnion.process.binders.publishers.APublisher;
import es.jovenesadventistas.arnion.process.binders.subscribers.ASubscriber;
import es.jovenesadventistas.arnion.process.binders.transfers.Transfer;

/**
 * This binder uses an Implementation of a Subscriber<T> and Publisher<S> of
 * different types of deliverables.
 * 
 * @author Adrian E. Sanchez Hurtado
 *
 */
public abstract class SplitBinder<T extends Transfer, S extends Transfer> implements ReactiveStreamBinder<T, S> {
	private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger();
	protected ASubscriber<T> subscriber;
	protected SubmissionPublisher<S> publisher;
	protected AtomicBoolean ready;
	protected AtomicBoolean join;
	protected AProcess associatedProcess;

	public SplitBinder(ASubscriber<T> inputSubscriber, SubmissionPublisher<S> outputPublisher, AProcess associatedProcess) {
		this.subscriber = inputSubscriber;
		this.publisher = outputPublisher;
		this.ready = new AtomicBoolean(false);
		this.join = new AtomicBoolean(false);
		this.associatedProcess = associatedProcess;
	}

	@Override
	public void onSubscribe(Subscription subscription) {
		if (subscription instanceof APublisher)
			this.subscriber.onSubscribe(subscription);
		else {
			logger.error("Cannot subscribe using an unknown subscription. It should implements APublisher.");
		}
	}

	/**
	 * this is called whenever the Publisher publishes a new message
	 */
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
	public void submit(S i) {
		this.publisher.submit(i);
	}

	@Override
	public void close() {
		this.publisher.close();
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

	public ASubscriber<T> getSubscriber() {
		return subscriber;
	}

	public void setSubscriber(ASubscriber<T> subscriber) {
		this.subscriber = subscriber;
	}

	public SubmissionPublisher<S> getPublisher() {
		return publisher;
	}

	public APublisher getAPublisher() throws Exception {
		if (this.publisher instanceof APublisher)
			return (APublisher) publisher;
		else if (this.publisher == null)
			return null;
		else
			throw new Exception("It is not an instance of APublisher.");
	}

	public void setPublisher(SubmissionPublisher<S> publisher) {
		this.publisher = publisher;
	}

	public AtomicBoolean getReady() {
		return ready;
	}

	public void setReady(AtomicBoolean ready) {
		this.ready = ready;
	}

	public AtomicBoolean getJoin() {
		return join;
	}

	public void setJoin(AtomicBoolean join) {
		this.join = join;
	}
	
	@Override
	public void setAProcess(AProcess proc) {
		this.associatedProcess = proc;
	}

	@Override
	public AProcess getAProcess() {
		return associatedProcess;
	}
}
