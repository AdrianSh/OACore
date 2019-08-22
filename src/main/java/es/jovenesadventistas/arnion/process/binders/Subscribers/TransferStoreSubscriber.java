package es.jovenesadventistas.arnion.process.binders.Subscribers;

import java.io.IOException;
import java.util.concurrent.Flow.Subscription;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import es.jovenesadventistas.arnion.process.binders.Publishers.APublisher;
import es.jovenesadventistas.arnion.process.binders.Transfers.Transfer;
import es.jovenesadventistas.arnion.process.persistence.TransferStore;

public class TransferStoreSubscriber<T extends Transfer> implements ASubscriber<T> {
	private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger();
	@Id
	private ObjectId id = new ObjectId();
	private APublisher subscription;
	private TransferStore<T> store;
	
	public TransferStoreSubscriber() {
		this.store = new TransferStore<T>();
	}

	public void request(Long n) throws IOException {
		if (this.subscription != null)
			this.subscription.request(n);
	}

	public void requestOne() throws IOException {
		this.request(1L);
	}

	@Override
	public void onSubscribe(Subscription subscription) {
		if(subscription instanceof APublisher) {
			this.subscription = (APublisher) subscription;
		} else {
			logger.error("Cannot onSubscribe using an unknown subscription. It should implements APublisher.");
		}
	}

	@Override
	public void onNext(T item) {
		this.store.add(item);
	}

	@Override
	public void onError(Throwable throwable) {

	}

	@Override
	public void onComplete() {
		
	}

	public TransferStore<T> getStore() {
		return this.store;
	}

	@Override
	public ObjectId getId() {
		return this.id;
	}
}
