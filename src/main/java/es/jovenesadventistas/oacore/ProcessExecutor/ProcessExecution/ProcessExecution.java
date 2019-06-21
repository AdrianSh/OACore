package es.jovenesadventistas.oacore.ProcessExecutor.ProcessExecution;

import es.jovenesadventistas.oacore.process.MProcess;

public interface ProcessExecution {
	public MProcess getProcessDefinition();
	public void setProcessDefinition(MProcess processDefinition);
}
