package es.jovenesadventistas.Arnion.Process.Binders;

import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import es.jovenesadventistas.Arnion.Process.Binders.Transfers.Transfer;

public interface Binder<T extends Transfer, S extends Transfer> extends Subscriber<T>, Publisher<S>, Runnable {
	public boolean ready();
	public boolean joined();
	public void markAsReady();
	public void submit(S i);
	public void close();
	default public Future<Boolean> asynchReady(){
		CompletableFuture<Boolean> r = new CompletableFuture<Boolean>();
		r.complete(this.ready());
		return r;
	}
}
