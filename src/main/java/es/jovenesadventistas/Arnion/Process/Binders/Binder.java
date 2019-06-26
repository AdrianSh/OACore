package es.jovenesadventistas.Arnion.Process.Binders;

import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;

import es.jovenesadventistas.Arnion.Process.Binders.Transfers.Transfer;

public interface Binder<T extends Transfer, S extends Transfer> extends Subscriber<T>, Publisher<S> {
	public boolean ready();
}
