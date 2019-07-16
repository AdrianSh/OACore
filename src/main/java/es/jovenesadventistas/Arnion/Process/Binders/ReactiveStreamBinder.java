package es.jovenesadventistas.Arnion.Process.Binders;

import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import es.jovenesadventistas.Arnion.Process.Binders.Transfers.Transfer;

public interface ReactiveStreamBinder<T extends Transfer, S extends Transfer> extends Subscriber<T>, Publisher<S>, Binder {
	public void close();
	public void submit(S i);
}
