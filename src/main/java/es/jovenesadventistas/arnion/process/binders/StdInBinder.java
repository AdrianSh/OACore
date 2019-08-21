package es.jovenesadventistas.arnion.process.binders;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.SubmissionPublisher;
import java.util.function.Function;

import es.jovenesadventistas.arnion.process_executor.ProcessExecution.ProcessExecutionDetails;
import es.jovenesadventistas.arnion.process.binders.Transfers.StringTransfer;

public class StdInBinder implements Binder {
	private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger();

	private ProcessExecutionDetails procExecDetails;
	private CompletableFuture<Boolean> futureReady;
	private SubmissionPublisher<StringTransfer> stdInPublisher, stdInErrorPublisher;
	private InputStream in;
	private InputStream inError;
	private Function<Void, Void> onFinishFunc;

	public StdInBinder(ProcessExecutionDetails procExecDetails, SubmissionPublisher<StringTransfer> stdInPublisher,
			SubmissionPublisher<StringTransfer> stdInErrorPublisher) {
		this.procExecDetails = procExecDetails;
		this.futureReady = new CompletableFuture<Boolean>();
		this.stdInPublisher = stdInPublisher;
		this.stdInErrorPublisher = stdInErrorPublisher;
		this.onFinishFunc = null;
	}

	@Override
	public void processInput() throws Exception {
		Process proc = this.procExecDetails.getSystemProcess().get();
		this.in = proc.getInputStream();
		this.inError = proc.getErrorStream();
	}

	@Override
	public void processOutput() throws Exception {
		this.readInputStream(this.in, this.stdInPublisher);
		this.readInputStream(this.inError, this.stdInErrorPublisher);
		if (this.onFinishFunc != null)
			this.onFinishFunc.apply(null);
	}

	private void readInputStream(InputStream in, SubmissionPublisher<StringTransfer> publisher) {
		try {
			if (in != null && publisher != null) {
				final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				String line = null;
				while ((line = reader.readLine()) != null) {
					publisher.submit(new StringTransfer(line));
				}
				reader.close();
				publisher.close();
			} else {
				logger.info("Cannot read from a null InputStream {}.", in);
			}
		} catch (final Exception e) {
			e.printStackTrace();
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
	public void onFinish(Function<Void, Void> f) {
		this.onFinishFunc = f;
	}

	@Override
	public String toString() {
		return "StdInBinder [procExecDetails=" + procExecDetails + ", futureReady=" + futureReady + ", stdInPublisher="
				+ stdInPublisher + ", stdInErrorPublisher=" + stdInErrorPublisher + ", in=" + in + ", inError="
				+ inError + "]";
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
