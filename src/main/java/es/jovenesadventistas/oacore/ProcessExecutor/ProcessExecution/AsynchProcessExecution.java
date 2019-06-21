package es.jovenesadventistas.oacore.ProcessExecutor.ProcessExecution;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import es.jovenesadventistas.oacore.process.ExitCodes;
import es.jovenesadventistas.oacore.process.MProcess;

public class AsynchProcessExecution implements ProcessExecution {
	private MProcess processDefinition;
	private AtomicBoolean executed;
	private CompletableFuture<Process> exitProcess;
	private CompletableFuture<Integer> exitCode;
	private CompletableFuture<Process> process;

	public AsynchProcessExecution(MProcess processDef) {
		this.processDefinition = processDef;
		this.process = new CompletableFuture<Process>();
		this.executed = new AtomicBoolean(false);
		this.exitCode = CompletableFuture.supplyAsync(() -> {
			try {
				return process.get().exitValue();
			} catch (InterruptedException e) {
				e.printStackTrace();
				return ExitCodes.InterruptedException.getCode();
			} catch (ExecutionException e) {
				e.printStackTrace();
				return ExitCodes.ExecutionException.getCode();
			}
		});
		
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
	
	public void executed() {
		this.executed.set(true);
	}

	public Future<Integer> getExitCode() {
		return this.exitCode;
	}

	public Future<Process> getProcess() {
		return process;
	}
	
	public CompletableFuture<Process> getCompletableProcess() {
		return process;
	}
	
	public CompletableFuture<Integer> getCompletableExitCode() {
		return this.exitCode;
	}
}
