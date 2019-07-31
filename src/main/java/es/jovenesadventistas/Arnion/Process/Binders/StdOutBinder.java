package es.jovenesadventistas.Arnion.Process.Binders;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

import es.jovenesadventistas.Arnion.Process.Binders.Transfers.StringTransfer;
import es.jovenesadventistas.Arnion.Process.Persistence.TransferStore;
import es.jovenesadventistas.Arnion.ProcessExecutor.ProcessExecution.ProcessExecutionDetails;

public class StdOutBinder implements Binder, Subscriber<StringTransfer> {
	private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger();
	
	private TransferStore<StringTransfer> transferStore;
	private ProcessExecutionDetails procExecDetails;
	private CompletableFuture<Boolean> futureReady;
	private OutputStream out;
	
	@SuppressWarnings("unused")
	private Subscription subscription;

	public StdOutBinder(ProcessExecutionDetails procExecDetails) {
		this.procExecDetails = procExecDetails;
		this.futureReady = new CompletableFuture<Boolean>();
		this.transferStore = new TransferStore<StringTransfer>();
	}

	@Override
	public void processInput() throws Exception {
		Process proc = this.procExecDetails.getSystemProcess().get();
		this.out = proc.getOutputStream();
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
			this.transferStore.add(item);
		} else {
			try {
				for (StringTransfer t : this.transferStore) {
					this.out.write(t.toString().getBytes());
				}
				this.transferStore.clear();
				this.out.write(item.toString().getBytes());
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
		try {
			this.transferStore.clear();
			this.out.close();
		} catch (IOException e) {
			logger.error("Error while closing the standard output stream.", e);
		}
	}
}
