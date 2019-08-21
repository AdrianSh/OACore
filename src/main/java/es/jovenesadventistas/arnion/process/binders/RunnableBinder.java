package es.jovenesadventistas.arnion.process.binders;

import java.util.HashMap;
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

	@Override
	public String getForm() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Binder parseForm(HashMap<String, String> data) {
		// TODO Auto-generated method stub
		return null;
	}

}
