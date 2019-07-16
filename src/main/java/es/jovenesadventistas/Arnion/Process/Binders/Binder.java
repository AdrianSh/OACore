package es.jovenesadventistas.Arnion.Process.Binders;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

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
}
