package es.jovenesadventistas.Arnion.Process;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.jovenesadventistas.Arnion.Process.Definitions.ExecutionMode;

public class SynchProcess extends AProcess {
	private static final Logger logger = LoggerFactory.getLogger(SynchProcess.class);
	
	public SynchProcess(String... p) {
		super(p);
		this.setExecutionMode(ExecutionMode.synchronous);
	}
	
	public void buildProcess(){
		logger.debug("Building process %s", this);
		pBuilder = new ProcessBuilder(this.command);

		if (this.modifiedEnvironment != null) {
			logger.debug("Applying environment changes... %s", this.modifiedEnvironment);
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
		logger.debug("Starting process... %s", this.command);
		return pBuilder.start();
	}

}
