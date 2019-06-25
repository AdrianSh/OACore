package es.jovenesadventistas.Arnion.ProcessExecutor.ProcessExecution;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import es.jovenesadventistas.Arnion.Process.AProcess;
import es.jovenesadventistas.Arnion.Process.Definitions.ExitCode;

public class ProcessExecutionDetails {
	private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger();
	
	private AProcess process;
	private AtomicBoolean executed;
	private CompletableFuture<Process> exitProcess;
	private CompletableFuture<ExitCode> exitCode;
	private CompletableFuture<Process> systemProcess;

	public ProcessExecutionDetails(AProcess processDef) {
		this.process = processDef;
		this.systemProcess = new CompletableFuture<Process>();
		this.executed = new AtomicBoolean(false);
		this.exitProcess = new CompletableFuture<Process>();
		this.exitCode = CompletableFuture.supplyAsync(() -> {
			logger.debug("Getting exit code... (asynch)");
			try {
				return new ExitCode(this.exitProcess.get().exitValue());
			} catch (InterruptedException | ExecutionException e) {
				logger.error("Exception when getting the Process.", e);
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
	
	public CompletableFuture<ExitCode> getExitCode() {
		return this.exitCode;
	}

	public CompletableFuture<Process> getSystemProcess() {
		return systemProcess;
	}
	
	public void setSystemProcess(Process proc) {
		this.systemProcess.complete(proc);
		this.exitProcess.completeAsync(() -> {
			try {
				return proc.onExit().get();
			} catch (InterruptedException | ExecutionException e) {
				logger.error("Couldn´t get the attached process on exit.",e);
				return null;
			}
		});
		
		// = proc.onExit();
	}

	@Override
	public String toString() {
		return "ProcessExecutionDetails [process=" + process + "]";
	}
}
