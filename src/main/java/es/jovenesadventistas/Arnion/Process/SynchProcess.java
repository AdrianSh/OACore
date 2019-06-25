package es.jovenesadventistas.Arnion.Process;

import java.io.IOException;
import java.util.Map;


import es.jovenesadventistas.Arnion.Process.Definitions.ExecutionMode;

public class SynchProcess extends AProcess {
	private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger();
	
	public SynchProcess(String... p) {
		super(p);
		this.setExecutionMode(ExecutionMode.synchronous);
	}
	
	public void buildProcess(){
		logger.debug("Building process {}", this);
		pBuilder = new ProcessBuilder(this.command);

		if (this.modifiedEnvironment != null) {
			logger.debug("Applying environment changes... {}", this.modifiedEnvironment);
			Map<String, String> env = pBuilder.environment();
			env.putAll(this.modifiedEnvironment);
		}

		if (this.workingDirectory != null)
			pBuilder.directory(this.workingDirectory);
	
		if(this.isInheritIO())
			pBuilder.inheritIO();
		else {
			pBuilder.redirectInput();
			pBuilder.redirectError();
			pBuilder.redirectOutput();
		}
	}

	@Override
	public java.lang.Process execute() throws IOException {
		this.buildProcess();
		logger.debug("Starting process... {}", this.command);
		return pBuilder.start();
	}

}
