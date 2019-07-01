package es.jovenesadventistas.Arnion.Process.Binders;

import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.Flow.Subscriber;

import es.jovenesadventistas.Arnion.Process.Binders.Transfers.IntegerTransfer;
import es.jovenesadventistas.Arnion.Process.Binders.Transfers.Transfer;
import es.jovenesadventistas.Arnion.ProcessExecutor.ProcessExecution.ProcessExecutionDetails;

public class ExitCodeBinder<T extends Transfer, S extends Transfer> extends SplitBinder<T, S> {
	private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger();
	
	private ProcessExecutionDetails<T, S> procExecDetails;
	private CompletableFuture<Boolean> futureReady;
	
	public ExitCodeBinder(ProcessExecutionDetails<T, S> procExecDetails, Subscriber<T> inputSubscriber, SubmissionPublisher<S> outputPublisher) {
		super(inputSubscriber, outputPublisher);
		this.procExecDetails = procExecDetails;
		this.futureReady = new CompletableFuture<Boolean>();
	} 
	
	@Override
	public void onNext(T item) {
		super.onNext(item);
		this.ready.set(true);
		this.futureReady.complete(true);
	}

	@Override
	public Future<Boolean> asynchReady() {
		return this.futureReady;
	}
	
	@Override
	public void run() {
		logger.debug("Exit code binder running {}", this.procExecDetails);
		try {
			int exitCode = this.procExecDetails.getExitCode().get().getExitCode();
			logger.debug("Exit code {} of {}", exitCode, this.procExecDetails);
			this.publisher.submit((S) new IntegerTransfer());
		} catch (InterruptedException | ExecutionException e) {
			logger.error("Error while binding exit code.", e);
		}
	}
}
