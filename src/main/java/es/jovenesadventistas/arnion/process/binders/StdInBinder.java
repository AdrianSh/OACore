package es.jovenesadventistas.arnion.process.binders;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.SubmissionPublisher;
import java.util.function.Function;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import es.jovenesadventistas.arnion.process_executor.ProcessExecution.ProcessExecutionDetails;
import es.jovenesadventistas.arnion.process.AProcess;
import es.jovenesadventistas.arnion.process.binders.Publishers.APublisher;
import es.jovenesadventistas.arnion.process.binders.Transfers.StringTransfer;

public class StdInBinder implements Binder {
	private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger();
	@Id
	private ObjectId id = new ObjectId();
	private ProcessExecutionDetails procExecDetails;
	private CompletableFuture<Boolean> futureReady;
	private SubmissionPublisher<StringTransfer> stdInPublisher, stdInErrorPublisher;
	private InputStream in;
	private InputStream inError;
	private Function<Void, Void> onFinishFunc;
	private AProcess associatedProcess;

	public StdInBinder(ProcessExecutionDetails procExecDetails, SubmissionPublisher<StringTransfer> stdInPublisher,
			SubmissionPublisher<StringTransfer> stdInErrorPublisher, AProcess associatedProcess) {
		this.procExecDetails = procExecDetails;
		this.futureReady = new CompletableFuture<Boolean>();
		this.stdInPublisher = stdInPublisher;
		this.stdInErrorPublisher = stdInErrorPublisher;
		this.onFinishFunc = null;
		this.associatedProcess = associatedProcess;
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
	public ObjectId getId() {
		return this.id;
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

	public SubmissionPublisher<StringTransfer> getStdInPublisher() {
		return stdInPublisher;
	}

	public void setStdInPublisher(SubmissionPublisher<StringTransfer> stdInPublisher) {
		this.stdInPublisher = stdInPublisher;
	}
	
	public APublisher getStdInAPublisher() throws Exception {
		if (this.stdInPublisher == null || this.stdInPublisher instanceof APublisher)
			return (APublisher) this.stdInPublisher;
		else
			throw new Exception("It is not an instance of APublisher.");
	}
	
	public APublisher getStdInErrorAPublisher() throws Exception {
		if (this.stdInErrorPublisher == null || this.stdInErrorPublisher instanceof APublisher)
			return (APublisher) this.stdInErrorPublisher;
		else
			throw new Exception("It is not an instance of APublisher.");
	}

	public SubmissionPublisher<StringTransfer> getStdInErrorPublisher() {
		return stdInErrorPublisher;
	}

	public void setStdInErrorPublisher(SubmissionPublisher<StringTransfer> stdInErrorPublisher) {
		this.stdInErrorPublisher = stdInErrorPublisher;
	}

	public InputStream getIn() {
		return in;
	}

	public void setIn(InputStream in) {
		this.in = in;
	}

	public InputStream getInError() {
		return inError;
	}

	public void setInError(InputStream inError) {
		this.inError = inError;
	}

	public Function<Void, Void> getOnFinishFunc() {
		return onFinishFunc;
	}

	public void setOnFinishFunc(Function<Void, Void> onFinishFunc) {
		this.onFinishFunc = onFinishFunc;
	}

	public void setId(ObjectId id) {
		if (id != null)
			this.id = id;
	}

	@Override
	public void setAProcess(AProcess proc) {
		this.associatedProcess = proc;
	}

	@Override
	public AProcess getAProcess() {
		return associatedProcess;
	}
}
