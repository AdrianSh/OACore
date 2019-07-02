package es.jovenesadventistas.Arnion.ProcessExecutor;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import es.jovenesadventistas.Arnion.Process.Binders.Binder;
import es.jovenesadventistas.Arnion.Process.Binders.Transfers.Transfer;
import es.jovenesadventistas.Arnion.Process.Definitions.ExitCode;
import es.jovenesadventistas.Arnion.Process.Definitions.ExitCode.ExitCodes;
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

	public <T extends Transfer, S extends Transfer> void execute(ExecutorService executorService, Binder<T, S> binder) {
		executorService.submit(() -> {
			logger.debug("Running binder... {}", binder);
			binder.run();
			logger.debug("Binder ends... {}", binder);
		});
	}

	public <T extends Transfer, S extends Transfer> void execute(ExecutorService executorService,
			ProcessExecutionDetails<T, S> p) throws IOException {
		executorService.submit((Runnable) () -> {
			if (p.getBinder() != null && p.getBinder().ready()) {
				this._execute(executorService, p);
			} else {
				try {
					logger.debug("Waiting for an asynch. ready response from the binder... {}", p);
					if (p.getBinder().asynchReady().get()) {
						this._execute(executorService, p);
					} else {
						logger.warn("Cannot execute the process because it's binder is not ready: {}", p);
						p.setExitCode(new ExitCode(ExitCodes.NOTBINDERREADY));
					}
				} catch (CancellationException | InterruptedException | ExecutionException e) {
					logger.error("Couldn't wait for binder to be ready.", e);
					p.setExitCode(new ExitCode(e));
				}
			}
		});
	}

	private <T extends Transfer, S extends Transfer> void _execute(ExecutorService executorService,
			ProcessExecutionDetails<T, S> p) {
		try {
			// Thread.currentThread().setDaemon(true); // It's a daemon thread by default
			// (or could be eclipse IDE)

			logger.debug("Executing the process {}", p);
			running.set(true);
			Process proc = p.getProcess().execute();
			p.setSystemProcess(proc);
		} catch (IOException e) {
			p.setExitCode(new ExitCode(e));
		} finally {
			running.set(false);
			p.executed();
		}
	}
}
