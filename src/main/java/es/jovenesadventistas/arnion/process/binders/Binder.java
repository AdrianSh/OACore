package es.jovenesadventistas.arnion.process.binders;

import java.util.ArrayList;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Function;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;


@Document
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
	 * 
	 * @param <T>
	 * @param <R>
	 * @param f   Function
	 */
	public void onFinish(Function<Void, Void> f);

	public static ArrayList<Class<? extends Binder>> binders() {
		ArrayList<Class<? extends Binder>> r = new ArrayList<>();
		r.add(DirectStdInBinder.class);
		r.add(ExitCodeBinder.class);
		r.add(RunnableBinder.class);
		r.add(SplitBinder.class);
		r.add(StdInBinder.class);
		r.add(StdOutBinder.class);
		return r;
	}
	
	public ObjectId getId();
	
	public void setId(ObjectId id);
}
