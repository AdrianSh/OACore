package es.jovenesadventistas.arnion.process.binders.Subscribers;

import java.io.IOException;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

import es.jovenesadventistas.arnion.process.binders.Transfers.Transfer;
import es.jovenesadventistas.arnion.process.persistence.TransferStore;

public class TransferStoreSubscriber<T extends Transfer> implements Subscriber<T> {
	private Subscription subscription;
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
		this.subscription = subscription;
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


}