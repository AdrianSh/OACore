/**
 * 
 */
package es.jovenesadventistas.oacore.process;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.Map;

/**
 * @author Adrian E. Sanchez Hurtado
 *
 */
public abstract class MProcess {
	private ArrayList<String> command;
	private File workingDirectory;
	private ExecutionMode executionMode;
	private Set<DeliverableType> supportedInputs;
	private Set<DeliverableType> supportedOutputs;
	private Map<String, String> modifiedEnvironment;
	private boolean inheritIO;
	protected ProcessBuilder pBuilder;
	

	public MProcess(String ...strings ) {
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
}
