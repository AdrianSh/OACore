package es.jovenesadventistas.Arnion.Process.Binders.Subscribers;

import java.io.IOException;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

import org.springframework.beans.factory.annotation.Autowired;

import es.jovenesadventistas.Arnion.Persistence.TransferBatch;
import es.jovenesadventistas.Arnion.Persistence.TransferBatchService;
import es.jovenesadventistas.Arnion.Process.Binders.Transfers.Transfer;

public class TransferBatchSubscriber<T extends Transfer> implements Subscriber<T> {
	@Autowired
	private TransferBatchService transferBatchService;
	private TransferBatch tBatch;
	private Subscription subscription;

	public TransferBatchSubscriber() {
		this.tBatch = new TransferBatch();
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
		this.tBatch.addTransfer(item.toString());
	}

	@Override
	public void onError(Throwable throwable) {

	}

	@Override
	public void onComplete() {
		this.transferBatchService.save(this.tBatch);
	}

	public TransferBatch gettBatch() {
		return tBatch;
	}

	public void settBatch(TransferBatch tBatch) {
		this.tBatch = tBatch;
	}

}
