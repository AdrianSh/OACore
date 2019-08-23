package es.jovenesadventistas.arnion.process.binders;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import es.jovenesadventistas.arnion.process_executor.ProcessExecution.ProcessExecutionDetails;
import es.jovenesadventistas.arnion.process.binders.Publishers.ConcurrentLinkedQueuePublisher;
import es.jovenesadventistas.arnion.process.binders.Subscribers.ConcurrentLinkedQueueSubscriber;
import es.jovenesadventistas.arnion.process.binders.Transfers.IntegerTransfer;

public class ExitCodeBinder extends SplitBinder<IntegerTransfer, IntegerTransfer> {
	private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger();
	@Id
	private ObjectId id = new ObjectId();
	private ProcessExecutionDetails procExecDetails;
	private CompletableFuture<Boolean> futureReady;
	private Function<Void, Void> onFinishFunc;

	public ExitCodeBinder(ProcessExecutionDetails procExecDetails,
			ConcurrentLinkedQueueSubscriber<IntegerTransfer> inputSubscriber,
			ConcurrentLinkedQueuePublisher<IntegerTransfer> outputPublisher) {
		super(inputSubscriber, outputPublisher);
		this.procExecDetails = procExecDetails;
		this.futureReady = new CompletableFuture<Boolean>();
		this.onFinishFunc = null;
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
			this.processInput();
			this.processOutput();
		} catch (InterruptedException | ExecutionException | IOException e) {
			logger.error("Error while binding exit codes.", e);
		}
		this.onFinishFunc.apply(null);
	}

	@Override
	public void processInput() throws IOException {
		// Process input for the already running process
		logger.debug("Processing input...");
		ConcurrentLinkedQueueSubscriber<IntegerTransfer> subscriber = (ConcurrentLinkedQueueSubscriber<IntegerTransfer>) this.subscriber;
		if (subscriber.isSubscribed()) {
			subscriber.requestOne();
			logger.debug("Exit code {} from subscription in {}", subscriber.getData().getData(), this.procExecDetails);
		}
	}

	@Override
	public void processOutput() throws InterruptedException, ExecutionException {
		// Process output for this running process
		logger.debug("Processing output...");
		int exitCode = this.procExecDetails.getExitCode().get().getExitCode();
		logger.debug("Exit code {} of {}", exitCode, this.procExecDetails);
		this.publisher.submit(new IntegerTransfer(exitCode));
		this.publisher.close();
	}

	@Override
	public void onFinish(Function<Void, Void> f) {
		this.onFinishFunc = f;
	}

	public ProcessExecutionDetails getProcExecDetails() {
		return procExecDetails;
	}

	public void setProcExecDetails(ProcessExecutionDetails procExecDetails) {
		this.procExecDetails = procExecDetails;
	}

	public CompletableFuture<Boolean> getFutureReady() {
		return futureReady;
	}

	public void setFutureReady(CompletableFuture<Boolean> futureReady) {
		this.futureReady = futureReady;
	}

	public Function<Void, Void> getOnFinishFunc() {
		return onFinishFunc;
	}

	public void setOnFinishFunc(Function<Void, Void> onFinishFunc) {
		this.onFinishFunc = onFinishFunc;
	}

	@Override
	public String toString() {
		return "ExitCodeBinder [procExecDetails=" + procExecDetails + ", futureReady=" + futureReady + ", onFinishFunc="
				+ onFinishFunc + "]";
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
}
