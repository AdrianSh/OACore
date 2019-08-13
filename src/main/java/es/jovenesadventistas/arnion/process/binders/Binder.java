package es.jovenesadventistas.arnion.process.binders;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Function;

public interface Binder extends Runnable {
	public boolean ready();

	public boolean joined();

	public void markAsReady();

	default public Future<Boolean> asynchReady() {
		CompletableFuture<Boolean> r = new CompletableFuture<Boolean>();
		r.complete(this.ready());
		return r;
	}

	/**
	 * Process input for the already running process
	 * 
	 * @throws Exception
	 */
	abstract void processInput() throws Exception;

	/**
	 * Process output for the already running process
	 * 
	 * @throws Exception
	 */
	abstract void processOutput() throws Exception;
	
	/**
	 * If something needs to be executed when the binder has finished
	 * @param <T>
	 * @param <R>
	 * @param f Function
	 */
	public void onFinish(Function<Void, Void> f);
}
