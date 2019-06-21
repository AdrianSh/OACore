package es.jovenesadventistas.oacore.ProcessExecutor.ProcessExecution;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import es.jovenesadventistas.oacore.process.ExitCodes;
import es.jovenesadventistas.oacore.process.MProcess;

public class BasicProcessExecution implements ProcessExecution {
	private MProcess processDefinition;
	private AtomicBoolean executed;
	private AtomicInteger exitCode;
	private Process process;
	
	public BasicProcessExecution(MProcess processDef) {
		this.processDefinition = processDef;
		this.executed = new AtomicBoolean();
		this.exitCode = new AtomicInteger(ExitCodes.NOTEXECUTED.getCode());
	}

	public MProcess getProcessDefinition() {
		return processDefinition;
	}

	public void setProcessDefinition(MProcess processDefinition) {
		this.processDefinition = processDefinition;
	}

	public AtomicBoolean getExecuted() {
		return executed;
	}

	public AtomicInteger getExitCode() {
		return exitCode;
	}

	public Process getProcess() {
		return process;
	}

	synchronized public void setProcess(Process process) {
		this.process = process;
	}
}
