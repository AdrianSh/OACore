/**
 * 
 */
package es.jovenesadventistas.arnion.process;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

/**
 * @author Adrian E. Sanchez Hurtado
 * @param <S>
 * @param <T>
 *
 */
public class AProcess {
	private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger();
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
		return "AProcess [command=" + command + ", workingDirectory=" + workingDirectory + ", modifiedEnvironment="
				+ modifiedEnvironment + "]";
	}
}
