package es.jovenesadventistas.Arnion.ProcessExecutor;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import es.jovenesadventistas.Arnion.Process.Definitions.ExitCode;
import es.jovenesadventistas.Arnion.ProcessExecutor.ProcessExecution.ProcessExecutionDetails;

/**
 * 
 * @author Adrian E. Sanchez Hurtado
 *
 */
public class ProcessExecutor {
	private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger();
	
	private static ProcessExecutor instance;
	private AtomicBoolean running;

	public ProcessExecutor() {
		this.running = new AtomicBoolean(false);
	}

	public static ProcessExecutor getInstance() {
		if (instance == null)
			instance = new ProcessExecutor();
		return instance;
	}

	public void execute(ExecutorService executorService, ProcessExecutionDetails p) throws IOException {
		executorService.submit((Runnable) () -> {
				Thread.currentThread().setDaemon(true);
			
				try {
					logger.debug("Executing the process %s", p);
					running.set(true);
					Process proc = p.getProcess().execute();
					p.setSystemProcess(proc);
				} catch (IOException e) {
					p.setExitCode(new ExitCode(e));
				} finally {
					running.set(false);
					p.executed();
				}
		});
	}
}
