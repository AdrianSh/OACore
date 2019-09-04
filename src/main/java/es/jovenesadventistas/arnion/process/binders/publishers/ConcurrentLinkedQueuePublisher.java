package es.jovenesadventistas.arnion.process.binders.publishers;

import java.util.concurrent.ConcurrentLinkedQueue;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import es.jovenesadventistas.arnion.process.binders.subscribers.ASubscriber;
import es.jovenesadventistas.arnion.process.binders.transfers.Transfer;

public class ConcurrentLinkedQueuePublisher<T extends Transfer> extends ASubmissionPublisher<T> {
	private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger();
	private ConcurrentLinkedQueue<ASubscriber<? super T>> subscribers;
	@Id
	private ObjectId id = new ObjectId();
	
	public ConcurrentLinkedQueuePublisher() {
		this.subscribers = new ConcurrentLinkedQueue<ASubscriber<? super T>>();
	}

	public void subscribe(ASubscriber<? super T> subscriber) {
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
