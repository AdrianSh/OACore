package es.jovenesadventistas.arnion.process.binders;

import java.util.concurrent.Flow.Publisher;

import es.jovenesadventistas.arnion.process.binders.subscribers.ASubscriber;
import es.jovenesadventistas.arnion.process.binders.transfers.Transfer;

public interface ReactiveStreamBinder<T extends Transfer, S extends Transfer> extends ASubscriber<T>, Publisher<S>, Binder {
	public void close();
	public void submit(S i);
}
