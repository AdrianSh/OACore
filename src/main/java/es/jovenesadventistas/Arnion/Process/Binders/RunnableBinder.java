package es.jovenesadventistas.Arnion.Process.Binders;

import java.util.function.Function;

public class RunnableBinder implements Binder {

	private Runnable runnable;
	private Function<Void, Void> onFinishFunc;

	public RunnableBinder(Runnable runnable) {
		this.runnable = runnable;
		this.onFinishFunc = null;
	}

	@Override
	public void run() {
		this.runnable.run();
		this.onFinishFunc.apply(null);
	}

	@Override
	public boolean ready() {
		return true;
	}

	@Override
	public boolean joined() {
		return true;
	}

	@Override
	public void markAsReady() {
	}

	@Override
	public void processInput() throws Exception {
	}

	@Override
	public void processOutput() throws Exception {
	}

	@Override
	public void onFinish(Function<Void, Void> f) {
		this.onFinishFunc = f;
	}

	@Override
	public String toString() {
		return "RunnableBinder [runnable=" + runnable + "]";
	}

}
