package es.jovenesadventistas.Arnion.Process.Binders;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.concurrent.Future;
import java.util.concurrent.SubmissionPublisher;

import es.jovenesadventistas.Arnion.Process.Binders.Transfers.StreamTransfer;
import es.jovenesadventistas.Arnion.ProcessExecutor.ProcessExecution.ProcessExecutionDetails;

public class DirectStdInBinder extends SubmissionPublisher<StreamTransfer> implements Binder, Subscriber<StreamTransfer> {
	private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger();
	
	private ProcessExecutionDetails procExecDetails;
	private CompletableFuture<Boolean> futureReady;
	private AtomicBoolean ready;
	private AtomicBoolean join;
	private Subscription subscription;
	private Function<Void, Void> onFinishFunc;

	public DirectStdInBinder(ProcessExecutionDetails procExecDetails) {
		this.procExecDetails = procExecDetails;
		this.futureReady = new CompletableFuture<Boolean>();
		this.onFinishFunc = null;
	}

	@Override
	public void onNext(StreamTransfer item) {
		this.ready.set(true);
		this.futureReady.complete(true);
		
		try {
			InputStream in = item.getData().getKey();
			OutputStream out = this.procExecDetails.getSystemProcess().get().getOutputStream();

			byte[] result = in.readAllBytes();
			
			do {
				out.write(result);
				result = in.readAllBytes();
			} while(result != null && result.length > 0);
			
			this.close();
			this.onFinishFunc.apply(null);
			
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
		logger.debug("Direct StdIn binder running, {}", this.procExecDetails);
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
		
		if (this.subscription != null) {
			this.subscription.request(1L);
		}
	}

	@Override
	public void processOutput() throws InterruptedException, ExecutionException {
		// Process output for this running process
		logger.debug("Processing output...");
		super.submit(new StreamTransfer(this.procExecDetails.getSystemProcess().get().getInputStream(),
				this.procExecDetails.getSystemProcess().get().getErrorStream()));
	}

	@Override
	public void onSubscribe(Subscription subscription) {
		this.subscription = subscription;
	}

	@Override
	public void subscribe(Subscriber<? super StreamTransfer> subscriber) {
		super.subscribe(subscriber);
	}

	@Override
	public void onError(Throwable throwable) {
		logger.error("An error ocurred while receiving a StreamTransfer for the StdIn.", throwable);
	}

	@Override
	public void onComplete() {
		// This could be useful for stop the reading of all the bytes from the stdin.
		logger.debug("Complete... no more transfers to receive.");
	}

	@Override
	public boolean ready() {
		return this.ready.get();
	}

	@Override
	public boolean joined() {
		return this.join.get();
	}

	@Override
	public void markAsReady() {
		this.ready.set(true);
		this.futureReady.complete(true);
	}
	
	@Override
	public void onFinish(Function<Void, Void> f) {
		this.onFinishFunc = f;
	}

	@Override
	public String toString() {
		return "DirectStdInBinder [procExecDetails=" + procExecDetails + ", futureReady=" + futureReady + ", ready="
				+ ready + ", join=" + join + ", subscription=" + subscription + "]";
	}

	
}
