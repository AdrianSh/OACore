package es.jovenesadventistas.Arnion.Process.Binders;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import es.jovenesadventistas.Arnion.Process.Binders.Publishers.ConcurrentLinkedQueuePublisher;
import es.jovenesadventistas.Arnion.Process.Binders.Subscribers.ConcurrentLinkedQueueSubscriber;
import es.jovenesadventistas.Arnion.Process.Binders.Transfers.StreamTransfer;
import es.jovenesadventistas.Arnion.ProcessExecutor.ProcessExecution.ProcessExecutionDetails;

public class StdInBinder extends SplitBinder<StreamTransfer, StreamTransfer> {
	private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger();

	private ProcessExecutionDetails procExecDetails;
	private CompletableFuture<Boolean> futureReady;

	public StdInBinder(ProcessExecutionDetails procExecDetails,
			ConcurrentLinkedQueueSubscriber<StreamTransfer> inputSubscriber,
			ConcurrentLinkedQueuePublisher<StreamTransfer> outputPublisher) {
		super(inputSubscriber, outputPublisher);
		this.procExecDetails = procExecDetails;
		this.futureReady = new CompletableFuture<Boolean>();
	}

	@Override
	public void onNext(StreamTransfer item) {
		super.onNext(item);
		this.ready.set(true);
		this.futureReady.complete(true);
		
		try {
			item.getData().getKey().transferTo(this.procExecDetails.getSystemProcess().get().getOutputStream());
			this.close();
		} catch (IOException | InterruptedException | ExecutionException e) {
			logger.error("Error while binding inputStream to the outputStream {} binder will stay open.", e);
		}
	}

	@Override
	public Future<Boolean> asynchReady() {
		return this.futureReady;
	}

	@Override
	public void run() {
		logger.debug("StdIn binder running, {}", this.procExecDetails);
		try {
			this.processInput();
			this.processOutput();
		} catch (InterruptedException | ExecutionException | IOException e) {
			logger.error("Error while binding stdin streams.", e);
		}
	}

	@Override
	public void processInput() throws IOException {
		// Process input for the already running process
		logger.debug("Processing input...");
		ConcurrentLinkedQueueSubscriber<StreamTransfer> subscriber = (ConcurrentLinkedQueueSubscriber<StreamTransfer>) this.subscriber;
		if (subscriber.isSubscribed()) {
			subscriber.requestOne();
		}
	}

	@Override
	public void processOutput() throws InterruptedException, ExecutionException {
		// Process output for this running process
		logger.debug("Processing output...");
		this.publisher.submit(new StreamTransfer(this.procExecDetails.getSystemProcess().get().getInputStream(), this.procExecDetails.getSystemProcess().get().getErrorStream()));
	}

	@Override
	public String toString() {
		return "StdInBinder [procExecDetails=" + procExecDetails + ", futureReady=" + futureReady + "]";
	}
}
