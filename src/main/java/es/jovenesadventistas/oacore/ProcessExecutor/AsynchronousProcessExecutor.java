package es.jovenesadventistas.oacore.ProcessExecutor;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import es.jovenesadventistas.oacore.ProcessExecutor.ProcessExecution.AsynchProcessExecution;
import es.jovenesadventistas.oacore.ProcessExecutor.ProcessExecution.ProcessExecution;
import es.jovenesadventistas.oacore.process.ExitCodes;

public class AsynchronousProcessExecutor implements ProcessExecutor {
	private static AsynchronousProcessExecutor instance;
	private AtomicBoolean running;

	private AsynchronousProcessExecutor() {
		this.running = new AtomicBoolean(false);
	}

	public static AsynchronousProcessExecutor getInstance() {
		if (instance == null)
			instance = new AsynchronousProcessExecutor();
		return instance;
	}

	@Override
	public void execute(ExecutorService executorService, ProcessExecution p) throws IOException {
		executorService.submit((Runnable) () -> {
			
			SET IT AS DAEMON THREAD
			
				try {
					System.out.println("Executing the process...");
					running.set(true);
					Process proc = p.getProcessDefinition().execute();
					((AsynchProcessExecution) p).getCompletableProcess().complete(proc);
					((AsynchProcessExecution) p).getCompletableExitCode().complete(proc.exitValue());
				} catch (IOException e) {
					((AsynchProcessExecution) p).getCompletableExitCode().complete(ExitCodes.IOException.getCode());
					e.printStackTrace();
				} finally {
					running.set(false);
					((AsynchProcessExecution) p).getExecuted().set(true);
				}
			
		});

	}

}
