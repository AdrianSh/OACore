package es.jovenesadventistas.Arnion.Process.Binders;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.SubmissionPublisher;

import es.jovenesadventistas.Arnion.Process.Binders.Transfers.StringCollectionTransfer;
import es.jovenesadventistas.Arnion.Process.Binders.Transfers.Transfer;
import es.jovenesadventistas.Arnion.ProcessExecutor.ProcessExecution.ProcessExecutionDetails;

public class StdInBinder extends SubmissionPublisher<StringCollectionTransfer> implements Binder {
	private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger();

	private ProcessExecutionDetails procExecDetails;
	private CompletableFuture<Boolean> futureReady;
	private Publisher<StringCollectionTransfer> publisher;
	private InputStream in;
	private InputStream inError;

	public StdInBinder(ProcessExecutionDetails procExecDetails, SubmissionPublisher<StringCollectionTransfer> publisher) {
		this.publisher = publisher;
		this.procExecDetails = procExecDetails;
		this.futureReady = new CompletableFuture<Boolean>();
	}

	@Override
	public void processInput() throws Exception {
		Process proc = this.procExecDetails.getSystemProcess().get();
		this.in = proc.getInputStream();
		this.inError = proc.getErrorStream();
	}

	@Override
	public void processOutput() throws Exception {
		/**
		 * LEER CADA LINEA DE INPUT Y SUBMITEAR
		 */
		while(in.available() > 0) {
			List<String> strs = new ArrayList<String>();
			StringCollectionTransfer t = new StringCollectionTransfer(strs);
			strs.add(in.)
			submit(t);
		}
	}

	@Override
	public void run() {
		try {
			processInput();
			processOutput();
		} catch (Exception e) {
			logger.error("An error ocurred while processing input/output on the StdInBinder.", e);
		}
	}

	@Override
	public void subscribe(Subscriber<? super StringCollectionTransfer> subscriber) {
		futureReady.complete(true);
		this.publisher.subscribe(subscriber);
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
}
