package es.jovenesadventistas.arnion.process.binders;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import es.jovenesadventistas.arnion.process.AProcess;
import es.jovenesadventistas.arnion.process.binders.Publishers.APublisher;
import es.jovenesadventistas.arnion.process.binders.Subscribers.ASubscriber;
import es.jovenesadventistas.arnion.process.binders.Transfers.StringTransfer;
import es.jovenesadventistas.arnion.process.persistence.TransferStore;
import es.jovenesadventistas.arnion.process_executor.ProcessExecution.ProcessExecutionDetails;

public class StdOutBinder implements Binder, ASubscriber<StringTransfer> {
	private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger();

	@Id
	private ObjectId id = new ObjectId();
	
	private TransferStore<StringTransfer> transfStore;
	private ProcessExecutionDetails procExecDetails;
	private CompletableFuture<Boolean> futureReady;
	private AtomicBoolean transferingStore;
	private CompletableFuture<Boolean> transferingStoreFlag;
	private OutputStream out;
	// private InputStream in;
	private Function<Void, Void> onFinishFunc;
	private APublisher subscription;
	private AProcess associatedProcess;

	public StdOutBinder(ProcessExecutionDetails procExecDetails, AProcess associatedProcess) {
		this.procExecDetails = procExecDetails;
		this.futureReady = new CompletableFuture<Boolean>();
		this.transferingStore = new AtomicBoolean(false);
		this.transferingStoreFlag = new CompletableFuture<Boolean>();
		this.transfStore = new TransferStore<StringTransfer>();
		this.onFinishFunc = null;
		this.out = null;
		// this.in = null;
		this.associatedProcess = associatedProcess;
	}

	@Override
	public void processInput() throws Exception {
		Process proc = this.procExecDetails.getSystemProcess().get();
		this.out = proc.getOutputStream();
		// this.in = proc.getInputStream();
		transferStore();
	}

	private void transferStore() throws IOException {
		if (this.transfStore.size() > 0) {
			this.transferingStore.set(true);
			for (StringTransfer t : this.transfStore) {
				this.out.write(t.getData().getBytes());
			}
			this.transfStore.clear();
			this.transferingStoreFlag.complete(true);
		}
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
		if(subscription instanceof APublisher) {
			this.subscription = (APublisher) subscription;
			this.futureReady.complete(true);
		} else {
			logger.error("Cannot onSubscribe using an unknown subscription. It should implements APublisher.");
		}
	}

	@Override
	public void onNext(StringTransfer item) {
		if (this.out == null) {
			this.transfStore.add(item);
		} else {
			try {
				if (this.transferingStore.get())
					try {
						this.transferingStoreFlag.get();
					} catch (InterruptedException | ExecutionException e) {
						logger.error(
								"Couldn't wait for the transfering of the String Transfer Store into the standard output stream.",
								e);
					}
				this.out.write(item.getData().getBytes());
				this.out.flush();
				
				logger.debug("Writting: {} on: {} of: {}", item.getData(), this.out, this.procExecDetails);

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
		logger.debug("Complete.");
		if(this.onFinishFunc != null) this.onFinishFunc.apply(null);
		/*
		try {
			this.transfStore.clear();
			this.out.close();
		} catch (IOException e) {
			logger.error("Error while closing the standard output stream.", e);
		}
		*/
	}
	
	@Override
	public void onFinish(Function<Void, Void> f) {
		this.onFinishFunc = f;
	}

	@Override
	public String toString() {
		return "StdOutBinder [procExecDetails=" + procExecDetails + ", futureReady=" + futureReady + ", out=" + out + ", subscription=" + subscription + "]";
	}

	@Override
	public ObjectId getId() {
		return this.id;
	}

	public TransferStore<StringTransfer> getTransfStore() {
		return transfStore;
	}

	public void setTransfStore(TransferStore<StringTransfer> transfStore) {
		this.transfStore = transfStore;
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

	public AtomicBoolean getTransferingStore() {
		return transferingStore;
	}

	public void setTransferingStore(AtomicBoolean transferingStore) {
		this.transferingStore = transferingStore;
	}

	public CompletableFuture<Boolean> getTransferingStoreFlag() {
		return transferingStoreFlag;
	}

	public void setTransferingStoreFlag(CompletableFuture<Boolean> transferingStoreFlag) {
		this.transferingStoreFlag = transferingStoreFlag;
	}

	public OutputStream getOut() {
		return out;
	}

	public void setOut(OutputStream out) {
		this.out = out;
	}

	public Function<Void, Void> getOnFinishFunc() {
		return onFinishFunc;
	}

	public void setOnFinishFunc(Function<Void, Void> onFinishFunc) {
		this.onFinishFunc = onFinishFunc;
	}

	public APublisher getSubscription() {
		return subscription;
	}

	public void setSubscription(APublisher subscription) {
		this.subscription = subscription;
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
