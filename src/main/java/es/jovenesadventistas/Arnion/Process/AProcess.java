/**
 * 
 */
package es.jovenesadventistas.Arnion.Process;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import es.jovenesadventistas.Arnion.Process.Binders.Binder;
import es.jovenesadventistas.Arnion.Process.Binders.Transfers.Transfer;
import es.jovenesadventistas.Arnion.Process.Definitions.DeliverableType;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

/**
 * @author Adrian E. Sanchez Hurtado
 *
 */
public class AProcess<T extends Transfer, S extends Transfer> implements Subscriber<T>, Publisher<S> {
	private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger();
	private Subscription inputSubscription;
	
	https://www.baeldung.com/java-9-reactive-streams
		
		
	private Set<Subscriber<S>> outputSubscribers;
	private ArrayList<String> command;
	private File workingDirectory; 
	private Map<String, String> modifiedEnvironment;
	private boolean inheritIO;
	private ProcessBuilder pBuilder;

	public AProcess(String... strings) {
		this.command = new ArrayList<String>(Arrays.asList(strings));
		this.setInheritIO(false);
	}

	public void buildProcess() {
		logger.debug("Building process {}", this);
		pBuilder = new ProcessBuilder(this.command);

		if (this.modifiedEnvironment != null) {
			logger.debug("Applying environment changes... {}", this.modifiedEnvironment);
			Map<String, String> env = pBuilder.environment();
			env.putAll(this.modifiedEnvironment);
		}

		if (this.workingDirectory != null)
			pBuilder.directory(this.workingDirectory);

		if (this.isInheritIO())
			pBuilder.inheritIO();
		else {
			pBuilder.redirectInput();
			pBuilder.redirectError();
			pBuilder.redirectOutput();
		}
	}

	public java.lang.Process execute() throws IOException {
		this.buildProcess();
		logger.debug("Starting process... {}", this.command);
		return pBuilder.start();
	}

	@Override
	public void onSubscribe(Subscription inputSubscription) {
		this.inputSubscription = inputSubscription;
		inputSubscription.request(1);
	}
	
	@Override
	public void onNext(T item) {
	    System.out.println("Got : " + item);
	    inputSubscription.request(1);
	}
	
	@Override
	public void onError(Throwable t) {
	    t.printStackTrace();
	}
	 
	@Override
	public void onComplete() {
	    System.out.println("Done");
	}
	
	@Override
	public void subscribe(Subscriber<? super S> subscriber) {
		// TODO Auto-generated method stub
		
	}

	public ArrayList<String> getCommand() {
		return command;
	}

	public void setCommand(ArrayList<String> command) {
		this.command = command;
	}

	public File getWorkingDirectory() {
		return workingDirectory;
	}

	public void setWorkingDirectory(File workingDirectory) {
		this.workingDirectory = workingDirectory;
	}

	public HashMap<DeliverableType, Binder> getSupportedOutputs() {
		return supportedOutputs;
	}

	public void setSupportedOutputs(HashMap<DeliverableType, Binder> supportedOutputs) {
		this.supportedOutputs = supportedOutputs;
	}

	public Map<String, String> getModifiedEnvironment() {
		return modifiedEnvironment;
	}

	public void setModifiedEnvironment(Map<String, String> modifiedEnvironment) {
		this.modifiedEnvironment = modifiedEnvironment;
	}

	public boolean isInheritIO() {
		return inheritIO;
	}

	public void setInheritIO(boolean inheritIO) {
		this.inheritIO = inheritIO;
	}

	@Override
	public String toString() {
		return "AProcess [command=" + command + ", workingDirectory=" + workingDirectory + ", inputSubscription="
				+ inputSubscription + ", supportedOutputs=" + supportedOutputs + ", modifiedEnvironment="
				+ modifiedEnvironment + "]";
	}
}
