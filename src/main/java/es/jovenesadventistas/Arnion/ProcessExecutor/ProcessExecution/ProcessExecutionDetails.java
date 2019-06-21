package es.jovenesadventistas.Arnion.ProcessExecutor.ProcessExecution;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.LoggerFactory;

import es.jovenesadventistas.Arnion.Process.AProcess;
import es.jovenesadventistas.Arnion.Process.Definitions.ExitCode;

public class ProcessExecutionDetails {
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ProcessExecutionDetails.class);
	
	private AProcess process;
	private AtomicBoolean executed;
	private CompletableFuture<ExitCode> exitCode;
	private CompletableFuture<Process> systemProcess;

	public ProcessExecutionDetails(AProcess processDef) {
		this.process = processDef;
		this.systemProcess = new CompletableFuture<Process>();
		this.executed = new AtomicBoolean(false);
		this.exitCode = CompletableFuture.supplyAsync(() -> {
			logger.debug("Getting exit code... (asynch)");
			try {
				return new ExitCode(systemProcess.get().exitValue());
			} catch (InterruptedException | ExecutionException e) {
				logger.debug("Exception when getting the Process.");
				return new ExitCode(e);
			}
		});
		
	}

	public AProcess getProcess() {
		return process;
	}

	public void setProcess(AProcess processDefinition) {
		this.process = processDefinition;
	}

	public boolean isExecuted() {
		return executed.get();
	}
	
	public void executed() {
		this.executed.set(true);
	}

	public synchronized void setExitCode(ExitCode exitCode) {
		logger.debug("Setting exit code %s", exitCode);
		this.exitCode.complete(exitCode);
	}
	
	public Future<ExitCode> getExitCode() {
		return this.exitCode;
	}

	public CompletableFuture<Process> getSystemProcess() {
		return systemProcess;
	}
	
	public void setSystemProcess(Process proc) {
		this.systemProcess.complete(proc);
	}

	@Override
	public String toString() {
		return "ProcessExecutionDetails [process=" + process + "]";
	}
}
