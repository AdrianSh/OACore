package es.jovenesadventistas.arnion.process.binders;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import es.jovenesadventistas.arnion.process_executor.ProcessExecution.ProcessExecutionDetails;
import es.jovenesadventistas.arnion.process.binders.Transfers.StringTransfer;
import es.jovenesadventistas.arnion.process.persistence.TransferStore;

public class StdOutBinder implements Binder, Subscriber<StringTransfer> {
	private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger();

	private TransferStore<StringTransfer> transfStore;
	private ProcessExecutionDetails procExecDetails;
	private CompletableFuture<Boolean> futureReady;
	private AtomicBoolean transferingStore;
	private CompletableFuture<Boolean> transferingStoreFlag;
	private OutputStream out;
	// private InputStream in;
	private Function<Void, Void> onFinishFunc;
	private Subscription subscription;

	public StdOutBinder(ProcessExecutionDetails procExecDetails) {
		this.procExecDetails = procExecDetails;
		this.futureReady = new CompletableFuture<Boolean>();
		this.transferingStore = new AtomicBoolean(false);
		this.transferingStoreFlag = new CompletableFuture<Boolean>();
		this.transfStore = new TransferStore<StringTransfer>();
		this.onFinishFunc = null;
		this.out = null;
		// this.in = null;
	}

	@Override
	public void processInput() throws Exception {
		Process proc = this.procExecDetails.getSystemProcess().get();
		this.out = proc.getOutputStream();
		// this.in = proc.getInputStream();
		transferStore();
	}

	private void transferStore() throws IOException {
		if (this.transfStore.size() > 0) {
			this.transferingStore.set(true);
			for (StringTransfer t : this.transfStore) {
				this.out.write(t.getData().getBytes());
			}
			this.transfStore.clear();
			this.transferingStoreFlag.complete(true);
		}
	}

	@Override
	public void processOutput() throws Exception {
		logger.info("Standard output stream set.");
	}

	@Override
	public void run() {
		try {
			processInput();
			processOutput();
		} catch (Exception e) {
			logger.error("An error ocurred while processing input/output on the StdOutBinder.", e);
		}
	}

	@Override
	public boolean ready() {
		try {
			return futureReady.get();
		} catch (InterruptedException | ExecutionException e) {
			logger.error("Error while getting ready status on StdInBinder.", e);
			return false;
		}
	}

	@Override
	public boolean joined() {
		try {
			return futureReady.get();
		} catch (InterruptedException | ExecutionException e) {
			logger.error("Error while getting ready status on StdInBinder.", e);
			return false;
		}
	}

	@Override
	public void markAsReady() {
		this.futureReady.complete(true);
	}

	@Override
	public void onSubscribe(Subscription subscription) {
		this.subscription = subscription;
		this.futureReady.complete(true);
	}

	@Override
	public void onNext(StringTransfer item) {
		if (this.out == null) {
			this.transfStore.add(item);
		} else {
			try {
				if (this.transferingStore.get())
					try {
						this.transferingStoreFlag.get();
					} catch (InterruptedException | ExecutionException e) {
						logger.error(
								"Couldn't wait for the transfering of the String Transfer Store into the standard output stream.",
								e);
					}
				this.out.write(item.getData().getBytes());
				this.out.flush();
				
				logger.debug("Writting: {} on: {} of: {}", item.getData(), this.out, this.procExecDetails);

			} catch (IOException e) {
				logger.error("Couldn't write String Transfer into the standard output stream.", e);
			}
		}
	}

	@Override
	public void onError(Throwable throwable) {
		logger.error("An error ocurred when receiving and standard output.", throwable);
	}

	@Override
	public void onComplete() {
		logger.debug("Complete.");
		if(this.onFinishFunc != null) this.onFinishFunc.apply(null);
		/*
		try {
			this.transfStore.clear();
			this.out.close();
		} catch (IOException e) {
			logger.error("Error while closing the standard output stream.", e);
		}
		*/
	}
	
	@Override
	public void onFinish(Function<Void, Void> f) {
		this.onFinishFunc = f;
	}

	@Override
	public String toString() {
		return "StdOutBinder [procExecDetails=" + procExecDetails + ", futureReady=" + futureReady + ", out=" + out + ", subscription=" + subscription + "]";
	}
}
