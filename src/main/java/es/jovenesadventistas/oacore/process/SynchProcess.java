package es.jovenesadventistas.oacore.process;

import java.io.IOException;
import java.util.Map;

public class SynchProcess extends MProcess {

	public SynchProcess(String... p) {
		super(p);
		this.setExecutionMode(ExecutionMode.synchronous);
	}
	
	public void buildProcess(){
		pBuilder = new ProcessBuilder(this.getCommand());

		if (this.getModifiedEnvironment() != null) {
			Map<String, String> env = pBuilder.environment();
			env.putAll(this.getModifiedEnvironment());
		}

		if (this.getWorkingDirectory() != null)
			pBuilder.directory(this.getWorkingDirectory());
	
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
		return pBuilder.start();
	}

}
