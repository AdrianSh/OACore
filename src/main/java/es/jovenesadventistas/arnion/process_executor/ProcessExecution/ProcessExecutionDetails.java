package es.jovenesadventistas.arnion.process_executor.ProcessExecution;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import es.jovenesadventistas.arnion.process.AProcess;
import es.jovenesadventistas.arnion.process.binders.Binder;
import es.jovenesadventistas.arnion.process.definitions.ExitCode;

public class ProcessExecutionDetails {
	private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger();

	private AProcess process;
	private AtomicBoolean executed;
	private ExitCode exitCode;
	private CompletableFuture<Process> systemProcess;
	private Binder binder;

	public ProcessExecutionDetails(AProcess processDef) {
		this.process = processDef;
		this.systemProcess = new CompletableFuture<Process>();
		this.executed = new AtomicBoolean(false);
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

	public synchronized void setExitCode(ExitCode exitCode) throws CancellationException {
		logger.debug("Setting exit code {}", exitCode.toString());
		this.exitCode = exitCode;
		this.systemProcess.complete(null);
	}

	public CompletableFuture<ExitCode> getExitCode() {
		return CompletableFuture.supplyAsync(() -> {
			logger.debug("Getting exit code... (asynch)");
			try {
				if(this.exitCode != null)
					return this.exitCode;
				return new ExitCode(this.systemProcess.get().onExit().get().exitValue());
			} catch (InterruptedException | ExecutionException e) {
				logger.error("Exception when getting the Process.", e);
				return new ExitCode(e);
			}
		});
	}

	public CompletableFuture<Process> getSystemProcess() {
		return systemProcess;
	}

	public void setSystemProcess(Process proc) {
		this.systemProcess.complete(proc);
	}

	public Binder getBinder() {
		return binder;
	}

	public void setBinder(Binder binder) {
		this.binder = binder;
	}

	@Override
	public String toString() {
		return "ProcessExecutionDetails [process=" + process + "]";
	}
}
