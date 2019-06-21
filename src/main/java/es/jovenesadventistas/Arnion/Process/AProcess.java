/**
 * 
 */
package es.jovenesadventistas.Arnion.Process;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.jovenesadventistas.Arnion.Process.Definitions.DeliverableType;
import es.jovenesadventistas.Arnion.Process.Definitions.ExecutionMode;

import java.util.Map;

/**
 * @author Adrian E. Sanchez Hurtado
 *
 */
public abstract class AProcess {
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(AProcess.class);
	protected ArrayList<String> command;
	protected File workingDirectory;
	protected ExecutionMode executionMode;
	protected Set<DeliverableType> supportedInputs;
	protected Set<DeliverableType> supportedOutputs;
	protected Map<String, String> modifiedEnvironment;
	protected boolean inheritIO;
	protected ProcessBuilder pBuilder;
	

	public AProcess(String ...strings ) {
		this.command = new ArrayList<String>(Arrays.asList(strings));
		this.executionMode = ExecutionMode.synchronous;
		this.supportedInputs = Set.of(DeliverableType.CompleteDeliverable);
		this.supportedOutputs = Set.of(DeliverableType.CompleteDeliverable);
		this.setInheritIO(false);
	}

	abstract public java.lang.Process execute() throws IOException;
	
	
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

	public ExecutionMode getExecutionMode() {
		return executionMode;
	}

	public void setExecutionMode(ExecutionMode executionMode) {
		this.executionMode = executionMode;
	}

	public Set<DeliverableType> getSupportedOutputs() {
		return supportedOutputs;
	}

	public void setSupportedOutputs(Set<DeliverableType> supportedOutputs) {
		this.supportedOutputs = supportedOutputs;
	}

	public Map<String, String> getModifiedEnvironment() {
		return modifiedEnvironment;
	}

	public void setModifiedEnvironment(Map<String, String> modifiedEnvironment) {
		this.modifiedEnvironment = modifiedEnvironment;
	}

	public Set<DeliverableType> getSupportedInputs() {
		return supportedInputs;
	}

	public void setSupportedInputs(Set<DeliverableType> supportedInputs) {
		this.supportedInputs = supportedInputs;
	}

	public boolean isInheritIO() {
		return inheritIO;
	}

	public void setInheritIO(boolean inheritIO) {
		this.inheritIO = inheritIO;
	}

	@Override
	public String toString() {
		return "AProcess [command=" + command + ", workingDirectory=" + workingDirectory + ", executionMode="
				+ executionMode + ", supportedInputs=" + supportedInputs + ", supportedOutputs=" + supportedOutputs
				+ ", modifiedEnvironment=" + modifiedEnvironment + "]";
	}
	
	
}
