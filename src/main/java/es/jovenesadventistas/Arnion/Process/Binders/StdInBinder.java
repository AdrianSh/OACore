package es.jovenesadventistas.Arnion.Process.Binders;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.SubmissionPublisher;

import es.jovenesadventistas.Arnion.Process.Binders.Transfers.StringTransfer;
import es.jovenesadventistas.Arnion.ProcessExecutor.ProcessExecution.ProcessExecutionDetails;

public class StdInBinder implements Binder {
	private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger();

	private ProcessExecutionDetails procExecDetails;
	private CompletableFuture<Boolean> futureReady;
	private SubmissionPublisher<StringTransfer> publisher;
	private InputStream in;
	private InputStream inError;

	public StdInBinder(ProcessExecutionDetails procExecDetails, SubmissionPublisher<StringTransfer> publisher) {
		this.procExecDetails = procExecDetails;
		this.futureReady = new CompletableFuture<Boolean>();
		this.publisher = publisher;
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

		try {
			final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String line = null;
			while ((line = reader.readLine()) != null) {
				this.publisher.submit(new StringTransfer(line));
				System.out.println(line);
				System.out.println("----------------------------------------");
			}
			reader.close();
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
}
