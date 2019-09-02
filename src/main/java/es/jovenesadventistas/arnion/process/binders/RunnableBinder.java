package es.jovenesadventistas.arnion.process.binders;

import java.util.function.Function;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import es.jovenesadventistas.arnion.process.AProcess;

public class RunnableBinder implements Binder {
	@Id
	private ObjectId id = new ObjectId();
	private AProcess associatedProcess;
	private Runnable runnable;
	private Function<Void, Void> onFinishFunc;

	public RunnableBinder(Runnable runnable, AProcess associatedProcess) {
		this.runnable = runnable;
		this.onFinishFunc = null;
		this.associatedProcess = associatedProcess;
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
	public ObjectId getId() {
		return this.id;
	}
	
	@Override
	public void setId(ObjectId id) {
		if (id != null)
			this.id = id;
	}

	public Runnable getRunnable() {
		return runnable;
	}

	public void setRunnable(Runnable runnable) {
		this.runnable = runnable;
	}

	@Override
	public void setAProcess(AProcess proc) {
		this.associatedProcess = proc;
	}

	@Override
	public AProcess getAProcess() {
		return associatedProcess;
	}
}
