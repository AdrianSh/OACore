package es.jovenesadventistas.Arnion.Process.Binders;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import es.jovenesadventistas.Arnion.Process.Binders.Publishers.ConcurrentLinkedQueuePublisher;
import es.jovenesadventistas.Arnion.Process.Binders.Subscribers.ConcurrentLinkedQueueSubscriber;
import es.jovenesadventistas.Arnion.Process.Binders.Transfers.IntegerTransfer;
import es.jovenesadventistas.Arnion.ProcessExecutor.ProcessExecution.ProcessExecutionDetails;

public class ExitCodeBinder extends SplitBinder<IntegerTransfer, IntegerTransfer> {
	private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger();

	private ProcessExecutionDetails<IntegerTransfer, IntegerTransfer> procExecDetails;
	private CompletableFuture<Boolean> futureReady;

	public ExitCodeBinder(ProcessExecutionDetails<IntegerTransfer, IntegerTransfer> procExecDetails,
			ConcurrentLinkedQueueSubscriber<IntegerTransfer> inputSubscriber,
			ConcurrentLinkedQueuePublisher<IntegerTransfer> outputPublisher) {
		super(inputSubscriber, outputPublisher);
		this.procExecDetails = procExecDetails;
		this.futureReady = new CompletableFuture<Boolean>();
	}

	@Override
	public void onNext(IntegerTransfer item) {
		super.onNext(item);
		this.ready.set(true);
		this.futureReady.complete(true);
		this.close();
	}

	@Override
	public Future<Boolean> asynchReady() {
		return this.futureReady;
	}

	@Override
	public void run() {
		logger.debug("Exit code binder running {}", this.procExecDetails);
		try {
			logger.debug("Processing input...");
			ConcurrentLinkedQueueSubscriber<IntegerTransfer> subscriber = (ConcurrentLinkedQueueSubscriber<IntegerTransfer>) this.subscriber;
			if (subscriber.isSubscribed()) {
				subscriber.requestOne();
				logger.debug("Exit code {} from subscription in {}", subscriber.getData().getData(),
						this.procExecDetails);
			}

			logger.debug("Processing output...");
			int exitCode = this.procExecDetails.getExitCode().get().getExitCode();
			logger.debug("Exit code {} of {}", exitCode, this.procExecDetails);
			this.publisher.submit(new IntegerTransfer(exitCode));
		} catch (InterruptedException | ExecutionException | IOException e) {
			logger.error("Error while binding exit codes.", e);
		}
	}
}
